package au.com.isell.rlm.module.user.constant;

import au.com.isell.rlm.common.freemarker.EnumMsgCode;

public enum UserStatus{
	@EnumMsgCode("status.active")
	ACTIVE,
	@EnumMsgCode("status.tempdisabled")
	TEMPDISABLED,
	@EnumMsgCode("status.disabled")
	DISABLED;
	
}
