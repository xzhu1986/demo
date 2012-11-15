package au.com.isell.rlm.entry.agency.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import au.com.isell.common.paging.Paging2;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.EntryFuncPath;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;
import au.com.isell.rlm.module.invoice.service.InvoiceService;
import au.com.isell.rlm.module.invoice.vo.InvoiceSearchFilters;
import au.com.isell.rlm.module.invoice.vo.InvoiceSearchVals;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.reseller.vo.ResellerSearchFilters;
import au.com.isell.rlm.module.reseller.vo.ResellerSearchVals;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdateExecute;
import au.com.isell.rlm.module.supplier.service.SupplierSearchService;
import au.com.isell.rlm.module.supplier.service.SupplierService;
import au.com.isell.rlm.module.supplier.vo.PriceUpdateSearchFilters;
import au.com.isell.rlm.module.supplier.vo.PriceUpdateSearchVals;
import au.com.isell.rlm.module.supplier.vo.SupplierSearchVals;

@Controller
@RequestMapping(value = EntryPath.AGENT)
public class AgentIndexController {
	@Autowired
	private SupplierSearchService supplierSearchService;

	@Autowired
	private ResellerService resellerService;
	@Autowired
	private SupplierService supplierService;
	@Autowired
	private InvoiceService invoiceService;

	@RequestMapping(value = EntryFuncPath.Home)
	public String home(ModelMap model) {
		model.put("resellerStatisticsByStatus", resellerService.getStatisticsByStatus());
		model.put("resellerStatisticsByCountry", resellerService.getStatisticsByCountry());
		model.put("supplierStatisticsByStatus", supplierService.getStatisticsByStatus());
		return EntryPath.AGENT + "/summary";
	}

	@RequiresPermissions("reseller:view")
	@RequestMapping(value = "/resellers")
	public String resellers(ModelMap model, @ModelAttribute ResellerSearchVals searchVals, String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		//default active
		if(StringUtils.isEmpty(filter) && searchVals.isEmpty()){
			filter=ResellerSearchFilters.Active.toString();
			model.put("filter", filter);
		}

		Pair<Long, List<ResellerSearchBean>> r = resellerService.query(searchVals, filter, pageSize, pageNo);
		String urlfmt = "%s/resellers?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.AGENT, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r.getKey().intValue()));
		model.put("data", r.getValue());
		return EntryPath.AGENT + "/resellers";
	}

	@RequiresPermissions("supplier:view")
	@RequestMapping(value = "/suppliers")
	public String suppliers(ModelMap model, @ModelAttribute SupplierSearchVals searchVals, String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		Pair<Long, List<SupplierSearchBean>> r = supplierSearchService.query(searchVals, filter, pageSize, pageNo);
		String urlfmt = "%s/suppliers?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.AGENT, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r.getKey().intValue()));
		model.put("data", r.getValue());
		return EntryPath.AGENT + "/suppliers";
	}

	@RequiresPermissions("invoice:view")
	@RequestMapping(value = "/invoices")
	public String invoices(ModelMap model, @ModelAttribute InvoiceSearchVals searchVals, String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		//default active
		if(StringUtils.isEmpty(filter) && searchVals.isEmpty()){
			filter=InvoiceSearchFilters.FollowupDue.name();
			model.put("filter", filter);
		}
		Pair<Long, List<InvoiceSearchBean>> r = invoiceService.query(searchVals, filter, pageSize, pageNo);
		String urlfmt = "%s/invoices?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.AGENT, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r.getKey().intValue()));
		model.put("data", r.getValue());
		return EntryPath.AGENT + "/invoices";
	}
	
	@RequiresPermissions("priceupdates:view")
	@RequestMapping(value = "/priceupdates")
	public String priceupdates(ModelMap model, @ModelAttribute PriceUpdateSearchVals searchVals, String filter, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize = pageSize == null ? 25 : pageSize;
		pageNo = pageNo == null ? 1 : pageNo;
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		//default active
		if(StringUtils.isEmpty(filter) && searchVals.isEmpty()){
			filter=PriceUpdateSearchFilters.DueNow.name();
			model.put("filter", filter);
		}
				
		Pair<Long, List<SupplierUpdateExecute>> r = supplierService.querySupplierUpdates(searchVals, filter, pageSize, pageNo);
		String urlfmt = "%s/priceupdates?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.AGENT, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r.getKey().intValue()));
		model.put("data", r.getValue());
		return EntryPath.AGENT + "/price-updates";
	}
}
