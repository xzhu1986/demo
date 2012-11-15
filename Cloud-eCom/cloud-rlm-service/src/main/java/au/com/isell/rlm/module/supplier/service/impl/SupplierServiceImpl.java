package au.com.isell.rlm.module.supplier.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterItem.Type;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.message.MessageChecker;
import au.com.isell.common.util.Formatter;
import au.com.isell.common.util.IDServerClient;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.mail.dao.EmailDao;
import au.com.isell.rlm.module.mail.domain.EmailNotifyHistory;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;
import au.com.isell.rlm.module.mail.service.EmailTemplateService;
import au.com.isell.rlm.module.mail.service.MailService;
import au.com.isell.rlm.module.mail.vo.MailAddress;
import au.com.isell.rlm.module.mail.vo.MailContent;
import au.com.isell.rlm.module.mail.vo.MailPreview;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchFilters;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchVals;
import au.com.isell.rlm.module.schedule.dao.ScheduleDao;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.schedule.domain.ScheduleHistory;
import au.com.isell.rlm.module.supplier.dao.SupplierDao;
import au.com.isell.rlm.module.supplier.domain.DataRequest;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.Supplier.Status;
import au.com.isell.rlm.module.supplier.domain.SupplierBillingInfo;
import au.com.isell.rlm.module.supplier.domain.SupplierBranch;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdateExecute;
import au.com.isell.rlm.module.supplier.domain.SupplierUser;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.supplier.vo.BranchSearchFilters;
import au.com.isell.rlm.module.supplier.vo.PriceBreakSearchFilters;
import au.com.isell.rlm.module.supplier.vo.PriceBreakSearchVals;
import au.com.isell.rlm.module.supplier.vo.PriceUpdateSearchFilters;
import au.com.isell.rlm.module.supplier.vo.PriceUpdateSearchVals;
import au.com.isell.rlm.module.supplier.vo.ResellerPortalSupplierSearchVals;
import au.com.isell.rlm.module.supplier.vo.SupplierFilter4Notify;
import au.com.isell.rlm.module.supplier.vo.SupplierPortalResellerMapFilters;
import au.com.isell.rlm.module.supplier.vo.SupplierPortalResellerMapVals;
import au.com.isell.rlm.module.upload.service.FileService;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.service.PermissionService;

@Service
public class SupplierServiceImpl implements SupplierService {
	private static final int MAX_RETURN = 30;

	private static Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);

	@Autowired
	private SupplierDao supplierDao;
	@Autowired
	private AddressService addressService;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private ResellerDao resellerDao;
	@Autowired
	private ScheduleDao scheduleDao;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private EmailTemplateService emailTemplateService;
	@Autowired
	private FileService fileService;
	@Autowired
	private MailService mailService;
	@Autowired
	private EmailDao emailDao;

	@Override
	public List<Pair<Pair<String, String>, List<Pair<String, Long>>>> getStatisticsByStatus() {
		List<Pair<Pair<String, String>, List<Pair<String, Long>>>> r = new ArrayList<Pair<Pair<String, String>, List<Pair<String, Long>>>>();

		Map<String, Long> spCount = supplierDao.getStatisticsByActiveSupplier();// country:count
		Map<String, Long> pbCount = supplierDao.getStatisticsByActivePriceBreak();
		Set<String> tempSet = new HashSet<String>();
		tempSet.addAll(spCount.keySet());
		tempSet.addAll(pbCount.keySet());

		Set<String> countrySet = new HashSet<String>();
		for (String country : tempSet) {
			if (permissionService.isAllowedCountryOfSupplierPerm(country)) {
				countrySet.add(country);
			}
		}
		String[] countries = countrySet.toArray(new String[countrySet.size()]);
		Arrays.sort(countries);
		long spTotal = 0l;
		long pbTotal = 0l;
		for (String countryCode : countries) {
			AddressItem addressItem = addressService.getAddressItem(countryCode);
			Long spc = spCount.get(countryCode);
			spc = spc == null ? 0l : spc;
			Long pdc = pbCount.get(countryCode);
			pdc = pdc == null ? 0l : pdc;

			spTotal += spc;
			pbTotal += pdc;

			List<Pair<String, Long>> subCount = new ArrayList<Pair<String, Long>>();
			subCount.add(new Pair<String, Long>(String.valueOf(Supplier.Status.Active.ordinal()), spc));
			subCount.add(new Pair<String, Long>(String.valueOf(PriceBreak.Status.Active.ordinal()), pdc));
			Pair<String, String> countryDef = new Pair<String, String>(countryCode, addressItem.getName());
			r.add(new Pair<Pair<String, String>, List<Pair<String, Long>>>(countryDef, subCount));
		}
		List<Pair<String, Long>> subCount = new ArrayList<Pair<String, Long>>();
		subCount.add(new Pair<String, Long>(String.valueOf(Supplier.Status.Active.ordinal()), spTotal));
		subCount.add(new Pair<String, Long>(String.valueOf(PriceBreak.Status.Active.ordinal()), pbTotal));
		Pair<String, String> total = new Pair<String, String>("", "Total");
		r.add(new Pair<Pair<String, String>, List<Pair<String, Long>>>(total, subCount));

		return r;
	}

	@Override
	public SupplierBillingInfo getBillingInfo(int supplierId) {
		return getSupplier(supplierId).getBillingInfo();
	}

	@Override
	public Supplier getSupplier(int supplierId) {
		return supplierDao.getSupplierById(supplierId);
	}

	@Override
	public Supplier getSupplier(String supplierId) {
		return getSupplier(Integer.valueOf(supplierId));
	}

	@Override
	public SupplierUser getSupplierUser(UUID userId) {
		return (SupplierUser) userDAO.getUserById(userId);
	}

	@Override
	public void saveBillingInfo(int supplierId, SupplierBillingInfo billingInfo) {
		Supplier supplier = getSupplier(supplierId);
		supplier.setBillingInfo(billingInfo);
		supplierDao.saveOrUpdateSupplier(supplier);
		try {
			MessageChecker.getInstance("rlm").sendMessages(supplier);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	@Override
	public int saveSupplierDetail(Supplier supplier, SupplierUser supplierUser) {
		int supplierId = supplier.getSupplierId();
		Integer newSupplierId = supplierId;
		// save reseller
		Supplier dbSupplier = null;
		if (supplierId != -1) {
			dbSupplier = getSupplier(supplierId);
		} else {
			newSupplierId = createNewSupplierId(supplier);
			supplier.setSupplierId(newSupplierId);
			supplier.setCreateDate(new Date());
			dbSupplier = supplier;
		}

		// save SupplierUser
		if (supplierId != -1) {
			SupplierUser dbSupplierUser = getSupplierUser(dbSupplier.getUserId());
			BeanUtils.copyPropsExcludeNull(dbSupplierUser, supplierUser);
			userDAO.saveUser(dbSupplierUser);
		} else {
			supplierUser.setUserId(UUID.randomUUID());
			supplierUser.setSupplierId(newSupplierId);
			userDAO.saveUser(supplierUser);
			// set userid
			dbSupplier.setUserId(supplierUser.getUserId());
		}
		BeanUtils.copyPropsExcludeNull(dbSupplier, supplier);
		supplierDao.saveOrUpdateSupplier(dbSupplier);
		try {
			MessageChecker.getInstance("rlm").sendMessages(supplier);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		return newSupplierId;
	}

	private Integer createNewSupplierId(Supplier formSupplier) {
		Integer newSupplierId = IDServerClient.getIntId("supplier_" + formSupplier.getCountry(), 1)[0];
		AddressItem addressItem = addressService.getAddressItem(formSupplier.getCountry());
		int phoneAreaCodeBind = addressItem.getPhoneAreaCodeBind();
		return (int) Math.pow(10, 6 - String.valueOf(phoneAreaCodeBind).length()) * phoneAreaCodeBind + newSupplierId;
	}

	@Override
	public DataRequest getDataRequest(int supplierId) {
		return getSupplier(supplierId).getDataRequest();
	}

	@Override
	public void saveDataRequest(int supplierId, DataRequest dataRequest, UUID defaultPriceBreakId) {
		Supplier sup = getSupplier(supplierId);
		sup.setDataRequest(dataRequest);
		if (defaultPriceBreakId != null) {
			sup.setDefaultPriceBreakID(defaultPriceBreakId);
		}
		supplierDao.saveOrUpdateSupplier(sup);
		try {
			MessageChecker.getInstance("rlm").sendMessages(sup);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	@Override
	public Pair<Long, List<PriceBreak>> queryPriceBreaks(String filter, int supplierId, PriceBreakSearchVals searchVals, Integer pageSize,
			Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = supplierDao.getPriceBreakMaker();

		FilterItem item1 = searchVals.getFilter(maker);
		FilterItem item2 = null;
		if (StringUtils.isNotBlank(filter)) {
			PriceBreakSearchFilters filters = (PriceBreakSearchFilters) BeanUtils.getEnum(PriceBreakSearchFilters.class, filter);
			item2 = filters.getFilterItem(maker);
		}
		FilterItem item3 = maker.makeNameFilter("supplierId", TextMatchOption.Is, String.valueOf(supplierId));
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("name", true);
		Pair<Long, List<PriceBreak>> r = supplierDao.queryPriceBreak(maker.linkWithAnd(item1, item2, item3), sorts, pageSize, pageNo);
		return r;
	}

	@Override
	public List<PriceBreak> queryPriceBreaks(String search, String supplierId) {
		Assert.isTrue(NumberUtils.isNumber(supplierId));

		FilterMaker maker = supplierDao.getPriceBreakMaker();
		FilterItem item1 = maker.makeNameFilter("supplierId", TextMatchOption.Is, supplierId);
		FilterItem item2 = null;
		if (StringUtils.isNotBlank(search))
			maker.makeNameFilter("name", TextMatchOption.StartWith, String.valueOf(search));
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("name", true);
		Pair<Long, List<PriceBreak>> r = supplierDao.queryPriceBreak(maker.linkWithAnd(item1, item2), sorts, MAX_RETURN, 1);
		return r.getValue();
	}

	@Override
	public List<PriceBreak> getPriceBreaks(int supplierId, UUID... priceBreakId) {
		Assert.notNull(priceBreakId);
		return supplierDao.getPriceBreaks(supplierId, priceBreakId);
	}

	@Override
	public PriceBreak getPriceBreakByName(int supplierId, String priceBreakName) {
		Assert.notNull(priceBreakName);
		FilterMaker maker = supplierDao.getPriceBreakMaker();
		FilterItem filterSupplierId = maker.makeNameFilter("supplierId", TextMatchOption.Is, String.valueOf(supplierId));
		FilterItem filterPBName = maker.makeNameFilter("name", TextMatchOption.Is, priceBreakName);
		List<PriceBreak> breaks = supplierDao.queryPriceBreaks(maker.linkWithAnd(filterSupplierId, filterPBName), null);
		if (breaks.size() == 0)
			return null;
		else
			return breaks.get(0);
	}

	@Override
	public UUID[] savePriceBreaks(int supplierId, boolean sync, PriceBreak... priceBreaks) {
		UUID[] ids = new UUID[priceBreaks.length];
		for (int i = 0; i < ids.length; i++) {
			PriceBreak priceBreak = priceBreaks[i];
			if (priceBreak.getPriceBreakId() == null) {
				priceBreak.init();
				priceBreak.setPriceBreakId(UUID.randomUUID());
			} else {
				List<PriceBreak> dbBreaks = getPriceBreaks(priceBreak.getSupplierId(), priceBreak.getPriceBreakId());
				if (dbBreaks.size() > 0) {
					PriceBreak dbBreak = dbBreaks.get(0);
					BeanUtils.copyPropsExcludeNull(dbBreak, priceBreak);
					priceBreak = dbBreak;
				}
			}
			supplierDao.savePriceBreak(priceBreak);
			ids[i] = priceBreak.getPriceBreakId();
		}
		if (sync) {
			try {
				MessageChecker.getInstance("rlm").sendMessages(supplierDao.getSupplierById(supplierId));
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return ids;
	}

	@Override
	public void deletePriceBreak(int supplierId, List<UUID> priceBreakIds) {
		Supplier supplier = getSupplier(supplierId);
		for (UUID id : priceBreakIds) {
			supplier.getPriceBreaks().remove(id);
			supplierDao.deletePriceBreakIndex(new PriceBreak(supplierId, id));
		}
		supplierDao.saveOrUpdateSupplier(supplier);
	}

	@Override
	public List<PriceBreak> queryPriceBreaksBySupplierId(String supplierId) {
		Assert.isTrue(NumberUtils.isNumber(supplierId));

		FilterMaker filterMaker = supplierDao.getPriceBreakMaker();
		FilterItem filterItem = filterMaker.makeNameFilter("supplierId", TextMatchOption.Is, supplierId);

		return supplierDao.queryPriceBreaks(filterItem, null);
	}

	@Override
	public Pair<Long, List<SupplierBranch>> queryBranches(String filter, int supplierId, Integer pageSize, Integer pageNo) {
		FilterMaker maker = supplierDao.getBranchMaker();

		FilterItem item1 = null;
		if (StringUtils.isNotBlank(filter)) {
			BranchSearchFilters filters = (BranchSearchFilters) BeanUtils.getEnum(BranchSearchFilters.class, filter);
			item1 = filters.getFilterItem(maker);
		}
		FilterItem item2 = maker.makeNameFilter("supplierId", TextMatchOption.Is, String.valueOf(supplierId));

		FilterItem item3 = maker.makeNameFilter("deleted", TextMatchOption.Is, String.valueOf(false));

		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("name", true);
		Pair<Long, List<SupplierBranch>> r = supplierDao.queryBranch(maker.linkWithAnd(item1, item2, item3), sorts, pageSize, pageNo);
		if (r.getValue() != null) {
			List<SupplierBranch> branches = new ArrayList<SupplierBranch>();
			SupplierBranch branch = null;
			for (SupplierBranch b : r.getValue()) {
				branch = getBranch(b.getSupplierId(), b.getBranchId().toString());
				if (branch != null) {
					branches.add(branch);
				}
			}
			return new Pair<Long, List<SupplierBranch>>(r.getKey(), branches);
		}
		return r;
	}

	@Override
	public SupplierBranch getBranch(int supplierId, String branchId) {
		Assert.notNull(branchId);
		return supplierDao.getBranch(supplierId, UUID.fromString(branchId));
	}

	@Override
	public UUID saveBranch(SupplierBranch branch, String bId) {
		Assert.notNull(bId);

		if ("new".equalsIgnoreCase(bId)) {
			branch.setBranchId(UUID.randomUUID());
		} else {
			UUID branchId = UUID.fromString(bId);
			branch.setBranchId(branchId);
			SupplierBranch dbBranch = getBranch(branch.getSupplierId(), bId);
			BeanUtils.copyPropsExcludeNull(dbBranch, branch);
			branch = dbBranch;
		}
		supplierDao.saveBranch(branch);
		try {
			MessageChecker.getInstance("rlm").sendMessages(branch);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		return branch.getBranchId();
	}

	@Override
	public void deleteBranch(int supplierId, String branchId) {
		supplierDao.deleteBranch(supplierId, branchId);
	}

	@Override
	public void saveSupplier(Supplier supplier, boolean sync) {
		supplierDao.saveOrUpdateSupplier(supplier);
		if (sync) {
			try {
				MessageChecker.getInstance("rlm").sendMessages(supplier);
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	@Override
	public Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMaps(String filter, int supplierId, ResellerSupplierMapSearchVals searchVals,
			Integer pageSize, Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();

		FilterItem item1 = searchVals.getFilter(maker);
		FilterItem item2 = null;
		if (StringUtils.isNotBlank(filter)) {
			ResellerSupplierMapSearchFilters filters = (ResellerSupplierMapSearchFilters) BeanUtils.getEnum(ResellerSupplierMapSearchFilters.class,
					filter);
			item2 = filters.getFilterItem(maker);
		}
		FilterItem item3 = maker.makeNameFilter("supplierId", TextMatchOption.Is, String.valueOf(supplierId));
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("company", true);
		Pair<Long, List<ResellerSupplierMap>> r = resellerDao.queryResellerSupplierMap(maker.linkWithAnd(item1, item2, item3), sorts, pageSize,
				pageNo);
		return r;
	}

	@Override
	public List<SupplierSearchBean> querySupplier4Combobox(String search, String supplierId) {
		FilterMaker maker = supplierDao.getSupplierSearchMaker();
		FilterItem item1 = null;
		FilterItem item2 = null;
		if (StringUtils.isNotBlank(supplierId) && NumberUtils.isNumber(supplierId)) {
			item1 = maker.makeNameFilter("supplierId", TextMatchOption.Is, supplierId);
		}
		if (StringUtils.isNotBlank(search) && NumberUtils.isNumber(search)) {
			item2 = maker.makeNameFilter("name", TextMatchOption.StartWith, search);
		}
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("shortName", true);
		return supplierDao.query(maker.linkWithAnd(item1, item2), sorts, null, null).getValue();
	}

	@Override
	public List<Supplier> querySuppliersInRSMapOfApproved() {
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();
		Set<Integer> supplierIds = resellerDao.querySupplierIdsInRSMap(maker.makeNameFilter("status", TextMatchOption.Is,
				String.valueOf(ApprovalStatus.Approved.ordinal())));

		List<Supplier> r = new ArrayList<Supplier>();
		for (Integer id : supplierIds) {
			r.add(this.getSupplier(id.toString()));
		}
		Collections.sort(r, new Comparator<Supplier>() {
			@Override
			public int compare(Supplier o1, Supplier o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return r;
	}

	@Override
	public long getResellersCountByPriceBreakId(String priceBreakId) {
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();
		FilterItem item = maker.makeNameFilter("priceBreakId", TextMatchOption.Is, priceBreakId);
		return supplierDao.getResellersCountByPriceBreakId(item);
	}

	@Override
	public Pair<Long, List<SupplierUpdateExecute>> querySupplierUpdates(PriceUpdateSearchVals searchVals, String filter, Integer pageSize,
			Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = supplierDao.getSupplierUpdatesMaker();

		FilterItem item = searchVals.getFilter(maker);
		if (StringUtils.isNotBlank(filter)) {
			PriceUpdateSearchFilters filters = (PriceUpdateSearchFilters) BeanUtils.getEnum(PriceUpdateSearchFilters.class, filter);
			item = maker.linkWithAnd(item, filters.getFilterItem(maker));
		}
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[3];
		sorts[0] = maker.makeSortItem("priority", true);
		sorts[1] = maker.makeSortItem("nextExecuteDate", true);
		sorts[2] = maker.makeSortItem("dataSkillLevelRequired", true);
		Pair<Long, List<SupplierUpdateExecute>> r = supplierDao.querySupplierUpdates(item, sorts, pageSize, pageNo);
		return r;
	}

	@Override
	public void saveSupplierUpdate(Supplier supplier, Schedule schedule, SupplierUpdateExecute formSupplierUpdateExecute) {
		SupplierUpdateExecute supplierUpdateExecute = getSupplierUpdateExecuteByScheduleId(schedule.getScheduleId().toString());
		if (supplierUpdateExecute == null) {
			supplierUpdateExecute = new SupplierUpdateExecute();
		}
		supplierUpdateExecute.setScheduleId(schedule.getScheduleId());
		supplierUpdateExecute.setDataSkillLevelRequired(supplier.getUpdateInfo().getDataSkillLevelRequired());
		supplierUpdateExecute.setEnable(schedule.isEnabled());
		if (formSupplierUpdateExecute.getNextExecuteDate() != null) {
			supplierUpdateExecute.setNextExecuteDate(formSupplierUpdateExecute.getNextExecuteDate());
		} else {
			if (supplierUpdateExecute.getRecentExecuteDate() == null) {
				supplierUpdateExecute.setNextExecuteDate(schedule.calcNextExecuteDate(DatePicker.getCalendarInstance().getTime()));
			} else {
				supplierUpdateExecute.setNextExecuteDate(schedule.calcNextExecuteDate(supplierUpdateExecute.getRecentExecuteDate()));
			}
		}
		supplierUpdateExecute.setPriority(supplier.getUpdateInfo().getPriority());
		supplierUpdateExecute.setSummary(supplier.getUpdateInfo().getSummary());
		supplierUpdateExecute.setSupplierId(supplier.getSupplierId());
		supplierUpdateExecute.setSupplierName(supplier.getName());

		supplierDao.saveSupplierUpdate(supplier, supplierUpdateExecute);
		scheduleDao.saveSchedule(schedule);
	}

	@Override
	public SupplierUpdateExecute getSupplierUpdateExecuteByScheduleId(String scheduleId) {
		return supplierDao.getSupplierUpdateExecuteByScheduleId(scheduleId);
	}

	@Override
	public void saveOperateSupplierUpdateInfo(SupplierUpdateExecute supplierUpdateExecute, ScheduleHistory scheduleHistory) {
		if (scheduleHistory != null) {
			scheduleDao.saveScheduleHistory(scheduleHistory);
		}
		if (supplierUpdateExecute != null) {
			supplierDao.saveSupplierUpdateExecute(supplierUpdateExecute);
		}
	}

	@Override
	public List<Map> querySuppliers4Notification(SupplierFilter4Notify filter) {
		// filter by reseller supplier map
		FilterMaker rsmMaker = resellerDao.getResellerSupplierMapMaker();
		FilterItem rsmFilterItem1 = filter.getFilterItem4RSMapSearch(rsmMaker);
		FilterItem rsmFilterItem2 = rsmMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ApprovalStatus.Pending.ordinal()));
		Iterable<ResellerSupplierMap> rsmlist = resellerDao.queryResellerSupplierMap(rsmMaker.linkWithAnd(rsmFilterItem1, rsmFilterItem2), null);
		Set<Integer> filterResIds = new HashSet<Integer>();
		if (rsmlist != null) {
			for (ResellerSupplierMap map : rsmlist) {
				filterResIds.add(map.getSupplierId());
			}
		}
		// recovery mode: remove item which was sent today
		Set<Integer> sentIdsToday = getNotifiedSupplierIdsToday(filter);
		// filter by reseller condition
		List<Map> result = new ArrayList<Map>();
		FilterMaker supMaker = supplierDao.getSupplierSearchMaker();
		FilterItem supFilterItem1 = filter.getFilter4SupplierSearch(supMaker);
		FilterItem supFilterItem2 = supMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.Active.ordinal()));
		Pair<String, Boolean>[] sorts = new Pair[] { supMaker.makeSortItem("shortName", true) };
		Iterable<SupplierSearchBean> suplist = supplierDao.querySupplierBeans(supMaker.linkWithAnd(supFilterItem1, supFilterItem2), sorts);
		for (SupplierSearchBean searchBean : suplist) {
			if (!filterResIds.contains(searchBean.getSupplierId()))
				continue;
			boolean sentToday = sentIdsToday.contains(searchBean.getSupplierId());
			if (filter.checkRecoveryMode() && sentToday)
				continue;
			Map map = new HashMap();
			map.put("supplierId", searchBean.getSupplierId());
			map.put("shortName", searchBean.getShortName());
			map.put("email", searchBean.getEmail() == null ? "" : searchBean.getEmail());
			map.put("email_cc", searchBean.getEmail_cc() == null ? "" : searchBean.getEmail_cc());
			map.put("sentToday", sentToday);
			result.add(map);
		}
		return result;
	}

	@Override
	public void sendNotifySupplierEmail(SupplierFilter4Notify filter, Set<String> includeIds, List<Map> datas) {
		UUID emailTypeId = filter.getEmailType();
		Assert.notNull(emailTypeId, "email type should not be null");
		EmailTemplate emailTemplate = emailTemplateService.getEmailTpl(emailTypeId.toString());

		Pair<String, File>[] attachements = cacheAttachments(emailTemplate);
		for (Map<String, Object> map : datas) {
			Integer supplierId = (Integer) map.get("supplierId");
			// Boolean sentToday = (Boolean) map.get("sentToday");
			if (includeIds.contains(supplierId.toString())) {
				String body = fillDataInNotifyMailBody(emailTemplate, supplierId);

				String email = (String) map.get("email");
				String shortName = (String) map.get("shortName");
				String email_cc = (String) map.get("email_cc");
				MailContent mailContent = new MailContent(emailTemplate.getSubject(), body, true).withTo(new MailAddress(email, shortName)).withCc(
						splitAddress(email_cc));
				if (StringUtils.isNotEmpty(filter.getSenderEmail()))
					mailContent.withFrom(new MailAddress(filter.getSenderEmail(), null));
				if (attachements != null && attachements.length > 0) {
					mailContent.withAttachment(attachements);
				}
				mailService.send(mailContent);

				EmailNotifyHistory notifyHistory = new EmailNotifyHistory(UUID.randomUUID(), new Date(), supplierId.toString(), "supplier");
				resellerDao.saveNotifyHistory(notifyHistory);
			}
		}

		cleanAttachments(attachements);

	}

	private MailAddress[] splitAddress(String rawMailAddrs) {
		if (rawMailAddrs == null)
			return null;
		List<MailAddress> addresses = new ArrayList<MailAddress>();
		for (String addr : rawMailAddrs.split(MailPreview.mailSeperator)) {
			if (addr == null)
				continue;
			addresses.add(new MailAddress(addr, null));
		}
		return addresses.toArray(new MailAddress[addresses.size()]);
	}

	private String fillDataInNotifyMailBody(EmailTemplate emailTemplate, Integer supplierId) {
		Map<String, String> tplData = new HashMap<String, String>();

		FilterMaker mapMaker = resellerDao.getResellerSupplierMapMaker();
		FilterItem mapFilterItem1 = mapMaker.makeNameFilter("supplierId", TextMatchOption.Is, supplierId.toString());

		FilterMaker resMaker = resellerDao.getResellerSearchMaker();
		FilterItem resFilterItem1 = resMaker.makePickFilter(
				"status",
				new String[] { String.valueOf(ResellerStatus.Setup.ordinal()), String.valueOf(ResellerStatus.Active.ordinal()),
						String.valueOf(ResellerStatus.OnHold.ordinal()) }, Type.Int);
		FilterItem resFilterItem2 = resMaker.makePickFilter(
				"type",
				new String[] { String.valueOf(ResellerType.Standard.ordinal()), String.valueOf(ResellerType.DataOnly.ordinal()),
						String.valueOf(ResellerType.Supplier.ordinal()) }, Type.Int);
		long count = calculateResellerCount4Notify(mapFilterItem1, resMaker.linkWithAnd(resFilterItem1, resFilterItem2));
		tplData.put("resellers.count", String.valueOf(count));

		FilterItem mapFilterItem2 = mapMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ApprovalStatus.Pending.ordinal()));
		count = calculateResellerCount4Notify(mapMaker.linkWithAnd(mapFilterItem1, mapFilterItem2),
				resMaker.linkWithAnd(resFilterItem1, resFilterItem2));
		tplData.put("resellers.count.pending", String.valueOf(count));

		mapFilterItem2 = mapMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ApprovalStatus.Approved.ordinal()));
		count = calculateResellerCount4Notify(mapMaker.linkWithAnd(mapFilterItem1, mapFilterItem2),
				resMaker.linkWithAnd(resFilterItem1, resFilterItem2));
		tplData.put("resellers.count.approved", String.valueOf(count));

		tplData.put("web.address", GlobalAttrManager.getClientInfo().getWebappBaseUrl());
		String body = Formatter.DollarBraceName.format(emailTemplate.getBody(), tplData);
		return body;
	}

	private long calculateResellerCount4Notify(FilterItem mapFilterItem, FilterItem resFilterItem) {
		Iterable<ResellerSupplierMap> it1 = resellerDao.queryResellerSupplierMap(mapFilterItem, null);
		Set<Integer> filterResIds = new HashSet<Integer>();
		for (ResellerSupplierMap map : it1) {
			filterResIds.add(map.getSerialNo());
		}
		Iterable<String> it2 = resellerDao.queryResellerKeys(resFilterItem);
		long count = 0l;
		for (String s : it2) {
			if (filterResIds.contains(Integer.valueOf(s)))
				count++;
		}
		return count;
	}

	private void cleanAttachments(Pair<String, File>[] attachements) {
		if (attachements != null) {
			for (Pair<String, File> filePair : attachements) {
				filePair.getValue().delete();
			}
		}
	}

	private Pair<String, File>[] cacheAttachments(EmailTemplate emailTemplate) {
		Pair<String, File>[] attachements = null;
		if (emailTemplate.getAttachments() != null) {
			List<Pair<String, File>> attachementsTemp = new ArrayList<Pair<String, File>>();
			for (String attachmentKey : emailTemplate.getAttachments()) {
				File file = fileService.cacheFile(attachmentKey);
				attachementsTemp.add(new Pair<String, File>(file.getName(), file));
			}
			attachements = attachementsTemp.toArray(new Pair[attachementsTemp.size()]);
		}
		return attachements;
	}

	private Set<Integer> getNotifiedSupplierIdsToday(SupplierFilter4Notify filter) {
		Set<Integer> sentIdsToday = new HashSet<Integer>();
		FilterMaker historyMaker = emailDao.getNotifyHistoryMaker();
		Date start = DatePicker.pickDay(new Date(), 0);
		Date end = DateUtils.addDays(start, 1);
		FilterItem historyQItem = historyMaker.makeDateRange("date", start, end, true, false);
		historyQItem = historyMaker.linkWithAnd(historyMaker.makeNameFilter("type", TextMatchOption.Is, "supplier"), historyQItem);
		if (filter.getEmailType() != null) {
			historyQItem = historyMaker.linkWithAnd(
					historyMaker.makeNameFilter("notifyTypeId", TextMatchOption.Is, filter.getEmailType().toString()), historyQItem);
		}

		List<EmailNotifyHistory> histories = emailDao.queryNotifyHistory(historyQItem, null);
		if (CollectionUtils.isNotEmpty(histories)) {
			for (EmailNotifyHistory history : histories) {
				sentIdsToday.add(Integer.valueOf(history.getRawId()));
			}
		}
		return sentIdsToday;
	}

	@Override
	public List<Map<String, String>> querySupplierPortalResellerMap(SupplierPortalResellerMapVals searchVals, String filter) {

		FilterMaker rsmMaker = resellerDao.getResellerSupplierMapMaker();
		FilterItem rsmFilterItem = searchVals.getFilterItem4RSMapSearch(rsmMaker);
		if (StringUtils.isNotBlank(filter)) {
			SupplierPortalResellerMapFilters filters = (SupplierPortalResellerMapFilters) BeanUtils.getEnum(SupplierPortalResellerMapFilters.class,
					filter);
			rsmFilterItem = rsmMaker.linkWithAnd(rsmFilterItem, filters.getFilterItem(rsmMaker));
		}
		List<ResellerSupplierMap> rsmlist = resellerDao.queryResellerSupplierMap(rsmFilterItem, null, null, null).getValue();

		Map<Integer, ResellerSupplierMap> filterResMaps = new HashMap<Integer, ResellerSupplierMap>();
		if (CollectionUtils.isNotEmpty(rsmlist)) {
			for (ResellerSupplierMap map : rsmlist) {
				filterResMaps.put(map.getSerialNo(), map);
			}
		}
		// filter by reseller condition
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		FilterMaker resMaker = resellerDao.getResellerSearchMaker();
		FilterItem resFilterItem = searchVals.getFilter4ResellerSearch(resMaker);
		resFilterItem = resMaker.linkWithAnd(resMaker.linkWithOr(
				resMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.Setup.ordinal())),
				resMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.Active.ordinal())),
				resMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.OnHold.ordinal()))));

		resFilterItem = resMaker.linkWithAnd(resMaker.linkWithOr(
				resMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.Standard.ordinal())),
				resMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.DataOnly.ordinal())),
				resMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.Supplier.ordinal()))));

		Pair<String, Boolean>[] sorts = new Pair[] { resMaker.makeSortItem("company", true) };
		Iterable<ResellerSearchBean> reslist = resellerDao.queryResellerBeans(resFilterItem, sorts);
		ResellerSupplierMap rsMap = null;
		for (ResellerSearchBean searchBean : reslist) {
			if (!filterResMaps.containsKey(searchBean.getSerialNo()))
				continue;
			rsMap = filterResMaps.get(searchBean.getSerialNo());
			Map<String, String> map = new HashMap<String, String>();
			map.put("company", searchBean.getCompany() == null ? "" : searchBean.getCompany());
			map.put("account", rsMap.getSupplierAccountNumber() == null ? "" : rsMap.getSupplierAccountNumber());
			map.put("contact", searchBean.getContact() == null ? "" : searchBean.getContact());
			map.put("phone", searchBean.getPhone() == null ? "" : searchBean.getPhone());
			map.put("status", rsMap.getStatus().toString());
			map.put("breakName", rsMap.getBreakName() == null ? "" : rsMap.getBreakName());
			map.put("dateGranted", rsMap.getApprovalDate() == null ? "" : DatePicker.getFormatedDate(rsMap.getApprovalDate()));
			map.put("priceBreakId", rsMap.getPriceBreakId().toString());
			map.put("serialNo", rsMap.getSerialNo().toString());

			result.add(map);
		}
		return result;
	}

	@Override
	public Pair<Long, List<SupplierSearchBean>> queryNotAllocatedSuppliers(int serialNo,ResellerPortalSupplierSearchVals searchVals, int pageNo, int pageSize) {
		// map
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();
		FilterItem item1 = maker.makeNameFilter("serialNo", TextMatchOption.Is, String.valueOf(serialNo));
		Set<Integer> mapedSids = new HashSet<Integer>();
		for (ResellerSupplierMap bean : resellerDao.queryResellerSupplierMap(item1, null)) {
			mapedSids.add(bean.getSupplierId());
		}
		// all supplier
		String country = resellerDao.getReseller(serialNo).getCountry();
		FilterMaker sMaker = supplierDao.getSupplierSearchMaker();
		FilterItem item2 = sMaker.makeNameFilter("country", TextMatchOption.Is, country);
		FilterItem item3 = sMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(Status.Disabled.ordinal()));
		FilterItem item4=searchVals.getFilter(sMaker);
//		List<Map<String, String>> r = new ArrayList<Map<String, String>>();
		
//		for (SupplierSearchBean bean : supplierDao.querySupplierBeans(sMaker.linkWithAnd(item2, item3,item4), new Pair[] { new Pair("name", true) })) {
//			if (mapedSids.contains(bean.getSupplierId()))
//				continue;
//
//			Map<String, String> m = new HashMap<String, String>();
//			m.put("id", String.valueOf(bean.getSupplierId()));
//			m.put("name", bean.getName());
//			m.put("country", addressService.getAddressItem(bean.getCountry()).getName());
//			// StringBuilder builder = getBranches(bean.getSupplierId());
//			// m.put("branches", builder.toString());
//			m.put("phone", bean.getPhone());
//			Supplier supplier = getSupplier(bean.getSupplierId());
//			m.put("website", supplier.getWebAddress());
//			m.put("overview", supplier.getMarketingSummary());
//			// m.put("status", supplier.getStatus().name());
//			r.add(m);
//		}
		
		return supplierDao.querySupplierMapsWithExcludeIds(sMaker.linkWithAnd(item2, item3,item4), mapedSids, new Pair[] { new Pair("name", true) }, pageSize, pageNo);
	}

	private StringBuilder getBranches(int supplierId) {
		List<SupplierBranch> branchs = queryBranches(null, supplierId, null, null).getValue();
		StringBuilder builder = new StringBuilder();
		if (branchs != null) {
			for (SupplierBranch branch : branchs) {
				if (builder.length() > 0)
					builder.append(",");
				builder.append(branch.getName());
			}
		}
		return builder;
	}
	
	@Override
	public int[] statisticRsMap4ResellerPortal(int serialNo){
		return supplierDao.statisticRsMap4ResellerPortal(serialNo);
	}
}
