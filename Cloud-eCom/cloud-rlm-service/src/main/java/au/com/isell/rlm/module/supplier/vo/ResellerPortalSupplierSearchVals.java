package au.com.isell.rlm.module.supplier.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class ResellerPortalSupplierSearchVals {
	private String name;
	private String overview;

	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker, "name", name, makers);
		addWildcardQueryItem(filterMaker, "marketingSummary", overview, makers);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}


	
}
