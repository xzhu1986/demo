package au.com.isell.rlm.module.invoice.domain;

import java.math.BigDecimal;
import java.util.Date;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.constant.PaymentMethod;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("invoice-schedule")
@IsellPath(value="data/invoices/${invoiceNumber}/schedules/${dueDate}.info.json")
@ISellIndex(name = "invoices", type = "schedule")
public class InvoiceSchedule extends AbstractModel {

	private int invoiceNumber;
	private BigDecimal scheduledAmount;
	private Date dueDate;
	private Date paidDatetime;
	private PaymentMethod paymentType;
	private String paymentReference;
	
	@ISellIndexKey
	public String getId() {
		return invoiceNumber + "_" + (dueDate.getTime()/1000);
	}
	
	@IsellPathField
	@ISellIndexValue
	public int getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(int invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	@ISellIndexValue
	public BigDecimal getScheduledAmount() {
		return scheduledAmount;
	}
	public void setScheduledAmount(BigDecimal scheduledAmount) {
		this.scheduledAmount = scheduledAmount;
	}
	@IsellPathField
	@ISellIndexValue
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public boolean isPaid() {
		return paidDatetime!=null;
	}
	@ISellIndexValue
	public Date getPaidDatetime() {
		return paidDatetime;
	}
	public void setPaidDatetime(Date paidDatetime) {
		this.paidDatetime = paidDatetime;
	}
	public PaymentMethod getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(PaymentMethod paymentType) {
		this.paymentType = paymentType;
	}
	public String getPaymentReference() {
		return paymentReference;
	}
	public void setPaymentReference(String paymentReference) {
		this.paymentReference = paymentReference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result + invoiceNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InvoiceSchedule other = (InvoiceSchedule) obj;
		if (dueDate == null) {
			if (other.dueDate != null)
				return false;
		} else if (!dueDate.equals(other.dueDate))
			return false;
		if (invoiceNumber != other.invoiceNumber)
			return false;
		return true;
	}
}
