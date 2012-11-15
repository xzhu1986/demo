package au.com.isell.rlm.module.jbe.common;

import org.xml.sax.*;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The SAX Parser Handler for convert xml text contents into XMLRequest object.
 */
public class MessageParser extends DefaultHandler2 {
    private static final Logger logger = Logger.getLogger(MessageParser.class.getName());

    private Message message = new Message();

    private StringBuilder sbCharacters;
    private DataElement current;

    private boolean hasRoot = false;
    private boolean hasHeader = false;
    private boolean hasBody = false;
    private boolean inHeader = false;

    private MessageParser() {
    }

    private void assertName(String name, String shouldEqual) throws SAXException {
        if (!name.equals(shouldEqual)) {
            throw new SAXException("Invalid xml document, was expecting '" + shouldEqual + "'");
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        sbCharacters = new StringBuilder();

        if (!hasRoot) {
            assertName(qName, Message.MESSAGE);
            current = message;
            hasRoot = true;
        } else if (!hasHeader) {
            assertName(qName, Message.HEADER);
            current = message.getHeader();
            hasHeader = true;
            inHeader = true;
        } else if (!hasBody && !inHeader) {
            assertName(qName, message.getBody().getName());
            current = message.getBody();
            hasBody = true;
        } else {
            current = current.addChild(qName);
        }
        if ("true".equals(attributes.getValue("repeatable"))) {
            current.setRepeatable(true);
        } else {
            current.setRepeatable(false);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inHeader && Message.HEADER.equals(qName)) {
            inHeader = false;
        } else {
            if (sbCharacters != null) {
                String content = sbCharacters.toString();
                current.setValue(convertCFLR(content));
                sbCharacters = null;
            }
        }
        current = current.getParent();
    }

    private static String convertCFLR(String string) {
        return string.replace("\r\n", "\n").replace("\n", "\r\n").replace("&#9;", "\t");
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    public void warning(SAXParseException e) throws SAXException {
        logger.warning(e.getMessage());
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (sbCharacters != null) {
            sbCharacters.append(ch, start, length);
        }
    }

    public Message getMessage() {
        return message;
    }


    public static void main(String[] args) throws Exception {
        File file = new File("c:\\testbr.xml");
        FileInputStream fin = new FileInputStream(file);
        Message message = parseMessage(streamfilter(fin));
        MessageWriter writer = new MessageWriter(System.out);
        writer.writeMessage(message);
    }

    public static Message parseMessage(InputStream in) throws SAXException, IOException {
        MessageParser handler = new MessageParser();

        XMLReader parser = XMLReaderFactory.createXMLReader();
        InputSource source = new InputSource(streamfilter(in));

        parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        parser.setContentHandler(handler);
        parser.setErrorHandler(handler);
        parser.parse(source);

        return handler.getMessage();
    }

     public static InputStream streamfilter(InputStream in) {
        InputStream in1 = null;
        try {
            StringBuilder sb = new StringBuilder();
            for (int c = in.read(); c != -1; c = in.read()) {
                sb.append((char) c);
            }
            char[] charAry = sb.toString().toCharArray();
            sb = new StringBuilder();
            for (char c : charAry) {
                if ((c >= 1) && (c <= 9)) {
                    sb.append("&#").append((int) c).append(";");
                } else if (((c > 10) && (c <13))||((c>13)&&(c<31))) {
                    sb.append("&#10;");
                } else if ((c >= 127) && (c < 251)) {
                    sb.append("&#").append((int) c).append(";");
                } else if ((c >= 252) && (c < 255)) {
                    sb.append("&#10;");
                } else {
                    sb.append((char)c);
                }
            }

            in1 = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
        return in1;
    }

     public static String byteToString(byte b) {
        byte high, low;
        byte maskHigh = (byte) 0xf0;
        byte maskLow = 0x0f;

        high = (byte) ((b & maskHigh) >> 4);
        low = (byte) (b & maskLow);

        StringBuffer buf = new StringBuffer();
        buf.append(findHex(high));
        buf.append(findHex(low));

        return buf.toString();
    }

    private static char findHex(byte b) {
        int t = new Byte(b).intValue();
        t = t < 0 ? t + 16 : t;

        if ((0 <= t) && (t <= 9)) {
            return (char) (t + '0');
        }

        return (char) (t - 10 + 'A');
    }
}
