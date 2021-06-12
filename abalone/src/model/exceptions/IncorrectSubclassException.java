package model.exceptions;

public class IncorrectSubclassException extends RuntimeException {
    //
	private static final long serialVersionUID = 5111349556680061332L;

	public IncorrectSubclassException() {
    }

    public IncorrectSubclassException(String message) {
        super(message);
    }
}
