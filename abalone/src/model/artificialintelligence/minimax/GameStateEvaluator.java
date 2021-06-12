package model.artificialintelligence.minimax;

import com.owlike.genson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import model.artificialintelligence.minimax.TranspositionTable.TranspositionValue;
import model.artificialintelligence.minimax.TranspositionTable.TranspositionValue.Flag;
import model.gamelogic.Board;
import model.gamelogic.GameState;
import model.gamelogic.Marble;
import model.gamelogic.Move;
import model.gamelogic.MoveUndo;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;
import model.gamelogic.Team;
import model.hex.Direction;
import model.hex.Hex;

public class GameStateEvaluator {
	//
	
	public static final int MAX_WEIGHT = 10000;
	static final Map<MarbleOracleEntry, Long> marblePositionOracle = initZobrist();
	
	/**
	 * Used to generate an immutable map of all possible MarbleOracleEntries going to random longs.
	 * @return An immutable map of all possible marble hashes.
	 */
	private static Map<MarbleOracleEntry, Long> initZobrist() {
		Map<MarbleOracleEntry, Long> result = new HashMap<>();
		for (MarbleOracleEntry key : MarbleOracleEntry.allPossibleEntries()) {
			long random;
			do {
				random = ThreadLocalRandom.current().nextLong();
			} while (result.containsValue(random));
			result.put(key, random);
		}
		return Collections.unmodifiableMap(result);
	}
	
	/**
	 * Returns the hash of the board that would follow from applying the move on the current board state.
	 * @param boardHash The hash of the current board.
	 * @param move The Move that would be applied.
	 * @return the hash of the board, xor'd with the hash of the move.
	 */
	static long hashOfBoard(Board board, Move move) {
		return hashOfBoard(hashOfBoard(board), move);
	}
	
	/**
	 * Returns the hash of the board that would follow from applying the move 
	 * on the current board state, with hash boardHash.
	 * @param boardHash The hash of the current board.
	 * @param move The Move that would be applied.
	 * @return the hash of the board, xor'd with the hash of the move.
	 */
	public static long hashOfBoard(long boardHash, Move move) {
		return boardHash ^ hashOfMove(move);
	}
	
	/**
	 * Returns a Zobrist hash of the board.
	 * Achieves this by emulating a random oracle for the key combination of the location 
	 * of a marble and its owner's color, then xor'ing all those values.
	 * @param board The Board for which the hash is computed.
	 * @return semi-random long.
	 */
	public static long hashOfBoard(Board board) {
		long hash = 0;
		for (Marble marble : board.getMarbles()) {
			hash ^= hashOfMarble(marble);
		}
		return hash;
	}
	
	/**
	 * Returns a Zobrist hash of the move.
	 * Achieves this by emulating a random oracle for the key combination of the location 
	 * of a marble and its owner's color, then xor'ing all those values.
	 * @param board The Board for which the hash is computed.
	 * @return semi-random long.
	 */
	static long hashOfMove(Move move) {
		long hash = 0;
		for (Marble marble : move.getMarbles()) {
			MarbleOracleEntry entry = new MarbleOracleEntry(marble);
			
			// Add the hash of the current position.
			hash ^= hashOfMarble(entry);
			
			// Shift the marble to the move direction and add that hash.
			entry = entry.shift(move.getDirection());
			hash ^= hashOfMarble(entry);
		}
		
		return hash;
	}
	
	static long hashOfMarble(Marble marble) {
		return marble.isCaptured() ? 0 : 
			hashOfMarble(new MarbleOracleEntry(marble.getHex(), marble.getColor()));
	}	

	private static long hashOfMarble(MarbleOracleEntry marble) {
		return marble.hex.length() >= Board.BOARD_RADIUS ? 0 :
			marblePositionOracle.get(marble);
	}
	
	@JsonProperty("abaPro") private boolean abaPro;
	@JsonProperty("considerEnemyPosition") private boolean considerEnemyPosition;
	@JsonProperty("marblesConqueredWeight") private int marblesConqueredWeight; 
	@JsonProperty("distanceFromCenterWeight") private int distanceFromCenterWeight; 
	@JsonProperty("coherenceWeight") private int coherenceWeight;
	@JsonProperty("formationBreakWeight") private int formationBreakWeight;
	@JsonProperty("immediateMarbleCapWeight") private int immediateMarbleCapWeight;
	@JsonProperty("singleMarbleCapWeight") private int singleMarbleCapWeight;
	@JsonProperty("doubleMarbleCapWeight") private int doubleMarbleCapWeight;
	private int ratingLowerBound;
	private int ratingUpperBound;
    TranspositionTable transpositionTable;
    int gameScoreHashes;
	
    private boolean hashing;
	
	/**
	 * Creates a new GameStateEvaluator.
	 * @param gameState GameState that is tracked to be rated.
	 */
	GameStateEvaluator() {
		this.transpositionTable = new TranspositionTable();
		gameScoreHashes = 0;
		hashing = false;
	}
	
	/**
	 * Enables hashing of gameState ratings.
	 */
	public void enableHashing() {
		this.hashing = true;
	}
	
	public boolean isHashingEnabled() {
		return hashing;
	}
	
	/**
	 * Initializes the rating bounds using the weights of this.
	 */
	void setRatingBounds() {
		// We just need to find a value related to the weights that is larger than we'll ever get.
		// maximize penalties, set bonuses to 0.
		ratingUpperBound = 6 * marblesConqueredWeight
						 + 7 * 14 * coherenceWeight 
						 + 4 * 14 * distanceFromCenterWeight
						 + 11 * formationBreakWeight
						 + 4 * 14 * immediateMarbleCapWeight
						 + 4 * 14 * singleMarbleCapWeight
						 + 4 * 14 * doubleMarbleCapWeight;
		ratingLowerBound = -ratingUpperBound;
	}
	
	/**
	 * Rates the current gameState by computing a number of factors for the optimizingTeam's marbles.
	 * @param gameState The gameState which is rated.
	 * @param optimizingTeam The team for which the gameState is being optimized.
	 * @return an integer describing the strength of the gameState for optimizingTeam.
	 */
	int rateGameState(GameState gameState, Team optimizingTeam) {
		return rateGameState(gameState, optimizingTeam, false);
	}
	
	/**
	 * Rates the current gameState by computing a number of factors for the optimizingTeam's marbles.
	 * @param gameState The gameState which is rated.
	 * @param optimizingTeam The team for which the gameState is being optimized.
	 * @param disableHashing If true: disables the use of hashing for this function call only.
	 *     else, the use of hashing is determined by evaluator.isHashingEnabled(). 
	 * @return an integer describing the strength of the gameState for optimizingTeam.
	 */
	int rateGameState(GameState gameState, Team optimizingTeam, boolean disableHashing) {
		boolean useHashing = hashing && !disableHashing;
		
		// Attempts to use the transposition table.
		if (useHashing) {
			TranspositionValue val = transpositionTable.get(gameState, optimizingTeam);
			if (val != null) {
				return val.value; 
			}
		}
		
		final Board board = gameState.getBoard();
		
		int result = abaPro 
				? computeRatingAbaPro(gameState, optimizingTeam, board) 
				: computeRatingPapadopoulos(gameState, optimizingTeam, board);
		// If this is a final state, override the result.
		if (gameState.isFinished()) {
			if (gameState.getWinner() == optimizingTeam) {
				result = Integer.MAX_VALUE;
			} else if (gameState.getWinner() != null) { 
				// If it is not a draw and the winner is not the optimizing team, 
				// then the other team must be winning.
				result = Integer.MIN_VALUE;
			}
		}
		if (useHashing) {
			createTranspositionTableEntry(gameState, optimizingTeam, 0, result, Flag.EXACT);
		}
		return result;
	}
	
	private int computeRatingAbaPro(GameState gameState, Team optimizingTeam, final Board board) {
		// 2. Take a weighted average of these centers and the center of the game board (which gets weighted
		//    with a factor below one). Call this reference point R.
		//    The center mass of every team has a weight of 3, center has a weight of 1.
		
		Hex R = new Hex(0, 0, 0); // center
		// 1. Compute the centers of mass of the respective teams.
		for (Team team : gameState.getTeams()) {
			R = R.add(team.computeCenterMass(board).scale(3));
		}
		R = R.divide(gameState.getTeams().size() * 3 + 1);
		
		// 4. The discrepancy of the two sums now gives the score of the position
		int score = 0;
		for (Team team : gameState.getTeams()) {
			// 3. Sum up the distances of all balls to R
			score += optimizingTeam.equals(team) 
					? -team.computeDistanceFromR(R)
					: team.computeDistanceFromR(R);
		}
		return score;
	}

	private int computeRatingPapadopoulos(GameState gameState, Team optimizingTeam, final Board board) {
		int marblesConqueredRating = 2 * optimizingTeam.getConqueredMarbles().size() 
				- gameState.getTotalNrOfConqueredMarbles();
		int coherence = 0;
		int distance = 0;
		int formationBreak = 0;
		int immediateMarbleCapturingDanger = 0;
		int singleMarbleCapturingDanger = 0;
		int doubleMarbleCapturingDanger = 0;
		List<Team> teams = considerEnemyPosition ? gameState.getTeams() : Arrays.asList(optimizingTeam);
		for (Team team : teams) {
			boolean isOptimizingTeam = team.equals(optimizingTeam);
			coherence += isOptimizingTeam
					?  team.computeCoherence(board) 
					: -team.computeCoherence(board);
			distance += isOptimizingTeam
					?  team.computeDistanceFromCenter()
					: -team.computeDistanceFromCenter();
			formationBreak = isOptimizingTeam
					?  team.computeFormationBreak(board)
					: -team.computeFormationBreak(board);
			immediateMarbleCapturingDanger = immediateMarbleCapWeight == 0 ? 0 : isOptimizingTeam
					?  team.computeImmediateMarbleCapturingDanger(board)
					: -team.computeImmediateMarbleCapturingDanger(board);
			singleMarbleCapturingDanger = singleMarbleCapWeight == 0 ? 0 : isOptimizingTeam
					?  team.computeSingleMarbleCapturingDanger(board)
					: -team.computeSingleMarbleCapturingDanger(board);
			doubleMarbleCapturingDanger = doubleMarbleCapWeight == 0 ? 0 : isOptimizingTeam
					?  team.computeDoubleMarbleCapturingDanger(board)
					: -team.computeDoubleMarbleCapturingDanger(board);
		}
		int result = marblesConqueredRating * marblesConqueredWeight
				+ coherence * coherenceWeight
				- distance * distanceFromCenterWeight
				+ formationBreak * formationBreakWeight
				- immediateMarbleCapturingDanger * immediateMarbleCapWeight
				- singleMarbleCapturingDanger * singleMarbleCapWeight
				- doubleMarbleCapturingDanger * doubleMarbleCapWeight;
		// Normalize result
		if (ratingLowerBound != 0 && result < 0) {
			result = result * (-MAX_WEIGHT / ratingLowerBound);
		} else if (ratingUpperBound != 0 && result > 0) {
			result = result * (MAX_WEIGHT / ratingUpperBound);
		}
		return result;
	}

	/**
	 * Rates the gameState that would result from applying the given move to the current gameState.
	 * @param gameState The current gameState.
	 * @param optimizingTeam The team for which to score the gameState resulting from the move.
	 * @param move The move that will be applied to the current gameState.
	 * @return The score of the gameState that results from applying the move.
	 */
	public int rateMove(GameState gameState, Team optimizingTeam, PlayableMove move) {
		// Attempts to use the hashing table
		Integer hashedRating = getStoredMoveRating(gameState, optimizingTeam, move);
		if (hashedRating != null) {
			return hashedRating;
		}
		
		// Hashing failed, so we apply the move, rate the gameState (which hashes the result if it's allowed),
		// then return the result.
		MoveUndo undo = gameState.makeMove(move);
		int rating = rateGameState(gameState, optimizingTeam);
		gameState.makeMove(undo);
		
		return rating;
	}

	/**
	 * Retrieves the stored rating for a move if one exists. If one doesn't exist, null.
	 * @param gameState The current gameState, whose hash will be combined with the move's hash to retrieve 
	 *     the rating.
	 * @param optimizingTeam The team to optimize for.
	 * @param move The move whose hash will be combined with the board to return a value if one exists.
	 * @return The rating for the move if one was found, null otherwise.
	 */
	Integer getStoredMoveRating(GameState gameState, Team optimizingTeam, PlayableMove move) {
		if (hashing) {
			long boardHash = hashOfBoard(gameState.getBoard().getBoardHash(), move); 
			TranspositionValue val = transpositionTable.get( 
					boardHash,
					optimizingTeam);
			if (val != null) {
				return val.value;
			}
		}
		return null;
	}
	
    void createTranspositionTableEntry(GameState gameState, Team team, 
    		int depthOfSubTree, int evaluationValue, Flag flag) {
    	transpositionTable.put(gameState, team, depthOfSubTree, evaluationValue, flag);
		gameScoreHashes++;
    }
    	
    private static class MarbleOracleEntry {
		Hex hex;
		String color;
		
		private static Set<MarbleOracleEntry> allPossibleEntries() {
			Set<MarbleOracleEntry> result = new HashSet<>();
			for (Hex hex : Hex.build(Board.BOARD_RADIUS)) {
				for (String color : Player.COLORS) {
					result.add(new MarbleOracleEntry(hex, color));
				}
			}
			return result;
		}
		
		MarbleOracleEntry(Marble marble) {
			this(marble.getHex(), marble.getColor());
		}
		
		MarbleOracleEntry(Hex hex, String color) {
			this.hex = hex;
			this.color = color;
		}
		
		MarbleOracleEntry shift(Direction dir) {
			return new MarbleOracleEntry(hex.neighbour(dir), color); 
		}
		
		@Override
	    public boolean equals(Object other) {
	        if (other instanceof MarbleOracleEntry) {
	            return hex.equals(((MarbleOracleEntry) other).hex)
	            		&& color.equals(((MarbleOracleEntry) other).color);
	        }
	        return false;
	    }
	    
	    @Override
	    public int hashCode() {
	        return hex.hashCode() * 11
	        		+ color.hashCode() * 13;
        }
	}

	
 	public static class Builder {
 		
		private int marblesConqueredWeight		= 0; 
		private int distanceFromCenterWeight	= 0; 
		private int coherenceWeight				= 0;
		private int formationBreakWeight		= 0;
		private int singleMarbleCapWeight		= 0;
		private int doubleMarbleCapWeight		= 0;
		private int immediateMarbleCapWeight	= 0;
		private boolean abaPro					= false;
		private boolean hashing 				= false;
		private boolean considerEnemyPosition	= false;
		
		/**
		 * Creates a builder for GameStateEvaluator.
		 */
		public Builder() {
		}
		
		public Builder withMarbleConqueredWeight(int weight) {
			this.marblesConqueredWeight = weight;
			return this;
		}
		
		public Builder withDistanceFromCenterWeight(int weight) {
			this.distanceFromCenterWeight = weight;
			return this;
		}
		
		public Builder withCoherenceWeight(int weight) {
			this.coherenceWeight = weight;
			return this;
		}
		
		public Builder withFormationBreakWeight(int weight) {
			this.formationBreakWeight = weight;
			return this;
		}
		
		public Builder withSingleMarbleCapWeight(int weight) {
			this.singleMarbleCapWeight = weight;
			return this;
		}
		
		public Builder withDoubleMarbleCapWeight(int weight) {
			this.doubleMarbleCapWeight = weight;
			return this;
		}
		
		public Builder withImmediateMarbleCapWeight(int weight) {
			this.immediateMarbleCapWeight = weight;
			return this;
		}
		
		public Builder enableHashing() {
			this.hashing = true;
			return this;
		}
		
		/**
		 * Enables the metrics for opponent marbles instead of just for your own marbles. 
		 * Heavily decreases the number of nodes that can be pruned. 
		 * @return this, for argument chaining.
		 */
		public Builder considerEnemyPosition() {
			this.considerEnemyPosition = true;
			return this;
		}
		
		/**
		 * Builds the GameStateEvaluator with the provided arguments.
		 * Untouched variables are initialized with 0 or false.
		 * @return a GameStateEvaluator spawned from the given arguments.
		 */
		public GameStateEvaluator build() {
			GameStateEvaluator evaluator = new GameStateEvaluator();
			evaluator.abaPro = this.abaPro;
			evaluator.marblesConqueredWeight = this.marblesConqueredWeight;
			evaluator.distanceFromCenterWeight = this.distanceFromCenterWeight;
			evaluator.coherenceWeight = this.coherenceWeight;
			evaluator.formationBreakWeight = this.formationBreakWeight;
			evaluator.singleMarbleCapWeight = this.singleMarbleCapWeight;
			evaluator.doubleMarbleCapWeight = this.doubleMarbleCapWeight;
			evaluator.immediateMarbleCapWeight = this.immediateMarbleCapWeight;
			evaluator.hashing = this.hashing;
			evaluator.considerEnemyPosition = this.considerEnemyPosition;
			evaluator.setRatingBounds();
			return evaluator;
		}
	}
}
