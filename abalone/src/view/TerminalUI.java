package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import model.exceptions.IllegalMoveException;
import model.gamelogic.Board;
import model.gamelogic.GameState;
import model.gamelogic.Move;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;
import model.gamelogic.Team;
import model.hex.Direction;
import model.hex.Hex;

public class TerminalUI extends UI {
	//
    @Override
    public void updateGameState(GameState gameState, Player current) {
        System.out.println(gameState.toString() + "\n"
                + "Turn " + (gameState.getTurn() + 1) + ": " + current.toString() + "'s turn.");
    }

    @Override
    public void showGameResult(GameState gameState, Team team) {
        if (team != null) {
            System.out.println(gameState.toString());
            System.out.println(team + " won!");
        } else {
            System.out.println("Turn limit reached! The game ends in a draw!");
        }
}

    @Override
    public PlayableMove getPlayerMove(Player player, Board board) {
        Direction dir = null;
        Set<Hex> marbleCoords = null;
        PlayableMove move = null;
        boolean legalMove = false;
        while (!legalMove) {
            marbleCoords = getMoveCoords(player);
            if (marbleCoords == null) {
                continue;
            }
            dir = getMoveDirection();
            if (dir == null) {
                continue;
            }
            try {
                move = Move.newMove(board, marbleCoords, dir, player);
                legalMove = move.isLegalThrows();
            } catch (IllegalMoveException e) {
                System.out.println(e.getMessage());
                legalMove = false;
            }
        }
        return move;
    }

    @Override
    public void showMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void showHint(GameState gameState, PlayableMove move) {
        System.out.println("HINT: the "
                + move.toString()
                + " is allowed.");
    }

    @Override
    public void showException(String message) {
        System.out.println("Exception: " + message);
    }

    /**
     * Acquires the coordinates of the given player's next move.
     * @param player The player who's turn it is.
     * @return A list of hexagons of all the involved marbles, or null if the
     *     input was incorrect.
     */
    private Set<Hex> getMoveCoords(Player player) {
        Set<Hex> marbleCoords = new HashSet<Hex>();
        System.out.println("What marbles does your move use?");

        String moveStr = readString();
        if (moveStr == null) {
            return null;
        }

        char[] marblesIDs = moveStr.replaceAll("\\s+", "").toCharArray();

        if (marblesIDs.length == 0) {
            System.out.println("Please specify more than 0 marbles.");
            return null;
        } else if (marblesIDs.length > 5) {
            System.out.println("Please specify up to 5 marbles.");
            return null;
        }

        for (char id : marblesIDs) {
            Hex coord = player.getHexagonOfMarble(id);
            if (coord == null) {
                System.out.println(id + " is not a valid"
                        + " marble identifier.");
                return null;
            }
            marbleCoords.add(coord);
        }
        return marbleCoords;
    }

    private Direction getMoveDirection() {
        Direction dir = null;
        System.out.println("What direction will your move be in?");
        String dirStr = readString().toLowerCase();
        switch (dirStr) {
        	case ("upper right"): 
            case ("ur"):
                dir = Direction.UPPER_RIGHT;
                break;
            case ("right"):
            case ("r"):
                dir = Direction.RIGHT;
                break;
            case ("lower right"):
            case ("lr"):
                dir = Direction.LOWER_RIGHT;
                break;
            case ("lower left"):
            case ("ll"):
                dir = Direction.LOWER_LEFT;
                break;
            case ("left"):
            case ("l"):
                dir = Direction.LEFT;
                break;
            case("upper left"):
            case("ul"):
                dir = Direction.UPPER_LEFT;
                break;
            default:
                System.out.println("Invalid direction. Use one of the following:\n"
                	+ "ur\t-\tupper right\n"
                	+ "r\t-\tright\n"
                	+ "lr\t-\tlower right\n"
                	+ "ll\t-\tlower left\n"
                	+ "l\t-\tleft\n"
                	+ "ul\t-\tupper left");
        }
        return dir;
    }

    /**
     * Reads one line from the user.
     * @return a String, not null.
     */
    public static String readString() {
        String response = null;
        while (response == null) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                response = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    @Override
    public void updateQueues(int twoPlayerSize, int threePlayerSize, int fourPlayerSize) {
        System.out.println("Two player queues: " + twoPlayerSize + " players.\n"
                + "Three player queues: " + threePlayerSize + " players.\n"
                + "Four player queues: " + fourPlayerSize + " players.\n");
    }

    @Override
    public void updateQueue(int gameSize, int queueSize) {
        System.out.println("You and " + (queueSize - 1)
                + " other players are waiting for a " + gameSize + "-player game.");
    }


}
