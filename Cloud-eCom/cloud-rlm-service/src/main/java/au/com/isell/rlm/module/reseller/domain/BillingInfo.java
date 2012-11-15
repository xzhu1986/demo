package au.com.isell.rlm.module.reseller.domain;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;


public class BillingInfo {

	private String firstName;
	private String lastName;
	private String phone;
	private String jobPosition;
	private String email;
	private String additionalEmail;
	private String currency;
	private BigDecimal gst;
	private String comments;

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

	public BigDecimal getGst() {
		return gst;
	}

	public void setGst(BigDecimal gst) {
		this.gst = gst;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "BillingInfo [firstName=" + firstName + ", lastName=" + lastName
				+ ", phone=" + phone + ", jobPosition=" + jobPosition
				+ ", email=" + email + ", additionalEmail=" + additionalEmail
				+ ", currency=" + currency + ", gst=" + gst + ", comments="
				+ comments + "]";
	}

}
