package au.com.isell.idworker;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;

@ISellIndex(name="idworker", type="id-gen")
public class IndexId {
	private int lastNumber;
	private String type;
	@ISellIndexValue
	public int getLastNumber() {
		return lastNumber;
	}
	public void setLastNumber(int lastNumber) {
		this.lastNumber = lastNumber;
	}
	public void setType(String type) {
		this.type = type;
	}
	@ISellIndexKey
	public String getType() {
		return type;
	}
}
