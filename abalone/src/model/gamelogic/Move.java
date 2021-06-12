package model.gamelogic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import model.artificialintelligence.minimax.GameStateEvaluator;
import model.exceptions.IllegalMoveException;
import model.hex.Direction;
import model.hex.FractionalHex;
import model.hex.Hex;

/**
 * Interface which contains everything that is required to make a move.
 * @author Michiel Verloop
 */
public interface Move {
	//
	/**
     * Factory method to determine which subclass should be constructed.
     * @param board The Board that the Move will use for internal calculations.
     * @param marbleCoords A list of <code>Hex</code> that are involved in the move.
     * @param direction The <code>Direction</code> in which the marble(s) will move.
     * @requires None of the arguments are null, none of the elements of marbleCoords are null, 
     *     1 <= coordinates.size() <= 5
     * @ensures A new move that can be legal for some board state is constructed.
     *     All constructor arguments for the subMoves are not null.
     * @return A new PlayableMove based on the arguments.
     */
	public static PlayableMove newMove(Board board, 
    								   Set<Hex> marbleCoords, 
    								   Direction direction, 
    								   Player initiator)
            throws IllegalMoveException {
        // Verify whether the number of coordinates is legal.
        if (!(1 <= marbleCoords.size() && marbleCoords.size() <= 5)) {
            throw new IllegalMoveException("No move can be made from these arguments."
                    + " 1 to 5 coordinates should be given");
        }

        // Checks whether the move is a sumito move. If size == 1, it is.
        if (marbleCoords.size() == 1) {
            return new MoveSumito(board, marbleCoords.iterator().next(), direction, initiator);
        }

        /* Checks whether all coordinates are all on one line.
         * Aka check if there exists some coordinate in marbleCoords and
         * some Direction for which all of the first marbleCoords.size() - 1
         * neighboring coordinates in that direction are contained in
         * marbleCoordinates.
         */
        if (!Hex.isLine(marbleCoords, true)) {
        	System.out.println(marbleCoords);
        	throw new IllegalMoveException("Not all coordinates are on one line."
                    + " No move can be made from these arguments.");
        }

        // Checks whether the move is a sumito move. if the direction is inline, it's a sumito move.
        for (Hex hex : marbleCoords) {
            if (marbleCoords.contains(hex.neighbour(direction))) {
                // Determine origin by going in the opposite direction
                // until the last marble, which is the origin in the original
                // direction, is found.
                for (Hex hex2: marbleCoords) {
                    if (!marbleCoords.contains(hex2.neighbour(direction.invert()))) {
                        return new MoveSumito(board, hex2, direction, initiator);
                    }
                }

            }
        }
        // It's not a sumito move so check it it is a valid sidestep move.
        if (!(marbleCoords.size() <= 3)) {
            throw new IllegalMoveException("Cannot construct a move from these arguments. "
                    + "The direction indicates this is a sidestep move, "
                    + "but the number of coordinates indicate this is a sumito move.");
        }
        return new MoveSidestep(board, marbleCoords, direction, initiator);
    }

	/**
	 * Converts a move from the tournament/Aba-Pro notation to one that can be used internally.
	 * @param board The board that the Move will use for internal calculations.
	 * @param moveNotation A String of the form (?i)([a-i][1-9]){2,3}
	 * @param initiator The initiator of the move.
	 * @return A new PlayableMove based on the arguments.
	 * @throws IllegalArgumentException if the string is not of the correct form.
	 * @throws IllegalMoveException if the string is of the correct form but no move can be made
	 *     from it.
	 */
	public static PlayableMove newMove(Board board, String moveNotation, Player initiator) {
		moveNotation = moveNotation.toLowerCase();
		if (!moveNotation.matches("([a-i][1-9]){2,3}")) {
			throw new IllegalArgumentException("The given moveNotation, " + moveNotation
					+ " should match (?i)([a-i][1-9]){2,3} but does not.");
		}
		Hex end1 = Board.abalToCube(moveNotation.substring(0, 2));
		Hex end2 = Board.abalToCube(moveNotation.substring(2, 4));
		// Case sumito moves:
		if (moveNotation.matches("([a-i][1-9]){2}")) {
			
			return newMove(board,
					new HashSet<>(Arrays.asList(end1)),
					Direction.fromHex(end2.subtract(end1)),
					initiator);
		}
		// else sidestep:
		Hex dir = Board.abalToCube(moveNotation.substring(4, 6));
		Set<Hex> from = new HashSet<>(FractionalHex.hexLinedraw(end1, end2));
		Direction direction = dir.subtract(end1).length() == 1
				? Direction.fromHex(dir.subtract(end1))
				: Direction.fromHex(dir.subtract(end2));
		return newMove(board, from, direction, initiator);
	}
	
    /**
     * Returns all legal moves for the current player given a gameState.
     * @requires initiator != null, gameState != null, initiator is a member of a team which
     *     is part of gameState.
     * @ensures result != null
     * @return all legal moves for that player in the current board state.
     */
    public static Set<PlayableMove> allLegalMoves(GameState gameState) {
    	final Player initiator = gameState.getCurrentPlayer();
        Set<PlayableMove> moves = new HashSet<>();

        // Generate all possible sumito and sidestep moves:
        // First, generate all possible combinations of 1, 2 or 3 marbles:
        Set<Set<Hex>> combinations = new HashSet<>();
        for (Marble marble : initiator.getMarbles()) {
            if (!marble.isCaptured()) {
                for (Direction dir : Direction.directions()) {
                    Hex hex = marble.getHex();
                    combinations.add(new HashSet<Hex>(Arrays.asList(hex)));
                    Set<Hex> pair2 = new HashSet<>();
                    Set<Hex> pair3 = new HashSet<>();
                    Field neighbor = gameState.getBoard().getFieldOffset(hex, dir, 1);
                    if (neighbor != null
                            && !neighbor.isEmpty()
                            && neighbor.getMarble().getTeam() 
                            == initiator.getTeam()) {
                        pair2.add(hex);
                        pair2.add(hex.neighbour(dir));
                        combinations.add(pair2);
                        // if the neighbour of that neighbour is not empty and allied
                        neighbor = gameState.getBoard().getFieldOffset(hex, dir, 2);
                        if (neighbor != null
                            && !neighbor.isEmpty()
                            && neighbor.getMarble().getTeam()
                            == initiator.getTeam()) {
                            pair3.add(hex);
                            pair3.addAll(hex.neighbours(dir, 2));
                            combinations.add(pair3);
                        }
                    }
                }
            }
        }
        
        // Then, for all combinations and all directions, add them to
        // the set of moves if they are legal.
        for (Set<Hex> pair : combinations) {
            for (Direction dir : Direction.directions()) {
                try {
                    PlayableMove move = Move.newMove(
                    		gameState.getBoard(),
                            pair,
                            dir,
                            initiator);
                    if (!moves.contains(move) && move.isLegal()) {
                        moves.add(move);
                    }
                } catch (IllegalMoveException e) {
                    continue;
                }
            }
        }
        
        // Finally, if the move would lead to a game state that's already been in the game, remove it
        moves = moves.stream()
        	.filter(m -> !gameState.gameHistory.isRepetition(
        					GameStateEvaluator.hashOfBoard(gameState.getBoard().boardHash, m)))
        	.collect(Collectors.toSet());
        return moves;
    }


    /**
     * Makes a move on a given board as the given player.
     * @requires this.isLegal(gameState)
     * @ensures The move will be made on the board and the initiator
     *     gets credited for any marbles that are pushed off.
     */
    void makeMove();

    /**
     * Returns the initiator of this move.
     * @ensures result != null
     * @return the initiator of this move.
     */
    public Player getInitiator();

    /**
     * Returns the direction of this move.
     * @ensures result != null.
     * @return the direction of this move.
     */
    public Direction getDirection();

    /**
     * Returns the marbles involved in this move.
     * @ensures result != null, contains no null elements, contains at least one element.
     * @return the marbles involved in this move.
     */
    public Set<Marble> getMarbles();

    @Override
    public String toString();
}
