package model.gamelogic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.hex.FractionalHex;
import model.hex.Hex;

@SuppressWarnings("serial")
public enum StartingLayout {
	//
	STANDARD2,
	STANDARD3,
	STANDARD4,
	BELGIAN_DAISY;
	
	final List<Hex> getStartingPosition(Player player) {
		switch (this) {
			case STANDARD2:
				return player.getColor().equals(Player.BLACK)
					? STARTING_POSITION_TWO_PLAYER_BLACK : STARTING_POSITION_TWO_PLAYER_WHITE;
			case STANDARD3:
				switch (player.getColor()) {
					case Player.BLACK:
						return STARTING_POSITION_THREE_PLAYER_LEFT;
					case Player.WHITE:
						return STARTING_POSITION_THREE_PLAYER_RIGHT;
					case Player.BLUE:
						return STARTING_POSITION_THREE_PLAYER_LOWER;
					default:
						throw new IllegalStateException("Unreachable state.");
				}
			case STANDARD4:
				switch (player.getColor()) {
					case Player.BLACK:
						return STARTING_POSITION_FOUR_PLAYER_LEFT;
					case Player.WHITE:
						return STARTING_POSITION_FOUR_PLAYER_RIGHT;
					case Player.BLUE:
						return STARTING_POSITION_FOUR_PLAYER_LOWER;
					case Player.RED:
						return STARTING_POSITION_FOUR_PLAYER_UPPER;
					default:
						throw new IllegalStateException("Unreachable state.");
				}
			case BELGIAN_DAISY:
				return player.getColor().equals(Player.BLACK)
					? STARTING_POSITION_BELGIAN_DAISY_BLACK : STARTING_POSITION_BELGIAN_DAISY_WHITE;
			default:
				throw new IllegalStateException("Unreachable state.");
		}
	}
	
    private static final List<Hex> BELGIAN_DAISY_DOT = new ArrayList<Hex>() {{
			addAll(FractionalHex.hexLinedraw(
				new Hex(0, 4, -4),
				new Hex(1, 3, -4)));
			addAll(FractionalHex.hexLinedraw(
				new Hex(-1, 4, -3),
				new Hex(1, 2, -3)));
			addAll(FractionalHex.hexLinedraw(
				new Hex(-1, 3, -2),
				new Hex(0, 2, -2)));
			sort(Comparator.<Hex>naturalOrder());
		}};
	private static final List<Hex> STARTING_POSITION_BELGIAN_DAISY_WHITE = new ArrayList<Hex>() {{
			addAll(BELGIAN_DAISY_DOT);
			addAll(Hex.rotateArrayRight(BELGIAN_DAISY_DOT, 3));	
			sort(Comparator.<Hex>naturalOrder());
		}};		
    private static final List<Hex> STARTING_POSITION_BELGIAN_DAISY_BLACK = new ArrayList<Hex>() {{
    		addAll(Hex.rotateArrayRight(BELGIAN_DAISY_DOT, 1));
    		addAll(Hex.rotateArrayRight(BELGIAN_DAISY_DOT, 4));
			sort(Comparator.<Hex>naturalOrder());
		}};
    
    private static final List<Hex> STARTING_POSITION_TWO_PLAYER_BLACK = new ArrayList<Hex>() {{
            addAll(FractionalHex.hexLinedraw(
                     new Hex(-4, 0, 4),
                     new Hex(0, -4, 4)));
            addAll(FractionalHex.hexLinedraw(
                     new Hex(-4, 1, 3),
                     new Hex(1, -4, 3)));
            addAll(FractionalHex.hexLinedraw(
                     new Hex(-2, 0, 2),
                     new Hex(0, -2, 2)));
            sort(Comparator.<Hex>naturalOrder());
        }};        
    private static final List<Hex> STARTING_POSITION_TWO_PLAYER_WHITE = new ArrayList<Hex>() {{
            addAll(Hex.rotateArrayRight(STARTING_POSITION_TWO_PLAYER_BLACK, 3));
            sort(Comparator.<Hex>naturalOrder());
            }};

    private static final List<Hex> STARTING_POSITION_THREE_PLAYER_LOWER = new ArrayList<Hex>() {{
            addAll(FractionalHex.hexLinedraw(
                    new Hex(-4, 0, 4),
                    new Hex(0, -4, 4)));
            addAll(FractionalHex.hexLinedraw(
                    new Hex(-4, 1, 3),
                    new Hex(1, -4, 3)));
            sort(Comparator.<Hex>naturalOrder());
            }};
    private static final List<Hex> STARTING_POSITION_THREE_PLAYER_RIGHT = new ArrayList<Hex>() {{
            addAll(Hex.rotateArrayRight(STARTING_POSITION_THREE_PLAYER_LOWER, 2));
            sort(Comparator.<Hex>naturalOrder());
            }};

    private static final List<Hex> STARTING_POSITION_THREE_PLAYER_LEFT = new ArrayList<Hex>() {{
            addAll(Hex.rotateArrayLeft(STARTING_POSITION_THREE_PLAYER_LOWER, 2));
            sort(Comparator.<Hex>naturalOrder());
            }};
    private static final List<Hex> STARTING_POSITION_FOUR_PLAYER_LOWER = new ArrayList<Hex>() {{
            addAll(FractionalHex.hexLinedraw(
                    new Hex(-3, -1, 4),
                    new Hex(0, -4, 4)));
            addAll(FractionalHex.hexLinedraw(
                    new Hex(-2, -1, 3),
                    new Hex(0, -3, 3)));
            addAll(FractionalHex.hexLinedraw(
                    new Hex(-1, -1, 2),
                    new Hex(0, -2, 2)));
            sort(Comparator.<Hex>naturalOrder());
            }};
    private static final List<Hex> STARTING_POSITION_FOUR_PLAYER_UPPER = new ArrayList<Hex>() {{
            addAll(Hex.rotateArrayRight(STARTING_POSITION_FOUR_PLAYER_LOWER, 3));
            sort(Comparator.<Hex>naturalOrder());
            }};
    private static final List<Hex> STARTING_POSITION_FOUR_PLAYER_RIGHT = new ArrayList<Hex>() {{
            addAll(FractionalHex.hexLinedraw(
                    new Hex(4, -4, 0),
                    new Hex(4, -1, -3)));
            addAll(FractionalHex.hexLinedraw(
                    new Hex(3, -3, 0),
                    new Hex(3, -1, -2)));
            addAll(FractionalHex.hexLinedraw(
                    new Hex(2, -2, 0),
                    new Hex(2, -1, -1)));
            sort(Comparator.<Hex>naturalOrder());
            }};
    private static final List<Hex> STARTING_POSITION_FOUR_PLAYER_LEFT = new ArrayList<Hex>() {{
            addAll(Hex.rotateArrayRight(STARTING_POSITION_FOUR_PLAYER_RIGHT, 3));
            sort(Comparator.<Hex>naturalOrder());
            }};
	

}
