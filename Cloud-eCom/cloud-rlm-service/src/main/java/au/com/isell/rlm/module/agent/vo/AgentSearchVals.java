package au.com.isell.rlm.module.agent.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.agent.domain.Agent.Status;

public class AgentSearchVals {
	private String name;

	private String country;

	private String company;
	
	private Status status;
	
	

	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addQueryItem(filterMaker, "name", name, makers);
		addISQueryItem(filterMaker, "country", country, makers);
		addQueryItem(filterMaker, "company", company, makers);
		if (status!=null) {
			makers.add(filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(status.ordinal())));
		}
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

	public void setName(String name) {
		this.name = name;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setStatus(Status status) {
		this.status = status;
	}


}
