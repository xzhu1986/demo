package au.com.isell.epd.eunomia.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

import au.com.isell.remote.common.model.Pair;

public class UnitType {
	private Integer typeId;
	private String typeName;
	private String description;
	private Unit baseUnit;
	private Date lastUpd;

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Date getLastUpd() {
		return lastUpd;
	}

	public void setLastUpd(Date lastUpd) {
		this.lastUpd = lastUpd;
	}

	public void setBaseUnit(Unit baseUnit) {
		this.baseUnit = baseUnit;
	}

	public Unit getBaseUnit() {
		return baseUnit;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getItemId() {
		return String.valueOf(typeId);
	}

	public String getItemName() {
		return typeName;
	}

	public String getItemDescription() {
		return description;
	}

	public static Pair<String, String> toCloseText(BigDecimal currentUnitBaseValue, Unit[] unitArr) {
		if (currentUnitBaseValue == null)
			return new Pair<String, String>("", "");
		String finalValue = "";
		String valueUnit = "";
		String tabString = "";
		BigDecimal textValue = BigDecimal.ZERO;
		DecimalFormat format = new DecimalFormat("################.###");
		for (int i = 0; i < unitArr.length; i++) {
			Unit cuUnit = unitArr[i];
			BigDecimal unitValue = cuUnit.convertFromBaseUnit(currentUnitBaseValue);
			if (i == unitArr.length - 1) {
				if (unitValue.compareTo(BigDecimal.ONE) >= 0) {
					textValue = unitArr[i].convertFromBaseUnit(currentUnitBaseValue);
					valueUnit = unitArr[i].getText();
					tabString = Character.isLetter(valueUnit.charAt(0))?" ":"";
					finalValue = format.format(textValue.setScale(3, RoundingMode.HALF_UP)) + tabString+ valueUnit;
					break;
				} else {
					textValue = unitArr[(i - 1) < 0 ? 0 : (i - 1)].convertFromBaseUnit(currentUnitBaseValue);
					valueUnit = unitArr[(i - 1) < 0 ? 0 : (i - 1)].getText();
					tabString = Character.isLetter(valueUnit.charAt(0))?" ":"";
					finalValue = format.format(textValue.setScale(3, RoundingMode.HALF_UP)) +tabString+ valueUnit;
					break;
				}
			} else {
				if (unitValue.compareTo(BigDecimal.ONE) < 0) {
					textValue = unitArr[(i - 1) < 0 ? 0 : (i - 1)].convertFromBaseUnit(currentUnitBaseValue);
					valueUnit = unitArr[(i - 1) < 0 ? 0 : (i - 1)].getText();
					tabString = Character.isLetter(valueUnit.charAt(0))?" ":"";
					finalValue = format.format(textValue.setScale(3, RoundingMode.HALF_UP)) +tabString + valueUnit;
					break;
				}
			}
		}
		return new Pair<String, String>(valueUnit, finalValue);
	}
}