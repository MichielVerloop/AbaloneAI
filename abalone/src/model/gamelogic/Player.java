package model.gamelogic;

import java.util.ArrayList;
import java.util.List;
import model.artificialintelligence.AggressivePusherStrategy;
import model.artificialintelligence.MinimaxStrategy;
import model.artificialintelligence.RandomStrategy;
import model.artificialintelligence.minimax.GameStateEvaluator;
import model.artificialintelligence.minimax.Minimax;
import model.color.ConsoleColors;
import model.hex.Hex;

public abstract class Player {
	//

    public static final String WHITE = "white";
    public static final String BLACK = "black";
    public static final String BLUE = "blue";
    public static final String RED = "red";
    public static final String[] COLORS = {BLACK, WHITE, BLUE, RED};
    public static final String PLAYER_NAME_PATTERN = "^[\\w.,: ]+$";
    
    private String name;
    private String color;
    private Team team;
    private List<Marble> marbles;

    /**
     * Factory method to create computer players that use dfs or iddfs.
     * @param name Name of the player (must match the regex [\w.,: ]+)
     * @param miniBuilder The builder for minimax which contains all search algorithm and heuristic settings.
     * @param evaluator The GameStateEvaluator that contains information on how to grade a GameState.
     * @return player of the appropriate subclass
     * @throws IllegalArgumentException If any of the arguments is null or does not fit the requirements
     */
    public static Player newPlayer(String name, Minimax.Builder miniBuilder, GameStateEvaluator evaluator) {
    	assert (name != null);
    	assert (miniBuilder != null);
    	assert (evaluator != null);
    	if (!name.matches(PLAYER_NAME_PATTERN)) {
            throw new IllegalArgumentException("Name must be at least one character and may"
                    + "only contain alphabetic characters, digits and the characters \",.: \".");
        }
    	
    	return new ComputerPlayer(name, new MinimaxStrategy(miniBuilder, evaluator));
    }

    /**
     * Factory method to create players.
     * @param name Name of the player (must match the regex [\w.,: ]+)
     * @param playerType Type of the player (must be human, random or aggressive)
     * @return player of the appropriate subclass
     * @throws IllegalArgumentException If any of the arguments is null or does not fit the requirements
     */
    public static Player newPlayer(String name, String playerType) {
    	assert (name != null);
    	assert (playerType != null);
    	if (!name.toLowerCase().matches(PLAYER_NAME_PATTERN)) {
            throw new IllegalArgumentException("Name \"" + name + "\" must be at least one character and may"
                    + "only contain alphabetic characters, digits and the characters \",.: \".");
        }
        
        playerType = playerType.toLowerCase();
        switch (playerType.toLowerCase()) {
        	case "human":
        		return new HumanPlayer(name);
        	case "random":
        		return new ComputerPlayer(name, new RandomStrategy());
        	case "aggressive":
        		return new ComputerPlayer(name, new AggressivePusherStrategy());
        	default:
        		throw new IllegalArgumentException("Invalid player type. "
        				+ "Strategy must be human|random|aggressive but was " + playerType + ".");
        }
    }

    /**
     * Constructs a player object.
     * @param name Name of the player
     * @param color Color of the marbles controlled by this player
     */
    protected Player(String name) {
        this.name = name;
        this.marbles = new ArrayList<Marble>();
    }

    public String getName() {
        return this.name;
    }

    public String getColor() {
        return this.color;
    }

    public Team getTeam() {
        return this.team;
    }

    public List<Marble> getMarbles() {
        return this.marbles;
    }

    /**
     * Returns the field to which the given marble belongs.
     * @param identifier the letter of the marble
     * @return Hex of the marble if it is found, null otherwise.
     */
    public Hex getHexagonOfMarble(char identifier) {
        for (Marble marble : marbles) {
            if (Character.toLowerCase(marble.getIdentifier())
                    == Character.toLowerCase(identifier)) {
                return marble.getHex();
            }
        }
        return null;
    }

    void setTeam(Team team) {
        this.team = team;
    }
    
    /**
     * Sets the color of a player based on their number.
     * @param i The number of the player.
     */
    void setColor(int i) {
    	this.color = Player.COLORS[i];
    }

    /**
     * Creates a given number of marbles for the player.
     * @param nrOfMarbles the number of marbles that the player will control.
     */
    void createMarbles(int nrOfMarbles) {
        this.marbles = new ArrayList<Marble>();
        for (char c = 'A'; c < 'A' + nrOfMarbles; c++) {
            marbles.add(new Marble(this, c));
        }
    }

    public abstract PlayableMove determineMove(GameState gameState);
    
    @Override
    public int hashCode() {
    	return name.hashCode();
    }
    
    @Override
    public String toString() {
    	switch (color) {
    		case WHITE:
    			return ConsoleColors.WHITE + ConsoleColors.GREEN_BACKGROUND 
    					+ getName() + ConsoleColors.RESET;
    		case BLACK:
    			return ConsoleColors.BLACK + ConsoleColors.GREEN_BACKGROUND 
    					+ getName() + ConsoleColors.RESET;
    		case BLUE:
    			return ConsoleColors.BLUE + ConsoleColors.GREEN_BACKGROUND 
    					+ getName() + ConsoleColors.RESET;
    		case RED:
    			return ConsoleColors.RED + ConsoleColors.GREEN_BACKGROUND 
    					+ getName() + ConsoleColors.RESET;
    		default:
    			throw new IllegalStateException();
    	}
    }
}
