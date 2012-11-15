package au.com.isell.rlm.module.reseller.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

/**
 * @author frankw 16/02/2012
 */
public class ResellerSearchVals {
	private String company;
	private String status;
	private String version;
	private String country;
	private String city;
	private String region;
	private String serialNo;

	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addQueryItem(filterMaker,"company", company,makers);
		addISQueryItem(filterMaker,"status", status,makers);
		addQueryItem(filterMaker,"version", version,makers);
		addQueryItem(filterMaker,"country", country,makers);
		addQueryItem(filterMaker,"city", city,makers);
		addQueryItem(filterMaker,"region", region,makers);
		addWildcardQueryItem(filterMaker,"serialNoWildcard", serialNo,makers);
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
	
	public boolean isEmpty(){
		return StringUtils.isEmpty(company)&&
				StringUtils.isEmpty(status)&&
				StringUtils.isEmpty(version)&&
				StringUtils.isEmpty(country)&&
				StringUtils.isEmpty(city)&&
				StringUtils.isEmpty(region)&&
				StringUtils.isEmpty(company)&&
				StringUtils.isEmpty(serialNo);
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

}
