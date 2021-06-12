package model.exceptions;

public class IllegalMoveException extends RuntimeException {
	//
	private static final long serialVersionUID = -2187571461837308759L;

	public IllegalMoveException() {
    }

    public IllegalMoveException(String message) {
        super(message);
    }
}
