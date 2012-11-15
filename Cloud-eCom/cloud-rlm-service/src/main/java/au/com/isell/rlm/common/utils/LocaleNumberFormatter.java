package au.com.isell.rlm.common.utils;

import java.text.DecimalFormat;
/**
 * @author frankw 11/05/2012
 */
public class LocaleNumberFormatter {
	private static final DecimalFormat DF = new DecimalFormat("###,###");
	
	public synchronized static String format(Object o){
		return DF.format(o);
	}
}
