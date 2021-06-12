package model.artificialintelligence;

import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;

public interface Strategy {
    //
    /**
     * Given a board, the underlying strategy will come up with a
     * move to make.
     * @param gameState The board on which the move should be made.
     * @requires gameState != null
     * @ensures Move != null
     * @return Move
     */
    public PlayableMove determineMove(GameState gameState);
}
