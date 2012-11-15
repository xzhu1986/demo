package au.com.isell.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author frankw 21/02/2012
 */
public class UrlUtils {
	public static Map getRequestMap(HttpServletRequest request) {
		Map<String, String[]> map = request.getParameterMap();
		Map<String, String> r = new HashMap<String, String>();
		for (String key : map.keySet()) {
			r.put(key, map.get(key)[0]);
		}
		return r;
	}

	public static String joinParam4Url(Map<String, String> params, String... excludes) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (excludes != null && Arrays.binarySearch(excludes, entry.getKey()) > -1)
				continue;
			if (builder.length() > 0)
				builder.append("&");
			builder.append(entry.getKey()).append("=").append(entry.getValue());
		}
		return builder.toString();
	}

	public static String encode(String v) {
		try {
			return URLEncoder.encode(v, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String decode(String v) {
		try {
			return URLDecoder.decode(v, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
