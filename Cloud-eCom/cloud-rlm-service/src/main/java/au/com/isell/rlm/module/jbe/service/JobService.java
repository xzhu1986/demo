package au.com.isell.rlm.module.jbe.service;

import java.util.Collection;
import java.util.Map;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.jbe.domain.JobStatus;
import au.com.isell.rlm.module.jbe.vo.JobAddBean;
import au.com.isell.rlm.module.jbe.vo.JobSearchVals;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;

public interface JobService {

	Pair<Integer, Collection> queryJobs(String filter, JobSearchVals searchVals, int pageSize, int pageNo);

	Map<String, Object> getDetail(String jobId);

	Collection getTasks(String jobId);

	Collection getNotes(String jobId);

	Collection getJobTypes();

	String createJob(JobAddBean addBean);

	Collection getJobStages();
	void createNote(String jobId, String note);

	Map<String, Object> createOrUpdateCustomer(ResellerUser formUser);

	int getJobCountByStatus(JobStatus jobStatus);

}
