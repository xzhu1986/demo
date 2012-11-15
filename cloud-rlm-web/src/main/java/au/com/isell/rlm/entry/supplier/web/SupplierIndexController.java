package au.com.isell.rlm.entry.supplier.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.EntryFuncPath;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.supplier.vo.SupplierPortalResellerMapFilters;
import au.com.isell.rlm.module.supplier.vo.SupplierPortalResellerMapVals;

@Controller
@RequestMapping(value=EntryPath.SUPPLIER)
public class SupplierIndexController {
	@Autowired
	private SupplierService supplierService;
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private AddressService addressService;
	
	@RequestMapping(value=EntryFuncPath.Home)
	public String home(ModelMap model, @ModelAttribute SupplierPortalResellerMapVals searchVals, String filter, HttpServletRequest request) {
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		//default active
		if(StringUtils.isEmpty(filter) && searchVals.isEmpty()){
			filter=SupplierPortalResellerMapFilters.WaitingApproval.name();
			model.put("filter", filter);
		}
		model.put("supplierId", GlobalAttrManager.getCurrentSupplierId());
		return "supplier-portal/supplier-portal-resellers";
	}
	
	@RequestMapping(value = "/resellers", method = RequestMethod.GET)
	public Result getSupplierPortalResellerMaps(ModelMap model, HttpServletRequest request) {
		SupplierPortalResellerMapVals searchVals = BeanUtils.constructBean(request, SupplierPortalResellerMapVals.class, null);
		return new Result(supplierService.querySupplierPortalResellerMap(searchVals, request.getParameter("filter")));
	}
	
	@RequestMapping(value = "/reseller-map/{serialNo}", method = RequestMethod.GET)
	public String getSupplierPortalResellerMap(ModelMap modelMap, @PathVariable int serialNo, @PathVariable String priceBreakId, HttpServletRequest request) {
		Integer supplierId = GlobalAttrManager.getCurrentSupplierId();
		ResellerSupplierMap rsMap = resellerService.getResellerSupplierMap(serialNo,supplierId);
		Reseller reseller = resellerService.getReseller(serialNo);
		GeneralAddress address = (GeneralAddress)reseller.getAddress();
		StringBuffer addr = new StringBuffer();
		if(StringUtils.isNotBlank(address.getAddress1())){
			addr.append(address.getAddress1()).append(",");
		}
		if(StringUtils.isNotBlank(address.getAddress2())){
			addr.append(address.getAddress2()).append(",");
		}
		if(StringUtils.isNotBlank(address.getCity())){
			addr.append(address.getCity()).append(",");
		}
		if(StringUtils.isNotBlank(address.getRegion())){
			addr.append(addressService.getRegionDisplayNames(address.getRegion())).append(",");
		}
		if(StringUtils.isNotBlank(address.getPostcode())){
			addr.append(address.getPostcode());
		}
		modelMap.put("address", addr.toString());
		modelMap.put("rsMap", rsMap);
		modelMap.put("reseller",reseller);
		modelMap.put("histories", resellerService.getResellerSupplierMapHistories(serialNo,supplierId));
		modelMap.put("supplierId",supplierId);
		return "supplier-portal/supplier-portal-reseller-map";
	}
	
	@RequestMapping(value = "/reseller-map", method = RequestMethod.POST)
	public Result updateSupplierPortalResellerMap(String optionUpdate,int oldSerialNo,int oldSupplierId, ModelMap modelMap,HttpServletRequest request) {
		ResellerSupplierMap formMap = BeanUtils.constructBean(request, ResellerSupplierMap.class, null);
		if("approval".equals(optionUpdate)){
			formMap.setStatus(ApprovalStatus.Approved);
		}else if("decline".equals(optionUpdate)){
			formMap.setStatus(ApprovalStatus.Disabled);
			formMap.setSupplierId(oldSupplierId);
		}
		resellerService.saveResellerSupplierMapFromForm(formMap, oldSerialNo, oldSupplierId, true);
		return new Result();
	}
}
