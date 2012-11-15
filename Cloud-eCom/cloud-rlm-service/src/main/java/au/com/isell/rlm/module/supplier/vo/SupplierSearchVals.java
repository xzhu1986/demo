package au.com.isell.rlm.module.supplier.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class SupplierSearchVals {
	private String name;
	private String supplierId;
	private String resellerCount;
	private String status;
	private String country;
	private String region;
	private String productCount;
	
	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker,"name", name,makers);
		addISQueryItem(filterMaker,"status", status,makers);
		addGreaterThanQueryItem(filterMaker,"resellerCount", resellerCount,makers);
		addGreaterThanQueryItem(filterMaker,"productCount", productCount,makers);
		addQueryItem(filterMaker,"country", country,makers);
		addWildcardQueryItem(filterMaker,"region", region,makers);
		addISQueryItem(filterMaker,"supplierId", supplierId,makers);
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
	
	private void addGreaterThanQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			BigDecimal dec = new BigDecimal(value);
			items.add(builder.makeDecimalRange(name, dec, null, true, false));
		}
	}
//	private void addIdsQueryItem(FilterMaker builder,String id,List<FilterMaker> makers) {
//		if(StringUtils.isNotBlank(id)){
//			makers.add(builder.createMaker().makePickFilter("_id", new String[]{id}, Type.Text));
//		}
//	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getResellerCount() {
		return resellerCount;
	}

	public void setResellerCount(String resellerCount) {
		this.resellerCount = resellerCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getProductCount() {
		return productCount;
	}

	public void setProductCount(String productCount) {
		this.productCount = productCount;
	}


}
