package au.com.isell.epd.eunomia.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class SearchableVal {
	private long searchableId;
	private UUID prodId;
	private String text;
	private BigDecimal baseValue;
	private String baseUnit;
	/**
	 * For store value with unknown units from source. Once value has been confirmed it will be clear as null.
	 */
	private BigDecimal value;
	/**
	 * For store unknown units from source data.. Once value has been confirmed it will be clear as null.
	 */
	private String unit;
	private ValueType tempValueType;
	private int status=Status.STATUS_PREPARING;
	private Date lastUpd;
	private String newText;
	private int count;

	public UUID getProdId() {
		return prodId;
	}
	public void setProdId(UUID prodId) {
		this.prodId = prodId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public BigDecimal getBaseValue() {
		return baseValue;
	}
	public void setBaseValue(BigDecimal baseValue) {
		this.baseValue = baseValue;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
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
	public void setTempValueType(ValueType tempValueType) {
		this.tempValueType = tempValueType;
	}
	public ValueType getTempValueType() {
		return tempValueType;
	}
	public void setBaseUnit(String baseUnit) {
		this.baseUnit = baseUnit;
	}
	public String getBaseUnit() {
		return baseUnit;
	}
	public long getSearchableId() {
		return searchableId;
	}
	public void setSearchableId(long searchableId) {
		this.searchableId = searchableId;
	}
	public String getNewText() {
		return newText;
	}
	public void setNewText(String newText) {
		this.newText = newText;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
