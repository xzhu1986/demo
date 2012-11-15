package au.com.isell.rlm.module.user.constant;

import au.com.isell.rlm.common.constant.EntryPath;

/**
 * login user type
 * 
 * @author frankw 20/01/2012
 */
public enum UserType {
	Reseller(EntryPath.RESELLER), Agent(EntryPath.AGENT), Supplier(EntryPath.SUPPLIER);

	private String entryPath;
	
	private UserType(String entryPath) {
		this.entryPath = entryPath;
	}

	public String getEntryPath() {
		return entryPath;
	}
	
	
}
