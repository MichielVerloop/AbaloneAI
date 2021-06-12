package controller;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import model.artificialintelligence.minimax.GameStateEvaluator;
import model.artificialintelligence.minimax.Minimax;
import model.gamelogic.GameState;
import model.gamelogic.GameState.Game;
import model.gamelogic.Move;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;
import model.gamelogic.ReplayPlayer;
import model.gamelogic.StartingLayout;
import view.TerminalUI;
import view.UI;


/**
 * Class that handles the flow for the Abalone game.
 * @author Michiel Verloop
 */
public class Abalone {
	//

    public GameState gameState;
    private List<Player> players;
    private StartingLayout layout;
    private Game replayGame;
    private File outputFile;
    private File statsFile;
    
    /**
     * Creates a new game from a list of players.
     * @requires players != null, 2 <= players.size() <= 4, elements in player != null
     * @invariant this.gameState != null
     * @param players The players that will participate in the game.
     * @param layout The layout that will be used for the game.
     * @param inputFile The file which determines which moves are made in replayMode.
     * @param outputFile The file to which this match is saved. 
     * @param statsFile The file to which the stats of this match are saved.
     * @throws FileNotFoundException Throws FileNotFoundException if the inputfile is not found.
     */
    private Abalone(List<Player> players, StartingLayout layout, Game replayGame, File outputFile, File statsFile) throws FileNotFoundException {
        this.players = players;
        this.layout = layout;
        this.outputFile = outputFile;
        this.statsFile = statsFile;
        this.replayGame = replayGame;         
    }
    
    /**
     * Handles the game flow in the case that it is played locally.
     * @throws IOException If writing the stats or game fails.
     */
    void playLocal() throws IOException {
    	gameState = new GameState(players, layout);
        
    	while (!gameState.isFinished()) {
            showGameState(gameState);
            // Determine move
            PlayableMove move = getNextMove();
            // Apply the move.
            gameState.makeMove(move);
            gameState.gameStats.commitTurn();
        }
        showGameResult(gameState);
        saveGame();
    }

    
    /**
     * Requests the move to be played from the current player. In replay mode, this will let the player compute the their move,
     * yet use the original one. If the player is a replay player, no move will be computed and the original move will be used.
     * @return The next move to be made in the game.
     */
    PlayableMove getNextMove() {
    	Player currentPlayer = gameState.getCurrentPlayer();
    	
    	// Compute the move if the player is not a replay player.
    	gameState.gameStats.initializeTurn();
    	PlayableMove move = null;
    	long time = -1;
    	if (!(currentPlayer instanceof ReplayPlayer)) {
    		time = System.nanoTime();
            move = gameState.getCurrentPlayer().determineMove(gameState);
            time = (System.nanoTime() - time) / 1000000;
    	}
    	gameState.gameStats.registerTiming(time);
    	
    	// Return the computed move if it is not a replay game. Otherwise, return the original move.
    	if (replayGame == null) {
    		return move;
    	} else {
    		return Move.newMove(gameState.getBoard(), 
    				replayGame.moves.get(gameState.getTurn()), 
    				gameState.getCurrentPlayer());
    	}
    }
    
	/**
	 * Saves the game and its stats to the outputFile and the statsFile if it they are present. 
	 * If this fails, the output is printed to the console instead.
	 * @throws IOException if writing the csv file fails.
	 */
	void saveGame() throws IOException {
		if (outputFile != null) {
    		try {
    			Json.serializeGame(gameState.gameHistory, new FileWriter(outputFile));
    		} catch (IOException e) {
    			System.err.println("The program was unable to serialize the game to the output file"
    					+ " so it is being printed to the console instead.");
    			Json.serializeGame(gameState.gameHistory, new OutputStreamWriter(System.out));
    		}
        }
		if (statsFile != null) {
			try {
				gameState.gameStats.writeToCSV(new FileWriter(statsFile));
			} catch (IOException e) {
				System.err.println("The program was unable to serialize the stats to the output file"
						+ " so it is being printed to the console instead.");
				try {
					gameState.gameStats.writeToCSV(new OutputStreamWriter(System.out));
				} catch (IOException ee) {
					System.err.println("The program was also unable to print the stats to the console.");
					throw ee;
				}
				throw e;
			}
		}
	}
    
    
    /**
     * Starts a local game, querying the user for how many players will participate,
     * the names for these players and the types for these players.
     * @throws IOException 
     */
    public static void startLocalGame(StartingLayout layout) throws IOException {
        if (UI.getInstance() == null) {
            initializeUI();
        }
        int nrOfPlayers = Integer.parseInt(
                getInputSatisfying(
                    "How many players will participate in the game?",
                    "Please enter 2, 3 or 4 players for 2-, 3- and 4-player games "
                    + "respectively.",
                    (input -> input.matches("[234]")))
                );
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < nrOfPlayers; i++) {
            players.add(constructPlayer());
        }

        Abalone game = new Abalone.Builder()
        		.withPlayers(players)
        		.withStartingLayout(layout)
        		.build();
        game.playLocal();
    }

    /*
     * Provides the user with the latest information about the gameState.
     */
    private static void showGameState(GameState gameState) {
        UI.getInstance().updateGameState(gameState, gameState.getCurrentPlayer());
    }

    /**
     * Shows the user the game result when the game is over.
     * @requires gameState.isFinished()
     */
    private static void showGameResult(GameState gameState) {
        UI.getInstance().showGameResult(gameState, gameState.getWinner());
    }

    /**
     * Queries the user for a valid playername and player type until they give it.
     * @return String[] with the first argument being name and the second argument being playerType.
     * @ensures getPlayer()[0] is a valid name, getPlayer()[1] is a valid type.
     */
    public static Player constructPlayer() {
        String name = getInputSatisfying(
                "Creating a new player: What will their name be?",
                "The name may only consist of alphabetic characters, "
                        + "digits, commas, dots, spaces and colons.",
                (input -> input.matches(Player.PLAYER_NAME_PATTERN)));
        String type = getInputSatisfying(
                "What type of player is " + name + "?",
                "Types can only be \"human\", \"random\" \"aggressive\", and \"minimax\".",
                (input -> input.matches("(?i)(human|aggressive|random|minimax)")));
        if (type.matches("(?i)human|aggressive|random")) {
        	return Player.newPlayer(name, type);
        }
        
        Minimax.Builder miniBuilder = constructMinimaxBuilder(name);
        GameStateEvaluator evaluator = constructGameStateEvaluator(name);
        
        return Player.newPlayer(name, miniBuilder, evaluator);
    }
    
    private static Minimax.Builder constructMinimaxBuilder(String playername) {
    	Minimax.Builder miniBuilder = new Minimax.Builder();
        
        String searchAlgorithm = getInputSatisfying(
        		"Will " + playername + " use dfs or iddfs as their search algorithm?",
        		"Available search algorithms are \"dfs\" and \"iddfs\".",
        		(input -> input.matches("(?i)(dfs|iddfs)")));
        switch (searchAlgorithm.toLowerCase()) {
        	case "dfs":
        		miniBuilder.withDfs(Integer.valueOf(getInputSatisfying(
						"Till which depth should the DFS search?",
						"Depth should be an integer between 1 and 8, inclusive.",
						(input -> input.matches("[12345678]")))));
        		break;
        	case "iddfs":
        		miniBuilder.withTimeBoundIddfs(Integer.valueOf(getInputSatisfying(
						"How long should the IDDFS search for each turn?",
						"The time limit should be a positive number of seconds.",
						(input -> isInteger(input) && Integer.parseInt(input) > 0))));
        		break;
        	default:
        		throw new IllegalStateException("Fatal Error. Regex failed to catch the "
        				+ "following user input: " + searchAlgorithm + ".");
        }
        
        if ("yes".equalsIgnoreCase(getInputSatisfying(
        		"Should the hashing heuristic be enabled?",
        		"Answer should be \"yes\" or \"no\".",
        		(input -> input.matches("(?i)yes|no"))))) {
        	miniBuilder.enableHashing();
        }
        if ("yes".equalsIgnoreCase(getInputSatisfying(
        		"Should the marble ordering heuristic be enabled?",
        		"Answer should be \"yes\" or \"no\".",
        		(input -> input.matches("(?i)yes|no"))))) {
        	int minDepth = Integer.parseInt(getInputSatisfying(
            		"From which depth should the marble ordering heuristic be enabled?",
            		"Answer should be a depth greater than 0.",
            		(input -> isInteger(input) && Integer.parseInt(input) > 0)));
        	int maxDepth = Integer.parseInt(getInputSatisfying(
        			"Till which depth should the marble ordering heuristic be enabled?",
            		"Answer should be a depth greater or equal to the minimum depth that"
            		+ "you just entered: " + minDepth + ".",
            		(input -> isInteger(input) && Integer.parseInt(input) >= minDepth)));
        	miniBuilder.enableMarbleOrdering(minDepth, maxDepth);
        }
        if ("yes".equalsIgnoreCase(getInputSatisfying(
        		"Should the evaluate sorting heuristic be enabled?",
        		"Answer should be \"yes\" or \"no\".",
        		(input -> input.matches("(?i)yes|no"))))) {
        	int minDepth = Integer.parseInt(getInputSatisfying(
            		"From which depth should the evaluate sorting heuristic be enabled?",
            		"Answer should be a depth greater than 0.",
            		(input -> isInteger(input) && Integer.parseInt(input) > 0)));
        	int maxDepth = Integer.parseInt(getInputSatisfying(
        			"Till which depth should the evaluate sorting heuristic be enabled?",
            		"Answer should be a depth greater or equal to the minimum depth that"
            		+ "you just entered: " + minDepth + ".",
            		(input -> isInteger(input) && Integer.parseInt(input) >= minDepth)));
        	miniBuilder.enableMarbleOrdering(minDepth, maxDepth);
        	miniBuilder.enableEvaluateSorting(minDepth, maxDepth);
        }
        return miniBuilder;
    }
    
    private static GameStateEvaluator constructGameStateEvaluator(String playername) {
    	GameStateEvaluator.Builder evaluatorBuilder = new GameStateEvaluator.Builder();
    	evaluatorBuilder.withMarbleConqueredWeight(Integer.parseInt(getInputSatisfying(
    			"What will the weight for taking out a marble be?",
    			"Weight must be between 0 and " + GameStateEvaluator.MAX_WEIGHT + " (inclusive).",
    			(input -> isInteger(input) 
    					&& Integer.parseInt(input) >= 0 
    					&& Integer.parseInt(input) <= GameStateEvaluator.MAX_WEIGHT))));
    	evaluatorBuilder.withDistanceFromCenterWeight(Integer.parseInt(getInputSatisfying(
    			"What will the weight for distance to center be?",
    			"Weight must be between 0 and " + GameStateEvaluator.MAX_WEIGHT + " (inclusive).",
    			(input -> isInteger(input) 
    					&& Integer.parseInt(input) >= 0 
    					&& Integer.parseInt(input) <= GameStateEvaluator.MAX_WEIGHT))));
    	evaluatorBuilder.withCoherenceWeight(Integer.parseInt(getInputSatisfying(
    			"What will the weight for coherence be?",
    			"Weight must be between 0 and " + GameStateEvaluator.MAX_WEIGHT + " (inclusive).",
    			(input -> isInteger(input) 
    					&& Integer.parseInt(input) >= 0 
    					&& Integer.parseInt(input) <= GameStateEvaluator.MAX_WEIGHT))));
    	evaluatorBuilder.withFormationBreakWeight(Integer.parseInt(getInputSatisfying(
    			"What will the weight for formation break be?",
    			"Weight must be between 0 and " + GameStateEvaluator.MAX_WEIGHT + " (inclusive).",
    			(input -> isInteger(input) 
    					&& Integer.parseInt(input) >= 0 
    					&& Integer.parseInt(input) <= GameStateEvaluator.MAX_WEIGHT))));
    	evaluatorBuilder.withSingleMarbleCapWeight(Integer.parseInt(getInputSatisfying(
    			"What will the weight for single marble capturing danger be?",
    			"Weight must be between 0 and " + GameStateEvaluator.MAX_WEIGHT + " (inclusive).",
    			(input -> isInteger(input) 
    					&& Integer.parseInt(input) >= 0 
    					&& Integer.parseInt(input) <= GameStateEvaluator.MAX_WEIGHT))));
    	evaluatorBuilder.withDoubleMarbleCapWeight(Integer.parseInt(getInputSatisfying(
    			"What will the weight for double marble capturing danger be?",
    			"Weight must be between 0 and " + GameStateEvaluator.MAX_WEIGHT + " (inclusive).",
    			(input -> isInteger(input) 
    					&& Integer.parseInt(input) >= 0 
    					&& Integer.parseInt(input) <= GameStateEvaluator.MAX_WEIGHT))));
    	
    	return evaluatorBuilder.build();
    }
    
    private static boolean isInteger(String str) {
    	try {
    		Integer.parseInt(str);
    		return true;
    	} catch (NumberFormatException e) {
    		return false;
    	}
    }

    /**
     * Queries the user for a string satisfying the predicate. Asks queryMessage
     * before each attempt, and outputs mistakeMessage if the predicate is
     * not satisfied.
     * @param queryMessage Message querying the user for input.
     * @param mistakeMessage Message displayed when the predicate is not satisfied.
     * @param pred The predicate that must hold for the string.
     * @requires none of the parameters equal null.
     * @ensures pred.test(result) holds.
     * @return a <code>String</code> satisfying the predicate.
     */
    public static String getInputSatisfying(String queryMessage, String mistakeMessage, Predicate<String> pred) {
        boolean satisfied = false;
        String input = null;
        while (!satisfied) {
            System.out.println(queryMessage);
            input = TerminalUI.readString();
            satisfied = pred.test(input);
            if (!satisfied) {
                System.out.println(mistakeMessage);
            }
        }
        return input;
    }

    /**
     * Initialises the TUI.
     * @ensures UI.getInstance() != null
     */
    public static void initializeUI() {
    	UI.setInstance(new TerminalUI());
    }
    
    public static class Builder {
    	private List<Player> players	= null;
    	private StartingLayout layout	= null;
    	private Game replayGame			= null;
    	private File gameOutput			= null;
    	private File gameStats			= null;
    	
    	private void checkWarnPlayersConsistent() {
    		if (players != null & replayGame != null) {
    			List<String> playerNames = players.stream().map(e -> e.getName()).collect(Collectors.toList());
    			if (!playerNames.equals(replayGame.players)) {
    				System.err.println("Warning: The given players are not consistent. " + playerNames 
    						+ " does not match " + replayGame.players + ".");
    			}
    		}
    	}
    	
    	public Builder withInputFile(File gameInput) throws FileNotFoundException {
    		if (gameInput != null) {
            	this.replayGame = Json.deserializeGame(new FileReader(gameInput));
            	if (this.players == null) {
            		this.players = new ArrayList<>();
            		for (String name : replayGame.players) {
                    	this.players.add(new ReplayPlayer(name));
                    }        		
            	}
            }
    		return this;
    	}
    	
    	public Builder withPlayers(List<Player> players) {
    		this.players = players;
    		return this;
    	}
    	
    	public Builder withPlayers(File playerFile) throws FileNotFoundException {
    		return withPlayers(Json.deserializePlayers(new FileReader(playerFile)));
    	}
    	
    	public Builder withStartingLayout(StartingLayout layout) {
    		this.layout = layout;
    		return this;
    	}
    	
    	public Builder withOutputFile(File gameOutput) {
    		this.gameOutput = gameOutput;
    		return this;
    	}
    	
    	public Builder withStatsFile(File gameStats) {
    		this.gameStats = gameStats;
    		return this;
    	}
    	
    	/**
    	 * Builds an Abalone game.
    	 * @return The Abalone game.
    	 * @throws FileNotFoundException Throws FileNotFoundException if the input file is not found.
    	 * @throws If both the players and gameInput are set to null.
    	 */
    	public Abalone build() throws FileNotFoundException {
    		if (players == null & replayGame == null) {
    			throw new RuntimeException("Abalone builder needs to either have an "
    					+ "input file or a list of players but has none.");
    		}
    		if (players.stream().anyMatch(p -> p instanceof ReplayPlayer) && replayGame == null) {
    			throw new RuntimeException("One or multiple players are specified as replay players, but no replay file was given. "
    					+ "As a result, no game can be constructed. Either provide an input game file or change the players.");
    		}
    		checkWarnPlayersConsistent();
    		return new Abalone(players, layout, replayGame, gameOutput, gameStats);
    	}
    }
}