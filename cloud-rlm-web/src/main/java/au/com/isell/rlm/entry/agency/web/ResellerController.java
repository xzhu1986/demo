package au.com.isell.rlm.entry.agency.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.paging.Paging2;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.address.constant.CountryCodeAddressMapping;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;
import au.com.isell.rlm.module.invoice.service.InvoiceService;
import au.com.isell.rlm.module.invoice.vo.InvoiceSearchFilters4Reseller;
import au.com.isell.rlm.module.reseller.domain.BillingInfo;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerUsage;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.domain.VersionHistory;
import au.com.isell.rlm.module.reseller.domain.license.EComLicense;
import au.com.isell.rlm.module.reseller.domain.license.EPDLicense;
import au.com.isell.rlm.module.reseller.domain.license.ITQLicense;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;
import au.com.isell.rlm.module.reseller.service.LicenseService;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.reseller.vo.ResellerFilter4Notify;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchVals;
import au.com.isell.rlm.module.reseller.vo.ResellerUserSearchFilters;
import au.com.isell.rlm.module.reseller.vo.ResellerUserSearchVals;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.service.UserService;

/**
 * @author frankw 06/02/2012
 */
@Controller
@RequestMapping(value = ModulePath.AGENT_RESELLERS)
public class ResellerController {
	private static Logger logger = LoggerFactory.getLogger(ResellerController.class);
	@Autowired
	private SupplierService supplierService;
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private InvoiceService invoiceService;
	@Autowired
	private UserService userService;

	@RequiresPermissions("reseller:view")
	@RequestMapping(value = "/{serialNo}/detail", method = RequestMethod.GET)
	public String detail(@PathVariable int serialNo, String historyId, HttpServletRequest request, ModelMap modelMap) {
		// serialNo==-1 means to create reseller
		Reseller reseller = null;
		if (StringUtils.isBlank(historyId)) {
			reseller = resellerService.getReseller(serialNo);
		} else {
			OperationHistory history=userService.getOperationHistory(UUID.fromString(historyId));
			reseller = history.getTargetWithType(Reseller.class);
			history.setTarget(null);
			modelMap.put("history", history);
		}
		if (reseller != null) {
			modelMap.put("reseller", reseller);
			modelMap.put("resellerUser", resellerService.getResellerUser(reseller.getUserId()));
			modelMap.put("resellerUsage", reseller.getUsage());
			modelMap.put("billingInfo", reseller.getBillingInfo());
			AddressItem addressItem = addressService.getAddressItem(reseller.getCountry());
			modelMap.put("phonePrefix", addressItem != null ? addressItem.getPhoneAreaCodeBind() : "");
		} else {
			buildData4Create(modelMap, request);
		}
		modelMap.put("serialNo", serialNo);
		modelMap.put("historyId", historyId);
		return "agent/module/resellers/resellerDetails";
	}

	private void buildData4Create(ModelMap modelMap, HttpServletRequest request) {
		Reseller reseller = new Reseller().init();
		String country = request.getParameter("reseller.country");
		reseller.setCountry(country);
		reseller.getAddress().setCountryCode(country);

		modelMap.put("reseller", reseller);
		modelMap.put("resellerUser", new ResellerUser(reseller).init());
		modelMap.put("resellerUsage", new ResellerUsage());
		AddressItem addressItem = addressService.getAddressItem(reseller.getCountry());// Default
		modelMap.put("phonePrefix", addressItem != null ? addressItem.getPhoneAreaCodeBind() : "");

		BillingInfo billingInfo = new BillingInfo();
		// billingInfo.setGst(addressItem.getTaxRate());
		modelMap.put("billingInfo", billingInfo);
	}

	@RequestMapping(value = "/{serialNo}/detail", method = RequestMethod.POST)
	public String saveDetail(@PathVariable int serialNo, HttpServletRequest request) {
		Reseller formReseller = BeanUtils.constructBean(request, Reseller.class, "reseller.");
		Address formAddress = getAddress(request);
		formReseller.setAddress(formAddress);
		formReseller.setCountry(formAddress.getCountryCode());
		BillingInfo formBillingInfo = BeanUtils.constructBean(request, BillingInfo.class, "billingInfo.");
		formReseller.setBillingInfo(formBillingInfo);
		logger.debug("form bean: " + formReseller.toString());

		ResellerUser formResellerUser = BeanUtils.constructBean(request, ResellerUser.class, "resellerUser.");
		logger.debug("form bean: " + formResellerUser.toString());

		int savedSerialNo = resellerService.saveDetail(serialNo, formReseller, formResellerUser);
		return String.format("redirect:%s/%s/detail", ModulePath.AGENT_RESELLERS, savedSerialNo);
	}

	private Address getAddress(HttpServletRequest request) {
		String countryCode = request.getParameter("address.countryCode");
		Class<? extends Address> addrCls = CountryCodeAddressMapping.getAddressCls(countryCode);
		Address formAddress = BeanUtils.constructBean(request, addrCls, "address.", "addressItems");
		formAddress.setCountryCode(countryCode);
		return formAddress;
	}

	@RequiresPermissions("reseller:view")
	@RequestMapping(value = "/{serialNo}/license", method = RequestMethod.GET)
	public String license(@PathVariable int serialNo, String historyId, ModelMap modelMap) {
		Reseller r=null;
		if (StringUtils.isBlank(historyId)) {
			r = resellerService.getReseller(serialNo);
		} else {
			OperationHistory history=userService.getOperationHistory(UUID.fromString(historyId));
			r = history.getTargetWithType(Reseller.class);
			history.setTarget(null);
			modelMap.put("history", history);
		}
		Map<LicenseType, LicModule> licenses = r.getLicenses();
		if (licenses != null) {
			modelMap.put("itqLicense", licenses.get(LicenseType.ITQLicense));
			modelMap.put("ecomLicense", licenses.get(LicenseType.EComLicense));
			modelMap.put("epdLicense", licenses.get(LicenseType.EPDLicense));
		} else {
			modelMap.put("itqLicense", new ITQLicense());
			modelMap.put("ecomLicense", new EComLicense());
			modelMap.put("epdLicense", new EPDLicense());
		}
		modelMap.put("billingInfo", r.getBillingInfo());
		modelMap.put("resellerUsage", r.getUsage());
		modelMap.put("reseller", r);
		if (r.getLastReviewUser() != null) {
			modelMap.put("lastReivewUser", userService.getUser(r.getLastReviewUser().toString()).getUsername());
		}

		modelMap.put("serialNo", serialNo);
		modelMap.put("historyId", historyId);
		return "agent/module/resellers/license";
	}

	@RequestMapping(value = "/next-review-date", method = RequestMethod.GET)
	public Result getNewLicReviewDate() {
		Map m = new HashMap();
		Date lastReview = new Date();
		Date nextRiview = DatePicker.pickFirstDayOfMonth(new Date(), 3);
		m.put("lastReviewDate", DatePicker.getFormatedDate(lastReview));
		m.put("lastReviewUser", GlobalAttrManager.getCurrentUser());
		m.put("nextReviewDate", DatePicker.getFormatedDate(nextRiview));
		return new Result(m);
	}

	@RequestMapping(value = "/{serialNo}/license", method = RequestMethod.POST)
	public String saveLicense(@PathVariable int serialNo, ModelMap modelMap, HttpServletRequest request) {
		Reseller formReseller = BeanUtils.constructBean(request, Reseller.class, "reseller.");
		formReseller.setSerialNo(serialNo);
		Map<LicenseType, LicModule> licenses = new HashMap<LicenseType, LicModule>();
		licenses.put(LicenseType.ITQLicense, BeanUtils.constructBean(request, ITQLicense.class, "itqLicense."));
		licenses.put(LicenseType.EComLicense, BeanUtils.constructBean(request, EComLicense.class, "ecomLicense."));
		licenses.put(LicenseType.EPDLicense, BeanUtils.constructBean(request, EPDLicense.class, "epdLicense."));
		licenseService.saveLicense(licenses, formReseller);

		return String.format("redirect:%s/%s/license", ModulePath.AGENT_RESELLERS, serialNo);
	}

	@RequiresPermissions("reseller:view")
	@RequestMapping("/{serialNo}/suppliers")
	public String resellerSupplierMaps(@PathVariable int serialNo, ModelMap model, @ModelAttribute ResellerSupplierMapSearchVals searchVals,
 String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		Pair<Long, List<ResellerSupplierMap>> r = resellerService.queryResellerSupplierMaps(filter, serialNo, searchVals, pageSize, pageNo);
		String urlfmt = "%s/%s/suppliers?%s";
		model.put("paging", new Paging2(String.format(urlfmt, ModulePath.AGENT_RESELLERS,serialNo, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("serialNo", serialNo);
		return "agent/module/resellers/suppliers";
	}

	@RequestMapping(value = "/{serialNo}/suppliers/{supplierIds}/delete", method = RequestMethod.GET)
	public String deleteResellerSupplierMap(@PathVariable Integer serialNo, @PathVariable String supplierIds) {
		String[] tmp = supplierIds.split(",");
		if (tmp != null && tmp.length > 0) {
			for (int i = 0; i < tmp.length; i++) {
				if (StringUtils.isNotBlank(tmp[i])) {
					resellerService.deleteResellerSupplierMap(serialNo, Integer.valueOf(tmp[i]));
				}
			}
		}
		return String.format("redirect:/%s/%s/suppliers", ModulePath.AGENT_RESELLERS, serialNo);
	}

	@RequestMapping(value = "{serialNo}/supplier-map/{oldSupplierId}/supplier-check-duplicate", method = RequestMethod.GET)
	public Result checkResellerDuplicate(@PathVariable int serialNo, @PathVariable int oldSupplierId, ModelMap modelMap, String supplierId) {
		if (StringUtils.isNotBlank(supplierId) && (oldSupplierId == -1 || oldSupplierId != Integer.parseInt(supplierId))) {
			if (resellerService.isDuplicateResellerSupplierMap(serialNo, Integer.parseInt(supplierId))) {
				return new Result("this item is duplicate!", false);
			}
		}
		return new Result();
	}

	@RequiresPermissions("reseller:view")
	@RequestMapping("/{serialNo}/downloads")
	public String downloads(@PathVariable int serialNo, ModelMap modelMap) {
		modelMap.put("dataFiles", resellerService.getPendingDownloadDataFiles(serialNo));
		modelMap.put("imageFiles", resellerService.getPendingDownloadImageFiles(serialNo));
		modelMap.put("epdDownloadInfo", resellerService.getReseller(serialNo).getUsage());
		modelMap.put("serialNo", serialNo);
		return "agent/module/resellers/downloads";
	}

	@RequiresPermissions("reseller:view")
	@RequestMapping("/{serialNo}/siteHistory")
	public String siteHistory(@PathVariable int serialNo, ModelMap modelMap) {
		List<VersionHistory> upgradeHistory = licenseService.getVersionHistory(serialNo);
		List<String[]> clientServerInformation = resellerService.getResellerSystemInfo(serialNo);
		modelMap.put("upgradeHistory", upgradeHistory);
		modelMap.put("clientServerInformation", clientServerInformation);
		modelMap.put("serialNo", serialNo);
		return "agent/module/resellers/siteHistory";
	}

	@RequestMapping(value = "{serialNo}/supplier-map/{supplierId}", method = RequestMethod.GET)
	public String getResellerSupplierMap(@PathVariable int serialNo, @PathVariable int supplierId, ModelMap modelMap) {
		ResellerSupplierMap supplierMap = null;
		if (supplierId < 0) {
			supplierMap = new ResellerSupplierMap().init();
			supplierMap.setSerialNo(serialNo);
		} else {
			supplierMap = resellerService.getResellerSupplierMap(serialNo, supplierId);
			Supplier supplier = supplierService.getSupplier(supplierId);
			PriceBreak priceBreak = supplier.getPriceBreaks().get(supplierMap.getPriceBreakId());
			if (priceBreak != null) {
				modelMap.put("priceBreakName", priceBreak.getName());
			}
		}
		modelMap.put("data", supplierMap);
		modelMap.put("serialNo", serialNo);
		modelMap.put("oldSupplierId", supplierId);

		modelMap.put("histories", resellerService.getResellerSupplierMapHistories(serialNo, supplierId));
		return "agent/module/resellers/reseller-supplier-map";
	}

	@RequestMapping(value = "{serialNo}/supplier-map/{supplierId}", method = RequestMethod.POST)
	public Result saveResellerSupplierMap(@PathVariable int serialNo, @PathVariable int supplierId, ModelMap modelMap, HttpServletRequest request) {
		ResellerSupplierMap formMap = BeanUtils.constructBean(request, ResellerSupplierMap.class, null);
		resellerService.saveResellerSupplierMapFromForm(formMap, serialNo, supplierId, true);
		// return String.format("redirect:%s/%s/supplier-map/%s", ModulePath.RESELLERS,
		// serialNo,formMap.getSupplierId());
		return new Result();
	}

	@RequiresPermissions({ "reseller:view", "invoice:view" })
	@RequestMapping(value = "{serialNo}/invoices")
	public String invoices(@PathVariable int serialNo, ModelMap model, String filter, HttpServletRequest request) {
		// pageSize = pageSize == null ? 25 : pageSize;
		// pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		if (StringUtils.isEmpty(filter)) {
			filter = InvoiceSearchFilters4Reseller.All.toString();
			model.put("filter", filter);
		}
		Pair<Long, List<InvoiceSearchBean>> r = invoiceService.query4Reseller(filter, serialNo);
		// String urlfmt = "%s/%s/invoices?%s";
		// model.put("paging", new Paging2(String.format(urlfmt, EntryPath.AGENT,serialNo,
		// UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("serialNo", serialNo);
		return "agent/module/resellers/resellerInvoices";
	}

	@RequestMapping(value = "/notify-reseller/filter-page", method = RequestMethod.GET)
	public String getNotifyResellerPage(ModelMap model, HttpServletRequest request) {
		return "agent/module/resellers/notify/notify-resellers";
	}

	@RequestMapping(value = "/notify-reseller/filter", method = RequestMethod.GET)
	public Result filterNotifyResellers(ModelMap model, HttpServletRequest request) {
		ResellerFilter4Notify filter = BeanUtils.constructBean(request, ResellerFilter4Notify.class, null);
		return new Result(resellerService.queryReseller4Notification(filter));
	}

	// @RequestMapping(value = "/notify-supplier/email", method = RequestMethod.GET)
	// public String getNotifySupplierEmailPage( ModelMap model, HttpServletRequest request) {
	// ResellerFilter4Notify filter = BeanUtils.constructBean(request, ResellerFilter4Notify.class, null);
	// List<ResellerSupplierMap> list = resellerService.queryReseller4Notification(filter);
	// MailPreview mailPreview = new MailPreview();
	// if (list != null) {
	// List<String> mails=new ArrayList<String>();
	// for (ResellerSupplierMap map : list) {
	// mails.add(map.getSupEmail());
	// }
	// mailPreview.setTo(StringConverter.join(MailPreview.mailSeperator, mails));
	// }
	// model.put("mail", mailPreview);
	// model.put("filter", UrlUtils.encode(JsonUtils.encode(filter)));
	// return "agent/module/resellers/reseller-sup-map-email-edit";
	// }

	@RequestMapping(value = "/notify-reseller/email", method = RequestMethod.POST)
	public Result sendNotifyEmail(ModelMap model, HttpServletRequest request, String sendIds, String data) {
		ResellerFilter4Notify filter = BeanUtils.constructBean(request, ResellerFilter4Notify.class, null);
		Set<String> ids = JsonUtils.decode(sendIds, Set.class);
		List<Map> datas = JsonUtils.decode(data, List.class);
		resellerService.sendNotifyResellerEmail(filter, ids, datas);
		return new Result();
	}

	
	@RequiresPermissions({ "reseller:view"})
	@RequestMapping(value = "{serialNo}/users")
	public String users(@PathVariable int serialNo, ModelMap model, @ModelAttribute ResellerUserSearchVals searchVals,String filter,Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		//default active
		if(StringUtils.isEmpty(filter) && searchVals.isEmpty()){
			filter=ResellerUserSearchFilters.All.toString();
			model.put("filter", filter);
		}
		Pair<Long, List<ResellerUser>> r = userService.queryResellerUser(searchVals, filter, String.valueOf(serialNo), Boolean.FALSE, pageSize, pageNo);
		String urlfmt = "%s/%s/users?%s";
		model.put("paging", new Paging2(String.format(urlfmt, ModulePath.AGENT_RESELLERS,String.valueOf(serialNo), UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("serialNo", serialNo);
		return "agent/module/resellers/users";
	}
	
	@RequestMapping(value="{serialNo}/user/{userId}",method=RequestMethod.GET)
	public String getResellerUser(@PathVariable int serialNo,@PathVariable String userId,ModelMap modelMap) {
		Assert.notNull(userId);
		ResellerUser user=null;
		if ("new".equals(userId)) {
			user = new ResellerUser().init();
		} else {
			user=userService.getUser(ResellerUser.class, UUID.fromString(userId));
		}
		modelMap.put("resellerUser", user);
		modelMap.put("serialNo", serialNo);
		return "agent/module/resellers/userDetails";
	}
	
	@RequestMapping(value="{serialNo}/user/{userId}",method=RequestMethod.POST)
	public String saveResellerUser(@PathVariable int serialNo,@PathVariable String userId,ModelMap modelMap,HttpServletRequest request,String permissionDatas) {
		ResellerUser user= BeanUtils.constructBean(request, ResellerUser.class, null);
		Reseller reseller = resellerService.getReseller(serialNo);
		user.setResellerName(reseller.getCompany());
		user.setPrimary(false);
		UUID savedId= resellerService.saveResellerUser(user,permissionDatas);
		return String.format("redirect:%s/%s/user/%s", ModulePath.AGENT_RESELLERS,serialNo, savedId);
	}
	
}
