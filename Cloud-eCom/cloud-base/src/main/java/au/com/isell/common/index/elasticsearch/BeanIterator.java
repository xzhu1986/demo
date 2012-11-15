package au.com.isell.common.index.elasticsearch;

import java.util.Iterator;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

class BeanIterator<T> implements Iterator<T>, Iterable<T> {
	private static final int FETCH_COUNT = 500;

	private List<T> keys;
	private IndexHelper indexHelper;
	private QueryParams queryParams;
	private Class<?> cls;

	private long totalCount;
	private long totalIndex;
	private int keysIndex;
	private int pageNo;

	public BeanIterator(Class<T> cls, QueryParams queryParams) {
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

	private List<T> query() {
		List r=indexHelper.queryBeans(cls, this.queryParams.withPaging(pageNo, FETCH_COUNT)).getValue();// will reset paging param
		return r;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if (totalIndex < totalCount)
			return true;
		return false;
	}

	@Override
	public T next() {
		T key = null;
		if (keysIndex < keys.size()) {
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

	private T getNextKey() {
		T key = keys.get(keysIndex);
		keysIndex++;
		return key;
	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}

}