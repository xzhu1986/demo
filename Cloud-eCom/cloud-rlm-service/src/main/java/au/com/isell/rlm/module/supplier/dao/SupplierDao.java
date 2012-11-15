package au.com.isell.rlm.module.supplier.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.storage.StorageException;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierBranch;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdateExecute;
import au.com.isell.rlm.module.supplier.domain.SupplierUser;

public interface SupplierDao {
	Supplier getSupplierById(int supplierId);

	List<PriceBreak> listPriceBreak(int supplierId,Pair<String, Boolean>[] sorts) throws StorageException;
	
	void indexSupplier(int supplierId);

	Map<String, Long> getStatisticsByActiveSupplier();

	SupplierUser getSupplierUserBySupplierId(int supplierId);

	FilterMaker getSupplierSearchMaker();
	
	Pair<Long, List<SupplierSearchBean>> query(FilterItem filterItem, Pair<String, Boolean>[] sorts,Integer pageSize, Integer pageNo);
	
	FilterMaker getPriceBreakMaker();
	
	Pair<Long, List<PriceBreak>> queryPriceBreak(FilterItem filterItem, Pair<String, Boolean>[] sorts,Integer pageSize, Integer pageNo) ;
	
	List<PriceBreak> getPriceBreaks(int supplierId,UUID... priceBreakId) ;
	
	void deletePriceBreakIndex(PriceBreak priceBreak);
	
	long getResellersCountByPriceBreakId(FilterItem filterItem) ;
	
	Pair<Long, List<SupplierBranch>> queryBranch(FilterItem filterItem, Pair<String, Boolean>[] sorts,Integer pageSize, Integer pageNo) ;

	FilterMaker getBranchMaker();
	
	List<PriceBreak> queryPriceBreaks(FilterItem filterItem, Pair<String, Boolean>[] sorts);
	
	SupplierBranch getBranch(int supplierId,UUID branchId) ;
	
	/** this object should be new or from db*/
	public void saveOrUpdateSupplier(Supplier supplier);
	
	void saveBranch(SupplierBranch branch);
	void savePriceBreak(PriceBreak priceBreak);
	
	void deleteBranch(int supplierId, String branchId);
	
	FilterMaker getSupplierUpdatesMaker();
	
	Pair<Long, List<SupplierUpdateExecute>> querySupplierUpdates(FilterItem filterItem, Pair<String, Boolean>[] sorts,Integer pageSize, Integer pageNo);
	void saveSupplierUpdate(Supplier supplier,SupplierUpdateExecute supplierUpdateExecute);
	
	SupplierUpdateExecute getSupplierUpdateExecuteByScheduleId(String scheduleId);
	
	void saveSupplierUpdateExecute(SupplierUpdateExecute supplierUpdateExecute);
	
	Map<String, Long> getStatisticsByActivePriceBreak();
	
	Iterable<SupplierSearchBean> querySupplierBeans(FilterItem filterItem,Pair<String, Boolean>[] sorts);

	void saveOperationHistory(Supplier supplier);

	int[] statisticRsMap4ResellerPortal(int serialNo);

	Iterable<Map> querySupplierMaps(FilterItem filterItem, Pair<String, Boolean>[] sorts);
	
	Pair<Long, List<SupplierSearchBean>> querySupplierMapsWithExcludeIds(FilterItem filterItem, Set<Integer> excludeIds, Pair<String, Boolean>[] sorts, Integer pageSize,
			Integer pageNo);
}
