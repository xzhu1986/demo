package au.com.isell.rlm.module.upload.dao;

import java.io.InputStream;

public interface FileDao {

	String getCloudFileUrl(String key);

	void deleteFile(String key);

	InputStream downloadFile(String key);

}
