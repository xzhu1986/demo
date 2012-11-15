package au.com.isell.rlm.importing.dao;

import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.rlm.common.dao.DAOSupport;

public class StoreHelper {

	private static IndexHelper helper = IndexHelper.getInstance();

	static{
		if(helper==null){
			helper = IndexHelper.getInstance();
		}
	}
	
	public static <T> void save(T... objects) {
		for (T obj : objects) {
			DAOSupport.saveSingleItem(obj);
		}
		indexValues(objects);
	}

	public static <T> void indexValues(T... objects) {
		helper.indexValues(objects);
	}
}
