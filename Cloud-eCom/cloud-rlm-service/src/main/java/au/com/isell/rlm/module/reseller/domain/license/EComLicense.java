package au.com.isell.rlm.module.reseller.domain.license;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.bean.BeanInitable;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

@XStreamAlias("ecom-lic")
public class EComLicense extends LicModule implements BeanInitable<EComLicense> {

	public EComLicense() {
		super("ecom");
	}

	public static enum EComType {
		@EnumMsgCode("ecom.type.none")
		None,
		@EnumMsgCode("ecom.type.ebiz_essentials")
		Ebiz_Essentials,
		@EnumMsgCode("ecom.type.advanced")
		Advanced,
		@EnumMsgCode("ecom.type.enterprise")
		Enterprise
	};

	private EComType ecomType;
	private boolean ecomMultiCatalogues;
	private boolean ecomExportCatalogues;
	private int ecomWebPortals;

	public EComType getEcomType() {
		return ecomType;
	}

	public void setEcomType(EComType ecomType) {
		this.ecomType = ecomType;
	}

	public boolean isEcomMultiCatalogues() {
		return ecomMultiCatalogues;
	}

	public void setEcomMultiCatalogues(boolean ecomMultiCatalogues) {
		this.ecomMultiCatalogues = ecomMultiCatalogues;
	}

	public boolean isEcomExportCatalogues() {
		return ecomExportCatalogues;
	}

	public void setEcomExportCatalogues(boolean ecomExportCatalogues) {
		this.ecomExportCatalogues = ecomExportCatalogues;
	}

	public int getEcomWebPortals() {
		return ecomWebPortals;
	}

	public void setEcomWebPortals(int ecomWebPortals) {
		this.ecomWebPortals = ecomWebPortals;
	}

	@Override
	public EComLicense init() {
		ecomType = EComType.None;
		super.setRenewalDate(DateUtils.addYears(new Date(), 1));
		return this;
	}

	@Override
	public String toString() {
		return "EComLicense [ecomType=" + ecomType + ", ecomMultiCatalogues="
				+ ecomMultiCatalogues + ", ecomExportCatalogues="
				+ ecomExportCatalogues + ", ecomWebPortals=" + ecomWebPortals
				+ ", getType()=" + getType() + ", getAnnualFee()="
				+ getAnnualFee() + ", getRenewalDate()=" + getRenewalDate()
				+ "]";
	}




	
}
