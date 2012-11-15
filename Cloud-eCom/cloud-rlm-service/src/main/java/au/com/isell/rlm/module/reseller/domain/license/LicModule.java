package au.com.isell.rlm.module.reseller.domain.license;

import java.math.BigDecimal;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import au.com.isell.common.model.AbstractModel;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(value = { @Type(value = ITQLicense.class, name = "ITQLicense"), @Type(value = EComLicense.class, name = "EComLicense"),
		@Type(value = EPDLicense.class, name = "EPDLicense") })
public class LicModule extends AbstractModel {
	
	private String type;
	
	private BigDecimal annualFee;
	private Date renewalDate;

	protected LicModule(String type) {
		setType(type);
	}
	public String getType() {
		return type;
	}
	private void setType(String type) {
		this.type = type;
	}
	public BigDecimal getAnnualFee() {
		return annualFee;
	}
	public void setAnnualFee(BigDecimal annualFee) {
		this.annualFee = annualFee;
	}
	public Date getRenewalDate() {
		return renewalDate;
	}
	public void setRenewalDate(Date renewalDate) {
		this.renewalDate = renewalDate;
	}
}
