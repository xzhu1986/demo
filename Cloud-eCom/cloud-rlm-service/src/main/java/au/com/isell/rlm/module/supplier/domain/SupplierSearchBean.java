package au.com.isell.rlm.module.supplier.domain;

import java.util.Date;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.supplier.domain.Supplier.Status;

@ISellIndex(name = "suppliers", type = "supplier")
public class SupplierSearchBean {
	private Supplier supplier;
	private GeneralAddress headOffice;
	private SupplierBillingInfo billingInfo;
	private int resellerCount=0;

	public SupplierSearchBean() {
		supplier = new Supplier();
		headOffice = new GeneralAddress();
		billingInfo = new SupplierBillingInfo();
		supplier.setBillingInfo(billingInfo);
	}

	public SupplierSearchBean(Supplier supplier, Address headOffice, int resellerCount) {
		this.supplier = supplier;
		this.billingInfo = supplier.getBillingInfo();
		this.headOffice = new GeneralAddress();
		if (headOffice != null) {
			this.headOffice.setRegion(headOffice.getRegion());
			this.headOffice.setCity(headOffice.getCity());
		}
		this.headOffice.setCountryCode(supplier.getCountry());
		this.resellerCount = resellerCount;
	}

	@ISellIndexKey
	public int getSupplierId() {
		return supplier.getSupplierId();
	}

	public void setSupplierId(int supplierId) {
		supplier.setSupplierId(supplierId);
	}

	@ISellIndexValue(wildcard=true)
	public String getShortName() {
		return supplier.getShortName();
	}

	public void setShortName(String shortName) {
		supplier.setShortName(shortName);
	}

	@ISellIndexValue(wildcard=true)
	public String getName() {
		return supplier.getName();
	}

	public void setName(String name) {
		supplier.setName(name);
	}

	@ISellIndexValue
	public String getCountry() {
		return headOffice.getCountryCode();
	}

	public void setCountry(String country) {
		headOffice.setCountryCode(country);
	}

	public void setStatus(Status active) {
		supplier.setStatus(active);
	}

	@ISellIndexValue
	public Status getStatus() {
		return supplier.getStatus();
	}

	public void setRegion(String region) {
		headOffice.setRegion(region);
	}

	@ISellIndexValue
	public String getRegion() {
		return headOffice.getRegion();
	}

	@ISellIndexValue
	public int getResellerCount() {
		return resellerCount;
	}

	public void setResellerCount(int resellerCount) {
		this.resellerCount = resellerCount;
	}

	@ISellIndexValue
	public int getProductCount() {
		return supplier.getProductCount();
	}

	public void setProductCount(int productCount) {
		supplier.setProductCount(productCount);
	}

	@ISellIndexValue
	public Date getLastImportDate() {
		return supplier.getLastImportDate();
	}

	public void setLastImportDate(Date lastImportDate) {
		supplier.setLastImportDate(lastImportDate);
	}

	public Supplier getSupplier() {
		return supplier;
	}

	@ISellIndexValue(wildcard=true)
	public String getCity() {
		return headOffice.getCity();
	}

	public void setCity(String city) {
		headOffice.setCity(city);
	}

	public Address getHeadOffice() {
		return headOffice;
	}

	public void setRenewDate(Date renewDate) {
		billingInfo.setRenewDate(renewDate);
	}

	@ISellIndexValue
	public Date getRenewDate() {
		return billingInfo.getRenewDate();
	}
	
	public void setEmail(String email){
		billingInfo.setEmail(email);
	}
	
	@ISellIndexValue(wildcard=true)
	public String getEmail() {
		return billingInfo.getEmail();
	}
	public void setPhone(String phone){
		billingInfo.setPhone(phone);
	}
	
	@ISellIndexValue(wildcard=true)
	public String getPhone() {
		return billingInfo.getPhone();
	}
	public void setMarketingSummary(String marketingSummary){
		supplier.setMarketingSummary(marketingSummary);
	}
	
	@ISellIndexValue(wildcard=true)
	public String getMarketingSummary() {
		return supplier.getMarketingSummary();
	}

	@ISellIndexValue(index="no")
	public String getEmail_cc() {
		return billingInfo.getEmail_cc();
	}

	public void setEmail_cc(String email_cc) {
		billingInfo.setEmail_cc(email_cc);
	}
	
	@ISellIndexValue(index="no")
	public String getWebAddress() {
		return supplier.getWebAddress();
	}
	
	public void setWebAddress(String webAddress) {
		supplier.setWebAddress(webAddress);
	}
	
	
	
}
