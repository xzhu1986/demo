package au.com.isell.rlm.module.invoice.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class InvoiceSearchVals {
	private String invoiceNumber;
	private String agentId;
	private String company;
	
	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker,"invoiceNumber", invoiceNumber,makers);
		addISQueryItem(filterMaker,"agentId", agentId,makers);
		addQueryItem(filterMaker,"company", company,makers);
		
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

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public boolean isEmpty(){
		return StringUtils.isEmpty(company)&&
				StringUtils.isEmpty(invoiceNumber)&&
				StringUtils.isEmpty(agentId);
	}

}
