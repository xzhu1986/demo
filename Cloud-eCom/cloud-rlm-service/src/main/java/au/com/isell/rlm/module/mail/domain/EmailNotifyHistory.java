package au.com.isell.rlm.module.mail.domain;

import java.util.Date;
import java.util.UUID;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;

@ISellIndex(name = "emails", type = "notify_history")
public class EmailNotifyHistory {
	private UUID notifyTypeId;
	private Date date;
	private String rawId;
	private String type;

	public EmailNotifyHistory(UUID notifyTypeId, Date date, String rawId,String type) {
		super();
		this.notifyTypeId = notifyTypeId;
		this.date = date;
		this.rawId = rawId;
		this.type=type;
	}

	public EmailNotifyHistory() {
		super();

	}

	@ISellIndexValue(index = "analyzed")
	public UUID getNotifyTypeId() {
		return notifyTypeId;
	}

	public void setNotifyTypeId(UUID notifyTypeId) {
		this.notifyTypeId = notifyTypeId;
	}

	@ISellIndexKey
	public String getKey() {
		return notifyTypeId + "_" + date.getTime() + "_" + rawId;
	}

	@ISellIndexValue(index = "analyzed")
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@ISellIndexValue(index = "analyzed")
	public String getRawId() {
		return rawId;
	}

	public void setRawId(String rawId) {
		this.rawId = rawId;
	}

	@ISellIndexValue
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
}
