package au.com.isell.common.xml;

import java.util.Hashtable;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.isell.common.exi.EXIDriver;
import au.com.isell.common.util.ClassFilter;
import au.com.isell.common.util.ClassMatcher;
import au.com.isell.common.xstream.XStreamAnnotationExtra;

import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author frankw 02/02/2012
 */
public class SerializeUtils {
	private static Logger logger = LoggerFactory.getLogger(SerializeUtils.class);
	
	private static GsonBuilder gson = new GsonBuilder();
	private static Map<String, Class<?>> mapper = new Hashtable<String, Class<?>>();
	private static XStream exiXStream = new XStream(new EXIDriver());
	private static XStream defaultXStream = new XStream();
	/** jackson */
	private static ObjectMapper defaultObjMapper=new ObjectMapper();
//	public static XStream jsonXStream = new XStream(new JettisonMappedXmlDriver());

	static {
		config(exiXStream);
		config(defaultXStream);
		configObjectMapper(defaultObjMapper);

		logger.info("scan and register types with [XStreamAlias]");
		ClassFilter.doFilter("au.com.isell", new ClassMatcher() {
			@Override
			public void match(Class<?> cls) {
				if (cls.isAnnotationPresent(XStreamAlias.class)) {
					logger.debug(cls.toString());
					XStreamAnnotationExtra.applyAnnotations(exiXStream, cls);
					XStreamAnnotationExtra.applyAnnotations(defaultXStream, cls);
//					XStreamAnnotationExtra.applyAnnotations(jsonXStream, cls);
					XStreamAlias alias = cls.getAnnotation(XStreamAlias.class);
					mapper.put(alias.value(), cls);
				}
			}
		});

	}
	
	public static void configObjectMapper(ObjectMapper mapper) {
		SerializationConfig serializationConfig = mapper.getSerializationConfig();
		serializationConfig = serializationConfig.with(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)
				.with(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS).withSerializationInclusion(Inclusion.NON_NULL);
		mapper.setSerializationConfig(serializationConfig);

		DeserializationConfig deserializationConfig = mapper.getDeserializationConfig().without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setDeserializationConfig(deserializationConfig);
	}

	private static void config(XStream xStream) {
		xStream.autodetectAnnotations(true);
		xStream.registerConverter(new CustomEnumConverter());
	}

	public static XStream getExiXStream() {
		return exiXStream;
	}

	public static XStream getDefaultXStream() {
		return defaultXStream;
	}
	
	public static GsonBuilder getDefaultGson() {
		return gson;
	}
	
	public static Class<?> getClassByAlias(String alias){
		return mapper.get(alias);
	}

	public static ObjectMapper getDefaultObjMapper() {
		return defaultObjMapper;
	}


}
