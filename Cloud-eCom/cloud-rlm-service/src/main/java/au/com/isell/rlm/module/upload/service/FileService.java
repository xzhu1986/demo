package au.com.isell.rlm.module.upload.service;

import java.io.File;

import au.com.isell.common.util.ContentType;
import au.com.isell.rlm.module.upload.vo.s3file.S3FileBean;

public interface FileService {
	S3FileBean constructFileBean(ContentType contentType,String callbackUrl);

	String getFileUrl(String key);

	void deleteFile(String key);
	
	File cacheFile(String key);
}
