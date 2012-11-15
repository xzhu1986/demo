package au.com.isell.common.util;

import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

public class TextFormat {
	
	private static final String PRE_FLAG = "${";
	
	public static String format(String pattern, Map<String, Object> variables) {
		StringTokenizer tokenizer = new StringTokenizer(pattern, "$}");
		StringBuilder sb = new StringBuilder();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("{")) {
				String key = token.substring(1);
				Object value = variables.get(key);
				sb.append(value == null ? PRE_FLAG+key+"}" : toString(value));
			} else {
				sb.append(token);
			}
		}
		return sb.toString();
	}
	
	private static String toString(Object value) {
		if (value instanceof Date) {
			return String.valueOf(((Date) value).getTime()/1000);
		}
		return value.toString();
	}

	public static boolean isFormatedStr(String str) {
		return StringUtils.isNotBlank(str) && str.indexOf(PRE_FLAG)>-1;
	}
}
