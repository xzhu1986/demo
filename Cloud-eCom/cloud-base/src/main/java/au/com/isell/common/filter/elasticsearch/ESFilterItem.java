package au.com.isell.common.filter.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.*;

import org.elasticsearch.index.query.QueryBuilder;

import au.com.isell.common.filter.FilterItem;

public class ESFilterItem implements FilterItem {
	private QueryBuilder qb;
	
	public ESFilterItem() {}
	public ESFilterItem(QueryBuilder qb) {
		this.qb = qb;
	}

	public void setQueryBuilder(QueryBuilder qb) {
		this.qb = qb;
	}

	public QueryBuilder getQueryBuilder() {
		return qb;
	}
	
	public QueryBuilder generateQueryBuilder() {
		if (qb == null) return matchAllQuery();
		return qb;
	}
}
