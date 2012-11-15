package au.com.isell.common.storage;

public class NotFoundException extends Exception {

	private static final long serialVersionUID = -6373577621343071124L;

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}
	
	public NotFoundException(Class<?> clazz, String key) {
		super("Type " + clazz.getName() +" with key "+ key + " is not found.");
	}

}
