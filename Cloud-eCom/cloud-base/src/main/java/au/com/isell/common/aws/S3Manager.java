package au.com.isell.common.aws;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.isell.common.io.IOUtils;
import au.com.isell.common.util.NetworkUtils;
import au.com.isell.common.util.SecurityUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * When you save some data into s3 it may takes couple of seconds to be able to get. So when design logic please don't make save then read immediately operations.
 * @author yezhou
 *
 */
public class S3Manager {
	public static final String META_CONTENTTYPE = "content-type";
	public static final String META_CONTENTENCODING = "Content-Encoding";
	
	public static final String META_LASTMODIFIED = "meta.lastModified";
	public static final String META_CHECKSUM = "meta.checksum";

	private static Logger logger = LoggerFactory.getLogger(S3Manager.class);

	private AmazonS3 s3;
	private static String data_bucket="isell.test";
	
	private static S3Manager instance;
	private static boolean inited = false;
	private static String accessKey;
	private static String secretKey;
	
	private synchronized static void init() {
		if (inited) return;
		try {
			Properties bundle = new Properties();
			bundle.load(S3Manager.class.getResourceAsStream("/settings.properties"));
			data_bucket = bundle.getProperty("s3.data.bucket");
		} catch (IOException ex) {
			throw new RuntimeException("Load config file failed", ex);
		}
		try {
			Properties bundle = new Properties();
			bundle.load(S3Manager.class.getResourceAsStream("/AwsCredentials.properties"));
			accessKey = bundle.getProperty("accessKey");
			secretKey = bundle.getProperty("secretKey");
		} catch (IOException ex) {
			throw new RuntimeException("Load config file failed", ex);
		}
	}

	private S3Manager() throws IOException {
		s3 = new AmazonS3Client(new PropertiesCredentials(
				S3Manager.class.getResourceAsStream("/AwsCredentials.properties")));
	}
	
	public static synchronized S3Manager getInstance() {
		if (!inited) init();
		if (instance == null) {
			try {
				instance = new S3Manager();
			} catch (IOException e) {
			}
			instance.tryCreateBucket(data_bucket);
		}
		return instance;
	}
	
	public static String getBucket() {
		if (!inited) init();
		return data_bucket;
	}

	/**
	 * @see com.amazonaws.services.s3.AmazonS3.getObject(String, String)
	 * @param key
	 * @return
	 */
	public InputStream getStream(String key) {
		S3Object object = getS3Object(key);
		if (object == null) return null;
		return object.getObjectContent();
	}

	public String getString(String key) {
		InputStream in = getStream(key);
		if (in == null) return null;
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		try {
			boolean begin = true;
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				if (begin) begin = false;
				else sb.append('\n');
				sb.append(line);
			}
		} catch (IOException e) {
		}
		return sb.toString();
	}
	
	public S3Object getS3Object(String key) {
		try {
			return s3.getObject(new GetObjectRequest(data_bucket, key));
		} catch (AmazonClientException e) {
			if (e.getMessage().contains("The specified key does not exist")) {
				logger.warn("Key "+ key + ": " + e.getMessage());
				return null;
			} else {
				throw new RuntimeException(e);
			}
		}
	}
	
	public Map<String, Object> getMeta(String key) {
		try {
			ObjectMetadata meta = s3.getObjectMetadata(data_bucket, key);
			Map<String, Object> metaData = new HashMap<String, Object>();
			metaData.putAll(meta.getUserMetadata());
			metaData.put(META_LASTMODIFIED, meta.getLastModified());
			metaData.put(META_CHECKSUM, meta.getETag());
			return metaData;
		} catch (AmazonClientException e) {
			if (e.getMessage().contains("The specified key does not exist")) {
				logger.warn("Key "+ key + ": " + e.getMessage(), e);
				return null;
			} else {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void setMeta(String key, Map<String, String> meta) {
		CopyObjectRequest req = new CopyObjectRequest(data_bucket, key, data_bucket, key);
		ObjectMetadata metaData = null;
		try {
			metaData = s3.getObjectMetadata(data_bucket, key);
		} catch (AmazonClientException e) {
			if (e.getMessage().contains("The specified key does not exist")) {
				logger.warn("Key "+ key + ": " + e.getMessage(), e);
				return;
			} else {
				throw new RuntimeException(e);
			}
		}
		for (Map.Entry<String, String> entry:meta.entrySet()) {
			metaData.addUserMetadata(entry.getKey(), entry.getValue());
		}
		req.setNewObjectMetadata(metaData);
		s3.copyObject(req);
	}
	
	public File getFile(String key) throws IOException {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("s3"+UUID.randomUUID(), ".tmp");
			InputStream in = getStream(key);
			IOUtils.transfer(in, new FileOutputStream(tempFile), 4096);
			return tempFile;
		} catch (IOException ex) {
			if (tempFile != null) {
				tempFile.delete();
			}
			throw ex;
		} catch (Throwable ex) {
			if (tempFile != null) {
				tempFile.delete();
			}
			throw new IOException(ex);
		}
	}

	public void putS3Object(String key, S3Object obj) {
		s3.putObject(data_bucket, key, obj.getObjectContent(), obj.getObjectMetadata());
	}
	
	public void putInputStream(String key, InputStream in, Map<String, String> meta, Date expireDate, Long length) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setUserMetadata(meta);
		if (expireDate != null) {
			metadata.setExpirationTime(expireDate);
		}
		if (length != null) {
			metadata.setContentLength(length);
		}
		metadata.setServerSideEncryption("AES256");
		s3.putObject(data_bucket, key, in, metadata);
	}

	public void uploadFile1(String key, File file,  Map<String, String> meta,Date expireDate) {
		if (!file.exists()) return;
		ObjectMetadata metadata = new ObjectMetadata();
		setContentType(meta, metadata);
		if (expireDate != null) {
			metadata.setExpirationTime(expireDate);
		}
		metadata.setServerSideEncryption("AES256");
		metadata.setContentLength(file.length());
		metadata.setUserMetadata(meta);
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			s3.putObject(data_bucket, key, in, metadata);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {}
			}
		}
	}

	private void setContentType(Map<String, String> meta, ObjectMetadata metadata) {
		if(meta.get(META_CONTENTTYPE)!=null){
			metadata.setContentType(meta.get(META_CONTENTTYPE));
			meta.remove(META_CONTENTTYPE);
		}
	}

	public void putObject(String key, String content, Map<String, String> meta, Date expireDate) {
		s3.deleteObject(data_bucket, key);
		byte[] bytes = content.getBytes();
		putObject(key, bytes, meta, expireDate);
	}
	
	public void putObject(String key, byte[] content, Map<String, String> meta, Date expireDate) {
		s3.deleteObject(data_bucket, key);
		ByteArrayInputStream in = new ByteArrayInputStream(content);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(content.length);
		if (meta != null) {
			String contentType = meta.remove(S3Manager.META_CONTENTTYPE);
			if (contentType != null) {
				metadata.setContentType(contentType);
			}
			String encoding = meta.remove(S3Manager.META_CONTENTENCODING);
			if (encoding != null) {
				metadata.setContentEncoding(encoding);
			}
			for (Map.Entry<String, String> entry : meta.entrySet()) {
				metadata.addUserMetadata(entry.getKey(), entry.getValue());
			}
		}
		if (expireDate != null) {
			metadata.setExpirationTime(expireDate);
		}
		metadata.setServerSideEncryption("AES256");
		s3.putObject(new PutObjectRequest(data_bucket, key, in, metadata));
	}

	public ObjectListing listFolder(String folder, String pageMarker,String delimiter) {
		ObjectListing listing = s3.listObjects(new ListObjectsRequest(data_bucket, folder, pageMarker, delimiter, 1000));
		return listing;
	}
	
	private void tryCreateBucket(String bucketName) {
		for (Bucket bucket : s3.listBuckets()) {
			if (bucket.getName().equals(bucketName)) return;
		}
		s3.createBucket(bucketName);
	}

	public void deleteObjects(String... keys) {
		for (String key : keys) {
			if (key != null) s3.deleteObject(data_bucket, key);
		}
	}
	
	public void copyObject(String source, String target) {
		s3.copyObject(data_bucket, source, data_bucket, target);
	}
	
	public void moveObject(String source, String target) {
		s3.copyObject(data_bucket, source, data_bucket, target);
		s3.deleteObject(data_bucket, source);
	}
	
	public String generateS3Url(String key, int timeoutSeconds) {
		try {
			String resource = "/" + data_bucket + "/" + URLEncoder.encode(key, "UTF-8").replace("%2F", "/").replace("+", " ");
			long expire = System.currentTimeMillis() / 1000 + timeoutSeconds;
			String signString = "GET\n\n\n" + expire + "\n" + resource;
			String signature = SecurityUtils.digestHMACSHA1(signString, secretKey);
			Properties prop = new Properties();
			prop.setProperty("AWSAccessKeyId", accessKey);
			prop.setProperty("Expires", String.valueOf(expire));
			prop.setProperty("Signature", signature);
			String url = "https://s3.amazonaws.com" + resource;
			return NetworkUtils.generateGetUrl(url, prop);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String getAccessKey() {
		return accessKey;
	}

	public static String getSecretKey() {
		return secretKey;
	}
	
	
}
