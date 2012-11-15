package au.com.isell.rlm.module.jbe.vo;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import au.com.isell.rlm.module.jbe.common.Request;

public class JobAddBean {
	private String jobName;
	private String typeID;
	private String statusID;
	private String description;
	 private String jobEmail;
	private String jobPhone;
	private String jobPriority;
	
	public void addParams(Request request){
		Assert.hasText(jobName);
		Assert.hasText(typeID);
		Assert.hasText(description);
		Assert.hasText(jobPhone);
		Assert.hasText(jobEmail);
		Assert.hasText(statusID);
		
		request.addChild("jobName",jobName);
		request.addChild("typeID",typeID);
		request.addChild("description",description);
		request.addChild("jobPhone",jobPhone);
		request.addChild("statusID",statusID);
		request.addChild("jobEmail",jobEmail);
		if(StringUtils.isNotEmpty(jobPriority)){
			request.addChild("jobPriority",jobPriority);
		}
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getTypeID() {
		return typeID;
	}

	public void setTypeID(String typeID) {
		this.typeID = typeID;
	}

	public String getStatusID() {
		return statusID;
	}

	public void setStatusID(String statusID) {
		this.statusID = statusID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJobPhone() {
		return jobPhone;
	}

	public void setJobPhone(String jobPhone) {
		this.jobPhone = jobPhone;
	}

	public String getJobPriority() {
		return jobPriority;
	}

	public void setJobPriority(String jobPriority) {
		this.jobPriority = jobPriority;
	}

	public String getJobEmail() {
		return jobEmail;
	}

	public void setJobEmail(String jobEmail) {
		this.jobEmail = jobEmail;
	}
	

}
