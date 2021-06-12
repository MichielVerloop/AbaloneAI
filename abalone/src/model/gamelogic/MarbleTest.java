package model.gamelogic;



import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.hex.Hex;

class MarbleTest {
	//
	
	GameState gameState;
	Board board;
    Player player1;
    Player player2;
    Player player3;
    Player player4;
    Hex center;
	
	@BeforeEach
	void setUp() throws Exception {
		center = new Hex(0, 0, 0);
        player1 = new HumanPlayer("Arnold");
        player2 = new HumanPlayer("Intelligent Twig");
        player3 = new HumanPlayer("Baksteen");
        player4 = new HumanPlayer("Sentient Potato");
        gameState = new GameState(Arrays.asList(player1, player2, player3, player4));
        board = gameState.getBoard();
	}

	@Test
	void testComputeCoherence() {
		assertEquals(4, board.getField(new Hex(-4, 4, 0)).getMarble().computeCoherence(board));
		assertEquals(5, board.getField(new Hex(-2, 2, 0)).getMarble().computeCoherence(board));
		assertEquals(6, board.getField(new Hex(-3, 2, 1)).getMarble().computeCoherence(board));
	}

	@Test
	void testComputeDistanceFromCenter() {
		assertEquals(2, board.getField(new Hex(-2, 2, 0)).getMarble().computeDistanceFromCenter());
		assertEquals(3, board.getField(new Hex(-3, 3, 0)).getMarble().computeDistanceFromCenter());
		assertEquals(4, board.getField(new Hex(-4, 4, 0)).getMarble().computeDistanceFromCenter());
		assertEquals(4, board.getField(new Hex(-4, 3, 1)).getMarble().computeDistanceFromCenter());
	}

	@Test
	void testIsFormationBreak() {
		assertFalse(board.getField(new Hex(-3, 2, 1))
				.getMarble()
				.isFormationBreak(board));
		board.getField(new Hex(-3, 2, 1)).setMarble(
				board.getField(new Hex(0, 4, -4)).getMarble());
		assertTrue(board.getField(new Hex(-3, 2, 1))
				.getMarble()
				.isFormationBreak(board));
	}

	@Test
	void testComputeNrOfSurroundingEnemyMarbles() {
		// Base case
		assertEquals(0, board.getField(new Hex(-1, -2, 3)).getMarble()
				.computeNrOfSurroundingEnemyMarbles(board));
		
		// Case surrounded
		board.getField(new Hex(-1, -2, 3)).setMarble(
				board.getField(new Hex(-4, 4, 0)).getMarble());
		assertEquals(6, board.getField(new Hex(-1, -2, 3)).getMarble()
				.computeNrOfSurroundingEnemyMarbles(board));
	}
	
	@Test
	void testIsInImmediateMarbleCapturingDanger() {
		Marble opponentMarble = board.getField(new Hex(2, -2, 0)).getMarble();
		
		// Not on the edge
		assertFalse(board.getField(new Hex(0, -3, 3)).getMarble().isInImmediateMarbleCapturingDanger(board));
		// On the edge but not threatened
		assertFalse(board.getField(new Hex(0, -4, 4)).getMarble().isInImmediateMarbleCapturingDanger(board));
		
		// 1 "threatened" by 1
		board.getField(new Hex(0, -3, 3)).setMarble(opponentMarble);
		assertFalse(board.getField(new Hex(0, -4, 4)).getMarble().isInImmediateMarbleCapturingDanger(board));
		
		// 1 threatened by 2
		board.getField(new Hex(0, -2, 2)).setMarble(opponentMarble);
		assertTrue(board.getField(new Hex(0, -4, 4)).getMarble().isInImmediateMarbleCapturingDanger(board));
		
		// 2 "threatened" by 1
		board.getField(new Hex(0, 2, -2)).setMarble(opponentMarble);
		assertFalse(board.getField(new Hex(0, 4, -4)).getMarble().isInImmediateMarbleCapturingDanger(board));
		
		// 2 "threatened" by 2
		board.getField(new Hex(0, 1, -1)).setMarble(opponentMarble);
		assertFalse(board.getField(new Hex(0, 4, -4)).getMarble().isInImmediateMarbleCapturingDanger(board));
		
		// 2 threatened by 3
		board.getField(new Hex(0, 0, 0)).setMarble(opponentMarble);
		assertTrue(board.getField(new Hex(0, 4, -4)).getMarble().isInImmediateMarbleCapturingDanger(board));
	}
}
