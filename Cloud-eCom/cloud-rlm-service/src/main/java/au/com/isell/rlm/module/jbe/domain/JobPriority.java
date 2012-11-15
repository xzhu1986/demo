package au.com.isell.rlm.module.jbe.domain;

public enum JobPriority {
	Immediate, High, Medium, Low,Future;
	public int getCode(){
		return this.ordinal()+1;
	}
}
