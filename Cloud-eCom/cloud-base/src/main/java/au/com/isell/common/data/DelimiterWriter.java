package au.com.isell.common.data;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DelimiterWriter {
	public static final char DEFAULT_DELIMETER = '\t';
    public static final String WINDOWS_NEW_LINE = "\r\n";
    public static final String UNIX_NEW_LINE = "\n";
    public static final String DEFAULT_QUOTE = "\"";
    public static final String DEFAULT_CHAR_SET = "ISO-8859-1";
    private BufferedOutputStream out = null;
    private String delimeter;
    private String newLine;
    private String charSet = DEFAULT_CHAR_SET;

    private boolean enableQuote = true;
    private boolean forceQuote = false;
    
    private boolean start = true;
	
	public DelimiterWriter(OutputStream writer, char delimeter, String newLine) {
		if (writer instanceof BufferedOutputStream) this.out = (BufferedOutputStream) writer;
		else this.out = new BufferedOutputStream(writer);
        this.delimeter = String.valueOf(delimeter);
        this.newLine = newLine;
    }

    public DelimiterWriter(OutputStream writer) {
        this(writer, DEFAULT_DELIMETER, WINDOWS_NEW_LINE);
    }

    public void writeEmptyLine(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            write("");
        }
       	newLine();
    }

    public void writeLine(String... values) throws IOException {
        writeAryLine(values);
    }

    public void writeAryLine(String[] values) throws IOException {
        for (String value : values) {
            write(value);
        }
        newLine();
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(String value) throws IOException {
        write(value, -1);
    }

    protected String formatCol(String value, boolean enableQuote,
            boolean forceQuote, String delimeter, String newLine) {
        String v = value;
        if (value == null) {
            v = "";
        } else {
            if (forceQuote
                    || (enableQuote && (value.contains(DEFAULT_QUOTE)
                            || value.contains(delimeter)
                            || value.contains(WINDOWS_NEW_LINE) 
                            || value.contains(UNIX_NEW_LINE)))) {
                v = DEFAULT_QUOTE
                        + value.replace(DEFAULT_QUOTE, DEFAULT_QUOTE
                                + DEFAULT_QUOTE) + DEFAULT_QUOTE;
            }
        }
        return v;
    }

    public void write(char value) throws IOException {
    	checkStart();
        out.write(value);
    }

    public void write(String value, int maxLen) throws IOException {
    	checkStart();
        String v = formatCol(value, enableQuote, forceQuote, delimeter, newLine);
        if (maxLen > 0 && v.length() > maxLen) {
            v = v.substring(0, maxLen);
        }
        out.write(v.getBytes(charSet));
    }

    public void newLine() throws IOException {
        out.write(newLine.getBytes(charSet));
        start = true;
    }

    /**
     * @throws IOException
     */
    private void checkStart() throws IOException {
        if (start) {
            start = false;
        } else {
        	out.write(delimeter.getBytes(charSet));
        }
    }

    public void close() throws IOException {
    	out.close();
    }

    public void setEnableQuote(boolean enableQuote) {
        this.enableQuote = enableQuote;
    }

    public void setForceQuote(boolean forceQuote) {
        this.forceQuote = forceQuote;
    }

	public String getCharSet() {
		return charSet;
	}

	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}
}
