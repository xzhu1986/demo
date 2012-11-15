package au.com.isell.rlm.module.jbe.common;

/**
 * Created by IntelliJ IDEA.
 * User: kierend
 * Date: 11/08/2005
 * Time: 09:42:12
 * To change this template use File | Settings | File Templates.
 */
public class Message extends DataElement {
    public static final String MESSAGE = "message";
    public static final String HEADER = "header";
    public static final String BODY = "body";

    Header header = new Header();
    DataElement body = new DataElement("body");

    public Message(DataElement body) {
        super("message");
        this.body = body;
        addChild(header);
        addChild(body);
    }

    public Message() {
        this(new DataElement("body"));
    }

    public Header getHeader() {
        return header;
    }

    public DataElement getBody() {
        return body;
    }

}
