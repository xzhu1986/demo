package au.com.isell.rlm.module.upload.dao.aws;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.upload.dao.FileDao;

/**
 * @author frankw 22/05/2012
 */
@Service
public class AwsFileDao extends DAOSupport implements FileDao{
	@Override
	public String getCloudFileUrl(String key) {
		return super.s3Manager.generateS3Url(key, 60);
	}
	
	@Override
	public void deleteFile(String key){
		super.s3Manager.deleteObjects(key);
	}
	@Override
	public InputStream downloadFile(String key){
		return super.s3Manager.getStream(key);
	}
	
}
