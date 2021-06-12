package model.gamelogic;

import java.util.ArrayList;
import java.util.List;

import model.color.ConsoleColors;
import model.hex.Direction;
import model.hex.Hex;

public class Marble {
	//
    private Player owner;
    private char identifier;
    private boolean isCaptured;
    private Hex hex;

    /**
     * Constructs a Marble.
     * @param owner The player that controls this marble.
     * @param identifier An integer indicating which marble this is.
     */
    public Marble(Player owner, char identifier) {
        this.owner = owner;
        this.identifier = identifier;
        isCaptured = false;
    }

    public char getIdentifier() {
        return this.identifier;
    }

    public String getColor() {
    	return this.getOwner().getColor();
    }
    
    public Player getOwner() {
        return this.owner;
    }

    public Team getTeam() {
    	return this.getOwner().getTeam();
    }

    
    public boolean isCaptured() {
    	return this.isCaptured;
    }

    /**
     * Capture a marble, crediting the capture to the given Team.
     */
    public void capture() {
        this.isCaptured = true;
    }
    
    public void unCapture() {
    	this.isCaptured = false;
    }

    public Hex getHex() {
    	return this.hex;
    }
    
    /**
     * Gets all friendly neighbours of this marble in the provided board. 
     * @param board The Board for which the neighbours are given.
     * @return A list of all allied marbles that neighbour this in the provided GameState.
     */
    public List<Marble> getAlliedNeighbours(Board board) {
    	List<Marble> result = new ArrayList<>();
    	for (Direction dir : Direction.directions()) {
    		Field neighbour = board.getFieldOffset(hex, dir, 1);
    		if (neighbour != null
    			&& !neighbour.isEmpty()
    			&& neighbour.getMarble().getTeam().equals(owner.getTeam())) {
    			result.add(neighbour.getMarble());
    		}
    	}
    	return result;
    }
    
    public void setHex(Hex hex) {
    	this.hex = hex;
    }
    
    /**
     * Computes the number of allied neighbours this marble has, with a bonus for
     * neighbours who's neighbour in the same direction are also allied.
     * @param gameState The GameState for which the coherence is computed.
     * @return Computes the coherence for this marble.
     */
    int computeCoherence(Board board) {
    	// If the marble is not on the board
    	if (isCaptured()) {
    		return 0;
    	}
    	int coherence = 0;
    	for (Direction dir : Direction.directions()) {
    		Field neighbour = board.getFieldOffset(hex, dir, 1);
    		// If the neighbouring marble is owned by this team:
    		
    		if (neighbour != null && !neighbour.isEmpty() 
    			&& neighbour.getMarble().getTeam()
					.equals(owner.getTeam())) {
				coherence++;
				Field neighbourOfNeighbour = neighbour.getNeighbour(dir); 
				// If the neighbouring marble of the neighbouring marble is owned by this team:
				if (neighbourOfNeighbour != null && !neighbourOfNeighbour.isEmpty() 
					&& neighbourOfNeighbour.getMarble().getTeam()
						.equals(owner.getTeam())) {
					coherence++;
				}
    		}
    	}
    	return coherence;
    }
    
    /**
     * Computes the distance from this center for the marble.
     * @ensures 0 <= result <= 4
     * @return The distance from the center for this marble.
     */
    int computeDistanceFromCenter() {
    	// The reason the distance is bounded by 4 is to prevent overlap
    	// between the marblesConquered metric and the distanceFromCenter metric.
    	return Math.min(4, hex.length());
    }
    
    /**
     * Computes the distance from R for the marble.
     * @ensures result >= 0 
     * @return The distance from R to this marble.
     */
    int computeDistanceFromR(Hex R) {
    	return this.isCaptured() ? 100 : hex.distance(R);
    }
    
    /**
     * Computes whether there is a marble from the opponent in opposing directions.
     * @param board The Board from which the opponent's marble locations are taken.
     * @return Whether the marble is in a formation break or not.
     */
    boolean isFormationBreak(Board board) {
    	for (Direction dir : Direction.directions().subList(0, Direction.directions().size() / 2)) {
    		// If in dir and dir.invert(), there is a marble from the opponent, increment sandwich.
    		Field neighbour = board.getFieldOffset(hex, dir, 1);
    		Field opposingneighbour = board.getFieldOffset(hex, dir, -1);
    		if (neighbour != null && !neighbour.isEmpty() 
    				&& !neighbour.getMarble().getTeam()
    				.equals(owner.getTeam())
    			&& opposingneighbour != null && !opposingneighbour.isEmpty()
    				&& !opposingneighbour.getMarble().getTeam()
    				.equals(owner.getTeam())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    int computeNrOfSurroundingEnemyMarbles(Board board) {
    	int marbles = 0;
    	for (Direction dir : Direction.directions()) {
    		Field neighbour = board.getFieldOffset(hex, dir, 1);
    		if (neighbour != null 
    				&& !neighbour.isEmpty() 
    				&& !neighbour.getMarble().getTeam()
    				.equals(owner.getTeam())) {
    			marbles++;
    		}
    	}
    	return marbles;
    }
    
    /**
     * Returns true if the marble can be pushed off the board within one move.
     * @param board The board for which it is checked whether the marble can be pushed off.
     * @return True if the marble can be pushed off within one move, false otherwise.
     */
    public boolean isInImmediateMarbleCapturingDanger(Board board) {
    	if (hex.length() != Board.BOARD_RADIUS - 1) {
    		return false;
    	}
    	for (Direction dir : Direction.directions()) {
    		// At least two marbles must be in one direction to push you off
    		Field neighbour = board.getFieldOffset(hex, dir, 1);
    		if (neighbour == null || neighbour.isEmpty()) {
    			continue;
    		}
    		Field neighbour2 = board.getFieldOffset(hex, dir, 2);
    		if (neighbour2 == null || neighbour2.isEmpty()) {
    			continue;
    		}
    		if (!neighbour.getMarble().getTeam().equals(owner.getTeam())) {
    			// If both of the neighbours are of the opponent, you're in danger
    			if (!neighbour2.getMarble().getTeam().equals(owner.getTeam())) {
    				return true;
    			}
    			// else: if the second neighbouring marble is of this owner's team again,
    			// it's a formation break and therefore no danger.
    			continue;
    		} // else: the first neighbour is of this owner's team.
    		// if the second neighbour is also of this team, you're safe.
    		if (neighbour2.getMarble().getTeam().equals(owner.getTeam())) {
    			continue;
    		}
    		// else: the first neighbour is of your team but the second is of the opponent's team.
    		// If there are two more neighbours in that direction from the opponent, you are in danger. 
    		Field neighbour3 = board.getFieldOffset(hex, dir, 3);
    		if (neighbour3 == null || neighbour3.isEmpty() 
    				|| neighbour3.getMarble().getTeam().equals(owner.getTeam())) {
    			continue;
    		}
    		Field neighbour4 = board.getFieldOffset(hex, dir, 4);
    		if (neighbour4 == null || neighbour4.isEmpty() 
    				|| neighbour4.getMarble().getTeam().equals(owner.getTeam())) {
    			continue;
    		}
    		return true;
		}
    	return false;
	}
    
    @Override
    public String toString() {
        String result = "";
        switch (getColor()) {
            case Player.WHITE:
                result += ConsoleColors.WHITE;
                break;
            case Player.BLACK:
                result += ConsoleColors.BLACK;
                break;
            case Player.BLUE:
                result += ConsoleColors.BLUE_BRIGHT;
                break;
            case Player.RED:
                result += ConsoleColors.RED;
                break;
            default:
                System.out.println("Player should have color white, black, blue or red but has "
                                   + getColor());
                result = null;
                break;
        }

        result += ConsoleColors.GREEN_BACKGROUND + String.format("%2c", identifier) + ConsoleColors.RESET
                + ConsoleColors.GREEN_BACKGROUND;
        return result;
    }
}
