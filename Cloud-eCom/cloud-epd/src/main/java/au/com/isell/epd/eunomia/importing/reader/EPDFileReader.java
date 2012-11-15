package au.com.isell.epd.eunomia.importing.reader;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import au.com.isell.common.data.CSVReader;

public abstract class EPDFileReader extends CSVReader {

	public static final String KEY_FILE_NAME = "file_name";
	public static final String FS_UK = "uk";
	public static final String FS_CN = "cn";
	public static final String KEY_OR_ID = "or_id";
	public static final String KEY_ISELL_ID = "isell_id";
	public static final String KEY_UNSPSC = "unspsc";
	public static final String KEY_VENDOR = "vendor";
	public static final String KEY_VENDOR_PART = "vendorPart";
	public static final String KEY_EOL = "eol";
	public static final String KEY_SEQUENCE = "sequence";
	protected String fileName;
	public static final String KEY_TYPE = "type";

	protected EPDFileReader(Reader in, String fileName) {
		super(in, '|', '"', true, false);
		this.fileName = fileName;
	}
	
	public Map<String, String>[] readRecords() throws IOException {
		String[] line = readLine();
		if (line == null) return null;
		Map<String, String>[] records = mapLine(line);
		return records;
	}

	protected abstract Map<String, String>[] mapLine(String[] line);
	
	protected void applyFileName(Map<String, String> record) {
		record.put(EPDFileReader.KEY_FILE_NAME, fileName);
		String orId = record.get(KEY_OR_ID);
		if (orId != null) {
			record.put(KEY_OR_ID, getFileSource()+"|"+orId);
		}
	}
	
	protected String getFileSource() {
		if (fileName.toLowerCase().contains("/china/")) {
			return EPDFileReader.FS_CN;
		} else {
			return EPDFileReader.FS_UK;
		}
	}

	public static int compareFile(String string, String string2) {
		return 0;
	}

	public static EPDFileReader getInstance(String fileName, Reader in) {
		if (fileName.endsWith("!Products.txt")) return new ProductFileReader(in, fileName);
		else if (fileName.endsWith("!Features.txt")) return new FeatureFileReader(in, fileName);
		else if (fileName.endsWith("!Options.txt")) return new OptionFileReader(in, fileName);
		return null;
	}

}
