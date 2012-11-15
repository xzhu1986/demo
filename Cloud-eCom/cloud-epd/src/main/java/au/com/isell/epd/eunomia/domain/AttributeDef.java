package au.com.isell.epd.eunomia.domain;

import java.util.Date;

public class AttributeDef {
	private long attrId;
	private int unspsc;
	private ValueType valueType;
	private UnitType unitType;
	private String tempUnit;
	private String name;
	private int status=Status.STATUS_PREPARING;
	private boolean imported;
	private Date lastUpd;

	public long getAttrId() {
		return attrId;
	}
	public void setAttrId(long attrId) {
		this.attrId = attrId;
	}
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(ValueType type) {
		this.valueType = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public UnitType getUnitType() {
		return unitType;
	}
	public void setUnitType(UnitType unitType) {
		this.unitType = unitType;
	}
	public int getUnspsc() {
		return unspsc;
	}
	public void setUnspsc(int unspsc) {
		this.unspsc = unspsc;
	}
	public String getTempUnit() {
		return tempUnit;
	}
	public void setTempUnit(String tempUnit) {
		this.tempUnit = tempUnit;
	}
	public void setImported(boolean imported) {
		this.imported = imported;
	}
	public boolean isImported() {
		return imported;
	}
}
