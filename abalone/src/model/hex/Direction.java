package model.hex;

import java.util.ArrayList;
import java.util.List;

public enum Direction {
	//

    UPPER_RIGHT,
    RIGHT,
    LOWER_RIGHT,
    LOWER_LEFT,
    LEFT,
    UPPER_LEFT;
    
    /**
     * Returns this rotated 60 degrees to the right.
     * @return this rotated 60 degrees to the right.
     */
    public Direction rotateRight() {
        switch (this) {
            case UPPER_RIGHT:
                return RIGHT;
            case RIGHT:
                return LOWER_RIGHT;
            case LOWER_RIGHT:
                return LOWER_LEFT;
            case LOWER_LEFT:
                return LEFT;
            case LEFT:
                return UPPER_LEFT;
            case UPPER_LEFT:
                return UPPER_RIGHT;
            default:
                throw new IllegalStateException("This should be unreachable code.");
        }
    }

    /**
     * Returns this rotated 60 * times degrees to the right.
     * @param times The number of times a 60 degree rotation to the right is made.
     * @return this rotated 60 * times degrees to the right.
     */
    public Direction rotateRight(int times) {
        Direction result = this;
        times = times % 6;
        for (int i = 0; i < times; i++) {
            result = result.rotateRight();
        }
        return result;
    }

    /**
     * Returns this rotated 60 degrees to the left.
     * @return this rotated 60 degrees to the left.
     */
    public Direction rotateLeft() {
        return rotateRight(5);
    }

    /**
     * Returns this rotated 60 * times degrees to the left.
     * @param times The number of times a 60 degree rotation to the left is made.
     * @return this rotated 60 * times degrees to the left.
     */
    public Direction rotateLeft(int times) {
        times = times % 6;
        times = 6 - times;
        return rotateRight(times);
    }

    /**
     * Take the direction opposite to this.
     * This is functionally identical to rotateRight(3).
     * @return the direction opposite to this.
     */
    public Direction invert() {
        return this.rotateRight(3);
    }

    /**
     * Returns a list of all directions.
     * @return a list of all directions.
     */
    public static List<Direction> directions() {
        List<Direction> dirs = new ArrayList<>();
        dirs.add(UPPER_RIGHT);
        dirs.add(RIGHT);
        dirs.add(LOWER_RIGHT);
        dirs.add(LOWER_LEFT);
        dirs.add(LEFT);
        dirs.add(UPPER_LEFT);
        return dirs;
    }

    /**
     * Takes string ur, r, lr, ll, l, ul (ignores case) to convert it into the respective
     * directions, or null if the string is not one of those values.
     * @param dir String representing a direction.
     * @return UPPER_RIGHT, RIGHT, LOWER_RIGHT, LOWER_LEFT, LEFT, UPPER_LEFT if the string is
     *     ur, r, lr, ll, l, ul (ignores case) respectively, or null otherwise.
     */
    public static Direction stringToDirection(String dir) {
        switch (dir.toLowerCase()) {
            case "ur":
                return UPPER_RIGHT;
            case "r":
                return RIGHT;
            case "lr":
                return LOWER_RIGHT;
            case "ll":
                return LOWER_LEFT;
            case "l":
                return LEFT;
            case "ul":
                return UPPER_LEFT;
            default:
                return null;
        }
    }
    
    /**
     * Converts a hex with length 1 to a direction.
     * @param hex The hex to be converted.
     * @return A representation of the hex from type Direction.
     */
    public static Direction fromHex(Hex hex) {
    	if (hex.length() == 1) {
    		for (Direction dir : directions()) {
    			if (Hex.direction(dir).equals(hex)) {
    				return dir;
    			}
     		}
    	}
    	throw new IllegalArgumentException("Hex " + hex + " cannot be converted to a direction.");
    }

}
