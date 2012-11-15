package au.com.isell.epd.eunomia.importing.reader;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class OptionFileReader extends EPDFileReader {
	
	public static final String KEY_OPTION_VENDOR_PART = "option_vendor_part";
	public static final String KEY_OPTION_OR_ID = "option_or_id";

	public OptionFileReader(Reader in, String fileName) {
		super(in, fileName);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String>[] mapLine(String[] line) {
		Map<String, String> prodMap = new HashMap<String, String>();
		for (int i = 0; i < line.length; i++) {
			switch (i) {
			case 0: prodMap.put(KEY_OR_ID, line[i]); break;
			case 1: prodMap.put(OptionFileReader.KEY_OPTION_VENDOR_PART, line[i]); break;
			case 2: prodMap.put(OptionFileReader.KEY_OPTION_OR_ID, line[i]); break;
			}
		}
		prodMap.put(EPDFileReader.KEY_TYPE, "option");
		applyFileName(prodMap);
		return new Map[]{prodMap};
	}

}
