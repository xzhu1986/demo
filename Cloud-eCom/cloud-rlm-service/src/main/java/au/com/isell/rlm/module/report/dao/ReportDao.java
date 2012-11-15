package au.com.isell.rlm.module.report.dao;

import java.io.File;
import java.util.Map;

import au.com.isell.rlm.module.report.domain.ReportTplHistory;

public interface ReportDao {
	void saveTplHistory(ReportTplHistory history);
	
	ReportTplHistory getReportTplHistory(String tplName);
	
	String[] getRenderedFileHash(String oldRenderedFileOnCloud);
	
	void uploadFile(File file,String key,Map<String, String> meta) ;
	
	String getRendererFileCloudUrl(String key);
}
