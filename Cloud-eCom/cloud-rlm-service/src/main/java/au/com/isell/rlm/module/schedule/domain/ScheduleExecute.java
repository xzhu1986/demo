package au.com.isell.rlm.module.schedule.domain;

import java.util.Date;
import java.util.UUID;

import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;

public class ScheduleExecute {
	private UUID scheduleId;
	private Status status;
	private Date nextExecuteDate;
	private Date recentExecuteDate;
	private String userName;
	private UUID userId;
	private boolean enable;
	
	@ISellIndexKey
	public UUID getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(UUID scheduleId) {
		this.scheduleId = scheduleId;
	}
	@ISellIndexValue
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	@ISellIndexValue
	public Date getNextExecuteDate() {
		return nextExecuteDate;
	}
	public void setNextExecuteDate(Date nextExecuteDate) {
		this.nextExecuteDate = nextExecuteDate;
	}
	@ISellIndexValue
	public Date getRecentExecuteDate() {
		return recentExecuteDate;
	}
	public void setRecentExecuteDate(Date recentExecuteDate) {
		this.recentExecuteDate = recentExecuteDate;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@ISellIndexValue(wildcard=true)
	public String getUserName() {
		return userName;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	@ISellIndexValue
	public boolean isEnable() {
		return enable;
	}
	@ISellIndexValue
	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
}
