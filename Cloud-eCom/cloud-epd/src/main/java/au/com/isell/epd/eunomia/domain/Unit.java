package au.com.isell.epd.eunomia.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * the calculation rule which can convert an value in base unit to this unit.
 * Algorithm:
 * 
 * Output = ROUND ( ( ( SV + PRECA) / CF / 10^SCALE) + POSTCA, PREC)
 * 
 * Where
 * 
 * SV = Source Value (Integer) PRECA = Pre Conversion Adjustment (Signed
 * Decimal) SCALE = Power of 10 Scale (Signed Integer) CF = Conversion Factor
 * Multiplier (Signed Decimal) POSTCA = Post Conversion Adjustment (Signed
 * Decimal) PREC = Precision (Integer) is used to round the output value to PREC
 * decimal places
 * 
 * @author Bruce Zhou
 * 
 */
public class Unit {
	private int id = -1;
	private String name;
	private String text;
	private BigDecimal preca = new BigDecimal(0);
	private BigDecimal cf = new BigDecimal(1);
	private int scale = 0;
	private BigDecimal postca = new BigDecimal(0);
	private int prec = 2;
	private int unitTypeId;
	private int status = Status.STATUS_PREPARING;
	private Date lastUpd;
	private String[] displayAlias;
	private String showDisplayAlias;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getPreca() {
		return preca.intValue();
	}

	public void setPreca(int preca) {
		this.preca = new BigDecimal(preca);
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public BigDecimal getPostca() {
		return postca;
	}

	public void setPostca(BigDecimal postca) {
		this.postca = postca;
	}

	public int getPrec() {
		return prec;
	}

	public void setPrec(int prec) {
		this.prec = prec;
	}

	public BigDecimal convertFromBaseUnit(BigDecimal baseValue) {
		if (baseValue == null) return null;
		return baseValue.add(preca).divide(cf,prec+1,RoundingMode.HALF_UP).divide(new BigDecimal(10).pow(scale),prec+1,RoundingMode.HALF_UP).add(postca).setScale(prec, RoundingMode.HALF_UP);
	}

	public BigDecimal convertToBaseValue(BigDecimal value) {
		if (value == null) return null;
		return value.subtract(postca).multiply(new BigDecimal(10).pow(scale)).multiply(cf).subtract(preca);
	}


	public BigDecimal getCf() {
		return cf;
	}

	public void setCf(BigDecimal cf) {
		this.cf = cf;
	}

	public int getUnitTypeId() {
		return unitTypeId;
	}

	public void setUnitTypeId(int unitTypeId) {
		this.unitTypeId = unitTypeId;
	}

	public Date getLastUpd() {
		return lastUpd;
	}

	public void setLastUpd(Date lastUpd) {
		this.lastUpd = lastUpd;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
	public String[] getDisplayAlias() {
		return displayAlias;
	}
	public void setDisplayAlias(String[] displayAlias) {
		if(displayAlias!=null){
			this.displayAlias = new String[displayAlias.length];
			for (int i = 0; i < displayAlias.length; i++) {
				this.displayAlias[i]=displayAlias[i].toLowerCase();
			}
		}else{
			this.displayAlias = displayAlias;
		}
	}

	public String getShowDisplayAlias() {
		if (this.displayAlias == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.displayAlias.length; i++) {
			sb.append(this.displayAlias[i]);
			if (i != this.displayAlias.length - 1) {
				sb.append(",");
			}
		}
		this.showDisplayAlias =  sb.toString();
		return this.showDisplayAlias;
	}

	public void setShowDisplayAlias(String showDisplayAlias) {
		this.showDisplayAlias = showDisplayAlias;
	}
}
