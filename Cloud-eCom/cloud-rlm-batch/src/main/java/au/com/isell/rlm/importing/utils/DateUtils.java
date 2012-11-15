package au.com.isell.rlm.importing.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.TimeZone;

import au.com.isell.common.util.StringUtils;

public class DateUtils {
	public static final String DF_SHORT = "dd/MM/yyyy";
	public static final String DF_LONG = "dd/MM/yyyy HH:mm:ss";
	
	private static final Map<String, SimpleDateFormat> dateFormats;
	
	static {
		Map<String, SimpleDateFormat> map = new Hashtable<String, SimpleDateFormat>();
		
		SimpleDateFormat DF_SHORT_FORMAT = new SimpleDateFormat(DF_SHORT);
		DF_SHORT_FORMAT.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
		map.put(DF_SHORT,DF_SHORT_FORMAT);
		
		SimpleDateFormat DF_LONG_FORMAT = new SimpleDateFormat(DF_SHORT);
		DF_LONG_FORMAT.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
		map.put(DF_LONG,DF_LONG_FORMAT);
		dateFormats = Collections.unmodifiableMap(map);
	}
	
    public static synchronized String formatDate(String pattern, Date date) {
    	SimpleDateFormat format = dateFormats.get(pattern);
    	synchronized (dateFormats) {
        	if (format == null) {
        		format = new SimpleDateFormat(pattern);
        	}
		}
    	return format.format(date);
    }

    /**
     * Parse a date string with given pattern. The pattern uses same format with java.text.SimpleDateFormat.
     * 
     * @param pattern
     * @param date the formatted date
     * @return The date in java.util.Date Object
     * @throws ParseException
     */
    public static synchronized Date parseDate(String pattern, String date) throws ParseException {
    	if(StringUtils.isEmpty(date)) return null;
    	SimpleDateFormat format = dateFormats.get(pattern);
    	synchronized (dateFormats) {
        	if (format == null) {
        		format = new SimpleDateFormat(pattern);
        	}
		}
    	return format.parse(date);
    }
    
    /**
     * Get a SimpleDateFormat object with given pattern for date formatting. 
     * Compare with formatDate and parseDate method each invoke requires a search operation to find if there is
     * a SimpleDateFormat object in cache. With this method you can get the SimpleDateFormat object before loop 
     * then uses it in the loop which can reduce many operations to search SimpleDateFormat object.
     * 
     * @param pattern the given pattern
     * @return The SimpleDateFormat object with given pattern
     */
    public static SimpleDateFormat getDateFormat(String pattern) {
    	SimpleDateFormat format = dateFormats.get(pattern);
    	synchronized (dateFormats) {
        	if (format == null) {
        		format = new SimpleDateFormat(pattern);
        	}
		}
    	return format;
    }
	
}