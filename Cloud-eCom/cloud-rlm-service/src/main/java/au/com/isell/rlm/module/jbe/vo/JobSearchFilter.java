package au.com.isell.rlm.module.jbe.vo;

import au.com.isell.rlm.module.jbe.domain.JobStatus;

public enum JobSearchFilter {
	All("All") {
		@Override
		public JobStatus getFilterStatus() {
			return null;
		}
	},
	Active("Active") {
		@Override
		public JobStatus getFilterStatus() {
			return  JobStatus.Active;
		}
	},
	Pending("Pending") {
		@Override
		public JobStatus getFilterStatus() {
			return  JobStatus.Pending;
		}
	},
	Completed("Completed") {
		@Override
		public JobStatus getFilterStatus() {
			return  JobStatus.Completed;
		}
	};

	public abstract JobStatus getFilterStatus();

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private JobSearchFilter(String display) {
		this.display = display;
	}

}
