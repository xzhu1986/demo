package au.com.isell.rlm.module.jbe.common;


/**
 * User: kierend
 * Date: 24/02/2005
 * Time: 09:51:20
 */
public class XMLException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5046514032437214053L;

	public XMLException(String message) {
        super(message);
    }

    public XMLException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMLException(Throwable cause) {
        super(cause);
    }
}
