package model.gamelogic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.artificialintelligence.minimax.GameStateEvaluator;
import model.hex.Direction;
import model.hex.FractionalHex;
import model.hex.Hex;

class BoardTest {
	//
	
	GameState gameState;
    Board board;
    Player player1;
    Player player2;
    
	@BeforeEach
	void setUp() throws Exception {
        player1 = new HumanPlayer("Arnold Baksteen");
        player2 = new HumanPlayer("Intelligent Twig");
        gameState = new GameState(Arrays.asList(player1, player2));
        board = gameState.getBoard();		
	}

	@Test
	void testBoardHashIsConsistent() {
		// Hash is initially correct
		long initialHashVerification = GameStateEvaluator.hashOfBoard(board);
		long initialHash = board.getBoardHash();
		assertEquals(initialHashVerification, initialHash);
		
		// Hash remains correct after making a move.
		MoveUndo undo = gameState.makeMove(Move.newMove(
				board, 
				new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(-4, 0, 4),
						new Hex(-2, 0, 2))),
				Direction.UPPER_RIGHT, 
				player1));
		long afterMoveHashVerification = GameStateEvaluator.hashOfBoard(board);
		long afterMoveHash = board.getBoardHash();
		assertEquals(afterMoveHashVerification, afterMoveHash);
		
		// Hash remains correct after undoing that move.
		gameState.makeMove(undo);
		long afterUndoHash = board.getBoardHash();
		assertEquals(initialHashVerification, afterUndoHash);
	}

	@Test
    void testAbalToCube() {
		// Test wrong string format cases
		assertThrows(AssertionError.class, () -> Board.abalToCube(""));
		assertThrows(AssertionError.class, () -> Board.abalToCube("a"));
		assertThrows(AssertionError.class, () -> Board.abalToCube("ad"));
		assertThrows(AssertionError.class, () -> Board.abalToCube("a9b"));
		
		// Test wrong coordinates cases
		assertThrows(AssertionError.class, () -> Board.abalToCube("a0"));
		assertThrows(AssertionError.class, () -> Board.abalToCube("a10"));
		assertThrows(AssertionError.class, () -> Board.abalToCube("z1"));
		assertThrows(AssertionError.class, () -> Board.abalToCube("j1"));

		// Test correct cases
		assertEquals(new Hex(0, 0, 0), Board.abalToCube("e5"));
		assertEquals(new Hex(0, 0, 0), Board.abalToCube("E5"));
    	assertEquals(new Hex(-4, 0, 4), Board.abalToCube("a1"));
    	assertEquals(new Hex(0, 4, -4), Board.abalToCube("i5"));
    	assertEquals(new Hex(1, 1, -2), Board.abalToCube("g6"));	
    }
	
	@Test
	void testCubeToAbal() {
		assertEquals("e5", Board.cubeToAbal(new Hex(0, 0, 0)));
		
		Set<Hex> legalPositions = Hex.build(Board.BOARD_RADIUS); 
		for (Hex hex : legalPositions) {
			assertEquals(hex, Board.abalToCube(Board.cubeToAbal(hex)));
		}
		
		Set<Hex> illegalPositions = Hex.build(Board.BOARD_RADIUS + 1);
		illegalPositions.removeAll(legalPositions);
		
		for (Hex hex : illegalPositions) {
			assertThrows(AssertionError.class, () -> Board.abalToCube(Board.cubeToAbal(hex)));
		}
	}
}
