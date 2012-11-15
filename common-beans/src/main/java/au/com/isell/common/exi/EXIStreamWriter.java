package au.com.isell.common.exi;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.siemens.ct.exi.api.stream.StAXEncoder;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;
import com.thoughtworks.xstream.io.AbstractWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;


/**
 * A stream writing that outputs to a StAXEncoder
 * 
 * @author Bruce Zhou
 */
public class EXIStreamWriter extends AbstractWriter {

    private final QNameMap qnameMap;
    private final StAXEncoder encoder;
    private final boolean writeEnclosingDocument;
    private boolean namespaceRepairingMode;

    private int tagDepth;
	private OutputStream out;

    public EXIStreamWriter(QNameMap qnameMap, OutputStream out) throws XMLStreamException, EXIException, IOException {
        this(qnameMap, out, true, true);
    }

    /**
     * Allows a StaxWriter to be created for partial XML output
     * 
     * @param qnameMap is the mapper of Java class names to QNames
     * @param out the stream to output to
     * @param nameCoder the xml-friendly replacer to escape Java names
     * @throws XMLStreamException if the events could not be written to the output
     */
    public EXIStreamWriter(QNameMap qnameMap, OutputStream out, NameCoder nameCoder)
        throws XMLStreamException, EXIException, IOException {
        this(qnameMap, out, true, true, nameCoder);
    }

    /**
     * Allows a StaxWriter to be created for partial XML output
     * 
     * @param qnameMap is the mapper of Java class names to QNames
     * @param out the stream to output to
     * @param writeEnclosingDocument a flag to indicate whether or not the start/end document
     *            events should be written
     * @param namespaceRepairingMode a flag to enable StAX' namespace repairing mode
     * @param nameCoder the xml-friendly replacer to escape Java names
     * @throws XMLStreamException if the events could not be written to the output
     * @throws EXIException 
     * @throws IOException
     */
    public EXIStreamWriter(
        QNameMap qnameMap, OutputStream out, boolean writeEnclosingDocument,
        boolean namespaceRepairingMode, NameCoder nameCoder) throws XMLStreamException, EXIException, IOException {
        super(nameCoder);
        this.qnameMap = qnameMap;
        this.out = out;
        encoder = new StAXEncoder(DefaultEXIFactory.newInstance());
        encoder.setOutputStream(out);
        this.writeEnclosingDocument = writeEnclosingDocument;
        this.namespaceRepairingMode = namespaceRepairingMode;
        if (writeEnclosingDocument) {
            encoder.writeStartDocument();
        }
    }

    /**
     * Allows a StaxWriter to be created for partial XML output
     * 
     * @param qnameMap is the mapper of Java class names to QNames
     * @param out the stream to output to
     * @param writeEnclosingDocument a flag to indicate whether or not the start/end document
     *            events should be written
     * @throws XMLStreamException if the events could not be written to the output
     */
    public EXIStreamWriter(
        QNameMap qnameMap, OutputStream out, boolean writeEnclosingDocument,
        boolean namespaceRepairingMode) throws XMLStreamException, EXIException, IOException {
        this(
            qnameMap, out, writeEnclosingDocument, namespaceRepairingMode,
            new XmlFriendlyNameCoder());
    }

    @Override
	public void flush() {
        try {
            out.flush();
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    /**
     * Call this method when you're finished with me
     */
    @Override
	public void close() {
        try {
            out.close();
        } catch (IOException e) {
            throw new StreamException(e);
        }
    }

    @Override
	public void addAttribute(String name, String value) {
        try {
            encoder.writeAttribute("", "", name, value);
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    @Override
	public void endNode() {
        try {
            tagDepth-- ;
            encoder.writeEndElement();
            if (tagDepth == 0 && writeEnclosingDocument) {
                encoder.writeEndDocument();
            }
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    @Override
	public void setValue(String text) {
        try {
            encoder.writeCharacters(text);
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    @Override
	public void startNode(String name) {
        try {
            QName qname = qnameMap.getQName(encodeNode(name));
            String prefix = qname.getPrefix();
            String uri = qname.getNamespaceURI();

            encoder.writeStartElement(prefix, qname.getLocalPart(), uri);
            tagDepth++ ;
        } catch (XMLStreamException e) {
            throw new StreamException(e);
        }
    }

    /**
     * Is StAX namespace repairing mode on or off?
     */
    public boolean isNamespaceRepairingMode() {
        return namespaceRepairingMode;
    }

    protected QNameMap getQNameMap() {
        return this.qnameMap;
    }

}
