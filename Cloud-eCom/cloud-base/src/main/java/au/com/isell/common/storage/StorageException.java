package au.com.isell.common.storage;

public class StorageException extends RuntimeException {

	private static final long serialVersionUID = 3348260701738407227L;

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}

	public StorageException(String message) {
		super(message);
	}

	public StorageException(Throwable cause) {
		super(cause);
	}

}
