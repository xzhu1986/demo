package au.com.isell.rlm.module.supplier.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class PriceBreakSearchVals {
	private String name;
	private String status;
	private String resllersCount;

	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addQueryItem(filterMaker,"name", name,makers);
		addISQueryItem(filterMaker,"status", status,makers);
		if(makers.size()>0) return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		return null;
	}

	private void addISQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			items.add(builder.makeNameFilter(name, TextMatchOption.Is, value.trim().toLowerCase()));
		}
	}
	private void addQueryItem(FilterMaker builder,String name,String value,List<FilterItem> items) {
		if(StringUtils.isNotBlank(value)){
			items.add(builder.makeNameFilter(name, TextMatchOption.StartWith, value.trim().toLowerCase()));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResllersCount() {
		return resllersCount;
	}

	public void setResllersCount(String resllersCount) {
		this.resllersCount = resllersCount;
	}



}
