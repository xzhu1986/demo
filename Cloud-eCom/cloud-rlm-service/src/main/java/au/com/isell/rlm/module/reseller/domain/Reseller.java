package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationContext;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.aws.S3Utils;
import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.AfterSaving;
import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.bean.DeepCopy;
import au.com.isell.common.message.MessageContainer;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/resellers/${serialNo}/basic.info.json")
@MessageDef(queue="apollo", type="reseller")
@XStreamAlias("reseller")
public class Reseller extends AbstractModel implements BeanInitable<Reseller>, MessageContainer,AfterSaving {

	public static enum ResellerType {
		@EnumMsgCode("reseller.type.standard")
		Standard,
		@EnumMsgCode("reseller.type.nodata")
		NoData,
		@EnumMsgCode("reseller.type.dataonly")
		DataOnly, 
		@EnumMsgCode("reseller.type.testlicense")
		TestLicense, 
		@EnumMsgCode("reseller.type.supplier")
		Supplier, 
		@EnumMsgCode("reseller.type.internal")
		Internal
	};

	public static enum ResellerStatus {
		@EnumMsgCode("status.setup")
		Setup,
		@EnumMsgCode("status.active")
		Active,
		@EnumMsgCode("status.onhold")
		OnHold, 
		@EnumMsgCode("status.disabled")
		Disabled
	};

	private Integer serialNo;
	private UUID userId;
	private String company;
	private String website;
	private String ecomSite;
	private String country;
	private Address address;
	private Date createdDatetime;
	private ResellerType type;
	private ResellerStatus status;
	private UUID agencyId;
	private UUID salesRepID;
	private String regKey;
	private String hardwareInfo;
	private String version;
	private Date lastSyncDate;
	private String email_cc;
	private BillingInfo billingInfo;
	private ResellerUsage usage;
	private Date nextReviewDate;
	private Date lastReviewDate;
	private UUID lastReviewUser;
	private String reviewComment;
	private Map<LicenseType, LicModule> licenses;
	
	public Reseller() {
		billingInfo = new BillingInfo();
		usage = new ResellerUsage();
		licenses = new HashMap<LicenseType, LicModule>();
	}

	public Reseller(int serialNo) {
		this.serialNo = serialNo;
		billingInfo = new BillingInfo();
		usage = new ResellerUsage();
		licenses = new HashMap<LicenseType, LicModule>();
	}

	@IsellPathField
	public Integer getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getEcomSite() {
		return ecomSite;
	}

	public void setEcomSite(String ecomSite) {
		this.ecomSite = ecomSite;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public ResellerType getType() {
		return type;
	}

	public void setType(ResellerType type) {
		this.type = type;
	}

	public ResellerStatus getStatus() {
		return status;
	}

	public void setStatus(ResellerStatus status) {
		this.status = status;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}

	public void setLastSyncDate(Date lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}

	public Date getLastSyncDate() {
		return lastSyncDate;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	@Override
	public Reseller init() {
		type = ResellerType.Standard;
		status = ResellerStatus.Setup;
		createdDatetime = new Date();
		Address defaultAddress = new GeneralAddress();
		defaultAddress.setCountryCode("au");
		address = defaultAddress;
		country = "au";
		version = "1.0";
		return this;
	}

	public void setRegKey(String regKey) {
		this.regKey = regKey;
	}

	public String getRegKey() {
		return regKey;
	}

	private String getRegistrationKeyPart(int start, int stop) {
		String reg = getRegKey();
		int len = reg.length();
		if (len <= start) {
			return "";
		}
		len = Math.min(len, stop);
		return reg.substring(start, len);
	}

	public String displayRegKey() {
		if (getRegKey() == null || getRegKey().length() == 0) {
			return "";
		}
		String regkeyDisp = getRegistrationKeyPart(0, 4) + "-"
				+ getRegistrationKeyPart(4, 8) + "-"
				+ getRegistrationKeyPart(8, 12) + "-"
				+ getRegistrationKeyPart(12, 16);
		String strSerialNo = serialNo > 10000000 ? ""+serialNo : (serialNo > 1000000 ? "0"+serialNo : "00"+serialNo);
		return regkeyDisp+'-'+strSerialNo.substring(0,4)+'-'+strSerialNo.substring(4);
	}

	@DeepCopy
	public BillingInfo getBillingInfo() {
		return billingInfo;
	}

	public void setBillingInfo(BillingInfo billingInfo) {
		this.billingInfo = billingInfo;
	}

	@DeepCopy
	public ResellerUsage getUsage() {
		return usage;
	}

	public void setUsage(ResellerUsage usage) {
		this.usage = usage;
	}

	@DeepCopy
	public Map<LicenseType, LicModule> getLicenses() {
		return licenses;
	}

	public void setLicenses(Map<LicenseType, LicModule> licenses) {
		this.licenses = licenses;
	}

	@Override
	public String toString() {
		return "Reseller [serialNo=" + serialNo + ", userId=" + userId + ", company=" + company + ", website=" + website + ", ecomSite=" + ecomSite
				+ ", country=" + country + ", address=" + address + ", createdDatetime=" + createdDatetime + ", type=" + type + ", status=" + status
				+ ", agencyId=" + agencyId + ", salesRepID=" + salesRepID + ", regKey=" + regKey + ", version=" + version + ", lastSyncDate="
				+ lastSyncDate + ", billingInfo=" + billingInfo + ", usage=" + usage + ", licenses=" + licenses + "]";
	}

	@Override
	public String toMessage() {
		return S3Manager.getBucket()+':'+S3Utils.getS3Key(this);
	}

	@Override
	public void operate(ApplicationContext context) {
		ResellerDao resellerDao=context.getBean(ResellerDao.class);
		resellerDao.indexResellerSearchBean(serialNo);
		resellerDao.indexResellerSupplierMap(serialNo,null);
		resellerDao.saveOperationHistory(this);
	}

	public String getEmail_cc() {
		return email_cc;
	}

	public void setEmail_cc(String email_cc) {
		this.email_cc = email_cc;
	}

	public void setHardwareInfo(String hardwareInfo) {
		this.hardwareInfo = hardwareInfo;
	}

	public String getHardwareInfo() {
		return hardwareInfo;
	}

	public Date getNextReviewDate() {
		return nextReviewDate;
	}

	public void setNextReviewDate(Date nextReviewDate) {
		this.nextReviewDate = nextReviewDate;
	}

	public Date getLastReviewDate() {
		return lastReviewDate;
	}

	public void setLastReviewDate(Date lastReviewDate) {
		this.lastReviewDate = lastReviewDate;
	}

	public UUID getLastReviewUser() {
		return lastReviewUser;
	}

	public void setLastReviewUser(UUID lastReviewUser) {
		this.lastReviewUser = lastReviewUser;
	}

	public String getReviewComment() {
		return reviewComment;
	}

	public void setReviewComment(String reviewComment) {
		this.reviewComment = reviewComment;
	}

}