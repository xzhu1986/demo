package au.com.isell.rlm.module.report.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.io.IOUtils;
import au.com.isell.common.util.ContentType;
import au.com.isell.common.util.HashBuilder;
import au.com.isell.rlm.common.exception.BizAssert;
import au.com.isell.rlm.module.report.dao.ReportDao;
import au.com.isell.rlm.module.report.domain.ReportTplHistory;
import au.com.isell.rlm.module.report.service.ReportService;
import au.com.isell.rlm.module.report.utils.ReportTemplateTransferUtils;

/**
 * ref : https://dws.docmosis.com/services/rs/application.wadl <br/>
 * all tplName should have correct suffix
 * 
 * @author frankw 16/04/2012
 */
@Service
public class ReportServiceImpl implements ReportService {
	private static final String baseUrl = "https://dws.docmosis.com/services/rs";
	@Value("${report.accessKey}")
	private String accessKey ;
	@Autowired
	private ReportDao reportDao;

	@Override
	public void uploadTpl(InputStream in, String tplName) {
		Properties params = new Properties();
		params.put("accessKey", accessKey);
		params.put("templateName", tplName);
		// params.put("templateDescription", "");
		// params.put("isSystemTemplate", "false");//true or false(default)
		// params.put("devMode", "true");// true or false(default)
		// params.put("fieldDelimPrefix", "<<");
		// params.put("fieldDelimSuffix", ">>");
		File uploadFile = null;
		try {
			uploadFile = ReportTemplateTransferUtils.createTempFile(tplName.replace("/", "_").replace("\\", "_"), ".temp");
			OutputStream out = new FileOutputStream(uploadFile);
			IOUtils.transfer(in, out, 4096);

			String hash = HashBuilder.MD5.getFileHash(uploadFile);
			reportDao.saveTplHistory(new ReportTplHistory(tplName, hash,new Date()));

			ReportTemplateTransferUtils.upload(baseUrl + "/uploadTemplate", "templateFile", uploadFile, params);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			uploadFile.delete();
		}
	}

	@Override
	public void downloadTpl(String tplName, OutputStream out) {
		Properties params = new Properties();
		params.put("accessKey", accessKey);
		params.put("templateName", tplName);
		ReportTemplateTransferUtils.request(baseUrl + "/getTemplate", params, out);
	}
	
	@Override
	public Date getLastTplUpdateDate(String tplName) {
		ReportTplHistory history = reportDao.getReportTplHistory(tplName);
		return history==null? null:history.getUploadDate();
	}

	@Override
	public String getRendererFileCloudUrl(String tplName, String outputName, String fillData, String oldRenderedFileOnCloud, String newRenderedFileOnCloud) {
		Assert.hasText(newRenderedFileOnCloud, "newRenderedFileOnCloud should not be null");
		String renderDataStr = fillData;
		String dataHash = HashBuilder.MD5.getStringHash(renderDataStr);
		ReportTplHistory history = reportDao.getReportTplHistory(tplName);
		BizAssert.notNull(history, "error.reoport.template_not_upload");
		String tplHash = history.getTplHash();

		String[] oldHashes = reportDao.getRenderedFileHash(oldRenderedFileOnCloud);
		if (oldHashes != null && tplHash != null && tplHash.equals(oldHashes[0]) && dataHash.equals(oldHashes[1])) {
			newRenderedFileOnCloud = oldRenderedFileOnCloud;
		} else{
			doRender(tplName, outputName, newRenderedFileOnCloud, renderDataStr, dataHash, tplHash);
		}
		
		return reportDao.getRendererFileCloudUrl(newRenderedFileOnCloud);

	}

	private void doRender(String tplName, String outputName, String newRenderedFileOnCloud, String renderDataStr, String dataHash, String tplHash) {
		File outputFile = null;
		try {
			Properties params = new Properties();
			params.put("accessKey", accessKey);
			params.put("templateName", tplName);
			params.put("outputName", outputName);
			params.put("data", renderDataStr);
			params.put("outputFormat", "pdf");
			outputFile = ReportTemplateTransferUtils.createTempFile("reportRenderFile", ".temp");
			OutputStream out = new FileOutputStream(outputFile);
			ReportTemplateTransferUtils.request(baseUrl + "/render", params, out);

			cacheRenderedFile(newRenderedFileOnCloud, dataHash, tplHash, outputFile,outputName);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			outputFile.delete();
		}
	}

	private void cacheRenderedFile(String newRenderedFileOnCloud, String dataHash, String tplHash, File outputFile,String fileName) {
		Map<String, String> newMeta = new HashMap<String, String>();
		newMeta.put("tplhash", tplHash);
		newMeta.put("datahash", dataHash);
		newMeta.put(S3Manager.META_CONTENTTYPE, ContentType.geType(fileName).getValue());
		reportDao.uploadFile(outputFile, newRenderedFileOnCloud, newMeta);
	}

}
