package au.com.isell.epd.eunomia.domain;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Product {
	private UUID prodId;
	private String vendorPart;
	private String vendor;
	private int unspsc;
	private String vendorFamily;
	private String vendorFamilyMember;
	private String name;
	private String briefDesc;
	private String marketing;
	private String marketing2;
	private Date createDate;
	private Date updateDate;
	private int status=Status.STATUS_PREPARING;
	private List<Image> images;

	public UUID getProdId() {
		return prodId;
	}
	public void setProdId(UUID prodId) {
		this.prodId = prodId;
	}
	public String getVendorPart() {
		return vendorPart;
	}
	public void setVendorPart(String partNo) {
		this.vendorPart = partNo;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public int getUnspsc() {
		return unspsc;
	}
	public void setUnspsc(int unspsc) {
		this.unspsc = unspsc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBriefDesc() {
		return briefDesc;
	}
	public void setBriefDesc(String breifDesc) {
		this.briefDesc = breifDesc;
	}
	public String getMarketing() {
		return marketing;
	}
	public void setMarketing(String marketing) {
		this.marketing = marketing;
	}
	public String getMarketing2() {
		return marketing2;
	}
	public void setMarketing2(String marketingAdd) {
		this.marketing2 = marketingAdd;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createdDate) {
		this.createDate = createdDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updatedDate) {
		this.updateDate = updatedDate;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void setVendorFamilyMember(String vendorFamilyMember) {
		this.vendorFamilyMember = vendorFamilyMember;
	}
	public String getVendorFamilyMember() {
		return vendorFamilyMember;
	}
	public void setVendorFamily(String vendorFamily) {
		this.vendorFamily = vendorFamily;
	}
	public String getVendorFamily() {
		return vendorFamily;
	}
	public void setImages(List<Image> images) {
		this.images = images;
	}
	public List<Image> getImages() {
		return images;
	}

}
