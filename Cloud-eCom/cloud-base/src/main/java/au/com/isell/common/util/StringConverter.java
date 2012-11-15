package au.com.isell.common.util;

import java.util.List;

/**
 * @author frankw 16/05/2012
 */
public class StringConverter {
	public static String join(String seperator, String... arr) {
		if (arr == null)
			return "";
		StringBuilder builder = new StringBuilder();
		for (String s : arr) {
			if (builder.length() > 0)
				builder.append(seperator);
			builder.append(s);
		}
		return builder.toString();
	}

	public static String join(String seperator, List<String> arr) {
		if (arr == null)
			return "";
		StringBuilder builder = new StringBuilder();
		for (String s : arr) {
			if (builder.length() > 0)
				builder.append(seperator);
			builder.append(s);
		}
		return builder.toString();
	}
}
