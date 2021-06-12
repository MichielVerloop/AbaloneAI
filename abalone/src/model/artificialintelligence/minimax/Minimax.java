package model.artificialintelligence.minimax;

import java.util.List;

import com.owlike.genson.annotation.JsonProperty;

import model.exceptions.IllegalBuildException;
import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;

public interface Minimax {
	//
	PlayableMove getBestMove();
	
	GameState getGameState();
	
	/**
	 * Returns a list of legal moves for the current gameState.
	 * Based on the depth and active heuristics, the list will be sorted.
	 * @param depth The depth for which these moves are created. Hence, 
	 *     if you wish to generate moves for depth 1, you use a depth of 1.
	 * @return A possibly sorted list of all legal moves.
	 */
	List<PlayableMove> getAllLegalMoves(int depth);
	
	public static class Builder {
		@JsonProperty("dfs") private boolean dfs = false;
		@JsonProperty("depthBoundIddfs") private boolean depthBoundIddfs = false;
		@JsonProperty("depth") private int depth = 0;
		
		@JsonProperty("timeBoundIddfs") private boolean timeBoundIddfs = false;
		@JsonProperty("time") private int time = 0;
		
		@JsonProperty("hashing") private boolean hashing = false;
		@JsonProperty("windowNarrowing") private boolean windowNarrowing = false;
		@JsonProperty("evaluateSorting") private boolean evaluateSorting = false;
		@JsonProperty("evaluateSortingMinDepth") private int evaluateSortingMinDepth = 0;
		@JsonProperty("evaluateSortingMaxDepth") private int evaluateSortingMaxDepth = 0;
		@JsonProperty("historyHeuristicSorting") private boolean historyHeuristicSorting = false;
		@JsonProperty("historyHeuristicSortingMinDepth") private int historyHeuristicSortingMinDepth = 0;
		@JsonProperty("historyHeuristicSortingMaxDepth") private int historyHeuristicSortingMaxDepth = 0;
		@JsonProperty("marbleOrdering") private boolean marbleOrdering = false;
		@JsonProperty("marbleOrderingMinDepth") private int marbleOrderingMinDepth = 0;
		@JsonProperty("marbleOrderingMaxDepth") private int marbleOrderingMaxDepth = 0;
		@JsonProperty("iterationSorting") private boolean iterationSorting = false;
		@JsonProperty("iterationSortingMinDepth") private int iterationSortingMinDepth = 0;
		@JsonProperty("iterationSortingMaxDepth") private int iterationSortingMaxDepth = 0;
		
		
		/** Creates a builder for Minimax.
		 */
		public Builder() {
		}
		
		/**
		 * Sets the search algorithm to DFS with the given depth. 
		 * Cannot be used in conjunction with withTimeBoundIddfs or with withDepthBoundIddfs. 
		 * Calling this will override previously set IDDFS/DFS settings.
		 * @param depth The depth till which the minimax will search.
		 * @return this, for argument chaining.
		 */
		public Builder withDfs(int depth) {
			this.dfs = true;
			this.depthBoundIddfs = false;
			this.depth = depth;
			
			this.timeBoundIddfs = false;
			this.time = 0;
			return this;
		}
		

		/**
		 * Sets the search algorithm to IDDFS with the depth limit. 
		 * Cannot be used in conjunction with withDfs or with withTimeBoundIddfs. 
		 * Calling this will override previously set IDDFS/DFS settings.
		 * @param depth The depth till which the minimax will search.
		 * @return this, for argument chaining.
		 */
		public Builder withDepthBoundIddfs(int depth) {
			this.dfs = false;
			this.depthBoundIddfs = true;
			this.depth = depth;
			
			this.timeBoundIddfs = false;
			this.time = 0;
			return this;
		}
		
		/**
		 * Sets the search algorithm to IDDFS with the given time limit. 
		 * Cannot be used in conjunction with withDfs or with withDepthBoundIddfs.
		 * Calling this will override previously set IDDFS/DFS settings.
		 * @param time The time limit for how long the IDDFS can search.
		 * @return this, for argument chaining.
		 */
		public Builder withTimeBoundIddfs(int time) {
			this.dfs = false;
			this.depthBoundIddfs = false;
			this.depth = 0;
			
			this.timeBoundIddfs = true;
			this.time = time;
			return this;
		}
		
		public Builder enableHashing() {
			this.hashing = true;
			return this;
		}
		
		/**
		 * Enables window narrowing, which is reducing the window size based on previous estimations
		 * of the best possible move one can get in a branch.
		 * Also enables hashing, as that is an absolute requirement for window narrowing to work.
		 * @return this, for argument chaining.
		 */
		public Builder enableWindowNarrowing() {
			this.hashing = true;
			this.windowNarrowing = true;
			return this;
		}
		
		/**
		 * Enables the evaluate sorting heuristic while the depth is between minDepth and maxDepth (inclusive).
		 * @param minDepth The minimum depth at which the evaluate sorting heuristic will apply.
		 * @param maxDepth The maximum depth at which the evaluate sorting heuristic will apply.
		 * @return this, for argument chaining.
		 */
		public Builder enableEvaluateSorting(int minDepth, int maxDepth) {
			assert (minDepth <= maxDepth);
			this.evaluateSorting = true;
			this.evaluateSortingMinDepth = minDepth;
			this.evaluateSortingMaxDepth = maxDepth;
			return this;
		}
		
		/**
		 * Enables the history sorting heuristic while the depth is between minDepth and maxDepth (inclusive).
		 * @param minDepth The minimum depth at which the evaluate sorting heuristic will apply.
		 * @param maxDepth The maximum depth at which the evaluate sorting heuristic will apply.
		 * @return this, for argument chaining.
		 */
		public Builder enableHistoryHeuristicSorting(int minDepth, int maxDepth) {
			assert (minDepth <= maxDepth);
			this.historyHeuristicSorting = true;
			this.historyHeuristicSortingMinDepth = minDepth;
			this.historyHeuristicSortingMaxDepth = maxDepth;
			return this;
		}
		
		/**
		 * Enables the marble ordering heuristic while the depth is between minDepth and maxDepth (inclusive).
		 * @param minDepth The minimum depth at which the marble ordering heuristic will apply.
		 * @param maxDepth The maximum depth at which the marble ordering heuristic will apply.
		 * @return this, for argument chaining.
		 */
		public Builder enableMarbleOrdering(int minDepth, int maxDepth) {
			assert (minDepth <= maxDepth);
			this.marbleOrdering = true;
			this.marbleOrderingMinDepth = minDepth;
			this.marbleOrderingMaxDepth = maxDepth;
			return this;
		}
		
		/**
		 * Enables the iteration sorting heuristic while the depth is between minDepth and maxDepth (inclusive).
		 * @param minDepth The minimum depth at which the iteration sorting heuristic will apply.
		 * @param maxDepth The maximum depth at which the iteration sorting heuristic will apply.
		 * @return this, for argument chaining.
		 */
		public Builder enableIterationSorting(int minDepth, int maxDepth) {
			assert (minDepth <= maxDepth);
			this.iterationSorting = true;
			this.iterationSortingMinDepth = minDepth;
			this.iterationSortingMaxDepth = maxDepth;
			return this;
		}
		
		/**
		 * Builds a minimax from the given arguments.
		 * @param gameState GameState for which the minimax will work.
		 * @param evaluator The GameStateEvaluator that determines how the gameState is rated.
		 * @return A minimax from the given build arguments.
		 * @throws IllegalBuildException If neither withDfs nor withIddfs was called.
		 */
		public Minimax build(GameState gameState, GameStateEvaluator evaluator) 
			throws IllegalBuildException {
			Minimax result;
			if (dfs) {
				result = new DepthFirstSearch(gameState, evaluator, depth);
			} else if (timeBoundIddfs) {
				result = new TimeBoundIterativeDeepeningDfs(gameState, evaluator, time);
			} else if (depthBoundIddfs) {
				result = new DepthBoundIterativeDeepeningDfs(gameState, evaluator, depth);
			} else {
				throw new IllegalBuildException("Build must use dfs or timeBoundIddfs.");
			}
			
			if (hashing) {
				((DefaultMinimax)result).evaluator.enableHashing();
				((DefaultMinimax)result).enableWindowNarrowing();
			}
			if (evaluateSorting) {
				((DefaultMinimax)result).enableEvaluateSorting(
						evaluateSortingMinDepth, evaluateSortingMaxDepth);
			}
			if (historyHeuristicSorting) {
				((DefaultMinimax)result).enableHistoryHeuristicSorting(
						historyHeuristicSortingMinDepth, historyHeuristicSortingMaxDepth);
			}
			if (marbleOrdering) {
				((DefaultMinimax)result).enableMarbleOrdering(
						marbleOrderingMinDepth, marbleOrderingMaxDepth);
			}
			if (iterationSorting) {
				if (hashing) {
					((DefaultMinimax)result).enableIterationSorting(
							iterationSortingMinDepth, iterationSortingMaxDepth);
				} else {
					System.err.println("Warning: Iteration sorting cannot function without "
							+ "the hashing functionality. Continuing program execution "
							+ "without hashing and without iteration sorting.");
				}
			}
			return result;
		}
		
	}
}
