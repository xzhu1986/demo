package au.com.isell.epd.eunomia.importing.reader;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class AttributeFileReader extends EPDFileReader {
	
	public static final String KEY_MARKETING2 = "marketing2";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_MARKETING = "marketing";

	public AttributeFileReader(Reader in, String fileName) {
		super(in, fileName);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String>[] mapLine(String[] line) {
		Map<String, String> attrMap = new HashMap<String, String>();
		Map<String, String> prodMap = new HashMap<String, String>();
		if (line[1].equalsIgnoreCase(AttributeFileReader.KEY_IMAGE)) {
			prodMap.put(EPDFileReader.KEY_OR_ID, line[0]);
			prodMap.put(AttributeFileReader.KEY_IMAGE, line[2]);
		} else if (line[1].equalsIgnoreCase(AttributeFileReader.KEY_MARKETING)) {
			prodMap.put(EPDFileReader.KEY_OR_ID, line[0]);
			prodMap.put(AttributeFileReader.KEY_MARKETING, line[2]);
		} else if (line[1].equalsIgnoreCase("description")) {
			prodMap.put(EPDFileReader.KEY_OR_ID, line[0]);
			prodMap.put(AttributeFileReader.KEY_MARKETING2, line[2]);
		}
		for (int i = 0; i < line.length; i++) {
			switch (i) {
			case 0: attrMap.put(EPDFileReader.KEY_OR_ID, line[i]); break;
			case 1: attrMap.put("name", line[i]); break;
			case 2: attrMap.put("value", line[i]); break;
			case 3: attrMap.put("units", line[i]); break;
			}
		}
		attrMap.put(EPDFileReader.KEY_TYPE, "attr");
		applyFileName(attrMap);		
		if (prodMap.size() == 0) return new Map[]{attrMap};

		prodMap.put(EPDFileReader.KEY_TYPE, "prod");
		applyFileName(prodMap);
		return new Map[]{attrMap, prodMap};
	}

}
