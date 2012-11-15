package au.com.isell.common.index;

public class IndexException extends RuntimeException {

	private static final long serialVersionUID = -3839341592413360316L;

	public IndexException(String message, Throwable cause) {
		super(message, cause);
	}

	public IndexException(String message) {
		super(message);
	}

	public IndexException(Throwable cause) {
		super(cause);
	}

}
