package au.com.isell.rlm.entry.reseller.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.address.constant.CountryCodeAddressMapping;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.jbe.domain.JobStatus;
import au.com.isell.rlm.module.jbe.service.JobService;
import au.com.isell.rlm.module.reseller.domain.BillingInfo;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.reseller.vo.ResellerUserSearchFilters;
import au.com.isell.rlm.module.reseller.vo.ResellerUserSearchVals;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.user.service.UserService;

@Controller
@RequestMapping(value = EntryPath.RESELLER+"/"+ModulePath.RESELLER_ACCOUNT)
public class RsAccountController {
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private SupplierService supplierService;;
	@Autowired
	private UserService userService;
	@Autowired
	private JobService jobService;
	@Autowired
	private AddressService addressService;

	@RequiresPermissions({ "resellerportal:create_edit_user"})
	@RequestMapping(value = "users")
	public String users(ModelMap model, @ModelAttribute ResellerUserSearchVals searchVals,String filter,Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		//default active
		if(StringUtils.isEmpty(filter) && searchVals.isEmpty()){
			filter=ResellerUserSearchFilters.All.toString();
			model.put("filter", filter);
		}
		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
		Pair<Long, List<ResellerUser>> r = userService.queryResellerUser(searchVals, filter, String.valueOf(serialNo), Boolean.FALSE, pageSize, pageNo);
		String urlfmt = "%s/%s/users?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.RESELLER,ModulePath.RESELLER_ACCOUNT, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("serialNo", serialNo);
		return "reseller-portal/account/users";
	}
	
	@RequiresPermissions({ "resellerportal:create_edit_user"})
	@RequestMapping(value="user/{userId}",method=RequestMethod.GET)
	public String getResellerUser(@PathVariable String userId,ModelMap modelMap) {
		Assert.notNull(userId);
		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
		ResellerUser user=null;
		if ("new".equals(userId)) {
			user = new ResellerUser().init();
		} else {
			user=userService.getUser(ResellerUser.class, UUID.fromString(userId));
		}
		Reseller reseller = resellerService.getReseller(serialNo);
		AddressItem addressItem = addressService.getAddressItem(reseller.getCountry());
		modelMap.put("phonePrefix", addressItem != null ? "+"+addressItem.getPhoneAreaCodeBind() : "");
		
		modelMap.put("resellerUser", user);
		modelMap.put("serialNo", serialNo);
		modelMap.put("isTestLicense", ResellerType.TestLicense.equals(reseller.getType()));
		return "reseller-portal/account/userDetails";
	}
	
	@RequiresPermissions({ "resellerportal:create_edit_user"})
	@RequestMapping(value="user/{userId}",method=RequestMethod.POST)
	public String saveResellerUser(@PathVariable String userId,ModelMap modelMap,HttpServletRequest request,String permissionDatas) {
		ResellerUser user= BeanUtils.constructBean(request, ResellerUser.class, null);
		UUID savedId= resellerService.saveResellerUser(user,permissionDatas);
		return String.format("redirect:%s/%s/user/%s", EntryPath.RESELLER,ModulePath.RESELLER_ACCOUNT, savedId);
	}
	
	@RequestMapping(value = "/summary", method = RequestMethod.GET)
	public String summary(ModelMap modelMap) {
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		Reseller reseller = resellerService.getReseller(user.getSerialNo());
		modelMap.put("companyName", reseller.getCompany());
		modelMap.put("loginUser", user.getFirstName()+" "+user.getLastName());
		AgentUser sUser=(AgentUser)userService.getUser(reseller.getSalesRepID().toString());
		modelMap.put("accountManager", sUser.getFirstName()+" "+sUser.getLastName());
		modelMap.put("accountManagerPhone", sUser.getPhone());
		
		int[] counts=supplierService.statisticRsMap4ResellerPortal(user.getSerialNo());
		modelMap.put("activeSups", counts[0]);
		modelMap.put("pendingSups", counts[1]);
		
		modelMap.put("pendingJobCount", jobService.getJobCountByStatus(JobStatus.Pending));
		modelMap.put("activeJobCount", jobService.getJobCountByStatus(JobStatus.Active));
		
		return "reseller-portal/account/resellerSummary";
	}

	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public String detail(ModelMap modelMap) {
		ResellerUser resellerUser = (ResellerUser) GlobalAttrManager.getCurrentUser();
		Boolean isPrimary = resellerUser.isPrimary();
		int serialNo = resellerUser.getSerialNo();
		Reseller reseller = resellerService.getReseller(serialNo);
		modelMap.put("reseller", reseller);
		modelMap.put("resellerUser", resellerService.getResellerUser(reseller.getUserId()));
		modelMap.put("resellerUsage", reseller.getUsage());
		modelMap.put("billingInfo", reseller.getBillingInfo());
		AddressItem addressItem = addressService.getAddressItem(reseller.getCountry());
		modelMap.put("phonePrefix", addressItem != null ? addressItem.getPhoneAreaCodeBind() : "");
		modelMap.put("serialNo", serialNo);
		modelMap.put("isPrimary", isPrimary);
		return "reseller-portal/account/resellerDetails";
	}

	@RequestMapping(value = "/detail", method = RequestMethod.POST)
	public String saveDetail(HttpServletRequest request) {
		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
		Reseller formReseller = BeanUtils.constructBean(request, Reseller.class, "reseller.");
		Address formAddress = getAddress(request);
		formReseller.setAddress(formAddress);
		formReseller.setCountry(formAddress.getCountryCode());
		BillingInfo formBillingInfo = BeanUtils.constructBean(request, BillingInfo.class, "billingInfo.");
		formReseller.setBillingInfo(formBillingInfo);

		ResellerUser formResellerUser = BeanUtils.constructBean(request, ResellerUser.class, "resellerUser.");

		resellerService.saveDetail(serialNo, formReseller, formResellerUser);
		return String.format("redirect:%s/%s/detail", EntryPath.RESELLER,ModulePath.RESELLER_ACCOUNT);
	}
	
	private Address getAddress(HttpServletRequest request) {
		String countryCode = request.getParameter("address.countryCode");
		Class<? extends Address> addrCls = CountryCodeAddressMapping.getAddressCls(countryCode);
		Address formAddress = BeanUtils.constructBean(request, addrCls, "address.", "addressItems");
		formAddress.setCountryCode(countryCode);
		return formAddress;
	}
}
