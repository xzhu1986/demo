package au.com.isell.rlm.module.jbe.domain;

public enum JobStatus {//1 based
	Pending, Active, Completed, Cancelled;
	
	public int getCode(){
		return this.ordinal()+1;
	}
}
