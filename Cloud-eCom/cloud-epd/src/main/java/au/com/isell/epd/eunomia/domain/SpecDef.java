package au.com.isell.epd.eunomia.domain;

import java.util.Date;

public class SpecDef {
	private long specId;
	private int unspsc;
	private String groupName;
	private String name;
	private int sequence;
	private int level;
	private int overview;
	private int status=Status.STATUS_PREPARING;
	private boolean imported;
	private Date lastUpd;
	public long getSpecId() {
		return specId;
	}
	public void setSpecId(long specId) {
		this.specId = specId;
	}
	public int getUnspsc() {
		return unspsc;
	}
	public void setUnspsc(int unspsc) {
		this.unspsc = unspsc;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getOverview() {
		return overview;
	}
	public void setOverview(int overview) {
		this.overview = overview;
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
	public void setImported(boolean imported) {
		this.imported = imported;
	}
	public boolean isImported() {
		return imported;
	}
}
