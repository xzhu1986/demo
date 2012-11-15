package au.com.isell.rlm.module.report.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import au.com.isell.common.io.IOUtils;

/**
 * @author frankw 16/04/2012
 */
public class ReportTemplateTransferUtils {

	public static void request(String url, Properties appendFields,OutputStream out) {
		HttpClient httpclient = new DefaultHttpClient();
		try {
			// enableHttps(httpclient);
			HttpEntity respEntity = sendPostRequests(url, null, null, appendFields, httpclient);
			InputStream in = respEntity.getContent();
			IOUtils.transfer(in, out, 4094);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception e) {
			}
		}
	}

	public static void upload(String url, String fieldName,File file, Properties appendFields) {
		HttpClient httpclient = new DefaultHttpClient();
		try {
			// enableHttps(httpclient);
			HttpEntity respEntity = sendPostRequests(url, fieldName, file, appendFields, httpclient);
			if (respEntity != null) {
				System.out.println(EntityUtils.toString(respEntity));
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}
	
	public static File createTempFile(String prefix,String suffix) {
		try {
			return File.createTempFile(prefix, suffix);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private static HttpEntity sendPostRequests(String url, String fieldName, File uploadFile, Properties appendFields, HttpClient httpclient)
			throws UnsupportedEncodingException, IOException, ClientProtocolException {
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		HttpPost httppost = new HttpPost(url);

		MultipartEntity entity = new MultipartEntity();
		// add params as fields
		if (appendFields != null) {
			for (Map.Entry<Object, Object> entry : appendFields.entrySet()) {
				entity.addPart(entry.getKey().toString(), new StringBody(entry.getValue().toString(), "text/plain", Charset.forName("UTF-8")));
			}
		}
		// new FileBody(file, "image/jpeg");
		if (StringUtils.isNotEmpty(fieldName) && uploadFile != null)
			entity.addPart(fieldName, new FileBody(uploadFile));

		httppost.setEntity(entity);
		System.out.println("executing request " + httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity respEntity = response.getEntity();

		System.out.println(response.getStatusLine());
		return respEntity;
	}

}
