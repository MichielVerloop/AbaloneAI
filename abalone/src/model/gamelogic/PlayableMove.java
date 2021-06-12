package model.gamelogic;

import model.exceptions.IllegalMoveException;

public interface PlayableMove extends Move, Comparable<PlayableMove> {
	//
    
	// Functions are intentionally left package private:
	// the controller should call board.isLegal(Move) and board.makeMove(Move) instead.

	/**
     * Checks whether a move is legal.
     * @requires board != null.
     * @ensures true or false is returned.
     * @return True if the move is legal, false otherwise.
     */
	boolean isLegal();

    /**
     * Checks whether a move is legal.
     * @requires board != null.
     * @ensures true is returned, or IllegalMoveException is thrown.
     * @return True if the move is legal, throws otherwise.
     * @throws IllegalMoveException when the move is illegal.
     */
    boolean isLegalThrows() throws IllegalMoveException;
    
    /**
     * Returns the number of marbles involved in the move. 
     * @return 1-5.
     */
    int getNrOfInvolvedMarbles();
    
    /**
     * Returns the Abalone notation of this move.
     * @return the Abalone notation of this move.
     */
    String getMoveNotation();
    
    /**
     * Returns the MoveUndo that, when applied to given gameState, will undo
     * this move when applied to the gameState.
     * @return The MoveUndo for this move.
     */
    public MoveUndo getUndo();
    
    @Override
	default int compareTo(PlayableMove o) {
		if (o == null) {
			throw new NullPointerException();
		}
		
		if (this instanceof MoveSumito && o instanceof MoveSumito
			|| this instanceof MoveSidestep && o instanceof MoveSidestep) {
			return Integer.signum(this.getNrOfInvolvedMarbles() - o.getNrOfInvolvedMarbles());
		}
		if (this instanceof MoveSumito) { // and other is SideStep
			if (this.getNrOfInvolvedMarbles() < o.getNrOfInvolvedMarbles()) {
				return -1;
			} else {
				return 1;
			}
		} else { // This is sidestep, other is sumito
			if (this.getNrOfInvolvedMarbles() <= o.getNrOfInvolvedMarbles()) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}
