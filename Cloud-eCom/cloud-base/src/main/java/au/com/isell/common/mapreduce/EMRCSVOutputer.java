package au.com.isell.common.mapreduce;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import au.com.isell.common.data.DelimiterWriter;

public class EMRCSVOutputer extends DelimiterWriter implements EMROutputer {

	public EMRCSVOutputer(OutputStream writer) {
		super(writer, '\t', "\n");
	}
	
	protected String formatCol(String value, boolean enableQuote,
            boolean forceQuote, String delimeter, String newLine) {
        String v = value;
        if (value == null) {
            v = "";
        } else {
        	v = v.replace("\r\n", "\\n").replace("\n", "\\n");
            if (forceQuote
                    || (enableQuote && (value.contains(DEFAULT_QUOTE)
                            || value.contains(delimeter)))) {
                v = DEFAULT_QUOTE
                        + value.replace(DEFAULT_QUOTE, DEFAULT_QUOTE
                                + DEFAULT_QUOTE) + DEFAULT_QUOTE;
            }
        }
        return v;
    }
	
	@Override
	public void writeRecord(String key, Map<String, String> content) throws IOException {
		write(key);
		for (Map.Entry<String, String> entry : content.entrySet()) {
			write(entry.getKey());
			write(entry.getValue());
		}
		newLine();
	}

	@Override
	public void writeMessage(String key, String message) throws IOException {
		writeLine(key, message);
	}

}
