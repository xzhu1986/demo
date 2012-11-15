package au.com.isell.rlm.module.supplier.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.user.constant.UserType;
import au.com.isell.rlm.module.user.domain.User;

@ISellIndex(name = "users", type = "supplier", checkParents=true)

public class SupplierUser extends User {
	private Integer supplierId;
	
	@JsonIgnore
	private transient String supplierName;
	private String phone;
	private String mobile;
	private String fax;
	
	public SupplierUser() {
		setType(UserType.Supplier);
	}

	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@Override
	public String toString() {
		return "SupplierUser [supplierId=" + supplierId + ", phone=" + phone + ", fax=" + fax + ", mobile=" + mobile + "]";
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}
	@ISellIndexValue(wildcard=true)
	public Integer getSupplierId() {
		return supplierId;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getFax() {
		return fax;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	@ISellIndexValue
	public String getSupplierName() {
		return supplierName;
	}
	
}
