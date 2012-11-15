package au.com.isell.common.xml;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.math.NumberUtils;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CustomEnumConverter extends EnumConverter {
	private static ConcurrentHashMap<Class, EnumValues> enumMap = new ConcurrentHashMap();

	@Override
	public boolean canConvert(Class type) {
		return super.canConvert(type);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.setValue(String.valueOf(((Enum) source).ordinal()));
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Class type = context.getRequiredType();
		if (type.getSuperclass() != Enum.class) {
			type = type.getSuperclass(); // polymorphic enums
		}
		String strVal = reader.getValue();
		if (!NumberUtils.isNumber(strVal))
			return null;
		EnumValues values = getEnumValues(type);
		return values.intToValue(Integer.valueOf(strVal));
	}

	private EnumValues getEnumValues(Class type) {
		EnumValues values = enumMap.get(type);
		if (values == null) {
			values=new EnumValues(type);
			enumMap.put(type, values);
		}
		return values;
	}

}

class EnumValues<E extends Enum<E>> {
	E[] values;

	public EnumValues(Class<E> enumType) {
		values = enumType.getEnumConstants();
	}

	public E intToValue(int i) {
		return values[i];
	}
}