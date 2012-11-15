package au.com.isell.epd.eunomia.importing.reader;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class FeatureFileReader extends EPDFileReader {
	
	public static final String KEY_TEXT = "description";
	public FeatureFileReader(Reader in, String fileName) {
		super(in, fileName);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String>[] mapLine(String[] line) {
		Map<String, String> prodMap = new HashMap<String, String>();
		prodMap.put(KEY_OR_ID, line[0]);
		prodMap.put(FeatureFileReader.KEY_TEXT, line[2]+'|'+line[1]);
		prodMap.put(EPDFileReader.KEY_TYPE, "prod");
		applyFileName(prodMap);
		return new Map[]{prodMap};
	}

}
