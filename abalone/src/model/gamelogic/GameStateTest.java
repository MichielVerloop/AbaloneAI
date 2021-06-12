package model.gamelogic;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import model.artificialintelligence.RandomStrategy;
import model.exceptions.GameNotOverException;
import model.exceptions.IllegalMoveException;
import model.hex.Direction;
import model.hex.FractionalHex;
import model.hex.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameStateTest {
	//
    Hex center;
    Player player1;
    Player player2;
    Player player3;
    Player player4;

    @BeforeEach
    void setUp() throws Exception {
        center = new Hex(0, 0, 0);
        player1 = new ComputerPlayer("Arnold", new RandomStrategy());
        player2 = new ComputerPlayer("Intelligent Twig", new RandomStrategy());
        player3 = new ComputerPlayer("Baksteen", new RandomStrategy());
        player4 = new ComputerPlayer("Sentient Potato", new RandomStrategy());
    }

    @Test
    void testPlayeresAreAssignedColorsInTwoPlayerGame() {
    	new GameState(Arrays.asList(player1, player2));
    	assertEquals(Player.COLORS[0], player1.getColor());
    	assertEquals(Player.COLORS[1], player2.getColor());
    }
    
    @Test
    void testPlayeresAreAssignedColorsInThreePlayerGame() {
    	new GameState(Arrays.asList(player1, player2, player3));
    	assertEquals(Player.COLORS[0], player1.getColor());
    	assertEquals(Player.COLORS[1], player2.getColor());
    	assertEquals(Player.COLORS[2], player3.getColor());
    }
    
    @Test
    void testPlayeresAreAssignedColorsInFourPlayerGame() {
    	new GameState(Arrays.asList(player1, player2, player3, player4));
    	assertEquals(Player.COLORS[0], player1.getColor());
    	assertEquals(Player.COLORS[1], player2.getColor());
    	assertEquals(Player.COLORS[2], player3.getColor());
    	assertEquals(Player.COLORS[3], player4.getColor());
    }
    
    @Test
    void testPopulate() {
        // 2 players
        GameState twoPlayerGame = new GameState(Arrays.asList(player1, player2));
        assertEquals(14, twoPlayerGame.teams.get(0).players.get(0).getMarbles().size());
        assertEquals(14, twoPlayerGame.teams.get(1).players.get(0).getMarbles().size());


        int nrOfPlacedMarbles = 0;
        for (Field field : twoPlayerGame.getBoard().getFields(Hex.build(Board.BOARD_RADIUS))) {
            if (!field.isEmpty()) {
                nrOfPlacedMarbles++;
            }
        }
        assertEquals(28, nrOfPlacedMarbles);


    }

    @Test
    void testReset() {
        GameState twoPlayerGame = new GameState(Arrays.asList(player1, player2));
        Board board = twoPlayerGame.getBoard();

        // TODO: Use the move function once it's tested in this test for cleaner code. Also make it less messy.
        assertEquals(61, board.board.size());
        assertEquals(center, board.getField(center).getHex());
        assertNull(board.getField(center).getMarble());
        assertEquals(twoPlayerGame.teams.get(0).players.get(0),
                board.getField(new Hex(-2, -1, 3)).getMarble().getOwner());

        board.getField(center).setMarble(new Marble(twoPlayerGame.teams.get(0).players.get(0), 'z'));
        assertNotNull(board.getField(center).getMarble());

        board.initialize(twoPlayerGame.teams, null);
        assertEquals(61, board.board.size());
        assertEquals(center, board.getField(center).getHex());
        assertNull(board.getField(center).getMarble());
        assertEquals(twoPlayerGame.teams.get(0).players.get(0),
                board.getField(new Hex(-2, -1, 3)).getMarble().getOwner());
    }

    @Test
    void testMakePlayableMove() {
        GameState twoPlayerGame = new GameState(Arrays.asList(player1, player2));
        Board board = twoPlayerGame.getBoard();
        // Verify whether initiators are correctly being checked.
        final Move legalMoveWrongInitiator = Move.newMove(
        	board,
        	new HashSet<>(Arrays.asList(new Hex(-3, -1, 4))),
            Direction.UPPER_RIGHT,
            player2
        );

        assertThrows(IllegalMoveException.class,
            () -> twoPlayerGame.makeMove(legalMoveWrongInitiator));

        final Move legalMoveCorrectInitiator = Move.newMove(
        	board,
        	new HashSet<>(Arrays.asList(new Hex(-3, -1, 4))),
            Direction.UPPER_RIGHT,
            player1
        );

        twoPlayerGame.makeMove(legalMoveCorrectInitiator);
        assertAll(
            () -> assertTrue(board.getField(new Hex(-3, -1, 4)).isEmpty()),
            () -> assertFalse(board.getField(new Hex(0, -1, 1)).isEmpty())
        );
    }
    
    @Test
    void testMakeUndoMove() {
        GameState gameState = new GameState(Arrays.asList(player1, player2, player3, player4));
        Board board = gameState.getBoard();
        PlayableMove move = Move.newMove(
        		board,
        		new HashSet<>(FractionalHex.hexLinedraw(
    					new Hex(-3, 3, 0),
    					new Hex(-3, 1, 2))), 
    			Direction.LOWER_RIGHT, 
    			player1);
        // Use the move's makeMove to avoid turn from incrementing and 
        // player's turns from alternating.
        move.makeMove();
    	
    	move = Move.newMove(
    			board,
    			new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(-3, 2, 1),
						new Hex(-3, 0, 3))), 
    			Direction.LOWER_RIGHT, 
    			player1);
    	MoveUndo undo = gameState.makeMove(move);
    	assertAll(
    			() -> assertEquals(1, gameState.getTurn()),
    			() -> assertEquals(player3, gameState.getCurrentPlayer()),
    			() -> assertEquals(1, gameState.teams.get(0).getConqueredMarbles().size())
    	);
    	
    	// Now when we apply the undo move, it should reset to player1 and 0 conquered marbles.
    	gameState.makeMove(undo);
    	assertAll(
    			() -> assertEquals(0, gameState.getTurn()),
    			() -> assertEquals(player1, gameState.getCurrentPlayer()),
    			() -> assertEquals(0, gameState.teams.get(0).getConqueredMarbles().size())
    	);
    }

    @Test
    void testIsFinished() {
        GameState twoPlayerBoard = new GameState(Arrays.asList(player1, player2));
        assertFalse(twoPlayerBoard.isFinished());

        // Set the conquered marbles size to 6 to fake player1 being the winner.
        player1.getTeam().getConqueredMarbles().addAll(
                Arrays.asList(null, null, null, null, null, null));
        assertTrue(twoPlayerBoard.isFinished());

        player1.getTeam().getConqueredMarbles().remove(0);
        assertFalse(twoPlayerBoard.isFinished());
        twoPlayerBoard.turn = GameState.TURN_LIMIT;
        assertTrue(twoPlayerBoard.isFinished());
    }

    @Test
    void testGetWinner() {
        GameState twoPlayerBoard = new GameState(Arrays.asList(player1, player2));

        assertThrows(GameNotOverException.class,
            () -> twoPlayerBoard.getWinner());

        // Draw case
        twoPlayerBoard.turn = GameState.TURN_LIMIT;
        assertNull(twoPlayerBoard.getWinner());

        // Winner case
        player1.getTeam().getConqueredMarbles().addAll(
                Arrays.asList(null, null, null, null, null, null));
        assertEquals(player1.getTeam(),
                twoPlayerBoard.getWinner());
    }

    @Test
    void testNextPlayer() {
        // two player board
        GameState twoPlayerBoard = new GameState(Arrays.asList(player1, player2));
        assertEquals(player1, twoPlayerBoard.getCurrentPlayer());

        twoPlayerBoard.makeMove(player1.determineMove(twoPlayerBoard));
        assertEquals(player2, twoPlayerBoard.getCurrentPlayer());

        twoPlayerBoard.makeMove(player2.determineMove(twoPlayerBoard));
        assertEquals(player1, twoPlayerBoard.getCurrentPlayer());

        // four player board
        GameState fourPlayerBoard = new GameState(Arrays.asList(player1, player2, player3, player4));
        assertEquals(player1, fourPlayerBoard.getCurrentPlayer());

        fourPlayerBoard.nextPlayer();
        assertEquals(player3, fourPlayerBoard.getCurrentPlayer());

        fourPlayerBoard.nextPlayer();
        assertEquals(player2, fourPlayerBoard.getCurrentPlayer());

        fourPlayerBoard.nextPlayer();
        assertEquals(player4, fourPlayerBoard.getCurrentPlayer());

        fourPlayerBoard.nextPlayer();
        assertEquals(player1, fourPlayerBoard.getCurrentPlayer());
    }

    @Test
    void testGameHistoryIsProperlyPopulated() {
    	List<Long> boardHashes = new ArrayList<>();
    	List<String> moves = new ArrayList<>();
    	GameState gameState = new GameState(Arrays.asList(player1, player2));
    	for (int i = 0; i < 10; i++) {
    		PlayableMove move = gameState.getCurrentPlayer().determineMove(gameState);
    		gameState.makeMove(move);
    		boardHashes.add(gameState.getBoard().getBoardHash());
    		moves.add(move.getMoveNotation());
    	}
    	
    	assertEquals(boardHashes, gameState.gameHistory.boardHashes);
    	assertEquals(moves, gameState.gameHistory.moves);
    }
    
    @Test
    void testGameHistoryIsProperlyUndone() {
    	List<MoveUndo> undoMoves = new ArrayList<>();
    	GameState gameState = new GameState(Arrays.asList(player1, player2));
    	// Populate the gameHistory
    	for (int i = 0; i < 10; i++) {
    		PlayableMove move = gameState.getCurrentPlayer().determineMove(gameState);
    		undoMoves.add(gameState.makeMove(move));
    	}
    	for (int i = 9; i >= 0; i--) {
    		gameState.makeMove(undoMoves.get(i));
    	}
    	assertEquals(0, gameState.gameHistory.boardHashes.size());
    	assertEquals(0, gameState.gameHistory.moves.size());
    }
    
    void testToString() {
        GameState twoPlayerBoard = new GameState(Arrays.asList(player1, player2));
        GameState threePlayerBoard = new GameState(Arrays.asList(player1, player2, player3));
        GameState fourPlayerBoard = new GameState(Arrays.asList(player1, player2, player3, player4));
        // TODO: Better test. the current one relies on visual inspection.
        System.out.println(twoPlayerBoard.toString());
        System.out.println();
        System.out.println(threePlayerBoard.toString());
        System.out.println();
        System.out.println(fourPlayerBoard.toString());
    }
}
