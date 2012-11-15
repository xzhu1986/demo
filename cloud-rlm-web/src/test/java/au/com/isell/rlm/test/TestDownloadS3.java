package au.com.isell.rlm.test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import au.com.isell.common.io.IOUtils;
import au.com.isell.common.util.NetworkUtils;
import au.com.isell.common.util.SecurityUtils;

public class TestDownloadS3 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String resource="/isell.resource.test/data/test_supplier_1/opera+house.jpg";
		long expire = System.currentTimeMillis() / 1000 + 60;
		String signString = "GET\n\n\n"+expire+"\n"+resource;
		String accessKey = "AKIAI5HCZMWKQJLZUDFA";
		String secureKey = "vGkI+Z52UokJiZWwMSvBtBYiH0FT31oN6rHFke7B";
		String signature = SecurityUtils.digestHMACSHA1(signString, secureKey);
		Properties prop = new Properties();
		prop.setProperty("AWSAccessKeyId", accessKey);
		prop.setProperty("Expires", String.valueOf(expire));
		prop.setProperty("Signature", signature);
		String url = "https://s3.amazonaws.com"+resource;
		InputStream in = NetworkUtils.makeHTTPGetRequest(url, prop, null);
		IOUtils.transfer(in, new FileOutputStream("/Users/yezhou/test.jpg"), 4096);
	}
}