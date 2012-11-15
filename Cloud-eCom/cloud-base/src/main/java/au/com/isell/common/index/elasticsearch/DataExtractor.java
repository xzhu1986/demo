package au.com.isell.common.index.elasticsearch;

import java.util.List;

import org.elasticsearch.action.search.SearchResponse;

public interface DataExtractor {
	<T> List<T> extractData(SearchResponse resp);
}
