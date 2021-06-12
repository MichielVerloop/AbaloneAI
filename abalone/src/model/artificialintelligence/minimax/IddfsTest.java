package model.artificialintelligence.minimax;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;

class IddfsTest {
	//
	GameState gameState;
	GameStateEvaluator evaluator;
    Player player1;
    Player player2;
    
    
	@BeforeEach
	void setUp() throws Exception {
		player1 = Player.newPlayer("x", "random");
		player2 = Player.newPlayer("y", "random");
        gameState = new GameState(Arrays.asList(player1, player2));
        evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(100)
        		.withDistanceFromCenterWeight(4)
        		.build();
	}

	@Test
	void testTimeBoundIddfsAbortsTimelyAndProvidesMoves() {
		int time = 1;
		Minimax.Builder iddfsMinimaxBuilder = new Minimax.Builder()
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE);
		long startTime = System.nanoTime();
		gameState.gameStats.initializeTurn();
		PlayableMove move = iddfsMinimaxBuilder
				.withTimeBoundIddfs(time)
				.build(gameState, evaluator)
				.getBestMove();
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / 1000000;
		assertNotNull(move);
		assertTrue(duration > time * 1000);
		assertTrue(duration < time * 1000 + 500, 
				"getBestMove() should be supplied in the given "
				+ "time + at maximum 500 milliseconds delay but the extra delay was: "
				+ (duration - time * 1000));
		
		time = 2;
		startTime = System.nanoTime();
		move = iddfsMinimaxBuilder
				.withTimeBoundIddfs(time)
				.build(gameState, evaluator)
				.getBestMove();
		endTime = System.nanoTime();
		duration = (endTime - startTime) / 1000000;
		assertNotNull(move);
		assertTrue(duration >= time * 1000 - 5,
				"getBestMove() should be supplied after computing at least (loosely) " + time * 1000
				+ " milliseconds but was finished in " + duration + " milliseconds.");
		assertTrue(duration < time * 1000 + 100, 
				"getBestMove() should be supplied in the given "
				+ "time + at maximum 100 milliseconds delay but the extra delay was: "
				+ (duration - time * 1000));
	}

	@Test
	void testTimeBoundIddfsThrowsOnIllegalTime() {
		int time = 0;
		final Minimax zeroSecIddfs = new Minimax.Builder()
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE)
				.withTimeBoundIddfs(time)
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		assertThrows(AssertionError.class, () -> zeroSecIddfs.getBestMove());
		
		time = -1;
		final Minimax minusOneSecIddfs = new Minimax.Builder()
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE)
				.withTimeBoundIddfs(time)
				.build(gameState, evaluator);
		assertThrows(AssertionError.class, () -> minusOneSecIddfs.getBestMove());
		
		time = Integer.MIN_VALUE;
		final Minimax minusInfinitySecMinimax = new Minimax.Builder()
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE)
				.withTimeBoundIddfs(time)
				.build(gameState, evaluator);
		assertThrows(AssertionError.class, () -> minusInfinitySecMinimax.getBestMove());
	}
	
	@Test
	void testDepthBoundIddfsReachesSameConclusionAsDfs() {
		Minimax.Builder builder = new Minimax.Builder()
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE);
		// Depth 1
		Minimax iddfs = builder.withDepthBoundIddfs(1).build(gameState, evaluator);
		Minimax dfs = builder.withDfs(1).build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		assertEquals(dfs.getBestMove(), iddfs.getBestMove());
		
		// Depth 2
		iddfs = builder.withDepthBoundIddfs(2).build(gameState, evaluator);
		dfs = builder.withDfs(2).build(gameState, evaluator);
		assertEquals(dfs.getBestMove(), iddfs.getBestMove());
		
		// Depth 3
		iddfs = builder.withDepthBoundIddfs(3).build(gameState, evaluator);
		dfs = builder.withDfs(3).build(gameState, evaluator);
		assertEquals(dfs.getBestMove(), iddfs.getBestMove());
		
		// Depth 4
		iddfs = builder.withDepthBoundIddfs(4).build(gameState, evaluator);
		dfs = builder.withDfs(4).build(gameState, evaluator);
		assertEquals(dfs.getBestMove(), iddfs.getBestMove());
	}
	
	@Test
	void testDepthBoundIddfsDoesNotCutBranchesBeforeDepth4() {
		Minimax.Builder builder = new Minimax.Builder()
				.enableHashing();
		// Depth 1
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(100)
        		.withCoherenceWeight(4)
        		.withDistanceFromCenterWeight(8)
        		.build();
		Minimax iddfs = builder.withDepthBoundIddfs(1).build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		iddfs.getBestMove();
		assertEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		gameState.gameStats.currentTurnCommittedDepth.exactCuts = 0;
		
		// Depth 2
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(100)
        		.withCoherenceWeight(4)
        		.withDistanceFromCenterWeight(8)
        		.build();
		iddfs = builder.withDepthBoundIddfs(2).build(gameState, evaluator);
		iddfs.getBestMove();
		assertEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		gameState.gameStats.currentTurnCommittedDepth.exactCuts = 0;
		
		// Depth 3
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(100)
        		.withCoherenceWeight(4)
        		.withDistanceFromCenterWeight(8)
        		.build();
		iddfs = builder.withDepthBoundIddfs(3).build(gameState, evaluator);
		iddfs.getBestMove();
		assertEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		gameState.gameStats.currentTurnCommittedDepth.exactCuts = 0;
	}
	
	@Test
	void testDepthBoundIddfsCutsBranchesAndNarrowsWindowsAtDepth4() {
		// Depth 4
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(100)
        		.withCoherenceWeight(4)
        		.withDistanceFromCenterWeight(8)
        		.build();
		Minimax iddfs = new Minimax.Builder()
				.withDepthBoundIddfs(4)
				.enableHashing()
				.enableWindowNarrowing()
				.enableEvaluateSorting(1, 1)
				.enableHistoryHeuristicSorting(2, 3)
				.enableMarbleOrdering(4, 4)
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		iddfs.getBestMove();
		assertNotEquals(0, gameState.gameStats.currentTurnCommittedDepth.exactCuts);
		assertNotEquals(0, gameState.gameStats.currentTurnCommittedDepth.windowCuts);
		gameState.gameStats.commitTurn();
		
		// nrOfWindowsNarrowed is quite small and not reliable to test without going to higher depths.
		//assertNotEquals(0, ((DefaultMinimax)iddfs).nrOfWindowsNarrowed);
	}
}
