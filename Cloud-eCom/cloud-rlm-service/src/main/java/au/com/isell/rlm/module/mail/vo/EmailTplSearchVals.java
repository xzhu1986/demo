package au.com.isell.rlm.module.mail.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;

public class EmailTplSearchVals {
	private String typeName;

	private String subject;

	public FilterItem getFilter(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker, "typeName", typeName, makers);
		addWildcardQueryItem(filterMaker, "subject", subject, makers);
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

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
