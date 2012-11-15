package au.com.isell.idworker.dao;

import au.com.isell.idworker.RegKey;

public interface IDWorkerDao {
	RegKey getRegKey(String type, int serialNo);
	void putRegKey(RegKey regKey);
	boolean isRegKeyExists(String type, String regKey);
	
	int getLastNumber(String type);
	void putLastNumber(String type, int lastNumber);
}
