package au.com.isell.rlm.module.schedule.domain;

import au.com.isell.rlm.common.freemarker.EnumMsgCode;

public enum Status {
	@EnumMsgCode("schedule.status.inprogress")
	InProgress,
	@EnumMsgCode("schedule.status.completed")
	Completed,
	@EnumMsgCode("schedule.status.fail")
	Failed
}