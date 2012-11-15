package au.com.isell.rlm.module.reseller.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class ResellerSupplierMapSearchVals {
	private String company;
	private String breakName;
	private String supplierName;
	private String supplierCountry;
	
	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addQueryItem(filterMaker,"supplierName", supplierName,makers);
		addQueryItem(filterMaker,"supplierCountry", supplierCountry,makers);
		addQueryItem(filterMaker,"company", company,makers);
		addQueryItem(filterMaker,"breakName", breakName,makers);
		if(makers.size()>0) return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		return null;
	}

	private void addISQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			items.add(builder.makeNameFilter(name, TextMatchOption.Is, value.trim()));
		}
	}
	private void addQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			items.add(builder.makeNameFilter(name, TextMatchOption.StartWith, value.trim()));
		}
	}
	private void addWildcardQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			items.add(builder.makeNameFilter(name, TextMatchOption.Contains, value.trim()));
		}
	}
//	private void addIdsQueryItem(FilterMaker builder,String id,List<FilterMaker> makers) {
//		if(StringUtils.isNotBlank(id)){
//			makers.add(builder.createMaker().makePickFilter("_id", new String[]{id}, Type.Text));
//		}
//	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getBreakName() {
		return breakName;
	}

	public void setBreakName(String breakName) {
		this.breakName = breakName;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getSupplierCountry() {
		return supplierCountry;
	}

	public void setSupplierCountry(String supplierCountry) {
		this.supplierCountry = supplierCountry;
	}


}
