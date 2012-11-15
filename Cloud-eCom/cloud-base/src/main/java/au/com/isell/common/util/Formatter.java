package au.com.isell.common.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * supported formats : ${name},{0},{name}
 * 
 * @author frankw 17/04/2012
 */
public enum Formatter {
	/**
	 * format like : ${name}
	 */
	DollarBraceName("\\$\\{(.+?)\\}") {
		@Override
		public String format(String text, Map<String, String> replacements) {
			return super.formatWithParams(text, replacements);
		}

	},
	/**
	 * format like : {0}
	 */
	BraceNumber("\\{(\\d+?)\\}") {
		@Override
		public String format(String text, String... replacements) {
			return super.formatWithParams(text, replacements);
		}

	},
	/**
	 * format like : {name}
	 */
	BraceName("\\{(.+?)\\}") {
		@Override
		public String format(String text, Map<String, String> replacements) {
			return super.formatWithParams(text, replacements);
		}

	};

	protected Pattern pattern;

	private Formatter(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	public String format(String text, String... replacements) {
		throw new RuntimeException("not supported operation");
	}

	public String format(String text, Map<String, String> replacements) {
		throw new RuntimeException("not supported operation");
	}

	protected <T> String formatWithParams(String text, T replacements) {
		Matcher matcher = pattern.matcher(text);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String replacement = getParam(replacements, matcher);
			if (replacement != null) {
				matcher.appendReplacement(buffer, "");
				buffer.append(replacement);
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private <T> String getParam(T replacements, Matcher matcher) {
		String replacement = null;
		String key = matcher.group(1);
		if (replacements instanceof Map) {
			Map pMap = (Map) replacements;
			replacement = (String) ((Map) replacements).get(key);
			// return "" if null and conatains this key
			replacement = replacement == null && pMap.containsKey(key) ? "" : replacement;
		} else if (replacements instanceof String[]) {
			replacement = ((String[]) replacements)[Integer.valueOf(key)];
			// return "" if null and conatains this index
			replacement = replacement == null ? "" : replacement;
		}
		return replacement;
	}

	public boolean isFormattedStr(String text) {
		return StringUtils.isNotBlank(text) && pattern.matcher(text).find();
	}

}
