package model.artificialintelligence.minimax;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Ordering;

import model.gamelogic.Board;
import model.gamelogic.GameState;
import model.gamelogic.Move;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;
import model.hex.Direction;
import model.hex.FractionalHex;
import model.hex.Hex;

class MinimaxTest {
	//
	GameState gameState;
	GameStateEvaluator evaluator;
	Board board;
    Player player1;
    Player player2;
    
    
	@BeforeEach
	void setUp() throws Exception {
		player1 = Player.newPlayer("x", "random");
		player2 = Player.newPlayer("y", "random");
        gameState = new GameState(Arrays.asList(player1, player2));
        board = gameState.getBoard();
	}
	
	@Test
	void testMinimaxShouldNotGeneratePerfectCutoffsBefore4PlyWithDfs() {
		GameStateEvaluator.Builder evalBuilder = new GameStateEvaluator.Builder()
        		.withDistanceFromCenterWeight(3)
        		.withCoherenceWeight(1);
		
		// Test 1 ply
		evaluator = evalBuilder.build();
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(1)
				.enableHashing()
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		gameState.makeMove(dfsMinimax.getBestMove());
		assertEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		gameState.gameStats.currentTurnCommittedDepth.exactCuts = 0;
		
		// Test 2 ply
		evaluator = evalBuilder.build();
		dfsMinimax = new Minimax.Builder()
				.withDfs(2)
				.enableHashing()
				.build(gameState, evaluator);
		gameState.makeMove(dfsMinimax.getBestMove());
		assertEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		gameState.gameStats.currentTurnCommittedDepth.exactCuts = 0;
		
		// Test 3 ply
		evaluator = evalBuilder.build();
		dfsMinimax = new Minimax.Builder()
				.withDfs(3)
				.enableHashing()
				.build(gameState, evaluator);
		gameState.makeMove(dfsMinimax.getBestMove());
		assertEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		gameState.gameStats.currentTurnCommittedDepth.exactCuts = 0;
	}
	
	@Test
	void testMinimaxShouldGeneratePerfectCutoffsAt4PlyWithDfs() {
		evaluator = new GameStateEvaluator.Builder()
        		.withDistanceFromCenterWeight(3)
        		.withCoherenceWeight(1)
        		.build();
		
		// Test 4 ply
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(4)
				.enableHashing()
				.enableEvaluateSorting(1, 1)
				.enableHistoryHeuristicSorting(2, 3)
				.enableMarbleOrdering(4, 4)
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		gameState.makeMove(dfsMinimax.getBestMove());
		assertNotEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		gameState.gameStats.commitTurn();
	}
	
	@Test
	void testMinimaxDoesProperOpeningMove() {
		evaluator = new GameStateEvaluator.Builder()
        		.withDistanceFromCenterWeight(3)
        		.build();
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(1)
				.enableHashing()
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		gameState.makeMove(dfsMinimax.getBestMove());
		assertTrue(!board.getField(new Hex(-1, 0, 1)).isEmpty()
				|| !board.getField(new Hex(0, -1, 1)).isEmpty());
	}
	
	@Test
	void testMinimaxFindsOneTurnKillMoveWithDepthOfOne() {
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(10)
        		.withDistanceFromCenterWeight(1)
        		.build();
		Minimax dfsMinimax = new Minimax.Builder()
			.withDfs(1)
			.enableHashing()
			.enableMarbleOrdering(1, Integer.MAX_VALUE)
			.build(gameState, evaluator);
		
		// Set up an OTK
		prepareUnsafeOneTurnKill();
		gameState.gameStats.initializeTurn();
		gameState.makeMove(dfsMinimax.getBestMove());
		assertEquals(1, gameState.getTotalNrOfConqueredMarbles());
	}
	
	@Test
	void testMinimaxDoesNotTakeUnsafeKillWithDepthOfTwo() {
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(10)
        		.withDistanceFromCenterWeight(1)
        		.build();
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(2)
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE)
				.build(gameState, evaluator);

		// Set up an unsafe OTK
		prepareUnsafeOneTurnKill();
		gameState.gameStats.initializeTurn();
		gameState.makeMove(dfsMinimax.getBestMove());
		assertEquals(0, gameState.getTotalNrOfConqueredMarbles());
	}
	
	@Test
	void testMinimaxFindsTwoTurnSafeKillMoveWithDepthOfThree() {
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(10)
        		.withDistanceFromCenterWeight(1)
        		.build();
		Minimax dfsMinimax =  new Minimax.Builder()
				.withDfs(3)
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE)
				.build(gameState, evaluator);
		
		// Set up an unsafe OTK which can be a 2-turn safe kill
		prepareUnsafeOneTurnKill();
		gameState.gameStats.initializeTurn();
		gameState.makeMove(dfsMinimax.getBestMove());
		assertEquals(0, gameState.getTotalNrOfConqueredMarbles());
		
		assertFalse(board.getField(new Hex(3, -3, 0)).isEmpty());
		
	}
	
	@Test
	void testCombinedMoveOrderingIsAppliedAndWorksAsExpectedAtDepth4() throws Exception { 
		// Depth 2 and 3 are not tested because set randomness is stronger than the heuristics at those depths. 
		
		// Expected is that no sorting methods (= baseline perform the worst)
		// The marble ordering heuristic is a little better
		// The history sorting heuristic is better still, but not always and only works at depth 3+
		// The evaluate sorting heuristic is the best, but not in the opening position. 
		// Expected is that combined move ordering should be faster than evaluate sorting at depth 4.
		GameStateEvaluator.Builder evalBuilder = new GameStateEvaluator.Builder()
				.withCoherenceWeight(4)
				.withDistanceFromCenterWeight(8)
				.withFormationBreakWeight(20)
				.withMarbleConqueredWeight(100);
		prepareTwoTurnKill();
		// Establish the time spent getting the best move when applying evaluate sorting
		// at depth 4.
		GameStateEvaluator eval = evalBuilder.build();
		Minimax evaluateSorting = new Minimax.Builder()
				.withDfs(4)
				.enableHashing()
				.enableEvaluateSorting(1, 5)
				.build(gameState, eval);
		
		gameState.gameStats.initializeTurn();
		long start = System.nanoTime();
		evaluateSorting.getBestMove();
		final long evalDuration = (System.nanoTime() - start) / 1000;
		
		// Establish the time spent getting the best move when applying all heuristics
		// at depth 4.
		eval = evalBuilder.build();
		Minimax combinedMoveOrdering = new Minimax.Builder()
				.withDfs(4)
				.enableHashing()
				.enableEvaluateSorting(1, 2)
				.enableHistoryHeuristicSorting(3, 3)
				.enableMarbleOrdering(4, 4)
				.build(gameState, eval);
		start = System.nanoTime();
		combinedMoveOrdering.getBestMove();
		long cmoDuration = (System.nanoTime() - start) / 1000;
		
		// Combined move ordering should be faster than evaluate sorting at depth 4.
		assertTrue(cmoDuration < evalDuration);
	}
	
	@Test
	void testMoveOrdering() {
		List<PlayableMove> moves = DefaultMinimax.marbleSort(
				Move.allLegalMoves(gameState).stream().collect(Collectors.toList())); 
        
        assertTrue(moves.stream().allMatch(e -> e.isLegal()));
        assertEquals(44, moves.size());
        
        assertTrue(isMarbleOrdered(moves));
	}
	
	void collide() {
		// This test shows that in a 2-player game you will not have 
		// colliding hashes before you run out of memory.
		// It is not practical to run this, so there is no @Test.
		long time = System.nanoTime();
		boolean collided = false;
		Set<Long> boardHashes = new HashSet<>();
		
		// Obtain list of legal marble hashes.
		List<Long> hashes = GameStateEvaluator.marblePositionOracle.values()
				.stream().collect(Collectors.toList());
		hashes = hashes.subList(0, 122);
		while (!collided) {
			// Create a random board hash
			Collections.shuffle(hashes);
			long hash = 0;
			
			for (int i = 0; i < 28; i++) {
				hash ^= hashes.get(i);
			}
			if (boardHashes.contains(hash)) {
				System.out.println(hash);
				//System.out.println(boardHashes);
				System.out.println("Time taken: " + ((System.nanoTime() - time) / 1000000) + "s");
				System.out.println("Hashes generated so far: " + boardHashes.size());
				return;
			}
			if (boardHashes.size() % 100000 == 0) {
				System.out.println(boardHashes.size());
			}
			boardHashes.add(hash);
		}
	}
	
	// Helper functions
	
	void prepareUnsafeOneTurnKill() {
		prepareTwoTurnKill();
		gameState.gameStats.initializeTurn();
		gameState.makeMove(Move.newMove(board,
				new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(1, -4, 3), 
        				new Hex(1, -4, 3) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
		gameState.gameStats.commitTurn();
		gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(2, 0, -2), 
        				new Hex(2, 0, -2))), 
        		Direction.UPPER_RIGHT,
        		player2));
        gameState.gameStats.commitTurn();
	}
	
	void prepareTwoTurnKill() {
		gameState.gameStats.initializeTurn();
		gameState.makeMove(Move.newMove(board,
				new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(-2, -2, 4), 
        				new Hex(-2, -2, 4) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
		gameState.gameStats.commitTurn();
		gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(4, 0, -4), 
        				new Hex(4, 0, -4))), 
        		Direction.LOWER_RIGHT, 
        		player2));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(-1, -2, 3), 
        				new Hex(-1, -2, 3) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(Arrays.asList(new Hex(4, -1, -3))), 
        		Direction.LOWER_RIGHT, 
        		player2));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(Arrays.asList(new Hex(-1, -3, 4))),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(3, 1, -4), 
        				new Hex(3, 0, -3))), 
        		Direction.RIGHT,
        		player2));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(Arrays.asList(new Hex(0, -3, 3))),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(Arrays.asList(new Hex(4, -1, -3))), 
        		Direction.LOWER_RIGHT,
        		player2));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(Arrays.asList(new Hex(0, -4, 4))),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.gameStats.commitTurn();
        gameState.gameStats.initializeTurn();
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(Arrays.asList(new Hex(4, 0, -4))), 
        		Direction.LOWER_RIGHT,
        		player2));
        gameState.gameStats.commitTurn();
	}
	
    boolean isMarbleOrdered(List<PlayableMove> moves) {
    	// Convert the moves to integers such that we can use built-in functions to determine whether it is sorted. 
    	List<Integer> sameOrderList = moves.stream().map(e -> DefaultMinimax.moveGroup(e)).collect(Collectors.toList());
    	return Ordering.natural().isOrdered(sameOrderList);
    }
}
