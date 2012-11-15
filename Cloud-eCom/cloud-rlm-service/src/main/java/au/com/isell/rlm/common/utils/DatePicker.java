package au.com.isell.rlm.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * warn: all picked date will reset time to 00:00:00
 * 
 * @author frankw 30/03/2012
 */
public class DatePicker {
	private static Logger logger = LoggerFactory.getLogger(DatePicker.class);

	public static Date pickFirstDayOfYear(Date date, int yearAmmount) {
		Calendar calendar = getCalendarInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, yearAmmount);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DATE, 1);
		resetTimeToZero(calendar);
		return calendar.getTime();
	}

	public static Calendar getCalendarInstance() {
		String timeZoneStr = GlobalAttrManager.getClientInfo().getTimezone();
		TimeZone timeZone = null;
		if (timeZoneStr == null) {
			logger.warn("//===Can not get timezone from threadlocal,use default instand===//");
			timeZone = TimeZone.getDefault();
		} else {
			timeZone = TimeZone.getTimeZone(timeZoneStr);
		}
		Calendar calendar = Calendar.getInstance(timeZone);
		return calendar;
	}

	public static Date pickLastDayOfYear(Date date, int yearAmmount) {
		Calendar calendar = getCalendarInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, yearAmmount);
		calendar.set(Calendar.MONTH, 12);
		calendar.set(Calendar.DATE, 31);
		resetTimeToZero(calendar);
		return calendar.getTime();
	}

	public static Date pickFirstDayOfMonth(Date date, int mounthAmmount) {
		Calendar calendar = getCalendarInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, mounthAmmount);
		calendar.set(Calendar.DATE, 1);
		resetTimeToZero(calendar);
		return calendar.getTime();
	}

	public static Date pickLastDayOfMonth(Date date, int mounthAmmount) {
		Calendar calendar = getCalendarInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DATE, 1);
		calendar.add(Calendar.MONTH, mounthAmmount + 1);
		calendar.add(Calendar.DATE, -1);
		resetTimeToZero(calendar);
		return calendar.getTime();
	}

	public static Date pickDay(Date date, int dayAmmount) {
		Calendar calendar = getCalendarInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, dayAmmount);
		resetTimeToZero(calendar);
		return calendar.getTime();
	}

	public static Date pickAssignDate(Date date, int mounthAmmount, int dayAmmount) {
		Calendar calendar = getCalendarInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, mounthAmmount);
		int currenMounth = calendar.get(Calendar.MONTH);

		calendar.set(Calendar.DATE, 1);
		calendar.add(Calendar.DATE, dayAmmount - 1);
		if (currenMounth < calendar.get(Calendar.MONTH)) {
			calendar.set(Calendar.DATE, 1);
			calendar.add(Calendar.DATE, -1);
		}
		resetTimeToZero(calendar);
		return calendar.getTime();
	}

	public static void resetTimeToZero(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	public static Date pick23DayOfMonth(Date date, int mounthAmmount) {
		Calendar calendar = getCalendarInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, mounthAmmount);
		calendar.set(Calendar.DATE, 23);
		resetTimeToZero(calendar);
		return calendar.getTime();
	}

	public static String getFormatedDate(Date date) {
		if (date == null)
			return null;
		SimpleDateFormat format = null;
		Locale locale = GlobalAttrManager.getClientInfo().getLocale();
		if (locale.toString().indexOf("zh") > -1) {
			format = new SimpleDateFormat("yyyy'年'M'月'd'日'", locale);
		} else {
			format = new SimpleDateFormat("dd MMM yyyy", locale);
		}
		format.setTimeZone(TimeZone.getTimeZone(GlobalAttrManager.getClientInfo().getTimezone()));
		return format.format(date);
	}

	public static String getFormatedDateTime(Date date) {
		if (date == null)
			return null;
		SimpleDateFormat format = null;
		Locale locale = GlobalAttrManager.getClientInfo().getLocale();
		if (GlobalAttrManager.getClientInfo().getLocale().toString().indexOf("zh") > -1) {
			format = new SimpleDateFormat("yyyy'年'M'月'd'日' ah'点'mm'分'", locale);
		} else {
			format = new SimpleDateFormat("dd MMM yyyy 'at' h:mm a", locale);
		}
		format.setTimeZone(TimeZone.getTimeZone(GlobalAttrManager.getClientInfo().getTimezone()));
		return format.format(date);
	}

	public static String getFormatedtime(Date date) {
		if (date == null)
			return null;
		SimpleDateFormat format = null;
		Locale locale = GlobalAttrManager.getClientInfo().getLocale();
		if (GlobalAttrManager.getClientInfo().getLocale().toString().indexOf("zh") > -1) {
			format = new SimpleDateFormat("ah'点'mm'分'", locale);
		} else {
			format = new SimpleDateFormat("h:mm a", locale);
		}
		format.setTimeZone(TimeZone.getTimeZone(GlobalAttrManager.getClientInfo().getTimezone()));
		return format.format(date);
	}
}
