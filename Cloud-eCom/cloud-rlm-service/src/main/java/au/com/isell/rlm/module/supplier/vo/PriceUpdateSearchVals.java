package au.com.isell.rlm.module.supplier.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class PriceUpdateSearchVals {
	private String supplierName;
	private String priority;
	private String dataSkillLevelRequired;
	private String userName;
	
	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker,"supplierName", supplierName,makers);
		addIntRangeQueryItem(filterMaker,"priority", priority,makers);
		addWildcardQueryItem(filterMaker,"userName", userName,makers);
		addIntRangeQueryItem(filterMaker,"dataSkillLevelRequired", dataSkillLevelRequired,makers);
		if(makers.size()>0) return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		return null;
	}

	private void addISQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			items.add(builder.makeNameFilter(name, TextMatchOption.Is, value.trim()));
		}
	}
	private void addWildcardQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			items.add(builder.makeNameFilter(name, TextMatchOption.Contains, value.trim()));
		}
	}
	private void addIntRangeQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value) && StringUtils.isNumeric(value)){
			items.add(builder.makeIntRange(name,Integer.valueOf(value), null, true, false));
		}
	}
	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getDataSkillLevelRequired() {
		return dataSkillLevelRequired;
	}

	public void setDataSkillLevelRequired(String dataSkillLevelRequired) {
		this.dataSkillLevelRequired = dataSkillLevelRequired;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isEmpty(){
		return StringUtils.isEmpty(supplierName)&&
				StringUtils.isEmpty(priority)&&
				StringUtils.isEmpty(dataSkillLevelRequired)&&
				StringUtils.isEmpty(userName);
	}


}
