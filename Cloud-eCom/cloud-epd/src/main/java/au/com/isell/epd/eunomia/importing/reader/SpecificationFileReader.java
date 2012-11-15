package au.com.isell.epd.eunomia.importing.reader;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class SpecificationFileReader extends EPDFileReader {
	
	public static final String KEY_OVERVIEW = "overview";
	public static final String KEY_LEVEL = "level";
	public static final String KEY_GROUP = "group";
	public static final String KEY_VALUE = "value";
	public static final String KEY_NAME = "name";

	public SpecificationFileReader(Reader in, String fileName) {
		super(in, fileName);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String>[] mapLine(String[] line) {
		Map<String, String> specMap = new HashMap<String, String>();
		for (int i = 0; i < line.length; i++) {
			switch (i) {
			case 0: specMap.put(EPDFileReader.KEY_OR_ID, line[i]); break;
			case 1: specMap.put(SpecificationFileReader.KEY_NAME, line[i]); break;
			case 2: specMap.put(SpecificationFileReader.KEY_VALUE, line[i]); break;
			case 3: specMap.put(SpecificationFileReader.KEY_GROUP, line[i]); break;
			case 4: specMap.put(EPDFileReader.KEY_SEQUENCE, line[i]); break;
			case 5: specMap.put(SpecificationFileReader.KEY_LEVEL, line[i]); break;
			case 6: specMap.put(SpecificationFileReader.KEY_OVERVIEW, line[i]); break;
			}
		}
		specMap.put(EPDFileReader.KEY_TYPE, "spec");
		applyFileName(specMap);		
		return new Map[]{specMap};
	}

}
