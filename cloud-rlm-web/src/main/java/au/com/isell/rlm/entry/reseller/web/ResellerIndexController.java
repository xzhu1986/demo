package au.com.isell.rlm.entry.reseller.web;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.paging.Paging2;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.EntryFuncPath;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.jbe.service.JobService;
import au.com.isell.rlm.module.jbe.vo.JobSearchFilter;
import au.com.isell.rlm.module.jbe.vo.JobSearchVals;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.UserService;

@Controller
@RequestMapping(value = EntryPath.RESELLER)
public class ResellerIndexController {
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private JobService jobService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private UserService userService;

	@RequestMapping(value = EntryFuncPath.Home)
	public String home(ModelMap modelMap) {
		return "forward:account/summary";
	}


	@RequestMapping("/downloads")
	public String downloads(String folder, ModelMap modelMap) {
//		if (SecurityUtils.getSubject().isPermitted("resellerportal:download_beta_update")){
//			modelMap.put("restrictedFiles", resellerService.getResellerManualFiles("restricted/"));
//		}
		if (SecurityUtils.getSubject().isPermitted("resellerportal:download_update")){
			modelMap.put("standardFiles", resellerService.getResellerManualFiles("standard/"));
		}
		return "reseller-portal/downloads";
	}

	@RequestMapping("/jobs")
	public String jobs(ModelMap model,String filter,Integer pageSize, Integer pageNo, HttpServletRequest request) {
		pageSize= pageSize==null? 25:pageSize;
		pageNo= pageNo==null? 1:pageNo;
		
		JobSearchVals searchVals = BeanUtils.constructBean(request,JobSearchVals.class, null);
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		if(StringUtils.isEmpty(filter)){
			filter=JobSearchFilter.All.name();
			model.put("filter", filter);
		}
		
		Pair<Integer,Collection> r = jobService.queryJobs(filter, searchVals, pageSize, pageNo);
		String urlfmt = "%s/jobs?%s";
		model.put("paging", new Paging2(String.format(urlfmt, EntryPath.RESELLER, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize, r.getKey().intValue()));
		model.put("data", r.getValue());
		return "reseller-portal/jobs/resellerJobs";
	}
	
	@RequestMapping("/contact-us")
	public String contactUs(ModelMap modelMap) {
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		UUID agentId=resellerService.getReseller(user.getSerialNo()).getAgencyId();
		Agent agent=agentService.getAgent(agentId.toString());
		modelMap.put("data", agent);
		
		UUID salerepid=resellerService.getReseller(user.getSerialNo()).getSalesRepID();
		User salesRep=userService.getUser(salerepid.toString());
		modelMap.put("accountManager",salesRep);
		return "reseller-portal/contactus";
	}
	
	@RequestMapping("/terms")
	public String terms() {
		return "reseller-portal/terms";
	}
	
	@RequestMapping("/privacy")
	public String privacy() {
		return "reseller-portal/privacy";
	}

	
}
