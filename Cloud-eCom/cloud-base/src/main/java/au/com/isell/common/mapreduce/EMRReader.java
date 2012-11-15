package au.com.isell.common.mapreduce;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import au.com.isell.common.data.CSVReader;
import au.com.isell.remote.common.model.Pair;

public class EMRReader extends CSVReader {

	public EMRReader(Reader in) {
		super(in, '\t', '"', false, false);
	}

	public Pair<String, Map<String, String>> readRecord() throws IOException {
		String[] line = readLine();
		if (line == null) return null;
		String key = line[0];
		Map<String, String> content = new HashMap<String, String>();
		for (int i=1; i < line.length; i+=2) {
			content.put(line[i], line[i+1]);
		}
		return new Pair<String, Map<String, String>>(key, content);
	}
}
