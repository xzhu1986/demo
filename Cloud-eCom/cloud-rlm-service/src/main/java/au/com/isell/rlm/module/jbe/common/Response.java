package au.com.isell.rlm.module.jbe.common;



/**
 * Created by IntelliJ IDEA.
 * User: kierend
 * Date: 9/08/2005
 * Time: 16:04:12
 * To change this template use File | Settings | File Templates.
 */
public class Response extends DataElement {
    private int responsecode;
    private int errorcode;
    private String message;


    public Response() {
        super("body");
    }

    public int getResponsecode() {
        return responsecode;
    }

    public void setResponsecode(int responsecode) {
        this.responsecode = responsecode;
    }

    public int getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(int errorcode) {
        this.errorcode = errorcode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
