package model.gamelogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.artificialintelligence.minimax.GameStateEvaluator;
import model.color.ConsoleColors;
import model.hex.Direction;
import model.hex.Hex;

public class Board {
	//
	HashMap<Hex, Field> board;
	Set<Marble> marbles;
	long boardHash;
	
    public static final int BOARD_RADIUS = 5;
    
    /**
     * Returns a Hex from the Abalone coordinate system.
     * @param coordinate A String matching (?i)[a-i][1-9]. 
     * @return A Hex from the arguments.
     */
    public static Hex abalToCube(String coordinate) {
    	coordinate = coordinate.toLowerCase();
    	assert (coordinate.matches("[a-i][1-9]"));
    	int q = Character.getNumericValue(coordinate.charAt(1)) - 5;
    	int s = 'e' - coordinate.charAt(0);
    	int r = -q - s;
    	return new Hex(q, r, s);
    }
    
    /**
     * Returns an abalone coordinate from a Hex.
     * @param hex A hex of the coordinate. 
     * @return A string representation of the hex in the abalone coordinate system.
     */
    public static String cubeToAbal(Hex hex) {
    	assert (hex.length() < BOARD_RADIUS);
    	String result = "";
    	result += (char)('e' - hex.coordS);
    	result += (hex.coordQ + 5);
    	return result;
    }
    
    StartingLayout startingLayout;
	
            
	public Board(List<Team> teams) {
		this(teams, null);
	}
	
	public Board(List<Team> teams, StartingLayout layout) {
		initialize(teams, layout);
	}
	
	public long getBoardHash() {
		return this.boardHash;
	}
	
	void updateBoardHash(Move move) {
		this.boardHash = GameStateEvaluator.hashOfBoard(this.boardHash, move);
	}
	
    /**
     * Returns all fields that map from the given hexagons.
     * @param hexes A list of hexagon.
     * @requires Every hexagon in hexagons should be a legal coordinate (distance to center < 5)
     * @return All fields that the hexagons map to.
     */
    Set<Field> getFields(Set<Hex> hexes) {
    	assert (hexes != null);
    	assert (!hexes.stream() // Assert not null and within range of the board.
    			.anyMatch((x) -> (x == null || x.length() > BOARD_RADIUS - 1)));
    	Set<Field> result = new HashSet<>();
    	for (Hex hex : hexes) {
    		result.add(board.get(hex));
    	}
    	return result;
    }

    /**
     * Returns the field of the given hex, null if no such Field exists.
     * @param hex The hexagon pointing to a field.
     * @return The field corresponding to the given hex, or null if no field corresponds to the hex.
     */
    public Field getField(Hex hex) {
    	assert (hex != null);
    	return hex.length() < Board.BOARD_RADIUS ? board.get(hex) : null;
    }
    
    public Set<Marble> getMarbles() {
    	return this.marbles;
    }
    
    Field getFieldOffset(Hex hex, Direction dir, int distance) {
    	assert (hex != null && dir != null);
    	return board.get(hex.neighbour(dir, distance));
    }

    /**
     * Resets the board to the default state for the given number of players.
     * @ensures getBoard().size() = 61, 
     *     For 2 players, each will have 14 marbles on their starting position.
     *     For 3 players, each will have 11 marbles on their starting position.
     *     For 4 players, each will have 9 marbles on their starting position.
     */
    public void initialize(List<Team> teams, StartingLayout layout) {
    	determineStartingLayout(teams, layout);
    	initializeGrid();
        populate(teams, layout);
        initializeMarbleSet(teams);
        this.boardHash = GameStateEvaluator.hashOfBoard(this);
    }

    void determineStartingLayout(List<Team> teams, StartingLayout layout) {
    	if (layout != null) {
    		this.startingLayout = layout;
    	} else {
	    	int nrOfPlayers = 0;
	        for (Team t : teams) {
	            nrOfPlayers += t.players.size();
	        }
	        assert (2 <= nrOfPlayers && nrOfPlayers <= 4);
	        this.startingLayout = 
	        		nrOfPlayers == 2 ? StartingLayout.STANDARD2 
	        		: nrOfPlayers == 3 ? StartingLayout.STANDARD3 
	        		: StartingLayout.STANDARD4;
    	}
    }
    
    /**
     * Resets the grid of this board. Populate() should be called afterwards.
     * @ensures getBoard().size() = 61
     */
    void initializeGrid() {
        board = new HashMap<Hex, Field>();
        for (Hex hex : Hex.build(BOARD_RADIUS)) {
            board.put(hex, new Field(this, hex));
        }
    }

    /**
     * Populates the grid with marbles of the participating players.
     * resetGrid() should be called beforehand.
     * @param teams The teams with their respective players.
     * @ensures For 2 players, each will have 14 marbles.
     *     For 3 players, each will have 11 marbles.
     *     For 4 players, each will have 9 marbles.
     */
    private void populate(List<Team> teams, StartingLayout layout) {
        int nrOfPlayers = 0;
        for (Team t : teams) {
            nrOfPlayers += t.players.size();
        }

        if (!(2 <= nrOfPlayers && nrOfPlayers <= 4)) {
            throw new IllegalArgumentException(
                    "Expected 2 to 4 players. Actual: " + nrOfPlayers);
        }

        Player player1;
        Player player2;
        Player player3;
        Player player4;

        switch (nrOfPlayers) {
            case 2:
                player1 = teams.get(0).players.get(0);
                player2 = teams.get(1).players.get(0);

                populatePlayerMarbles(player1, startingLayout.getStartingPosition(player1));
                populatePlayerMarbles(player2, startingLayout.getStartingPosition(player2));
                break;
            case 3:
                player1 = teams.get(0).players.get(0);
                player2 = teams.get(1).players.get(0);
                player3 = teams.get(2).players.get(0);

                populatePlayerMarbles(player1, startingLayout.getStartingPosition(player1));
                populatePlayerMarbles(player2, startingLayout.getStartingPosition(player2));
                populatePlayerMarbles(player3, startingLayout.getStartingPosition(player3));
                break;
            case 4:
                player1 = teams.get(0).players.get(0);
                player2 = teams.get(0).players.get(1);
                player3 = teams.get(1).players.get(0);
                player4 = teams.get(1).players.get(1);

                populatePlayerMarbles(player1, startingLayout.getStartingPosition(player1));
                populatePlayerMarbles(player2, startingLayout.getStartingPosition(player2));
                populatePlayerMarbles(player3, startingLayout.getStartingPosition(player3));
                populatePlayerMarbles(player4, startingLayout.getStartingPosition(player4));
                break;
            default:
                throw new Error("Reached unreachable code."
                        + "The game should consist of 2, 3 or 4 players but consists of "
                        + nrOfPlayers + " players.");
        }
    }

    /**
     * For a given player and grid positions on the board, this function populates those positions by
     * marbles owned by the player.
     * @param player The player that will own the marbles.
     * @param positions The positions that will hold the marbles.
     * @requires player != null, positions != null, positions.stream.allMatch((p -> p != null))
     * @ensures The player will own exactly positions.length() marbles, which are placed on the positions.
     */
    void populatePlayerMarbles(Player player, List<Hex> positions) {
        player.createMarbles(positions.size());
        for (int i = 0; i < positions.size(); i++) {
            Field field = board.get(positions.get(i));
            field.setMarble(player.getMarbles().get(i));
        }
    }

    void initializeMarbleSet(List<Team> teams) {
    	marbles = new HashSet<>();
        for (Team team : teams) {
			for (Player player : team.players) {
				for (Marble marble : player.getMarbles()) {
					marbles.add(marble);
				}
			}
        }
    }
    
    /**
     * Creates a String representation for the board of this board, then returns it.
     * @requires ANSI color coding in order for the result to be remotely human-readable.
     * @ensures result != null
     * @return The String representation for the board of this gameState.
     */
    @Override
    public String toString() {
        // acquire a sorted board
        List<Hex> gridInOrder = new ArrayList<Hex>();
        gridInOrder.addAll(Hex.build(BOARD_RADIUS));
        Collections.sort(gridInOrder);

        // for this sorted board, combine all toStrings
        StringBuilder result = new StringBuilder(ConsoleColors.RESET + "    " + ConsoleColors.GREEN_BACKGROUND);
        Hex prev = null;
        for (Hex hex : gridInOrder) {
            // Check if this marble belongs to the next row
            if (prev != null && prev.coordS < hex.coordS) {
                result.append(" " + ConsoleColors.RESET + System.lineSeparator());
                if (hex.coordS != 0) {
                    result.append(String.format("%" + Math.abs(hex.coordS) + "s", ""));
                }
                result.append(ConsoleColors.GREEN_BACKGROUND);
            }

            result.append(board.get(hex).toString());
            prev = hex;
        }
        result.append(" " + ConsoleColors.RESET);
        return result.toString();
    }
    
}
