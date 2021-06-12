package model.gamelogic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.artificialintelligence.minimax.GameStateEvaluator;
import model.hex.Direction;
import model.hex.FractionalHex;
import model.hex.Hex;

class TeamTest {
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
	void testThereIsNoOverlapInDistanceFromCenterAndMarblesConquered() {
		// aka if your marble is knocked off the board, that doesn't increase nor
		// decrease the distanceFromCenter metric.
		prepareUnsafeOneTurnKill();
		int initialDistanceFromCenter = player2.getTeam().computeDistanceFromCenter();
		gameState.makeMove(Move.newMove(board, 
				new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(2, -4, 2),
						new Hex(2, -4, 2))), 
				Direction.UPPER_RIGHT, 
				player1));
		int resultingDistanceFromCenter = player2.getTeam().computeDistanceFromCenter();
		assertEquals(0, resultingDistanceFromCenter - initialDistanceFromCenter);
	}

	@Test
	void testImmediateMarbleCapturingDanger() {
		// Case white is not threatened, but white threatens black 2 to 1.
		prepareUnsafeOneTurnKill();
		assertEquals(0, player1.getTeam().computeImmediateMarbleCapturingDanger(board));
		assertEquals(1, player2.getTeam().computeImmediateMarbleCapturingDanger(board));
		
		// Set up next case
		gameState.makeMove(Move.newMove(board, 
				new HashSet<>(Arrays.asList(new Hex(2, -4, 2))), 
				Direction.UPPER_RIGHT, 
				player1));
		
		// Case white does not threaten, but black threatens threatens white 3 to 1
		assertEquals(1, player1.getTeam().computeImmediateMarbleCapturingDanger(board));
		assertEquals(0, player2.getTeam().computeImmediateMarbleCapturingDanger(board));
		
		// Set up next case
		board.getField(new Hex(-1, 4, -3)).setMarble(
				board.getField(new Hex(-4, 1, 3)).getMarble());
		board.getField(new Hex(-4, 1, 3)).setMarble(null);
		board.getField(new Hex(0, 3, -3)).setMarble(
				board.getField(new Hex(-4, 0, 4)).getMarble());
		board.getField(new Hex(-4, 0, 4)).setMarble(null);
		
		// Case white does not threaten, but black threatens white 3 to 2 and 3 to 1
		assertEquals(2, player1.getTeam().computeImmediateMarbleCapturingDanger(board));
		assertEquals(0, player2.getTeam().computeImmediateMarbleCapturingDanger(board));
		
		// Set up final case
		board.getField(new Hex(-1, 3, -2)).setMarble(
				board.getField(new Hex(0, 2, -2)).getMarble());
		board.getField(new Hex(-1, 2, -1)).setMarble(
				board.getField(new Hex(1, 1, -2)).getMarble());
		board.getField(new Hex(1, 1, -2)).getMarble();
		
		// Case white does not threaten, but black threatens a single marble twice and
		// another marble once.
		assertEquals(2, player1.getTeam().computeImmediateMarbleCapturingDanger(board));
		assertEquals(0, player2.getTeam().computeImmediateMarbleCapturingDanger(board));
	}
	
	@Test
	void testSingleMarbleCapturingDanger() {
		// Case 1 of each player's marbles threaten each other.
		prepareUnsafeOneTurnKill();
		assertEquals(1, player1.getTeam().computeSingleMarbleCapturingDanger(board));
		assertEquals(1, player2.getTeam().computeSingleMarbleCapturingDanger(board));
		
		// Case player 1 has a marble threatening player 2's marble and
		// a marble threatening 2 marbles of player 2.
		Move undo = gameState.makeMove(Move.newMove(board, 
				new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(0, -2, 2),
						new Hex(2, -2, 0))), 
				Direction.UPPER_RIGHT, 
				player1));
		assertEquals(1, player1.getTeam().computeSingleMarbleCapturingDanger(board));
		assertEquals(3, player2.getTeam().computeSingleMarbleCapturingDanger(board));
		
		// Case player 1 threatens two marbles with 1 marble.
		gameState.makeMove(undo);
		gameState.makeMove(Move.newMove(board, 
				new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(3, -4, 1),
						new Hex(3, -4, 1))), 
				Direction.UPPER_LEFT, 
				player1));
		assertEquals(0, player1.getTeam().computeSingleMarbleCapturingDanger(board));
		assertEquals(2, player2.getTeam().computeSingleMarbleCapturingDanger(board));
	}
	
	@Test
	void testDoubleMarbleCapturingDanger() {
		// Case 1 of each player's marbles threaten each other and have a neighboring ally.
		prepareUnsafeOneTurnKill();
		assertEquals(1, player1.getTeam().computeDoubleMarbleCapturingDanger(board));
		assertEquals(1, player2.getTeam().computeDoubleMarbleCapturingDanger(board));
		
		// Case player 1 has a marble threatening player 2's marble
		// and a marble threatening 2 marbles of player 2
		// and all of the threatened marbles have allied neighbors.
		Move undo = gameState.makeMove(Move.newMove(board, 
				new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(0, -2, 2),
						new Hex(2, -2, 0))), 
				Direction.UPPER_RIGHT, 
				player1));
		assertEquals(1, player1.getTeam().computeDoubleMarbleCapturingDanger(board));
		assertEquals(3, player2.getTeam().computeDoubleMarbleCapturingDanger(board));
		
		// Case player 1 threatens two marbles with 1 marble.
		gameState.makeMove(undo);
		Move.newMove(board, 
				new HashSet<>(FractionalHex.hexLinedraw(
								new Hex(3, -4, 1),
								new Hex(3, -4, 1))), 
						Direction.UPPER_LEFT, 
						player1).makeMove();
		assertEquals(0, player1.getTeam().computeDoubleMarbleCapturingDanger(board));
		assertEquals(2, player2.getTeam().computeDoubleMarbleCapturingDanger(board));
		
		// Case player 1 threatens 1 marble with allied neighbors and 1 marble without allied neighbors.
		Move.newMove(board, 
				new HashSet<>(FractionalHex.hexLinedraw(
						new Hex(1, -3, 2),
						new Hex(1, -3, 2))), 
				Direction.UPPER_RIGHT, 
				player1).makeMove();
		assertEquals(0, player1.getTeam().computeDoubleMarbleCapturingDanger(board));
		assertEquals(1, player2.getTeam().computeDoubleMarbleCapturingDanger(board));	
	}
	
	void prepareUnsafeOneTurnKill() {
		prepareTwoTurnKill();
		gameState.makeMove(Move.newMove(board,
				new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(1, -4, 3), 
        				new Hex(1, -4, 3) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(2, 0, -2), 
        				new Hex(2, 0, -2))), 
        		Direction.UPPER_RIGHT,
        		player2));
	}
	
	void prepareTwoTurnKill() {
		gameState.makeMove(Move.newMove(board,
				new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(-2, -2, 4), 
        				new Hex(-2, -2, 4) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(4, 0, -4), 
        				new Hex(4, 0, -4))), 
        		Direction.LOWER_RIGHT, 
        		player2));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(-1, -2, 3), 
        				new Hex(-1, -2, 3) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(4, -1, -3), 
        				new Hex(4, -1, -3))), 
        		Direction.LOWER_RIGHT, 
        		player2));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(-1, -3, 4), 
        				new Hex(-1, -3, 4) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(3, 1, -4), 
        				new Hex(3, 0, -3))), 
        		Direction.RIGHT,
        		player2));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(0, -3, 3), 
        				new Hex(0, -3, 3) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(4, -1, -3), 
        				new Hex(4, -1, -3))), 
        		Direction.LOWER_RIGHT,
        		player2));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(0, -4, 4), 
        				new Hex(0, -4, 4) 
        				)),
        		Direction.UPPER_RIGHT,
        		player1));
        gameState.makeMove(Move.newMove(board,
        		new HashSet<>(FractionalHex.hexLinedraw(
        				new Hex(4, 0, -4), 
        				new Hex(4, 0, -4))), 
        		Direction.LOWER_RIGHT,
        		player2));   
	}
}
