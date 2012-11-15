package au.com.isell.rlm.common.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.com.isell.common.util.LRUCache;

public class DataLoadUtils {
	private static LRUCache<String, Class<?>> classCache = new LRUCache<String, Class<?>>(50);
	/**
	 * invoke this after sever startup
	 */
	public static Object loadServiceData4Web(String serviceClassName, String methodClassName, Object... methodArgs) {
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(ContextManager.getContext());
		return loadServiceData4Spring(applicationContext, serviceClassName, methodClassName, methodArgs);
	}

	public static Object loadServiceData4Spring(ApplicationContext applicationContext, String serviceClassName, String methodClassName,
			Object... methodArgs) {
		try {
			Class serviceCls = getClass(serviceClassName.trim());
			Object service = applicationContext.getBean(serviceCls);
			Object r = null;
			if (methodArgs != null && methodArgs[0]!=null) {
				Class[] paramsCls = getParamCls(methodArgs);
				r = serviceCls.getMethod(methodClassName.trim(), paramsCls).invoke(service, methodArgs);
			} else {
				r = serviceCls.getMethod(methodClassName.trim()).invoke(service);
			}
			if (r == null)
				r = new Object();
			return r;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static Class[] getParamCls(Object[] methodArgs) {
		Class[] paramsCls = new Class[methodArgs.length];
		for (int i = 0; i < methodArgs.length; i++) {
			paramsCls[i] = methodArgs[i].getClass();
		}
		return paramsCls;
	}

	private static Class getClass(String clsName) throws ClassNotFoundException {
		Class c = classCache.get(clsName);
		if (c == null) {
			c = Class.forName(clsName);
			classCache.put(clsName, c);
		}
		return c;
	}
}
