package au.com.isell.rlm.module.supplier.domain;

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
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.supplier.dao.SupplierDao;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/suppliers/${supplierId}/basic.info.json")
@MessageDef(queue="apollo", type="supplier")
@XStreamAlias("supplier")
public class Supplier extends AbstractModel implements BeanInitable<Supplier>, MessageContainer,AfterSaving{
	public static enum Status {
		@EnumMsgCode("status.setup")
		Setup, 
		@EnumMsgCode("status.active")
		Active, 
		@EnumMsgCode("status.disabled")
		Disabled}

	private Integer supplierId;
	private UUID userId;
	private String shortName;
	private String name;
	private String webAddress;
	private String mainPhoneNumber;
	private String country;
	private Status status;
	private String isellNotes;
	private Date createDate;
	private String marketingSummary;
	private String detailedSummary;
	private int productCount;
	private Date lastImportDate;
	private UUID defaultBranchId;
	private UUID defaultPriceBreakID;
	private SupplierBillingInfo billingInfo;
	private DataRequest dataRequest;
	private SupplierUpdate updateInfo;
	private Map<UUID, PriceBreak> priceBreaks=new HashMap<UUID, PriceBreak>(0);
	
	public Supplier() {
		billingInfo = new SupplierBillingInfo();
		dataRequest = new DataRequest();
	}
	
	public Supplier(int supplierId) {
		this();
		setSupplierId(supplierId);
	}

	@IsellPathField
	public Integer getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getWebAddress() {
		return webAddress;
	}
	public void setWebAddress(String webAddress) {
		this.webAddress = webAddress;
	}
	public String getIsellNotes() {
		return isellNotes;
	}
	public void setIsellNotes(String notes) {
		this.isellNotes = notes;
	}
	public void setStatus(Status active) {
		this.status = active;
	}
	public Status getStatus() {
		return status;
	}
	public String getMainPhoneNumber() {
		return mainPhoneNumber;
	}
	public void setMainPhoneNumber(String mainPhoneNumber) {
		this.mainPhoneNumber = mainPhoneNumber;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getMarketingSummary() {
		return marketingSummary;
	}
	public void setMarketingSummary(String marketingSummary) {
		this.marketingSummary = marketingSummary;
	}
	public String getDetailedSummary() {
		return detailedSummary;
	}
	public void setDetailedSummary(String detailedSummary) {
		this.detailedSummary = detailedSummary;
	}
	public Date getLastImportDate() {
		return lastImportDate;
	}
	public void setLastImportDate(Date lastImportDate) {
		this.lastImportDate = lastImportDate;
	}
	public void setDefaultBranchId(UUID headOfficeId) {
		this.defaultBranchId = headOfficeId;
	}
	public UUID getDefaultBranchId() {
		return defaultBranchId;
	}

	public UUID getDefaultPriceBreakID() {
		return defaultPriceBreakID;
	}

	public void setDefaultPriceBreakID(UUID defaultPriceBreakID) {
		this.defaultPriceBreakID = defaultPriceBreakID;
	}
	@Override
	public Supplier init() {
		status = Status.Setup;
		createDate = new Date();
		return this;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}

	public void setDataRequest(DataRequest dataRequest) {
		this.dataRequest = dataRequest;
	}

	@DeepCopy
	public DataRequest getDataRequest() {
		return dataRequest;
	}

	public void setBillingInfo(SupplierBillingInfo billingInfo) {
		this.billingInfo = billingInfo;
	}
	@DeepCopy
	public SupplierBillingInfo getBillingInfo() {
		return billingInfo;
	}

	@Override
	public String toString() {
		return "Supplier [supplierId=" + supplierId + ", userId=" + userId
				+ ", shortName=" + shortName + ", name=" + name
				+ ", webAddress=" + webAddress + ", mainPhoneNumber="
				+ mainPhoneNumber + ", country=" + country + ", status="
				+ status + ", isellNotes=" + isellNotes + ", createDate="
				+ createDate + ", marketingSummary=" + marketingSummary
				+ ", detailedSummary=" + detailedSummary + ", lastImportDate=" 
				+ lastImportDate + ", defaultBranchId="
				+ defaultBranchId + ", defaultPriceBreakID="
				+ defaultPriceBreakID + ", billingInfo=" + billingInfo
				+ ", dataRequest=" + dataRequest + "]";
	}

	@Override
	public String toMessage() {
		return S3Manager.getBucket()+':'+S3Utils.getS3Key(this);
	}

	public void setUpdateInfo(SupplierUpdate updateInfo) {
		this.updateInfo = updateInfo;
	}

	public SupplierUpdate getUpdateInfo() {
		return updateInfo;
	}

	public void setPriceBreaks(Map<UUID, PriceBreak> priceBreaks) {
		this.priceBreaks = priceBreaks;
	}

	@DeepCopy
	public Map<UUID, PriceBreak> getPriceBreaks() {
		return priceBreaks;
	}
	
	public void addPriceBreak(PriceBreak priceBreak){
		this.priceBreaks.put(priceBreak.getPriceBreakId(), priceBreak);
	}
	
	
	@Override
	public void operate(ApplicationContext context) {
		SupplierDao supplierDao=context.getBean(SupplierDao.class);
		supplierDao.indexSupplier(supplierId);
		supplierDao.saveOperationHistory(this);
		
		ResellerDao resellerDao=context.getBean(ResellerDao.class);
		resellerDao.indexResellerSupplierMap(null,supplierId);
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}

	public int getProductCount() {
		return productCount;
	}

}

