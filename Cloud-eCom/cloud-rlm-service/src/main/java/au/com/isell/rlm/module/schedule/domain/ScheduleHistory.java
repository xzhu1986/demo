package au.com.isell.rlm.module.schedule.domain;

import java.util.Date;
import java.util.UUID;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/schedules/${scheduleId}/histories/${operationDate}.info.json")
@ISellIndex(name="schedules", type="history")
@XStreamAlias("schedule-history")
public class ScheduleHistory extends AbstractModel {

	private UUID scheduleId;
	private Date operationDate;
	private Status status;
	private String ipAddress;
	private UUID userId;
	private String notes;

	@ISellIndexKey
	public String getKey() {
		return "schedules/"+scheduleId+"/history/"+(operationDate.getTime()/1000)+".info.json";
	}

	@IsellPathField
	@ISellIndexValue
	public UUID getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(UUID scheduleId) {
		this.scheduleId = scheduleId;
	}
	@IsellPathField
	@ISellIndexValue
	public Date getOperationDate() {
		return operationDate;
	}
	public void setOperationDate(Date operationDate) {
		this.operationDate = operationDate;
	}
	@ISellIndexValue
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	@ISellIndexValue
	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

}
