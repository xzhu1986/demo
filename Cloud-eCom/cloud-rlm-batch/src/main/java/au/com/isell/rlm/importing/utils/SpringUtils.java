package au.com.isell.rlm.importing.utils;

import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/***
 * SpringUtil, a Spring utility.
 * <p>
 * Applicable to Spring Framework version 2.x or later
 * </p>
 * 
 * @author peterz
 */
public class SpringUtils {
	private static ApplicationContext context;
	/***
	 * Get the spring bean by the specified name.
	 */
	public static Object getBean(String name) {
		Object o = null;
		try {
			if (getContext().containsBean(name)) {
				o = getContext().getBean(name);
			}
		} catch (NoSuchBeanDefinitionException e) {
			e.printStackTrace();
		}
		return o;
	}

	/***
	 * Get the spring bean by the specified name and class.
	 */
	public static <T> T getBean(String name, Class<T> cls) {
		T o = null;
		try {
			if (getContext().containsBean(name)) {
				o = getContext().getBean(name, cls);
			}
		} catch (NoSuchBeanDefinitionException e) {
			e.printStackTrace();
		} catch (BeanNotOfRequiredTypeException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	/***
	 * Get the spring bean by the specified name and class.
	 */
	public static <T> T getBean(Class<T> cls) {
		T o = null;
		try {
			o = getContext().getBean(cls);
		} catch (NoSuchBeanDefinitionException e) {
			e.printStackTrace();
		} catch (BeanNotOfRequiredTypeException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	private synchronized static ApplicationContext getContext() {
		if (context == null) {
			context = new ClassPathXmlApplicationContext("applicationContext.xml");
		}
		return context;
	}
}
