package au.com.isell.common.bean;

import java.util.UUID;

import org.apache.commons.beanutils.converters.AbstractConverter;
import org.apache.commons.lang.StringUtils;

/**
 * @author frankw 10/02/2012
 */
public class UUIDValueConverter extends AbstractConverter {

	@Override
	protected Object convertToType(Class type, Object value) throws Throwable {
		if (value == null || StringUtils.isEmpty(value.toString()))
			return null;
		if (value instanceof UUID) {
			return value;
		}
		return UUID.fromString(value.toString());
	}

	@Override
	protected Class getDefaultType() {
		return UUID.class;
	}

}
