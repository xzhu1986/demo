package au.com.isell.rlm.entry.reseller.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.paging.Paging2;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.reseller.vo.RSMapSearchFilters4ResellerPortal;
import au.com.isell.rlm.module.supplier.domain.DataRequest;
import au.com.isell.rlm.module.supplier.domain.DataRequest.SignupMethod;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierBranch;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.supplier.vo.ResellerPortalSupplierSearchVals;
import au.com.isell.rlm.module.user.service.UserService;

@Controller
@RequestMapping(value = EntryPath.RESELLER)
public class RsSupplierController {
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private SupplierService supplierService;
	@Autowired
	private UserService userService;
	@Autowired
	private AddressService addressService;

	@RequiresPermissions({ "resellerportal:request_supplier"})
	@RequestMapping("/suppliers")
	public String filterSuppliers(ModelMap model, String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		if(StringUtils.isEmpty(filter)){
			filter=RSMapSearchFilters4ResellerPortal.MySuppliers.name();
			model.put("filter", filter);
		}

		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
		Pair<Long, List<ResellerSupplierMap>> r = resellerService.queryResellerSupplierMaps4ResellerPortal(filter, serialNo, pageSize, pageNo);
		String urlfmt = "%s/suppliers?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.RESELLER, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("serialNo", serialNo);
		return "reseller-portal/supplier/suppliers";
	}

	@RequiresPermissions({ "resellerportal:request_supplier"})
	@RequestMapping("/suppliers/not-allcocated")
	public String getNotAllocatedSuppliersPage(ModelMap model, HttpServletRequest request,@ModelAttribute ResellerPortalSupplierSearchVals searchVals,Integer pageNo, Integer pageSize) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		
		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
		model.put("serialNo", serialNo);
		
		Pair<Long, List<SupplierSearchBean>> r=supplierService.queryNotAllocatedSuppliers(serialNo,searchVals,pageNo,pageSize);
		String urlfmt = "%s/suppliers/not-allcocated?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.RESELLER, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("serialNo", serialNo);
		return "reseller-portal/supplier/not-allocated-suppliers";
	}

//	@RequestMapping("/suppliers/not-allcocated/data")
//	public String filterNotAllocatedSuppliers(ModelMap model, HttpServletRequest request,@ModelAttribute ResellerPortalSupplierSearchVals searchVals) {
//		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
//		Result r = new Result(supplierService.queryNotAllocatedSuppliers(serialNo,searchVals));
//		return r;
//	}

	@RequiresPermissions({ "resellerportal:request_supplier"})
	@RequestMapping(value = "/supplier-map/{supplierId}", method = RequestMethod.GET)
	public String getResellerSupplierMap(@PathVariable int supplierId, ModelMap modelMap) {
		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
		ResellerSupplierMap supplierMap = null;
		supplierMap = resellerService.getResellerSupplierMap(serialNo, supplierId);
		Supplier supplier = supplierService.getSupplier(supplierId);
		modelMap.put("supplierName", supplier.getName());
		PriceBreak priceBreak = supplier.getPriceBreaks().get(supplierMap.getPriceBreakId());
		if (priceBreak != null) {
			modelMap.put("priceBreakName", priceBreak.getName());
		}
		modelMap.put("data", supplierMap);
		modelMap.put("serialNo", serialNo);

		return "reseller-portal/supplier/reseller-supplier-map";
	}

	@RequiresPermissions({ "resellerportal:request_supplier"})
	@RequestMapping(value = "/supplier-map/add/{supplierId}", method = RequestMethod.GET)
	public String getAddResellerSupplierMapPage(@PathVariable int supplierId, ModelMap modelMap) {
		int serialNo = ((ResellerUser) GlobalAttrManager.getCurrentUser()).getSerialNo();
		ResellerSupplierMap supplierMap = new ResellerSupplierMap().init();
		supplierMap.setSerialNo(serialNo);
		supplierMap.setSupplierId(supplierId);

		Supplier supplier = supplierService.getSupplier(supplierId);
		if (supplier.getDefaultPriceBreakID() != null) {
			supplierMap.setPriceBreakId(supplier.getDefaultPriceBreakID());
		}

		DataRequest dataRequest = supplier.getDataRequest();
		if (dataRequest != null && dataRequest.getSignupMethod() == SignupMethod.AutoApprove) {
			supplierMap.setStatus(ApprovalStatus.Approved);
		}
		modelMap.put("supplierName", supplier.getName());
		modelMap.put("data", supplierMap);
		modelMap.put("serialNo", serialNo);
		modelMap.put("oldSupplierId", supplierId);

		return "reseller-portal/supplier/reseller-supplier-map-add";
	}

	@RequiresPermissions({ "resellerportal:request_supplier"})
	@RequestMapping(value = "/supplier/{supplierId}/detail", method = RequestMethod.GET)
	public String detail(@PathVariable int supplierId, HttpServletRequest request, ModelMap modelMap) {
		Supplier supplier = supplierService.getSupplier(supplierId);
		modelMap.put("supplier", supplier);
		modelMap.put("supplierUser", supplierService.getSupplierUser(supplier.getUserId()));
		this.setPhonePrefix(modelMap, supplier);
		modelMap.put("supplierId", supplierId);
		return "reseller-portal/supplier/supplierDetails";
	}

	private void setPhonePrefix(ModelMap modelMap, Supplier supplier) {
		if (supplier != null) {
			AddressItem addressItem = addressService.getAddressItem(supplier.getCountry());
			modelMap.put("phonePrefix", addressItem != null ? addressItem.getPhoneAreaCodeBind() : "");
		}
	}

	@RequiresPermissions({ "resellerportal:request_supplier"})
	@RequestMapping(value = "/supplier/{supplierId}/branches", method = RequestMethod.GET)
	public String branches(@PathVariable int supplierId, ModelMap model, String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		Pair<Long, List<SupplierBranch>> r = supplierService.queryBranches(filter, supplierId, pageSize, pageNo);
		String urlfmt = "%s/supplier/%s/branches?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.RESELLER,supplierId, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r
				.getKey().intValue()));
		model.put("data", r.getValue());
		model.put("supplierId", supplierId);
		Supplier supplier = supplierService.getSupplier(supplierId);
		model.put("supplier", supplier);
		return "reseller-portal/supplier/branches";
	}

}
