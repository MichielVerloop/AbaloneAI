package model.gamelogic;

import java.util.Set;

import model.hex.Direction;
import model.hex.Hex;

public class Field {
	//
    private final Hex hex;
    private Marble marble;
    private Board board;

    public Field(Board board, Hex hex) {
        this(board, hex, null);
    }

    /**
     * Constructs a field given a board and hex and populates it with the given marble.
     * @param board The board on which the field exists.
     * @param hex The position of this field.
     * @param marble The marble that is on this field.
     */
    public Field(Board board, Hex hex, Marble marble) {
    	this.board = board;
        this.hex = hex;
        this.marble = marble;
    }

    public Hex getHex() {
        return hex;
    }

    public Marble getMarble() {
        return marble;
    }

    public Set<Field> getNeighbours() {
    	return board.getFields(hex.ring(1));
    }
    
    public Field getNeighbour(Direction dir) {
    	return board.getField(hex.neighbour(dir));
    }
    
    
    /**
     * Change this field's marble.
     * Change the marble's hexagon.
     * @param marble The new marble
     */
    public void setMarble(Marble marble) {
        this.marble = marble;
        if (this.marble != null) {
            this.marble.setHex(hex);
        }
    }

    public boolean isEmpty() {
        return this.marble == null;
    }

    @Override
    public String toString() {
        if (marble != null) {
            return marble.toString();
        } else {
            return String.format("  ");
        }
    }
}
