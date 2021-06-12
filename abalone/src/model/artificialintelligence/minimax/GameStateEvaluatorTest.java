package model.artificialintelligence.minimax;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import model.gamelogic.Board;
import model.gamelogic.GameState;
import model.gamelogic.Move;
import model.gamelogic.MoveUndo;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;
import model.hex.Direction;
import model.hex.FractionalHex;
import model.hex.Hex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



class GameStateEvaluatorTest {
	//
	
	GameState gameState;
	GameStateEvaluator evaluator;
	Board board;
    Player player1;
    Player player2;
	
	@BeforeEach
	void setUp() throws Exception {
		player1 = Player.newPlayer("Arnold", "random");
		player2 = Player.newPlayer("Baksteen", "random");
        gameState = new GameState(Arrays.asList(player1, player2));
        evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(1)
        		.build();
        board = gameState.getBoard();
	}
	
	@Test
	void testHashOfBoardIsEqualToHashOfBoardWithMoves() {
		// Tests whether the hash of a board, xor'd with the hash of a move results in the same hash 
		// that would follow from applying the move to the board and then hashing the board.
		
		PlayableMove move = Move.newMove(board, new HashSet<>(FractionalHex.hexLinedraw(
					new Hex(-2, -2, 4),
					new Hex(0, -2, 2))),
				Direction.UPPER_RIGHT, 
				player1);
		
		long combinedBoardHash = GameStateEvaluator.hashOfBoard(board, move);
		
		gameState.makeMove(move);
		long normalBoardHash = GameStateEvaluator.hashOfBoard(board);
		assertEquals(normalBoardHash, combinedBoardHash);
	}
	
	@Test
	void testBoardHashWithHashOfMoveIsEqualToHashOfBoardWhenTheMoveIsApplied() {
		// Tests whether the board hash, xor'd with the hash of a move results in the same hash
		// that would follow from applying the move to the board and then hashing the board.
		
		long initialBoardHash = GameStateEvaluator.hashOfBoard(board);
		PlayableMove move = Move.newMove(board, new HashSet<>(FractionalHex.hexLinedraw(
					new Hex(-2, -2, 4),
					new Hex(0, -2, 2))), 
				Direction.UPPER_RIGHT, 
				player1);
		
		long combinedBoardHash = GameStateEvaluator.hashOfBoard(initialBoardHash, move);
		
		gameState.makeMove(move);
		long normalBoardHash = GameStateEvaluator.hashOfBoard(board);
		assertEquals(normalBoardHash, combinedBoardHash);
	}
	
	@Test
	void testBoardHashIsTheSameAfterApplyingAndUndoingAMove() {
		// Tests whether the initial board hash is equal to the board hash we get from 
		// applying and then undoing a move.
		
		long initialBoardHash = board.getBoardHash();
		Move move = Move.newMove(board, new HashSet<>(FractionalHex.hexLinedraw(
					new Hex(-2, -2, 4),
					new Hex(0, -2, 2))), 
				Direction.UPPER_RIGHT, 
				player1);

		// Make and undo the move.
		gameState.makeMove(gameState.makeMove(move));
		long afterBoardHash = board.getBoardHash();
		assertEquals(initialBoardHash, afterBoardHash);
	}

	@Test
	void testMoveHashOfAMoveAndItsUndoAreEqual() {
		// Tests whether the hash of a move and the hash of the corresponding undo move are equal.
		
		// Sumito
		PlayableMove sumito = Move.newMove(board, new HashSet<>(FractionalHex.hexLinedraw(
					new Hex(-2, 0, 2),
					new Hex(0, -2, 2))), 
				Direction.RIGHT, 
				player1);
		final long hashOfSumitoMove = GameStateEvaluator.hashOfMove(sumito);
		MoveUndo sumitoUndo = gameState.makeMove(sumito);
		long hashOfSumitoUndo = GameStateEvaluator.hashOfMove(sumitoUndo);
		assertEquals(hashOfSumitoMove, hashOfSumitoUndo);
		
		// cleanup
		gameState.makeMove(sumitoUndo);
		// Sidestep
		PlayableMove sidestep = Move.newMove(board, new HashSet<>(FractionalHex.hexLinedraw(
					new Hex(-2, 0, 2),
					new Hex(0, -2, 2))), 
				Direction.UPPER_RIGHT, 
				player1);
		long hashOfSidestepMove = GameStateEvaluator.hashOfMove(sidestep);
		long hashOfSidestepUndo = GameStateEvaluator.hashOfMove(gameState.makeMove(sidestep));
		assertEquals(hashOfSidestepMove, hashOfSidestepUndo);
	}
	
	@Test
	void testHashOfMarbleIsInversible() {
		
		final long hash1 = GameStateEvaluator.hashOfMarble(board.getField(new Hex(-2, 0, 2)).getMarble());
		final long hash2 = GameStateEvaluator.hashOfMarble(board.getField(new Hex(-1, -1, 2)).getMarble());
		final long hash3 = GameStateEvaluator.hashOfMarble(board.getField(new Hex(0, -2, 2)).getMarble());
		assertThrows(NullPointerException.class, () -> 
			GameStateEvaluator.hashOfMarble(board.getField(new Hex(1, -3, 2)).getMarble()));
		
		PlayableMove sumito = Move.newMove(board, new HashSet<>(FractionalHex.hexLinedraw(
					new Hex(-2, 0, 2),
					new Hex(0, -2, 2))), 
				Direction.RIGHT, 
				player1);
		final long moveHash = GameStateEvaluator.hashOfMove(sumito);
		gameState.makeMove(sumito);
		
		assertThrows(NullPointerException.class, () -> 
			GameStateEvaluator.hashOfMarble(board.getField(new Hex(-2, 0, 2)).getMarble()));
		long hash2afterMove = GameStateEvaluator.hashOfMarble(board.getField(new Hex(-1, -1, 2)).getMarble());
		long hash3afterMove = GameStateEvaluator.hashOfMarble(board.getField(new Hex(0, -2, 2)).getMarble());
		long hash4 = GameStateEvaluator.hashOfMarble(board.getField(new Hex(1, -3, 2)).getMarble());
		
		assertEquals(hash2, hash2afterMove);
		assertEquals(hash3, hash3afterMove);
		
		assertEquals(moveHash, hash1 ^ hash2 ^ hash3 ^ hash2afterMove ^ hash3afterMove ^ hash4);
	}
	
	@Test
	void testRatingIsMaxValueOnWin() {
		GameState spyState = spy(gameState);
		when(spyState.isFinished()).thenReturn(true);
		when(spyState.getWinner()).thenReturn(player1.getTeam());
		GameStateEvaluator evaluator = new GameStateEvaluator.Builder().build();
		assertEquals(Integer.MAX_VALUE, evaluator.rateGameState(spyState, player1.getTeam()));
	}
	
	@Test
	void testRatingIsMinValueOnLoss() {
		GameState spyState = spy(gameState);
		when(spyState.isFinished()).thenReturn(true);
		when(spyState.getWinner()).thenReturn(player2.getTeam());
		GameStateEvaluator evaluator = new GameStateEvaluator.Builder().build();
		assertEquals(Integer.MIN_VALUE, evaluator.rateGameState(spyState, player1.getTeam()));
	}
	
	@Test
	void testRatingIsNormalOnGameNotFinished() {
		GameStateEvaluator evaluator = new GameStateEvaluator.Builder()
				.withDistanceFromCenterWeight(1)
				.build();
		// Default distance from middle is -46. Normalized this becomes -8188.
		assertEquals(-8188, evaluator.rateGameState(gameState, player1.getTeam()));
	}
	
	@Test
	void testRatingIsNormalized() { 
		// None of the metrics apply to the default gameState, so we should see a rating of 0.
		GameStateEvaluator evaluator = new GameStateEvaluator.Builder()
				.withMarbleConqueredWeight(1)
				.withFormationBreakWeight(1)
				.withSingleMarbleCapWeight(1)
				.withDoubleMarbleCapWeight(1)
				.build();
		assertEquals(0, evaluator.rateGameState(gameState, player1.getTeam()));
		
		// For the other weights, regardless of the values, we should see that
		// the starting gameState evaluates to roughly -8150 or 7700.
		// Why? This is just what you get after normalizing the values.
		evaluator = new GameStateEvaluator.Builder()
				.withDistanceFromCenterWeight(1)
				.build();
		assertEquals(-8188, evaluator.rateGameState(gameState, player1.getTeam()));
		evaluator = new GameStateEvaluator.Builder()
				.withDistanceFromCenterWeight(88)
				.build();
		assertEquals(-8096, evaluator.rateGameState(gameState, player1.getTeam()));
		
		evaluator = new GameStateEvaluator.Builder()
				.withCoherenceWeight(1)
				.build();
		assertEquals(8364, evaluator.rateGameState(gameState, player1.getTeam()));
		evaluator = new GameStateEvaluator.Builder()
				.withCoherenceWeight(88)
				.build();
		assertEquals(7216, evaluator.rateGameState(gameState, player1.getTeam()));
	}
	
	@Test
	void testRateGameStateConqueredMarbles() {
		GameStateEvaluator evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(1)
        		.build(); 
		// Baseline 0 kills
		assertEquals(0, evaluator.rateGameState(gameState, player1.getTeam()));
		
		// After a kill, score is 1. Normalized this is 1666.
		player1.getTeam().getConqueredMarbles().add(null);
		assertEquals(1666, evaluator.rateGameState(gameState, player1.getTeam()));
	}
	
	@Test
	void testRateGameStateHashesOnceHashingIsEnabled() {
		// Starts at 0 hashes.
		assertEquals(0, evaluator.gameScoreHashes);
		
		// On enabling hashing, we get hashes.
		evaluator.enableHashing();
		evaluator.rateGameState(gameState, player1.getTeam());
		assertEquals(1, evaluator.gameScoreHashes);
	}
	
	@Test
	void testRateGameStateDoesNotHashWhenHashingIsDisabled() {
		// Starts at 0 hashes.
		assertEquals(0, evaluator.gameScoreHashes);
		
		// Hashing still disabled, evaluating it gives 0 hashes again.
		evaluator.rateGameState(gameState, player1.getTeam());
		assertEquals(0, evaluator.gameScoreHashes);
	}
	
	@Test
	void testConsiderEnemyPosition() throws Exception {
		// If the enemy position is not considered, then in the first gameState, 
		// the score should improve after two moves.
		GameStateEvaluator.Builder evalBuilder = new GameStateEvaluator.Builder()
				.withDistanceFromCenterWeight(8)
				.withFormationBreakWeight(20)
				.withMarbleConqueredWeight(100);
		GameStateEvaluator p1Evaluator = evalBuilder.build();
		GameStateEvaluator p2Evaluator = evalBuilder.build();
		Minimax.Builder minBuilder = new Minimax.Builder()
				.withDfs(2)
				.enableHashing()
				.enableEvaluateSorting(1, 1)
				.enableMarbleOrdering(2, 2);
		
		int initialRating = p1Evaluator.rateGameState(gameState, player1.getTeam());
		gameState.gameStats.initializeTurn();
		gameState.makeMove(minBuilder.build(gameState, p1Evaluator).getBestMove()); // p1 moves
		gameState.gameStats.commitTurn();
		gameState.gameStats.initializeTurn();
		gameState.makeMove(minBuilder.build(gameState, p2Evaluator).getBestMove()); // p2 moves
		gameState.gameStats.commitTurn();
		int finalRating = p1Evaluator.rateGameState(gameState, player1.getTeam());
		assertTrue(initialRating < finalRating,
				"Expected the initial rating, " + initialRating + " to be smaller than "
				+ "the final rating, " + finalRating + " but this did not hold true.");
		
		// If the enemy position is considered, then in the first gameState, 
		// the score should be equal after two moves due to both of the
		// the teams gaining the same advantage.
		setUp();
		evalBuilder = evalBuilder.considerEnemyPosition();
		p1Evaluator = evalBuilder.build();
		p2Evaluator = evalBuilder.build();
		
		initialRating = p1Evaluator.rateGameState(gameState, player1.getTeam());
		gameState.gameStats.initializeTurn();
		gameState.makeMove(minBuilder.build(gameState, p1Evaluator).getBestMove()); // p1 moves
		gameState.gameStats.commitTurn();
		gameState.gameStats.initializeTurn();
		gameState.makeMove(minBuilder.build(gameState, p2Evaluator).getBestMove()); // p2 moves
		gameState.gameStats.commitTurn();
		finalRating = p1Evaluator.rateGameState(gameState, player1.getTeam());
		assertEquals(initialRating, finalRating);
		
		// If the enemy position is considered , then the score of as viewed
		// by a certain team is always minus the score of the other team.
		setUp();
		evalBuilder = evalBuilder.considerEnemyPosition();
		p1Evaluator = evalBuilder.build();
		p2Evaluator = evalBuilder.build();
		gameState.gameStats.initializeTurn();
		gameState.makeMove(minBuilder.build(gameState, p1Evaluator).getBestMove()); // p1 moves
		gameState.gameStats.commitTurn();
		p1Evaluator = evalBuilder.build();
		//System.out.println(gameState);
		assertEquals(p1Evaluator.rateGameState(gameState, player1.getTeam()),
				-p2Evaluator.rateGameState(gameState, player2.getTeam()));
	}
	
	@Test
	void testRevertingToPreviousGameStateDoesNotCreateMoreHashes() {
		evaluator.enableHashing();
		// Baseline 1 hash
		evaluator.rateGameState(gameState, player1.getTeam());
		assertEquals(1, evaluator.gameScoreHashes);
		
		// Making a move adds a new hash
		MoveUndo undo = gameState.makeMove(Move.newMove(board,
				new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(-2, 0, 2), 
						new Hex(0, -2, 2))), 
				Direction.UPPER_LEFT, 
				player1));
		evaluator.rateGameState(gameState, player1.getTeam());
		assertEquals(2, evaluator.gameScoreHashes);
		
		// Undoing the move keeps the number of hashed gamestates constant.
		gameState.makeMove(undo);
		evaluator.rateGameState(gameState, player1.getTeam());
		assertEquals(2, evaluator.gameScoreHashes);
	}
	
	@Test
	void testMarblePositionOracleStartsWith244Hashes() {
		// Hash of initial gamestate gives 244 marble combinations (61 positions * 4 colors).
		assertEquals(244, GameStateEvaluator.marblePositionOracle.size());
	}
	
	@Test
	void testEmptyMapGivesSizeOfZero() {
		Map<Integer, Map<Integer, Integer>> nestedMap = new HashMap<>();
		nestedMap.put(0, new HashMap<>());
	}
	
	@Test
	void testOneNestedEntryGivesSizeOfOne() {
		Map<Integer, Map<Integer, Integer>> nestedMap = new HashMap<>();
		Map<Integer, Integer> map = new HashMap<>();
		map.put(0, 0);
		nestedMap.put(0, map);
		assertEquals(1, mapSize(nestedMap));
	}
	
	@Test
	void testThreeNestedEntryGiveSizeOfThree() {
		Map<Integer, Map<Integer, Integer>> nestedMap = new HashMap<>();
		Map<Integer, Integer> map = new HashMap<>();
		map.put(0, 0);
		map.put(1, 1);
		nestedMap.put(0, map);
		Map<Integer, Integer> map2 = new HashMap<>();
		map2.put(2, 2);
		nestedMap.put(1, map2);
		assertEquals(3, mapSize(nestedMap));
	}
	
	<K, U, V> int mapSize(Map<K, Map<U, V>> nestedMap) {
		int size = 0;
		for (Map<U, V> map : nestedMap.values()) {
			size += map.size();
		}
		return size;
	}
}
