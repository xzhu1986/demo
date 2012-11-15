package au.com.isell.common.exi;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.siemens.ct.exi.api.stream.StAXDecoder;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;
import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.AbstractPullReader;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

public class EXIStreamReader extends AbstractPullReader {

	private final QNameMap qnameMap;
    private final StAXDecoder in;

    public EXIStreamReader(QNameMap qnameMap, InputStream in) throws EXIException, IOException, XMLStreamException {
        this(qnameMap, in, new XmlFriendlyNameCoder());
    }

    /**
     * @throws EXIException 
     * @throws XMLStreamException 
     * @throws IOException 
     */
    public EXIStreamReader(QNameMap qnameMap, InputStream in, NameCoder replacer) throws EXIException, IOException, XMLStreamException {
        super(replacer);
        this.qnameMap = qnameMap;
        this.in = new StAXDecoder(DefaultEXIFactory.newInstance());
        this.in.setInputStream(in);
        moveDown();
    }

    @Override
	protected int pullNextEvent() {
        try {
            switch(in.next()) {
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.START_ELEMENT:
                    return START_NODE;
                case XMLStreamConstants.END_DOCUMENT:
                case XMLStreamConstants.END_ELEMENT:
                    return END_NODE;
                case XMLStreamConstants.CHARACTERS:
                    return TEXT;
                case XMLStreamConstants.COMMENT:
                    return COMMENT;
                default:
                    return OTHER;
            }
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    @Override
	protected String pullElementName() {
        // let the QNameMap handle any mapping of QNames to Java class names
        QName qname = in.getName();
        return qnameMap.getJavaClassName(qname);
    }

    @Override
	protected String pullText() {
        return in.getText();
    }

    @Override
	public String getAttribute(String name) {
    	//in.getAttributeValue(arg0, arg1) is not implemented
    	int attrCount=in.getAttributeCount();
        if(attrCount==0) return null;
    	for(int i=0;i<attrCount;i++){
    		if(in.getAttributeName(i).getLocalPart().equals(name)){
    			return in.getAttributeValue(i);
    		}
    	}
        return null;
    }

    @Override
	public String getAttribute(int index) {
        return in.getAttributeValue(index);
    }

    @Override
	public int getAttributeCount() {
        return in.getAttributeCount();
    }

    @Override
	public String getAttributeName(int index) {
        return decodeAttribute(in.getAttributeLocalName(index));
    }

    @Override
	public void appendErrors(ErrorWriter errorWriter) {
        errorWriter.add("line number", String.valueOf(in.getLocation().getLineNumber()));
    }

    @Override
	public void close() {
        try {
            in.close();
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

}
