package au.com.isell.common.aws;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.util.TextFormat;

public class S3Utils {
	private static final String OBJECT_IS_NULL = "[object] is null!";
	private static final String OBJECT_MUST_HAS_ANNOTATION2 = "object must has annotation [" + IsellPath.class.toString() + "]";

	public static String getS3Key(Object object) {
		Assert.isTrue(object != null, OBJECT_IS_NULL);
		Class<?> cls = object.getClass();
		while (!cls.isAnnotationPresent(IsellPath.class)) {
			cls = cls.getSuperclass();
			Assert.notNull(cls, OBJECT_MUST_HAS_ANNOTATION2);
		}
		Assert.isTrue(cls.getAnnotation(IsellPath.class) != null, OBJECT_MUST_HAS_ANNOTATION2);
		IsellPath s3Path = cls.getAnnotation(IsellPath.class);
		String pathFmt = s3Path.value();
		// remove first '/'
		pathFmt = pathFmt.startsWith("/") ? pathFmt.substring(1, pathFmt.length()) : pathFmt;

		if (!TextFormat.isFormatedStr(pathFmt))
			return pathFmt;
		try {
			Map<String, Object> fmtParams = getS3PathParams(object, object.getClass());
			Assert.isTrue(fmtParams.size()>0,"No s3 path formated params : "+object.toString());
			return TextFormat.format(pathFmt, fmtParams);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static Map<String, Object> getS3PathParams(Object object, Class<?> cls) throws IllegalAccessException {
		Map<String, Object> fmtParams = new HashMap<String, Object>();
		try {
			BeanInfo info = Introspector.getBeanInfo(cls);
			for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
				Method method = prop.getReadMethod();
				if (method == null)
					continue;
				if (method.isAnnotationPresent(IsellPathField.class)) {
					Object valValue = method.invoke(object);
					fmtParams.put(prop.getName(), valValue);
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException("Set S3 path for class " + cls.getName() + " error: " + e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Set S3 path for class " + cls.getName() + " error: " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Set S3 path for class " + cls.getName() + " error: " + e.getMessage(), e);
		}
		Class<?> superClass = cls.getSuperclass();
		if (superClass != null) {
			fmtParams.putAll(getS3PathParams(object, superClass));
		}
		return fmtParams;
	}

}
