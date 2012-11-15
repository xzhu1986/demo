package au.com.isell.common.xstream;

import java.lang.reflect.Field;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class XStreamAnnotationExtra {
	public static void applyAnnotations(XStream xs, Class<?> clazz) {
		if (clazz.isAnnotationPresent(XStreamAlias.class)) {
			XStreamAlias alias = clazz.getAnnotation(XStreamAlias.class);
			xs.alias(alias.value(), clazz);
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass == null) return;
		for(Field field : superClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(XStreamOmitField.class)) xs.omitField(superClass, field.getName());
		}
		applyAnnotations(xs, superClass);
	}
}
