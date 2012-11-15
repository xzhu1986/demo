package au.com.isell.rlm.module.reseller.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.constant.UserType;
import au.com.isell.rlm.module.user.domain.User;


@ISellIndex(name = "users", type = "reseller", checkParents=true)
public class ResellerUser extends User implements BeanInitable<ResellerUser>{
	
	public static enum Role {
		TECHNOLOGY,
		ACCOUNTANT
	}
	
	private Integer serialNo;
	private boolean primary;
	@JsonIgnore
	private transient String resellerName;
	
	private String phone;
	private String mobile;
	private String altPhone;
	private String fax;
	private String jobPosition;

	public ResellerUser() {
		setType(UserType.Reseller);
	}
	
	public ResellerUser(Reseller reseller) {
		this();
		setSerialNo(reseller.getSerialNo());
		setResellerName(reseller.getCompany());
	}
	
	@Override
	@JsonIgnore
	@ISellIndexValue
	public String getToken(){
		return serialNo+ User.tokenSpliter +super.getEmail();
	}
	
	@ISellIndexValue(wildcard=true)
	public Integer getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}
	
	@ISellIndexValue(wildcard=true)
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
	public String getAltPhone() {
		return altPhone;
	}
	public void setAltPhone(String altPhone) {
		this.altPhone = altPhone;
	}
	@ISellIndexValue(wildcard=true)
	public String getJobPosition() {
		return jobPosition;
	}
	public void setJobPosition(String jobPosition) {
		this.jobPosition = jobPosition;
	}

	@Override
	public String toString() {
		return "ResellerUser [serialNo=" + serialNo + ", phone=" + phone + ", mobile=" + mobile + ", altPhone=" + altPhone + ", jobPosition="
				+ jobPosition + "]";
	}

	@Override
	public ResellerUser init() {
		super.setStatus(UserStatus.ACTIVE);
		return this;
	}

	public void setResellerName(String resellerName) {
		this.resellerName = resellerName;
	}
	@ISellIndexValue(wildcard=true)
	public String getResellerName() {
		return resellerName;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getFax() {
		return fax;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
	@ISellIndexValue
	public boolean isPrimary() {
		return primary;
	}
}
