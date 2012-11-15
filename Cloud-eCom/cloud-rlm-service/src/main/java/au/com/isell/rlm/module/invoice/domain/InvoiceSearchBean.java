package au.com.isell.rlm.module.invoice.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.invoice.domain.Invoice.AmountType;
import au.com.isell.rlm.module.invoice.domain.Invoice.BusinessType;
import au.com.isell.rlm.module.invoice.domain.Invoice.InvoiceStatus;
import au.com.isell.rlm.module.invoice.domain.Invoice.PaymentProbaility;

@ISellIndex(name = "invoices", type = "main")
public class InvoiceSearchBean {
	private UUID agentId;
	private String agentName;
	private BigDecimal balance;
	private String company;

	private String contact;
	private Date createdDate;
	private Date followupDate;
	private Date fullPaidDate;
	private Date invoiceDate;
	private Date promisedPaymentDate;
	private PaymentProbaility probability;
	private int invoiceNumber;
	private Integer resellerSerialNo;
	private InvoiceStatus status;
	private String toEmail;
	private String toFirstName;
	private String toLastName;
	private String toMobile;
	private String toPhone;
	private BigDecimal totalAmount;
	private BigDecimal totalAmountInc;
	private String currency;
	private boolean serviceOnly;
	private BigDecimal taxRate;
	private UUID salesRepId;
	private BusinessType businessType;
	private Map<String, BigDecimal> amounts = new HashMap<String, BigDecimal>();

	public InvoiceSearchBean() {
		super();

	}

	public InvoiceSearchBean(Invoice invoice, Agent agent, BigDecimal totalPaid,String phoneCountryCode,Date fullPaidDate) {
		this.balance = invoice.getTotalAmountInc().subtract(totalPaid);
		this.agentName = agent.getName();
		this.fullPaidDate = fullPaidDate;
		getInvoiceVals(invoice,phoneCountryCode);
	}

	public InvoiceSearchBean(Invoice invoice, Agent agent, List<InvoiceSchedule> schedules, List<InvoiceFollowups> followups,String phoneCountryCode) {
		balance = new BigDecimal(0);
		if (schedules != null) {
			Date fullPaidDate = null;
			boolean fullPaid = true;
			for (InvoiceSchedule schedule : schedules) {
				if (schedule.isPaid()) {
					balance = balance.add(schedule.getScheduledAmount());
					if (fullPaidDate == null) fullPaidDate = schedule.getPaidDatetime();
					else if (fullPaidDate.before(schedule.getPaidDatetime())) fullPaidDate = schedule.getPaidDatetime();
				}
				fullPaid = fullPaid && schedule.isPaid();
			}
			balance = invoice.getTotalAmountInc().subtract(balance);
			if (fullPaid) {
				this.fullPaidDate = fullPaidDate;
			}
		}
		this.agentName = agent.getName();
		getInvoiceVals(invoice,phoneCountryCode);

	}

	public Map<String, BigDecimal> getAmounts() {
		return amounts;
	}

	public void setAmounts(Map<AmountType, BigDecimal> amounts) {
		for (Entry<AmountType, BigDecimal> entry : amounts.entrySet()) {
			this.amounts.put(entry.getKey().toString(), entry.getValue());
		}
	}

	@ISellIndexValue
	public UUID getAgentId() {
		return agentId;
	}

	@ISellIndexValue
	public String getAgentName() {
		return agentName;
	}

	@ISellIndexValue
	public BigDecimal getBalance() {
		return balance;
	}

	@ISellIndexValue(wildcard = true)
	public String getCompany() {
		return company;
	}

	@ISellIndexValue(index = "analyzed")
	public String getContact() {
		return contact;
	}

	@ISellIndexValue
	public Date getCreatedDate() {
		return createdDate;
	}

	@ISellIndexValue
	public Date getFollowupDate() {
		return followupDate;
	}

	@ISellIndexValue
	public Date getFullPaidDate() {
		return fullPaidDate;
	}

	@ISellIndexValue
	public Date getInvoiceDate() {
		return invoiceDate;
	}

	@ISellIndexKey(wildcard = true)
	public int getInvoiceNumber() {
		return this.invoiceNumber;
	}

	private void getInvoiceVals(Invoice invoice,String phoneCountryCode) {
		this.agentId = invoice.getAgentId();
		this.company = invoice.getCompanyName();
		this.totalAmount = invoice.getTotalAmount();
		this.totalAmountInc = invoice.getTotalAmountInc();
		this.invoiceNumber = invoice.getInvoiceNumber();
		this.resellerSerialNo = invoice.getResellerSerialNo();
		this.contact = (invoice.getToFirstName() == null ? "" : invoice.getToFirstName()) + " "
				+ (invoice.getToLastName() == null ? "" : invoice.getToLastName());
		this.createdDate = invoice.getCreatedDate();
		this.followupDate = invoice.getFollowupDate();
		this.invoiceDate = invoice.getInvoiceDate();
		this.resellerSerialNo = invoice.getResellerSerialNo();
		this.status = invoice.getStatus();
		this.toEmail = invoice.getToEmail();
		this.toFirstName = invoice.getToFirstName();
		this.toLastName = invoice.getToLastName();
		this.toMobile = invoice.getToMobile();
		this.toPhone = joinPhone(invoice.getToPhone(),phoneCountryCode);
		this.currency=invoice.getCurrency();
		this.taxRate = invoice.getTaxRate();
		this.probability = invoice.getProbability();
		this.promisedPaymentDate = invoice.getPromisedPaymentDate();
		Map<AmountType, BigDecimal> amounts=invoice.getAmounts();
		if (amounts.containsKey(AmountType.ECom) 
				|| amounts.containsKey(AmountType.EPD)
				|| amounts.containsKey(AmountType.ITQuoter)) {
			serviceOnly = false;
		} else {
			serviceOnly = true;
		}
		this.salesRepId=invoice.getSalesRepId();
		this.businessType=invoice.getBusinessType();
	}
	
	private String joinPhone(String phone,String phoneAreaCodeBind) {
		phone= phone != null ? phone.replaceAll("[^0-9+]", "").replaceFirst("^0+", "") : "";
		phone= "+"+(phoneAreaCodeBind==null?"":phoneAreaCodeBind)+ phone;
		return phone;
	}
	
	@ISellIndexValue
	public int getResellerSerialNo() {
		return resellerSerialNo;
	}

	@ISellIndexValue
	public InvoiceStatus getStatus() {
		return status;
	}

	@ISellIndexValue
	public String getToEmail() {
		return toEmail;
	}

	@ISellIndexValue
	public String getToFirstName() {
		return toFirstName;
	}

	@ISellIndexValue
	public String getToLastName() {
		return toLastName;
	}

	@ISellIndexValue
	public String getToMobile() {
		return toMobile;
	}

	@ISellIndexValue(wildcard = true)
	public String getToPhone() {
		return toPhone;
	}

	@ISellIndexValue
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	
	@ISellIndexValue
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setAgentId(UUID agentId) {
		this.agentId = agentId;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public void setFollowupDate(Date followupDate) {
		this.followupDate = followupDate;
	}

	public void setFullPaidDate(Date fullPaidDate) {
		this.fullPaidDate = fullPaidDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public void setInvoiceNumber(int invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public void setResellerSerialNo(Integer resellerSerialNo) {
		this.resellerSerialNo = resellerSerialNo;
	}

	public void setStatus(InvoiceStatus status) {
		this.status = status;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public void setToFirstName(String toFirstName) {
		this.toFirstName = toFirstName;
	}

	public void setToLastName(String toLastName) {
		this.toLastName = toLastName;
	}

	public void setToMobile(String toMobile) {
		this.toMobile = toMobile;
	}

	public void setToPhone(String toPhone) {
		this.toPhone = toPhone;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public void setTotalAmountInc(BigDecimal totalAmountInc) {
		this.totalAmountInc = totalAmountInc;
	}
	@ISellIndexValue
	public BigDecimal getTotalAmountInc() {
		return totalAmountInc;
	}

	public void setPromisedPaymentDate(Date promisedPaymentDate) {
		this.promisedPaymentDate = promisedPaymentDate;
	}
	@ISellIndexValue
	public Date getPromisedPaymentDate() {
		return promisedPaymentDate;
	}

	public void setProbability(PaymentProbaility probability) {
		this.probability = probability;
	}
	@ISellIndexValue
	public PaymentProbaility getProbability() {
		return probability;
	}

	public void setServiceOnly(boolean serviceOnly) {
		this.serviceOnly = serviceOnly;
	}
	@ISellIndexValue
	public boolean isServiceOnly() {
		return serviceOnly;
	}
	@ISellIndexValue(index="no")
	public BigDecimal getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}
	@ISellIndexValue
	public UUID getSalesRepId() {
		return salesRepId;
	}
	@ISellIndexValue
	public BusinessType getBusinessType() {
		return businessType;
	}

	public void setSalesRepId(UUID salesRepId) {
		this.salesRepId = salesRepId;
	}

	public void setBusinessType(BusinessType businessType) {
		this.businessType = businessType;
	}

	@Override
	public String toString() {
		return "InvoiceSearchBean [agentId=" + agentId + ", agentName=" + agentName + ", balance=" + balance + ", company=" + company + ", contact="
				+ contact + ", createdDate=" + createdDate + ", followupDate=" + followupDate + ", fullPaidDate=" + fullPaidDate + ", invoiceDate="
				+ invoiceDate + ", promisedPaymentDate=" + promisedPaymentDate + ", probability=" + probability + ", invoiceNumber=" + invoiceNumber
				+ ", resellerSerialNo=" + resellerSerialNo + ", status=" + status + ", toEmail=" + toEmail + ", toFirstName=" + toFirstName
				+ ", toLastName=" + toLastName + ", toMobile=" + toMobile + ", toPhone=" + toPhone + ", totalAmount=" + totalAmount
				+ ", totalAmountInc=" + totalAmountInc + ", currency=" + currency + ", serviceOnly=" + serviceOnly + ", taxRate=" + taxRate
				+ ", salesRepId=" + salesRepId + ", businessType=" + businessType + ", amounts=" + amounts + "]";
	}
	
}
