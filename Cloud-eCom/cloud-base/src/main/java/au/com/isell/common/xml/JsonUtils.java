package au.com.isell.common.xml;

import java.io.InputStream;
import java.io.Reader;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
/**
 * @author frankw 02/05/2012
 */
public class JsonUtils {
	public static <T> T decode(String source, Class<T> type) {
		try {
			return getObjMapper().readValue(source, type);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static ObjectMapper getObjMapper() {
		ObjectMapper objectMapper=SerializeUtils.getDefaultObjMapper();
		configObjectMapper(objectMapper);
		return objectMapper;
	}

	public static <T> T decode(Reader source, Class<T> type) {
		try {
			return getObjMapper().readValue(source, type);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static <T> T decode(InputStream source, Class<T> type) {
		try {
			return getObjMapper().readValue(source, type);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String encode(Object obj) {
		try {
			return getObjMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static void configObjectMapper(ObjectMapper defaultObjMapper) {
		SerializationConfig serializationConfig = defaultObjMapper.getSerializationConfig();
		serializationConfig = serializationConfig.with(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS).without(
				SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS). withSerializationInclusion(Inclusion.NON_NULL);
		defaultObjMapper.setSerializationConfig(serializationConfig);
//		
//		DeserializationConfig deserializationConfig = defaultObjMapper.getDeserializationConfig();
//		deserializationConfig.without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
//		defaultObjMapper.setDeserializationConfig(deserializationConfig);
//
//		defaultObjMapper.setDateFormat(new SimpleDateFormat("yyyyMMdd HHmmss"));
//		
//		defaultObjMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
//		defaultObjMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		defaultObjMapper.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmss"));
	}
}
