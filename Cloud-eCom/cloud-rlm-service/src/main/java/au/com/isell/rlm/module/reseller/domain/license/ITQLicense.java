package au.com.isell.rlm.module.reseller.domain.license;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.bean.BeanInitable;

@XStreamAlias("itq-lic")
public class ITQLicense extends LicModule implements BeanInitable<ITQLicense>{

	private int itqFullAccessUsers;
	private int itqServiceUsers;
	private int itqCrmUsers;
	private int itqTempUsers;
	private Date itqTempExpiryDate;
	private boolean itqImageFeeds;
	private boolean bidImporter;
	private boolean itqApiSupport;
	private boolean itqSilentUpgrades;
	private boolean itqServiceManger;
	private boolean itqForeignCurrency;
	private boolean itqWarehouseModule;

	public ITQLicense() {
		super("itq");
	}

	public int getItqFullAccessUsers() {
		return itqFullAccessUsers;
	}

	public void setItqFullAccessUsers(int itqFullAccessUsers) {
		this.itqFullAccessUsers = itqFullAccessUsers;
	}

	public int getItqServiceUsers() {
		return itqServiceUsers;
	}

	public void setItqServiceUsers(int itqServiceUsers) {
		this.itqServiceUsers = itqServiceUsers;
	}

	public int getItqCrmUsers() {
		return itqCrmUsers;
	}

	public void setItqCrmUsers(int itqCrmUsers) {
		this.itqCrmUsers = itqCrmUsers;
	}

	public int getItqTempUsers() {
		return itqTempUsers;
	}

	public void setItqTempUsers(int itqTempUsers) {
		this.itqTempUsers = itqTempUsers;
	}

	public Date getItqTempExpiryDate() {
		return itqTempExpiryDate;
	}

	public void setItqTempExpiryDate(Date itqTempExpiryDate) {
		this.itqTempExpiryDate = itqTempExpiryDate;
	}

	public boolean isItqImageFeeds() {
		return itqImageFeeds;
	}

	public void setItqImageFeeds(boolean itqImageFeeds) {
		this.itqImageFeeds = itqImageFeeds;
	}

	public boolean isItqApiSupport() {
		return itqApiSupport;
	}

	public void setItqApiSupport(boolean itqApiSupport) {
		this.itqApiSupport = itqApiSupport;
	}

	public boolean isItqSilentUpgrades() {
		return itqSilentUpgrades;
	}

	public void setItqSilentUpgrades(boolean itqSilentUpgrades) {
		this.itqSilentUpgrades = itqSilentUpgrades;
	}

	public boolean isItqServiceManger() {
		return itqServiceManger;
	}

	public void setItqServiceManger(boolean itqServiceManger) {
		this.itqServiceManger = itqServiceManger;
	}

	public boolean isItqForeignCurrency() {
		return itqForeignCurrency;
	}

	public void setItqForeignCurrency(boolean itqForeignCurrency) {
		this.itqForeignCurrency = itqForeignCurrency;
	}

	public boolean isItqWarehouseModule() {
		return itqWarehouseModule;
	}

	public void setItqWarehouseModule(boolean itqWarehouseModule) {
		this.itqWarehouseModule = itqWarehouseModule;
	}

	public boolean isBidImporter() {
		return bidImporter;
	}

	public void setBidImporter(boolean bidImporter) {
		this.bidImporter = bidImporter;
	}

	@Override
	public ITQLicense init() {
		itqTempExpiryDate = DateUtils.addYears(new Date(), 1);
		super.setRenewalDate(DateUtils.addYears(new Date(), 1));
		return this;
	}

	@Override
	public String toString() {
		return "ITQLicense [itqFullAccessUsers=" + itqFullAccessUsers
				+ ", itqServiceUsers=" + itqServiceUsers + ", itqCrmUsers="
				+ itqCrmUsers + ", itqTempUsers=" + itqTempUsers
				+ ", itqTempExpiryDate=" + itqTempExpiryDate
				+ ", itqImageFeeds=" + itqImageFeeds + ", bidImporter="
				+ bidImporter + ", itqApiSupport=" + itqApiSupport
				+ ", itqSilentUpgrades=" + itqSilentUpgrades
				+ ", itqServiceManger=" + itqServiceManger
				+ ", itqForeignCurrency=" + itqForeignCurrency
				+ ", itqWarehouseModule=" + itqWarehouseModule + ", getType()="
				+ getType() + ", getAnnualFee()=" + getAnnualFee()
				+ ", getRenewalDate()=" + getRenewalDate() + "]";
	}
	
	
}
