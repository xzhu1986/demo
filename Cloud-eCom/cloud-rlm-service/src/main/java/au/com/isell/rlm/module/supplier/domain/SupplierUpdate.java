package au.com.isell.rlm.module.supplier.domain;

import java.util.UUID;

public class SupplierUpdate {

	private UUID scheduleId;
	private int dataSkillLevelRequired;
	private String summary;
	private String instruction;
	private String webLink;
	private String username;
	private String password;
	private int priority;
	
	public int getDataSkillLevelRequired() {
		return dataSkillLevelRequired;
	}

	public void setDataSkillLevelRequired(int dataSkillLevelRequired) {
		this.dataSkillLevelRequired = dataSkillLevelRequired;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getWebLink() {
		return webLink;
	}

	public void setWebLink(String webLink) {
		this.webLink = webLink;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setScheduleId(UUID scheduleId) {
		this.scheduleId = scheduleId;
	}

	public UUID getScheduleId() {
		return scheduleId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
