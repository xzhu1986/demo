package au.com.isell.rlm.module.reseller.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.mail.domain.EmailNotifyHistory;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMapHistory;
import au.com.isell.rlm.module.reseller.domain.VersionHistory;

public interface ResellerDao {
	Reseller getReseller(int serialNo);
	
	void indexResellerSearchBean(int... serialNos);
	
	FilterMaker getResellerSearchMaker();
	
	Pair<Long, List<ResellerSearchBean>> query(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize, Integer pageNo);
	
	long[] getStatisticsByStatus() ;

	Map<String, Map<Integer, Long>>  getStatisticsByCountry();
	
	void save(boolean reindex, Reseller... resellers);
	
	FilterMaker getResellerSupplierMapMaker();

	Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMap(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize, Integer pageNo);
	
	Iterable<ResellerSupplierMap> queryResellerSupplierMap(FilterItem filterItem, Pair<String, Boolean>[] sorts);

	void deleteResellerSupplierMap(Integer serialNo,Integer supplierId);
	
	void deleteResellerSupplierMapHistories(ResellerSupplierMapHistory... histories);
	
	long getResellerSupplierMapCount(FilterItem filterItem);
	
	ResellerSupplierMap getResellerSupplierMap(Integer serialNo,Integer supplierId) ;
	
	void saveResellerSupplierMap(ResellerSupplierMap... maps);

	Pair<Long, List<VersionHistory>> getVersionHistory(int serialNo, Integer from, Integer size);
	
	void saveVersionHistories(VersionHistory... histories);
	
	void saveNotifyHistory(EmailNotifyHistory notifyHistory);
	
	public Iterable<String> queryResellerKeys(FilterItem filterItem) ;
	
	void indexResellerSupplierMap(Integer serialNo,Integer supplierId);
	
	Iterable<ResellerSearchBean> queryResellerBeans(FilterItem filterItem,Pair<String, Boolean>[] sorts);
	
	void saveResellerSupplierMapHistory(ResellerSupplierMapHistory history);
	
	List<ResellerSupplierMapHistory> getResellerSupplierMapHistories(FilterItem filterItem);
	
	FilterMaker getResellerSupplierMapHistoryMaker();

	Set<Integer> querySupplierIdsInRSMap(FilterItem filterItem);

	void indexResellerSearchBeans(ResellerSearchBean... beans);

	void saveOperationHistory(Reseller reseller);

}
