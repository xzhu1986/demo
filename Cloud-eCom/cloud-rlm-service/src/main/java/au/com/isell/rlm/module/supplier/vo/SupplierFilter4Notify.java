package au.com.isell.rlm.module.supplier.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.supplier.domain.Supplier.Status;

public class SupplierFilter4Notify {
	private String shortName;
	private Status status;
	private String country;
	private String phone;
	private String email;
	private String region;
	private String city;
	private String serialNo;
	private ApprovalStatus rsmStatus;

	private String recoveryMode;//checkbox
	
	private UUID emailType;
	private String subject;
	private String senderEmail;
	
	private String mailTo;

	public FilterItem getFilter4SupplierSearch(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker, "shortName", shortName, makers);
		if (status != null)
			addISQueryItem(filterMaker, "status", String.valueOf(status.ordinal()), makers);
		addISQueryItem(filterMaker, "country", country, makers);
		addISQueryItem(filterMaker, "region", region, makers);
		addWildcardQueryItem(filterMaker, "city", city, makers);
		addWildcardQueryItem(filterMaker, "email", email, makers);
		addWildcardQueryItem(filterMaker, "phone", phone, makers);
		if (makers.size() > 0)
			return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		return null;
	}

	public FilterItem getFilterItem4RSMapSearch(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker, "serialNo", serialNo, makers);
		if (rsmStatus != null)
			addISQueryItem(filterMaker, "status", String.valueOf(rsmStatus.ordinal()), makers);
		if (makers.size() > 0)
			return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		return null;
	}

	private void addISQueryItem(FilterMaker builder, String name, String value, List<FilterItem> items) {
		if (StringUtils.isNotBlank(value)) {
			items.add(builder.makeNameFilter(name, TextMatchOption.Is, value.trim()));
		}
	}

	private void addQueryItem(FilterMaker builder, String name, String value, List<FilterItem> items) {
		if (StringUtils.isNotBlank(value)) {
			items.add(builder.makeNameFilter(name, TextMatchOption.StartWith, value.trim()));
		}
	}

	private void addWildcardQueryItem(FilterMaker builder, String name, String value, List<FilterItem> items) {
		if (StringUtils.isNotBlank(value)) {
			items.add(builder.makeNameFilter(name, TextMatchOption.Contains, value.trim()));
		}
	}


	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}


	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public ApprovalStatus getRsmStatus() {
		return rsmStatus;
	}

	public void setRsmStatus(ApprovalStatus rsmStatus) {
		this.rsmStatus = rsmStatus;
	}

	public UUID getEmailType() {
		return emailType;
	}

	public void setEmailType(UUID emailType) {
		this.emailType = emailType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public boolean checkRecoveryMode() {
		return recoveryMode!=null&& recoveryMode.equals("on");
	}

	public String getRecoveryMode() {
		return recoveryMode;
	}

	public void setRecoveryMode(String recoveryMode) {
		this.recoveryMode = recoveryMode;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}


}
