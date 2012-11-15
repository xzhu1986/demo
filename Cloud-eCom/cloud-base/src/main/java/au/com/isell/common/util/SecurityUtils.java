package au.com.isell.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Encoder;

public class SecurityUtils {
	
	private static final BASE64Encoder encoder = new BASE64Encoder();
	
	public static final int DIGEST_LENGTH = SecurityUtils.digest("1").length();

	public static String digest(String source) {
		byte[] bytes = HashBuilder.SHA256.getStringHashBytes(source);
		return encoder.encode(bytes);
	}
	
	public static String digestPassword(String token, String password) {
		return digest(salt(token, password));
	}
	
	public static String salt(String token, String password) {
		String newPasswd = token+password;
		return newPasswd+newPasswd.length();
	}

	private static final String UTF8 = "UTF-8";
	private static final String HMACSHA1 = "HmacSHA1";

	public static String digestHMACSHA1(String source, String key)
			throws Exception {
		byte[] dataBytes = source.getBytes(UTF8);
	    byte[] secretBytes = key.getBytes(UTF8);
		SecretKeySpec signingKey = new SecretKeySpec(secretBytes, HMACSHA1);

	    Mac mac = Mac.getInstance(HMACSHA1);
	    mac.init(signingKey);
	    byte[] signature = mac.doFinal(dataBytes);

	    return new String(Base64.encodeBase64(signature, false), "UTF-8");

	}
}