package au.com.isell.rlm.module.reseller.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.data.CSVReader;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.message.MessageChecker;
import au.com.isell.common.util.Formatter;
import au.com.isell.common.util.IDServerClient;
import au.com.isell.common.util.NetworkUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.exception.BizException;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.jbe.service.JobService;
import au.com.isell.rlm.module.mail.dao.EmailDao;
import au.com.isell.rlm.module.mail.domain.EmailNotifyHistory;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;
import au.com.isell.rlm.module.mail.service.EmailTemplateService;
import au.com.isell.rlm.module.mail.service.MailService;
import au.com.isell.rlm.module.mail.vo.MailAddress;
import au.com.isell.rlm.module.mail.vo.MailContent;
import au.com.isell.rlm.module.mail.vo.MailPreview;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.ManualFile;
import au.com.isell.rlm.module.reseller.domain.PendingDataFile;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMapHistory;
import au.com.isell.rlm.module.reseller.domain.ResellerUsage;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.reseller.vo.RSMapSearchFilters4ResellerPortal;
import au.com.isell.rlm.module.reseller.vo.ResellerFilter4Notify;
import au.com.isell.rlm.module.reseller.vo.ResellerSearchFilters;
import au.com.isell.rlm.module.reseller.vo.ResellerSearchVals;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchFilters;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchVals;
import au.com.isell.rlm.module.supplier.dao.SupplierDao;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.upload.service.FileService;
import au.com.isell.rlm.module.user.constant.UserType;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.PermissionService;
import au.com.isell.rlm.module.user.service.UserService;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.json.JSONArray;
import com.google.gson.Gson;

@Service
public class ResellerServiceImpl implements ResellerService {
	private static Logger logger = LoggerFactory.getLogger(ResellerServiceImpl.class);
	public static S3Manager s3Manager = S3Manager.getInstance();
	private static String updateServer;
	private static String resellerManualRoot;

	@Autowired
	private UserDAO userDAO;
	@Autowired
	private ResellerDao resellerDao;
	@Autowired
	private SupplierDao supplierDao;
	@Autowired
	private SupplierService supplierService;
	@Autowired
	private UserService userService;
	@Autowired
	private AddressService addressService;
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

	private JobService jobService;

	@Autowired
	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	static {
		try {
			Properties bundle = new Properties();
			bundle.load(ResellerServiceImpl.class.getResourceAsStream("/settings.properties"));
			updateServer = bundle.getProperty("apollo.updateServer");
			resellerManualRoot = bundle.getProperty("s3.manual.files.reseller");
		} catch (Exception ex) {
		}
	}

	@Override
	public Reseller getReseller(int serialNo) {
		return resellerDao.getReseller(serialNo);
	}

	/** for freemarker */
	@Override
	public Reseller getReseller(String serialNo) {
		return resellerDao.getReseller(Integer.valueOf(serialNo));
	}

	@Override
	public ResellerUser getResellerUser(UUID userId) {
		return (ResellerUser) userDAO.getUserById(userId);
	}

	/**
	 * serialNo==-1 means to create
	 */
	@Override
	public int saveDetail(int serialNo, Reseller reseller, ResellerUser user) {
		user.setPrimary(true);
		user.setPermissions(permissionService.getResellerPortalPermissions());

		if (serialNo == -1) {
			int newSerialNo = createNewSerialNo(reseller);
			if (reseller.getRegKey() == null)
				reseller.setRegKey(generateRegKey(newSerialNo));
			reseller.setSerialNo(newSerialNo);
			user.setUserId(UUID.randomUUID());
			user.setSerialNo(newSerialNo);
			user.setUsername(user.getToken());
			reseller.setUserId(user.getUserId());
			resellerDao.save(true, reseller);
			checkResellerUserPermission(reseller);
			userDAO.saveUser(user);
		} else {
			Reseller dbReseller = resellerDao.getReseller(serialNo);
			reseller.setUserId(user.getUserId());
			BeanUtils.copyPropsExcludeNull(dbReseller, reseller);
			reseller = dbReseller;
			resellerDao.save(true, reseller);

			ResellerUser dbResellerUser = getResellerUser(dbReseller.getUserId());
			user.setSerialNo(reseller.getSerialNo());
			user.setUsername(user.getToken());
			BeanUtils.copyPropsExcludeNull(dbResellerUser, user);
			
			checkResellerUserPermission(reseller);
			userDAO.saveUser(dbResellerUser);
		}
		try {
			MessageChecker.getInstance("rlm").sendMessages(reseller);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		return reseller.getSerialNo();
	}

	private void checkResellerUserPermission(Reseller reseller) {
		if(ResellerType.TestLicense.equals(reseller.getType())){
			Pair<Long, List<ResellerUser>> r = userService.queryResellerUser(null, null, reseller.getSerialNo().toString(), null, 1, 500);
			if(r.getValue()!=null && r.getValue().size()>0){
				ResellerUser dbResellerUser = null;
				for(ResellerUser ru : r.getValue()){
					dbResellerUser = getResellerUser(ru.getUserId());
					Set<String> permissions = dbResellerUser.getPermissions();
					if(permissions!=null){
						permissions.remove("resellerportal:view_job");
						permissions.remove("resellerportal:create_edit_job");
						dbResellerUser.setPermissions(permissions);
						userDAO.saveUser(dbResellerUser);
					}
				}
			}
		}
	}

	private String generateRegKey(int serialNo) {
		return IDServerClient.getRegKey("reseller", serialNo);
	}

	private Integer createNewSerialNo(Reseller formReseller) {
		Integer newSerialNo;
		newSerialNo = IDServerClient.getIntId("reseller_" + formReseller.getCountry(), 1)[0];
		AddressItem addressItem = addressService.getAddressItem(formReseller.getCountry());
		int phoneAreaCodeBind = addressItem.getPhoneAreaCodeBind();
		return (int) Math.pow(10, 7 - String.valueOf(phoneAreaCodeBind).length()) * phoneAreaCodeBind + newSerialNo;
	}

	@Override
	public long[] getStatisticsByStatus() {
		return resellerDao.getStatisticsByStatus();
	}

	@Override
	public List<Pair<Pair<String, String>, List<Pair<String, Long>>>> getStatisticsByCountry() {
		List<Pair<Pair<String, String>, List<Pair<String, Long>>>> result = new ArrayList();
		Map<String, Map<Integer, Long>> tempR = resellerDao.getStatisticsByCountry();
		Map<Integer, Long> totalCounts = new HashMap<Integer, Long>();
		for (Map.Entry<String, Map<Integer, Long>> entry : tempR.entrySet()) {
			List<Pair<String, Long>> group = new ArrayList<Pair<String, Long>>();
			for (ResellerStatus status : ResellerStatus.values()) {
				Long val = entry.getValue().get(status.ordinal());
				val = val == null ? 0l : val;
				group.add(new Pair<String, Long>(String.valueOf(status.ordinal()), val));
				calTotal(totalCounts, status, val);
			}
			AddressItem country = addressService.getAddressItem(entry.getKey());
			if (country != null)
				result.add(new Pair<Pair<String, String>, List<Pair<String, Long>>>(new Pair(country.getCode(), country.getName()), group));
		}
		Collections.sort(result, new Comparator<Pair<Pair<String, String>, List<Pair<String, Long>>>>() {
			@Override
			public int compare(Pair<Pair<String, String>, List<Pair<String, Long>>> o1, Pair<Pair<String, String>, List<Pair<String, Long>>> o2) {
				return o1.getKey().getKey().compareTo(o2.getKey().getKey());
			}
		});
		// add total
		addTotalCounts(result, totalCounts);
		return result;
	}

	private void addTotalCounts(List<Pair<Pair<String, String>, List<Pair<String, Long>>>> result, Map<Integer, Long> totalCounts) {
		List<Pair<String, Long>> group = new ArrayList<Pair<String, Long>>();
		for (ResellerStatus status : ResellerStatus.values()) {
			Long val = totalCounts.get(status.ordinal());
			group.add(new Pair<String, Long>(String.valueOf(status.ordinal()), val));
		}
		result.add(new Pair<Pair<String, String>, List<Pair<String, Long>>>(new Pair("", "Total"), group));
	}

	private void calTotal(Map<Integer, Long> totalCounts, ResellerStatus status, Long val) {
		Long total = totalCounts.get(status.ordinal());
		total = total == null ? 0 : total;
		total = total + val;
		totalCounts.put(status.ordinal(), total);
	}

	@Override
	public Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMaps(String filter, int serialNo, ResellerSupplierMapSearchVals searchVals, int pageSize, int pageNo) {
		Assert.isTrue(pageSize > 0, "pageSize should not be null");
		Assert.isTrue(pageNo > 0, "pageNo should not be null");
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();

		FilterItem item1 = searchVals.getFilter(maker);
		FilterItem item2 = null;
		if (StringUtils.isNotBlank(filter)) {
			ResellerSupplierMapSearchFilters filters = (ResellerSupplierMapSearchFilters) BeanUtils.getEnum(ResellerSupplierMapSearchFilters.class, filter);
			item2 = filters.getFilterItem(maker);
		}
		FilterItem item3 = maker.makeNameFilter("serialNo", TextMatchOption.Is, String.valueOf(serialNo));

		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("supplierName", true);
		Pair<Long, List<ResellerSupplierMap>> r = resellerDao.queryResellerSupplierMap(maker.linkWithAnd(item1, item2, item3), sorts, pageSize, pageNo);
		return r;
	}

	@Override
	public Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMaps4ResellerPortal(String filter, int serialNo, int pageSize, int pageNo) {
		Assert.isTrue(pageSize > 0, "pageSize should not be null");
		Assert.isTrue(pageNo > 0, "pageNo should not be null");
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();

		// FilterItem item1 = searchVals.getFilter(maker);
		FilterItem item2 = null;
		if (StringUtils.isNotBlank(filter)) {
			RSMapSearchFilters4ResellerPortal filters = (RSMapSearchFilters4ResellerPortal) BeanUtils.getEnum(RSMapSearchFilters4ResellerPortal.class, filter);
			item2 = filters.getFilterItem(maker);
		}
		FilterItem item3 = maker.makeNameFilter("serialNo", TextMatchOption.Is, String.valueOf(serialNo));

		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("supplierName", true);
		Pair<Long, List<ResellerSupplierMap>> r = resellerDao.queryResellerSupplierMap(maker.linkWithAnd(item2, item3), sorts, pageSize, pageNo);
		return r;
	}

	@Override
	public List<Map> queryReseller4Notification(ResellerFilter4Notify filter) {
		// filter by reseller supplier map
		FilterMaker rsmMaker = resellerDao.getResellerSupplierMapMaker();
		FilterItem rsmFilterItem1 = filter.getFilterItem4RSMapSearch(rsmMaker);
		Iterable<ResellerSupplierMap> rsmlist = resellerDao.queryResellerSupplierMap(rsmFilterItem1, null);
		Set<Integer> filterResIds = new HashSet<Integer>();
		if (rsmlist != null) {
			for (ResellerSupplierMap map : rsmlist) {
				filterResIds.add(map.getSerialNo());
			}
		}
		// recovery mode: remove item which was sent today
		Set<Integer> sentIdsToday = getNotifiedResellerIdsToday(filter);
		// filter by reseller condition
		List<Map> result = new ArrayList<Map>();
		FilterMaker resMaker = resellerDao.getResellerSearchMaker();
		FilterItem resFilterItem = filter.getFilter4ResellerSearch(resMaker);
		Pair<String, Boolean>[] sorts = new Pair[] { resMaker.makeSortItem("company", true) };
		Iterable<ResellerSearchBean> reslist = resellerDao.queryResellerBeans(resFilterItem, sorts);
		for (ResellerSearchBean searchBean : reslist) {
			if (!filterResIds.contains(searchBean.getSerialNo()))
				continue;
			boolean sentToday = sentIdsToday.contains(searchBean.getSerialNo());
			if (filter.checkRecoveryMode() && sentToday)
				continue;
			Map map = new HashMap();
			map.put("serialNo", searchBean.getSerialNo());
			map.put("company", searchBean.getCompany());
			map.put("email", searchBean.getEmail() == null ? "" : searchBean.getEmail());
			map.put("email_cc", searchBean.getEmail_cc() == null ? "" : searchBean.getEmail_cc());
			map.put("sentToday", sentToday);
			result.add(map);
		}
		return result;
	}

	private Set<Integer> getNotifiedResellerIdsToday(ResellerFilter4Notify filter) {
		Set<Integer> sentIdsToday = new HashSet<Integer>();
		FilterMaker historyMaker = emailDao.getNotifyHistoryMaker();
		Date start = DatePicker.pickDay(new Date(), 0);
		Date end = DateUtils.addDays(start, 1);
		FilterItem historyQItem = historyMaker.makeDateRange("date", start, end, true, false);
		historyQItem = historyMaker.linkWithAnd(historyMaker.makeNameFilter("type", TextMatchOption.Is, "reseller"), historyQItem);
		if (filter.getEmailType() != null) {
			historyQItem = historyMaker.linkWithAnd(historyMaker.makeNameFilter("notifyTypeId", TextMatchOption.Is, filter.getEmailType().toString()), historyQItem);
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
	public void deleteResellerSupplierMap(Integer serialNo, Integer supplierId) {
		resellerDao.deleteResellerSupplierMap(serialNo, supplierId);
		deleteResellerSupplierMapHistories(serialNo, supplierId);
	}

	@Override
	public void deleteResellerSupplierMapHistories(Integer serialNo, Integer supplierId) {
		List<ResellerSupplierMapHistory> histories = getResellerSupplierMapHistories(serialNo, supplierId);
		if (histories != null && histories.size() > 0) {
			resellerDao.deleteResellerSupplierMapHistories(histories.toArray(new ResellerSupplierMapHistory[histories.size()]));
		}
	}

	@Override
	public List<PendingDataFile> getPendingDownloadDataFiles(int serialNo) {
		Reseller reseller = getReseller(serialNo);
		String regKey = reseller.displayRegKey();
		if (regKey.length() == 0)
			return new ArrayList<PendingDataFile>();
		String urlStr = "http://" + updateServer + "/NSupplierSummary";
		Properties param = new Properties();
		param.setProperty("registerkey", regKey);
		InputStream in = null;
		try {
			in = NetworkUtils.makeHTTPGetRequest(urlStr, param, null);
			CSVReader reader = new CSVReader(new InputStreamReader(in), '\t', '"', true, false);
			List<PendingDataFile> list = new ArrayList<PendingDataFile>();
			for (String[] line = reader.readLine(); line != null; line = reader.readLine()) {
				PendingDataFile f = new PendingDataFile();
				f.setSupplierId(line[1]);
				f.setSupplierName(line[2]);
				f.setFilename(line[5]);
				f.setUrl("http://" + updateServer + line[5]);
				String ts = line[5].substring(line[5].indexOf('/', 2) + 1);
				ts = ts.substring(0, ts.indexOf('/'));
				f.setCreateDate(new Date(Long.parseLong(ts)));
				list.add(f);
			}
			return list;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public List<PendingDataFile> getPendingDownloadImageFiles(int serialNo) {
		Reseller reseller = getReseller(serialNo);
		String regKey = reseller.displayRegKey();
		String urlStr = "http://" + updateServer + "/NResourceSummary";
		if (regKey.length() == 0)
			return new ArrayList<PendingDataFile>();
		Properties param = new Properties();
		param.setProperty("registerkey", regKey);
		InputStream in = null;
		try {
			in = NetworkUtils.makeHTTPGetRequest(urlStr, param, null);
			CSVReader reader = new CSVReader(new InputStreamReader(in), '\t', '"', true, false);
			List<PendingDataFile> list = new ArrayList<PendingDataFile>();
			for (String[] line = reader.readLine(); line != null; line = reader.readLine()) {
				PendingDataFile f = new PendingDataFile();
				f.setSupplierId(line[1]);
				f.setSupplierName(line[2]);
				f.setFilename(line[5]);
				f.setUrl("http://" + updateServer + line[5]);
				String ts = line[5].substring(line[5].indexOf('/', 2) + 1);
				ts = ts.substring(0, ts.indexOf('/'));
				f.setCreateDate(new Date(Long.parseLong(ts)));
				list.add(f);
			}
			return list;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public ResellerSupplierMap getResellerSupplierMap(Integer serialNo, Integer supplierId) {
		Assert.notNull(serialNo);
		Assert.notNull(supplierId);
		return resellerDao.getResellerSupplierMap(serialNo, supplierId);
	}

	@Override
	public void saveResellerSupplierMapFromForm(ResellerSupplierMap formMap, int oldSerialNo, int oldSupplierId, boolean sync) {
		ResellerSupplierMap toSaveMap = null;
		boolean isEmail = false, haveHistory = false;
		if (oldSerialNo < 0 || oldSupplierId < 0) {// save
			toSaveMap = formMap;
			if (ApprovalStatus.Approved.equals(toSaveMap.getStatus()) || ApprovalStatus.Disabled.equals(toSaveMap.getStatus())) {
				haveHistory = true;
				isEmail = true;
			}
		} else {// update
			Assert.notNull(oldSerialNo);
			Assert.notNull(oldSupplierId);
			ResellerSupplierMap dbmap = resellerDao.getResellerSupplierMap(Integer.valueOf(oldSerialNo), Integer.valueOf(oldSupplierId));
			ApprovalStatus oldStatus = dbmap.getStatus();
			BeanUtils.copyPropsExcludeNull(dbmap, formMap);
			toSaveMap = dbmap;

			if (oldSerialNo != formMap.getSerialNo() || oldSupplierId != formMap.getSupplierId()) {
				resellerDao.deleteResellerSupplierMap(oldSerialNo, oldSupplierId);
				oldStatus = null;
			}
			if (ApprovalStatus.Approved.equals(toSaveMap.getStatus()) && !ApprovalStatus.Approved.equals(oldStatus)) {
				haveHistory = true;
				isEmail = true;
			} else if (ApprovalStatus.Disabled.equals(toSaveMap.getStatus()) && !ApprovalStatus.Disabled.equals(oldStatus)) {
				haveHistory = true;
				isEmail = true;
			}
		}
		if (!ApprovalStatus.Approved.equals(toSaveMap.getStatus())) {
			toSaveMap.setApprovalDate(null);
		}
		if (haveHistory) {
			if (ApprovalStatus.Approved.equals(toSaveMap.getStatus())) {
				toSaveMap.setApprovalDate(new Date());
				toSaveMap.setDisabledDate(null);
				saveResellerSupplierMapHistroy(toSaveMap, "Approval");
			} else if (ApprovalStatus.Disabled.equals(toSaveMap.getStatus())) {
				toSaveMap.setApprovalDate(null);
				toSaveMap.setDisabledDate(new Date());
				saveResellerSupplierMapHistroy(toSaveMap, "Decline");
			}
		}
		resellerDao.saveResellerSupplierMap(toSaveMap);
		if (isEmail) {
			sendSupplierPortalResellerMapEmail(toSaveMap);
		}

		if (sync) {
			try {
				MessageChecker.getInstance("rlm").sendMessages(toSaveMap);
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	private void sendSupplierPortalResellerMapEmail(ResellerSupplierMap toSaveMap) {
		if (UserType.Supplier.equals(GlobalAttrManager.getCurrentUser().getType())) {
			String subject = "";
			String body = "";
			if (ApprovalStatus.Approved.equals(toSaveMap.getStatus())) {
				subject = "Approve notify: supplier " + toSaveMap.getSupplierName();
				body = " Supplier " + toSaveMap.getSupplierName() + " has approved reseller " + toSaveMap.getCompany() + " on price break " + toSaveMap.getBreakName();
			} else if (ApprovalStatus.Disabled.equals(toSaveMap.getStatus())) {
				subject = "Decline notify: supplier " + toSaveMap.getSupplierName();
				body = " Supplier " + toSaveMap.getSupplierName() + " has declined reseller " + toSaveMap.getCompany() + " because of:<br> " + toSaveMap.getSupplierDisableComment();
			}
			MailContent mailContent = new MailContent(subject, body, false);
			// datasupport@isell.com.au
			mailContent.withTo(new MailAddress("datasupport@isell.com.au", null));
			mailService.send(mailContent);
		}

	}

	private void saveResellerSupplierMapHistroy(ResellerSupplierMap toSaveMap, String operate) {
		User currentUser = GlobalAttrManager.getCurrentUser();
		ResellerSupplierMapHistory history = new ResellerSupplierMapHistory();
		history.setDate(new Date());
		history.setOperate(operate);
		history.setSupplierId(toSaveMap.getSupplierId());
		history.setSerialNo(toSaveMap.getSerialNo());
		history.setVia(GlobalAttrManager.getClientInfo().getIpAddress());
		history.setUserName(currentUser.getUsername());
		history.setUserId(currentUser.getUserId());
		resellerDao.saveResellerSupplierMapHistory(history);
	}

	@Override
	public Pair<Long, List<ResellerSearchBean>> query(ResellerSearchVals searchVals, String filter, Integer pageSize, Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = resellerDao.getResellerSearchMaker();

		FilterItem item = searchVals.getFilter(maker);
		if (StringUtils.isNotBlank(filter)) {
			ResellerSearchFilters filters = (ResellerSearchFilters) BeanUtils.getEnum(ResellerSearchFilters.class, filter);
			item = maker.linkWithAnd(item, filters.getFilterItem(maker));
		}
		if (CollectionUtils.isNotEmpty(permissionService.getAgentsOfResellerPerm())) {
			List<FilterItem> agentFiltes = new ArrayList<FilterItem>();
			for (String id : permissionService.getAgentsOfResellerPerm()) {
				agentFiltes.add(maker.makeNameFilter("agencyId", TextMatchOption.Is, id));
			}
			item = maker.linkWithAnd(item, maker.linkWithOr(agentFiltes.toArray(new FilterItem[agentFiltes.size()])));
		} else {
			return new Pair<Long, List<ResellerSearchBean>>(0l, null);
		}

		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("company", true);
		Pair<Long, List<ResellerSearchBean>> r = resellerDao.query(item, sorts, pageSize, pageNo);
		return r;
	}

	@Override
	public List<ResellerSearchBean> queryAllReseller() {
		FilterMaker maker = resellerDao.getResellerSearchMaker();
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("company", true);
		return resellerDao.query(maker.makeAllQuery(), sorts, null, null).getValue();
	}

	@Override
	public List<String[]> getResellerSystemInfo(int serialNo) {
		ResellerUsage usage = resellerDao.getReseller(serialNo).getUsage();
		String systemInfo = usage == null ? null : usage.getSystemInfo();

		List<String[]> result = null;
		// System.out.println(systemInfo);
		if (StringUtils.isNotBlank(systemInfo)) {
			result = new ArrayList<String[]>();
			ByteArrayInputStream bis = new ByteArrayInputStream(systemInfo.getBytes());
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			String line = null;
			StringBuffer keyValue = new StringBuffer();
			StringBuffer sbValue = new StringBuffer();
			int lastIndex = 0;
			try {
				while ((line = br.readLine()) != null) {
					if (StringUtils.isNotBlank(line)) {
						if (!Character.isSpaceChar(line.charAt(0))) {
							result.add(new String[] { keyValue.toString(), sbValue.toString() });
							keyValue.setLength(0);
							sbValue.setLength(0);
							lastIndex = line.lastIndexOf(": ");
							keyValue.append(line.substring(0, lastIndex).trim());
							sbValue.append(line.substring(lastIndex + 1).trim());
						} else {
							sbValue.append("\n");
							sbValue.append(line.trim());
						}
					}
					if (!br.ready()) {
						result.add(new String[] { keyValue.toString(), sbValue.toString() });
						keyValue.setLength(0);
						sbValue.setLength(0);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			result.remove(0);
		}
		return result;
	}

	private static final SimpleDateFormat SDF_TRANSFER = new SimpleDateFormat("yyyyMMddHHmmss");
	private Gson gson = new Gson();

	@Override
	public void syncAllResellerInfoExchangeDate() {
		String urlStr = "http://" + updateServer + "/services/GetResellerSyncTime";
		Properties param = new Properties();
		Reader in = null;
		List<Reseller> syncResellers = new ArrayList<Reseller>();
		try {
			in = new InputStreamReader(NetworkUtils.makeHTTPGetRequest(urlStr, param, null), "UTF-8");
			// parse json content
			@SuppressWarnings("unchecked")
			Map<Object, Object> info = gson.fromJson(in, Map.class);
			// setup reseller synctime
			for (Map.Entry<Object, Object> entry : info.entrySet()) {
				Reseller rs = resellerDao.getReseller(((Double) entry.getKey()).intValue());
				try {
					rs.setLastSyncDate(SDF_TRANSFER.parse((String) entry.getValue()));
					syncResellers.add(rs);
				} catch (ParseException e) {
				}
			}
			resellerDao.save(true, syncResellers.toArray(new Reseller[syncResellers.size()]));
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);// TODO throw later..
			// throw new RuntimeException(ex);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public boolean isDuplicateResellerSupplierMap(int serialNo, int supplierId) {
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();
		FilterItem item1 = maker.makeNameFilter("supplierId", TextMatchOption.Is, String.valueOf(supplierId));
		FilterItem item2 = maker.makeNameFilter("serialNo", TextMatchOption.Is, String.valueOf(serialNo));
		long count = resellerDao.getResellerSupplierMapCount(maker.linkWithAnd(item1, item2));
		if (count > 0) {
			return true;
		}
		return false;
	}

	@Override
	public Date getLatestRenewDate(int serialNo) {
		Reseller reseller = getReseller(serialNo);
		Map<LicenseType, LicModule> licenses = reseller.getLicenses();
		Date date = null;
		if (licenses == null)
			return null;
		for (LicModule lic : licenses.values()) {
			if (lic == null)
				continue;
			if (date == null)
				date = lic.getRenewalDate();
			else if (lic.getRenewalDate() != null && date.after(lic.getRenewalDate()))
				date = lic.getRenewalDate();
		}
		return date;
	}

	@Override
	public void sendNotifyResellerEmail(ResellerFilter4Notify filter, Set<String> ids, List<Map> datas) {
		UUID emailTypeId = filter.getEmailType();
		Assert.notNull(emailTypeId, "email type should not be null");
		EmailTemplate emailTemplate = emailTemplateService.getEmailTpl(emailTypeId.toString());

		Pair<String, File>[] attachements = cacheAttachments(emailTemplate);

		for (Map<String, Object> map : datas) {
			Integer serialNo = (Integer) map.get("serialNo");
			// Boolean sentToday = (Boolean) map.get("sentToday");
			if (!ids.contains(serialNo.toString()))
				continue;
			String email = (String) map.get("email");
			String company = (String) map.get("company");
			String email_cc = (String) map.get("email_cc");

			String mailBody = formatNotifyMailBody(emailTemplate.getBody(), serialNo);

			MailContent mailContent = new MailContent(emailTemplate.getSubject(), mailBody, true).withTo(new MailAddress(email, company)).withCc(splitAddress(email_cc));
			String senderEmail = filter.getSenderEmail();
			if (StringUtils.isNotEmpty(senderEmail))
				mailContent.withFrom(new MailAddress(senderEmail, null));
			if (attachements != null && attachements.length > 0) {
				mailContent.withAttachment(attachements);
			}
			mailService.send(mailContent);

			EmailNotifyHistory notifyHistory = new EmailNotifyHistory(UUID.randomUUID(), new Date(), serialNo.toString(), "reseller");
			resellerDao.saveNotifyHistory(notifyHistory);
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

	private String formatNotifyMailBody(String mailBody, Integer serialNo) {
		Reseller reseller = getReseller(serialNo);
		Map<String, String> replacemetns = new HashMap<String, String>();
		replacemetns.put("custfirstname", reseller.getBillingInfo().getFirstName());
		replacemetns.put("custlastname", reseller.getBillingInfo().getLastName());
		replacemetns.put("custfullname", reseller.getBillingInfo().getFirstName() + " " + reseller.getBillingInfo().getLastName());
		replacemetns.put("custserialnumber", serialNo.toString());
		replacemetns.put("web.address", GlobalAttrManager.getClientInfo().getWebappBaseUrl());
		return Formatter.DollarBraceName.format(mailBody, replacemetns);
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

	@Override
	public List<ResellerSupplierMapHistory> getResellerSupplierMapHistories(Integer serialNo, Integer supplierId) {
		FilterMaker maker = resellerDao.getResellerSupplierMapHistoryMaker();
		FilterItem item1 = maker.makeNameFilter("supplierId", TextMatchOption.Is, supplierId.toString());
		FilterItem item2 = maker.makeNameFilter("serialNo", TextMatchOption.Is, serialNo.toString());
		return resellerDao.getResellerSupplierMapHistories(maker.linkWithAnd(item1, item2));
	}

	@Override
	public List<ManualFile> getResellerManualFiles(String folder) {
		List<ManualFile> r = new ArrayList<ManualFile>();
		refreshFileList(r, resellerManualRoot + folder, null);
		beforeSortManualFiles(null, r);
		return r;
	}

	private void beforeSortManualFiles(List<ManualFile> roots, List<ManualFile> data) {
		List<ManualFile> nextRoots = null;
		int insert_index = 0;
		ManualFile m = null;
		if (roots != null && roots.size() > 0) {
			for (ManualFile root : roots) {
				nextRoots = new ArrayList<ManualFile>();
				for (int i = 0; i < data.size(); i++) {
					m = data.get(i);
					if (root.getId().equals(m.getId())) {
						insert_index = i+1;
					}
					if (root.getId().equals(m.getParentId())) {
						nextRoots.add(m);
						data.remove(m);
						i--;
					}
				}
				sortManualFiles(data, nextRoots, insert_index);
			}
		} else {
			nextRoots = new ArrayList<ManualFile>();
			for (int i = 0; i < data.size(); i++) {
				m = data.get(i);
				if (m.getParentId() == null) {
					nextRoots.add(m);
					data.remove(i);
					i--;
				}
			}
			sortManualFiles(data, nextRoots, insert_index);
		}

	}

	private void sortManualFiles(List<ManualFile> data, List<ManualFile> nextRoots, int p_index) {
		Collections.sort(nextRoots, new Comparator<ManualFile>() {
			@Override
			public int compare(ManualFile a, ManualFile b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		if (nextRoots.size() > 0) {
			data.addAll(p_index, nextRoots);
			beforeSortManualFiles(nextRoots, data);
		}
	}

	public void refreshFileList(List<ManualFile> r, String folder, UUID parentId) {
		ManualFile mf = null;
		ObjectListing ol = s3Manager.listFolder(folder, null, "/");
		List<String> folderPaths = ol.getCommonPrefixes();
		List<S3ObjectSummary> s3ObjectSummaries = ol.getObjectSummaries();
		if (s3ObjectSummaries != null && s3ObjectSummaries.size() > 0) {
			Map<String, ManualFile> map = new HashMap<String, ManualFile>();
			String path = null;
			for (S3ObjectSummary s3ObjSummary : s3ObjectSummaries) {
				path = s3ObjSummary.getKey();
				if (path.equals(folder)) {
					continue;
				}
				mf = new ManualFile();
				mf.setName(path.replaceFirst(folder, ""));
				mf.setPath(path);
				mf.setSize(formatFileSize(s3ObjSummary.getSize()));
				mf.setLastModified(s3ObjSummary.getLastModified());
				mf.setType(path.substring(path.lastIndexOf(".") + 1));
				mf.setParentId(parentId);
				mf.setId(UUID.randomUUID());
				map.put(path, mf);
			}

			String mainPath = null;
			for (S3ObjectSummary s3ObjSummary : s3ObjectSummaries) {
				path = s3ObjSummary.getKey();
				if (path.endsWith(".txt")) {
					mainPath = path.substring(0, path.lastIndexOf(".txt"));
					if (map.containsKey(mainPath)) {
						map.remove(path);
						mf = map.get(mainPath);
						mf.setDesc(s3Manager.getString(path));
					}
				}
				removeMultiSuffix(path, map);
			}
			r.addAll(map.values());
		}
		if (folderPaths != null && folderPaths.size() > 0) {
			for (String path : folderPaths) {
				mf = new ManualFile();
				mf.setName(path.replaceFirst(folder, "").replaceAll("/", ""));
				mf.setType("folder");
				mf.setParentId(parentId);
				mf.setId(UUID.randomUUID());
				r.add(mf);
				refreshFileList(r, path, mf.getId());
			}
		}
	}

	private void removeMultiSuffix(String path, Map<String, ManualFile> map) {
		int count = 0;
		if (path.indexOf(".txt") > 0) {
			count++;
		}
		if (path.indexOf(".pdf") > 0) {
			count++;
		}
		if (path.indexOf(".exe") > 0) {
			count++;
		}
		if (path.indexOf(".zip") > 0) {
			count++;
		}
		if (count > 1 && map.containsKey(path)) {
			map.remove(path);
		}
	}

	private String formatFileSize(long longSize) {
		long SIZE_BT = 1024L;
		long SIZE_KB = SIZE_BT * 1024L;
		long SIZE_MB = SIZE_KB * 1024L;
		long SIZE_GB = SIZE_MB * 1024L;
		long SIZE_TB = SIZE_GB * 1024L;
		int SACLE = 0;

		if (longSize >= 0 && longSize < SIZE_BT) {
			return longSize + "B";
		} else if (longSize >= SIZE_BT && longSize < SIZE_KB) {
			String result = new BigDecimal(longSize).divide(new BigDecimal(SIZE_BT), SACLE, BigDecimal.ROUND_HALF_UP).toString();
			return result + "KB";
		} else if (longSize >= SIZE_KB && longSize < SIZE_MB) {
			String result = new BigDecimal(longSize).divide(new BigDecimal(SIZE_KB), SACLE, BigDecimal.ROUND_HALF_UP).toString();
			return result + "MB";
		} else if (longSize >= SIZE_MB && longSize < SIZE_GB) {
			String result = new BigDecimal(longSize).divide(new BigDecimal(SIZE_MB), SACLE, BigDecimal.ROUND_HALF_UP).toString();
			return result + "GB";
		} else {
			String result = new BigDecimal(longSize).divide(new BigDecimal(SIZE_GB), SACLE, BigDecimal.ROUND_HALF_UP).toString();
			return result + "TB";
		}
	}

	@Override
	public UUID saveResellerUser(ResellerUser formUser, String permissionDatas) {
		Assert.notNull(formUser.getSerialNo());
		Map<String, Object> resp = jobService.createOrUpdateCustomer(formUser);
		if ("0".equals(resp.get("responseCode"))) {
			if (StringUtils.isNotBlank(permissionDatas)) {
				formUser.getPermissions().clear();
				try {
					JSONArray permArray = new JSONArray(permissionDatas);
					for (int i = 0; i < permArray.length(); i++) {
						formUser.addPermission(permArray.getString(i));
					}
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
			formUser.setUsername(formUser.getToken());
			return userDAO.saveUser(formUser);
		} else {
			throw new BizException(resp.get("errorDescription").toString());
		}

	}
}