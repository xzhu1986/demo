package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;

public class PendingDataFile {
	private String supplierId;
	private String supplierName;
	private String filename;
	private String url;
	private Date createDate;
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}
	public String getSupplierId() {
		return supplierId;
	}
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	public String getSupplierName() {
		return supplierName;
	}
}
