package au.com.isell.rlm.module.jbe.common;

public class JBEException extends Exception {

	private static final long serialVersionUID = -2579371416094548173L;

	public JBEException() {
		super();
	}

	public JBEException(String message, Throwable t) {
		super(message, t);
	}

	public JBEException(String message) {
		super(message);
	}

	public JBEException(Throwable t) {
		super(t);
	}

}
