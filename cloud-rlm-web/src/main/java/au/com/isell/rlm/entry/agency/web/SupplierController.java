package au.com.isell.rlm.entry.agency.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.paging.Paging2;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ControllerConstant;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.address.constant.CountryCodeAddressMapping;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.reseller.vo.ResellerSupplierMapSearchVals;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.schedule.domain.Schedule.Frequency;
import au.com.isell.rlm.module.schedule.domain.ScheduleHistory;
import au.com.isell.rlm.module.schedule.domain.Status;
import au.com.isell.rlm.module.schedule.service.ScheduleService;
import au.com.isell.rlm.module.supplier.domain.DataRequest;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierBillingInfo;
import au.com.isell.rlm.module.supplier.domain.SupplierBranch;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdate;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdateExecute;
import au.com.isell.rlm.module.supplier.domain.SupplierUser;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.supplier.vo.PriceBreakSearchVals;
import au.com.isell.rlm.module.supplier.vo.SupplierFilter4Notify;
import au.com.isell.rlm.module.user.domain.User;

@Controller
@RequestMapping(value = ModulePath.AGENT_SUPPLIER)
public class SupplierController {
	private static Logger logger = LoggerFactory.getLogger(SupplierController.class);
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private SupplierService supplierService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private ScheduleService scheduleService;
	
	@RequiresPermissions("supplier:annualBilling:view")
	@RequestMapping(value = "/{supplierId}/billing", method = RequestMethod.GET)
	public String annualBilling(@PathVariable int supplierId, ModelMap modelMap) {
		SupplierBillingInfo billingInfo = supplierService.getBillingInfo(supplierId);
		modelMap.put("billingInfo", billingInfo);
		Supplier supplier = supplierService.getSupplier(supplierId);
		setPhonePrefix(modelMap, supplier);
		modelMap.put("supplierId", supplierId);
		modelMap.put("country", supplier.getCountry());
		return "agent/module/suppliers/annualBilling";
	}

	@RequestMapping(value = "/{supplierId}/billing", method = RequestMethod.POST)
	public String saveAnnualBilling(@PathVariable int supplierId, ModelMap modelMap, HttpServletRequest request) {
		SupplierBillingInfo billingInfo = BeanUtils.constructBean(request, SupplierBillingInfo.class, null);
		supplierService.saveBillingInfo(supplierId, billingInfo);
		return annualBilling(supplierId, modelMap);
	}
	
	@RequiresPermissions("supplier:view")
	@RequestMapping(value = "/{supplierId}/details", method = RequestMethod.GET)
	public String detail(@PathVariable int supplierId, HttpServletRequest request, ModelMap modelMap) {
		if (supplierId == -1) {
			Supplier supplier = new Supplier().init();
			String country = request.getParameter("supplier.country");
			supplier.setCountry(country);

			SupplierUser supplierUser = new SupplierUser();
			modelMap.put("supplier", supplier);
			modelMap.put("supplierUser", supplierUser);

			this.setPhonePrefix(modelMap, supplier);
		} else {
			Supplier supplier = supplierService.getSupplier(supplierId);
			modelMap.put("supplier", supplier);
			if (supplier != null)
				modelMap.put("supplierUser", supplierService.getSupplierUser(supplier.getUserId()));
			this.setPhonePrefix(modelMap, supplier);
		}
		modelMap.put("supplierId", supplierId);
		return "agent/module/suppliers/supplierDetails";
	}

	private void setPhonePrefix(ModelMap modelMap, Supplier supplier) {
		if (supplier != null) {
			AddressItem addressItem = addressService.getAddressItem(supplier.getCountry());
			modelMap.put("phonePrefix", addressItem != null ? addressItem.getPhoneAreaCodeBind() : "");
		}
	}

	@RequestMapping(value = "/{supplierId}/details", method = RequestMethod.POST)
	public String saveDetail(@PathVariable int supplierId, HttpServletRequest request) {
		Supplier formSupplier = BeanUtils.constructBean(request, Supplier.class, "supplier.");
		formSupplier.setSupplierId(supplierId);
		if (supplierId == -1) {
			SupplierBillingInfo billingInfo = new SupplierBillingInfo().init();
			formSupplier.setBillingInfo(billingInfo);

			DataRequest dataRequest = new DataRequest().init();
			formSupplier.setDataRequest(dataRequest);
		}
		logger.debug("form bean: " + formSupplier.toString());
		SupplierUser formSupplierUser = BeanUtils.constructBean(request, SupplierUser.class, "supplierUser.");
		formSupplierUser.setSupplierId(supplierId);
		logger.debug("form bean: " + formSupplierUser.toString());
		int savedSupplierId = supplierService.saveSupplierDetail(formSupplier, formSupplierUser);
		return String.format("redirect:%s/%s/details", ModulePath.AGENT_SUPPLIER, savedSupplierId);
	}

	@RequiresPermissions("supplier:dataRequest:view")
	@RequestMapping(value = "/{supplierId}/data-requests", method = RequestMethod.GET)
	public String dataRequests(@PathVariable int supplierId, ModelMap modelMap) {
		DataRequest dataRequest = supplierService.getDataRequest(supplierId);
		modelMap.put("dataRequest", dataRequest);
		modelMap.put("supplierId", supplierId);
		Supplier supplier = supplierService.getSupplier(supplierId);
		modelMap.put("defaultPriceBreakId", supplier.getDefaultPriceBreakID());
		return "agent/module/suppliers/dataRequests";
	}

	@RequestMapping(value = "/{supplierId}/data-requests", method = RequestMethod.POST)
	public String saveDataRequests(@PathVariable int supplierId, ModelMap modelMap, HttpServletRequest request, String defaultPriceBreak) {
		DataRequest dataRequest = BeanUtils.constructBean(request, DataRequest.class, null);
		if (StringUtils.isNotBlank(defaultPriceBreak)) {
			supplierService.saveDataRequest(supplierId, dataRequest, UUID.fromString(defaultPriceBreak));
		} else {
			supplierService.saveDataRequest(supplierId, dataRequest, null);
		}
		
		return dataRequests(supplierId, modelMap);
	}

	@RequiresPermissions("supplier:priceBreak:view")
	@RequestMapping(value = "/{supplierId}/price-breaks", method = RequestMethod.GET)
	public String priceBreaks(@PathVariable int supplierId, ModelMap model, @ModelAttribute PriceBreakSearchVals searchVals, String filter,
			Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		Pair<Long, List<PriceBreak>> r = supplierService.queryPriceBreaks(filter, supplierId, searchVals, pageSize, pageNo);
		String urlfmt = "%s/%s/price-breaks?%s";
		model.put("paging", new Paging2(String.format(urlfmt, ModulePath.AGENT_SUPPLIER, supplierId,UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("supplierId", supplierId);

		return "agent/module/suppliers/priceBreaks";
	}

	@RequestMapping(value = "/{supplierId}/query-price-breaks", method = RequestMethod.GET)
	public Result queryPriceBreaks(@PathVariable int supplierId, String query) {
		List<PriceBreak> r = supplierService.queryPriceBreaks(query, String.valueOf(supplierId));
		return new Result("success", r != null ? r.size() : 0, r);
	}
	@RequiresPermissions("supplier:priceBreak:view")
	@RequestMapping(value = "/{supplierId}/price-breaks/{priceBreakId}", method = RequestMethod.GET)
	public String getPriceBreak(@PathVariable int supplierId, @PathVariable String priceBreakId, ModelMap modelMap) {
		PriceBreak break1 = null;
		if ("new".equals(priceBreakId)) {
			break1 = new PriceBreak().init();
		} else {
			List<PriceBreak> breaks = supplierService.getPriceBreaks(supplierId, UUID.fromString(priceBreakId));
			if (breaks.size() > 0) {
				break1 = breaks.get(0);
			} else {
				break1 = new PriceBreak().init();
				break1.setSupplierId(supplierId);
				break1.setPriceBreakId(UUID.fromString(priceBreakId));
			}
		}
		modelMap.put("data", break1);
		modelMap.put("supplierId", supplierId);
		return "agent/module/suppliers/priceBreaks-Edit";
	}

	@RequestMapping(value = "/{supplierId}/price-breaks/{priceBreakId}", method = RequestMethod.POST)
	public String savePriceBreak(@PathVariable int supplierId, @PathVariable String priceBreakId, ModelMap modelMap, HttpServletRequest request) {
		PriceBreak priceBreak = BeanUtils.constructBean(request, PriceBreak.class, null);
		priceBreak.setSupplierId(supplierId);
		if (!"new".equals(priceBreakId)) {
			priceBreak.setPriceBreakId(UUID.fromString(priceBreakId));
		}

		supplierService.savePriceBreaks(supplierId, true, priceBreak);
		return String.format("redirect:/%s/%s/price-breaks", ModulePath.AGENT_SUPPLIER, supplierId);
	}
	
	@RequestMapping(value = "/{supplierId}/price-breaks/{priceBreakIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public Result deletePriceBreak(@PathVariable int supplierId, @PathVariable String priceBreakIds) {
		String[] arr = priceBreakIds.split(",");
		List<UUID> ids=new ArrayList<UUID>();
		for(String priceBreakId:arr){
			ids.add(UUID.fromString(priceBreakId));
		}
//		if(priceBreakId!=null && priceBreakId.length>0){
//			PriceBreak[] priceBreaks = new PriceBreak[priceBreakId.length];
//			for(int i=0;i<priceBreakId.length;i++){
//				priceBreaks[i] = new PriceBreak(supplierId,UUID.fromString(priceBreakId[i]));
//			}
			supplierService.deletePriceBreak(supplierId,ids);
//		}
		return new Result("ok",true);
	}

	@RequiresPermissions("supplier:branch:view")
	@RequestMapping(value = "/{supplierId}/branches", method = RequestMethod.GET)
	public String branches(@PathVariable int supplierId, ModelMap model, String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		Pair<Long, List<SupplierBranch>> r = supplierService.queryBranches(filter, supplierId, pageSize, pageNo);
		String urlfmt = "%s/%s/branches?%s";
		model.put("paging", new Paging2(String.format(urlfmt, ModulePath.AGENT_SUPPLIER,supplierId, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("supplierId", supplierId);
		Supplier supplier = supplierService.getSupplier(supplierId);
		model.put("supplier", supplier);
		return "agent/module/suppliers/branches";
	}

//	@RequiresPermissions("supplier:branch:view")
	@RequestMapping(value = "/{supplierId}/branches/{branchId}", method = RequestMethod.GET)
	public String getBranch(@PathVariable int supplierId, @PathVariable String branchId, ModelMap modelMap) {
		SupplierBranch branch = null;
		Supplier supplier = supplierService.getSupplier(supplierId);
		if ("new".equals(branchId)) {
			branch = new SupplierBranch().init();
			branch.setCountry(supplier.getCountry());
		} else {
			branch = supplierService.getBranch(supplierId, branchId);
		}

		setPhonePrefix(modelMap, supplier);

		modelMap.put("branch", branch);
		modelMap.put("supplierId", supplierId);
		return "agent/module/suppliers/branches-Edit";
	}

	@RequestMapping(value = "/{supplierId}/branches/{branchId}", method = RequestMethod.POST)
	public String saveBranch(@PathVariable int supplierId, @PathVariable String branchId, ModelMap modelMap, HttpServletRequest request) {
		SupplierBranch branch = BeanUtils.constructBean(request, SupplierBranch.class, "branch.");
		String countryCode = branch.getCountry();
		Class<? extends Address> addrCls = CountryCodeAddressMapping.getAddressCls(countryCode);
		Address address = BeanUtils.constructBean(request, addrCls, "address.", "addressItems");
		address.setCountryCode(countryCode);
		Address postalAddress = BeanUtils.constructBean(request, addrCls, "postalAddress.", "addressItems");
		postalAddress.setCountryCode(countryCode);
		Address warehouseAddress = BeanUtils.constructBean(request, addrCls, "warehouseAddress.", "addressItems");
		warehouseAddress.setCountryCode(countryCode);

		branch.setSupplierId(supplierId);
		branch.setAddress(address);
		branch.setWarehouseAddress(warehouseAddress);
		branch.setPostalAddress(postalAddress);

		supplierService.saveBranch(branch, branchId);
		return String.format("redirect:/%s/%s/branches", ModulePath.AGENT_SUPPLIER, supplierId);
	}

	@RequestMapping(value = "/{supplierId}/branches/{branchId}", method = RequestMethod.DELETE)
	public Result deleteBranch(@PathVariable int supplierId, @PathVariable String branchId) {
		supplierService.deleteBranch(supplierId, branchId);
		return new Result(String.format("/%s/%s/branches", ModulePath.AGENT_SUPPLIER, supplierId),true);
	}

	@RequestMapping(value = "/{supplierId}/branches/{branchId}/savesupplierdefaultbranch", method = RequestMethod.GET)
	public Result saveSupplierDefaultBranch(@PathVariable int supplierId, @PathVariable String branchId) {
		if (StringUtils.isNotBlank(branchId)) {
			Supplier supplier = supplierService.getSupplier(supplierId);
			supplier.setDefaultBranchId(UUID.fromString(branchId));
			supplierService.saveSupplier(supplier, true);
		}
		return new Result("success", true);
	}

	@RequiresPermissions("reseller:view")
	@RequestMapping(value = "/{supplierId}/resellers", method = RequestMethod.GET)
	public String resellerSupplierMaps(@PathVariable int supplierId, ModelMap model, @ModelAttribute ResellerSupplierMapSearchVals searchVals,
			String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		Pair<Long, List<ResellerSupplierMap>> r = supplierService.queryResellerSupplierMaps(filter, supplierId, searchVals, pageSize, pageNo);
		String urlfmt = "%s/%s/resellers?%s";
		model.put("paging", new Paging2(String.format(urlfmt, ModulePath.AGENT_SUPPLIER,supplierId, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("supplierId", supplierId);
		return "agent/module/suppliers/resellers";
	}
	
	@RequestMapping(value = "/{supplierId}/resellers/{serialNos}", method = RequestMethod.DELETE)
	public Result deleteResellerSupplierMap(@PathVariable int supplierId, @PathVariable String serialNos) {
		String[] tmp = serialNos.split(",");
		if(tmp!=null && tmp.length>0){
			for(int i=0;i<tmp.length;i++){
				if(StringUtils.isNotBlank(tmp[i])){
					resellerService.deleteResellerSupplierMap(Integer.valueOf(tmp[i]),supplierId);
				}
			}
		}
		return new Result("ok",true);
	}

	@RequestMapping(value = "{supplierId}/reseller-map/{serialNo}", method = RequestMethod.GET)
	public String getSupplierResellerMap(@PathVariable int supplierId, @PathVariable int serialNo, ModelMap modelMap) {

		ResellerSupplierMap supplierMap = null;
		if (serialNo<0) {
			supplierMap = new ResellerSupplierMap().init();
			supplierMap.setSupplierId(supplierId);
		} else {
			supplierMap = resellerService.getResellerSupplierMap(serialNo, supplierId);
		}
		Supplier supplier = supplierService.getSupplier(supplierId);
		modelMap.put("data", supplierMap);
		modelMap.put("supplierId", supplierId);
		modelMap.put("dataRequest", supplier.getDataRequest());
		modelMap.put("oldSerialNo", serialNo);
		modelMap.put("histories", resellerService.getResellerSupplierMapHistories(serialNo, supplierId));
		return "agent/module/suppliers/reseller-supplier-map";
	}
	@RequestMapping(value = "{supplierId}/reseller-map/{oldSerialNo}/reseller-check-duplicate", method = RequestMethod.GET)
	public Result checkResellerDuplicate(@PathVariable int supplierId,@PathVariable int oldSerialNo, ModelMap modelMap,String serialNo) {
		if(StringUtils.isNotBlank(serialNo) && (oldSerialNo==-1 || oldSerialNo!= Integer.parseInt(serialNo))){
			if(resellerService.isDuplicateResellerSupplierMap(Integer.parseInt(serialNo),supplierId)){
				return new Result("this item is duplicate!",false);
			}
		}
		return new Result();
	}
	
	@RequestMapping(value = "{supplierId}/supplier-datarequest", method = RequestMethod.GET)
	public Result getSupplierDataRequest(@PathVariable int supplierId,ModelMap modelMap) {
		Supplier supplier = supplierService.getSupplier(supplierId);
		return new Result(supplier.getDataRequest());
	}
	
	@RequestMapping(value = "{supplierId}/reseller-map/{serialNo}", method = RequestMethod.POST)
	public Result saveSupplierResellerMap(@PathVariable int supplierId, @PathVariable int serialNo, ModelMap modelMap,
			HttpServletRequest request) {
		ResellerSupplierMap formMap = BeanUtils.constructBean(request, ResellerSupplierMap.class, null);
		resellerService.saveResellerSupplierMapFromForm(formMap, serialNo, supplierId, true);
		//return String.format("redirect:%s/%s/reseller-map/%s", ModulePath.SUPPLIER, supplierId,formMap.getSerialNo());
		return new Result();
	}
	
	@RequiresPermissions("priceupdates:view")
	@RequestMapping(value = "/{supplierId}/supplierupdate", method = RequestMethod.GET)
	public String getSupplierUpdateDetail(@PathVariable int supplierId,ModelMap modelMap, HttpServletRequest request) {
		Schedule schedule = null;
		Supplier supplier = supplierService.getSupplier(supplierId);
		SupplierUpdate supplierUpdate=supplier.getUpdateInfo();
		boolean showStartBtn=true,showOtherOperationBtn=true;
		if (supplierUpdate!=null && supplierUpdate.getScheduleId()!=null) {
			schedule = scheduleService.getSchedule(supplierUpdate.getScheduleId().toString());
			getPriceUpdateDetails(supplier,modelMap, request, schedule, showStartBtn,showOtherOperationBtn);
			modelMap.put("scheduleId", supplierUpdate.getScheduleId().toString());
		} else {
			showStartBtn=false;
			showOtherOperationBtn=false;
			schedule = new Schedule();
			modelMap.put("scheduleId", "new");
			modelMap.put("showStartBtn", showStartBtn);
			modelMap.put("showOtherOperationBtn", showOtherOperationBtn);
			modelMap.put("supplierId", supplier.getSupplierId());
			modelMap.put("supplierUpdate", supplier.getUpdateInfo());
			modelMap.put("supplierName", supplier.getName());
			modelMap.put("schedule", schedule);
			modelMap.put("lastDayofMonthChecked",scheduleService.checkeScheduleTime(String.valueOf(schedule.getDaysOfMonth()), String.valueOf(Schedule.LAST_DAY_OF_MONTH)));
		}
		
		return "agent/module/suppliers/supplierUpdateDetail";
	}

	private void getPriceUpdateDetails(Supplier supplier,ModelMap modelMap, HttpServletRequest request, Schedule schedule, boolean showStartBtn, boolean showOtherOperationBtn) {
		User loginUser = GlobalAttrManager.getCurrentUser();
		String dayofMonth1="",dayofMonth2="";
		int count=0;
		for(int i=1;i<=31;i++){
			if(scheduleService.checkeScheduleTime(String.valueOf(schedule.getDaysOfMonth()), String.valueOf((long)Math.pow(2, i-1)))){
				count++;
				if(count==1){
					dayofMonth1 = String.valueOf(i);
				}
				if(count==2){
					dayofMonth2 = String.valueOf(i);
					break;
				}
			}
		}
		SupplierUpdateExecute supplierUpdateExecute = supplierService.getSupplierUpdateExecuteByScheduleId(schedule.getScheduleId().toString());
		if(supplierUpdateExecute!=null ){
			if(supplierUpdateExecute.getStatus()==Status.InProgress){
				if(supplierUpdateExecute.getUserId()!=null && !supplierUpdateExecute.getUserId().toString().equals(loginUser.getUserId().toString())){
					showStartBtn = false;
					showOtherOperationBtn = false;
					modelMap.put("executeMsg","Executing by "+supplierUpdateExecute.getUserName());
				}else{
					showStartBtn = false;
					showOtherOperationBtn = true;
				}
			}else{
				showStartBtn = true;
				showOtherOperationBtn = false;
			}
			
			modelMap.put("nextDueDate", supplierUpdateExecute.getNextExecuteDate());
			modelMap.put("lastUpdateDate",supplierUpdateExecute.getRecentExecuteDate());
			modelMap.put("lastUpdateByUserName",supplierUpdateExecute.getUserName());
		}else{
			modelMap.put("nextDueDate", schedule.calcNextExecuteDate(new Date()));
		}
		
		if(!schedule.isEnabled()){
			showStartBtn = false;
			showOtherOperationBtn = false;
		}
		modelMap.put("showStartBtn", showStartBtn);
		modelMap.put("showOtherOperationBtn", showOtherOperationBtn);
		modelMap.put("dayofMonth1", dayofMonth1);
		modelMap.put("dayofMonth2", dayofMonth2);
		modelMap.put("lastDayofMonthChecked",scheduleService.checkeScheduleTime(String.valueOf(schedule.getDaysOfMonth()), String.valueOf(Schedule.LAST_DAY_OF_MONTH)));
		modelMap.put("supplierName", supplier.getName());
		modelMap.put("schedule", schedule);
		modelMap.put("supplierId", supplier.getSupplierId());
		modelMap.put("supplierUpdate", supplier.getUpdateInfo());
	}
	
	@RequestMapping(value = "/{supplierId}/supplierupdate", method = RequestMethod.POST)
	public String saveSupplierUpdate(@PathVariable int supplierId,ModelMap modelMap, HttpServletRequest request) {
		Supplier supplier = supplierService.getSupplier(supplierId);
		SupplierUpdate formSupplierUpdate = BeanUtils.constructBean(request, SupplierUpdate.class, "supplierUpdate.");
		Schedule formSchedule = BeanUtils.constructBean(request, Schedule.class, "schedule.");
		SupplierUpdateExecute formSupplierUpdateExecute = BeanUtils.constructBean(request, SupplierUpdateExecute.class, "supplierUpdateExecute.");
		SupplierUpdate supplierUpdate=supplier.getUpdateInfo();
		UUID scheduleId = UUID.randomUUID();
		if (supplierUpdate!=null && supplierUpdate.getScheduleId()!=null) {
			scheduleId = supplierUpdate.getScheduleId();
		}
		formSupplierUpdate.setScheduleId(scheduleId);
		formSchedule.setScheduleId(scheduleId);
		
		makeSupplerUpdateInfo(request, supplier, formSupplierUpdate, formSchedule);
		supplierService.saveSupplierUpdate(supplier,formSchedule,formSupplierUpdateExecute);
		
		return getSupplierUpdateDetail(supplierId,modelMap,request);
	}

	private void makeSupplerUpdateInfo(HttpServletRequest request, Supplier supplier, SupplierUpdate formSupplierUpdate, Schedule formSchedule) {
		if(formSchedule.getFrequency()==Frequency.WeekDays){
			String[] dayofWeeks = request.getParameterValues("dayofWeek");
			int weekDays=0;
			for(int i=0;dayofWeeks!=null && i<dayofWeeks.length;i++){
				weekDays+=Integer.parseInt(dayofWeeks[i]);
			}
			formSchedule.setWeekDays(weekDays);
		}else if(formSchedule.getFrequency()==Frequency.WeeksOfMonth){
			String[] dayofWeeks = request.getParameterValues("dayofWeek");
			int weekDays=0;
			for(int i=0;dayofWeeks!=null && i<dayofWeeks.length;i++){
				weekDays+=Integer.parseInt(dayofWeeks[i]);
			}
			formSchedule.setWeekDays(weekDays);
			String[] weekofMonths = request.getParameterValues("weekofMonth");
			int weeksOfMonth=0;
			for(int i=0;weekofMonths!=null && i<weekofMonths.length;i++){
				weeksOfMonth+=Integer.parseInt(weekofMonths[i]);
			}
			formSchedule.setWeeksOfMonth(weeksOfMonth);
		}else if(formSchedule.getFrequency()==Frequency.DayOfMonth){
			long daysOfMonth = 0l;
			String dayofMonth1 = request.getParameter("dayofMonth1");
			if(StringUtils.isNotBlank(dayofMonth1)){
				daysOfMonth+=Math.pow(2,Integer.parseInt(dayofMonth1)-1);
			}
			String dayofMonth2 = request.getParameter("dayofMonth2");
			if(StringUtils.isNotBlank(dayofMonth2)){
				daysOfMonth+=Math.pow(2,Integer.parseInt(dayofMonth2)-1);
			}
			String lastDayofMonthChecked = request.getParameter("lastDayofMonthChecked");
			if("true".equals(lastDayofMonthChecked)){
				daysOfMonth+=Schedule.LAST_DAY_OF_MONTH;
			}
			formSchedule.setDaysOfMonth(daysOfMonth);
		
		}
		supplier.setUpdateInfo(formSupplierUpdate);
	}
	
	@RequiresPermissions("priceupdates:view")
	@RequestMapping(value = "/{supplierId}/supplierupdatehistory", method = RequestMethod.GET)
	public String supplierUpdateHistories(@PathVariable int supplierId, ModelMap model,Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		Supplier supplier = supplierService.getSupplier(supplierId);
		String scheduleId = null;
		if(supplier.getUpdateInfo()!=null && supplier.getUpdateInfo().getScheduleId()!=null){
			scheduleId = supplier.getUpdateInfo().getScheduleId().toString();
		}
		Pair<Long, List<ScheduleHistory>> r = scheduleService.queryScheduleHistories(scheduleId, pageSize, pageNo);
		String urlfmt = "%s/%s/supplierupdatehistory?%s";
		model.put("paging", new Paging2(String.format(urlfmt, ModulePath.AGENT_SUPPLIER,supplierId,UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("supplierId", supplierId);
		return "agent/module/suppliers/supplierUpdateHistory";
	}
	
	@RequiresPermissions("priceupdates:view")
	@RequestMapping(value = "/{supplierId}/priceupdates/{scheduleId}/details", method = RequestMethod.GET)
	public String getPriceUpdateDetail(@PathVariable int supplierId,@PathVariable String scheduleId,ModelMap modelMap, HttpServletRequest request) {
		Schedule schedule = scheduleService.getSchedule(scheduleId);
		Supplier supplier = supplierService.getSupplier(supplierId);
		modelMap.put("scheduleId", scheduleId);
		modelMap.put(ControllerConstant.DATA_SAVED_FLAG, request.getParameter(ControllerConstant.DATA_SAVED_FLAG));
		boolean showStartBtn=true,showOtherOperationBtn=true;
		getPriceUpdateDetails(supplier,modelMap, request, schedule, showStartBtn,showOtherOperationBtn);
		return "agent/module/suppliers/priceUpdateDetail";
	}
	
	@RequestMapping(value = "/{supplierId}/priceupdates/{scheduleId}/details", method = RequestMethod.POST)
	public String savePriceUpdate(@PathVariable int supplierId,@PathVariable String scheduleId,ModelMap modelMap, HttpServletRequest request) {
		Supplier supplier = supplierService.getSupplier(supplierId);
		SupplierUpdate formSupplierUpdate = BeanUtils.constructBean(request, SupplierUpdate.class, "supplierUpdate.");
		Schedule formSchedule = BeanUtils.constructBean(request, Schedule.class, "schedule.");
		formSupplierUpdate.setScheduleId(UUID.fromString(scheduleId));
		formSchedule.setScheduleId(UUID.fromString(scheduleId));
		
		SupplierUpdateExecute formSupplierUpdateExecute = BeanUtils.constructBean(request, SupplierUpdateExecute.class, "supplierUpdateExecute.");
		
		makeSupplerUpdateInfo(request, supplier, formSupplierUpdate, formSchedule);
		supplierService.saveSupplierUpdate(supplier,formSchedule,formSupplierUpdateExecute);
		return getPriceUpdateDetail(supplierId,scheduleId,modelMap,request);
	}
	
	@RequiresPermissions("priceupdates:view")
	@RequestMapping(value = "/{supplierId}/priceupdates/{scheduleId}/history", method = RequestMethod.GET)
	public String priceUpdateHistories(@PathVariable int supplierId,@PathVariable String scheduleId, ModelMap model,Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		Pair<Long, List<ScheduleHistory>> r = scheduleService.queryScheduleHistories(scheduleId, pageSize, pageNo);
		String urlfmt = "%s/%s/priceupdates/%s/history?%s";
		model.put("paging", new Paging2(String.format(urlfmt, ModulePath.AGENT_SUPPLIER,supplierId,scheduleId, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("supplierId", supplierId);
		model.put("scheduleId", scheduleId);
		return "agent/module/suppliers/priceUpdateHistory";
	}
	
//	@RequestMapping(value = "/{supplierId}/supplierupdate/{operation}", method = RequestMethod.GET)
//	public String operateSupplierUpdate(@PathVariable int supplierId,@PathVariable String operation,ModelMap modelMap, HttpServletRequest request) {
//		Supplier supplier = supplierService.getSupplier(supplierId);
//		String scheduleId = supplier.getUpdateInfo().getScheduleId().toString();
//		operateSchedule(operation, request, scheduleId);
//		return String.format("redirect:%s/%s/supplierupdate", ModulePath.SUPPLIER,supplierId);
//	}
	
	@RequestMapping(value = "/{supplierId}/priceupdates/{scheduleId}/{operation}", method = RequestMethod.GET)
	public String operatePriceUpdate(@PathVariable int supplierId,@PathVariable String scheduleId,@PathVariable String operation,ModelMap modelMap, HttpServletRequest request) {
		operateSchedule(operation, request, scheduleId);
		if("Started".equals(operation)){
			return String.format("redirect:%s/%s/priceupdates/%s/details", ModulePath.AGENT_SUPPLIER,supplierId,scheduleId);
		}else{
			return String.format("redirect:%s/%s/priceupdates/%s/details?%s", ModulePath.AGENT_SUPPLIER,supplierId,scheduleId,ControllerConstant.DATA_SAVED_FLAG + "=true");
		}
	}
	
	private void operateSchedule(String operation, HttpServletRequest request, String scheduleId) {
		Schedule schedule = scheduleService.getSchedule(scheduleId);
		SupplierUpdateExecute supplierUpdateExecute = supplierService.getSupplierUpdateExecuteByScheduleId(scheduleId);
		User loginUser = GlobalAttrManager.getCurrentUser();
		if("Started".equals(operation)){
			supplierUpdateExecute.setStatus(Status.InProgress);
			supplierUpdateExecute.setRecentExecuteDate(DatePicker.getCalendarInstance().getTime());
			supplierUpdateExecute.setUserName(loginUser.getFirstName()+" "+loginUser.getLastName());
			supplierUpdateExecute.setUserId(loginUser.getUserId());
			supplierService.saveOperateSupplierUpdateInfo(supplierUpdateExecute, null);
			
		}else if("Completed".equals(operation)){
			supplierUpdateExecute.setStatus(Status.Completed);
			supplierUpdateExecute.setNextExecuteDate(schedule.calcNextExecuteDate(supplierUpdateExecute.getRecentExecuteDate()));
			supplierUpdateExecute.setUserId(loginUser.getUserId());
			
			ScheduleHistory scheduleHistory = new ScheduleHistory();
			scheduleHistory.setScheduleId(schedule.getScheduleId());
			scheduleHistory.setUserId(loginUser.getUserId());
			scheduleHistory.setOperationDate(DatePicker.getCalendarInstance().getTime());
			scheduleHistory.setStatus(Status.Completed);
			scheduleHistory.setIpAddress(GlobalAttrManager.getClientInfo().getIpAddress());
			String notes = "Update Success by %s on %s (%s)";
			SimpleDateFormat sdf=new SimpleDateFormat("EEEEEEEE ','dd MMMMMMMMMM yyyy 'at' h:mm a");
			scheduleHistory.setNotes(String.format(notes,loginUser.getFirstName()+" "+loginUser.getLastName(),sdf.format(new Date()),scheduleHistory.getIpAddress()));
			supplierService.saveOperateSupplierUpdateInfo(supplierUpdateExecute, scheduleHistory);
			
		}else if("NoDataAvailable".equals(operation)){
			supplierUpdateExecute.setStatus(Status.Failed);
			supplierUpdateExecute.setNextExecuteDate(schedule.calcNextExecuteDate(supplierUpdateExecute.getRecentExecuteDate()));
			supplierUpdateExecute.setUserId(loginUser.getUserId());
			
			ScheduleHistory scheduleHistory = new ScheduleHistory();
			scheduleHistory.setScheduleId(schedule.getScheduleId());
			scheduleHistory.setUserId(loginUser.getUserId());
			scheduleHistory.setOperationDate(DatePicker.getCalendarInstance().getTime());
			scheduleHistory.setStatus(Status.Failed);
			scheduleHistory.setIpAddress(GlobalAttrManager.getClientInfo().getIpAddress());
			String notes = "Update Failure by %s on %s (%s)";
			SimpleDateFormat sdf=new SimpleDateFormat("EEEEEEEE ','dd MMMMMMMMMM yyyy 'at' h:mm a");
			scheduleHistory.setNotes(String.format(notes,loginUser.getFirstName()+" "+loginUser.getLastName(),sdf.format(new Date()),scheduleHistory.getIpAddress()));
			supplierService.saveOperateSupplierUpdateInfo(supplierUpdateExecute, scheduleHistory);
			
		}else if("Snooze".equals(operation)){
			supplierUpdateExecute.setStatus(null);
			if(supplierUpdateExecute.getNextExecuteDate()!=null){
				supplierUpdateExecute.setNextExecuteDate(DatePicker.pickDay(supplierUpdateExecute.getNextExecuteDate(), 1));
			}
			supplierUpdateExecute.setUserId(loginUser.getUserId());
			supplierService.saveOperateSupplierUpdateInfo(supplierUpdateExecute, null);
			
		}
	}

	
	@RequestMapping(value = "/notify-supplier/filter-page", method = RequestMethod.GET)
	public String getNotifyResellerPage(ModelMap model, HttpServletRequest request) {
		return "agent/module/suppliers/notify/notify-suppliers";
	}
	
	@RequestMapping(value = "/notify-supplier/filter", method = RequestMethod.GET)
	public Result filterNotifySuppliers(ModelMap model, HttpServletRequest request) {
		SupplierFilter4Notify filter = BeanUtils.constructBean(request, SupplierFilter4Notify.class, null);
		return new Result(supplierService.querySuppliers4Notification(filter));
	}

	@RequestMapping(value = "/notify-supplier/email", method = RequestMethod.POST)
	public Result sendNotifySupplierEmail(ModelMap model, HttpServletRequest request, String includeIds, String data) {
		SupplierFilter4Notify filter = BeanUtils.constructBean(request, SupplierFilter4Notify.class, null);
		Set<String> incIds = JsonUtils.decode(includeIds, Set.class);
		List<Map> datas = JsonUtils.decode(data, List.class);
		supplierService.sendNotifySupplierEmail(filter,incIds,datas);
		return new Result();
	}
	
}
