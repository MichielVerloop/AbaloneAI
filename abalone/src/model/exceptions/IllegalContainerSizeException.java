package model.exceptions;

public class IllegalContainerSizeException extends IllegalArgumentException {
    //
	private static final long serialVersionUID = 2687208642527109998L;
	
	int min;
    int max;
    int actual;

    /**
     * Constructs an IllegalContainerSizeException.
     * @param min The minimum size the container can be (inclusive).
     * @param max The maximum size the container can be (inclusive).
     * @param actual The actual size of the container.
     */
    public IllegalContainerSizeException(int min, int max, int actual) {
        this.min = min;
        this.max = max;
        this.actual = actual;
    }

    @Override
    public String getMessage() {
        return String.format("Container size should be between %d and %d but was %d", min, max, actual);
    }
}
