package au.com.isell.epd.eunomia.domain;

import java.util.Date;

public class UnspscCatMap {
	private Integer unspsc;
	private String unspscSegment;
	private String unspscFamily;
	private String unspscClass;
	private String unspscCommodity;
	private String friendlyName;
	private String fnamecategory;
	
	private Date lastUpd;
	
	private boolean checked;
	
	public Integer getUnspsc() {
		return unspsc;
	}
	public void setUnspsc(Integer unspsc) {
		this.unspsc = unspsc;
	}
	public String getUnspscSegment() {
		return unspscSegment;
	}
	public void setUnspscSegment(String unspscSegment) {
		this.unspscSegment = unspscSegment;
	}
	public String getUnspscFamily() {
		return unspscFamily;
	}
	public void setUnspscFamily(String unspscFamily) {
		this.unspscFamily = unspscFamily;
	}
	public String getUnspscClass() {
		return unspscClass;
	}
	public void setUnspscClass(String unspscClass) {
		this.unspscClass = unspscClass;
	}
	public String getUnspscCommodity() {
		return unspscCommodity;
	}
	public void setUnspscCommodity(String unspscCommodity) {
		this.unspscCommodity = unspscCommodity;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public Date getLastUpd() {
		return lastUpd;
	}
	public void setLastUpd(Date lastUpd) {
		this.lastUpd = lastUpd;
	}
	public String getFnamecategory() {
		return fnamecategory;
	}
	public void setFnamecategory(String fnamecategory) {
		this.fnamecategory = fnamecategory;
	}
	
}
