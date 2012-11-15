package au.com.isell.common.bean;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.util.Assert;

/**
 * @author frankw 31/03/2012
 */
public class BeanUtils {
	static {
		registConverter();
	}

	private static boolean isRegisted = false;

	public synchronized static void registConverter() {
		if (isRegisted)
			return;
		ConvertUtils.register(new DateValueConverter(), Date.class);
		ConvertUtils.register(new UUIDValueConverter(), UUID.class);
	}

	public static <S, T> T constructBean(S source, Class<T> beanClass, String namePrefix, String... excludeProps) {
		try {
			Object bean = beanClass.newInstance();
			PropertyDescriptor[] pds = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
			boolean usePrefixedName = StringUtils.isNotEmpty(namePrefix) ? true : false;
			SourceAdapter sourceAdapter = SourceAdapter.getAdapter(source);
			for (PropertyDescriptor pd : pds) {
				if (pd.getWriteMethod() == null)
					continue;
				String propName = pd.getName();
				Class<?> propType = pd.getPropertyType();
				if (BeanUtilsHelper.contains(propName, excludeProps))
					continue;
				if (BeanUtilsHelper.isIgnoreValueType(propType))
					continue;
				Object value = sourceAdapter.getValue(usePrefixedName ? namePrefix + propName : propName);
				// prevent to transfer '' or null to 0
				if (BeanUtilsHelper.ignoreValueWhenConstruct(propType, value)) {
					continue;
				}
				// special value type
				if (propType.isEnum()) {
					if (value != null && StringUtils.isNotEmpty(value.toString())) {
						value = getEnum(propType, value.toString());
					} else {
						continue;
					}
				}
				if(value==null) continue;
				org.apache.commons.beanutils.BeanUtils.setProperty(bean, propName, value);
// 				pd.getWriteMethod().invoke(bean, ConvertUtils.convert(value, propType));
			}
			return (T) bean;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Object getEnum(Class enumCls, String value) {
		return NumberUtils.isNumber(value) ? enumCls.getEnumConstants()[Integer.valueOf(value)] : Enum.valueOf(enumCls, value);
	}

	public static void copyPropsIncludeNull(Object dest, Object orgi, String... excludeProps) {
		copyPropsOnDemond(dest, orgi, true, excludeProps);
	}

	public static void copyPropsExcludeNull(Object dest, Object orgi, String... excludeProps) {
		copyPropsOnDemond(dest, orgi, false, excludeProps);
	}

	private static void copyPropsOnDemond(Object dest, Object orgi, boolean copyNullValue, String... excludeProps) {
		try {
			PropertyDescriptor[] orgiPds = Introspector.getBeanInfo(orgi.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor orgiPd : orgiPds) {
				Method destWriteMethod = null;
				// ignore
				if (Class.class == orgiPd.getPropertyType() || orgiPd.getReadMethod() == null
						|| (destWriteMethod = getDestWriteMethod(dest, orgiPd, destWriteMethod)) == null) {
					continue;
				}
				// test value
				Method readMethod = orgiPd.getReadMethod();
				Object value = readMethod.invoke(orgi);
				if (value == null && (!copyNullValue && !BeanUtilsHelper.contains(orgiPd.getName(), excludeProps))) {
					continue;
				}
				// copy
				if (!readMethod.isAnnotationPresent(DeepCopy.class) || value == null || readMethod.invoke(dest) == null) {
					destWriteMethod.invoke(dest, value);
				} else {  // deep copy
					recruciveCopy(dest, copyNullValue, orgiPd, readMethod, value, excludeProps);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void recruciveCopy(Object dest, boolean copyNullValue, PropertyDescriptor orgiPd, Method readMethod, Object value,
			String... excludeProps) throws IllegalAccessException, InvocationTargetException {
		String propName = orgiPd.getName()+".";
		List<String> excludes = new ArrayList<String>();
		for (String ex : excludeProps) {
			if (! ex.startsWith(propName)) continue;
			String str = ex.substring(propName.length());
			if (str.length() > 0) excludes.add(str);
		}
		Object destValue = readMethod.invoke(dest);
		if (value instanceof Map && destValue instanceof Map) {
			Map<Object, Object> destMap = (Map<Object, Object>) destValue;
			Map<Object, Object> srcMap = (Map<Object, Object>) value;
			for (Map.Entry<Object, Object> srcItem : srcMap.entrySet()) {
				Object key = srcItem.getKey();
				if (destMap.get(key) == null) destMap.put(key, srcItem.getValue());
				else copyPropsOnDemond(destMap.get(key), srcItem.getValue(), copyNullValue, excludes.toArray(new String[excludes.size()]));
			}
		} else if (value instanceof List) {
			List<Object> destList = (List<Object>) destValue;
			List<Object> srcList = (List<Object>) value;
			for (int i = 0; i < srcList.size(); i++) {
				if (i >= destList.size()) destList.add(srcList.get(i));
				else copyPropsOnDemond(destList.get(i), srcList.get(i), copyNullValue, excludes.toArray(new String[excludes.size()]));
			}
		} else {
			copyPropsOnDemond(destValue, value, copyNullValue, excludes.toArray(new String[excludes.size()]));
		}
	}

	private static Method getDestWriteMethod(Object dest, PropertyDescriptor orgiPd, Method destWriteMethod) {
		try {
			return dest.getClass().getMethod(orgiPd.getWriteMethod().getName(), orgiPd.getWriteMethod().getParameterTypes());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * copy value from several source bean by set method with annotation
	 * 
	 * @param target  should has @FromProp on write method
	 * @param source
	 *             different type
	 */
	public static void copyFromMutliSources(Object target, Object... source) {
		Assert.notNull(source, "source can not be null");
		Map<Class, Object> sourceMap = new HashMap<Class, Object>();
		for (Object object : source) {
			if(object==null)  continue;
			sourceMap.put(object.getClass(), object);
		}
		try {
			PropertyDescriptor[] pds = Introspector.getBeanInfo(target.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				Method writer = pd.getWriteMethod();
				if (writer == null  || !writer.isAnnotationPresent(FromProp.class))
					continue;
				FromProp fromProp = writer.getAnnotation(FromProp.class);
				Class searchType = fromProp.type();
				String propPath = fromProp.value();
				Object fromBean = sourceMap.get(searchType);
				if (fromBean == null)
					continue;
				String[] path = propPath.split("\\.");
				Object propValue = searchPropValue(fromBean, path, 0);
				if (propValue != null)
					org.apache.commons.beanutils.BeanUtils.setProperty(target, pd.getName(), propValue);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} 

	}
	

	private static Object searchPropValue(Object obj, String[] propPath, int i) throws Exception {
		if(obj==null) return null;
		if (i < propPath.length) {
			Object prop = PropertyUtils.getProperty(obj, propPath[i]);
			return searchPropValue(prop, propPath, i + 1);
		} else {
			return obj;
		}
	}
	
	public static Map toMap(Object bean){
		try {
			return org.apache.commons.beanutils.BeanUtils.describe(bean);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}

class MapSourceAdapter extends SourceAdapter {
	private Map map;

	MapSourceAdapter(Object source) {
		map = (Map) source;

	}

	@Override
	public Object getValue(String fieldName) {
		Object value = map.get(fieldName);
		if (value != null)
			return value;
		value = map.get(fieldName.toUpperCase());
		if (value != null)
			return value;
		return map.get(fieldName.toLowerCase());
	}

}

class HttpSourceAdapter extends SourceAdapter {
	private HttpServletRequest request;

	HttpSourceAdapter(Object source) {
		request = (HttpServletRequest) source;

	}

	@Override
	public Object getValue(String fieldName) {
		Object value = request.getParameter(fieldName);
		if (value != null)
			return value;
		value = request.getParameter(fieldName.toUpperCase());
		if (value != null)
			return value;
		return value = request.getParameter(fieldName.toLowerCase());
	}

}

abstract class SourceAdapter {
	static SourceAdapter getAdapter(Object source) {
		if (HttpServletRequest.class.isInstance(source)) {
			return new HttpSourceAdapter(source);
		} else if (Map.class.isInstance(source)) {
			return new MapSourceAdapter(source);
		} else {
			throw new RuntimeException("Unsupported source type : " + source.getClass().toString());
		}
	}

	public abstract Object getValue(String fieldName);
}
