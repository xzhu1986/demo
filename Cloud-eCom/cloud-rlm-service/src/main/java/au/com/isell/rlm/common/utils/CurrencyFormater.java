package au.com.isell.rlm.common.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author frankw 27/04/2012
 */
public class CurrencyFormater {
	public static String format(Number number){
		Locale locale = GlobalAttrManager.getClientInfo().getLocale();
		NumberFormat numberFormat=NumberFormat.getNumberInstance(locale);
		return numberFormat.format(number);
	}
	
}
