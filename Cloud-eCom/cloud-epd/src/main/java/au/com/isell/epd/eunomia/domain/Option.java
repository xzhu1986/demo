package au.com.isell.epd.eunomia.domain;

import java.util.UUID;

public class Option {
	private String vendor;
	private String vendorPart;
	private UUID prodId;   // for export only
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getVendorPart() {
		return vendorPart;
	}
	public void setVendorPart(String vendorPart) {
		this.vendorPart = vendorPart;
	}
	public UUID getProdId() {
		return prodId;
	}
	public void setProdId(UUID prodId) {
		this.prodId = prodId;
	}
}
