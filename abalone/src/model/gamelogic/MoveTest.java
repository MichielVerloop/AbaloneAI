package model.gamelogic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.exceptions.IllegalMoveException;
import model.hex.Direction;
import model.hex.FractionalHex;
import model.hex.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveTest {
	//

    GameState gameState;
    Board board;
    Player player1;
    Player player2;
    Player player3;
    Player player4;
    Hex center;

    final Set<Hex> rowOfThreeMarblesFromPlayerOne = new HashSet<>(FractionalHex.hexLinedraw(
            new Hex(-4, 4, 0),
            new Hex(-2, 2, 0)));
    
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
    void testNewMove() {
    	// Default constructor
        Set<Hex> coords = new HashSet<>(FractionalHex.hexLinedraw(new Hex(-4, 4, 0), new Hex(-2, 2, 0)));

        Move sumito = Move.newMove(board, coords, Direction.RIGHT, player1);
        assertTrue(sumito instanceof MoveSumito);

        Move sidestep = Move.newMove(board, coords, Direction.UPPER_RIGHT, player1);
        assertTrue(sidestep instanceof MoveSidestep);

        assertThrows(IllegalMoveException.class,
            () -> Move.newMove(board, new HashSet<>(), Direction.LEFT, player1));
        assertThrows(IllegalMoveException.class,
            () -> Move.newMove(board, new HashSet<>(FractionalHex.hexLinedraw(
    		new Hex(-4, 4, 0),
    		new Hex(1, -1, 0))), 
    		Direction.LEFT, player1));
        
        // Abalone notation constructor
        Move sumitoFromNotation1 = Move.newMove(board, "e1e2", player1);
        assertEquals(sumito, sumitoFromNotation1);
        Move sumitoFromNotation2 = Move.newMove(board, "e2e1", player1);
        assertNotEquals(sumito, sumitoFromNotation2);
        
        Move sidestepFromNotation1 = Move.newMove(board, "e1e3f4", player1);
        Move sidestepFromNotation2 = Move.newMove(board, "e3e1f4", player1);
        assertEquals(sidestep, sidestepFromNotation1);
        assertEquals(sidestep, sidestepFromNotation2);
    }
    
    @Test
    void testGetMoveNotation() {
    	for (PlayableMove move : Move.allLegalMoves(gameState)) {
    		assertEquals(move,
    				Move.newMove(board, move.getMoveNotation(), gameState.getCurrentPlayer()));
    	}
    }

    @Test
    void testSumitoIsLegal() {
        final Hex edge = new Hex(4, -4, 0);
        board.initializeGrid();

        // Cannot move nothing
        PlayableMove move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center, center)),
                Direction.LOWER_RIGHT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("There is no marble to be moved.",
                    e.getMessage());
        }

        board.populatePlayerMarbles(player1,
                FractionalHex.hexLinedraw(center, new Hex(2, -2, 0)));
        board.populatePlayerMarbles(player3,
                FractionalHex.hexLinedraw(new Hex(3, -3, 0),  edge));

        // Can move to empty spaces
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center, center)),
                Direction.UPPER_RIGHT,
                player1);
        assertTrue(move.isLegal());

        // Cannot move what you do not own.
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center, center)),
                Direction.RIGHT,
                player3);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("You do not own this marble.",
                    e.getMessage());
        }
        
        // 3 push 2 off the edge
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center, edge)),
                Direction.RIGHT,
                player1);
        move.isLegalThrows();
        assertTrue(move.isLegal());

        // 3 push 1 off the edge
        gameState.makeMove(move);
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center.neighbour(Direction.RIGHT), edge)),
                Direction.RIGHT,
                player1);
        assertTrue(move.isLegal());

        // 3 push for suicide
        move.makeMove();
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(new Hex(2, -2, 0), edge)),
                Direction.RIGHT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("This move would lead to suicide.",
                    e.getMessage());
        }
        
        // cannot push with 4 or more
        board.populatePlayerMarbles(player1,
                FractionalHex.hexLinedraw(center, edge));
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center, edge)),
                Direction.RIGHT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("There are too many allied marbles that push.",
                    e.getMessage());
        }

        // cannot push without outnumbering
        board.populatePlayerMarbles(player3,
                FractionalHex.hexLinedraw(new Hex(2, -2, 0), edge));
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center, new Hex(1, -1, 0))),
                Direction.RIGHT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("The push is not strong enough.",
                    e.getMessage());
        }

        // cannot push an opponent's marble if your own is behind it.
        board.populatePlayerMarbles(player1,
                FractionalHex.hexLinedraw(center, edge));
        board.populatePlayerMarbles(player3,
                FractionalHex.hexLinedraw(
                        new Hex(3, -3, 0),
                        new Hex(3, -3, 0)));
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(center, center)),
                Direction.RIGHT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("An allied marble is behind the marble you are trying to push.",
                    e.getMessage());
        }
    }

    @Test
    void testSumitoMakeMove() {
        // Move into empty space works.
        Move move = Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.RIGHT,
                player1);
        gameState.makeMove(move);
        assertTrue(player1.getMarbles().contains(
                board.getField(new Hex(-1, 1, 0)).getMarble()));
        assertNull(board.getField(new Hex(-4, 4, 0)).getMarble());

        // Trying to make an illegal move fails
        move = Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.LEFT,
                player1);

        final Move finalMove = move; // required lambda expression requires effectively final.
        assertThrows(IllegalMoveException.class, () -> gameState.makeMove(finalMove));

        // ConqueredMarbles is added to when pushing off an opponent's marble
        // Set a marble up to die
        board.getField(new Hex(-4, 0, 4))
            .setMarble(board.getField(new Hex(-1, 1, 0)).getMarble());
        move = new MoveSumito(board, new Hex(-2, -2, 4), Direction.LEFT, player3);
        move.makeMove();
        assertEquals(1, player3.getTeam().getConqueredMarbles().size());
    }

    @Test
    void testSidestepIsLegal() {
        // Cannot commit suicide
        PlayableMove move = Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.LEFT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("This move would lead to suicide.",
                    e.getMessage());
        }

        // Cannot sidestep without your team owning all of the marbles
        
        board.getField(new Hex(-2, 0, 2))
            .setMarble(board.getField(new Hex(-1, -1, 2)).getMarble());
        
        move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(new Hex(-2, 2, 0), new Hex(-2, 0, 2))),
                Direction.RIGHT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("Not all marbles are owned by your team.",
                    e.getMessage());
        }

        // Cannot sidestep without the initiator owning any of the marbles
        move = Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.UPPER_RIGHT,
                player2);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("You do not own at least one of the marbles.",
                    e.getMessage());
        }
        
        // Can sidestep into an unoccupied space
        move = Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.UPPER_RIGHT,
                player1);
        assertTrue(move.isLegal());

        // Cannot sidestep into an occupied space
        move = Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.LOWER_RIGHT,
                player1);
        assertFalse(move.isLegal());
        try {
            move.isLegalThrows();
            fail("isLegalThrows didn't throw when it was expected to.");
        } catch (IllegalMoveException e) {
            assertEquals("This move is blocked by other marbles.",
                    e.getMessage());
        }
    }

    @Test
    void testSidestepMakeMove() {
        // Throws IllegalMoveException if the move is illegal.
        final Move moveFinal = Move.newMove(// final modifier for the lambda expression
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(new Hex(-2, -2, 4), new Hex(0, -4, 4))),
                Direction.LOWER_RIGHT,
                player1);
        assertThrows(IllegalMoveException.class, () -> moveFinal.makeMove());

        // Executes the move if it's legal.
        Move move = Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.UPPER_RIGHT,
                player1);
        gameState.makeMove(move);
        assertTrue(player1.getMarbles().contains(
                board.getField(new Hex(-2, 3, -1)).getMarble()));
        assertNull(board.getField(new Hex(-3, 3, 0)).getMarble());
    }

    @Test
    void testAllLegalMoves() {
    	// Tests whether 44 moves are available in a 2-player game.
    	player1 = new HumanPlayer("Arnold");
        player2 = new HumanPlayer("Intelligent Twig");
        gameState = new GameState(Arrays.asList(player1, player2));
        board = gameState.getBoard();
        
        Set<PlayableMove> moves = Move.allLegalMoves(gameState);

        assertTrue(moves.stream().allMatch(e -> e.isLegal()));
        assertEquals(44, moves.size());
    }
    
    @Test
    void testMoveSumitoCollectInvolvedCoords() {
        MoveSumito move = (MoveSumito)Move.newMove(
        		board,
                rowOfThreeMarblesFromPlayerOne,
                Direction.RIGHT,
                player1);
        // Because there is no hash code nor equality defined for moves,
        // use the "both ways subset" method to determine equality.
        assertTrue(move.collectInvolvedCoords().containsAll(rowOfThreeMarblesFromPlayerOne));
        assertTrue(rowOfThreeMarblesFromPlayerOne.containsAll(move.collectInvolvedCoords()));
    }
    
    @Test
    void testUndoingSidestepMoveWorks() {
    	List<Hex> rowAboveTheRowOfThreeMarblesFromPlayerOne = new ArrayList<>();
    	for (Hex hex : rowOfThreeMarblesFromPlayerOne) {
    		rowAboveTheRowOfThreeMarblesFromPlayerOne.add(hex.neighbour(Direction.UPPER_RIGHT));
    	}
    	
    	// Apply a sidestep move.
    	MoveUndo undo = gameState.makeMove(
    			Move.newMove(
    					board,
    					rowOfThreeMarblesFromPlayerOne,
    					Direction.UPPER_RIGHT,
    					player1));
    	// Undo the move
    	gameState.makeMove(undo);
    	// Check whether the original place is populated with marbles again.
    	assertTrue(rowOfThreeMarblesFromPlayerOne.stream().allMatch(
					hex -> !board.getField(hex).isEmpty()));
    	// Check if the place where marbles were placed is now empty.
    	assertTrue(rowOfThreeMarblesFromPlayerOne.stream().allMatch(
				hex -> !board.getField(hex).isEmpty()));
    }
    
    @Test
    void testUndoingSumitoMoveWorks() {
        PlayableMove move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(
    					new Hex(-3, 3, 0),
    					new Hex(-3, 1, 2))), 
    			Direction.LOWER_RIGHT, 
    			player1);
        // Use the move's makeMove to avoid turn from incrementing and 
        // player's turns from alternating.
        MoveUndo undo = move.getUndo();
        move.makeMove();
        undo.makeMove();
        assertAll(
        		() -> assertFalse(board.getField(new Hex(-3, 3, 0)).isEmpty()),
        		() -> assertFalse(board.getField(new Hex(-3, 2, 1)).isEmpty()),
        		() -> assertFalse(board.getField(new Hex(-3, 1, 2)).isEmpty()),
        		() -> assertTrue(board.getField(new Hex(-3, 0, 3)).isEmpty())
        );
        
        move.makeMove();
        move = Move.newMove(
        		board,
        		new HashSet<>(Arrays.asList(new Hex(-3, 2, 1))), 
    			Direction.LOWER_RIGHT, 
    			player1);
        undo = move.getUndo();
        Marble marbleThatWillBePushedFromTheBoard = board.getField(new Hex(-3, -1, 4)).getMarble();
        move.makeMove();
        assertTrue(marbleThatWillBePushedFromTheBoard.isCaptured());
        
        undo.makeMove();
        assertAll(
        		() -> assertEquals(marbleThatWillBePushedFromTheBoard,
        				board.getField(new Hex(-3, -1, 4)).getMarble()),
        		() -> assertFalse(board.getField(new Hex(-3, 2, 1)).isEmpty()),
        		() -> assertFalse(board.getField(new Hex(-3, 1, 2)).isEmpty()),
        		() -> assertFalse(board.getField(new Hex(-3, 0, 3)).isEmpty()),
        		() -> assertFalse(board.getField(new Hex(-3, -1, 4)).isEmpty())
        );
    	
    }
    
    @Test
    void testSidestepIsSmallerThanSumitoWithEqualNumberOfMarbles() {
    	// size 3
    	MoveSumito sumito = new MoveSumito(board, new Hex(0, -2, 2), Direction.LOWER_RIGHT, player1);
    	MoveSidestep sidestep = new MoveSidestep(board, 
    			new HashSet<>(FractionalHex.hexLinedraw(new Hex(0, -2, 2), new Hex(0, -4, 4))), 
    			Direction.UPPER_RIGHT, 
    			player1);
    	assertEquals(-1, sidestep.compareTo(sumito));
    	assertEquals(1, sumito.compareTo(sidestep));
    	
    	// size 2
    	sumito = new MoveSumito(board, new Hex(0, -3, 3), Direction.UPPER_LEFT, player1);
    	sidestep = new MoveSidestep(board, 
    			new HashSet<>(FractionalHex.hexLinedraw(new Hex(0, -2, 2), new Hex(0, -3, 3))), 
    			Direction.UPPER_RIGHT, 
    			player1);
    	assertEquals(-1, sidestep.compareTo(sumito));
    	assertEquals(1, sumito.compareTo(sidestep));
    }
    
    @Test
    void testSameMoveTypeAndEqualNumberOfMarblesMeansTheMovesAreEqual() {
    	MoveSumito sumito1 = new MoveSumito(board, new Hex(0, -3, 3), Direction.LOWER_RIGHT, player1);
    	MoveSumito sumito2 = new MoveSumito(board, new Hex(0, -3, 3), Direction.UPPER_LEFT, player1);
    	assertEquals(0, sumito1.compareTo(sumito2));
    	
    	MoveSidestep sidestep1 = new MoveSidestep(board,
    			new HashSet<>(FractionalHex.hexLinedraw(new Hex(0, -2, 2), new Hex(0, -4, 4))), 
    			Direction.UPPER_RIGHT, 
    			player1);
    	MoveSidestep sidestep2 = new MoveSidestep(board,
    			new HashSet<>(FractionalHex.hexLinedraw(new Hex(-2, 0, 2), new Hex(-4, 0, 4))), 
    			Direction.UPPER_LEFT, 
    			player1);
    	assertEquals(0, sidestep1.compareTo(sidestep2));
    }
    
    @Test
    void testMoveWithMoreMarblesIsGreater() {
    	MoveSumito sumito1 = new MoveSumito(board, new Hex(0, -2, 2), Direction.RIGHT, player1);
    	MoveSidestep sidestep2 = new MoveSidestep(board, 
    			new HashSet<>(FractionalHex.hexLinedraw(new Hex(0, -2, 2), new Hex(0, -3, 3))), 
    			Direction.UPPER_RIGHT, 
    			player1);
    	MoveSumito sumito3 = new MoveSumito(board, new Hex(0, -2, 2), Direction.LOWER_RIGHT, player1);
    	
    	assertEquals(-1, sumito1.compareTo(sidestep2));
    	assertEquals(1, sidestep2.compareTo(sumito1));
    	assertEquals(-1, sidestep2.compareTo(sumito3));
    }
}
