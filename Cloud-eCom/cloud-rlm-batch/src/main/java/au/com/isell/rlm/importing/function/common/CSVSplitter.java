package au.com.isell.rlm.importing.function.common;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tap.TapException;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.util.Util;

/**
 * see au.com.isell.common.data.CSVReader  innerReadLine
 */
public class CSVSplitter extends BaseOperation implements Function {
	private String delimiter;
	private String quote;
	private boolean useQuote;
	private boolean strict = true;

	@ConstructorProperties({ "fieldDeclaration", "delimiter", "quote", "useQuote", "strict" })
	public CSVSplitter(Fields fieldDeclaration, String delimiter, String quote, boolean useQuote, boolean strict) {
		super(fieldDeclaration);

		this.delimiter = delimiter;
		this.quote = quote;
		this.useQuote = useQuote;
		this.strict = strict;
	}

	@ConstructorProperties({ "fieldDeclaration", "delimiter", "quote", "useQuote" })
	public CSVSplitter(Fields fieldDeclaration, String delimiter, String quote, boolean useQuote) {
		super(fieldDeclaration);

		this.delimiter = delimiter;
		this.quote = quote;
		this.useQuote = useQuote;
	}

	@Override
	public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
		String value = functionCall.getArguments().getString(0);

		if (value == null)
			value = "";

		Tuple output = new Tuple();

		// TODO: optimize this
		int numValues = fieldDeclaration.isUnknown() ? -1 : fieldDeclaration.size();
		String[] split = null;
		try {
			split = innerReadLine(value, delimiter, quote, useQuote);
		} catch (IOException e) {
		}
		if ((split == null && numValues > 0) || (split.length != numValues)) {
			String message = "did not parse correct number of values from input data, expected: " + numValues + ", got: " + split.length + ":" + Util.join(",", split);
			if (strict)
				throw new TapException(message);
		} else {
			for (int i = 0; i < split.length; i++)
				output.add(split[i]);
			functionCall.getOutputCollector().add(output);
		}
	}

	private String[] innerReadLine(String l, String delimiter, String quote, boolean useQuote) throws IOException {
		if (useQuote) {
			ArrayList<String> list = new ArrayList<String>();
			boolean inQuote = false;
			boolean pendingQuote = false;
			StringBuilder sb = new StringBuilder();
			while (true) {
				if (l == null)
					return null;
				StringTokenizer tokenizer = new StringTokenizer(l, delimiter + quote, true);
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
							} else
								pendingQuote = true;
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
					pendingQuote = false;
					inQuote = false;
					return list.toArray(new String[list.size()]);
				}
			}
		} else {
			if (l == null)
				return null;
			StringTokenizer tokenizer = new StringTokenizer(l, delimiter, true);
			List<String> line = new ArrayList<String>();
			boolean beforeDelimiter = false;
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.equals(new String(delimiter))) {
					if (beforeDelimiter)
						line.add("");
					beforeDelimiter = true;
				} else {
					beforeDelimiter = false;
					line.add(token);
				}
			}
			if (beforeDelimiter)
				line.add("");
			return line.toArray(new String[line.size()]);
		}
	}
}
