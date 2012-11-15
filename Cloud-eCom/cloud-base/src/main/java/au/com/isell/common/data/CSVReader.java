package au.com.isell.common.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import au.com.isell.common.model.Tuple;
import au.com.isell.common.model.TupleType;

public class CSVReader {
	public static final char DEFAULT_DELIMITER = ',';
    public static final char DEFAULT_QUOTE = '\"';
	
	private BufferedReader reader;
	
	private String delimiter;
    private String quote;
    private boolean hasHeader = false;
    private boolean useQuote = false;
    private int lineNo = 0;
    private String[] header = null;
    private boolean closed=false;
    private static TupleType type = TupleType.DefaultFactory.create(Integer.class, String[].class);
	
	public CSVReader(Reader in, char delim, char quote, boolean hasHeader, boolean useQuote) {
		if (in instanceof BufferedReader) reader = (BufferedReader) in;
		else reader = new BufferedReader(in);
		delimiter = new String(new char[]{delim});
		this.quote = new String(new char[]{quote});
		this.hasHeader = hasHeader;
		this.useQuote = useQuote;
	}
	
	public String[] readLine() throws IOException {
		if (hasHeader && lineNo == 0) {
			Tuple line = innerReadLine();
			if (line != null) header = line.getNthValue(1);
		}
		Tuple line = innerReadLine();
		if (line == null) return null;
		return line.getNthValue(1);
	}
	
	public Tuple readLineWithLineNo() throws IOException {
		if (hasHeader && lineNo == 0) {
			
			Tuple line = innerReadLine();
			header = line.getNthValue(1);
		}
		return innerReadLine();
	}
	
	public String[] getHeader() throws IOException {
		if (!hasHeader) return null;
		if (header == null) header = innerReadLine().getNthValue(1);
		return header;
	}
	
	public void close() throws IOException {
		reader.close();
		closed = true;
	}
	
	public boolean isClosed() throws IOException {
		return closed;
	}

	private synchronized Tuple innerReadLine() throws IOException {
		if (closed) return null;
		if (useQuote) {
			ArrayList<String> list = new ArrayList<String>();
			boolean inQuote = false;
		    boolean pendingQuote = false;
			StringBuilder sb = new StringBuilder();
			while (true) {
				String l = reader.readLine();
				if (l == null) return null;
			    StringTokenizer tokenizer = new StringTokenizer(l, delimiter+quote, true);
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.equals(delimiter)) {
						if (inQuote && !pendingQuote) {
							sb.append(token);
						} else {
							list.add(sb.toString());
							sb = new StringBuilder();
							pendingQuote = false;
							inQuote = false;
						}
					} else if (token.equals(quote)) {
						if (inQuote) {
							if (pendingQuote) {
								sb.append(token);
								pendingQuote = false;
							}
							else pendingQuote = true;
						} else if (sb.length() == 0) {
							inQuote = true;
						} else {
							sb.append(token);
						}
					} else {
						if (pendingQuote) {
							pendingQuote = false;
							sb.append(quote);
						}
						sb.append(token);
					}
				}
				if (inQuote && !pendingQuote) {
					sb.append("\r\n");
				} else {
					list.add(sb.toString());
					lineNo++;
					pendingQuote = false;
					inQuote = false;
					return type.createTuple(lineNo, list.toArray(new String[list.size()]));
				}
			}
		} else {
			String l = reader.readLine();
			if (l == null) return null;
			lineNo++;
			StringTokenizer tokenizer = new StringTokenizer(l, delimiter, true);
			List<String> line = new ArrayList<String>();
			boolean beforeDelimiter = false;
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.equals(new String(delimiter))) {
					if (beforeDelimiter) line.add("");
					beforeDelimiter=true;
				} else {
					beforeDelimiter=false;
					line.add(token);
				}
			}
			if (beforeDelimiter) line.add("");
			return type.createTuple(lineNo, line.toArray(new String[line.size()]));
		}
	}

	public int getLineNo() {
		return lineNo;
	}

}
