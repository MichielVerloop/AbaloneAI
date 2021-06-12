package model.gamelogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.hex.Hex;

public class Team {
	//

    public List<Player> players;
    private List<Marble> conqueredMarbles;

    // The first player that should make a move once this team is allowed to make a move.
    private Player currentPlayer;

    /**
     * Constructs a team consisting of the given players.
     * @param players A set of the players participating in this team.
     */
    public Team(List<Player> players) {
        this.players = players;
        this.conqueredMarbles = new ArrayList<Marble>();
        for (Player player : this.players) {
            player.setTeam(this);
        }

        currentPlayer = players.get(0);
    }

    /**
     * Constructs a team consisting of the given player.
     * @param player The player that will make up the team.
     */
    public Team(Player player) {
        this(Arrays.asList(player));
    }

    public List<Marble> getConqueredMarbles() {
        return this.conqueredMarbles;
    }

    Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    Player getPreviousPlayer() {
    	return players.get(Math.floorMod(
    			players.indexOf(currentPlayer) - 1,
    			players.size()));
    }
    
    void nextPlayer() {
        currentPlayer = players.get((players.indexOf(currentPlayer) + 1) % players.size());
    }
    
    void previousPlayer() {
    	currentPlayer = getPreviousPlayer();
    }

    /**
     * Computes the coherence for all allied marbles.
     * Coherence is defined as the number of allied neighbours a marble has, with a bonus for
     * neighbours who's neighbour in the same direction are also allied.
     * @param board The Board for which the coherence is computed.
     * @return The coherence for all allied marbles.
     */
    public int computeCoherence(Board board) {
    	int coherence = 0;
    	for (Player player : players) {
    		for (Marble marble : player.getMarbles()) {
    			if (!marble.isCaptured()) {
    				coherence += marble.computeCoherence(board);
    			}
    		}
    	}
    	return coherence;
    }
    
    /**
     * Computes the sum of the distances to the center for all allied marbles.
     * @return The sum of the distances to the center for all allied marbles.
     */
    public int computeDistanceFromCenter() {
    	int distanceFromCenter = 0;
    	for (Player player : players) {
    		for (Marble marble : player.getMarbles()) {
    			distanceFromCenter += marble.computeDistanceFromCenter();
    		}
    	}
    	return distanceFromCenter;
    }
    
    /**
     * Computes the sum of the distances to R for all allied marbles.
     * @return The sum of the distances to R for all allied marbles.
     */
    public int computeDistanceFromR(Hex R) {
    	int distanceFromR = 0;
    	for (Player player : players) {
    		for (Marble marble : player.getMarbles()) {
    			distanceFromR += marble.computeDistanceFromR(R);
    		}
    	}
    	return distanceFromR;
    }
    
    /**
     * Computes the sum of the formation break factor for all allied marbles.
     * @param board The Board for which the formation break factor of the marbles is computed.
     * @return The sum of the formation break factor for all allied marbles.
     */
    public int computeFormationBreak(Board board) {
    	int sandwich = 0;
    	for (Player player : players) {
    		for (Marble marble : player.getMarbles()) {
    			if (!marble.isCaptured() && marble.isFormationBreak(board)) {
    				sandwich += 1;
    			}
    		}
    	}
    	return sandwich;
    }
    
    /**
     * Computes the sum of the immediate marble capturing danger factor for all allied marbles.
     * @param board The board for which the computation is done.
     * @return The number of marbles that could be thrown off the board within 1 move if it were
     *     the opponent's turn.
     */
    public int computeImmediateMarbleCapturingDanger(Board board) {
		int danger = 0;
		for (Player player : players) {
			for (Marble marble : player.getMarbles()) {
				if (marble.isInImmediateMarbleCapturingDanger(board)) {
					danger++;
				}
			}
		}
		return danger;
	}
    
    /**
     * Computes the sum of the single marble capturing danger factor for all allied marbles.
     * @param board The Board for which the computation is done.
     * @return The number of opponent marbles surrounding allied marbles that are on the edge of the board.
     */
    public int computeSingleMarbleCapturingDanger(Board board) {
		int danger = 0;
		for (Player player : players) {
			for (Marble marble : player.getMarbles()) {
				if (marble.getHex().length() == Board.BOARD_RADIUS - 1) {
					danger += marble.computeNrOfSurroundingEnemyMarbles(board);
				}
			}
		}
		return danger;
	}
    
    /**
     * Computes the sum of the double marble capturing danger factor for all allied marbles. 
     * @param board The Board for which the computation is done.
     * @return The number of opponent marbles surrounding allied marbles that are on the edge of 
     *     the board and have an ally as a neighbor on the edge. Counts shared threat marbles twice.
     */
    public int computeDoubleMarbleCapturingDanger(Board board) {
		int danger = 0;
		for (Player player : players) {
			for (Marble marble : player.getMarbles()) {
				if (marble.getHex().length() == Board.BOARD_RADIUS - 1) {
					for (Marble neighbor : marble.getAlliedNeighbours(board)) {
						if (neighbor.getHex().length() == Board.BOARD_RADIUS - 1) {
							danger += marble.computeNrOfSurroundingEnemyMarbles(board);
							break;
						}
					}
				}
				
			}
		}
		return danger;
	}
    
    public Hex computeCenterMass(Board board) {
    	List<Hex> combination = new ArrayList<>();
    	for (Player player : players) {
    		for (Marble marble : player.getMarbles()) {
    			if (!marble.isCaptured()) {
    				combination.add(marble.getHex());
    			}
    		}
    	}
    	return Hex.centerMass(combination);
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Player player : players) {
            result.append(player.getName());
        }
        return result.toString();
    }
    
    @Override
    public int hashCode() {
    	return players.hashCode();
    }
}
