package au.com.isell.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * new Date() is always utc time ! calendar & dateformat will include timezone<br/>
 * GMT Greenwich Mean Time<br/>
 * CST Central Standard Time (USA) UT-6:00<br/>
 * CST Central Standard Time (Australia) UT+9:30<br/>
 * CST China Standard Time UT+8:00<br/>
 * CST Cuba Standard Time UT-4:00<br/>
 * UTC Universal Time Coordinated<br/>
 * GMT + 8 = UTC + 8 = CST
 * 
 * @author frankw 20/02/2012
 */
public class UTCDateUtils {
	public static final String storeDataFormat = "yyyyMMddHHmmss";
	private static final String readDataFormat1 = "yyyy-MM-dd HH:mm:ss";
//	private static final String readDataFormat2 = "yyyy-MM-dd";

	public static String format(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(storeDataFormat);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	public static Date parse(String utcDateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(storeDataFormat);
		 sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return sdf.parse(utcDateStr);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	/** get formated date base on local timezone*/
	public static String moreReadable(String utcDateStr) {
		SimpleDateFormat sdf1 = new SimpleDateFormat(storeDataFormat);
		sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdf2 = new SimpleDateFormat(readDataFormat1);
		try {
			return sdf2.format(sdf1.parse(utcDateStr));
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

//this is wrong!	
//	public static Date getUTCDate(Date date) {
//		Calendar c = Calendar.getInstance();
//		TimeZone z = c.getTimeZone();
//		int offset = z.getRawOffset();
//		int offsetHrs = offset / 1000 / 60 / 60;
//		int offsetMins = offset / 1000 / 60 % 60;
//
//		c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
//		c.add(Calendar.MINUTE, (-offsetMins));
//
//		return c.getTime();
//	}

	public static void main(String[] args) {
//		Date date = new Date();
//		System.out.println(format(date));
//		System.out.println(moreReadable(format(date)));
//		System.out.println(parse(format(date)));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, 11);
		System.out.println(Calendar.FRIDAY-calendar.get(Calendar.DAY_OF_WEEK));
		calendar.add(Calendar.DATE, 7+(Calendar.FRIDAY-calendar.get(Calendar.DAY_OF_WEEK)));
		System.out.println(calendar.getTime());
		calendar.add(Calendar.DATE,7);
		System.out.println(calendar.getTime());
		calendar.setTimeInMillis(calendar.getTimeInMillis()+24*60*60*1000*7*2);
		System.out.println(calendar.getTime());
		
//		System.out.println(getUTCDate(date));
	}
}
