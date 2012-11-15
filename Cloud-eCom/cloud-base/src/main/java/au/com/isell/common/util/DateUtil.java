package au.com.isell.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * This is a common util class for date related operations.
 */
public class DateUtil {
    public static String[] getWeekHeaders() {
        return new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
    }

    /**
     * Store time period
     * @author yezhou
     */
    public static class TimePeriod {
    	private long period;

    	/**
    	 * Create in milliseconds
    	 * @param period
    	 */
		public TimePeriod(long period) {
    		this.period = period;
    	}

		public static TimePeriod parseBySeconds(long seconds) {
			return new TimePeriod(seconds * 1000);
		}
		
		public static TimePeriod parseByMinutes(long miniutes) {
			return new TimePeriod(miniutes * 1000 * 60);
		}
		
		public static TimePeriod parseByHours(long hours) {
			return new TimePeriod(hours * 1000 * 60 * 60);
		}
		
		public static TimePeriod parseByDays(long days) {
			return new TimePeriod(days * 1000 * 60 * 60 * 24);
		}
		
		public static TimePeriod parseByDates(Date start, Date end) {
			return new TimePeriod(end.getTime()-start.getTime());
		}
		
		public long getInDays() {
			return period / (1000 * 60 * 60 * 24);
		}
		
		public long getInHours() {
			return period / (1000 * 60 * 60);
		}
		
		public long getInMinutes() {
			return period / (1000 * 60);
		}
		
		public long getInSeconds() {
			return period / 1000;
		}
		
		public long getInMilliseconds() {
			return period;
		}
    	
    	@Override
		public String toString() {
            long ms = period % 1000;
    		long s = period / 1000;
            long m = s / 60;
            s = s % 60;
            long h = m / 60;
            m = m % 60;

            String strH = (h < 10 ? "0" : "") + h;
            String strM = (m < 10 ? "0" : "")+m;
            String strS = (s < 10 ? "0" : "")+s;
            String strMS = "00"+ms;

            String time = strH + ":" + 
            	strM + ":"+ strS + "." + strMS.substring(strMS.length() - 3);
            return time;
    	}
    }
    
    private static final Map<String, SimpleDateFormat> dateFormats = new Hashtable<String, SimpleDateFormat>();
    
    /**
     * Format date by given pattern. The pattern uses same format with java.text.SimpleDateFormat.
     * 
     * @param pattern
     * @param date
     * @return the formatted date
     */
    public static String formatDate(String pattern, Date date) {
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
    public static Date parseDate(String pattern, String date) throws ParseException {
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

	public static final String FORMAT_TRANSFER_DETAIL="yyyyMMddHHmmss";
	public static final String DF_SHORT = "dd/MM/yyyy";
	public static final String DF_LONG = "dd/MM/yyyy HH:mm:ss";
	
}