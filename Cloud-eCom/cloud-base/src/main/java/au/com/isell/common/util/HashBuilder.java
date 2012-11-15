package au.com.isell.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * generate all kinds of hash values,support multithreading invoking
 * 
 * @author frankw 16/02/2011
 */
public enum HashBuilder {
	MD5("MD5"), SHA1("SHA1"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512");
	
	private String hashType;

	private HashBuilder(String hashType) {
		this.hashType = hashType;
	}

	public byte[] getFileHashBytes(File file) {
		if (!file.exists() || file.length() == 0l) {
			return new byte[0];
		}
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return getStreamHashBytes(in);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * This will not close stream internal. Users need to close stream by themselves.
	 */
	public byte[] getStreamHashBytes(InputStream data) {
		MessageDigest messageDigest = getMessageDigest();
		DigestInputStream in = null;
		in = new DigestInputStream(data, messageDigest);
		in.on(true);
		byte[] buf = new byte[2048];
		try {
			for (int i = in.read(buf); i >= 0; i = in.read(buf))
				;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return messageDigest.digest();
	}

	public byte[] getStringHashBytes(String source) {
		MessageDigest messageDigest = getMessageDigest();
		messageDigest.update(source.getBytes());
		return messageDigest.digest();
	}

	public String getStreamHash(InputStream data){
		return convertToHex(getStreamHashBytes(data));
	}

	public String getFileHash(File file)  {
		return convertToHex(getFileHashBytes(file));
	}

	public String getStringHash(String data){
		return convertToHex(getStringHashBytes(data));
	}

	private MessageDigest getMessageDigest() {
		try {
			return  MessageDigest.getInstance(hashType);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

}
