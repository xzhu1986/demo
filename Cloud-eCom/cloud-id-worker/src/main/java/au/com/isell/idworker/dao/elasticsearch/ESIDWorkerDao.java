package au.com.isell.idworker.dao.elasticsearch;

import java.util.List;

import org.elasticsearch.action.get.GetResponse;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.idworker.IndexId;
import au.com.isell.idworker.RegKey;
import au.com.isell.idworker.dao.IDWorkerDao;
import au.com.isell.remote.common.model.Pair;

public class ESIDWorkerDao implements IDWorkerDao {

	@Override
	public RegKey getRegKey(String type, int serialNo) {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(RegKey.class);
		FilterItem itemSerialNo = maker.makeNameFilter("serialNo", TextMatchOption.Is, String.valueOf(serialNo));
		FilterItem itemType = maker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(type));
		FilterItem item = maker.linkWithAnd(itemSerialNo, itemType);
		Pair<Long, List<RegKey>> result = IndexHelper.getInstance().queryBeans(RegKey.class,
				new QueryParams(((ESFilterItem) item).getQueryBuilder(), null).withRoutings(new String[0]));
		if (result.getKey() > 0) {
			return result.getValue().get(0);
		}
		return null;
	}

	@Override
	public void putRegKey(RegKey regKey) {
		RegKey dbKey = getRegKey(regKey.getType(), regKey.getSerialNo());
		if (dbKey != null && !dbKey.getRegKey().equals(regKey.getRegKey()))
			IndexHelper.getInstance().deleteByObj(dbKey);
		IndexHelper.getInstance().indexValues(regKey);

	}

	@Override
	public int getLastNumber(String type) {
		GetResponse get = IndexHelper.getInstance().getRecord(IndexId.class, type, "lastNumber");
		if (!get.exists()) {
			IndexId index = new IndexId();
			index.setType(type);
			index.setLastNumber(0);
			IndexHelper.getInstance().indexValues(index);
			return 0;
		} else {
			return Integer.parseInt(get.field("lastNumber").getValue().toString());
		}
	}

	@Override
	public void putLastNumber(String type, int lastNumber) {
		IndexId index = new IndexId();
		index.setType(type);
		index.setLastNumber(lastNumber);
		IndexHelper.getInstance().indexValues(index);
	}

	@Override
	public boolean isRegKeyExists(String type, String key) {
		RegKey regKey = new RegKey();
		regKey.setRegKey(key);
		regKey.setType(type);
		GetResponse get = IndexHelper.getInstance().getRecord(RegKey.class, regKey.getKey(), "regKey");
		return get.exists();
	}

}
