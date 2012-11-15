package au.com.isell.rlm.module.supplier.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class SupplierPortalResellerMapVals {
	private String serialNo;
	private String contact;
	private String phone;
	private String company;
	
	private String supplierId;
	private String breakName;
	private String supplierAccountNumber;
	

	

	public FilterItem getFilter4ResellerSearch(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker, "company", company, makers);
		addWildcardQueryItem(filterMaker, "contact", contact, makers);
		addWildcardQueryItem(filterMaker, "phone", phone, makers);
		if (makers.size() > 0)
			return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		
		return null;
	}

	public FilterItem getFilterItem4RSMapSearch(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addISQueryItem(filterMaker, "supplierId", supplierId, makers);
		addISQueryItem(filterMaker, "serialNo", serialNo, makers);
		addWildcardQueryItem(filterMaker, "breakName", breakName, makers);
		addWildcardQueryItem(filterMaker, "supplierAccountNumber", supplierAccountNumber, makers);
		if (makers.size() > 0)
			return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		return null;
	}

	private void addISQueryItem(FilterMaker builder, String name, String value, List<FilterItem> items) {
		if (StringUtils.isNotBlank(value)) {
			items.add(builder.makeNameFilter(name, TextMatchOption.Is, value.trim()));
		}
	}

	private void addQueryItem(FilterMaker builder, String name, String value, List<FilterItem> items) {
		if (StringUtils.isNotBlank(value)) {
			items.add(builder.makeNameFilter(name, TextMatchOption.StartWith, value.trim()));
		}
	}

	private void addWildcardQueryItem(FilterMaker builder, String name, String value, List<FilterItem> items) {
		if (StringUtils.isNotBlank(value)) {
			items.add(builder.makeNameFilter(name, TextMatchOption.Contains, value.trim()));
		}
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getBreakName() {
		return breakName;
	}

	public void setBreakName(String breakName) {
		this.breakName = breakName;
	}

	public String getSupplierAccountNumber() {
		return supplierAccountNumber;
	}

	public void setSupplierAccountNumber(String supplierAccountNumber) {
		this.supplierAccountNumber = supplierAccountNumber;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public boolean isEmpty(){
		return StringUtils.isEmpty(company)&&
				StringUtils.isEmpty(contact)&&
				StringUtils.isEmpty(phone)&&
				StringUtils.isEmpty(supplierId)&&
				StringUtils.isEmpty(breakName)&&
				StringUtils.isEmpty(supplierAccountNumber)&&
				StringUtils.isEmpty(serialNo);
	}
}
