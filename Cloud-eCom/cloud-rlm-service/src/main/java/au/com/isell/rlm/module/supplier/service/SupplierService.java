package au.com.isell.rlm.module.supplier.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchVals;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.schedule.domain.ScheduleHistory;
import au.com.isell.rlm.module.supplier.domain.DataRequest;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierBillingInfo;
import au.com.isell.rlm.module.supplier.domain.SupplierBranch;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdateExecute;
import au.com.isell.rlm.module.supplier.domain.SupplierUser;
import au.com.isell.rlm.module.supplier.vo.PriceBreakSearchVals;
import au.com.isell.rlm.module.supplier.vo.PriceUpdateSearchVals;
import au.com.isell.rlm.module.supplier.vo.ResellerPortalSupplierSearchVals;
import au.com.isell.rlm.module.supplier.vo.SupplierFilter4Notify;
import au.com.isell.rlm.module.supplier.vo.SupplierPortalResellerMapVals;

public interface SupplierService {
	List<Pair<Pair<String, String>, List<Pair<String, Long>>>> getStatisticsByStatus();

	SupplierBillingInfo getBillingInfo(int supplierId);

	Supplier getSupplier(int supplierId);
	/** freemarker */
	Supplier getSupplier(String supplierId);

	SupplierUser getSupplierUser(UUID userId);

	void saveBillingInfo(int supplierId, SupplierBillingInfo billingInfo);

	DataRequest getDataRequest(int supplierId);

	void saveDataRequest(int supplierId, DataRequest dataRequest,UUID defaultPriceBreakId);

	int saveSupplierDetail(Supplier supplier, SupplierUser supplierUser);

	Pair<Long, List<PriceBreak>> queryPriceBreaks(String filter, int supplierId, PriceBreakSearchVals searchVals,Integer pageSize, Integer pageNo);
	
	List<PriceBreak> queryPriceBreaks(String search, String supplierId) ;
	
	List<PriceBreak> getPriceBreaks(int supplierId,UUID... priceBreakId);
	PriceBreak getPriceBreakByName(int supplierId,String priceBreakName);
	/** freemarker */
	long getResellersCountByPriceBreakId(String priceBreakId);
	
	UUID[] savePriceBreaks(int supplierId,boolean sync, PriceBreak... priceBreaks);

	void deletePriceBreak(int supplierId,List<UUID> priceBreakIds);
	
	List<PriceBreak> queryPriceBreaksBySupplierId(String supplierId);
	
	Pair<Long, List<SupplierBranch>> queryBranches(String filter, int supplierId,Integer pageSize, Integer pageNo);
	
	SupplierBranch getBranch(int supplierId,String branchId);
	
	UUID saveBranch(SupplierBranch branch,String branchId);
	
	void deleteBranch(int supplierId,String branchId);
	
	void saveSupplier(Supplier supplier, boolean sync);
	
	Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMaps(String filter, int supplierId,ResellerSupplierMapSearchVals searchVals,Integer pageSize, Integer pageNo);
	
	public List<SupplierSearchBean> querySupplier4Combobox(String search,String supplierId);
	
	Pair<Long, List<SupplierUpdateExecute>> querySupplierUpdates(PriceUpdateSearchVals searchVals, String filter, Integer pageSize, Integer pageNo);
	
	void saveSupplierUpdate(Supplier supplier,Schedule schedule,SupplierUpdateExecute supplierUpdateExecute);
	
	SupplierUpdateExecute getSupplierUpdateExecuteByScheduleId(String scheduleId);
	
	void saveOperateSupplierUpdateInfo(SupplierUpdateExecute supplierUpdateExecute,ScheduleHistory scheduleHistory);
	
	List<Map> querySuppliers4Notification(SupplierFilter4Notify filter);
	
	void sendNotifySupplierEmail(SupplierFilter4Notify filter, Set<String> excludeIds, List<Map> datas) ;

	List<Map<String,String>> querySupplierPortalResellerMap(SupplierPortalResellerMapVals searchVals, String filter);

	List<Supplier> querySuppliersInRSMapOfApproved();

	Pair<Long, List<SupplierSearchBean>> queryNotAllocatedSuppliers(int serialNo,ResellerPortalSupplierSearchVals searchVals, int pageNo, int pageSize);

	int[] statisticRsMap4ResellerPortal(int serialNo);
}