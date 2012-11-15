package au.com.isell.common.index.elasticsearch;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import au.com.isell.remote.common.model.Pair;

public class QueryParams {
	private QueryBuilder qb;
	private FilterBuilder fb;
	private Pair<String, Boolean>[] sorts;
	private Integer pageNo;
	private Integer pageSize;
	private String[] routings;
	private String[] columns;

	public QueryParams(QueryBuilder qb, Pair<String, Boolean>[] sorts) {
		this.qb = qb;
		this.sorts = sorts;
	}

	public QueryParams withPaging(Integer pageNo, Integer pageSize) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		return this;
	}

	public QueryParams withRoutings(String... routings) {
		this.routings = routings;
		return this;
	}
	public QueryParams withColumns(String... columns) {
		this.columns = columns;
		return this;
	}

	public QueryBuilder getQb() {
		return qb;
	}

	public Pair<String, Boolean>[] getSorts() {
		return sorts;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public String[] getRoutings() {
		return routings;
	}

	public String[] getColumns() {
		return columns;
	}

	public FilterBuilder getFilterBuilder() {
		return fb;
	}

	public void setFilterBuilder(FilterBuilder fb) {
		this.fb = fb;
	}

}