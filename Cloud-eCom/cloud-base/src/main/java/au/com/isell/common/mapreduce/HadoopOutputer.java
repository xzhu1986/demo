package au.com.isell.common.mapreduce;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;

public class HadoopOutputer implements EMROutputer {
    public static final String DEFAULT_QUOTE = "\"";
    public static final String DEFAULT_DELIMITER = "\t";
	private OutputCollector<Text, Text> output;
	private Text txtKey = new Text();
	private Text txtContent = new Text();

	private String formatCol(String value) {
        String v = value;
        if (value == null) {
            v = "";
        } else {
        	v = v.replace("\r\n", "\\n").replace("\n", "\\n");
            if (value.contains(DEFAULT_QUOTE)) {
                v = DEFAULT_QUOTE
                        + value.replace(DEFAULT_QUOTE, DEFAULT_QUOTE
                                + DEFAULT_QUOTE) + DEFAULT_QUOTE;
            }
        }
        return v;
    }
	
	@Override
	public void writeRecord(String key, Map<String, String> content)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : content.entrySet()) {
			if (first) first = false;
			else sb.append(DEFAULT_DELIMITER);
			sb.append(formatCol(entry.getKey())).append(DEFAULT_DELIMITER).append(formatCol(entry.getValue()));
		}
		txtKey.set(key);
		txtContent.set(sb.toString());
		output.collect(txtKey, txtContent);
	}

	@Override
	public void writeMessage(String key, String message) throws IOException {
		txtKey.set(key);
		txtContent.set(message == null ? "" : message);
		output.collect(txtKey, txtContent);
	}

	@Override
	public void close() throws IOException {
	}
	
	public void setOutputCollector(OutputCollector<Text, Text> output) {
		this.output = output;
	}

}
