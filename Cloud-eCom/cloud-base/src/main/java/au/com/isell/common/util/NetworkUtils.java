package au.com.isell.common.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtils {
	private static Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
	private static Proxy proxy;
	private static String userName;
	private static String password;
	private static int bufferSize = 40960;
	private static String proxyType;
	private static String proxyServer;
	private static int proxyPort;
	private static List<String> localIP;

	public static InputStream makeHTTPPostRequest(String urlStr, Properties params, Properties requestProperties) throws IOException {
		String reqStr = generateRequestStr(params);
		return makeHTTPPostRequest(urlStr, reqStr, requestProperties);
	}

	public static InputStream makeHTTPPostRequest(String urlStr, String reqStr,
			Properties requestProperties) throws MalformedURLException,
			IOException, ProtocolException {
		URL url = null;
		OutputStream writer = null;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlStr);
			logger.info("Begin to request(post) : {}", urlStr);
			conn = (HttpURLConnection) connect(url);
			if (requestProperties != null) {
				for (Map.Entry<Object, Object> entry : requestProperties.entrySet()) {
					conn.setRequestProperty(entry.getKey().toString(), entry.getValue().toString());
				}
			}
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setAllowUserInteraction(false);
			if (requestProperties == null || requestProperties.get("Content-Type") == null) {
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			}
			writer = conn.getOutputStream();
			writer.write(reqStr.getBytes());
			writer.flush();
			writer.close();
			writer = null;
			int responseStatus = conn.getResponseCode();
			if (responseStatus >= 200 && responseStatus < 400) {// response
																// success
				return conn.getInputStream();
			} else {// response failed
				throw new IOException(conn.getResponseMessage());
			}
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static InputStream makeHTTPGetRequest(String urlStr, Properties params, Properties requestProperties) throws IOException {
		urlStr = generateGetUrl(urlStr, params);
		URL url = null;
		OutputStream os = null;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlStr);
			logger.info("Begin to request(get) : {}", urlStr);
			conn = (HttpURLConnection) connect(url);
			if (requestProperties != null) {
				for (Map.Entry<Object, Object> entry : requestProperties.entrySet()) {
					conn.setRequestProperty(entry.getKey().toString(), entry.getValue().toString());
				}
			}
			int responseStatus = conn.getResponseCode();
			if (responseStatus >= 200 && responseStatus < 400) {// response
																// success
				return conn.getInputStream();
			} else {// response failed
				throw new IOException("(" + responseStatus + "): " + conn.getResponseMessage());
			}
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String generateGetUrl(String urlStr, Properties params) throws UnsupportedEncodingException {
		String reqStr = generateRequestStr(params);
		if (reqStr.length() > 0) {
			if (urlStr.contains("?")) {
				urlStr = urlStr + '&' + reqStr;
			} else {
				urlStr = urlStr + '?' + reqStr;
			}
		}
		return urlStr;
	}

	private static String generateRequestStr(Properties params) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		boolean start = true;
		if (params == null)
			params = new Properties();
		for (Map.Entry<Object, Object> entry : params.entrySet()) {
			if (start)
				start = false;
			else
				sb.append('&');
			sb.append(URLEncoder.encode(entry.getKey().toString(), "UTF-8")).append('=')
					.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
		}
		return sb.toString();
	}

	private static URLConnection connect(URL url) throws IOException {
		URLConnection conn = null;
		if (url == null)
			return null;
		if (url.getHost() == null)
			return null;
		if (proxy == null || isLocalIP(url.getHost())) {
			conn = url.openConnection();
		} else {
			if (userName != null && password != null) {
				Authenticator.setDefault(new ProxyAuthenticator(userName, password));
			}
			conn = url.openConnection(proxy);
		}
		return conn;
	}

	private static class ProxyAuthenticator extends Authenticator {
		private String userName;
		private String password;

		public ProxyAuthenticator(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(userName, password.toCharArray());
		}
	}

	public static void setProxy(String proxyType, String proxyServer, String proxyPort, String userName, String password) {
		NetworkUtils.proxyType = proxyType;
		NetworkUtils.proxyServer = proxyServer;
		NetworkUtils.proxyPort = Integer.parseInt(proxyPort);
		NetworkUtils.userName = userName;
		NetworkUtils.password = password;
		Proxy.Type type = Proxy.Type.HTTP;
		if ("http".equals(proxyType))
			type = Proxy.Type.HTTP;
		else if ("socket".equals(proxyType))
			type = Proxy.Type.SOCKS;
		else if ("direct".equals(proxyType))
			type = Proxy.Type.DIRECT;
		proxy = new Proxy(type, new InetSocketAddress(proxyServer, NetworkUtils.proxyPort));
	}

	public static void downloadFile(String fileUrl, String localPath, Properties queryParams) throws IOException {
		downloadFile(fileUrl, localPath, queryParams, false);
	}

	public static void downloadFile(String fileUrl, String localPath, Properties queryParams, boolean useBreakPoint) throws IOException {
		File downloadToFile = new File(localPath);
		// clean
		if (downloadToFile.exists() && !useBreakPoint) {
			if (!downloadToFile.delete()) {
				logger.warn("Can not delete file {},try overrid !!!", downloadToFile);
			}
		}
		// retry policy
		// getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
		// new DefaultHttpMethodRetryHandler());
		for (int i = 0; i < 5; i++) {
			try {
				// set break point range
				Properties header = setReqHeader(useBreakPoint, downloadToFile);
				// download
				download(fileUrl, localPath, queryParams, header);
				break;
			} catch (IOException ex) {
				if (i < 4) {
					logger.warn("Download file: " + fileUrl + " failed. retry again", ex);
				} else {
					logger.error("Download file: " + fileUrl + " failed. ", ex);
				}
			}
		}
	}

	private static Properties setReqHeader(boolean useBreakPoint, File downloadToFile) {
		if (useBreakPoint && downloadToFile.exists()) {
			Properties prop = new Properties();
			long existFileLength = downloadToFile.length();
			prop.setProperty("RANGE", "bytes=" + existFileLength + "-");
			return prop;
		} else
			return null;
	}

	private static void download(String fileUrl, String localPath, Properties params, Properties header) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = makeHTTPGetRequest(fileUrl, params, header);
			out = new BufferedOutputStream(new FileOutputStream(localPath, true));
			IOUtils.transfer(in, out, bufferSize);
		} finally {
			try {
				in.close();
				out.close();
			} catch (Exception e) {
			}
		}
	}

	private static boolean isLocalIP(String host) {
		if (host.contains("127.0.0.1") || host.toLowerCase().contains("localhost"))
			return true;
		for (String local : localIP) {
			if (host.contains(local))
				return true;
		}
		return false;
	}

	public static void upload(String url, String fieldName, File uploadFile, Properties appendFields) throws Exception {

		HttpClient httpclient = new DefaultHttpClient();
		try {
			// enableHttps(httpclient);
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
			if (respEntity != null) {
				System.out.println(EntityUtils.toString(respEntity));
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}
	

	private static void enableHttps(HttpClient httpClient) throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager easyTrustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
					throws java.security.cert.CertificateException {
				// To change body of implemented methods use File | Settings
				// | File Templates.
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
					throws java.security.cert.CertificateException {
				// To change body of implemented methods use File | Settings
				// | File Templates.
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// To change body of implemented methods use File | Settings
				// | File Templates.
				return new java.security.cert.X509Certificate[0];
			}
		};

		SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);
		SSLSocketFactory sf = new SSLSocketFactory(sslcontext);

		Scheme https = new Scheme("https", 443, sf);

		httpClient.getConnectionManager().getSchemeRegistry().register(https);
	}
}
