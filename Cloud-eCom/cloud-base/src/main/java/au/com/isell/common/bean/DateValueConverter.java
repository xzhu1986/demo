package au.com.isell.common.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author frankw 10/02/2012
 */
public class DateValueConverter extends DateTimeConverter {
	private static Logger logger = LoggerFactory.getLogger(DateValueConverter.class);
	public final static String[] defaultFormat = new String[] { "dd MMM yyyy 'at' h:mm a", "dd MMM yyyy", "yyyy-MM-dd HH:mm:ss",
			"yyyyMMdd HHmmss", "yyyyMMddHHmmss", "yyyyMMdd", "yyyy-MM-dd" };

	@Override
	protected Class getDefaultType() {
		return Date.class;
	}

	private static Pattern chinaPattern1 = Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日 (上午|下午)\\d{1,2}点\\d{1,2}分");
	private static Pattern chinaPattern2 = Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日");

	@Override
	public Object convert(Class type, Object value) {
		if (value == null || StringUtils.isBlank(value.toString())) {
			return null;
		}

		if (value instanceof Date) {
			return value;
		}

		if (value instanceof Long) {
			Long longValue = (Long) value;
			return new Date(longValue.longValue());
		}
		if (value instanceof String) {
			try {
				if (chinaPattern1.matcher((String) value).find()) {
					return new SimpleDateFormat("yyyy'年'M'月'd'日' ah'点'mm'分'", Locale.CHINESE).parse((String) value);
				} else if (chinaPattern2.matcher((String) value).find()) {
					return new SimpleDateFormat("yyyy'年'M'月'd'日'", Locale.CHINESE).parse((String) value);
				}
				return DateUtils.parseDate(value.toString(), defaultFormat);
			} catch (ParseException e) {
				logger.info("encounter problmem {} when use default format to convvert date,switch to apache converter...", e.getMessage());
				return super.convert(type, value);
			}
		}
		return null;
	}

//	public static void main(String[] args) throws ParseException {
//		System.out.println("2012年3月15日 下午3点15分".matches("\\d{4}年\\d{1,2}月\\d{1,2}日 (上午|下午)\\d{1,2}点\\d{1,2}分"));
//		System.out.println("2012年3月20日 星期二".matches("\\d{4}年\\d{1,2}月\\d{1,2}日 星期[一二三四五六日]"));
//		SimpleDateFormat sdf=new SimpleDateFormat("yyyy'年'M'月'd'日' E",Locale.CHINA);
//		System.out.println(sdf.format(new Date()));
//	}
}
