package au.com.isell.epd.eunomia.domain;

import java.util.Date;
import java.util.UUID;

public class SpecVal {
	private int specId;
	private UUID prodId;
	private String value;
	private int status=Status.STATUS_PREPARING;
	private Date lastUpd;

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getLastUpd() {
		return lastUpd;
	}
	public void setLastUpd(Date lastUpd) {
		this.lastUpd = lastUpd;
	}
	public UUID getProdId() {
		return prodId;
	}
	public void setProdId(UUID prodId) {
		this.prodId = prodId;
	}
	public int getSpecId() {
		return specId;
	}
	public void setSpecId(int defId) {
		this.specId = defId;
	}
}
