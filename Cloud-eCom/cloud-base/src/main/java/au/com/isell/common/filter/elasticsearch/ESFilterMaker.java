package au.com.isell.common.filter.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.idsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.QueryBuilders.textPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;

import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterItem.Type;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.util.UTCDateUtils;
import au.com.isell.remote.common.model.Pair;

public class ESFilterMaker implements FilterMaker {

	private FieldMapper mapper;
	private String index;
	private Map<String, String[]> fieldMapper;
	private String type;
	private Class<?> clazz;

	public ESFilterMaker() {
		fieldMapper = new HashMap<String, String[]>();
		mapper = new FieldMapper();
	}

	public ESFilterMaker(FieldMapper mapper) {
		fieldMapper = new HashMap<String, String[]>();
		this.mapper = mapper;
	}

	@Override
	public FilterItem makeNameFilter(String fieldName, TextMatchOption matchOption, String condition) {
		String[] physicalField = getPhysicalFields(fieldName);
		switch (matchOption) {
		case Contains: { // needs work with analyzed fields
			if (physicalField[1] != null)
				return new ESFilterItem(textPhraseQuery(physicalField[1], makeWildcardCondition(condition, true, true)));
			else if (physicalField[2] != null)
				return new ESFilterItem(textPhraseQuery(physicalField[2], condition.toLowerCase()));
			else
				return new ESFilterItem(textPhraseQuery(physicalField[0], condition));
		}
		case NotContains: { // needs work with analyzed fields
			if (physicalField[1] != null)
				return new ESFilterItem(boolQuery().mustNot(
						textPhraseQuery(physicalField[1], makeWildcardCondition(condition, true, true))));
			else if (physicalField[2] != null)
				return new ESFilterItem(boolQuery().mustNot(textPhraseQuery(physicalField[2], condition.toLowerCase())));
			else
				return new ESFilterItem(boolQuery().mustNot(textPhraseQuery(physicalField[0], condition)));
		}
		case Is: {
			return new ESFilterItem(termQuery(physicalField[0], condition));
		}
		case IsNot: {
			return new ESFilterItem(boolQuery().mustNot(termQuery(physicalField[0], condition)));
		}
		case StartWith: {
			if (physicalField[1] != null)
				return new ESFilterItem(textPhraseQuery(physicalField[1], makeWildcardCondition(condition, false, true)));
			else if (physicalField[2] != null)
				return new ESFilterItem(textPhraseQuery(physicalField[2], condition.toLowerCase()));
			else
				return new ESFilterItem(wildcardQuery(physicalField[0], condition+"*"));
		}
		case EndWith: { // not support
			if (physicalField[1] != null)
				return new ESFilterItem(textPhraseQuery(physicalField[1], makeWildcardCondition(condition, true, false)));
			else if (physicalField[2] != null)
				return new ESFilterItem(textPhraseQuery(physicalField[2], condition.toLowerCase()));
			else
				return new ESFilterItem(termQuery(physicalField[0], condition));
		}
		}
		return null;
	}

	private String makeWildcardCondition(String keyword, boolean wildcardStart, boolean wildcardEnd) {
		StringBuilder sb = new StringBuilder();
		boolean start = true;
		for (char ch : keyword.toCharArray()) {
			if (start)
				start = false;
			else
				sb.append(' ');
			if (ch == ' ') ch='_';
			sb.append(Character.toLowerCase(ch));
		}
		if (wildcardStart && wildcardEnd)
			return sb.toString();
		else if (wildcardStart)
			return sb.toString() + " $";
		else if (wildcardEnd)
			return "$ " + sb.toString();
		return sb.toString();
	}

	@Override
	public FilterItem makePickFilter(String fieldName, String[] pickedItems, Type type) {
		String[] physicalField = getPhysicalFields(fieldName);
		if ("_id".equals(physicalField[0])) {
			return new ESFilterItem(idsQuery(this.type).addIds(pickedItems));
		} else if (pickedItems == null || pickedItems.length == 0) {
			return new ESFilterItem();
		} else {
			TermsQueryBuilder terms = termsQuery(fieldName, pickedItems);
			String[] formated = new String[pickedItems.length];
			int i = 0;
			for (String item : pickedItems) {
				if (type == Type.Date) {
					try {
						formated[i]=UTCDateUtils.format(FORMAT.parse(item));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else {
					formated[i]=item;
				}
				i++;
			}
			return new ESFilterItem(terms);
		}

	}

	@Override
	public FilterItem makeDateRange(String fieldName, Date start, Date end, boolean includeLower, boolean includeUpper) {
		RangeQueryBuilder builder = rangeQuery(getPhysicalFields(fieldName)[0]);
		if (start != null) {
			builder = builder.from(UTCDateUtils.format(start)).includeLower(includeLower);
		}
		if (end != null)
			builder = builder.to(UTCDateUtils.format(end)).includeUpper(includeUpper);

		return new ESFilterItem(builder);
	}

	@Override
	public FilterItem makeIntRange(String fieldName, Integer bottom, Integer top, boolean includeLower, boolean includeUpper) {
		RangeQueryBuilder builder = rangeQuery(getPhysicalFields(fieldName)[0]);
		if (bottom != null)
			builder = builder.from(bottom).includeLower(includeLower);
		if (top != null)
			builder = builder.to(top).includeUpper(includeUpper);
		return new ESFilterItem(builder);
	}

	@Override
	public FilterItem makeDecimalRange(String fieldName, BigDecimal bottom, BigDecimal top, boolean includeLower, boolean includeUpper) {
		RangeQueryBuilder builder = rangeQuery(getPhysicalFields(fieldName)[0]);
		if (bottom != null)
			builder = builder.from(bottom).includeLower(includeLower);
		if (top != null)
			builder = builder.to(top).includeUpper(includeUpper);
		return new ESFilterItem(builder);
	}

	@Override
	public FilterItem linkWithAnd(FilterItem... items) {
		BoolQueryBuilder boolQB = boolQuery();
		for (FilterItem item : items) {
			if (item == null)
				continue;
			if (!(item instanceof ESFilterItem))
				throw new IllegalArgumentException("Maker must be ESFilterItem");
			if (((ESFilterItem) item).getQueryBuilder() == null)
				continue;
			boolQB = boolQB.must(((ESFilterItem) item).getQueryBuilder());
		}
		if (!boolQB.hasClauses()) {
			return new ESFilterItem(null);
		}
		return new ESFilterItem(boolQB);
	}

	@Override
	public FilterItem linkWithOr(FilterItem... items) {
		BoolQueryBuilder boolQB = boolQuery();
		for (FilterItem item : items) {
			if (!(item instanceof ESFilterItem))
				throw new IllegalArgumentException("Maker must be ESFilterItem");
			if (((ESFilterItem) item).getQueryBuilder() == null)
				continue;
			boolQB = boolQB.should(((ESFilterItem) item).getQueryBuilder());
		}
		if (!boolQB.hasClauses()) {
			return new ESFilterItem();
		}
		return new ESFilterItem(boolQB);
	}

	@Override
	public void setFieldMapper(FieldMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public FilterItem makeAllQuery() {
		return new ESFilterItem(matchAllQuery());
	}

	@Override
	public FilterItem makeNullQuery(String field) {
		return new ESFilterItem(queryString(String.format("-%s:[* TO *]", field)));
	}

	@Override
	public <T> void setType(Class<T> type) {
		this.clazz = type;
		if (!type.isAnnotationPresent(ISellIndex.class)) return;
		ISellIndex indexDef = type.getAnnotation(ISellIndex.class);
		index = indexDef.name();
		if (!indexDef.type().equals("")) {
			this.type = indexDef.type();
		}
		try {
			BeanInfo info = Introspector.getBeanInfo(type);
			for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
				Method method = prop.getReadMethod();
				if (method == null)
					continue;
				String fieldName = prop.getName();
				String physicalField = mapper.getQueryName(fieldName);
				if (method.isAnnotationPresent(ISellIndexValue.class)) {
					ISellIndexValue value = method.getAnnotation(ISellIndexValue.class);
					String[] realFields = new String[]{physicalField, null, null};
					if (value.wildcard()) {
						realFields[1] = physicalField+"_wildcard";
					}
					if ("analyzed".equals(value.index())) {
						realFields[2] = physicalField+"_analyzed";
					}
					fieldMapper.put(fieldName, realFields);
				} else if (method.isAnnotationPresent(ISellIndexKey.class)) {
					ISellIndexKey value = method.getAnnotation(ISellIndexKey.class);
					String[] realFields = new String[]{"_id", null, null};
					if (value.wildcard()) {
						realFields[1] = physicalField+"_wildcard";
					}
					fieldMapper.put(fieldName, realFields);
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException("Register " + type.getName() + " error: " + e.getMessage(), e);
		}
	}
	@Override
	public Pair<String, Boolean> makeSortItem(String sortField, boolean asc) {
		String[] physicalFields = getPhysicalFields(sortField);
		String field = physicalFields[0];
		if (field.equals("_id")) field = "_uid";
		return new Pair<String, Boolean>(field, asc);
	}

	private String[] getPhysicalFields(String field) {
		String[] physicalFields =  fieldMapper.get(field);
		if (physicalFields == null) throw new RuntimeException("Field "+field +" has not been defined in type "+clazz.getName());
		return physicalFields;
	}

	@Override
	public String getPhysicalField(String fieldName) {
		return getPhysicalFields(fieldName)[0];
	}

	@Override
	public String getPhysicalWildcardField(String fieldName) {
		String physicalField = getPhysicalFields(fieldName)[1];
		if (physicalField == null) throw new RuntimeException("Field "+fieldName +" has not been defined as wildcard in type "+clazz.getName());
		return physicalField;
	}
}
