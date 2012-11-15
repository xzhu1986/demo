package au.com.isell.rlm.entry.agency.web;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.util.Formatter;
import au.com.isell.common.util.WebUtils;
import au.com.isell.common.xml.SerializeUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ControllerConstant;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.common.freemarker.func.EnumHelper;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.common.utils.I18NUtils;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.invoice.domain.Invoice;
import au.com.isell.rlm.module.invoice.domain.Invoice.AmountType;
import au.com.isell.rlm.module.invoice.domain.Invoice.BusinessType;
import au.com.isell.rlm.module.invoice.domain.Invoice.PaymentProbaility;
import au.com.isell.rlm.module.invoice.domain.Invoice.PaymentTerm;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups;
import au.com.isell.rlm.module.invoice.domain.InvoiceSchedule;
import au.com.isell.rlm.module.invoice.service.InvoiceService;
import au.com.isell.rlm.module.invoice.vo.InvoiceAutoSchedulesDesc;
import au.com.isell.rlm.module.invoice.vo.InvoiceStatisticReportType;
import au.com.isell.rlm.module.mail.vo.MailPreview;
import au.com.isell.rlm.module.report.constant.ReportType;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.system.domain.Currency;
import au.com.isell.rlm.module.system.service.SystemService;
import au.com.isell.rlm.module.user.domain.User;

import com.google.gson.Gson;

/**
 * @author frankw 28/03/2012
 */
@Controller
@RequestMapping(value = ModulePath.AGENT_INVOICE)
public class InvoiceController {
	@Autowired
	private InvoiceService invoiceService;
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private SystemService systemService;

	@RequiresPermissions("invoice:view")
	@RequestMapping(value = "/{invoiceNumber}", method = RequestMethod.GET)
	public String invoice(@PathVariable String invoiceNumber, ModelMap modelMap, Integer resellerSerialNo, HttpServletRequest request) {
		Invoice invoice = null;// resellerSerialNo=4400002;
		List<InvoiceSchedule> schedules = null;
		List<InvoiceFollowups> followups = null;
		if ("new".equalsIgnoreCase(invoiceNumber)) {
			Assert.notNull(resellerSerialNo);
			invoice = new Invoice();
			invoice.setResellerSerialNo(resellerSerialNo);
			invoice = invoiceService.initInvoice4Create(invoice,true);

		} else {
			Assert.isTrue(NumberUtils.isNumber(invoiceNumber));
			Integer invoiceNum = Integer.parseInt(invoiceNumber);
			invoice = invoiceService.getInvoice(invoiceNum);
			schedules = invoiceService.getInvoiceSchedules(invoiceNum);
			followups = invoiceService.getinInvoiceFollowups(invoiceNum);
			resellerSerialNo = invoice.getResellerSerialNo();
			if (invoice.getTaxRate() != null) {
				modelMap.put("taxRateDisp", invoice.getTaxRate() + "% " + (invoice.getTaxName() == null ? "" : invoice.getTaxName()));
			} else {
				Pair<BigDecimal, String> r = getTaxRateDisp(invoice.getAgentId(), invoice.getRegionCode());
				if (r != null) {
					modelMap.put("taxRateDisp", r.getValue());
				}
			}
			BigDecimal totalPaid = BigDecimal.ZERO;
			BigDecimal totalOwning = BigDecimal.ZERO;
			if (schedules != null && schedules.size() > 0) {
				for (InvoiceSchedule schedule : schedules) {
					if (schedule.isPaid()) {
						totalPaid = totalPaid.add(schedule.getScheduledAmount());
					} else {
						totalOwning = totalOwning.add(schedule.getScheduledAmount());
					}
				}
			}
			modelMap.put("totalPaid", totalPaid);
			modelMap.put("totalOwning", totalOwning);

		}

		Reseller reseller = resellerService.getReseller(resellerSerialNo);
		String countryCode = reseller.getCountry();
		// UUID agentId = null;
		// String currency = null;
		// if ("new".equals(invoiceNumber)) {
		// agentId = reseller.getAgencyId();
		// invoice.setRegionCode(reseller.getAddress().getRegion());
		// BillingInfo billingInfo = reseller.getBillingInfo();
		// currency = billingInfo != null ? billingInfo.getCurrency() : null;
		// } else {
		// agentId = invoice.getAgentId();
		// currency = invoice.getCurrency();
		// }
		modelMap.put("countryCode", countryCode);
		modelMap.put("phonePrefix", addressService.getAddressItem(countryCode).getPhoneAreaCodeBind());
		modelMap.put("agentId", invoice.getAgentId());
		modelMap.put("resellerSerialNo", resellerSerialNo);
		setCurrency(modelMap, invoice.getCurrency());
		modelMap.put("regionName", addressService.getRegionDisplayNames(invoice.getRegionCode()));
		if (invoice.getProbability() == null)
			invoice.setProbability(PaymentProbaility.Unconfirmed);

		modelMap.put("invoice", invoice);
		modelMap.put("schedules", schedules);
		modelMap.put("followups", followups);

		if (invoice.getAmounts() != null) {
			Map<String, BigDecimal> amounts = new HashMap<String, BigDecimal>();
			for (Map.Entry<AmountType, BigDecimal> entry : invoice.getAmounts().entrySet()) {
				amounts.put(entry.getKey().toString(), entry.getValue());
			}
			modelMap.put("amounts", amounts);

		}
		modelMap.put("basePath", WebUtils.getBasePath(request));
		modelMap.put("invoiceNumber", invoiceNumber);
		return "agent/module/invoice/invoice-detail";
	}

	@RequestMapping(value = "/quick-create/{serialNo}", method = RequestMethod.GET)
	public String getPage4QuickInvoiceCreate(@PathVariable String serialNo, ModelMap modelMap, String businessType) {
		Reseller reseller = resellerService.getReseller(serialNo);
		boolean hasInoivce = invoiceService.hasInvoice(Integer.valueOf(serialNo));
		Map<LicenseType, LicModule> liMap = reseller.getLicenses();
		List<Map> r = new ArrayList<Map>();
		for (LicenseType licenseType : LicenseType.values()) {
			LicModule lm = liMap.get(licenseType);
			Map m = new HashMap();
			m.put("key", licenseType.name());
			m.put("name", getDisplayName(licenseType));
			m.put("value", lm);
			r.add(m);
		}
		modelMap.put("data", r);
		modelMap.put("hasInoivce", hasInoivce);
		modelMap.put("currency", reseller.getBillingInfo().getCurrency());
		Date renewDate = resellerService.getLatestRenewDate(Integer.valueOf(serialNo));
		if (renewDate != null)
			renewDate = DateUtils.addYears(renewDate, 1);
		modelMap.put("renewDate", renewDate);
		modelMap.put("serialNo", serialNo);
		modelMap.put("businessType", businessType);
		return "agent/module/invoice/renewalInvoice";
	}

	private String getDisplayName(LicenseType type) {
		String s = "";

		switch (type) {
		case ITQLicense:
			s = "ITQuoter";
			break;
		case EComLicense:
			s = "Ecommence";
			break;
		case EPDLicense:
			s = "EPD";
			break;
		default:
			break;
		}
		return s;
	}

	private Gson gson = SerializeUtils.getDefaultGson().create();

	@RequestMapping(value = "/quick-create/{serialNo}", method = RequestMethod.POST)
	public Result createInvoiceByRenewal(@PathVariable String serialNo, ModelMap modelMap, String nextRenewal, String lics, String businessType) {
		Integer invoiceNo = invoiceService.quickCreateInvoice(gson.fromJson(nextRenewal, Map.class), gson.fromJson(lics, Map.class),
				Integer.valueOf(serialNo), businessType);
		Result r = new Result(Formatter.BraceNumber.format("/invoice/{0}", String.valueOf(invoiceNo)));
		return r;
	}

	private void setCurrency(ModelMap modelMap, String currency) {
		modelMap.put("currency", currency);
		modelMap.put("currencySymbol", "");
		if (StringUtils.isNotBlank(currency)) {
			Currency currencyObj = systemService.getCurrency(currency);
			if (currencyObj != null && StringUtils.isNotBlank(currencyObj.getSymbol())) {
				modelMap.put("currencySymbol", currencyObj.getSymbol());
			}
		}
	}

	private Pair<BigDecimal, String> getTaxRateDisp(UUID agentId, String regionCode) {
		String taxRateDisp = null;
		AgentRegion region = agentService.getAgentRegion(agentId, regionCode);
		if (region != null) {
			taxRateDisp = (region.getTaxRate() + "% " + (region.getTaxName() == null ? "" : region.getTaxName()));
			return new Pair<BigDecimal, String>(region.getTaxRate(), taxRateDisp);
		}
		return null;
	}

	@RequestMapping("/tax-rate-disp/{agentId}/{regionCode}")
	public Result loadTaxRateDisp(@PathVariable String agentId, @PathVariable String regionCode) {
		Result r = new Result("success", 1, getTaxRateDisp(UUID.fromString(agentId), regionCode));
		return r;
	}

	@RequestMapping(value = "/{invoiceNumber}", method = RequestMethod.POST)
	public String save(@PathVariable String invoiceNumber, ModelMap modelMap, HttpServletRequest request) {
		Invoice invoice = BeanUtils.constructBean(request, Invoice.class, null);
		if (invoice.getBusinessType() == null) {
			invoice.setBusinessType(BusinessType.Renewal);
		}
		Map<AmountType, BigDecimal> amounts = getAmounts(request);
		invoice.setAmounts(amounts);
		int savedId = invoiceService.saveInvoice(invoice);
		return String.format("redirect:%s/%s", ModulePath.AGENT_INVOICE, savedId);
	}

	private Map<AmountType, BigDecimal> getAmounts(HttpServletRequest request) {
		String amountPrefix = "amount.";
		Map<AmountType, BigDecimal> amounts = new HashMap<Invoice.AmountType, BigDecimal>();
		for (AmountType amountType : AmountType.values()) {
			String strV = request.getParameter(amountPrefix + amountType.toString());
			if (StringUtils.isNotEmpty(strV) && NumberUtils.isNumber(strV)) {
				amounts.put(amountType, new BigDecimal(strV));
			}
		}
		return amounts;
	}

	@RequestMapping(value = "/{invoiceNumber}/schedule/{dueDate}", method = RequestMethod.GET)
	public String invoiceSchedule(@PathVariable Integer invoiceNumber, @PathVariable String dueDate, HttpServletRequest request, ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		InvoiceSchedule schedule = null;
		if ("new".equalsIgnoreCase(dueDate)) {
			Invoice invoice = invoiceService.getInvoice(invoiceNumber);
			schedule = new InvoiceSchedule();
			schedule.setInvoiceNumber(invoiceNumber);
			PaymentTerm invoicePaymentTerm = invoice.getPaymentTerm();
			if (invoicePaymentTerm == Invoice.PaymentTerm.DueNow) {
				schedule.setDueDate(DatePicker.pickDay(invoice.getInvoiceDate(), 0));
			} else if (invoicePaymentTerm == Invoice.PaymentTerm.Day7) {
				schedule.setDueDate(DatePicker.pickDay(invoice.getInvoiceDate(), 7));
			} else if (invoicePaymentTerm == Invoice.PaymentTerm.Day14) {
				schedule.setDueDate(DatePicker.pickDay(invoice.getInvoiceDate(), 14));
			} else if (invoicePaymentTerm == Invoice.PaymentTerm.Day30) {
				schedule.setDueDate(DatePicker.pickDay(invoice.getInvoiceDate(), 30));
			} else if (invoicePaymentTerm == Invoice.PaymentTerm.MonthDay1) {
				schedule.setDueDate(DatePicker.pickFirstDayOfMonth(invoice.getInvoiceDate(), 1));
			} else if (invoicePaymentTerm == Invoice.PaymentTerm.MonthDay23) {
				if (DatePicker.getCalendarInstance().before(DatePicker.pick23DayOfMonth(invoice.getInvoiceDate(), 0))) {
					schedule.setDueDate(DatePicker.pick23DayOfMonth(invoice.getInvoiceDate(), 0));
				} else {
					schedule.setDueDate(DatePicker.pick23DayOfMonth(invoice.getInvoiceDate(), 1));
				}
			} else if (invoicePaymentTerm == Invoice.PaymentTerm.MonthEnd) {
				schedule.setDueDate(DatePicker.pickLastDayOfMonth(invoice.getInvoiceDate(), 0));
			}
			schedule.setScheduledAmount(invoice.getTotalAmountInc());
		} else {
			Assert.isTrue(NumberUtils.isNumber(dueDate));
			schedule = invoiceService.getInvoiceSchedule(invoiceNumber, Long.parseLong(dueDate));
		}
		modelMap.put("schedule", schedule);
		modelMap.put("invoiceNumber", invoiceNumber);
		modelMap.put(ControllerConstant.DATA_SAVED_FLAG, request.getParameter(ControllerConstant.DATA_SAVED_FLAG));
		return "agent/module/invoice/invoice-schedule-detail";
	}

	@RequestMapping(value = "/{invoiceNumber}/schedule/{dueDate}", method = RequestMethod.POST)
	public String saveInvoiceSchedule(@PathVariable Integer invoiceNumber, @PathVariable String dueDate, HttpServletRequest request, ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		InvoiceSchedule formSchedule = BeanUtils.constructBean(request, InvoiceSchedule.class, null);
		if ("new".equalsIgnoreCase(dueDate)) {
			invoiceService.saveInvoiceSchedules(invoiceNumber, formSchedule);

		} else {
			InvoiceSchedule dbSchedule = invoiceService.getInvoiceSchedule(invoiceNumber, Long.parseLong(dueDate));
			if (dbSchedule.getDueDate().getTime() != formSchedule.getDueDate().getTime()) {
				invoiceService.deleteInvoiceSchedule(invoiceNumber, dueDate);
			}
			invoiceService.saveInvoiceSchedules(invoiceNumber, formSchedule);
		}
		return String.format("redirect:%s/%s/schedule/%s?%s", ModulePath.AGENT_INVOICE, formSchedule.getInvoiceNumber(), formSchedule.getDueDate()
				.getTime(), ControllerConstant.DATA_SAVED_FLAG + "=true");
	}

	@RequestMapping(value = "/{invoiceNumber}/schedule/{dueDates}", method = RequestMethod.DELETE)
	@ResponseBody
	public Result deleteInvoiceSchedule(@PathVariable Integer invoiceNumber, @PathVariable String dueDates) {
		Assert.notNull(invoiceNumber);
		String[] dueDate = dueDates.split(",");
		invoiceService.deleteInvoiceSchedule(invoiceNumber, dueDate);
		return new Result("success", true);
	}

	@RequestMapping(value = "/{invoiceNumber}/schedulelist", method = RequestMethod.GET)
	public String invoiceScheduleList(@PathVariable Integer invoiceNumber, HttpServletRequest request, ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		List<InvoiceSchedule> schedules = invoiceService.getInvoiceSchedules(invoiceNumber);
		modelMap.put("schedules", schedules);
		modelMap.put("invoiceNumber", invoiceNumber);
		modelMap.put("basePath", WebUtils.getBasePath(request));
		Invoice invoice = invoiceService.getInvoice(invoiceNumber);
		setCurrency(modelMap, invoice.getCurrency());
		BigDecimal totalOwning = BigDecimal.ZERO;
		BigDecimal totalPaid = BigDecimal.ZERO;
		if (schedules != null && schedules.size() > 0) {
			for (InvoiceSchedule schedule : schedules) {
				if (schedule.isPaid()) {
					totalPaid = totalPaid.add(schedule.getScheduledAmount());
				} else {
					totalOwning = totalOwning.add(schedule.getScheduledAmount());
				}
			}
		}
		modelMap.put("totalPaid", totalPaid);
		modelMap.put("totalOwning", totalOwning);
		return "agent/module/invoice/invoice-schedule-list";
	}

	@RequestMapping(value = "/{invoiceNumber}/autoschedules", method = RequestMethod.GET)
	public String invoiceAutoSchedules(@PathVariable Integer invoiceNumber, HttpServletRequest request, ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		InvoiceAutoSchedulesDesc autoScheduleDesc = invoiceService.createBlankAutoScheduleDesc(invoiceNumber);
		modelMap.put("autoScheduleDesc", autoScheduleDesc);
		modelMap.put("invoiceNumber", invoiceNumber);
		modelMap.put(ControllerConstant.DATA_SAVED_FLAG, request.getParameter(ControllerConstant.DATA_SAVED_FLAG));
		return "agent/module/invoice/invoice-auto-schedule-detail";
	}

	@RequestMapping(value = "/{invoiceNumber}/autoschedules", method = RequestMethod.POST)
	public String saveInvoiceAutoSchedules(@PathVariable Integer invoiceNumber, HttpServletRequest request, ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		InvoiceAutoSchedulesDesc autoScheduleDesc = BeanUtils.constructBean(request, InvoiceAutoSchedulesDesc.class, null);
		invoiceService.createInvoiceScheduleByAutoDesc(invoiceNumber, autoScheduleDesc);
		return String.format("redirect:%s/%s/autoschedules?%s", ModulePath.AGENT_INVOICE, invoiceNumber, ControllerConstant.DATA_SAVED_FLAG + "=true");
	}

	@RequestMapping(value = "/{invoiceNumber}/followup/{followupDate}", method = RequestMethod.GET)
	public String invoiceFollowup(@PathVariable Integer invoiceNumber, @PathVariable String followupDate, HttpServletRequest request,
			ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		InvoiceFollowups followup = null;
		if ("new".equalsIgnoreCase(followupDate)) {
			followup = new InvoiceFollowups();
			followup.setInvoiceNumber(invoiceNumber);
			followup.setFollowupDate(DatePicker.getCalendarInstance().getTime());
			User loginUser = GlobalAttrManager.getCurrentUser();
			followup.setUserId(loginUser.getUserId());
		} else {
			Assert.isTrue(NumberUtils.isNumber(followupDate));
			followup = invoiceService.getInvoiceFollowup(invoiceNumber, Long.parseLong(followupDate));
		}
		modelMap.put("followup", followup);
		modelMap.put("followupDate", followupDate);
		modelMap.put("invoiceNumber", invoiceNumber);
		modelMap.put(ControllerConstant.DATA_SAVED_FLAG, request.getParameter(ControllerConstant.DATA_SAVED_FLAG));
		return "agent/module/invoice/invoice-followup-detail";
	}

	@RequestMapping(value = "/{invoiceNumber}/followup/check-duplicate", method = RequestMethod.GET)
	public Result checkDuplicateFollowup(@PathVariable Integer invoiceNumber, String followupDate) {
		try {
			Date date = (Date) ConvertUtils.convert(URLDecoder.decode(followupDate, "UTF-8"), Date.class);
			InvoiceFollowups followup = invoiceService.getInvoiceFollowup(invoiceNumber, date.getTime());
			if (followup != null) {
				return new Result("this follow up is exists!", false);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return new Result();
	}

	@RequestMapping(value = "/{invoiceNumber}/followup/{followupDate}", method = RequestMethod.POST)
	public String saveInvoiceFollowup(@PathVariable Integer invoiceNumber, @PathVariable String followupDate, HttpServletRequest request,
			ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		InvoiceFollowups formFollowup = BeanUtils.constructBean(request, InvoiceFollowups.class, null);
		if ("new".equalsIgnoreCase(followupDate)) {
			invoiceService.saveInvoiceFollowup(formFollowup);
		} else {
			InvoiceFollowups dbFollowup = invoiceService.getInvoiceFollowup(invoiceNumber, Long.parseLong(followupDate));
			if (dbFollowup.getFollowupDate().getTime() != formFollowup.getFollowupDate().getTime()) {
				invoiceService.deleteInvoiceFollowups(invoiceNumber, followupDate);
			}
			invoiceService.saveInvoiceFollowup(formFollowup);
		}
		return String.format("redirect:%s/%s/followup/%s?%s", ModulePath.AGENT_INVOICE, formFollowup.getInvoiceNumber(), formFollowup.getFollowupDate()
				.getTime(), ControllerConstant.DATA_SAVED_FLAG + "=true");
	}

	@RequestMapping(value = "/{invoiceNumber}/followuplist", method = RequestMethod.GET)
	public String invoiceFollowupList(@PathVariable Integer invoiceNumber, HttpServletRequest request, ModelMap modelMap) {
		Assert.notNull(invoiceNumber);
		List<InvoiceFollowups> followups = invoiceService.getinInvoiceFollowups(invoiceNumber);
		modelMap.put("followups", followups);
		modelMap.put("invoiceNumber", invoiceNumber);
		modelMap.put("basePath", WebUtils.getBasePath(request));
		return "agent/module/invoice/invoice-followup-list";
	}

	@RequestMapping(value = "/{invoiceNumber}/report/mail/{emailTplType}", method = RequestMethod.GET)
	public String previewReportMail(@PathVariable Integer invoiceNumber, @PathVariable String emailTplType, ModelMap modelMap) {
		modelMap.put("invoiceNumber", invoiceNumber);
		modelMap.put("emailTplType", emailTplType);
		modelMap.put("reportPathType", AgentEmailTemplate.Type.calEmailTplType(emailTplType).getReportPathType().toString());
		modelMap.put("mail", invoiceService.getInvoiceMailPreview(invoiceNumber, emailTplType));
		return "agent/module/invoice/invoice-email";
	}

	@RequestMapping(value = "/{invoiceNumber}/report/mail/{emailTplType}", method = RequestMethod.POST)
	public Result sendInvoiceMail(@PathVariable Integer invoiceNumber, @PathVariable String emailTplType, @ModelAttribute MailPreview mailPreview) {
		invoiceService.sendInvoiceMail(invoiceNumber, emailTplType, mailPreview);
		return new Result();
	}

	@RequestMapping(value = "/statistic-report/mail", method = RequestMethod.GET)
	public String previewStatisticReportMail(String invoiceStatisticReportType, ModelMap modelMap) {
		InvoiceStatisticReportType reportType = (InvoiceStatisticReportType) BeanUtils.getEnum(InvoiceStatisticReportType.class,
				invoiceStatisticReportType);
		modelMap.put("reportPathType", reportType.getReportPathType());
		MailPreview mailPreview = new MailPreview();
		mailPreview.setTo(GlobalAttrManager.getCurrentUser().getEmail());
		mailPreview.setSubject(I18NUtils.getMsg(EnumHelper.getDisplayCodeOrMsg(InvoiceStatisticReportType.class, reportType)));
		mailPreview.setBody("&nbsp;");
		modelMap.put("mail", mailPreview);
		modelMap.put("invoiceStatisticReportType", invoiceStatisticReportType);
		return "agent/module/invoice/invoiceStatisticsReportMailPreview";
	}

	@RequestMapping(value = "/statistic-report/mail", method = RequestMethod.POST)
	public Result sendInvoicestatisticMail(String reportPathType, @ModelAttribute MailPreview mailPreview) {
		ReportType type = (ReportType) BeanUtils.getEnum(ReportType.class, reportPathType);
		invoiceService.sendInvoiceStatisticMail(type, mailPreview);
		return new Result();
	}

	@RequestMapping(value = "/{invoiceNumber}/report/cloudpath", method = RequestMethod.GET)
	public Result previewReport(@PathVariable Integer invoiceNumber, HttpServletResponse response, String reportPathType) {
		ReportType type = (ReportType) BeanUtils.getEnum(ReportType.class, reportPathType);
		return new Result(invoiceService.previewReport(invoiceNumber, type));
	}
}
