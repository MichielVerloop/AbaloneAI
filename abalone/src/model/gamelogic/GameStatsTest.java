package model.gamelogic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.artificialintelligence.minimax.GameStateEvaluator;
import model.artificialintelligence.minimax.Minimax;
import model.gamelogic.GameStats.Average;
import model.gamelogic.GameStats.TurnStats;

class GameStatsTest {

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
        		.considerEnemyPosition()
        		.withMarbleConqueredWeight(100)
        		.withDistanceFromCenterWeight(4)
        		.withCoherenceWeight(1)
        		.build();
	}

	@Test
	void testNotInitializingTheTurnLeadsToAssertionError() {
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(1)
				.enableHashing()
				.build(gameState, evaluator);
		assertThrows(NullPointerException.class, () -> dfsMinimax.getBestMove());
	}
	
	@Test
	void testBranchingFactorSumEqualsNodesVisited() {
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(3)
				.enableHashing()
				.build(gameState, evaluator);
		
		gameState.gameStats.initializeTurn();
		dfsMinimax.getBestMove();
		assertEquals(gameState.gameStats.currentTurnCommittedDepth.nodesVisited,
				gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(1).sum + 
				gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(2).sum +
				gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(3).sum);
	}
	
	@Test
	void testBranchingFactorIsNotZero() {
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(1)
				.enableHashing()
				.build(gameState, evaluator);
		
		gameState.gameStats.initializeTurn();
		dfsMinimax.getBestMove();
		assertTrue(0 < gameState.gameStats.currentTurnCommittedDepth.nodesVisited);
	}
	
	@Test
	void testSubsequentBranchesHaveMoreVisitedNodes() {
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(4)
				.enableHashing()
				.build(gameState, evaluator);
		
		gameState.gameStats.initializeTurn();
		dfsMinimax.getBestMove();
		assertTrue(gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(1).sum
				< gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(2).sum);
		assertTrue(gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(2).sum
				< gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(3).sum);
		assertTrue(gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(3).sum
				< gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(4).sum);
	}
	
	@Test
	void testIddfsRegistersTheFinalDepthAndTranspositionTableSize() {
		Minimax dfsMinimax = new Minimax.Builder()
				.withDepthBoundIddfs(2)
				.enableHashing()
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		dfsMinimax.getBestMove();
		assertEquals(2, gameState.gameStats.currentTurnCommittedDepth.depth);
		assertTrue(gameState.gameStats.currentTurnCommittedDepth.transpositionTableSize > 45); // 45 is the amount of nodes of depth 0 + 1.
	}
	
	@Test
	void testLeafNodesIsCorrect() throws Exception {
		Minimax dfsMinimax = new Minimax.Builder()
				.withDfs(1)
				.enableHashing()
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		dfsMinimax.getBestMove();
		assertEquals(44, gameState.gameStats.currentTurnCommittedDepth.leafNodes);
		
		setUp();
		dfsMinimax = new Minimax.Builder()
				.withDfs(2)
				.enableHashing()
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		dfsMinimax.getBestMove();
		assertEquals(gameState.gameStats.currentTurnCommittedDepth.nodesVisited,
				gameState.gameStats.currentTurnCommittedDepth.branchingFactorByDepth.get(1).sum + gameState.gameStats.currentTurnCommittedDepth.leafNodes);
		
	}
	
	@Test
	void testToCsvString() {
		TurnStats ts = new TurnStats(gameState, new HumanPlayer("x"));
		ts.actualScore = gameState.getScore();
		ts.timeSpent = 2304;
		ts.nodesVisited = 34050;
		ts.leafNodes = 27931;
		ts.ratedScore = 0;
		ts.depth = 4;
		ts.exactCuts = 206;
		ts.windowCuts = 1723;
		ts.windowsNarrowed = 127;
		ts.transpositionTableSize = 26043;
		ts.branchingFactorByDepth = new HashMap<>();
		Average avg = new Average();
		avg.sum = 44;
		avg.count = 1;
		ts.branchingFactorByDepth.put(1, avg);
		avg = new Average();
		avg.sum = 195;
		avg.count = 44;
		ts.branchingFactorByDepth.put(2, avg);
		avg = new Average();
		avg.sum = 5880;
		avg.count = 195;
		ts.branchingFactorByDepth.put(3, avg);
		avg = new Average();
		avg.sum = 27931;
		avg.count = 3951;
		ts.branchingFactorByDepth.put(4, avg);
		String[] expected = {"x","0","0","2304","34050","27931","0","4","206","1723","127","26043","44","4","30","7"};
		assertTrue(Arrays.equals(expected, ts.toStringArray()));
	}
	
	@Test
	void testMergeTurnStats() {
		Player human = new HumanPlayer("x");
		TurnStats confirmed = new TurnStats(gameState, human);
		confirmed.actualScore = gameState.getScore();
		confirmed.timeSpent = 2304;
		confirmed.nodesVisited = 34050;
		confirmed.leafNodes = 27931;
		confirmed.ratedScore = 10;
		confirmed.depth = 4;
		confirmed.exactCuts = 206;
		confirmed.windowCuts = 1723;
		confirmed.windowsNarrowed = 127;
		confirmed.transpositionTableSize = 26043;
		confirmed.branchingFactorByDepth = new HashMap<>();
		Average avg = new Average();
		avg.sum = 44;
		avg.count = 1;
		confirmed.branchingFactorByDepth.put(1, avg);
		
		TurnStats newer = new TurnStats(gameState, human);
		newer.actualScore = gameState.getScore();
		newer.actualScore.score.put(gameState.getCurrentTeam(), 1);
		newer.timeSpent = 23043;
		newer.nodesVisited = 340550;
		newer.leafNodes = 273931;
		newer.ratedScore = 100;
		newer.depth = 5;
		newer.exactCuts = 2306;
		newer.windowCuts = 165723;
		newer.windowsNarrowed = 1237;
		newer.transpositionTableSize = 260413;
		newer.branchingFactorByDepth = new HashMap<>();
		avg = new Average();
		avg.sum = 44;
		avg.count = 1;
		newer.branchingFactorByDepth.put(1, avg);
		avg = new Average();
		avg.sum = 195;
		avg.count = 44;
		newer.branchingFactorByDepth.put(2, avg);
		
		confirmed.merge(newer);
		String[] expected = {"x", "0", "0", "25347", "374600", "301862", "100", "5", "2512", "167446", "1364", "260413", "44", "4"};
		assertTrue(Arrays.equals(expected, confirmed.toStringArray()));
	}
}
