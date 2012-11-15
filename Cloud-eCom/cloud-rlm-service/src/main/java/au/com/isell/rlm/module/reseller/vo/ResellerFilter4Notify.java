package au.com.isell.rlm.module.reseller.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;

public class ResellerFilter4Notify {
	private String name;
	private ResellerStatus status;
	private String country;
	private String region;
	private String city;
	private String phone;
	private String email;
	private String version;
	private ResellerType type;
	private UUID agencyId;
	private UUID salesRepID;

	
	private String supplierId;
	private String supplierBreakId;
	private ApprovalStatus rsmStatus=ApprovalStatus.Approved;

	private String recoveryMode;//checkbox
	
	private UUID emailType;
	private String subject;
	private String senderEmail;
	
	private String mailTo;
	

	public FilterItem getFilter4ResellerSearch(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addWildcardQueryItem(filterMaker, "company", name, makers);
		if (status != null)
			addISQueryItem(filterMaker, "status", String.valueOf(status.ordinal()), makers);
		addISQueryItem(filterMaker, "country", country, makers);
		addISQueryItem(filterMaker, "region", region, makers);
		addWildcardQueryItem(filterMaker, "city", city, makers);
		addWildcardQueryItem(filterMaker, "email", email, makers);
		addWildcardQueryItem(filterMaker, "phone", phone, makers);
		addWildcardQueryItem(filterMaker, "version", version, makers);
		if (type != null)
			addISQueryItem(filterMaker, "type", String.valueOf(type.ordinal()), makers);
		if (agencyId != null)
			addISQueryItem(filterMaker, "agencyId", agencyId.toString(), makers);
		if (salesRepID != null)
			addISQueryItem(filterMaker, "salesRepID", salesRepID.toString(), makers);
		if (makers.size() > 0)
			return filterMaker.linkWithAnd(makers.toArray(new FilterItem[makers.size()]));
		
		
		
		return null;
	}

	public FilterItem getFilterItem4RSMapSearch(FilterMaker filterMaker) {
		List<FilterItem> makers = new ArrayList<FilterItem>();
		addISQueryItem(filterMaker, "supplierId", supplierId, makers);
		addISQueryItem(filterMaker, "priceBreakId", supplierBreakId, makers);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResellerStatus getStatus() {
		return status;
	}

	public void setStatus(ResellerStatus status) {
		this.status = status;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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

	public ResellerType getType() {
		return type;
	}

	public void setType(ResellerType type) {
		this.type = type;
	}

	public UUID getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(UUID agencyId) {
		this.agencyId = agencyId;
	}

	public UUID getSalesRepID() {
		return salesRepID;
	}

	public void setSalesRepID(UUID salesRepID) {
		this.salesRepID = salesRepID;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSupplierBreakId() {
		return supplierBreakId;
	}

	public void setSupplierBreakId(String supplierBreakId) {
		this.supplierBreakId = supplierBreakId;
	}

}
