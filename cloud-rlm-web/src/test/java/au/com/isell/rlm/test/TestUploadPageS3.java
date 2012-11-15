package au.com.isell.rlm.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Encoder;
import au.com.isell.common.util.SecurityUtils;
import au.com.isell.common.util.TextFormat;

public class TestUploadPageS3 {
	
	private static String readFile(String filename) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			boolean start = true;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (start) start = false;
				else sb.append('\n');
				sb.append(line);
			}
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception ex) {}
			}
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		PrintWriter writer = null;
		try {
			String policy = readFile("/Users/yezhou/projects/cloud/scripts/test/policy.txt");
			String htmlTemp = readFile("/Users/yezhou/projects/cloud/scripts/test/upload_s3.html");
			String base64 = new String(Base64.encodeBase64(policy.getBytes("UTF-8"), false), "UTF-8");
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("policy", base64);
			values.put("access_key", "AKIAI5HCZMWKQJLZUDFA");
			values.put("signature", SecurityUtils.digestHMACSHA1(base64, "vGkI+Z52UokJiZWwMSvBtBYiH0FT31oN6rHFke7B"));
			String html = TextFormat.format(htmlTemp, values);
			writer = new PrintWriter("/Users/yezhou/projects/cloud/scripts/test/upload_s3_real.html");
			writer.print(html);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception ex) {}
			}
		}
	}
}
