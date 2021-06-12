package model.artificialintelligence.minimax;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import model.artificialintelligence.RandomStrategy;
import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;

public class TimeBoundIterativeDeepeningDfs extends DefaultMinimax {
	//
	private int time;
	
	/**
	 * Creates a new minimax for the current player of the given GameState.
	 */
	public TimeBoundIterativeDeepeningDfs(
			GameState gameState, GameStateEvaluator evaluator, int time) {
		super(gameState, evaluator);
		this.time = time;
	}
	
	public PlayableMove getBestMove() {
		return iterativeDeepeningDepthFirstSearch(time);
	}
	
	/**
	 * Returns the best rating of the gameState possible for gameState.currentPlayer().getTeam(),
	 * optimized for this.optimizingTeam
	 * @param time How deep the IDDFS will go.
	 * @return The best move that can be applied to the gameState for gameState.currentPlayer().
	 */
	public PlayableMove iterativeDeepeningDepthFirstSearch(int time) {
		assert (time > 0);
		long startTime = System.currentTimeMillis();
		PlayableMove bestMove = null;
		
		// While the time limit has not been reached, increase depth:
		for (int depth = 1; startTime + time * 1000 > System.currentTimeMillis(); depth++) {
			// Start minimax thread to generate a better move.
			final int immutableDepth = depth;
			Future<PlayableMove> futureMove = Executors.newCachedThreadPool()
					.submit(() -> minimax(immutableDepth)); 
			
			// Retrieve the move. On timeout, interrupt the thread and wait for it to finish.
			try {
				bestMove = futureMove.get(
						time * 1000 - (System.currentTimeMillis() - startTime), 
						TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				if (e instanceof TimeoutException) {
					futureMove.cancel(true);
					// Wait for the futureMove thread to properly die.
					try {
						gameStateLock.lock();
					} finally {
						gameStateLock.unlock();
					}
				} else {
					e.printStackTrace();
				}
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
