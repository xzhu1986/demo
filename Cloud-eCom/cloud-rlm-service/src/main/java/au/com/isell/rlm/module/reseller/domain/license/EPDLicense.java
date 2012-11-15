package au.com.isell.rlm.module.reseller.domain.license;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.bean.BeanInitable;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

@XStreamAlias("epd-lic")
public class EPDLicense extends LicModule implements BeanInitable<EPDLicense> {

	public static enum Level {
		@EnumMsgCode("license.level.none")
		None,
		@EnumMsgCode("license.level.standard")
		Standard, 
		@EnumMsgCode("license.level.premium")
		Premium, 
		@EnumMsgCode("license.level.enterprise")
		Enterprise
	}

	public EPDLicense() {
		super("epd");
	}

	private Level epdITQuoterUsage;
	private Level epdEcomUsage;
	private UUID epdAgencyID;

	public Level getEpdITQuoterUsage() {
		return epdITQuoterUsage;
	}

	public void setEpdITQuoterUsage(Level epdITQuoterUsage) {
		this.epdITQuoterUsage = epdITQuoterUsage;
	}

	public Level getEpdEcomUsage() {
		return epdEcomUsage;
	}

	public void setEpdEcomUsage(Level epdEcomUsage) {
		this.epdEcomUsage = epdEcomUsage;
	}

	public UUID getEpdAgencyID() {
		return epdAgencyID;
	}

	public void setEpdAgencyID(UUID epdAgencyID) {
		this.epdAgencyID = epdAgencyID;
	}

	@Override
	public EPDLicense init() {
		epdITQuoterUsage = Level.None;
		epdEcomUsage = Level.None;
		super.setRenewalDate(DateUtils.addYears(new Date(), 1));
		return this;
	}

	@Override
	public String toString() {
		return "EPDLicense [epdITQuoterUsage=" + epdITQuoterUsage
				+ ", epdEcomUsage=" + epdEcomUsage + ", epdAgencyID="
				+ epdAgencyID + ", getType()=" + getType()
				+ ", getAnnualFee()=" + getAnnualFee() + ", getRenewalDate()="
				+ getRenewalDate() + "]";
	}

}
