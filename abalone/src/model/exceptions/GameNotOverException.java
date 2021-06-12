package model.exceptions;

public class GameNotOverException extends RuntimeException {
    //
	private static final long serialVersionUID = 6508038707555873701L;

	public GameNotOverException() {
    }

    public GameNotOverException(String message) {
        super(message);
    }
}
