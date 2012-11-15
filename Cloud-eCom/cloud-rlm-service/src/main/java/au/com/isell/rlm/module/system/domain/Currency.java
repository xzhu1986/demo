package au.com.isell.rlm.module.system.domain;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@ISellIndex(name = "location", type = "currency")
@IsellPath("data/location/currency/${code}.info.json")
@XStreamAlias("currency")
public class Currency extends AbstractModel {
	private String code;
	private String name;
	private String symbol;
	private int minorUnit=2;
	
	public Currency() {}
	public Currency(String code, String symble, String name) {
		setCode(code);
		setSymbol(symble);
		setName(name);
	}
	
	@ISellIndexKey
	@IsellPathField
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@ISellIndexValue(wildcard=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ISellIndexValue(index="no")
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	@ISellIndexValue
	public void setMinorUnit(int minorUnit) {
		this.minorUnit = minorUnit;
	}
	public int getMinorUnit() {
		return minorUnit;
	}
}
