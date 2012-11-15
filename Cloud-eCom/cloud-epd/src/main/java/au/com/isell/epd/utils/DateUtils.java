package au.com.isell.epd.utils;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class DateUtils {
	public static final String SHORT_DATE = "yyyy-MM-dd";
	public static final String SHORT_DATE_ZMS = "yyyyMMdd";
	public static final String LONG_DATE = "yyyy-MM-dd HH:mm:ss";
	public static final SimpleDateFormat DF_SHORT_CN_ZMS = new SimpleDateFormat(SHORT_DATE_ZMS, Locale.US);
	public static final SimpleDateFormat DF_SHORT_CN = new SimpleDateFormat(SHORT_DATE, Locale.US);
	public static final SimpleDateFormat DF_CN = new SimpleDateFormat(LONG_DATE, Locale.US);

	private DateUtils() {
	}

	/**
	 * Calendar -> String
	 */
	public static String format(Calendar cal) {
		return format(cal.getTime());
	}

	/**
	 * Calendar,String -> String
	 */
	public static String format(Calendar cal, String pattern) {
		return format(cal.getTime(), pattern);
	}

	/**
	 * Calendar,DateFormat -> String
	 */
	public static String format(Calendar cal, DateFormat df) {
		return format(cal.getTime(), df);
	}

	/**
	 * Date -> String
	 */
	public static String format(Date date) {
		return format(date, DF_CN);
	}

	/**
	 * Date,String -> String
	 */
	public static String format(Date date, String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		return format(date, df);
	}

	/**
	 * Date,DateFormat -> String
	 */
	public static String format(Date date, DateFormat df) {
		if (date == null)
			return "";

		if (df != null) {
			return df.format(date);
		}
		return DF_CN.format(date);
	}

	/**
	 * String -> Calendar
	 */
	public static Calendar parse(String strDate) {
		return parse(strDate, null);
	}

	/**
	 * String,DateFormate -> Calendar
	 */
	public static Calendar parse(String strDate, DateFormat df) {
		Date date = parseDate(strDate, df);
		if (date == null)
			return null;

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * String -> Date
	 */
	public static Date parseDate(String strDate) {
		return parseDate(strDate, null);
	}

	/**
	 * String,DateFormate -> Date
	 */
	public static Date parseDate(String strDate, DateFormat df) {
		if (df == null)
			df = DF_CN;
		ParsePosition parseposition = new ParsePosition(0);

		return df.parse(strDate, parseposition);
	}

	public static Calendar parseDateString(String str, String format) {
		if (str == null) {
			return null;
		}
		Date date = null;
		SimpleDateFormat df = new SimpleDateFormat(format);
		try {
			date = df.parse(str);
		} catch (Exception ex) {

		}
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * returns the current date in the default format
	 */
	public static String getToday() {
		return format(new Date());
	}

	public static Date getYesterday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);

		return cal.getTime();
	}

	public static Calendar getFirstDayOfMonth() {
		Calendar cal = getNow();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		return cal;
	}

	public static Calendar getNow() {
		return Calendar.getInstance();
	}

	/**
	 * add some month from the date
	 */
	public static Date addMonth(Date date, int n) throws Exception {
		Calendar cal = getNow();
		cal.setTime(date);
		cal.add(Calendar.MONTH, n);
		return cal.getTime();
	}

	public static int daysBetween(Date returnDate) {
		return daysBetween(null, returnDate);
	}

	public static int daysBetween(Date now, Date returnDate) {
		if (returnDate == null)
			return 0;

		Calendar cNow = getNow();
		Calendar cReturnDate = getNow();
		if (now != null) {
			cNow.setTime(now);
		}
		cReturnDate.setTime(returnDate);
		setTimeToMidnight(cNow);
		setTimeToMidnight(cReturnDate);
		long nowMs = cNow.getTimeInMillis();
		long returnMs = cReturnDate.getTimeInMillis();
		return millisecondsToDays(nowMs - returnMs);
	}

	private static int millisecondsToDays(long intervalMs) {
		return (int) (intervalMs / (1000 * 86400));
	}

	private static void setTimeToMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
	}

	public static String formatDate(Object obj, String format) {
		String result = "";
		try {
			Date date = (Date) obj;
			result = format(date, format);
		} catch (Exception e) {

		}
		return result;
	}

	public static String formatDate(Object obj) {
		return formatDate(obj, SHORT_DATE);
	}

	public static String getSunday(String date) {
		Calendar c = DateUtils.parseDateString(date, "yyyy-MM-dd");
		int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0) {
			dayofweek = 0;
		}
		c.add(Calendar.DATE, -dayofweek);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}

	public static void main(String[] args) {
		System.out.println(DateUtils.getYesterday());
		Calendar cal = getNow();
		System.out.println(cal.getTimeInMillis());
		Long ts = cal.getTimeInMillis();
		Date date = new Date(ts);
		System.out.println(DateUtils.format(date));
		System.out.println("----------");
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 10);
		System.out.println(DateUtils.format(cal));
		System.out.println(DateUtils.daysBetween(cal.getTime()));
		System.out.println(DateUtils.parseDateString("2008-6-31", DateUtils.SHORT_DATE) == null);
		System.out.println(DateUtils.format(DateUtils.parseDateString("2008-6-31", DateUtils.SHORT_DATE)));
	}
}