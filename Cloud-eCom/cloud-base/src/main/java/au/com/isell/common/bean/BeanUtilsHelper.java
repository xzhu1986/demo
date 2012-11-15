package au.com.isell.common.bean;

import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

class BeanUtilsHelper {
	static boolean isInvlidOrBlankNumber(Class<?> type, Object value) {
		return Number.class.isAssignableFrom(type) && (value == null || !NumberUtils.isNumber(value.toString()));
	}

	static boolean isIgnoreValueType(Class target) {
		return target.isArray() || Set.class.isAssignableFrom(target) || target == Class.class ? true : false;
	}
	
	static boolean contains(String propertyName, String... props) {
		for (String prop : props) {
			if (prop.equalsIgnoreCase(propertyName)) {
				return true;
			}
		}
		return false;
	}

	static boolean ignoreValueWhenConstruct(Class<?> type, Object value) {
		//value instanceof JSONNull?
		return isInvlidOrBlankNumber(type, value);
	}
}