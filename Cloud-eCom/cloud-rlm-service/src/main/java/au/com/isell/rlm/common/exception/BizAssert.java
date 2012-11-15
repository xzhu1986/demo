package au.com.isell.rlm.common.exception;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

//import org.springframework.util.CollectionUtils;
//import org.springframework.util.ObjectUtils;
//import org.springframework.util.StringUtils;
/**
 * refer to : org.springframework.util.Assert 
 * @author frankw 26/04/2012
 */
public class BizAssert {

	public static void isTrue(boolean expression, String msgCode, String... params) {
		if (!expression) {
			throw new BizException(msgCode, params);
		}
	}

	public static void isNull(Object object, String msgCode, String... params) {
		if (object != null) {
			throw new BizException(msgCode, params);
		}
	}

	public static void notNull(Object object, String msgCode, String... params) {
		if (object == null) {
			throw new BizException(msgCode, params);
		}
	}

	public static void hasText(String text, String msgCode, String... params) {
		if (StringUtils.isEmpty(text)) {
			throw new BizException(msgCode, params);
		}
	}

	public static void doesNotContain(String textToSearch, String substring, String msgCode, String... params) {
		if (StringUtils.isNotEmpty(textToSearch) && StringUtils.isNotEmpty(substring) && textToSearch.indexOf(substring) != -1) {
			throw new BizException(msgCode, params);
		}
	}

	public static void notEmpty(Object[] array, String msgCode, String... params) {
		if (ArrayUtils.isEmpty(array)) {
			throw new BizException(msgCode, params);
		}
	}

	public static void notEmpty(Collection collection, String msgCode, String... params) {
		if (CollectionUtils.isEmpty(collection)) {
			throw new BizException(msgCode, params);
		}
	}

	public static void notEmpty(Map map, String msgCode, String... params) {
		if (MapUtils.isEmpty(map)) {
			throw new BizException(msgCode, params);
		}
	}

	public static void state(boolean expression, String msgCode, String... params) {
		if (!expression) {
			throw new BizException(msgCode,params);
		}
	}
}
