package au.com.isell.rlm.module.invoice.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.reseller.domain.LicenseType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("invoice")
@IsellPath(value="data/invoices/${invoiceNumber}/basic.info.json")
public class Invoice extends AbstractModel implements BeanInitable<Invoice>{
	
	public static enum InvoiceStatus {
		@EnumMsgCode("status.waiting_approval")
		WaitingApproval,
		@EnumMsgCode("status.onhold")
		OnHold, 
		@EnumMsgCode("status.waiting_payment")
		WaitingPayment, 
		@EnumMsgCode("status.payment_schedule")
		PaymentSchedule, 
		@EnumMsgCode("status.full_paid")
		FullPaid, 
		@EnumMsgCode("status.cancelled")
		Cancelled}

	public static enum AmountType {
		ITQuoter, ECom, EPD, Serivce, Other;
		public static AmountType getAmmoutTypeFromLicenseType(LicenseType licenseType) {
			switch (licenseType) {
			case ITQLicense:
				return ITQuoter;
			case EComLicense:
				return ECom;
			case EPDLicense:
				return EPD;
			default:
				return null;
			}
		}
	}
	public static enum PaymentTerm {
		@EnumMsgCode("invoice.payterm.duenow")
		DueNow, 
		@EnumMsgCode("invoice.payterm.day7")
		Day7, 
		@EnumMsgCode("invoice.payterm.day14")
		Day14, 
		@EnumMsgCode("invoice.payterm.day30")
		Day30, 
		@EnumMsgCode("invoice.payterm.monthday1")
		MonthDay1, 
		@EnumMsgCode("invoice.payterm.monthday23")
		MonthDay23, 
		@EnumMsgCode("invoice.payterm.monthend")
		MonthEnd
		}
	
	public static enum PaymentProbaility {
		@EnumMsgCode("invoice.probability.unlikely")
		Unlikely, 
		@EnumMsgCode("invoice.probability.unconfirmed")
		Unconfirmed, 
		@EnumMsgCode("invoice.probability.confirmed")
		Confirmed
		}
	
	public static enum BusinessType {
		@EnumMsgCode("invoice.biz_type.new_business")
		NewBusiness,
		@EnumMsgCode("invoice.biz_type.renewal")
		Renewal
		}

	private Integer invoiceNumber;
	private Integer resellerSerialNo;
	private String companyName;
	private Address companyAddress;
	private BusinessType businessType;
	private InvoiceStatus status;
	private PaymentTerm paymentTerm;
	private Date createdDate;
	private Date invoiceDate;
	private Date followupDate;
	private Date promisedPaymentDate;
	private PaymentProbaility probability;
	private String details;
	private String terms;
	private UUID reportId;
	private String notes;
	private UUID agentId; 
	private String agentName;
	private String regionCode;
	private UUID salesRepId;  // user with sales rep flag
	private String toFirstName;
	private String toLastName;
	private String toPhone;
	private String toMobile;
	private String toEmail;
	private String toEmailcc;
	private Map<AmountType, BigDecimal> amounts=new HashMap<Invoice.AmountType, BigDecimal>();
	private String currency;
	private BigDecimal taxRate;
	private String taxName;

	@IsellPathField
	public Integer getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(Integer invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public Integer getResellerSerialNo() {
		return resellerSerialNo;
	}
	public void setResellerSerialNo(Integer resellerSerialNo) {
		this.resellerSerialNo = resellerSerialNo;
	}
	public InvoiceStatus getStatus() {
		return status;
	}
	public void setStatus(InvoiceStatus status) {
		this.status = status;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public Date getFollowupDate() {
		return followupDate;
	}
	public void setFollowupDate(Date followupDate) {
		this.followupDate = followupDate;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getTerms() {
		return terms;
	}
	public void setTerms(String terms) {
		this.terms = terms;
	}
	public UUID getReportId() {
		return reportId;
	}
	public void setReportId(UUID reportId) {
		this.reportId = reportId;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public UUID getAgentId() {
		return agentId;
	}
	public void setAgentId(UUID agentId) {
		this.agentId = agentId;
	}
	public UUID getSalesRepId() {
		return salesRepId;
	}
	public void setSalesRepId(UUID salesRepId) {
		this.salesRepId = salesRepId;
	}
	
	public String getToFirstName() {
		return toFirstName;
	}
	public void setToFirstName(String toFirstName) {
		this.toFirstName = toFirstName;
	}
	public String getToLastName() {
		return toLastName;
	}
	public void setToLastName(String toLastName) {
		this.toLastName = toLastName;
	}
	public String getToPhone() {
		return toPhone;
	}
	public void setToPhone(String toPhone) {
		this.toPhone = toPhone;
	}
	public String getToMobile() {
		return toMobile;
	}
	public void setToMobile(String toMobile) {
		this.toMobile = toMobile;
	}
	public String getToEmail() {
		return toEmail;
	}
	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}
	public String getToEmailcc() {
		return toEmailcc;
	}
	public void setToEmailcc(String toEmailcc) {
		this.toEmailcc = toEmailcc;
	}
	public Map<AmountType, BigDecimal> getAmounts() {
		return amounts;
	}
	public void setAmounts(Map<AmountType, BigDecimal> amounts) {
		this.amounts = amounts;
	}
	public BigDecimal getTotalAmount() {
		BigDecimal r=new BigDecimal("0");
		if(amounts==null) return r;
		for(BigDecimal amount:amounts.values()){
			r = r.add(amount);
		}
		return r;
	}
	public BigDecimal getTotalAmountInc() {
		BigDecimal r=getTotalAmount();
		if(taxRate==null) return new BigDecimal("0");
		return r.multiply(taxRate.divide(new BigDecimal("100")).add(new BigDecimal("1"))).setScale(2, RoundingMode.HALF_UP);
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public BigDecimal getTaxRate() {
		return taxRate;
	}
	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}
	public String getTaxName() {
		return taxName;
	}
	public void setTaxName(String taxName) {
		this.taxName = taxName;
	}
	public String getRegionCode() {
		return regionCode;
	}
	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}
	public void setPaymentTerm(PaymentTerm paymentTerm) {
		this.paymentTerm = paymentTerm;
	}
	public PaymentTerm getPaymentTerm() {
		return paymentTerm;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getAgentName() {
		return agentName;
	}
	@Override
	public Invoice init() {
		setInvoiceDate(DatePicker.pickDay(new Date(), 0));
		setFollowupDate(DatePicker.pickDay(new Date(), 3));
		setPaymentTerm(PaymentTerm.Day7);
		setStatus(InvoiceStatus.WaitingApproval);
		return this;
	}
	public void setCompanyAddress(Address companyAddress) {
		this.companyAddress = companyAddress;
	}
	public Address getCompanyAddress() {
		return companyAddress;
	}
	public void setPromisedPaymentDate(Date promisedPaymentDate) {
		this.promisedPaymentDate = promisedPaymentDate;
	}
	public Date getPromisedPaymentDate() {
		return promisedPaymentDate;
	}
	public BusinessType getBusinessType() {
		return businessType;
	}
	public void setBusinessType(BusinessType businessType) {
		this.businessType = businessType;
	}
	public PaymentProbaility getProbability() {
		return probability;
	}
	public void setProbability(PaymentProbaility probaility) {
		this.probability = probaility;
	}
	
}
