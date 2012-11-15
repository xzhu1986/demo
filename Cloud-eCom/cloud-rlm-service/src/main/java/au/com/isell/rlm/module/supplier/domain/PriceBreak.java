package au.com.isell.rlm.module.supplier.domain;

import java.util.Date;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.message.MessageContainer;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

//@IsellPath("data/suppliers/${supplierId}/breaks/${priceBreakId}.info.json")
@ISellIndex(name="suppliers", type="price_break")
@MessageDef(queue="apollo", type="price-break")
public class PriceBreak extends AbstractModel implements BeanInitable<PriceBreak>, MessageContainer {
	public static enum Status {
		@EnumMsgCode("status.setup")
		Setup, 
		@EnumMsgCode("status.active")
		Active,
		@EnumMsgCode("status.disabled")
		Disabled}

	private int supplierId;
	private UUID priceBreakId;
	private int idataId;
	private String name;
	private Date lastCacheDate;
	private Date fullFeedDate;
	private Date createDate;
	private Date disabledDate;
	private Status status;
	@JsonIgnore
	private transient String country;

	public PriceBreak() {
		
	}
	
	public PriceBreak(int supplierId, UUID priceBreakId) {
		this();
		setSupplierId(supplierId);
		setPriceBreakId(priceBreakId);
	}

	@Override
	public PriceBreak init() {
		createDate=new Date();
		return this;
	}
	
	@IsellPathField
	@ISellIndexValue
	public int getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}
	@IsellPathField
	@ISellIndexKey
	public UUID getPriceBreakId() {
		return priceBreakId;
	}
	public void setPriceBreakId(UUID priceBreakId) {
		this.priceBreakId = priceBreakId;
	}
	@ISellIndexValue
	public int getIdataId() {
		return idataId;
	}
	public void setIdataId(int idataId) {
		this.idataId = idataId;
	}
	@ISellIndexValue(wildcard=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getLastCacheDate() {
		return lastCacheDate;
	}
	public void setLastCacheDate(Date lastCacheDate) {
		this.lastCacheDate = lastCacheDate;
	}
	public Date getFullFeedDate() {
		return fullFeedDate;
	}
	public void setFullFeedDate(Date fullFeedDate) {
		this.fullFeedDate = fullFeedDate;
	}
	public void setDisabledDate(Date disabledDate) {
		this.disabledDate = disabledDate;
	}
	public Date getDisabledDate() {
		return disabledDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	@ISellIndexValue
	public Status getStatus() {
		return status;
	}

	@Override
	public String toMessage() {
		return S3Manager.getBucket()+':'+supplierId+":"+priceBreakId;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	@ISellIndexValue
	public String getCountry() {
		return country;
	}
}
