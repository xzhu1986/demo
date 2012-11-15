package au.com.isell.epd.eunomia.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AttributeValue {
	private int attrId;
	private UUID prodId;
	private String text;
	private BigDecimal baseValue;
	private BigDecimal value;
	private String unit;
	private int status=Status.STATUS_PREPARING;
	private Date lastUpd;
	
	private List<String> units;
	private int count;
	private String display;
	
	
	public List<String> getUnits() {
		return units;
	}
	public void setUnits(List<String> units) {
		this.units = units;
	}
	
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
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
	public void setAttrId(int attrId) {
		this.attrId = attrId;
	}
	public int getAttrId() {
		return attrId;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("attrId:").append(attrId).append(',');
		sb.append("prodId:").append(prodId).append(',');
		sb.append("text:").append(text).append(',');
		sb.append("value:").append(value).append(',');
		sb.append("unit:").append(unit).append(',');
		sb.append("baseValue:").append(baseValue).append(',');
		sb.append("status:").append(status);
		return sb.toString();
	}
}
