package au.com.isell.rlm.entry.agency.web;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.module.address.constant.CountryCodeAddressMapping;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.domain.InvoiceTermsTemplate;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;
import au.com.isell.rlm.module.mail.service.EmailTemplateService;
import au.com.isell.rlm.module.report.constant.ReportFormat;
import au.com.isell.rlm.module.report.constant.ReportType;
import au.com.isell.rlm.module.system.constant.SettingEntryPath;
import au.com.isell.rlm.module.system.domain.Currency;
import au.com.isell.rlm.module.system.service.SystemService;
import au.com.isell.rlm.module.system.vo.CurrencySearchVals;

@Controller
@RequestMapping(value = ModulePath.AGENT_SETTINGS)
public class SettingController {
	@Autowired
	private SystemService systemService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private EmailTemplateService emailTemplateService;

	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.countries + "/{code}", method = RequestMethod.GET)
	public String getCountry(@PathVariable String code, ModelMap modelMap) {
		if (StringUtils.isBlank(code) || "new".equals(code)) {
			AddressItem country = new AddressItem();
			country.setType(AddressItem.TYPE_COUNTRY);
			modelMap.put("country", country);
		} else {
			AddressItem country = addressService.getAddressItem(code);
			modelMap.put("country", country);

			List<AddressItem> regions = addressService.getSubItems(code);
			String regionType = null, showRegion = null;
			if (regions != null && regions.size() > 0) {
				showRegion = "checked";
			}
			modelMap.put("showRegion", showRegion);
			modelMap.put("regionType", regionType);
			modelMap.put("regionData", regions);
		}
		modelMap.put("code", code);
		return "agent/module/settings/countryDetails";
	}

	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.currencies + "/{code}", method = RequestMethod.GET)
	public String getCurrency(@PathVariable String code, ModelMap modelMap) {
		if (StringUtils.isBlank(code) || "new".equals(code)) {
			Currency currency = new Currency();
			modelMap.put("currency", currency);
		} else {
			Currency currency = systemService.getCurrency(code);
			modelMap.put("currency", currency);
		}
		modelMap.put("code", code);
		return "agent/module/settings/currencyDetails";
	}

	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.currencies + "/query", method = RequestMethod.GET)
	public Result queryCurrencys(String query, ModelMap modelMap) {
		CurrencySearchVals searchVals = new CurrencySearchVals();
		searchVals.setName(query);
		List<Currency> list = systemService.searchCurrency(searchVals, 50, 1).getValue();
		return new Result("finish", list != null ? list.size() : 0, list);
	}

	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}", method = RequestMethod.GET)
	public String getAgent(@PathVariable String code, ModelMap modelMap, String country) {
		Agent agent = null;
		if ("new".equals(code)) {
			Assert.notNull(country);
			agent = new Agent(country).init();
		} else {
			agent = agentService.getAgent(code);
		}
		modelMap.put("data", agent);
		modelMap.put("agentId", code);
		return "agent/module/settings/agentDetails";
	}
	
	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.emailTemplate + "/{code}", method = RequestMethod.GET)
	public String getEmailTemplate(@PathVariable String code, ModelMap modelMap) {
		EmailTemplate data = null;
		if ("new".equals(code)) {
			data = new EmailTemplate();
		} else {
			data = emailTemplateService.getEmailTpl(code);
			if(data.getTarget()!=null)
				modelMap.put("targetParams", data.getTarget().getParameterDef());
		}
		modelMap.put("data", data);
		modelMap.put("code", code);
		return "agent/module/settings/email/emailTplDetail";
	}
	
	@RequestMapping(value = "/" + SettingEntryPath.emailTemplate + "/{code}", method = RequestMethod.DELETE)
	public Result deleteEmailTemplate(@PathVariable String code, ModelMap modelMap) {
		emailTemplateService.deleteEmailTpl(code); 
		return new Result();
	}

	@RequestMapping(value = "/" + SettingEntryPath.emailTemplate + "/{code}", method = RequestMethod.POST)
	public String saveEmailTemplate(@PathVariable String code, ModelMap modelMap, HttpServletRequest request,String allAttaches) {
		EmailTemplate formObj = BeanUtils.constructBean(request, EmailTemplate.class, null);
		if(!code.equals("new")){
			formObj.setTypeId(UUID.fromString(code));
		}
		UUID id=emailTemplateService.saveEmailTpl(formObj,JsonUtils.decode(allAttaches, List.class));
		return String.format("redirect:%s/%s/%s", ModulePath.AGENT_SETTINGS, SettingEntryPath.emailTemplate,id);
	}
	
	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/region", method = RequestMethod.GET)
	public String getAgentRegions(@PathVariable String code, ModelMap modelMap) {
		List<AgentRegion> regions = agentService.getAgentRegions(code);
		modelMap.put("data", regions);
		modelMap.put("agentId", code);
		return "agent/module/settings/agentRegions";
	}

	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/region/{regionCode}", method = RequestMethod.GET)
	public String getAgentRegion(@PathVariable String code, ModelMap modelMap, @PathVariable String regionCode) {
		AgentRegion region = null;
		if ("new".equals(regionCode)) {
			region = new AgentRegion().init();
			region.setAgentId(UUID.fromString(code));
			Agent agent = agentService.getAgent(code);
			AddressItem addressItem = addressService.getAddressItem(agent.getCountry());
			region.setTaxName(addressItem.getTaxName());
			region.setTaxRate(addressItem.getTaxRate());
		} else {
			region = agentService.getAgentRegion(UUID.fromString(code), regionCode);
		}
		modelMap.put("data", region);
		modelMap.put("agentId", code);
		return "agent/module/settings/agentRegionDetail";
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{agentId}/region", method = RequestMethod.POST)
	public String saveAgentRegion(@PathVariable String agentId, ModelMap modelMap, HttpServletRequest request, String oldRegionCode) {
		AgentRegion formObj = BeanUtils.constructBean(request, AgentRegion.class, null);
		agentService.saveAgentRegion(formObj, oldRegionCode);
		return String.format("redirect:%s/%s/%s/region/%s", ModulePath.AGENT_SETTINGS, SettingEntryPath.agents, agentId, formObj.getRegionCode());
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{agentId}/region/delete", method = RequestMethod.POST)
	public Result deleteAgentRegion(@PathVariable String agentId, String regionCodes, ModelMap modelMap) {
		agentService.deleteAgentRegions(agentId, regionCodes);
		return new Result("Operation Finished", true);
	}

	@RequestMapping(value = "/" + SettingEntryPath.countries + "/{code}", method = RequestMethod.POST)
	public String saveCountry(@PathVariable String code, HttpServletRequest request, String regionDatas) {
		AddressItem formCountry = BeanUtils.constructBean(request, AddressItem.class, "country.");
		addressService.saveAddressItem(formCountry);
		// region
		if (StringUtils.isNotBlank(regionDatas)) {
			addressService.saveRegion4Country(formCountry, regionDatas);
		}
		return String.format("redirect:%s/%s/%s", ModulePath.AGENT_SETTINGS, SettingEntryPath.countries, formCountry.getCode());
	}

	@RequestMapping(value = "/" + SettingEntryPath.countries + "/{code}", method = RequestMethod.DELETE)
	public Result deleteCountry(@PathVariable String code, HttpServletRequest request) {
		addressService.deleteAddressItem(code);
		return new Result("ok", true);
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}", method = RequestMethod.POST)
	public String saveAgent(@PathVariable String code, HttpServletRequest request) {
		String countryCode = request.getParameter("address.countryCode");
		Class<? extends Address> addrCls = CountryCodeAddressMapping.getAddressCls(countryCode);
		Address address = BeanUtils.constructBean(request, addrCls, "address.", "addressItems");
		Assert.notNull(address);
		Assert.notNull(address.getCountryCode());

		Agent agent = BeanUtils.constructBean(request, Agent.class, null);
		agent.setAddress(address);
		agent.setCountry(address.getCountryCode());
		UUID id = agentService.saveAgent(agent);
		return String.format("redirect:%s/%s/%s", ModulePath.AGENT_SETTINGS, SettingEntryPath.agents, id);
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/tax/{id}", method = RequestMethod.DELETE)
	public Result deleteAgentRate(@PathVariable String id) {
		return new Result("Operation finished", true);
	}

	@RequestMapping(value = "/" + SettingEntryPath.currencies + "/{code}", method = RequestMethod.POST)
	public String saveCurrency(@PathVariable String code, HttpServletRequest request) {
		Currency formCurrency = BeanUtils.constructBean(request, Currency.class, "currency.");
		systemService.saveCurrency(formCurrency);
		code = formCurrency.getCode();
		return String.format("redirect:%s/%s/%s", ModulePath.AGENT_SETTINGS, SettingEntryPath.currencies, code);
	}

	@RequestMapping(value = "/" + SettingEntryPath.currencies + "/{code}", method = RequestMethod.DELETE)
	public Result deleteCurrency(@PathVariable String code, HttpServletRequest request) {
		systemService.deleteCurrency(code);
		return new Result("ok", true);
	}

	@RequestMapping(value = "/" + SettingEntryPath.currencies + "/{code}/default")
	public Result saveDefaultCurrency(@PathVariable String code, HttpServletRequest request) {
		if (StringUtils.isNotBlank(code)) {
			Currency currency = systemService.getCurrency(code);
			if (currency == null) {
				currency = new Currency();
			}
			currency.setCode(code);
			List<String[]> defaultCurrencies = systemService.getDefaultCurrencies();
			if (defaultCurrencies != null) {
				for (String[] defaultCurrency : defaultCurrencies) {
					if (code.equals(defaultCurrency[1])) {
						currency.setName(defaultCurrency[0]);
						currency.setMinorUnit(defaultCurrency[2] == null ? currency.getMinorUnit() : Integer.parseInt(defaultCurrency[2]));
						break;
					}
				}
			}
			systemService.saveCurrency(currency);
			return new Result("ok", 1, systemService.getCurrencies());
		}
		return new Result("no change", true);
	}

	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/emailtemplate/{type}", method = RequestMethod.GET)
	public String getAgentEmailTemplate(@PathVariable String code, @PathVariable String type, ModelMap modelMap) {
		AgentEmailTemplate.Type emType = AgentEmailTemplate.Type.calEmailTplType(type);
		AgentEmailTemplate emailTemplate = agentService.getAgentEmailTemplate(UUID.fromString(code), emType);
		if (emailTemplate == null) {
			emailTemplate = new AgentEmailTemplate();
			emailTemplate.setAgentId(UUID.fromString(code));
			emailTemplate.setType(emType);
		}
		modelMap.put("emailTemplate", emailTemplate);
		modelMap.put("agentId", code);
		modelMap.put("type", type);
		modelMap.put("reportPathType", emType.getReportPathType().toString());
		return "agent/module/settings/agentEmailTemplate";
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/emailtemplate/{type}", method = RequestMethod.POST)
	public String saveAgentEmailTemplate(@PathVariable String code, @PathVariable String type, HttpServletRequest request) {
		AgentEmailTemplate emailTemplate = BeanUtils.constructBean(request, AgentEmailTemplate.class, null, "type");
		emailTemplate.setType(AgentEmailTemplate.Type.calEmailTplType(type));
		agentService.saveAgentEmailTemplate(emailTemplate);
		if (emailTemplate.getType() == AgentEmailTemplate.Type.DirectDebitBank) {
			emailTemplate.setType(AgentEmailTemplate.Type.DirectDebitCreditCard);
			agentService.saveAgentEmailTemplate(emailTemplate);
		}
		return String.format("redirect:%s/%s/%s/emailtemplate/%s", ModulePath.AGENT_SETTINGS, SettingEntryPath.agents, code, type);
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/download-report-template", method = RequestMethod.GET)
	public void downloadAgentReportTpl(@PathVariable String code, String type, HttpServletResponse response) throws IOException {
		ReportType reportType = (ReportType) BeanUtils.getEnum(ReportType.class, type);
		response.addHeader("Content-Disposition",
				"attachment; filename=" + reportType.getTplName(ReportFormat.PDF).substring(reportType.getTplName(ReportFormat.PDF).lastIndexOf("/") + 1));
		agentService.downloadTpl(reportType, UUID.fromString(code), response.getOutputStream());
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/report-template", method = RequestMethod.GET)
	public String getAgentReportTpl(@PathVariable String code, ModelMap modelMap) {
		modelMap.put("data", agentService.getReportTplUploadFormDatas(UUID.fromString(code)));
		modelMap.put("agentId", code);
		return "agent/module/settings/agentReportTplUpload";
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/report-template", method = RequestMethod.POST)
	public String uploadAgentReportTpl(@PathVariable String code, String type, ModelMap modelMap, @ModelAttribute("uploadForm") UploadForm form) {
		ReportType reportType = (ReportType) BeanUtils.getEnum(ReportType.class, type);
		agentService.uploadAgentReportTpl(form.getFile(), reportType, UUID.fromString(code));
		modelMap.put("agentId", code);
		return String.format("redirect:/settings/%s/%s/report-template", SettingEntryPath.agents, code);
	}

	@RequiresPermissions("setting:view")
	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/invoicetermstemplate", method = RequestMethod.GET)
	public String getAgentInvoiceTermsTemplate(@PathVariable String code, ModelMap modelMap) {
		InvoiceTermsTemplate invoiceTermsTemplate = agentService.getAgent(code).getInvoiceTermsTemplate();
		modelMap.put("invoiceTermsTemplate", invoiceTermsTemplate);
		modelMap.put("agentId", code);
		return "agent/module/settings/agentInvoiceTermsTemplate";
	}

	@RequestMapping(value = "/" + SettingEntryPath.agents + "/{code}/invoicetermstemplate", method = RequestMethod.POST)
	public String saveAgentInvoiceTermsTemplate(@PathVariable String code, HttpServletRequest request) {
		InvoiceTermsTemplate invoiceTermsTemplate = BeanUtils.constructBean(request, InvoiceTermsTemplate.class, null);
		Agent agent = agentService.getAgent(code);
		if (agent != null) {
			agent.setInvoiceTermsTemplate(invoiceTermsTemplate);
		}
		agentService.saveAgent(agent);
		return String.format("redirect:%s/%s/%s/invoicetermstemplate", ModulePath.AGENT_SETTINGS, SettingEntryPath.agents, code);
	}
}

class UploadForm {

	private String name = null;
	private CommonsMultipartFile file = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CommonsMultipartFile getFile() {
		return file;
	}

	public void setFile(CommonsMultipartFile file) {
		this.file = file;
		this.name = file.getOriginalFilename();
	}
}