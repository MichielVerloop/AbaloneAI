package model.gamelogic;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.hex.Direction;
import model.hex.Hex;

public class MoveUndo implements Move {
	//
	private Board board;
	private Set<Marble> involvedMarbles;
    private Direction direction;
    private Player initiator;
    
    /**
     * Constructs a MoveUndo, used to undo a move.
     * @param involvedMarbles The marbles that were involved in the original move.
     * @param dir The inverse of the direction in which the original move was made.
     * @param initiator The initiator of the original move.
     */
    public MoveUndo(Board board, Set<Marble> involvedMarbles, Direction dir, Player initiator) {
    	this.board = board;
		this.involvedMarbles = involvedMarbles;
        this.direction = dir;
        this.initiator = initiator;
	}

	@Override
	public void makeMove() {
		// Checks whether the direction of the marbles is inline and therefore a sumito move.
		List<Hex> involvedCoords = involvedMarbles.stream()
				.map(Marble::getHex)
				.collect(Collectors.toList());
		List<Hex> neighboursOfInvolvedCoords = involvedCoords.stream()
				.map(hex -> hex.neighbour(direction.invert()))
				.collect(Collectors.toList());
		boolean inline = !Collections.disjoint(involvedCoords, neighboursOfInvolvedCoords);
		
		if (inline) {
			undoAsSumito(involvedCoords);
		} else {
			undoAsSidestep();
		}
	}
	
	private void undoAsSumito(List<Hex> involvedCoords) {
		// Get the origin of undo: The marble for which the neighbour in the original move's
		// direction is not contained within the list of involved coordinates.
		Marble undoOrigin = involvedMarbles.stream().filter(
				marble -> !involvedCoords
				.contains(marble.getHex().neighbour(direction.invert())))
				.collect(Collectors.toList()).get(0);
		
		Field field = board.getField(undoOrigin.getHex());
		if (field != null) {
			field.setMarble(null);
		} else {
			initiator.getTeam().getConqueredMarbles().remove(undoOrigin);
			undoOrigin.unCapture();
		}
		
		Field neighbouringField = board.getFieldOffset(undoOrigin.getHex(), direction, 1);
		Marble neighbouringMarble = neighbouringField.getMarble();
		neighbouringField.setMarble(undoOrigin);
		
		// Recursive case
		undoAsSumito(involvedCoords.size() - 1, neighbouringMarble);
	}
	
	private void undoAsSumito(int nrOfMarblesToReplace, Marble marble) {
		// base case:
		if (nrOfMarblesToReplace == 0) {
			return;
		}
		
		// step:
		Field neighbouringField = board.getFieldOffset(marble.getHex(), direction, 1);
		Marble neighbouringMarble = neighbouringField.getMarble();
		neighbouringField.setMarble(marble);
		// recursive case:
		undoAsSumito(nrOfMarblesToReplace - 1, neighbouringMarble);
	}
	
	private void undoAsSidestep() {
		for (Marble marble : involvedMarbles) {
			// Remove the marble from its current field.
			Field field = board.getField(marble.getHex());
			field.setMarble(null);
			// Add the marble to the neighbouring field.
			Field neighbouringField = board.getFieldOffset(marble.getHex(), direction, 1);
			neighbouringField.setMarble(marble);
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
		return involvedMarbles;
	}

}
