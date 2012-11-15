package au.com.isell.rlm.module.jbe.common;

/**
 * Created by IntelliJ IDEA.
 * User: kierend
 * Date: 9/08/2005
 * Time: 17:46:35
 * To change this template use File | Settings | File Templates.
 */
public class Header extends DataElement {
    public static final String REQ_USERNAME = "username";
    public static final String REQ_PASSWORD = "password";
    public static final String REQ_NAME = "name";


//    public static final String RESP_CODE = "responseCode";
    public static final String ERROR_CODE = "errorCode";
    public static final String RESP_MESSAGE = "message";

    public Header() {
        super("header");
    }

}
