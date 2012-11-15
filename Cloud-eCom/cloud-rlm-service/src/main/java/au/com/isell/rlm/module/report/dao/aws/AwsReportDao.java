package au.com.isell.rlm.module.report.dao.aws;

import java.io.File;
import java.util.Map;

import org.springframework.stereotype.Repository;

import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.report.dao.ReportDao;
import au.com.isell.rlm.module.report.domain.ReportTplHistory;

import com.amazonaws.services.s3.model.S3Object;

@Repository
public class AwsReportDao extends DAOSupport implements ReportDao {

	@Override
	public void saveTplHistory(ReportTplHistory history) {
		super.save(history);
	}
	
	@Override
	public ReportTplHistory getReportTplHistory(String tplName){
		ReportTplHistory history=new ReportTplHistory();
		history.setTplName(tplName);
		return super.get(history);
	}
	
	@Override
	public String[] getRenderedFileHash(String oldRenderedFileOnCloud){
		S3Object s3Object=super.s3Manager.getS3Object(oldRenderedFileOnCloud);
		if(s3Object==null ) return null;
		Map<String,String> meta=s3Object.getObjectMetadata().getUserMetadata();
		String tplHash=meta.get("tplhash");//will auto converted to lowercase !s
		String dataHash=meta.get("datahash");
		return new String[]{tplHash,dataHash};
	}
	
	@Override
	public void uploadFile(File file,String key,Map<String, String> meta) {
		super.s3Manager.uploadFile1(key, file, meta, null);
	}

	@Override
	public String getRendererFileCloudUrl(String key) {
		return super.s3Manager.generateS3Url(key,60);
	}
	
}
