package au.com.isell.common.index.elasticsearch;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;

/**
 * @author frankw 20/02/2012
 */
public class IsellIndexBeanUtils {

	static <S, T> T constructBean(Map source, Class<T> beanClass) {
		BeanUtils.registConverter();
		
		Object bean = null;
		try {
			bean = beanClass.newInstance();
			PropertyDescriptor[] pds = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (pd.getWriteMethod() == null)
					continue;
				String propName = pd.getName();

				Object value = null;
				if (pd.getReadMethod().isAnnotationPresent(ISellIndexKey.class)) {
					value = source.containsKey("_id") ? source.get("_id") : source.get(propName);
				} else if (pd.getReadMethod().isAnnotationPresent(ISellIndexValue.class)) {
					value = source.get(propName);
				} else {
					continue;
				}
				if (isIgnoreValue(pd, value)) {
					continue;
				}
				Class<?> propertyType = pd.getPropertyType();
				if (propertyType.isEnum()) {// use enum string name
					if (value != null && StringUtils.isNotEmpty(value.toString())) {
						value = BeanUtils.getEnum(propertyType, value.toString());
					} else {
						continue;
					}
				} 
//				else if (UUID.class.isAssignableFrom(propertyType)) {
//					if (value != null && StringUtils.isNotEmpty(value.toString())) {
//						value = UUID.fromString(value.toString());
//					} else {
//						continue;
//					}
//				} 
				else if (Date.class.isAssignableFrom(propertyType)) {
					value = UTCDateConverter.convert(value);
				} else if (BigDecimal.class.isAssignableFrom(propertyType)) {
					value = new BigDecimal(value.toString());
				}
				if(value==null) continue;
				org.apache.commons.beanutils.BeanUtils.setProperty(bean, propName, value);
//				pd.getWriteMethod().invoke(bean, ConvertUtils.convert(value, propertyType));
			}
			return (T) bean;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static boolean isIgnoreValue(PropertyDescriptor pd, Object value) {
		return isBlankNumber(pd, value);
	}

	private static boolean isBlankNumber(PropertyDescriptor pd, Object value) {
		return (value == null || "".equals(value.toString()) || "null".equals(value.toString()))
				&& Number.class.isAssignableFrom(pd.getPropertyType());
	}

}
