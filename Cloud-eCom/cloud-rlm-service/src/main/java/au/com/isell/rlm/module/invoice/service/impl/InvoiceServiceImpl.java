package au.com.isell.rlm.module.invoice.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.util.Formatter;
import au.com.isell.common.util.NetworkUtils;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.PaymentMethod;
import au.com.isell.rlm.common.freemarker.func.EnumHelper;
import au.com.isell.rlm.common.utils.CurrencyFormater;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.common.utils.I18NUtils;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate.Type;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.invoice.dao.InvoiceDao;
import au.com.isell.rlm.module.invoice.domain.Invoice;
import au.com.isell.rlm.module.invoice.domain.Invoice.AmountType;
import au.com.isell.rlm.module.invoice.domain.Invoice.BusinessType;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups.FollowupResult;
import au.com.isell.rlm.module.invoice.domain.InvoiceSchedule;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;
import au.com.isell.rlm.module.invoice.service.InvoiceReportService;
import au.com.isell.rlm.module.invoice.service.InvoiceService;
import au.com.isell.rlm.module.invoice.vo.InvoiceAutoSchedulesDesc;
import au.com.isell.rlm.module.invoice.vo.InvoiceSearchFilters;
import au.com.isell.rlm.module.invoice.vo.InvoiceSearchFilters4Reseller;
import au.com.isell.rlm.module.invoice.vo.InvoiceSearchVals;
import au.com.isell.rlm.module.mail.service.MailService;
import au.com.isell.rlm.module.mail.vo.MailAddress;
import au.com.isell.rlm.module.mail.vo.MailContent;
import au.com.isell.rlm.module.mail.vo.MailPreview;
import au.com.isell.rlm.module.report.constant.ReportFormat;
import au.com.isell.rlm.module.report.constant.ReportPath;
import au.com.isell.rlm.module.report.constant.ReportType;
import au.com.isell.rlm.module.report.service.ReportService;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.BillingInfo;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.license.ITQLicense;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.system.domain.Currency;
import au.com.isell.rlm.module.system.service.SystemService;
import au.com.isell.rlm.module.user.service.UserService;

/**
 * @author frankw 28/03/2012
 */
@Service
public class InvoiceServiceImpl implements InvoiceService {
	@Autowired
	private InvoiceDao invoiceDao;
	@Autowired
	private AgentService agentService;
	@Autowired
	private ResellerDao resellerDao;
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private ReportService reportService;
	@Autowired
	private MailService mailService;
	@Autowired
	private UserService userService;
	@Autowired
	private SystemService sysService;
	@Autowired
	private AddressService addrService;

	@Override
	public Invoice getInvoice(int invoiceNumber) {
		return invoiceDao.getInvoice(invoiceNumber);
	}

	@Override
	public List<InvoiceSchedule> getInvoiceSchedules(int invoiceNumber) {
		return invoiceDao.getInvoiceSchedules(invoiceNumber);
	}

	@Override
	public List<InvoiceFollowups> getinInvoiceFollowups(int invoiceNumber) {
		return invoiceDao.getinInvoiceFollowups(invoiceNumber);
	}

	@Override
	public int saveInvoice(Invoice invoice) {
		Assert.notNull(invoice.getAgentId(), "agent id can not be null");
		Assert.isTrue(StringUtils.isNotEmpty(invoice.getRegionCode()), "region code can not be empty");

		if (invoice.getInvoiceNumber() != null) {
			Invoice dbinvoice = this.getInvoice(invoice.getInvoiceNumber());
			if (invoice.getPromisedPaymentDate() != null && invoice.getPromisedPaymentDate() != dbinvoice.getPromisedPaymentDate()) {
				setNewInvoiceDate(invoice);
			}
			BeanUtils.copyPropsExcludeNull(dbinvoice, invoice);
			invoice = dbinvoice;
		} else {
			// new
			Agent agent = agentService.getAgent(invoice.getAgentId().toString());
			if (agent != null) {
				Map<String, String> tplData = getInvoiceTermsFillData(invoice);
				invoice.setTerms(Formatter.DollarBraceName.format(agent.getInvoiceTermsTemplate().getContent(), tplData));
			}
			invoice.setCreatedDate(new Date());

			if (invoice.getPromisedPaymentDate() != null) {
				setNewInvoiceDate(invoice);
			}
		}
		updateTaxRate(invoice);
		return invoiceDao.saveInvoice(invoice);
	}

	private Map<String, String> getInvoiceTermsFillData(Invoice invoice) {
		Map<String, String> tplData = new HashMap<String, String>();

		tplData.put("custfirstname", invoice.getToFirstName());
		tplData.put("custlastname", invoice.getToLastName());
		tplData.put("custfullname", invoice.getToFirstName() + " " + invoice.getToLastName());
		tplData.put("custserialnumber", invoice.getResellerSerialNo().toString());

		Reseller reseller = resellerService.getReseller(invoice.getResellerSerialNo());
		Map<LicenseType, LicModule> licenses = reseller.getLicenses();
		if (licenses != null) {
			Date largestRenewalDate = null;
			for (Iterator<LicModule> i = licenses.values().iterator(); i.hasNext();) {
				LicModule lic = i.next();
				if (lic.getRenewalDate() != null) {
					if (largestRenewalDate == null) {
						largestRenewalDate = lic.getRenewalDate();
					} else if (largestRenewalDate.before(lic.getRenewalDate())) {
						largestRenewalDate = lic.getRenewalDate();
					}
				}
			}
			tplData.put("licenseExpiryDate",
					largestRenewalDate == null ? "" : au.com.isell.common.util.DateUtil.formatDate("dd MMM yyyy", largestRenewalDate));
			ITQLicense itqLicense = (ITQLicense) licenses.get(LicenseType.ITQLicense);
			if (itqLicense != null) {
				tplData.put("totalUsers",
						String.valueOf(itqLicense.getItqFullAccessUsers() + itqLicense.getItqServiceUsers() + itqLicense.getItqCrmUsers()));
			}
		}
		return tplData;
	}

	private void updateTaxRate(Invoice invoice) {
		if (invoice.getTaxRate() != null) {
			return;
		}
		BigDecimal gst = resellerService.getReseller(invoice.getResellerSerialNo()).getBillingInfo().getGst();
		AgentRegion agentRegion = agentService.getAgentRegion(invoice.getAgentId(), invoice.getRegionCode());
		if (gst != null) {
			invoice.setTaxRate(gst);
			invoice.setTaxName(agentRegion.getTaxName());
		} else {
			invoice.setTaxRate(agentRegion.getTaxRate());
			invoice.setTaxName(agentRegion.getTaxName());
		}
	}

	private void setNewInvoiceDate(Invoice invoice) {
		if (invoice.getInvoiceDate() == null || invoice.getInvoiceDate().before(new Date())) {
			Date invoiceDate = DateUtils.addDays(invoice.getPromisedPaymentDate(), 1);
			invoice.setInvoiceDate(invoiceDate);
		}
	}

	@Override
	public Pair<Long, List<InvoiceSearchBean>> query(InvoiceSearchVals searchVals, String filter, Integer pageSize, Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = invoiceDao.getFilterMaker();

		FilterItem filterItem = searchVals.getFilter(maker);
		if (StringUtils.isNotBlank(filter)) {
			InvoiceSearchFilters filters = (InvoiceSearchFilters) BeanUtils.getEnum(InvoiceSearchFilters.class, filter);
			filterItem = maker.linkWithAnd(filterItem, filters.getFilterItem(maker));
		}
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("invoiceNumber", false);
		Pair<Long, List<InvoiceSearchBean>> r = invoiceDao.searchInvoice(filterItem, sorts, pageSize, pageNo);
		return r;
	}

	@Override
	public Pair<Long, List<InvoiceSearchBean>> query4Reseller(String filter, int serialNo) {
		// Assert.notNull(pageSize, "pageSize should not be null");
		// Assert.notNull(pageNo, "pageNo should not be null");
		Assert.notNull(filter, "filter can not be null");
		FilterMaker maker = invoiceDao.getFilterMaker();

		InvoiceSearchFilters4Reseller filters = (InvoiceSearchFilters4Reseller) BeanUtils.getEnum(InvoiceSearchFilters4Reseller.class, filter);
		FilterItem filterItem1 = filters.getFilterItem(maker);
		FilterItem filterItem2 = maker.makeNameFilter("resellerSerialNo", TextMatchOption.Is, String.valueOf(serialNo));
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("invoiceNumber", false);
		Pair<Long, List<InvoiceSearchBean>> r = invoiceDao.searchInvoice(maker.linkWithAnd(filterItem1, filterItem2), sorts, null, null);
		if (r.getValue() != null) {
			for (InvoiceSearchBean searchBean : r.getValue()) {
				searchBean.setAmounts(this.getInvoice(searchBean.getInvoiceNumber()).getAmounts());
			}
		}
		return r;
	}

	@Override
	public InvoiceSchedule getInvoiceSchedule(int invoiceNumber, long dueDate) {
		return invoiceDao.getInvoiceSchedule(invoiceNumber, dueDate);
	}

	@Override
	public InvoiceAutoSchedulesDesc createBlankAutoScheduleDesc(Integer invoiceNumber) {
		Invoice invoice = getInvoice(invoiceNumber);

		InvoiceAutoSchedulesDesc autoScheduleDesc = new InvoiceAutoSchedulesDesc();
		autoScheduleDesc.setDayOfMonth(1);
		autoScheduleDesc.setFirstScheduledDate(DatePicker.getCalendarInstance().getTime());
		autoScheduleDesc.setInvoiceTotalAmount(invoice.getTotalAmountInc());

		return autoScheduleDesc;
	}

	@Override
	public void createInvoiceScheduleByAutoDesc(Integer invoiceNumber, InvoiceAutoSchedulesDesc autoScheduleDesc) {
		InvoiceSchedule invoiceSchedule = null;
		InvoiceSchedule[] invoiceSchedules = new InvoiceSchedule[autoScheduleDesc.getScheduleds()];
		for (int i = 0; i < autoScheduleDesc.getScheduleds(); i++) {
			invoiceSchedule = new InvoiceSchedule();
			invoiceSchedule.setInvoiceNumber(invoiceNumber);
			invoiceSchedule.setPaymentType(PaymentMethod.EFT);
			if (i == 0) {
				invoiceSchedule.setDueDate(autoScheduleDesc.getFirstScheduledDate());
				invoiceSchedule.setScheduledAmount(autoScheduleDesc.getFirstScheduledAmount());
			} else {
				invoiceSchedule.setDueDate(DatePicker.pickAssignDate(autoScheduleDesc.getFirstScheduledDate(), i, autoScheduleDesc.getDayOfMonth()));
				invoiceSchedule.setScheduledAmount(autoScheduleDesc.getEachSecheduleAmount());
			}
			invoiceSchedules[i] = invoiceSchedule;
		}
		this.saveInvoiceSchedules(invoiceNumber, invoiceSchedules);
	}

	@Override
	public void saveInvoiceSchedules(int invoiceNumber, InvoiceSchedule... invoiceSchedules) {
		invoiceDao.saveInvoiceSchedules(invoiceNumber, invoiceSchedules);
	}

	@Override
	public void deleteInvoiceSchedule(int invoiceNumber, String... dueDate) {
		invoiceDao.deleteInvoiceSchedule(invoiceNumber, dueDate);
	}

	@Override
	public InvoiceFollowups getInvoiceFollowup(int invoiceNumber, long followupDate) {
		return invoiceDao.getInvoiceFollowup(invoiceNumber, followupDate);
	}

	@Override
	public void saveInvoiceFollowup(InvoiceFollowups followup) {
		invoiceDao.saveInvoiceFollowup(followup);
	}

	@Override
	public void deleteInvoiceFollowups(int invoiceNumber, String... followupDate) {
		invoiceDao.deleteInvoiceFollowups(invoiceNumber, followupDate);
	}

	@Override
	public Invoice initInvoice4Create(Invoice invoice,boolean addAmountFromReseller) {
		Reseller reseller = resellerService.getReseller(invoice.getResellerSerialNo());
		invoice.init();

		invoice.setSalesRepId(reseller.getSalesRepID());
		BillingInfo billingInfo = reseller.getBillingInfo();
		invoice.setToFirstName(billingInfo.getFirstName());
		invoice.setToLastName(billingInfo.getLastName());
		invoice.setToPhone(billingInfo.getPhone());
		invoice.setToEmail(billingInfo.getEmail());
		invoice.setToEmailcc(billingInfo.getAdditionalEmail());
		invoice.setCompanyName(reseller.getCompany());
		invoice.setCompanyAddress(reseller.getAddress());

		if(addAmountFromReseller){
		Map<LicenseType, LicModule> lics = reseller.getLicenses();
		if (lics != null) {
			putLicenseVal(invoice, lics, LicenseType.ITQLicense, AmountType.ITQuoter);
			putLicenseVal(invoice, lics, LicenseType.EComLicense, AmountType.ECom);
			putLicenseVal(invoice, lics, LicenseType.EPDLicense, AmountType.EPD);
		}
		}

		UUID agentId = reseller.getAgencyId();
		invoice.setAgentId(agentId);
		invoice.setRegionCode(reseller.getAddress().getRegion());
		invoice.setCurrency(billingInfo != null ? billingInfo.getCurrency() : null);
		return invoice;
	}

	private void putLicenseVal(Invoice invoice, Map<LicenseType, LicModule> lics, LicenseType licenseType, AmountType amountType) {
		LicModule licModule = lics.get(licenseType);
		if (licModule != null) {
			invoice.getAmounts().put(amountType, licModule.getAnnualFee());
		}
	}

	@Override
	public MailPreview getInvoiceMailPreview(Integer invoiceNumber, String emailTplType) {
		Assert.notNull(invoiceNumber);
		Assert.notNull(emailTplType);

		Type tplType = (Type) BeanUtils.getEnum(AgentEmailTemplate.Type.class, emailTplType);
		Invoice invoice = getInvoice(invoiceNumber);

		AgentEmailTemplate emailTemplate = agentService.getAgentEmailTemplate(invoice.getAgentId(), tplType);
		if (emailTemplate == null) {
			throw new RuntimeException("can not find template for report");
		}

		MailPreview mailPreview = new MailPreview();
		mailPreview.setSubject(emailTemplate.getSubject());
		mailPreview.setTo(invoice.getToEmail());
		mailPreview.setCc(invoice.getToEmailcc() );
		mailPreview.setBcc(emailTemplate.getBcc() );

		Map<String, String> tplData = getMailPreviewFillData(invoiceNumber, invoice);

		mailPreview.setBody(Formatter.DollarBraceName.format(emailTemplate.getBody(), tplData));
		return mailPreview;
	}

	private Map<String, String> getMailPreviewFillData(Integer invoiceNumber, Invoice invoice) {
		Map<String, String> tplData = new HashMap<String, String>();
		AgentUser loginUser = (AgentUser) GlobalAttrManager.getCurrentUser();
		tplData.put("userfirstname", loginUser.getFirstName());
		tplData.put("userlastname", loginUser.getLastName());
		tplData.put("userfullname", loginUser.getName());
		tplData.put("userphone", loginUser.getPhone());
		tplData.put("useremail", loginUser.getEmail());

		tplData.put("custfirstname", invoice.getToFirstName());
		tplData.put("custlastname", invoice.getToLastName());
		tplData.put("custfullname", invoice.getToFirstName() + " " + invoice.getToLastName());
		tplData.put("custserialnumber", invoice.getResellerSerialNo().toString());

		Reseller reseller = resellerService.getReseller(invoice.getResellerSerialNo());
		Currency currency = sysService.getCurrency(reseller.getBillingInfo().getCurrency());
		boolean sameCurrency = invoice.getCurrency().equals(currency.getCode());

		tplData.put("invoiceno", invoice.getInvoiceNumber().toString());
		tplData.put("invoicetotalex", formatCurrencyNum(currency, invoice.getTotalAmount(), sameCurrency));
		BigDecimal taxRate = invoice.getTaxRate().divide(new BigDecimal("100")).add(new BigDecimal("1"));
		tplData.put("invoicetotalinc",
				formatCurrencyNum(currency, invoice.getTotalAmount().multiply(taxRate).setScale(2, RoundingMode.HALF_UP), sameCurrency));
		BigDecimal blance = invoice.getTotalAmount().subtract(getInvoicePaid(invoiceNumber));
		tplData.put("invoicebalanceex", formatCurrencyNum(currency, blance.setScale(2, RoundingMode.HALF_UP), sameCurrency));
		tplData.put("invoicebalanceinc", formatCurrencyNum(currency, blance.multiply(taxRate).setScale(2, RoundingMode.HALF_UP), sameCurrency));
		return tplData;
	}

	private String formatCurrencyNum(Currency currency, Number number, boolean sameCurrency) {
		String formated = currency.getSymbol() + CurrencyFormater.format(number);
		if (!sameCurrency) {
			formated = formated + " " + currency.getCode();
		}
		return formated;
	}

	private BigDecimal getInvoicePaid(Integer invoiceNumber) {
		BigDecimal r = new BigDecimal("0");
		List<InvoiceSchedule> schedules = getInvoiceSchedules(invoiceNumber);
		if (schedules == null)
			return r;
		for (InvoiceSchedule schedule : schedules) {
			if (schedule.getPaidDatetime() != null) {
				r.add(schedule.getScheduledAmount());
			}
		}
		return r;
	}

	@Autowired
	private InvoiceReportService invoiceReportService;
	
	@Override
	public void sendInvoiceStatisticMail(ReportType type, MailPreview mailPreview) {
		MailContent mailContent = new MailContent(mailPreview.getSubject(), mailPreview.getBody(), false);
		mailContent.withTo(splitAddress(mailPreview.getTo()));

		File reportFile1 = null;
		File reportFile2 = null;
		try {
			UUID agentId = ((AgentUser) GlobalAttrManager.getCurrentUser()).getAgentId();
			Assert.notNull(agentId);

			reportFile1 = cacheRenderedStatisticsReportFileLocal(agentId, type, ReportFormat.PDF);
			
			if (type == ReportType.InvoiceAgentInvoiceSummary) {
				reportFile2 = invoiceReportService.generateCsvReport4AgentInvoiceSummary();
			} else if (type == ReportType.InvoiceForcast12Month) {
				reportFile2 = invoiceReportService.generateCSVReportInvoiceForcast12Month();
			} else if (type == ReportType.InvoicePaymentsDueSixWeeks) {
				reportFile2 = invoiceReportService.generateCSVReportInvoicePaymentsDueSixWeeks();
			}
			mailContent.withAttachment(new Pair<String, File>(new ReportPath(type, ReportFormat.PDF).getOutputName(), reportFile1),
					new Pair<String, File>(new ReportPath(type, ReportFormat.CSV).getOutputName(), reportFile2));
			mailService.send(mailContent);
		} finally {
			if (reportFile1 != null)
				reportFile1.delete();
			if (reportFile2 != null)
				reportFile2.delete();
		}

	}


	private File cacheRenderedStatisticsReportFileLocal(UUID agentId, ReportType reportPathType,ReportFormat format) {
		try {
			File tempPdf = File.createTempFile(new ReportPath(reportPathType,format).getOutputName(), ".pdf");
			NetworkUtils.downloadFile(getRenderedStatisticsReportFilePath(agentId, reportPathType,format), tempPdf.getAbsolutePath(), null);
			return tempPdf;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private String getRenderedStatisticsReportFilePath(UUID agentId, ReportType type,ReportFormat format) {
		ReportPath reportPath = new ReportPath(type,format);
		reportPath.addParam("agentId", agentId.toString());

		String rendererFileCloudKey = reportPath.getRenderFileCloudKey();
		String invoiceStatisticRenderData = "";
		if(type==ReportType.InvoiceAgentInvoiceSummary){
			invoiceStatisticRenderData = invoiceReportService.generatePdfReportData4AgentInvoiceSummary();
		}else if(type==ReportType.InvoiceForcast12Month){
			invoiceStatisticRenderData = invoiceReportService.generatePDFReportInvoiceForcast12Month();
		}else if(type==ReportType.InvoicePaymentsDueSixWeeks){
			invoiceStatisticRenderData = invoiceReportService.generatePDFReportInvoicePaymentsDueSixWeeks();
		}
		
		String pdfUrl = reportService.getRendererFileCloudUrl(reportPath.getTplName(), reportPath.getOutputName(), invoiceStatisticRenderData,
				rendererFileCloudKey, rendererFileCloudKey);
		return pdfUrl;
	}

	@Override
	public void sendInvoiceMail(Integer invoiceNumber, String emailTplType, MailPreview mailPreview) {
		Assert.notNull(invoiceNumber);
		Assert.notNull(emailTplType);

		Type tplType = (Type) BeanUtils.getEnum(AgentEmailTemplate.Type.class, emailTplType);
		Invoice invoice = getInvoice(invoiceNumber);
		AgentEmailTemplate emailTemplate = agentService.getAgentEmailTemplate(invoice.getAgentId(), tplType);

		MailContent mailContent = new MailContent(mailPreview.getSubject(), mailPreview.getBody(), false).withFrom(new MailAddress(emailTemplate
				.getFromEmail(), emailTemplate.getFromName()));
		mailContent.withTo(splitAddress(mailPreview.getTo())).withCc(splitAddress(mailPreview.getCc())).withBcc(splitAddress(mailPreview.getBcc()));

		File tempPdf = null;
		try {
			tempPdf = cacheRenderedFileLocal(invoiceNumber, invoice,tplType.getReportPathType());
			mailContent.withAttachment(new Pair<String, File>(new ReportPath(au.com.isell.rlm.module.report.constant.ReportType.Invoice,ReportFormat.PDF)
					.getOutputName(), tempPdf));
			mailService.send(mailContent);

			InvoiceFollowups followups = createFollowup4ReportMail(invoiceNumber, mailContent);
			invoiceDao.saveInvoiceFollowup(followups);
		} finally {
			if (tempPdf != null)
				tempPdf.delete();
		}
	}

	private InvoiceFollowups createFollowup4ReportMail(Integer invoiceNumber, MailContent mailContent) {
		InvoiceFollowups followups = new InvoiceFollowups();
		followups.setEmailBody(mailContent.getBody());
		followups.setEmailSubject(mailContent.getSubject());
		followups.setFollowupDate(new Date());
		followups.setInvoiceNumber(invoiceNumber);
		followups.setResults(FollowupResult.Emailed);
		followups.setUserId(GlobalAttrManager.getCurrentUser().getUserId());
		return followups;
	}

	@Override
	public String previewReport(Integer invoiceNumber, ReportType type) {
		return getRenderedFile(getInvoice(invoiceNumber),type);
	}

	private File cacheRenderedFileLocal(Integer invoiceNumber, Invoice invoice,ReportType type) {
		try {
			File tempPdf = File.createTempFile(new ReportPath(au.com.isell.rlm.module.report.constant.ReportType.Invoice,ReportFormat.PDF).getOutputName(),
					".pdf");
			NetworkUtils.downloadFile(getRenderedFile(invoice,type), tempPdf.getAbsolutePath(), null);
			return tempPdf;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private String getRenderedFile(Invoice invoice,ReportType type) {
//		List<InvoiceSchedule> schedules = getInvoiceSchedules(invoice.getInvoiceNumber());
		ReportPath reportPath = new ReportPath(type,ReportFormat.PDF);
		reportPath.addParam("invoiceNumber", invoice.getInvoiceNumber().toString()).addParam("agentId", invoice.getAgentId().toString());

		String rendererFileCloudKey = reportPath.getRenderFileCloudKey();
		String pdfUrl = reportService.getRendererFileCloudUrl(reportPath.getTplName(), reportPath.getOutputName(),
				getInoviceReportData(invoice.getInvoiceNumber()), rendererFileCloudKey, rendererFileCloudKey);
		return pdfUrl;
	}

	private MailAddress[] splitAddress(String rawMailAddrs) {
		if (rawMailAddrs == null)
			return null;
		List<MailAddress> addresses = new ArrayList<MailAddress>();
		for (String addr : rawMailAddrs.split(MailPreview.mailSeperator)) {
			if(addr==null) continue;
			addresses.add(new MailAddress(addr, null));
		}
		return addresses.toArray(new MailAddress[addresses.size()]);
	}

	private static final SimpleDateFormat SDF = new SimpleDateFormat("d MMM yyyy");
	private static final DecimalFormat DF = new DecimalFormat("###,###,##0.00");

	@Override
	public String getInoviceReportData(int invoiceNumber) {
		Map<String, Object> data = new HashMap<String, Object>();
		Invoice inv = getInvoice(invoiceNumber);
		data.put("invoice", inv.getInvoiceNumber());
		data.put("invdate", SDF.format(inv.getInvoiceDate()));
		AgentUser user = userService.getUser(AgentUser.class, inv.getSalesRepId());
		data.put("salesrep", user.getFirstName() + " " + user.getLastName());
		data.put("serialno", inv.getResellerSerialNo());
		Currency cur = sysService.getCurrency(inv.getCurrency());
		data.put("cur", cur.getCode());
		data.put("details", inv.getDetails());
		data.put("totalex", cur.getSymbol() + DF.format(inv.getTotalAmount()));
		BigDecimal totalTax = inv.getTotalAmountInc().subtract(inv.getTotalAmount());
		List<InvoiceSchedule> schedules = getInvoiceSchedules(invoiceNumber);
		List<Map<String, Object>> schedDatas = new ArrayList<Map<String, Object>>();
		BigDecimal totalPaidInc = new BigDecimal(0);
		for (InvoiceSchedule schedule : schedules) {
			Map<String, Object> schData = new HashMap<String, Object>();
			schData.put("scheddate", SDF.format(schedule.getDueDate()));
			String payment = null;
			if (schedule.getPaymentType() != null) {
				PaymentMethod method = schedule.getPaymentType();
				payment = EnumHelper.getDisplayCodeOrMsg(PaymentMethod.class, method);
			}
			if (schedule.isPaid()) {
				schData.put("scheddetails", "Paid by " + I18NUtils.getMsg(payment));
				schData.put("schedamount", "(paid) " + cur.getSymbol() + DF.format(schedule.getScheduledAmount()));
				totalPaidInc = totalPaidInc.add(schedule.getScheduledAmount());
			} else {
				schData.put("scheddetails", "Scheduled " + (payment != null ? "for " + I18NUtils.getMsg(payment) : ""));
				schData.put("schedamount", cur.getSymbol() + DF.format(schedule.getScheduledAmount()));
			}
			schedDatas.add(schData);
		}
		data.put("schedules", schedDatas);
		data.put("payCount", schedDatas.size());
		BigDecimal totalAmountInc = inv.getTotalAmountInc();
		data.put("totalinc", cur.getSymbol() + DF.format(totalAmountInc));
		data.put("totaltax", cur.getSymbol() + DF.format(totalTax));
		data.put("totalpaid", cur.getSymbol() + DF.format(totalPaidInc));
		data.put("totalowning", cur.getSymbol() + DF.format(totalAmountInc.subtract(totalPaidInc)));
		AgentRegion region = agentService.getAgentRegion(inv.getAgentId(), inv.getRegionCode());
		data.put("gstname", region.getTaxName());
		data.put("terms", inv.getTerms());
		data.put("tofullname", inv.getToFirstName() + " " + inv.getToLastName());
		data.put("custcompany", inv.getCompanyName());
		Address addr = inv.getCompanyAddress();
		if (addr != null) {
			data.put("custaddress", addr.toReportAddress(addrService));
		}
		return JsonUtils.encode(data);
	}

	@Override
	public boolean hasInvoice(int serialNo) {
		FilterMaker maker = invoiceDao.getFilterMaker();
		FilterItem filterItem = maker.makeNameFilter("resellerSerialNo", TextMatchOption.Is, String.valueOf(serialNo));
		Pair<Long, List<InvoiceSearchBean>> r = invoiceDao.searchInvoice(filterItem, null, null, null);
		return r.getKey() > 0l;
	}

	@Override
	public Integer quickCreateInvoice(Map nextRenewal, Map lics, Integer serialNo, String businessType) {
		Reseller reseller = resellerService.getReseller(serialNo);
		Invoice invoice = new Invoice();
		invoice.setResellerSerialNo(serialNo);
		invoice = initInvoice4Create(invoice,false);
		BusinessType busType = (BusinessType) BeanUtils.getEnum(BusinessType.class, businessType);
		invoice.setBusinessType(busType);
		boolean updateRenewal = nextRenewal.size() > 0;
		Date rDate = updateRenewal ? (Date) ConvertUtils.convert(nextRenewal.get("val"), Date.class) : null;
		Boolean rChecked = true;//updateRenewal ? (Boolean) ConvertUtils.convert(nextRenewal.get("checked"), Boolean.class) : null;
		Map<String, Map<String, Object>> licMap = lics;
		setRenewalDateAndAmmount(reseller, invoice, updateRenewal, rDate, rChecked, licMap);
		if (updateRenewal)
			resellerDao.save(true, reseller);

		int invoiceNumber = this.saveInvoice(invoice);
		// payment schedule
		InvoiceAutoSchedulesDesc autoScheduleDesc = createBlankAutoScheduleDesc(invoiceNumber);
		autoScheduleDesc.setScheduleds(1);
		autoScheduleDesc.setFirstScheduledAmount(invoice.getTotalAmountInc());
		createInvoiceScheduleByAutoDesc(invoiceNumber, autoScheduleDesc);
		return invoiceNumber;
	}

	private void setRenewalDateAndAmmount(Reseller reseller, Invoice invoice, boolean updateRenewal, Date rDate, Boolean rChecked,
			Map<String, Map<String, Object>> licMap) {
		for (LicenseType licenseType : LicenseType.values()) {
			Map<LicenseType, LicModule> licenses = reseller.getLicenses();
			LicModule licModule = licenses.get(licenseType);
			if (licModule == null)
				continue;
			Map<String, Object> licSubMap = licMap.get(licenseType.name());
			if(licSubMap==null) continue;

			Boolean checked = (Boolean) ConvertUtils.convert(licSubMap.get("checked"), Boolean.class);
			if(!checked) continue;
			// update renewal date
			if (rDate!=null) {
				licModule.setRenewalDate(rDate);
			}
			// set invoice ammount
			String valStr = (String) licSubMap.get("val");
			if (StringUtils.isEmpty(valStr))
				continue;
			AmountType amountType = AmountType.getAmmoutTypeFromLicenseType(licenseType);
			invoice.getAmounts().put(amountType, new BigDecimal(valStr));
		}
	}
}
