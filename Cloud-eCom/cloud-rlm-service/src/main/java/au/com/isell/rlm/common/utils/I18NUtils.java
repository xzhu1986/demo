package au.com.isell.rlm.common.utils;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
public class I18NUtils {
	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/message");

	public static String getMsg(String msgCode, String... params) {
		String fmt = resourceBundle.getString(msgCode);
		if (fmt == null) {
			throw new RuntimeException(String.format("Message code [%s] is not exists", msgCode));
		}
		if (params != null && params.length > 0) {
			return MessageFormat.format(fmt, params);
		}
		return fmt;
	}
	
	public static boolean exists(String msgCode){
		try {
			return resourceBundle.getString(msgCode)!=null? true:false;
		} catch (MissingResourceException e) {
			return false;
		}
	}
	
}
