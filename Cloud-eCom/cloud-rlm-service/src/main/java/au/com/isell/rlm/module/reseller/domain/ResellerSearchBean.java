package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.Assert;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;

@ISellIndex(name = "resellers", type = "basic")
public class ResellerSearchBean {
	private Integer serialNo;
	private String company;
	private String city;
	private String region;
	private Date createdDatetime;
	private Date lastSyncDate;
	private ResellerType type;
	private ResellerStatus status;
	private String version;
	private String country;
	private Date syncDate;
	private Date renewDate;
	private int supplierApprovalRemain;
	private UUID agencyId;
	private String phone;
	private String email;
	private UUID salesRepID;
	private String contact;
	private String email_cc;
	private Date nextReviewDate;
	private String regKey;
	private String hardwareInfo;

	public ResellerSearchBean() {
	}

	public ResellerSearchBean(Reseller reseller, List<ResellerSupplierMap> maps,String phoneAreaCodeBind) {
		Assert.notNull(reseller);

		serialNo = reseller.getSerialNo();
		company = reseller.getCompany();
		city = reseller.getAddress().getCity();
		region = reseller.getAddress().getRegion();
		createdDatetime = reseller.getCreatedDatetime();
		type = reseller.getType();
		status = reseller.getStatus();
		version = reseller.getVersion();
		country = reseller.getCountry();
		lastSyncDate = reseller.getLastSyncDate();
		renewDate = calRenewDate(reseller.getLicenses());
		supplierApprovalRemain = calSupplierApprovalRemain(maps);
		joinPhone(reseller, phoneAreaCodeBind);
		setAgencyId(reseller.getAgencyId());
		email=reseller.getBillingInfo().getEmail();
		contact = (reseller.getBillingInfo().getFirstName()==null?"":reseller.getBillingInfo().getFirstName())
				+" "+(reseller.getBillingInfo().getLastName()==null?"":reseller.getBillingInfo().getLastName());
		salesRepID=reseller.getSalesRepID();
		email_cc = reseller.getEmail_cc();
		nextReviewDate = reseller.getNextReviewDate();
		setRegKey(reseller.getRegKey());
		setHardwareInfo(reseller.getHardwareInfo());
	}

	private void joinPhone(Reseller reseller, String phoneAreaCodeBind) {
		phone =  reseller.getBillingInfo().getPhone();
		phone= phone != null ? phone.replaceAll("[^0-9+]", "").replaceFirst("^0+", "") : "";
		
		String phoneArea="";
		if(phoneAreaCodeBind!=null) phoneArea="+"+phoneAreaCodeBind;
		phone= phoneArea+ phone;
	}
	

	public Date calRenewDate(Map<LicenseType, LicModule> licenses) {
		Date date = null;
		if (licenses == null)
			return null;
		for (LicModule lic : licenses.values()) {
			if (lic == null)
				continue;
			if (date == null)
				date = lic.getRenewalDate();
			else if (lic.getRenewalDate() != null && date.after(lic.getRenewalDate()))
				date = lic.getRenewalDate();
		}
		return date;
	}

	public int calSupplierApprovalRemain(List<ResellerSupplierMap> maps) {
		if (maps == null)
			return 0;
		int count = 0;
		for (ResellerSupplierMap map : maps) {
			if (map.getStatus() != ResellerSupplierMap.ApprovalStatus.Approved)
				count++;
		}
		return count;
	}

	@ISellIndexKey(wildcard = true)
	public Integer getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}

	@ISellIndexValue(wildcard = true)
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	@ISellIndexValue(wildcard = true)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@ISellIndexValue
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	@ISellIndexValue
	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	@ISellIndexValue
	public Date getLastSyncDate() {
		return lastSyncDate;
	}

	public void setLastSyncDate(Date lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}

	@ISellIndexValue
	public ResellerType getType() {
		return type;
	}

	public void setType(ResellerType type) {
		this.type = type;
	}

	@ISellIndexValue
	public ResellerStatus getStatus() {
		return status;
	}

	public void setStatus(ResellerStatus status) {
		this.status = status;
	}

	@ISellIndexValue(wildcard=true)
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@ISellIndexValue
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@ISellIndexValue
	public Date getSyncDate() {
		return syncDate;
	}

	public void setSyncDate(Date syncDate) {
		this.syncDate = syncDate;
	}

	@ISellIndexValue
	public Date getRenewDate() {
		return renewDate;
	}

	public void setRenewDate(Date renewDate) {
		this.renewDate = renewDate;
	}

	@ISellIndexValue
	public int getSupplierApprovalRemain() {
		return supplierApprovalRemain;
	}

	public void setSupplierApprovalRemain(int supplierApprovalRemain) {
		this.supplierApprovalRemain = supplierApprovalRemain;
	}

	public void setAgencyId(UUID agencyId) {
		this.agencyId = agencyId;
	}

	@ISellIndexValue
	public UUID getAgencyId() {
		return agencyId;
	}

	@ISellIndexValue(wildcard=true)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@ISellIndexValue(wildcard=true)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@ISellIndexValue
	public UUID getSalesRepID() {
		return salesRepID;
	}

	public void setSalesRepID(UUID salesRepID) {
		this.salesRepID = salesRepID;
	}

	@ISellIndexValue(wildcard = true)
	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}
	@ISellIndexValue(index="no")
	public String getEmail_cc() {
		return email_cc;
	}

	public void setEmail_cc(String email_cc) {
		this.email_cc = email_cc;
	}

	public void setNextReviewDate(Date nextReviewDate) {
		this.nextReviewDate = nextReviewDate;
	}
	@ISellIndexValue
	public Date getNextReviewDate() {
		return nextReviewDate;
	}

	public void setRegKey(String regKey) {
		this.regKey = regKey;
	}

	public String getRegKey() {
		return regKey;
	}

	public void setHardwareInfo(String hardwareInfo) {
		this.hardwareInfo = hardwareInfo;
	}

	public String getHardwareInfo() {
		return hardwareInfo;
	}
	
}
