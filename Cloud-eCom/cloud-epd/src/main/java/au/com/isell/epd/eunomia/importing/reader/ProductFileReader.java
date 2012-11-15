package au.com.isell.epd.eunomia.importing.reader;

import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProductFileReader extends EPDFileReader {

	public static final String KEY_CHANGE_DATE = "change_date";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_MODEL = "model";
	public static final String KEY_UNSPSC_SEGMENT = "unspsc_segment";
	public static final String KEY_UNSPSC_FAMILY = "unspsc_family";
	public static final String KEY_UNSPSC_CLASS = "unspsc_class";
	public static final String KEY_UNSPSC_COMMODITY = "unspsc_commodity";
	public static final String KEY_VENDOR_TIER1 = "vendor_tier1";
	public static final String KEY_VENDOR_TIER2 = "vendor_tier2";

	private static final SimpleDateFormat SDF_FROM = new SimpleDateFormat("MMM d yyyy h:mma");
	private static final SimpleDateFormat SDF_TO = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public ProductFileReader(Reader in, String fileName) {
		super(in, fileName);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String>[] mapLine(String[] line) {
		Map<String, String> prodMap = new HashMap<String, String>();
		Map<String, String> unspscMap = new HashMap<String, String>();
		for (int i = 0; i < line.length; i++) {
			switch (i) {
			case 0: prodMap.put(EPDFileReader.KEY_OR_ID, line[i]); break;
			case 1: prodMap.put(EPDFileReader.KEY_VENDOR, line[i]); break;
			case 2: prodMap.put(EPDFileReader.KEY_VENDOR_PART, line[i]); break;
			case 3: prodMap.put(ProductFileReader.KEY_MODEL, line[i]); break;
			case 4: prodMap.put(ProductFileReader.KEY_VENDOR_TIER1, line[i]); break;
			case 5: prodMap.put(ProductFileReader.KEY_VENDOR_TIER2, line[i]); break;
			case 6: unspscMap.put(ProductFileReader.KEY_UNSPSC_SEGMENT, line[i]); break;
			case 7: unspscMap.put(ProductFileReader.KEY_UNSPSC_FAMILY, line[i]); break;
			case 8: unspscMap.put(ProductFileReader.KEY_UNSPSC_CLASS, line[i]); break;
			case 9: unspscMap.put(ProductFileReader.KEY_UNSPSC_COMMODITY, line[i]); break;
			case 10: {
				prodMap.put(EPDFileReader.KEY_UNSPSC, line[i]);
				unspscMap.put(EPDFileReader.KEY_UNSPSC, line[i]); 
				break;
			}
			case 11: prodMap.put(ProductFileReader.KEY_IMAGE, "epd/"+getFileSource()+'/'+prodMap.get(EPDFileReader.KEY_VENDOR)+'/'+line[i]); break;
			case 12: {
				try {
					prodMap.put(ProductFileReader.KEY_CHANGE_DATE, SDF_TO.format(SDF_FROM.parse(line[i])));
				} catch (ParseException e) {
					prodMap.put(ProductFileReader.KEY_CHANGE_DATE, SDF_TO.format(new Date()));
				}
				break;
			}
			case 13: prodMap.put(EPDFileReader.KEY_EOL, line[i].equalsIgnoreCase("current") ? "0" : "1"); break;
			}
		}
		prodMap.put(EPDFileReader.KEY_TYPE, "prod");
		applyFileName(prodMap);
		unspscMap.put(EPDFileReader.KEY_TYPE, "unspsc");
		applyFileName(unspscMap);
		return new Map[]{prodMap, unspscMap};
	}

}
