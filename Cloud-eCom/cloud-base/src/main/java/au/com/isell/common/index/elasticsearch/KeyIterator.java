package au.com.isell.common.index.elasticsearch;

import java.util.Iterator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

class KeyIterator implements Iterator<String>, Iterable<String> {
	private static final int FETCH_COUNT = 500;

	private String[] keys;
	private IndexHelper indexHelper;
	private QueryParams queryParams;
	private Class<?> cls;

	private long totalCount;
	private long totalIndex;
	private int keysIndex;
	private int pageNo;

	public KeyIterator(Class<?> cls, QueryParams queryParams) {
		indexHelper = IndexHelper.getInstance();
		this.queryParams = queryParams;
		pageNo = 1;
		keysIndex = 0;
		totalIndex = 0;
		this.cls = cls;
		totalCount = indexHelper.count(cls, this.queryParams.getQb());
		keys = query();
	}

	int simulateIndex = 0;

	private String[] query() {
		return indexHelper.queryKeys(cls, this.queryParams.withPaging(pageNo, FETCH_COUNT));// will reset paging param
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if (totalIndex < totalCount)
			return true;
		return false;
	}

	@Override
	public String next() {
		String key = null;
		if (keysIndex < keys.length) {
			key = getNextKey();
		} else {
			pageNo++;
			keys = query();
			keysIndex = 0;
			key = getNextKey();
		}
		totalIndex++;
		return key;
	}

	private String getNextKey() {
		String key = keys[keysIndex];
		keysIndex++;
		return key;
	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}

}