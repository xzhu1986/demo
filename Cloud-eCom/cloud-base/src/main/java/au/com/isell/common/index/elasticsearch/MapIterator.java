package au.com.isell.common.index.elasticsearch;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import au.com.isell.remote.common.model.Pair;

class MapIterator implements Iterator<Map>, Iterable<Map> {
	private static final int FETCH_COUNT = 500;

	private List<Map> keys;
	private IndexHelper indexHelper;
	private QueryParams queryParams;
	private Class<?> cls;

	private long totalCount;
	private long totalIndex;
	private int keysIndex;
	private int pageNo;

	public MapIterator(Class cls, QueryParams queryParams) {
		indexHelper = IndexHelper.getInstance();
		this.queryParams = queryParams;
		pageNo = 1;
		keysIndex = 0;
		totalIndex = 0;
		this.cls = cls;
		Pair<Long, List<Map>> result = query();
		totalCount = result.getKey();
		keys = result.getValue();
	}

	int simulateIndex = 0;

	private Pair<Long, List<Map>> query() {
		return indexHelper.queryMaps(cls, this.queryParams.withPaging(pageNo, FETCH_COUNT));// will reset paging param
	}

	@Override
	public Iterator<Map> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if (totalIndex < totalCount)
			return true;
		return false;
	}

	@Override
	public Map next() {
		Map key = null;
		if (keysIndex < keys.size()) {
			key = getNextKey();
		} else {
			pageNo++;
			keys = query().getValue();
			keysIndex = 0;
			key = getNextKey();
		}
		totalIndex++;
		return key;
	}

	private Map getNextKey() {
		Map key = (Map) keys.get(keysIndex);
		keysIndex++;
		return key;
	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}

}