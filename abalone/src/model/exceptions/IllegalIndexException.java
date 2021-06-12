package model.exceptions;

public class IllegalIndexException extends RuntimeException {
    //
	private static final long serialVersionUID = -8935117654984753832L;

	public IllegalIndexException() {
    }

    public IllegalIndexException(String message) {
        super(message);
    }
}
