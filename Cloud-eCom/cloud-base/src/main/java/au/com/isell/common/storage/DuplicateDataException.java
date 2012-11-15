package au.com.isell.common.storage;


public class DuplicateDataException extends StorageException {

	private static final long serialVersionUID = 5278188329632798586L;

	public DuplicateDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateDataException(Throwable cause) {
		super(cause);
	}

	public DuplicateDataException(String message) {
		super(message);
	}

}
