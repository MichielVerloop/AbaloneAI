package model.artificialintelligence.minimax;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.artificialintelligence.minimax.TranspositionTable.TranspositionValue.Flag;
import model.gamelogic.GameState;
import model.gamelogic.Move;
import model.gamelogic.MoveSidestep;
import model.gamelogic.MoveSumito;
import model.gamelogic.MoveUndo;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;
import model.gamelogic.Team;

class MoveOrderingTest {
	//
	GameState gameState;
	GameStateEvaluator evaluator;
    Player player1;
    Player player2;
	
	@BeforeEach
	void setUp() throws Exception {
		player1 = Player.newPlayer("Arnold", "random");
		player2 = Player.newPlayer("Baksteen", "random");
        gameState = new GameState(Arrays.asList(player1, player2));
        evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(100)
        		.withDistanceFromCenterWeight(1)
        		.build();
	}

	@Test
	void testEvaluateProperlySorts() {
		Minimax minimax = new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableEvaluateSorting(1, 3)
				.build(gameState, evaluator);
		List<PlayableMove> sortedMoves = minimax.getAllLegalMoves(1);
		
		PlayableMove oldMove = null;
		for (PlayableMove newMove : sortedMoves) {
			if (oldMove != null) {
				assertTrue(evaluator.rateMove(gameState, gameState.getCurrentTeam(), oldMove) 
						>= evaluator.rateMove(gameState, gameState.getCurrentTeam(), newMove),
						"Expected the value of oldMove, " 
						+ evaluator.rateMove(gameState, gameState.getCurrentTeam(), oldMove) 
						+ " to be greater than or equal to the value of newMove, "
						+ evaluator.rateMove(gameState, gameState.getCurrentTeam(), newMove)
						+ ".");
			}
			oldMove = newMove;
		}
		assertNotEquals(Move.allLegalMoves(gameState), sortedMoves);
	}
	
	@Test
	void testEvaluateSortIsConsistent() {
		Team optimizingTeam = gameState.getCurrentPlayer().getTeam();
		Minimax minimax = new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableEvaluateSorting(1, 3)
				.build(gameState, evaluator);
		gameState.gameStats.initializeTurn();
		PlayableMove move = minimax.getBestMove();
		
		int score = evaluator.rateMove(gameState, optimizingTeam, move);
		gameState.makeMove(move);
		gameState.gameStats.commitTurn();
		assertEquals(score, evaluator.rateGameState(gameState, optimizingTeam));
	}

	@Test
	void testHistoryProperlySorts() {
		DefaultMinimax minimax = ((DefaultMinimax)new Minimax.Builder()
				.enableHashing()
				.withDfs(2)
				.enableHashing()
				.enableHistoryHeuristicSorting(1, 2)
				.build(gameState, evaluator));
		List<PlayableMove> unSortedMoves = minimax.getAllLegalMoves(1);
		// Naturally populate the history table with the prunings from the first 3 depths.
		gameState.gameStats.initializeTurn();
		gameState.makeMove(gameState.makeMove(minimax.getBestMove()));
		gameState.gameStats.commitTurn();
		
		List<PlayableMove> sortedMoves = minimax.getAllLegalMoves(1);
		PlayableMove oldMove = null;
		for (PlayableMove newMove : sortedMoves) {
			if (oldMove != null) {
				assertTrue(minimax.historyTable.get(oldMove) 
						>= minimax.historyTable.get(newMove),
						"Expected the value of oldMove, " 
						+ minimax.historyTable.get(oldMove) 
						+ " to be greater than or equal to the value of newMove, "
						+ minimax.historyTable.get(newMove)
						+ ".");
			}
			oldMove = newMove;
		}
		assertNotEquals(unSortedMoves, sortedMoves);
	}
	
	@Test
	void testHistorySortIsConsistent() {
		Team optimizingTeam = gameState.getCurrentPlayer().getTeam();
		Minimax minimax = new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableHistoryHeuristicSorting(1, 3)
				.build(gameState, evaluator);
		// Naturally populate the history table with the prunings from the first 3 depths.
		gameState.gameStats.initializeTurn();
		gameState.makeMove(gameState.makeMove(minimax.getBestMove()));
		
		PlayableMove move = minimax.getBestMove();
		
		int score = evaluator.rateMove(gameState, optimizingTeam, move);
		gameState.makeMove(move);
		gameState.gameStats.commitTurn();
		
		assertEquals(score, evaluator.rateGameState(gameState, optimizingTeam));
	}
	
	@Test
	void testHistoryHeuristicSortingIsApplied() {
		DefaultMinimax minimax = ((DefaultMinimax)new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableHistoryHeuristicSorting(1, 2)
				.build(gameState, evaluator));

		// Construct two arbitrary moves.
		Iterator<PlayableMove> moves = Move.allLegalMoves(gameState).iterator();
		PlayableMove firstMove = moves.next();
		PlayableMove secondMove = moves.next();
		
		// Manually increment the kills caused by these moves at the given depth.
		MoveHistoryTable table = minimax.historyTable;
		table.incrementKills(firstMove, 3);
		table.incrementKills(secondMove, 5);
		
		// This should result in the second move being the first in the sorted list and the first move
		// to be the second entry in the list.
		List<PlayableMove> resultMoves = minimax.getAllLegalMoves(1);
		assertEquals(secondMove, resultMoves.get(0));
		assertEquals(firstMove, resultMoves.get(1));
	}
	
	@Test
	void testMarbleSortsProperly() {
		Minimax marbleOrdering = ((DefaultMinimax)new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableMarbleOrdering(1, 3)
				.build(gameState, evaluator));
		List<PlayableMove> moves = marbleOrdering.getAllLegalMoves(1);
		PlayableMove oldMove = null;
		for (PlayableMove move : moves) {
			if (oldMove != null) {
				assertTrue(oldMove.getNrOfInvolvedMarbles() >= move.getNrOfInvolvedMarbles());
				if (oldMove.getNrOfInvolvedMarbles() == move.getNrOfInvolvedMarbles()) {
					if (!((oldMove instanceof MoveSumito && move instanceof MoveSumito)
						|| (oldMove instanceof MoveSumito && move instanceof MoveSidestep)
						|| (oldMove instanceof MoveSidestep && move instanceof MoveSidestep))) {
						fail("oldMove is a sidestep move but was sorted before move "
								+ "which is a sumito move.");
					}
				}
			}
			oldMove = move;
		}
	}
	
	@Test
	void testNotEnablingMarbleOrderingDoesNotResultInProperlySortedMarbles() {
		List<PlayableMove> moves = ((DefaultMinimax)new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.build(gameState, evaluator))
				.getAllLegalMoves(1);
		PlayableMove oldMove = null;
		boolean sorted = true;
		for (PlayableMove move : moves) {
			if (oldMove != null && oldMove.compareTo(move) < 0) {
				sorted = false;
			}
			oldMove = move;
		}
		assertFalse(sorted);
	}
	
	@Test
	void testIterationSortsProperyly() {
		Team optimizingTeam = gameState.getCurrentTeam();
		// If we populate the evaluator's hashing table with moves, then iterationSort
		// combined with evaluation sort should generate the same result as just evaluate sort.
		Minimax evaluateSorting = ((DefaultMinimax)new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableEvaluateSorting(1, 1)
				.build(gameState, evaluator));
		gameState.gameStats.initializeTurn();
		evaluateSorting.getBestMove();
		
		Minimax iterationSorting = ((DefaultMinimax)new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableEvaluateSorting(1, 1)
				.enableIterationSorting(1, 1)
				.build(gameState, evaluator));
		assertEquals(evaluateSorting.getAllLegalMoves(1), iterationSorting.getAllLegalMoves(1));
		
		// If we manually populate the hashing table with moves, then iterationSort will
		// reflect these changes.
		evaluator = new GameStateEvaluator.Builder()
        		.withMarbleConqueredWeight(100)
        		.withDistanceFromCenterWeight(1)
        		.build();
		List<PlayableMove> fakedMoves = new ArrayList<>(Move.allLegalMoves(gameState));
		fakedMoves = fakedMoves.subList(6, 12);
		for (PlayableMove move : fakedMoves) {
			MoveUndo undo = gameState.makeMove(move);
			evaluator.createTranspositionTableEntry(gameState, optimizingTeam, 3, 
					ThreadLocalRandom.current().nextInt(), Flag.EXACT);
			gameState.makeMove(undo);
		}
		
		iterationSorting = ((DefaultMinimax)new Minimax.Builder()
				.enableHashing()
				.withDfs(1)
				.enableHashing()
				.enableIterationSorting(1, 1)
				.build(gameState, evaluator));
		List<PlayableMove> iterationSorted = iterationSorting.getAllLegalMoves(1); 
		assertTrue(evaluator.getStoredMoveRating(gameState, optimizingTeam, iterationSorted.get(0))
				>= evaluator.getStoredMoveRating(gameState, optimizingTeam, iterationSorted.get(1)));
		assertTrue(evaluator.getStoredMoveRating(gameState, optimizingTeam, iterationSorted.get(1))
				>= evaluator.getStoredMoveRating(gameState, optimizingTeam, iterationSorted
						.get(iterationSorted.size() - 1)));
		
		// For all remaining moves, check whether they are worse than the best one-third and better
		// than the worst one-sixth.
		fakedMoves.remove(iterationSorted.get(0));
		fakedMoves.remove(iterationSorted.get(1));
		fakedMoves.remove(iterationSorted.get(iterationSorted.size() - 1));
		for (PlayableMove move : fakedMoves) {
			assertTrue(evaluator.getStoredMoveRating(gameState, optimizingTeam, iterationSorted.get(1))
					>= evaluator.getStoredMoveRating(gameState, optimizingTeam, move));
			assertTrue(evaluator.getStoredMoveRating(gameState, optimizingTeam, move)
					>= evaluator.getStoredMoveRating(gameState, optimizingTeam, iterationSorted
							.get(iterationSorted.size() - 1)));
		}
	}
}
