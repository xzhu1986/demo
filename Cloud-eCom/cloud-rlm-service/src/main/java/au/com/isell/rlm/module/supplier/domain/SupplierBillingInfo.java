package au.com.isell.rlm.module.supplier.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import au.com.isell.common.bean.BeanInitable;

public class SupplierBillingInfo implements BeanInitable<SupplierBillingInfo>{
	private Date renewDate;
	private BigDecimal annualFee;
	private String firstName;
	private String lastName;
	private String phone;
	private String jobPosition;
	private String email;
	private String additionalEmail;
	private String currency;
	private BigDecimal tax;
	private String comments;
	private String email_cc;

	public Date getRenewDate() {
		return renewDate;
	}

	public void setRenewDate(Date renewDate) {
		this.renewDate = renewDate;
	}

	public BigDecimal getAnnualFee() {
		return annualFee;
	}

	public void setAnnualFee(BigDecimal annualFee) {
		this.annualFee = annualFee;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getJobPosition() {
		return jobPosition;
	}

	public void setJobPosition(String jobPosition) {
		this.jobPosition = jobPosition;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAdditionalEmail() {
		return additionalEmail;
	}

	public void setAdditionalEmail(String additionalEmail) {
		this.additionalEmail = additionalEmail;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getTax() {
		return tax;
	}

	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getEmail_cc() {
		return email_cc;
	}

	public void setEmail_cc(String email_cc) {
		this.email_cc = email_cc;
	}
	
	@Override
	public SupplierBillingInfo init() {
		renewDate=DateUtils.addYears(new Date(), 1);
		return this;
	}

	@Override
	public String toString() {
		return "SupplierBillingInfo [renewDate=" + renewDate + ", annualFee="
				+ annualFee + ", firstName=" + firstName + ", lastName="
				+ lastName + ", phone=" + phone + ", jobPosition="
				+ jobPosition + ", email=" + email + ", additionalEmail="
				+ additionalEmail + ", currency=" + currency + ", tax=" + tax+ ", email_cc=" + email_cc
				+ ", comments=" + comments + "]";
	}

	
}
