package au.com.isell.epd.pojo;

import java.util.UUID;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;

@ISellIndex(name="prod_ids", type="ids")
public class ProductIdMapping {

	private UUID productUUID;
	private int cnID;
	private int ukID;
	private int unspsc;
	private String manufacturer;
	private String partNo;
	
	@ISellIndexKey
	public UUID getProductUUID() {
		return productUUID;
	}
	public void setProductUUID(UUID productUUID) {
		this.productUUID = productUUID;
	}
	@ISellIndexValue
	public int getCnID() {
		return cnID;
	}
	public void setCnID(int productID) {
		this.cnID = productID;
	}
	@ISellIndexValue
	public int getUnspsc() {
		return unspsc;
	}
	public void setUnspsc(int unspsc) {
		this.unspsc = unspsc;
	}
	@ISellIndexValue(index="no")
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	@ISellIndexValue(index="no")
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	public void setUkID(int ukID) {
		this.ukID = ukID;
	}
	@ISellIndexValue
	public int getUkID() {
		return ukID;
	}
	@ISellIndexValue(lowercase=true, index="not_analyzed")
	public String getPartNoKey() {
		return manufacturer+"|"+partNo;
	}
}
