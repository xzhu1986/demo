package au.com.isell.rlm.module.jbe.common;



/**
 * User: kierend
 * Date: 24/02/2005
 * Time: 09:52:07
 */
public class XMLFormatException extends XMLException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -323181963200277975L;

	public XMLFormatException(String message) {
        super(message);
    }

    public XMLFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMLFormatException(Throwable cause) {
        super(cause);
    }
}
