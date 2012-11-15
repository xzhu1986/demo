package au.com.isell.rlm.module.reseller.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.reseller.domain.ManualFile;
import au.com.isell.rlm.module.reseller.domain.PendingDataFile;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMapHistory;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.vo.ResellerFilter4Notify;
import au.com.isell.rlm.module.reseller.vo.ResellerSearchVals;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchVals;

public interface ResellerService {
	Reseller getReseller(int serialNo);

	ResellerUser getResellerUser(UUID userId);
	
	int saveDetail(int serialNo, Reseller reseller, ResellerUser user);
	
	long[] getStatisticsByStatus();
	List<Pair<Pair<String, String>, List<Pair<String, Long>>>> getStatisticsByCountry();

	Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMaps(String filter, int serialNo,ResellerSupplierMapSearchVals searchVals,int pageSize, int pageNo);
	void deleteResellerSupplierMap(Integer serialNo,Integer supplierId);
	
	void deleteResellerSupplierMapHistories(Integer serialNo, Integer supplierId);
	
	List<PendingDataFile> getPendingDownloadDataFiles(int serialNo);
	List<PendingDataFile> getPendingDownloadImageFiles(int serialNo);
	
	ResellerSupplierMap getResellerSupplierMap(Integer serialNo,Integer supplierId);
	
	boolean isDuplicateResellerSupplierMap(int serialNo,int supplierId);
	
//	UUID saveResellerSupplierMap4Reseller(ResellerSupplierMap map,String priceBreakId,String oldPriceBreakId);
	
	void saveResellerSupplierMapFromForm(ResellerSupplierMap map,int oldSerialNo, int oldSupplierId, boolean sync);
	
	Pair<Long, List<ResellerSearchBean>> query(ResellerSearchVals searchVals, String filter, Integer pageSize, Integer pageNo);
	
	List<ResellerSearchBean> queryAllReseller();

	List<String[]> getResellerSystemInfo(int serialNo);

	/**for freemarker*/
	public Reseller getReseller(String serialNo);
	
	void syncAllResellerInfoExchangeDate();

	java.util.Date getLatestRenewDate(int serialNo);

	void sendNotifyResellerEmail(ResellerFilter4Notify filter,Set<String> ids,List<Map> datas ) ;
	
	List<Map> queryReseller4Notification(ResellerFilter4Notify filter);
	
	List<ResellerSupplierMapHistory> getResellerSupplierMapHistories(Integer serialNo,Integer supplierId);

	List<ManualFile> getResellerManualFiles(String folder);

	Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMaps4ResellerPortal(String filter, int serialNo, int pageSize, int pageNo);

	UUID saveResellerUser(ResellerUser formUser, String permissionDatas);
}
