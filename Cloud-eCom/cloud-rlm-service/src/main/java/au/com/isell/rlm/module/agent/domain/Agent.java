package au.com.isell.rlm.module.agent.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.AfterSaving;
import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.user.service.PermissionPreloadingService;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/agents/${agentId}/basic.info.json")
@ISellIndex(name = "agents", type = "basic")
@XStreamAlias("agent")
public class Agent extends AbstractModel implements BeanInitable<Agent>, AfterSaving {
	public enum Status {
		@EnumMsgCode("status.pending")
		Pending(), @EnumMsgCode("status.active")
		Active(), @EnumMsgCode("status.disabled")
		Disabled();

	}

	private UUID agentId;
	private String name;
	private String country;
	private Address address;
	private String company;
	private Status status;
	private String phone;
	private String internationalPhone;
	private String fax;
	private String email;
	private String accountsPhone;
	private String accountsEmail;
	private String contactTimes;
	private boolean includeAddrInContact;
	private BigDecimal defaultCommission;
	private InvoiceTermsTemplate invoiceTermsTemplate;

	public Agent() {
	}

	public Agent(String country) {
		this.country = country;
	}

	@Override
	public void operate(ApplicationContext context) {
		context.getBean(PermissionPreloadingService.class).updatePermissionCache(new ClassPathResource("permissions/agent/show-supplier-by-country.xml"));
	}

	@ISellIndexKey
	@IsellPathField
	public UUID getAgentId() {
		return agentId;
	}

	public void setAgentId(UUID agentId) {
		this.agentId = agentId;
	}

	@ISellIndexValue(wildcard = true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ISellIndexValue
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@ISellIndexValue(wildcard = true)
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@ISellIndexValue
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public Agent init() {
		Assert.notNull(country);
		Address address = new GeneralAddress();
		address.setCountryCode(country);
		setAddress(address);
		InvoiceTermsTemplate invoiceTermsTemplate = new InvoiceTermsTemplate();
		invoiceTermsTemplate.setContent(InvoiceTermsTemplate.DEFAULE_INVOICE_TERMS);
		setInvoiceTermsTemplate(invoiceTermsTemplate);
		return this;
	}

	public void setDefaultCommission(BigDecimal defaultCommission) {
		this.defaultCommission = defaultCommission;
	}

	public BigDecimal getDefaultCommission() {
		return defaultCommission;
	}

	public InvoiceTermsTemplate getInvoiceTermsTemplate() {
		return invoiceTermsTemplate;
	}

	public void setInvoiceTermsTemplate(InvoiceTermsTemplate invoiceTermsTemplate) {
		this.invoiceTermsTemplate = invoiceTermsTemplate;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhone() {
		return phone;
	}

	public String getInternationalPhone() {
		return internationalPhone;
	}

	public void setInternationalPhone(String internationalPhone) {
		this.internationalPhone = internationalPhone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAccountsPhone() {
		return accountsPhone;
	}

	public void setAccountsPhone(String accountsPhone) {
		this.accountsPhone = accountsPhone;
	}

	public String getAccountsEmail() {
		return accountsEmail;
	}

	public void setAccountsEmail(String accountsEmail) {
		this.accountsEmail = accountsEmail;
	}

	public String getContactTimes() {
		return contactTimes;
	}

	public void setContactTimes(String contactTimes) {
		this.contactTimes = contactTimes;
	}

	public void setIncludeAddrInContact(boolean showAddressOnContactUs) {
		this.includeAddrInContact = showAddressOnContactUs;
	}

	public boolean isIncludeAddrInContact() {
		return includeAddrInContact;
	}

}
