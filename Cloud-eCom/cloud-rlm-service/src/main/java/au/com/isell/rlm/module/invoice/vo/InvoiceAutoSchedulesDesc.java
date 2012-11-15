package au.com.isell.rlm.module.invoice.vo;

import java.math.BigDecimal;
import java.util.Date;


public class InvoiceAutoSchedulesDesc {
	private int scheduleds;
	private BigDecimal invoiceTotalAmount;
	private BigDecimal firstScheduledAmount;
	private BigDecimal eachSecheduleAmount;
	private Date firstScheduledDate;
	private int dayOfMonth;
	
	public int getScheduleds() {
		return scheduleds;
	}
	public void setScheduleds(int scheduleds) {
		this.scheduleds = scheduleds;
	}
	public BigDecimal getInvoiceTotalAmount() {
		return invoiceTotalAmount;
	}
	public void setInvoiceTotalAmount(BigDecimal invoiceTotalAmount) {
		this.invoiceTotalAmount = invoiceTotalAmount;
	}
	public BigDecimal getFirstScheduledAmount() {
		return firstScheduledAmount;
	}
	public void setFirstScheduledAmount(BigDecimal firstScheduledAmount) {
		this.firstScheduledAmount = firstScheduledAmount;
	}
	public BigDecimal getEachSecheduleAmount() {
		return eachSecheduleAmount;
	}
	public void setEachSecheduleAmount(BigDecimal eachSecheduleAmount) {
		this.eachSecheduleAmount = eachSecheduleAmount;
	}
	public int getDayOfMonth() {
		return dayOfMonth;
	}
	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
	public Date getFirstScheduledDate() {
		return firstScheduledDate;
	}
	public void setFirstScheduledDate(Date firstScheduledDate) {
		this.firstScheduledDate = firstScheduledDate;
	}
	
	
}
