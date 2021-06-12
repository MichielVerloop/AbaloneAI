package model.artificialintelligence.minimax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import model.artificialintelligence.minimax.TranspositionTable.TranspositionValue;
import model.artificialintelligence.minimax.TranspositionTable.TranspositionValue.Flag;
import model.gamelogic.GameState;
import model.gamelogic.Move;
import model.gamelogic.MoveSumito;
import model.gamelogic.MoveUndo;
import model.gamelogic.PlayableMove;
import model.gamelogic.Team;


public abstract class DefaultMinimax implements Minimax {
	//
	private static int DEFAULT_DEPTH = 2;
	
	protected GameState gameState;
	protected GameStateEvaluator evaluator;
	protected Deque<MoveUndo> history;
	protected Team optimizingTeam;
	
	MoveHistoryTable historyTable;
	protected ReentrantLock gameStateLock = new ReentrantLock();
	
	private boolean windowNarrowing		= false;
	private boolean evaluateSorting		= false;
	int evaluateSortingMinDepth			= 0;
	int evaluateSortingMaxDepth			= 0;
	boolean historyHeuristicSorting		= false;
	int historyHeuristicSortingMinDepth	= 0;
	int historyHeuristicSortingMaxDepth	= 0;
	boolean marbleOrdering				= false;
	int marbleOrderingMinDepth			= 0;
	int marbleOrderingMaxDepth			= 0;
	boolean iterationSorting			= false;
	int iterationSortingMinDepth		= 0;
	int iterationSortingMaxDepth		= 0;
	
	/**
	 * Constructor for default minimax: The base minimax that is implemented by IDFS and DFS versions.
	 * @param gameState The GameState on which the minimax implementation will find a move.
	 * @param evaluator The evaluator that the current player in the given gameState uses.
	 */
	public DefaultMinimax(GameState gameState, GameStateEvaluator evaluator) {
		this.gameState = gameState;
		this.evaluator = evaluator;
		this.history = new ArrayDeque<>();
		this.optimizingTeam = gameState.getCurrentPlayer().getTeam();
		this.historyTable = new MoveHistoryTable();
	}

	void enableWindowNarrowing() {
		this.windowNarrowing = true;
	}
	
	void enableEvaluateSorting(int minDepth, int maxDepth) {
		this.evaluateSorting = true;
		this.evaluateSortingMinDepth = minDepth;
		this.evaluateSortingMaxDepth = maxDepth;
	}
	
	void enableHistoryHeuristicSorting(int minDepth, int maxDepth) {
		this.historyHeuristicSorting = true;
		this.historyHeuristicSortingMinDepth = minDepth;
		this.historyHeuristicSortingMaxDepth = maxDepth;
	}
	
	void enableMarbleOrdering(int minDepth, int maxDepth) {
		this.marbleOrdering = true;
		this.marbleOrderingMinDepth = minDepth;
		this.marbleOrderingMaxDepth = maxDepth;
	}
	
	void enableIterationSorting(int minDepth, int maxDepth) {
		this.iterationSorting = true;
		this.iterationSortingMinDepth = minDepth;
		this.iterationSortingMaxDepth = maxDepth;
	}
	
	public void resetHistoryTable() {
		this.historyTable = new MoveHistoryTable();
	}
	
	@Override
 	public PlayableMove getBestMove() {
		try {
			return minimax(DEFAULT_DEPTH);
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	@Override
	public GameState getGameState() {
		return this.gameState;
	}

	@Override
	public List<PlayableMove> getAllLegalMoves(int depth) {
		assert (depth > 0);
		List<PlayableMove> allMoves = new ArrayList<PlayableMove>(Move.allLegalMoves(gameState));
		if (evaluateSorting && evaluateSortingMinDepth <= depth && depth <= evaluateSortingMaxDepth) {
			allMoves = evaluateSort(allMoves);
		}
		if (historyHeuristicSorting 
				&& historyHeuristicSortingMinDepth <= depth 
				&& depth <= historyHeuristicSortingMaxDepth) {
			allMoves = historySort(allMoves);
		}
		if (marbleOrdering && marbleOrderingMinDepth <= depth && depth <= marbleOrderingMaxDepth) {
			allMoves = marbleSort(allMoves);
		}
		if (iterationSorting && iterationSortingMinDepth <= depth && depth <= iterationSortingMaxDepth) {
			allMoves = iterationSort(allMoves);
		}
		return allMoves;
	}

	private List<PlayableMove> evaluateSort(List<PlayableMove> allMoves) {
		List<ScoredMove> scoredMoves = new ArrayList<>();
		for (PlayableMove move : allMoves) {
			scoredMoves.add(new ScoredMove(
					move, 
					evaluator.rateMove(gameState, optimizingTeam, move)));
		}
		
		scoredMoves.sort(Comparator.reverseOrder());
		return scoredMoves.stream()
			.map((scoredMove) -> scoredMove.move)
			.collect(Collectors.toList());
	}
	
	private List<PlayableMove> historySort(List<PlayableMove> allMoves) {
		List<ScoredMove> scoredMoves = new ArrayList<>();
		for (PlayableMove move : allMoves) {
			scoredMoves.add(new ScoredMove(
					move, 
					historyTable.get(move)));
		}
		
		scoredMoves.sort(Comparator.reverseOrder());
		return scoredMoves.stream()
			.map((scoredMove) -> scoredMove.move)
			.collect(Collectors.toList());
	}
	
	static List<PlayableMove> marbleSort(List<PlayableMove> allMoves) {
		Map<Integer, List<PlayableMove>> countingSort = new HashMap<>();
		for (int i = 0; i < 7; i++) {
			countingSort.put(i, new ArrayList<>());
		}
		for (PlayableMove move : allMoves) {
			countingSort.get(moveGroup(move)).add(move);
		}
		
		List<PlayableMove> result = countingSort.get(0);
		for (int i = 1; i < 7; i++) {
			result.addAll(countingSort.get(i));
		}
		return result;
		
	}
	
	/**
	 * Converts a move into an integer on which it can be sorted for marble ordering.
	 * @param move Move to be converted into an integer
	 * @return 0 for the moves that should be explored first, 6 for the moves that should be explored last.
	 */
	static int moveGroup(PlayableMove move) {
    	if (move instanceof MoveSumito) {
    		switch (move.getNrOfInvolvedMarbles()) {
    			case 5: return 0;
    			case 4: return 1;
    			case 3: return 2;
    			case 2: return 4;
    			case 1: return 6;
    			default: return -1;
    		}
    	} else {
    		switch (move.getNrOfInvolvedMarbles()) {
    			case 3: return 3;
    			case 2: return 5;
    			default: return -1;
    		}
    	}
    }
	
	private List<PlayableMove> iterationSort(List<PlayableMove> allMoves) {
		// Add moves that have a hashed rating to initialSort, then sort it.
		List<ScoredMove> filteredList = new ArrayList<>();
		for (PlayableMove move : allMoves) {
			Integer rating = evaluator.getStoredMoveRating(gameState, optimizingTeam, move);
			if (rating != null) {
				filteredList.add(new ScoredMove(move, rating));
			}
		}
		filteredList.sort(Comparator.reverseOrder());
		List<PlayableMove> filteredSortedList = filteredList
				.stream()
				.map((scoredMove) -> scoredMove.move)
				.collect(Collectors.toList());
		// Isolate the best oneThird & the worst oneSixth from the initialSort as per the paper.
		// Remove all elements from these lists in the original list of moves, then add the best
		// and worst at the front and back of the list respectively.
		List<PlayableMove> bestOneThird = filteredSortedList
				.subList(0, filteredSortedList.size() / 3);
		List<PlayableMove> worstOneSixth = filteredSortedList
				.subList(filteredSortedList.size() * 5 / 6, filteredSortedList.size());
		allMoves.removeAll(bestOneThird);
		allMoves.removeAll(worstOneSixth);
		allMoves.addAll(0, bestOneThird);
		allMoves.addAll(worstOneSixth);
		
		// Find the indices in allMoves for which the moves have been rated already. 
		List<Integer> evaluationIndices = new ArrayList<>();
		for (int i = allMoves.size() / 3; i < allMoves.size() * 5 / 6; i++) {
			if (filteredSortedList.contains(allMoves.get(i))) {
				evaluationIndices.add(i);
			}
		}
		// For these indices, apply the sorted ordering.
		for (int i = 0; i < evaluationIndices.size(); i++) {
			allMoves.set(evaluationIndices.get(i), 
					filteredSortedList.get(filteredSortedList.size() / 3 + i));
		}
		return allMoves;
	}
	
	/**
	 * Returns the best rating of the gameState possible for gameState.currentPlayer().getTeam(),
	 * optimized for this.optimizingTeam
	 * @param depthLimit How much deeper the DFS will go.
	 * @return The best move that can be applied to the gameState. 
	 * @throws InterruptedException If the thread is interrupted while executing this function.
	 */
	protected PlayableMove minimax(int depthLimit) throws InterruptedException {
		gameState.gameStats.registerScore(evaluator.rateGameState(gameState, optimizingTeam, true));
		int bestScore = Integer.MIN_VALUE;
		PlayableMove bestMove = null;
		gameStateLock.lock();
		try {
			List<PlayableMove> legalMoves = getAllLegalMoves(1);
			gameState.gameStats.registerBranchingFactor(legalMoves.size(), 1);
			for (PlayableMove move : legalMoves) {
				int score = 0; 
				try {
					score = minimax(depthLimit, 1, bestScore, Integer.MAX_VALUE, move);
				} catch (InterruptedException e) {
					// Reset the history to return to the actual gameState, then rethrow.
					while (!history.isEmpty()) {
						gameState.makeMove(history.removeLast());
					}
					throw e;
				}
				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
				}
			}
			// If all moves are losing moves, return an arbitrary move.
			if (bestMove == null) {
				System.out.println("All moves are losing. Making an arbitrary move.");
				return getAllLegalMoves(1).get(0);
			}
			assert (!gameState.gameHistory.isRepetition(GameStateEvaluator.hashOfBoard(gameState.getBoard().getBoardHash(), bestMove)));
			
			// Save the best results if hashing is enabled.
			saveResults(Integer.MIN_VALUE,
					Integer.MAX_VALUE,
					evaluator.transpositionTable.get(gameState, gameState.getCurrentTeam()), 
					depthLimit, 
					0, 
					bestScore);
		} finally {
			gameStateLock.unlock();
		}
		// Register stats. In the case of iterative deepening methods, it is either overridden or added to as necessary.
		gameState.gameStats.registerDepth(depthLimit); // Overrides it for this turn so time-based iddfs gives the final value.
		gameState.gameStats.registerTranspositionTableSize(evaluator.transpositionTable.size()); // Overrides it in the same way.
		gameState.gameStats.commitDepth();
		return bestMove;
	}
	
	/**
	 * Returns the best rating of the gameState possible for gameState.currentPlayer().getTeam(),
	 * optimized for this.optimizingTeam
	 * @param depthLimit The maximum depth that can be reached by the DFS.
	 * @param currentDepth The current depth that has been reached by the DFS.
	 * @param alpha The minimum score that the maximizing player can guarantee
	 * @param beta The maximum score that the minimizing player can guarantee
	 * @param move The move that should be applied to the gameState in this branch or leaf.
	 * @return the best score that can be attained in the current gameState.
	 * @throws InterruptedException If the thread is interrupted while executing this function.
	 */
	protected int minimax(int depthLimit, int currentDepth, int alpha, int beta, PlayableMove move) 
			throws InterruptedException {
		assert (currentDepth <= depthLimit);
		int originalAlpha = alpha;
		int originalBeta = beta;
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		
		history.addLast(gameState.makeMove(move));
		gameState.gameStats.addNodeVisited();
		
		if (currentDepth == depthLimit) { // if leaf node
			// Will use hashed value if enabled
			int rating = evaluator.rateGameState(gameState, optimizingTeam); 
			gameState.makeMove(history.removeLast());
			gameState.gameStats.addLeafNode();
			return rating;
		}
		
		// If hashing is enabled & this state has already been explored
		TranspositionValue entry = null;
		if (evaluator.isHashingEnabled()) {
			entry = evaluator.transpositionTable
					.get(gameState, gameState.getCurrentTeam());
			// If the depth of subtree is better than or equal to the remaining depth, use the exact value.
			// System.out.println(val.depthOfSubtree + "\t" + depthLimit + "\t" + currentDepth);
			if (entry != null && entry.depthOfSubtree >= depthLimit - currentDepth) {
				//System.out.println("Cut made at depth: " + currentDepth);
				if (entry.flag == Flag.EXACT) {
					gameState.makeMove(history.removeLast());
					gameState.gameStats.addExactCut();
					return entry.value;
				}
				if (windowNarrowing) {
					if (entry.flag == Flag.LOWER_BOUND) {
						alpha = Math.max(alpha, entry.value);
					} else if (entry.flag == Flag.UPPER_BOUND) {
						beta = Math.min(beta, entry.value);
					}
					if (alpha >= beta) {
						gameState.makeMove(history.removeLast());
						gameState.gameStats.addWindowCut();
						return entry.value;
					}
					gameState.gameStats.addWindowNarrowed();
				}
			}
		}
		
		int best;
		List<PlayableMove> legalMoves = getAllLegalMoves(currentDepth + 1);
		if (gameState.getCurrentTeam().equals(optimizingTeam)) {
			best = Integer.MIN_VALUE;
			
			// Recursion for all legal moves of yours.
			int i;
			for (i = 0; i < legalMoves.size(); i++) {
				PlayableMove childMove = legalMoves.get(i);
				int value = minimax(depthLimit, currentDepth + 1, alpha, beta, childMove);

				best = Math.max(alpha, value);
				alpha = Math.max(alpha, best);
				
				if (alpha >= beta) {
					historyTable.incrementKills(childMove, depthLimit - currentDepth);
					i++; // breaking avoids the i++, so we have to do it manually to keep a consistent branching factor.
					break;
				}
			}
			gameState.gameStats.registerBranchingFactor(i, currentDepth + 1);
			// Save the best results if hashing is enabled.
			saveResults(originalAlpha, originalBeta, entry, depthLimit, currentDepth, best);
		} else {
			best = Integer.MAX_VALUE;
			
			// Recursion for all legal moves of the opponent.
			int i;
			for (i = 0; i < legalMoves.size(); i++) {
				PlayableMove childMove = legalMoves.get(i);
				int value = minimax(depthLimit, currentDepth + 1, alpha, beta, childMove);

				best = Math.min(best, value);
				beta = Math.min(beta, best);
				
				if (alpha >= beta) {
					historyTable.incrementKills(move, depthLimit - currentDepth);
					i++; // breaking avoids the i++, so we have to do it manually to keep a consistent branching factor.
					break;
				}
			}
			gameState.gameStats.registerBranchingFactor(i, currentDepth + 1);
			//Save the best results if hashing is enabled and the results are deeper than the previous entry
			saveResults(originalAlpha, originalBeta, entry, depthLimit, currentDepth, best);
		}
		gameState.makeMove(history.removeLast());
		return best;
	}
	
	private void saveResults(int originalAlpha, int beta, TranspositionValue entry, 
			int depthLimit, int currentDepth, int best) {
		if (evaluator.isHashingEnabled()) {
			// If there previously was no entry or the information is 
			// more relevant than the previous entry, save the results.
			if (entry == null || depthLimit - currentDepth >= entry.depthOfSubtree) {
				Flag flag = Flag.EXACT;
				if (best <= originalAlpha) {
					flag = Flag.UPPER_BOUND;
				}
				if (best >= beta) {
					flag = Flag.LOWER_BOUND;
				}
				evaluator.createTranspositionTableEntry(gameState, gameState.getCurrentTeam(), 
						depthLimit - currentDepth, best, flag);
			}
		}
	}
}
