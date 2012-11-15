package au.com.isell.rlm.module.jbe.vo;

import org.apache.commons.lang.StringUtils;

import au.com.isell.rlm.module.jbe.common.Request;

public class JobSearchVals {
	private String jobNo;
	private String jobName;
	private String type;
	private String status;
	private String stage;
	private String priority;

	public void addSearchParam(Request req){
		if(StringUtils.isNotEmpty(jobNo)){
			req.addChild("jobNo", jobNo);
		}
		if(StringUtils.isNotEmpty(jobName)){
			req.addChild("searchText", jobName);
		}
		if(StringUtils.isNotEmpty(type)){
			req.addChild("typeID", type);
		}
		if(StringUtils.isNotEmpty(stage)){
			req.addChild("jobStage", stage);
		}
		if(StringUtils.isNotEmpty(priority)){
			req.addChild("jobPriority", priority);
		}
	}
	
	public String getJobNo() {
		return jobNo;
	}

	public void setJobNo(String jobNo) {
		this.jobNo = jobNo;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

}
