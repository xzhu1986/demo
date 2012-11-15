package au.com.isell.common.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {

	public static List<String> array2List(String[] strs) {
		List<String> result = new ArrayList<String>(0);
		try {
			for (int i = 0; i < strs.length; i++) {
				result.add(strs[i]);
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static String[] string2Array(String str, String seperator) {
		String[] result = new String[0];
		try {
			if (!str.trim().equalsIgnoreCase("")) {
				result = str.split(seperator);
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static String array2String(String[] str, String seperator) {
		String result = "";
		if (str == null || str.length == 0) {
			return result;
		}
		boolean isFirst = true;
		for (int i = 0; i < str.length; i++) {
			if (isFirst) {
				isFirst = false;
			} else {
				result += seperator == null ? "" : seperator;
			}
			result += str[i] == null ? "" : str[i];

		}
		return result;
	}

	public static String array2String(List<?> str, String seperator) {
		String result = "";
		if (str == null || str.size() == 0) {
			return result;
		}
		boolean isFirst = true;
		for (int i = 0; i < str.size(); i++) {
			if (isFirst) {
				isFirst = false;
			} else {
				result += seperator == null ? "" : seperator;
			}
			result += str.get(i) == null ? "" : str.get(i);

		}
		return result;
	}

	public static String replaceFirst(String orignalString, String regex, String replacement) {
		String result = orignalString;
		try {
			result = orignalString.replaceFirst(regex, replacement);
		} catch (Exception e) {
		}
		return result;
	}

	public static String object2String(Object o) {
		if (o == null) {
			return null;
		}
		return o.toString();
	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static String addValueToString(String orignal, String newValue, String seperator) {
		String result = orignal;
		String[] array = string2Array(orignal, seperator);
		array = addValueToArray(array, newValue);
		result = array2String(array, seperator);
		return result;
	}

	public static String removeValueFromString(String orignal, String removeValue, String seperator) {
		String result = orignal;
		String[] array = string2Array(orignal, seperator);
		array = removeValueFromArray(array, removeValue);
		result = array2String(array, seperator);
		return result;
	}

	public static String[] addValueToArray(String[] orignal, String newValue) {
		String[] result = orignal;
		if (exists(orignal, newValue)) {
			return result;
		}
		List<String> list = array2List(result);
		list.add(newValue);
		result = list2Array(list);
		return result;
	}

	public static boolean exists(String[] strs, String str) {
		boolean exists = false;
		for (int i = 0; strs != null && i < strs.length; i++) {
			if (equalsIgnoreCase(strs[i], str)) {
				exists = true;
				break;
			}
		}
		return exists;

	}

	public static boolean exists(List<?> strs, String str) {
		boolean exists = false;
		for (int i = 0; strs != null && i < strs.size(); i++) {
			if (equalsIgnoreCase((String) strs.get(i), str)) {
				exists = true;
				break;
			}
		}
		return exists;

	}

	public static boolean include(String str, String substr, String seperator) {
		String[] strs = string2Array(str, seperator);
		return exists(strs, substr);
	}

	public static String[] removeValueFromArray(String[] orignal, String oldValue) {
		String[] result = orignal;
		List<String> list = array2List(result);
		int i = list.size() - 1;
		while (i >= 0) {
			String item = list.get(i);
			if (equalsIgnoreCase(item, oldValue)) {
				list.remove(i);
			}
			i--;
		}
		result = list2Array(list);
		return result;
	}

	public static boolean equalsIgnoreCase(String str1, String str2) {
		boolean result = false;

		if (str1 == null && str2 == null) {
			result = true;
		}
		if (str1 != null && str2 != null && str1.trim().equalsIgnoreCase(str2)) {
			result = true;
		}
		return result;

	}

	public static boolean equalsIgnoreCase(Object str1, Object str2) {
		return equalsIgnoreCase((str1 == null ? null : str1.toString()), (str2 == null ? null : str2.toString()));
	}

	public static String[] list2Array(List<String> list) {
		String[] result = new String[0];
		try {
			result = list.toArray(new String[list.size()]);
		} catch (Exception e) {
		}
		return result;

	}

	public static boolean validFloat(String strValue) {
		boolean result = false;
		if (strValue == null || strValue.trim().equalsIgnoreCase("")) {
			return result;
		}
		try {
			Float.valueOf(strValue);
			result = true;
		} catch (Exception e) {
		}
		return result;
	}

	public static boolean validNumber(String strNumber) {
		if (strNumber == null)
			return false;
		return validFloat(strNumber) && strNumber.indexOf("\\.") == -1;

	}

	public static String UpperCaseFirst(String strValue) {
		if (strValue == null)
			return null;
		char[] strChars = strValue.toCharArray();
		if (strChars != null && strChars.length > 1) {
			char[] newStrChars = new char[strChars.length];
			newStrChars[0] = Character.toUpperCase(strChars[0]);
			for (int i = 1; i < strChars.length; i++) {
				if (!Character.isLetter(strChars[i - 1]) && strChars[i - 1] != "'".charAt(0)) {
					newStrChars[i] = Character.toUpperCase(strChars[i]);
				} else {
					newStrChars[i] = Character.toLowerCase(strChars[i]);
				}
			}
			return String.valueOf(newStrChars);
		}else{
			return strValue;
		}
	}

	public static String StringToTrim(String strValue){
		if (strValue == null)
			return null;
		return strValue.trim();
	}
	
	public static Integer StringToInteger(String strNumber){
		if(validNumber(strNumber)){
			return Integer.parseInt(strNumber.trim());
		}
		return null;
	}
	
	public static BigDecimal StringToBigDecimal(String strNumber){
		if(validNumber(strNumber)){
			return new BigDecimal(strNumber.trim());
		}
		return null;
	}
	
	public static void main(String[] args){
		System.out.println(validNumber("123"));
		System.out.println(validNumber("123.02"));
		System.out.println(validNumber("0.123.02 "));
		System.out.println(validNumber("0.123"));
	}
	
}
