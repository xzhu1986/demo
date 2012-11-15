package au.com.isell.rlm.module.report.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public interface ReportService {
	void uploadTpl(InputStream in, String tplName);

	void downloadTpl(String tplName, OutputStream out);

	String getRendererFileCloudUrl(String tplName, String outputName, String fillData, String oldRenderedFileOnCloud, String newRenderedFileOnCloud);

	Date getLastTplUpdateDate(String tplName);
}
