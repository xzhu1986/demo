package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.aws.S3Utils;
import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.bean.FromProp;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.message.MessageContainer;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.module.supplier.domain.Supplier;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/reseller_supplier/${serialNo}/${supplierId}/basic.info.json")
@ISellIndex(name="reseller_suppliers",type="basic")
@MessageDef(queue="apollo", type="reseller-break")
@XStreamAlias("reseller-suppliers")
public class ResellerSupplierMap extends AbstractModel implements BeanInitable<ResellerSupplierMap>, MessageContainer {
	
	public static enum ApprovalStatus {
		@EnumMsgCode("status.pending")
		Pending,
		@EnumMsgCode("status.approved")
		Approved, 
		@EnumMsgCode("status.onhold")
		OnHold, 
		@EnumMsgCode("status.disabled")
		Disabled}
	
	private Integer serialNo;
	private Integer supplierId;
	private UUID priceBreakId;
	@JsonIgnore
	private transient String supplierName;
	@JsonIgnore
	private transient String supplierCountry;
	@JsonIgnore
	private transient String breakName;
	@JsonIgnore
	private transient String supEmail;
	@JsonIgnore
	private transient String supPhone;
	@JsonIgnore
	private transient String company;
	@JsonIgnore
	private transient String resCountry;
	@JsonIgnore
	private transient String resEmail;
	@JsonIgnore
	private transient String resPhone;
	@JsonIgnore
	private transient UUID resAgent;
	
	private ApprovalStatus status;
	private boolean existingCustomer;
	private String supplierAccountNumber;
	private String supplierUsername;
	private String supplierPassword;
	private Date approvalDate;
	private Date disabledDate;
	private String approveDetails;
	private Date lastDownload;
	private boolean fullFeedsOnly;
	private boolean sendOriginalTiers;
	private boolean useOriginalTiers;
	private String additionalInfo;
	private boolean supplierDisableRequested;
	private String supplierDisableComment;
	
	public ResellerSupplierMap() {
		super();
	}
	
	public ResellerSupplierMap(Integer serialNo, Integer supplierId) {
		super();
		this.serialNo = serialNo;
		this.supplierId = supplierId;
	}
	
	@Override
	public ResellerSupplierMap init() {
		status=ApprovalStatus.Pending;
		return this;
	}

	@ISellIndexKey
	public String getKey(){
		return serialNo+"_"+supplierId;
	}
	
	@IsellPathField
	@ISellIndexValue
	public Integer getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}
	@IsellPathField
	@ISellIndexValue
	public Integer getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}
	@ISellIndexValue
	public UUID getPriceBreakId() {
		return priceBreakId;
	}
	public void setPriceBreakId(UUID priceBreakId) {
		this.priceBreakId = priceBreakId;
	}
	@ISellIndexValue
	public ApprovalStatus getStatus() {
		return status;
	}
	public void setStatus(ApprovalStatus stauts) {
		this.status = stauts;
	}
	public boolean isExistingCustomer() {
		return existingCustomer;
	}
	public void setExistingCustomer(boolean existingCustomer) {
		this.existingCustomer = existingCustomer;
	}
	@ISellIndexValue(wildcard=true)
	public String getSupplierAccountNumber() {
		return supplierAccountNumber;
	}
	public void setSupplierAccountNumber(String supplierAccountNumber) {
		this.supplierAccountNumber = supplierAccountNumber;
	}
	@ISellIndexValue
	public Date getApprovalDate() {
		return approvalDate;
	}
	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}
	public String getApproveDetails() {
		return approveDetails;
	}
	public void setApproveDetails(String approveDetails) {
		this.approveDetails = approveDetails;
	}
	public boolean isFullFeedsOnly() {
		return fullFeedsOnly;
	}
	public void setFullFeedsOnly(boolean fullFeedsOnly) {
		this.fullFeedsOnly = fullFeedsOnly;
	}
	public boolean isSupplierDisableRequested() {
		return supplierDisableRequested;
	}
	public void setSupplierDisableRequested(boolean supplierDisableRequested) {
		this.supplierDisableRequested = supplierDisableRequested;
	}
	public String getSupplierDisableComment() {
		return supplierDisableComment;
	}
	public void setSupplierDisableComment(String supplierDisableComment) {
		this.supplierDisableComment = supplierDisableComment;
	}

	public void setSupplierPassword(String supplierPassword) {
		this.supplierPassword = supplierPassword;
	}

	public String getSupplierPassword() {
		return supplierPassword;
	}

	public void setSupplierUsername(String supplierUsername) {
		this.supplierUsername = supplierUsername;
	}
	
	@ISellIndexValue(wildcard=true)
	public String getCompany() {
		return company;
	}
	
	@FromProp(type=Reseller.class,value="company")
	public void setCompany(String company) {
		this.company = company;
	}
	@ISellIndexValue
	public String getBreakName() {
		return breakName;
	}
	public void setBreakName(String breakName) {
		this.breakName = breakName;
	}

	public String getSupplierUsername() {
		return supplierUsername;
	}
	@ISellIndexValue(wildcard=true)
	public String getSupplierName() {
		return supplierName;
	}

	@FromProp(type=Supplier.class,value="name")	
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	@ISellIndexValue
	public String getSupplierCountry() {
		return supplierCountry;
	}
	@FromProp(type=Supplier.class,value="country")
	public void setSupplierCountry(String supplierCountry) {
		this.supplierCountry = supplierCountry;
	}

	public void setLastDownload(Date lastDownload) {
		this.lastDownload = lastDownload;
	}

	public Date getLastDownload() {
		return lastDownload;
	}

	public Date getDisabledDate() {
		return disabledDate;
	}

	public void setDisabledDate(Date disabledDate) {
		this.disabledDate = disabledDate;
	}

	public boolean isSendOriginalTiers() {
		return sendOriginalTiers;
	}

	public void setSendOriginalTiers(boolean sendOriginalTiers) {
		this.sendOriginalTiers = sendOriginalTiers;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	

	@ISellIndexValue(wildcard=true)
	public String getSupEmail() {
		return supEmail;
	}
	@FromProp(type=Supplier.class,value="billingInfo.email")
	public void setSupEmail(String supEmail) {
		this.supEmail = supEmail;
	}

	@ISellIndexValue(wildcard=true)
	public String getSupPhone() {
		return supPhone;
	}

	@FromProp(type=Supplier.class,value="billingInfo.phone")
	public void setSupPhone(String supPhone) {
		this.supPhone = supPhone;
	}

	@Override
	public String toMessage() {
		return S3Manager.getBucket()+':'+S3Utils.getS3Key(this);
	}
	@ISellIndexValue(wildcard=true)
	public String getResCountry() {
		return resCountry;
	}
	@FromProp(type=Reseller.class,value="country")
	public void setResCountry(String resCountry) {
		this.resCountry = resCountry;
	}
	@ISellIndexValue(wildcard=true)
	public String getResEmail() {
		return resEmail;
	}
	@FromProp(type=Reseller.class,value="billingInfo.email")
	public void setResEmail(String resEmail) {
		this.resEmail = resEmail;
	}
	@ISellIndexValue(wildcard=true)
	public String getResPhone() {
		return resPhone;
	}
	@FromProp(type=Reseller.class,value="billingInfo.phone")
	public void setResPhone(String resPhone) {
		this.resPhone = resPhone;
	}
	@ISellIndexValue
	public UUID getResAgent() {
		return resAgent;
	}
	@FromProp(type=Reseller.class,value="agencyId")
	public void setResAgent(UUID resAgent) {
		this.resAgent = resAgent;
	}

	public boolean isUseOriginalTiers() {
		return useOriginalTiers;
	}

	public void setUseOriginalTiers(boolean useOriginalTiers) {
		this.useOriginalTiers = useOriginalTiers;
	}

	
}
