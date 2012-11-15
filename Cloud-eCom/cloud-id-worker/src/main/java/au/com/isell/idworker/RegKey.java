package au.com.isell.idworker;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;

@ISellIndex(name="idworker", type="regkey-gen")
public class RegKey {
	private String type;
	private int serialNo;
	private String regKey;
	@ISellIndexValue
	public int getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(int serialNo) {
		this.serialNo = serialNo;
	}
	@ISellIndexValue
	public String getRegKey() {
		return regKey;
	}
	public void setRegKey(String regKey) {
		this.regKey = regKey;
	}
	public void setType(String type) {
		this.type = type;
	}
	@ISellIndexValue
	public String getType() {
		return type;
	}
	@ISellIndexKey
	public String getKey() {
		return type+":"+regKey;
	}
}
