package model.gamelogic;

import com.owlike.genson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import model.exceptions.GameNotOverException;
import model.exceptions.IllegalMoveException;

/**
 * Class which contains the entirety of the abalone gameState.
 * @author Michiel Verloop
 */
public class GameState {
	//

    public static final int TURN_LIMIT = Integer.MAX_VALUE;

    public Game gameHistory;
    public GameStats gameStats;
    
    Board board;
    List<Team> teams;
    List<Player> players;

    private Team currentTeam;
    int turn;

    /**
     * Returns the board of this gameState.
     * @ensures result != null, result.size() = 61
     * @return the board of this gameState.
     */
    public Board getBoard() {
        return this.board;
    }

    public List<Team> getTeams() {
    	return this.teams;
    }
    
    /**
     * Returns the Player whose turn it currently is.
     * @invariant result != null
     * @ensures result != null
     * @return The first player that should make a move in this gameState.
     */
    public Player getCurrentPlayer() {
        return this.currentTeam.getCurrentPlayer();
    }
    
    /**
     * Returns the Team whose turn it currently is.
     * @invariant result != null
     * @ensures result != null
     * @return The first team that should make a move in this gameState. 
     */
    public Team getCurrentTeam() {
    	return currentTeam;
    }
    
    private Team getPreviousTeam() {
    	return teams.get(Math.floorMod(
    			teams.indexOf(currentTeam) - 1,
    			teams.size()));
    }
    
    /**
     * Returns the previous player that made a move.
     * @return The previous player that made a move.
     */
    public Player getPreviousPlayer() {
    	return getPreviousTeam()
    			.getPreviousPlayer();
    }

    /**
     * Fetches the player by their name.
     * @param name The name of the player.
     * @requires name != null
     * @ensures result != null
     * @return The Player if they are present in the game, null if they aren't.
     */
    public Player getPlayerByName(String name) {
        for (Player player : players) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    public int getTurn() {
        return this.turn;
    }

    public Score getScore(List<Team> teams) {
    	return new Score(teams);
    }
    
    public Score getScore() {
    	return new Score(teams);
    }
    
    /**
     * Constructs a game of abalone with a standard starting layout.
     * @param players The players that play on this board.
     * @invariant 0 <= turn < 96, getBoard.size() = 61.
     * @requires players != null, players.stream().allMatch((player -> player != null))
     * @ensures The board is initialized according to the number of players given.
     *     The board is populated with each player's marbles, with the location and number of
     *     marbles depending on how many players there are total.
     *     The players have been assigned teams in accordance to the protocol.
     */
    public GameState(List<Player> players) {
    	this(players, null);
    }
    
    /**
     * Constructs a game of abalone.
     * @param players The players that play on this board.
     * @param layout The starting layout for this board.
     * @invariant 0 <= turn < 96, getBoard.size() = 61.
     * @requires players != null, players.stream().allMatch((player -> player != null))
     * @ensures The board is initialized according to the number of players given.
     *     The board is populated with each player's marbles, with the location and number of
     *     marbles depending on how many players there are total.
     *     The players have been assigned teams in accordance to the protocol.
     */
    public GameState(List<Player> players, StartingLayout layout) {
    	// Players
    	for (int i = 0; i < players.size(); i++) {
    		players.get(i).setColor(i);
    	}
        this.players = players;
        
        // Teams
        this.teams = new ArrayList<Team>();
        if (players.size() == 2 || players.size() == 3) {
            for (Player player : players) {
                teams.add(new Team(player));
            }
        } else if (players.size() == 4) {
            teams.add(new Team(Arrays.asList(players.get(0), players.get(1))));
            teams.add(new Team(Arrays.asList(players.get(2), players.get(3))));
        } else {
            throw new IllegalArgumentException("Expected 2, 3 or 4 players but got "
                    + players.size() + " players.");
        }
        
        turn = 0;
        currentTeam = teams.get(0);
        board = new Board(teams, layout);
        
        gameHistory = new Game(layout, players.stream().map(x -> x.getName()).collect(Collectors.toList()));
        gameStats = new GameStats(this);
    }

    /**
     * Returns whether it is legal to apply move to this gameState.
     * @param move The move for which legality is checked.
     * @return true if the move is legal for this gameState, false otherwise.
     */
    public boolean isLegal(PlayableMove move) {
    	return ((PlayableMove)move).isLegal();
    }
    
    /**
     * Apply a move to the current board and give the turn to the next player.
     * Returns a MoveUndo that will undo the move that was made after calling this method.
     * @param move Move to be applied.
     * @requires move.isLegal() holds, getCurrentPlayer() == move.getInitiator()
     * @ensures The move is applied to the board, getCurrentPlayer now returns the
     *     next player that has to make a move and turn is increased by 1.
     * @return A MoveUndo that will undo the move that was made after calling this method.
     * @throws IllegalMoveException if the player making the move is not
     *     the next person that should make a move.
     */
    public MoveUndo makeMove(Move move) throws IllegalMoveException {
        // If playable move
        if (move instanceof PlayableMove) {
        	if (getCurrentPlayer() != move.getInitiator()) {
                throw new IllegalMoveException("Tried to apply a move when it was not your turn.");
            }
        	// Generate a moveUndo for the move.
        	final MoveUndo moveUndo = ((PlayableMove) move).getUndo();
        	
        	// Save the move if the move to the gameState history 
        	gameHistory.commitMoveToHistory((PlayableMove) move);
        	
        	// Core of making the move
        	getBoard().updateBoardHash(move);
        	move.makeMove();
        	nextPlayer();
        	
        	// Save the new board hash to the gameState history
        	gameHistory.commitGameStateToHistory(this);
        	
        	turn++;
        	return moveUndo;
        } // else: it's an undo move
    	if (getPreviousPlayer() != move.getInitiator()) {
            throw new IllegalMoveException("Tried to undo a move from " + move.getInitiator().getName()
            		+ " but the player that previously made a move was " + getPreviousPlayer() + ".");
        }
    	// Remove the top move from the gameState history
    	gameHistory.undoMoveInHistory();
    	
    	// apply the move.
    	getBoard().updateBoardHash(move);
    	move.makeMove();
    	previousPlayer();
    	turn--;
    	return null;
    }

    /**
     * Sets current to the next player that should make a move.
     */
    void nextPlayer() {
        currentTeam.nextPlayer();
        currentTeam = teams.get((teams.indexOf(currentTeam) + 1) % teams.size());
    }
    
    /**
     * Sets current to the previous player that made a move.
     */
    void previousPlayer() {
    	currentTeam = getPreviousTeam();
    	currentTeam.previousPlayer();
    }

    /**
     * Returns whether the game is over.
     * @return true if the turn limit has been reached or any of the players
     *     have 6 or more marbles conquered.
     */
    public boolean isFinished() {
        return isDraw() || hasWinner();
    }

    /**
     * Returns whether the game is a draw.
     * @return true if the turn limit has been reached, false otherwise.
     */
    private boolean turnLimitExpired() {
        return turn >= TURN_LIMIT;
    }
    
    /**
     * Returns whether there is a team with more marbles conquered than another.
     * @return True if there is a team with more marbles conquered than another, false otherwise.
     */
    private boolean atLeastOneTeamHasMoreMarblesThanAnother() {
    	for (Team team : teams) {
    		for (Team team2 : teams) {
    			if (team.getConqueredMarbles().size() != team2.getConqueredMarbles().size()) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * Returns whether the game is a draw.
     * @return true if the turn limit has been reached and there is no team with more
     *     marbles conquered than another. False otherwise.
     */
    private boolean isDraw() {
    	return turnLimitExpired() && !atLeastOneTeamHasMoreMarblesThanAnother();
    }

    /**
     * Returns whether the game has a winner.
     * @return true if there is a team with 6 or more conquered
     *     marbles, false otherwise.
     */
    private boolean hasWinner() {
        return teams.stream()
                .anyMatch((e) -> (e.getConqueredMarbles().size() >= 6))
                || (turnLimitExpired() && atLeastOneTeamHasMoreMarblesThanAnother());
    }

    /**
     * Returns the winning team if the game has a winner, null if the game ends in a draw.
     * @return the winning team if the game has a winner, null if the game ends in a draw.
     * @throws GameNotOverException if the game is not yet finished.
     */
    public Team getWinner() throws GameNotOverException {
        if (!this.isFinished()) {
            throw new GameNotOverException();
        }

        try {
            return teams.stream()
                    .filter(e -> e.getConqueredMarbles().size() >= 6)
                    .findFirst().get();
        } catch (NoSuchElementException e) {
        	// case game ended due to the turn limit.
        	for (Team team1 : teams) {
        		for (Team team2 : teams) {
        			if (team1.getConqueredMarbles().size() > team2.getConqueredMarbles().size()) {
        				return team1;
        			}
        		}
        	}
        	return null;
        }
    }

    /**
     * Returns the number of the given team according to the protocol.
     * @param team The Team who's number is queried.
     * @ensures result > 0
     * @return the number of team according to the protocol.
     */
    public int getTeamNumber(Team team) {
        return this.teams.indexOf(team) + 1;
    }

    @Override
    public String toString() {
        return board.toString() + "\n" + teamsToString();
    }

    /**
     * Creates a String representation of all teams of this gameState, then returns it.
     * @ensures result != null
     * @return a String representation of all teams of this gameState.
     */
    private String teamsToString() {
        StringBuilder teamString = new StringBuilder();
        for (Team team : teams) {
            teamString.append("Team " + team.toString() + " cleared "
                    + team.getConqueredMarbles().size() + " marbles."
                    + System.lineSeparator());
        }
        return teamString.toString();
    }

    /**
     * Returns the team based on the given number according to protocol.
     * @param number The number that identifies a team.
     * @requires 0 < number <= teams.size()
     * @ensures result != null
     * @return the team based on the given number according to protocol.
     */
    public Team getTeamByNumber(int number) {
        return teams.get(number - 1);
    }
    
    /**
     * Returns the total number of marbles that have been wiped off
     * the board this game.
     * @return the total number of marbles that have been wiped off
     *     the board this game.
     */
    public int getTotalNrOfConqueredMarbles() {
    	int result = 0;
    	for (Team team : teams) {
    		result += team.getConqueredMarbles().size();
    	}
    	return result;
    }
    
    public static class Score {
		Map<Team, Integer> score;
		
		/**
		 * Generates the Score object from the teams.
		 * @param teams The teams from which to generate the Score.
		 */
		public Score(List<Team> teams) {
			score = new LinkedHashMap<>();
			for (Team team : teams) {
				score.put(team, team.getConqueredMarbles().size());
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Score) {
				return score.equals(((Score) obj).score);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return score.hashCode();
		}
		
		/**
		 * Returns true if any of the score's values are lower than the other score.
		 * As such, if a.lowerThan(b), b.lowerThan(a) is also possible.
		 * @param other The other Score.
		 * @return true if any of the score's values are lower than the other score.
		 */
		public boolean lowerThan(Score other) {
			List<Integer> scores = new ArrayList<Integer>(score.values());
			List<Integer> otherScores = new ArrayList<Integer>(other.score.values());
			for (int i = 0; i < scores.size(); i++) {
				if (scores.get(i) < otherScores.get(i)) {
					return true;
				}
			}
			return false;
		}
	}
    
    public static class Game {	
		String description;
		public StartingLayout layout;
    	public List<String> players;
		public List<String> moves;
		List<Long> boardHashes;
		Map<Long, Integer> boardHashesMap;
		
		public Game(StartingLayout layout, List<String> players) {
			this("Your game description here...", layout, players);
		}
		
		Game(@JsonProperty("description") String description, 
			 @JsonProperty("layout") StartingLayout layout, 
			 @JsonProperty("players") List<String> players) {
			this.description = description;
			this.layout = layout;
			this.players = players;
			this.moves = new ArrayList<>();
			this.boardHashes = new ArrayList<>();
		}
		
		private void commitMoveToHistory(PlayableMove move) {
			moves.add(move.getMoveNotation());
		}
		
		private void commitGameStateToHistory(GameState gameState) {
			boardHashes.add(gameState.getBoard().getBoardHash());
		}
		
		private void undoMoveInHistory() {
			moves.remove(moves.size() - 1);
			boardHashes.remove(boardHashes.size() - 1);
		}
		
		/**
		 * Checks whether the resulting hash has already been played in the past.
		 * @param hash The hash of the board after applying the move.
		 * @return True if the hash is found in the game's history, false otherwise.
		 */
		public boolean isRepetition(Long hash) {
			return boardHashes.contains(hash);
		}
    }
}
