package au.com.isell.common.index.elasticsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

/**
 * @author frankw 20/02/2012
 */
public class BeanDataExtractor<T> implements DataExtractor {
	private Class<T> beanCls;

	public BeanDataExtractor(Class<T> beanCls) {
		this.beanCls=beanCls;
	}

	@Override
	public List<T> extractData(SearchResponse resp) {
		SearchHit[] hits = resp.getHits().hits();
		List<T> data = new ArrayList<T>(hits.length);
		for (int i = 0; i < hits.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("_id", hits[i].getId());
			map.putAll(hits[i].getSource());
			data.add(IsellIndexBeanUtils.constructBean(map, beanCls));
		}
		return data;
	}

}
