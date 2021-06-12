package model.artificialintelligence.minimax;

import model.artificialintelligence.RandomStrategy;
import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;

public class DepthBoundIterativeDeepeningDfs extends DefaultMinimax {
	//
	private int depth;
	
	/**
	 * Creates a new minimax for the current player of the given GameState.
	 */
	public DepthBoundIterativeDeepeningDfs(
			GameState gameState, GameStateEvaluator evaluator, int depth) {
		super(gameState, evaluator);
		this.depth = depth;
	}
	
	public PlayableMove getBestMove() {
		return iterativeDeepeningDepthFirstSearch(depth);
	}
	
	/**
	 * Returns the best rating of the gameState possible for gameState.currentPlayer().getTeam(),
	 * optimized for this.optimizingTeam
	 * @param depthLimit How deep the IDDFS will go.
	 * @return The best move that can be applied to the gameState for gameState.currentPlayer().
	 */
	public PlayableMove iterativeDeepeningDepthFirstSearch(int depthLimit) {
		assert (depthLimit > 0);
		PlayableMove bestMove = null;
		
		// While the time limit has not been reached, increase depth:
		for (int depth = 1; depth <= depthLimit; depth++) {
			try {
				bestMove = minimax(depth);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (bestMove == null) {
			System.err.println("TimeBoundIterativeDeepeningDfs was unable "
					+ "to supply a move before it got interrupted, "
					+ "supplying a random move instead.");
			return new RandomStrategy().determineMove(gameState);
		}
		return bestMove;
	}
}
