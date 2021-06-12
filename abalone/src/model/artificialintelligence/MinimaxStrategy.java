package model.artificialintelligence;

import com.owlike.genson.annotation.JsonProperty;

import model.artificialintelligence.minimax.DefaultMinimax;
import model.artificialintelligence.minimax.GameStateEvaluator;
import model.artificialintelligence.minimax.Minimax;
import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;

public class MinimaxStrategy implements Strategy {
	//
	Minimax.Builder miniBuilder;
	Minimax minimax = null;
	GameStateEvaluator evaluator;
	long totalTime = 0;
	int weight = 0;
	
	/**
	 * Constructs minimaxStrategy from a minimaxBuilder and gameStateEvaluator.
	 * @param miniBuilder Contains the information on which heuristics and search algorithms to use.
	 * @param evaluator Contains the information on how to rate gameStates.
	 */
	public MinimaxStrategy(@JsonProperty("miniBuilder") Minimax.Builder miniBuilder, 
						   @JsonProperty("evaluator") GameStateEvaluator evaluator) {
		this.miniBuilder = miniBuilder;
		this.evaluator = evaluator;
	}
	
	@Override
	public PlayableMove determineMove(GameState gameState) {
		if (minimax == null) {
			minimax = miniBuilder.build(gameState, evaluator);
		}

		long startTime = System.nanoTime();
		final PlayableMove move = minimax.getBestMove();
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / 1000000;
		((DefaultMinimax) minimax).resetHistoryTable();
		totalTime += duration;
		weight++;
		System.out.println("Average of " 
				+ (totalTime / weight + " milliseconds."));
		return move;
	}
	
	/**
	 * Gets the GameStateEvaluator for this minimaxStrategy.
	 * @return the GameStateEvaluator for this minimaxStrategy.
	 */
	public GameStateEvaluator getEvaluator() {
		if (evaluator == null) {
			evaluator = new GameStateEvaluator.Builder()
					.withMarbleConqueredWeight(50)
					.withCoherenceWeight(7)
					.withDistanceFromCenterWeight(2)
					.withFormationBreakWeight(10)
					.build();
		}
		return evaluator;
	}
}
