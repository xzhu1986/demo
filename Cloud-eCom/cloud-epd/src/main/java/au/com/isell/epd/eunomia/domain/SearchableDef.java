package au.com.isell.epd.eunomia.domain;

import java.util.Date;

public class SearchableDef {
	private long searchableId;
	private int unspsc;
	private String groupName;
	private String name;
	private UnitType unitType;
	private ValueType valueType;
	private String tempUnit;
	private int sequence;
	private int status=Status.STATUS_PREPARING;
	private boolean imported;
	private int attrId;
	private Date lastUpd;

	public long getSearchableId() {
		return searchableId;
	}
	public void setSearchableId(long searchableId) {
		this.searchableId = searchableId;
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
	public void setUnitType(UnitType unitType) {
		this.unitType = unitType;
	}
	public UnitType getUnitType() {
		return unitType;
	}
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	public void setTempUnit(String tempUnit) {
		this.tempUnit = tempUnit;
	}
	public String getTempUnit() {
		return tempUnit;
	}
	public void setImported(boolean imported) {
		this.imported = imported;
	}
	public boolean isImported() {
		return imported;
	}
	public int getAttrId() {
		return attrId;
	}
	public void setAttrId(int attrId) {
		this.attrId = attrId;
	}
	
}
