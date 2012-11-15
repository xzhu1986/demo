package au.com.isell.rlm.importing.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Constants {
	public static final int MAX_OBJECTS_PER_ADD = 200;
	
	public static final Map<String,String> CountryMap;
	public static final Map<String,Boolean> BooleanMap;
	
	static{
		Map<String,String> map = new HashMap<String,String>();
		map.put("61", "au");
		map.put("64", "nz");//region nonrth
		map.put("01", "us");
		map.put("1", "us");
		map.put("44", "gb");
		map.put("353", "ie");
		CountryMap = Collections.unmodifiableMap(map);
		
		Map<String,Boolean> booleanMap = new HashMap<String,Boolean>();
		booleanMap.put("1", Boolean.TRUE);
		booleanMap.put("0",Boolean.FALSE);
		BooleanMap = Collections.unmodifiableMap(booleanMap);
	}
}
