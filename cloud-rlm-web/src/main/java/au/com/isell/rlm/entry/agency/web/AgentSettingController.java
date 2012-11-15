package au.com.isell.rlm.entry.agency.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.paging.Paging2;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.agent.vo.AgentSearchVals;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;
import au.com.isell.rlm.module.mail.service.EmailTemplateService;
import au.com.isell.rlm.module.mail.vo.EmailTplSearchVals;
import au.com.isell.rlm.module.system.constant.SettingEntryPath;
import au.com.isell.rlm.module.system.domain.Currency;
import au.com.isell.rlm.module.system.service.SystemService;
import au.com.isell.rlm.module.system.vo.CountrySearchVals;
import au.com.isell.rlm.module.system.vo.CurrencySearchVals;

@Controller
@RequestMapping(value = EntryPath.AGENT + "/settings")
public class AgentSettingController {
	private static final int DEFAULT_FIRST_PAGE = 1;
	private static final int DEFAULT_PAGE_SIZE = 25;
	@Autowired
	private SystemService systemService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private EmailTemplateService emailTemplateService;

	@RequestMapping(method = RequestMethod.GET)
	public String index() {
		return String.format("forward:%s/settings/%s", EntryPath.AGENT, SettingEntryPath.users);
	}

	@RequestMapping(value = "/" + SettingEntryPath.countries)
	public String getCountries(ModelMap model, @ModelAttribute CountrySearchVals searchVals, Integer pageSize, Integer pageNo,
			HttpServletRequest request) {
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		pageNo = pageNo == null ? DEFAULT_FIRST_PAGE : pageNo;

		model.put("category", SettingEntryPath.countries);
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		String urlfmt = "%s/settings/%s?%s";
		Pair<Long, List<AddressItem>> result = systemService.searchCountry(searchVals, false, pageSize, pageNo);
		Paging2 paging = new Paging2(
				String.format(urlfmt, EntryPath.AGENT, SettingEntryPath.countries, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize,
				result.getKey().intValue());

		model.put("data", result.getValue());
		model.put("paging", paging);
		return "agent/setting-terms/countries";
	}

	@RequestMapping(value = "/" + SettingEntryPath.currencies)
	public String getCurrencies(ModelMap model, @ModelAttribute CurrencySearchVals searchVals, Integer pageSize, Integer pageNo,
			HttpServletRequest request) {
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		pageNo = pageNo == null ? DEFAULT_FIRST_PAGE : pageNo;

		model.put("category", SettingEntryPath.currencies);
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		String urlfmt = "%s/settings/%s?%s";
		Pair<Long, List<Currency>> result = systemService.searchCurrency(searchVals, pageSize, pageNo);
		Paging2 paging = new Paging2(
				String.format(urlfmt, EntryPath.AGENT, SettingEntryPath.currencies, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize,
				result.getKey().intValue());

		model.put("data", result.getValue());
		model.put("paging", paging);
		return "agent/setting-terms/currencies";
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents)
	public String getAgenets(ModelMap model, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		AgentSearchVals searchVals = BeanUtils.constructBean(request, AgentSearchVals.class, null);
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		pageNo = pageNo == null ? DEFAULT_FIRST_PAGE : pageNo;

		model.put("category", SettingEntryPath.agents);
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		String urlfmt = "%s/settings/%s?%s";
		Pair<Long, List<Agent>> result = agentService.search(searchVals, pageSize, pageNo);
		Paging2 paging = new Paging2(String.format(urlfmt, EntryPath.AGENT, SettingEntryPath.agents, UrlUtils.joinParam4Url(requestMap, "pageNo")),
				pageNo, pageSize, result.getKey().intValue());

		model.put("data", result.getValue());
		model.put("paging", paging);
		return "agent/setting-terms/agents";
	}
	
	@RequestMapping(value = "/" + SettingEntryPath.emailTemplate)
	public String getEmailTemplates(ModelMap model, Integer pageSize, Integer pageNo, HttpServletRequest request) {
		EmailTplSearchVals searchVals = BeanUtils.constructBean(request, EmailTplSearchVals.class, null);
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		pageNo = pageNo == null ? DEFAULT_FIRST_PAGE : pageNo;
		
		model.put("category", SettingEntryPath.emailTemplate);
		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);
		
		String urlfmt = "%s/settings/%s?%s";
		Pair<Long, List<EmailTemplate>> result = emailTemplateService.search(searchVals, pageSize, pageNo);
		Paging2 paging = new Paging2(String.format(urlfmt, EntryPath.AGENT, SettingEntryPath.emailTemplate, UrlUtils.joinParam4Url(requestMap, "pageNo")),
				pageNo, pageSize, result.getKey().intValue());
		
		model.put("data", result.getValue());
		model.put("paging", paging);
		return "agent/setting-terms/email-templates";
	}
	
	
}
