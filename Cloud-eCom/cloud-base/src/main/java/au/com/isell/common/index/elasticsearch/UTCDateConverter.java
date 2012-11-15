package au.com.isell.common.index.elasticsearch;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.util.UTCDateUtils;

/**
 * 
 * @author frankw 10/02/2012
 */
public class UTCDateConverter {
	
	public static Date convert(Object value) {
		if (value == null || StringUtils.isBlank(value.toString())) {
			return null;
		}

		if (value instanceof Date) {
			return (Date)value;
		}

		if (value instanceof Long) {
			Long longValue = (Long) value;
			return new Date(longValue.longValue());
		}
		if (value instanceof String) {
			return  UTCDateUtils.parse(value.toString());
		}
		return null;
	}
}
