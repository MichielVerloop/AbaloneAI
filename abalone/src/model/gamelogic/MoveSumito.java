package model.gamelogic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.exceptions.IllegalMoveException;
import model.hex.Direction;
import model.hex.Hex;

public class MoveSumito implements PlayableMove {
	//
	private Board board;
    private Hex origin;
    private Direction direction;
    private Player initiator;
    private Set<Field> originalMarbleFields;

    /**
     * Constructs a sumitoMove.
     * @param origin The origin of the move.
     * @param direction The direction of the move.
     */
    MoveSumito(Board board, Hex origin, Direction direction, Player initiator) {
    	this.board = board;
        this.origin = origin;
        this.direction = direction;
        this.initiator = initiator;
        this.originalMarbleFields = new HashSet<>();
        for (Hex hex : collectInvolvedCoords()) {
        	Field field = board.getField(hex);
        	if (field.getMarble() != null) {
    			originalMarbleFields.add(field);
    		}
        }
    }

    /**
     * Collects the hexagons of the marbles that are involved in this move.
     * Does not make an isLegal check.
     * @param board The board containing the marbles.
     * @return List of all marbles involved in the move.
     */
    public List<Hex> collectInvolvedCoords() {
        List<Hex> coords = origin.neighbours(direction,
                collectNumberOfInvolvedCoords(origin.neighbour(direction)));
        coords.add(origin);
        assert (coords.size() == collectNumberOfInvolvedCoords(origin.neighbour(direction)) + 1);
        return coords;
    }

    /**
     * Returns the number of neighbours that have marbles that are involved in this move.
     * @param board The board containing the marbles.
     * @param hex The current coordinate that is being evaluated.
     * @return number of neighbours that have marbles that are involved in this move.
     */
    private int collectNumberOfInvolvedCoords(Hex hex) {
        if (board.getField(hex) == null || board.getField(hex).isEmpty()) {
            return 0;
        } else {
            return 1 + collectNumberOfInvolvedCoords(hex.neighbour(direction));
        }
    }

    @Override
    public boolean isLegal() {        
        Field field = board.getField(origin);
        Marble originMarble = field.getMarble();
        
        return pushHasMarbleOnOrigin(originMarble)
        	&& pushInitiatorOwnsOrigin(originMarble) 
        	&& isLegal(origin.neighbour(direction), 1, 0); // recursive check
    }

    /**
     * Recursively checks whether a move is legal.
     * @param board The board for which the legality of the move is checked.
     * @param initiator The player that executes the move.
     * @param hex The hexagon that is currently being evaluated.
     * @requires board != null, owner != null, strength > 0
     * @ensures true or false is returned.
     * @return True if the move is legal, throws otherwise.
     */
    private boolean isLegal(Hex hex, int pushStrength, int oppositionStrength) {
        if (board == null) {
            throw new IllegalArgumentException("GameState should not equal null");
        } else if (initiator == null) {
            throw new IllegalArgumentException("The initiater of the move should not equal null");
        } else if (pushStrength == 0) {
            throw new IllegalArgumentException("Call isLegal() instead.");
        } else if (pushStrength < 0) {
            throw new IllegalArgumentException("Strength should be greater than 0 but was " + pushStrength);
        }
        Field field = board.getField(hex);

        // Base cases:
        if (pushIsTooStrong(pushStrength)
        	|| pushIsTooWeak(pushStrength, oppositionStrength)
        	|| pushIsSuicide(field, oppositionStrength)) {
        	return false;
        }
        
        // pushing off the board OR valid push on the board:
        if (field == null || field.isEmpty()) {
        	return true;
        }
        
        if (pushIsFormationBreak(field, oppositionStrength)) {
        	return false;
        }
        
        // Recursive cases
        // Case allied marble is on this spot
        if (field.getMarble().getTeam().equals(initiator.getTeam())) {
            return isLegal(hex.neighbour(direction), pushStrength + 1, oppositionStrength);
        } else {
            // Case opponent marble is on this spot
            return isLegal(hex.neighbour(direction), pushStrength, oppositionStrength + 1);
        }
    }
    
    
    @Override
    public boolean isLegalThrows() throws IllegalMoveException {
        Field field = board.getField(origin);
        Marble originMarble = field.getMarble();
        // Base cases
        // There is no marble to be moved
        
        if (!pushHasMarbleOnOrigin(originMarble)) {
        	throw new IllegalMoveException("There is no marble to be moved.");
        } else if (!pushInitiatorOwnsOrigin(originMarble)) {
        	throw new IllegalMoveException("You do not own this marble.");
        }
        // Recursive case
        return isLegalThrows(origin.neighbour(direction), 1, 0);
    }

    /**
     * Recursively checks whether a move is legal.
     * @param board The board for which the legality of the move is checked.
     * @param initiator The player that executes the move.
     * @param hex The hexagon that is currently being evaluated.
     * @requires board != null, owner != null, strength > 0
     * @ensures true or false is returned.
     * @return True if the move is legal, throws otherwise.
     * @throws IllegalMoveException when the move is illegal.
     */
    private boolean isLegalThrows(Hex hex, int pushStrength, int oppositionStrength)
        throws IllegalMoveException {
        if (board == null) {
            throw new IllegalArgumentException("GameState should not equal null");
        } else if (initiator == null) {
            throw new IllegalArgumentException("The initiater of the move should not equal null");
        } else if (pushStrength == 0) {
            throw new IllegalArgumentException("Call isLegal(board, initiator) instead.");
        } else if (pushStrength < 0) {
            throw new IllegalArgumentException("Strength should be greater than 0 but was " + pushStrength);
        }
        Field field = board.getField(hex);

        // Base cases:
        if (pushIsTooStrong(pushStrength)) {
        	throw new IllegalMoveException("There are too many allied marbles that push.");
        } else if (pushIsTooWeak(pushStrength, oppositionStrength)) {
        	throw new IllegalMoveException("The push is not strong enough.");
        } else if (pushIsSuicide(field, oppositionStrength)) {
        	throw new IllegalMoveException("This move would lead to suicide.");
        }
        
        // pushing off the board OR valid push on the board:
        if (field == null || field.isEmpty()) {
        	return true;
        }
        if (pushIsFormationBreak(field, oppositionStrength)) {
        	throw new IllegalMoveException("An allied marble is behind the marble you are trying to push.");
        }
        
        // Recursive cases
        // Case allied marble is on this spot
        if (field.getMarble().getTeam().equals(initiator.getTeam())) {
            return isLegalThrows(hex.neighbour(direction), pushStrength + 1, oppositionStrength);
        } else {
            // Case opponent marble is on this spot
            return isLegalThrows(hex.neighbour(direction), pushStrength, oppositionStrength + 1);
        }
    }

    private boolean pushHasMarbleOnOrigin(Marble origin) {
    	return origin != null;
    }
    
    private boolean pushInitiatorOwnsOrigin(Marble origin) {
    	return origin.getOwner() == initiator;
    }
    
    private boolean pushIsTooStrong(int pushStrength) {
    	return pushStrength >= 4;
    }
    
    private boolean pushIsTooWeak(int pushStrength, int oppositionStrength) {
    	return pushStrength <= oppositionStrength;
    }
    
    private boolean pushIsSuicide(Field field, int oppositionStrength) {
    	return field == null && oppositionStrength == 0;
    }
    
    private boolean pushIsFormationBreak(Field field, int oppositionStrength) {
    	return oppositionStrength >= 1 
    			&& field.getMarble().getTeam()
    			.equals(initiator.getTeam());
    }
    
    @Override
    public void makeMove() {
        if (!isLegal()) {
            throw new IllegalMoveException("Tried to apply an illegal move.");
        }

        Field field = board.getField(origin);
        Marble shiftingMarble = field.getMarble();
        field.setMarble(null);

        makeMove(initiator, origin.neighbour(direction), shiftingMarble);
    }

    private void makeMove(Player initiator, Hex hex, Marble newMarble) {
        Field field = board.getField(hex);

        // Base case: Initiator pushes newMarble off the board.
        if (field == null) {
            initiator.getTeam().getConqueredMarbles().add(newMarble);
            newMarble.capture();
            newMarble.setHex(hex);
            return;
        }
        // Update marble for this hex
        Marble shiftingMarble = field.getMarble();
        field.setMarble(newMarble);
        // Base case: Empty space on the board was reached, no marbles pushed off.
        if (shiftingMarble == null) {
            return;
        }

        // Recursive call
        makeMove(initiator, hex.neighbour(direction), shiftingMarble);
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
    	for (Field field : originalMarbleFields) {
    		result.add(field.getMarble());
    	}
    	return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("sumito move originating from ");
        result.append(board.getField(origin).getMarble().getIdentifier());
        result.append(" in direction " + direction.toString());
        result.append(" by player " + initiator.getName());
        return result.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((initiator == null) ? 0 : initiator.hashCode());
		result = prime * result + ((originalMarbleFields == null) ? 0 : originalMarbleFields.hashCode());
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
		MoveSumito other = (MoveSumito) obj;
		if (direction != other.direction)
			return false;
		if (initiator == null) {
			if (other.initiator != null)
				return false;
		} else if (!initiator.equals(other.initiator))
			return false;
		if (originalMarbleFields == null) {
			if (other.originalMarbleFields != null)
				return false;
		} else if (!originalMarbleFields.equals(other.originalMarbleFields))
			return false;
		return true;
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
		return originalMarbleFields.size();
	}

	@Override
	public String getMoveNotation() {
		StringBuilder notation = new StringBuilder();
		
		notation.append(Board.cubeToAbal(origin));
		notation.append(Board.cubeToAbal(origin.add(Hex.direction(direction))));
		return notation.toString();
	}
}
