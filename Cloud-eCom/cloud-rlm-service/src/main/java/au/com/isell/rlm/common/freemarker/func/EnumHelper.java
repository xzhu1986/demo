package au.com.isell.rlm.common.freemarker.func;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import au.com.isell.common.util.LRUCache;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

/**
 * @author frankw 31/03/2012
 */
public class EnumHelper {
	static LRUCache<String, Class<?>> classCache = new LRUCache<String, Class<?>>(100);

	private static Method getDisplayMethod(Class c) {
		Method displayMethod = null;
		try {
			displayMethod = c.getMethod("display");// display method
		} catch (NoSuchMethodException e1) {
		}
		return displayMethod;
	}

	static Class getEnumClass(String clsName)  {
		Class c = classCache.get(clsName);
		try {
			if (c == null) {
				c = Class.forName(clsName);
				classCache.put(clsName, c);
			}
			return c;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String getDisplayCodeOrMsg(Class c, Enum e)  {
		String name = null;
		Field f = null;
		Method displayMethod = null;
		try {
			f = c.getField(e.name());
		} catch (NoSuchFieldException e1) {
		}
		if (f != null && f.isAnnotationPresent(EnumMsgCode.class)) {
			name = f.getAnnotation(EnumMsgCode.class).value();
		} else if ((displayMethod = getDisplayMethod(c)) != null) {
			try {
				name = (String) displayMethod.invoke(e);
			} catch (Exception e1) {
				throw new RuntimeException(e1.getMessage(), e1);
			} 
		} else {
			name = e.toString();
		}
		return name;
	}
}
