package au.com.isell.rlm.module.upload.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.io.IOUtils;
import au.com.isell.common.util.ContentType;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.upload.dao.FileDao;
import au.com.isell.rlm.module.upload.service.FileService;
import au.com.isell.rlm.module.upload.vo.s3file.PolicyCondition;
import au.com.isell.rlm.module.upload.vo.s3file.PolicyCondition.Compare;
import au.com.isell.rlm.module.upload.vo.s3file.S3FileBean;
import au.com.isell.rlm.module.upload.vo.s3file.S3FilePolicy;
import au.com.isell.rlm.module.user.service.UserService;

@Service
public class FileServiceImpl implements FileService {
	private static final String SUCCESS_REDIRECT = "http://localhost/cloud-rlm-web/upload/info";
	private static final String KEY = "user/upload/";
	private static String uploadBucket = S3Manager.getBucket();

	@Autowired
	private UserService userService;
	@Autowired
	private FileDao fileDao;

	@Override
	public S3FileBean constructFileBean(ContentType contentType, String callbackUrl) {
		Pair<String, String> keyPair = userService.getAccessAndSecretKeys();
		S3FilePolicy policy = new S3FilePolicy(DateUtils.addMinutes(new Date(), 30));
		policy.withCondition(new PolicyCondition(PolicyCondition.NAME_BUCKET, uploadBucket));
		policy.withCondition(new PolicyCondition(PolicyCondition.NAME_KEY, KEY, Compare.STARTS_WITH));
		policy.withCondition(new PolicyCondition(PolicyCondition.NAME_ACL, PolicyCondition.VALUE_AUTHENTICATED_READ));
		if (callbackUrl != null)
			policy.withCondition(new PolicyCondition(PolicyCondition.NAME_SUCCESS_ACTION_REDIRECT, callbackUrl));
		if (contentType != null) {// image must set this if want to display with <img src=.../>
			policy.withCondition(new PolicyCondition(PolicyCondition.NAME_CONTENT_TYPE, contentType.getValue(), Compare.EQ));
		}
		return new S3FileBean(keyPair.getKey(), keyPair.getValue(), policy);
	}

	@Override
	public String getFileUrl(String key) {
		Assert.hasText(key, "key should not be null");
		return fileDao.getCloudFileUrl(key);
	}

	@Override
	public void deleteFile(String key) {
		Assert.hasText(key, "key should not be null");
		fileDao.deleteFile(key);
	}

	@Override
	public File cacheFile(String key) {
		InputStream in= null;
		OutputStream out=null;
		File f=null;
		try {
			in=fileDao.downloadFile(key);
			f=File.createTempFile("cloudFileCache", ".temp");
			out=new FileOutputStream(f);
			IOUtils.transfer(in, out, 4096);
			fileDao.downloadFile(key);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}finally{
			try {
				in.close();
				out.close();
			} catch (IOException e) {
			}
		}
		return f;
	}
	
}
