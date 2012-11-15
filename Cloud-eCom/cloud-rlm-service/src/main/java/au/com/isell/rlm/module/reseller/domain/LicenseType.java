package au.com.isell.rlm.module.reseller.domain;

import au.com.isell.rlm.common.freemarker.EnumMsgCode;

public enum LicenseType {
	@EnumMsgCode("license.type.itqlicense")
	ITQLicense, 
	@EnumMsgCode("license.type.ecomlicense")
	EComLicense, 
	@EnumMsgCode("license.type.epdlicense")
	EPDLicense
}
