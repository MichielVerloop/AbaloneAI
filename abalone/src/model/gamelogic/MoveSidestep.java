package model.gamelogic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.exceptions.IllegalContainerSizeException;
import model.exceptions.IllegalMoveException;
import model.exceptions.IncorrectSubclassException;
import model.hex.Direction;
import model.hex.Hex;

public class MoveSidestep implements PlayableMove {
	//
	private Board board;
    private Set<Hex> marbleLocations;
	private Direction direction;
    private Player initiator;

    /**
     * Constructs a sidestepMove.
     * @param marbleLocations The Hexagons that point to the marbles that are to be moved.
     * @param direction The direction of the move.
     * @param initiator The player that is to execute this move.
     * @requires Direction is valid, initiator != null,  2 >= marbleLocations.size() <= 3
     *     and for all of the marbles, none of their neighbors in the supplied direction
     *     overlaps with the location of another of the supplied marbles.
     * @ensures The move is created and is indeed a sidestep move.
     */
    MoveSidestep(Board board, Set<Hex> marbleLocations, Direction direction, Player initiator) {
    	this.board = board;
        this.marbleLocations = marbleLocations;
        this.direction = direction;
        this.initiator = initiator;

        // Checks whether the size of the supplied marbleLocations is legal.
        if (!(2 <= marbleLocations.size() && marbleLocations.size() <= 3)) {
            throw new IllegalContainerSizeException(2, 3, marbleLocations.size());
        }

        // Checks whether the move is indeed a side-step move.
        for (Hex hex : marbleLocations) {
            if (marbleLocations.contains(hex.neighbour(direction))) {
                throw new IncorrectSubclassException(
                    "The given parameters make this a Sumito move as opposed to a side-step move. "
                    + "Use MoveSumito(origin, direction, initiator) instead");
            }
        }
    }

    @Override
    public boolean isLegal() {
    	return pushInitiatorTeamOwnsAllMarbles()
        		&& pushInitiatorOwnsAtLeastOneMarble()
        		&& !isSuicide()
        		&& !pushIsBlocked();
        // All marbles are owned by the initiator's team,
        // the initiator owns at least one of the marbles,
        // none of the marbles will fall off the board,
        // and the move is not blocked by other marbles.
    }

    @Override
    public boolean isLegalThrows() throws IllegalMoveException {
        if (!pushInitiatorTeamOwnsAllMarbles()) {
        	throw new IllegalMoveException("Not all marbles are owned by your team.");
        } else if (!pushInitiatorOwnsAtLeastOneMarble()) {
        	throw new IllegalMoveException("You do not own at least one of the marbles.");
        } else if (isSuicide()) {
        	throw new IllegalMoveException("This move would lead to suicide.");
        } else if (pushIsBlocked()) {
        	throw new IllegalMoveException("This move is blocked by other marbles.");
        }
        
    	return true;
    }

    private boolean pushInitiatorTeamOwnsAllMarbles() {
    	for (Hex hex : marbleLocations) {
            if (!board.getField(hex).getMarble().getTeam().equals(initiator.getTeam())) {
            	return false;
            }
        }
    	return true;
    }
    
    private boolean pushInitiatorOwnsAtLeastOneMarble() {
    	boolean atLeastOneMarbleIsOwnedByInitiator = false;
        for (Hex hex : marbleLocations) {
            if (board.getField(hex).getMarble().getOwner().equals(initiator)) {
                atLeastOneMarbleIsOwnedByInitiator = true;
                break;
            }
        }
        return atLeastOneMarbleIsOwnedByInitiator;
    }
    
    private boolean isSuicide() throws IllegalMoveException {
    	for (Hex hex : marbleLocations) {
            if (hex.neighbour(direction).length() >= Board.BOARD_RADIUS) {
                return true;
            }
        }
    	return false;
    }
    
    private boolean pushIsBlocked() {
    	for (Hex hex : marbleLocations) {
            if (!board.getFieldOffset(hex, direction, 1).isEmpty()) {
                return true;
            }
        }
    	return false;
    }
    
    @Override
    public void makeMove() {
        if (!isLegal()) {
            throw new IllegalMoveException("Tried to apply an illegal move.");
        }

        for (Hex hex : marbleLocations) {
            Marble marb = board.getField(hex).getMarble();
            board.getField(hex).setMarble(null);
            board.getFieldOffset(hex, direction, 1).setMarble(marb);
        }
    }

    @Override
    public Player getInitiator() {
        return this.initiator;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public Set<Marble> getMarbles() {
    	Set<Marble> result = new HashSet<>();
    	for (Hex hex : this.marbleLocations) {
    		result.add(board.getField(hex).getMarble());
    	}
    	return result;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("sidestep move involving the marbles ");
        for (Hex hex : marbleLocations) {
            result.append(board.getField(hex).getMarble().getIdentifier());
            result.append(" ");
        }
        result.append("in direction " + direction.toString());
        result.append(" by player " + initiator.getName());
        return result.toString();
    }


	@Override
	public MoveUndo getUndo() {
		return new MoveUndo(
				board,
				getMarbles(), 
				direction.invert(), 
				initiator);
	}

	@Override
	public int getNrOfInvolvedMarbles() {
		return marbleLocations.size();
	}
	
	@Override
	public String getMoveNotation() {
		StringBuilder notation = new StringBuilder();
		
		Hex end1;
		Hex end2;
		Hex middle = null;
		if (marbleLocations.size() == 2) {
			Iterator<Hex> marbles = marbleLocations.iterator();
			end1 = marbles.next();
			end2 = marbles.next();
		} else { // size == 3
			Iterator<Hex> marbles = marbleLocations.iterator();
			Hex marble1 = marbles.next();
			Hex marble2 = marbles.next();
			Hex marble3 = marbles.next();
			if (marble1.isNeighbourOf(marble2) && marble1.isNeighbourOf(marble3)) {
				end1 = marble2;
				end2 = marble3;
				middle = marble1;
			} else if (marble2.isNeighbourOf(marble1) && marble2.isNeighbourOf(marble3)) {
				end1 = marble1;
				end2 = marble3;
				middle = marble2;
			} else if (marble3.isNeighbourOf(marble1) && marble3.isNeighbourOf(marble2)) {
				end1 = marble1;
				end2 = marble2;
				middle = marble3;
			} else {
				throw new IllegalStateException("Unreachable code.");
			}
		}
		
		notation.append(Board.cubeToAbal(end1));
		notation.append(Board.cubeToAbal(end2));
		
		// Check the direction for the notation.
		Hex dirEnd1 = end1.add(Hex.direction(direction));
		Hex dirEnd2 = end2.add(Hex.direction(direction));
		if (marbleLocations.size() == 2) {
			notation.append(Board.cubeToAbal(dirEnd1.isNeighbourOf(end2)
					? dirEnd2 : dirEnd1));
		} else { // size == 3
			notation.append(Board.cubeToAbal(dirEnd1.isNeighbourOf(middle)
					? dirEnd2 : dirEnd1));
		}
		return notation.toString();	
	}
	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((initiator == null) ? 0 : initiator.hashCode());
		result = prime * result + ((marbleLocations == null) ? 0 : marbleLocations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MoveSidestep other = (MoveSidestep) obj;
		if (direction != other.direction)
			return false;
		if (initiator == null) {
			if (other.initiator != null)
				return false;
		} else if (!initiator.equals(other.initiator))
			return false;
		if (marbleLocations == null) {
			if (other.marbleLocations != null)
				return false;
		} else if (!marbleLocations.equals(other.marbleLocations))
			return false;
		return true;
	}

	
}
