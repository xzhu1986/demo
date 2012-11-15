package au.com.isell.common.filter;

import java.util.Hashtable;
import java.util.Map;

public class FieldMapper {
	private Map<String, String> mapper = new Hashtable<String, String>();
	public void addMap(String displayName, String queryName) {
		if (queryName == null) return;
		mapper.put(displayName, queryName);
	}
	
	public String getQueryName(String displayName) {
		String queryName = mapper.get(displayName);
		queryName = queryName == null ? displayName : queryName;
		return queryName;
	}
}