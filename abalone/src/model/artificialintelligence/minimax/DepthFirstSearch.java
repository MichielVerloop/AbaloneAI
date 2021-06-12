package model.artificialintelligence.minimax;

import model.artificialintelligence.RandomStrategy;
import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;

public class DepthFirstSearch extends DefaultMinimax {
	//
	protected int depth;
	
	public DepthFirstSearch(GameState gameState, GameStateEvaluator evaluator, int depth) {
		super(gameState, evaluator);
		this.depth = depth;
	}
	
	@Override
	public PlayableMove getBestMove() {
		try {
			return minimax(depth);
		} catch (InterruptedException e) {
			System.err.println("DepthFirstSearch was unable to supply a move before "
					+ "it got interrupted, supplying a random move instead.");
			return new RandomStrategy().determineMove(gameState);
		}
	}

}
