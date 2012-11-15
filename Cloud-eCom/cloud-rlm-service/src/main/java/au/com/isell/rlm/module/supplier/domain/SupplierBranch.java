package au.com.isell.rlm.module.supplier.domain;

import java.util.UUID;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.aws.S3Utils;
import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.message.MessageContainer;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.GeneralAddress;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/suppliers/${supplierId}/branches/${branchId}.info.json")
@ISellIndex(name="suppliers", type="branch")
@MessageDef(queue="apollo", type="supplier-branch")
@XStreamAlias("supplier-branch")
public class SupplierBranch extends AbstractModel implements BeanInitable<SupplierBranch>, MessageContainer {
	public static enum Status {
		@EnumMsgCode("status.active")
		Active, 
		@EnumMsgCode("status.disabled")
		Disabled
	}

	private UUID branchId;
	private int supplierId;
	private String name;
	private Status status;
	private String accountNumber;
	private String phone;
	private String international;
	private String fax;
	private String country;
	private Address address;
	private Address postalAddress;
	private Address warehouseAddress;
	private boolean deleted = false;
	
	public SupplierBranch() {
		super();
	}
	public SupplierBranch(UUID branchId) {
		this();
		this.branchId = branchId;
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
	public UUID getBranchId() {
		return branchId;
	}
	public void setBranchId(UUID branchId) {
		this.branchId = branchId;
	}
	@ISellIndexValue(wildcard=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public Address getPostalAddress() {
		return postalAddress;
	}
	public void setPostalAddress(Address postalAddress) {
		this.postalAddress = postalAddress;
	}
	public Address getWarehouseAddress() {
		return warehouseAddress;
	}
	public void setWarehouseAddress(Address warehouseAddress) {
		this.warehouseAddress = warehouseAddress;
	}
	@ISellIndexValue
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getInternational() {
		return international;
	}
	public void setInternational(String international) {
		this.international = international;
	}
	@Override
	public SupplierBranch init() {
		Address defaultAddress=new GeneralAddress();
		defaultAddress.setCountryCode("au");
		address=defaultAddress;
		this.deleted = false;
		return this;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	@ISellIndexValue
	public boolean isDeleted() {
		return deleted;
	}
	@Override
	public String toMessage() {
		return S3Manager.getBucket()+':'+S3Utils.getS3Key(this);
	}
}
