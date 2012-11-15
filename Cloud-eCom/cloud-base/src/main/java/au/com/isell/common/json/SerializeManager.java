package au.com.isell.common.json;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class SerializeManager {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static <T> T toObject(InputStream in, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		if (in == null) return null;
		return mapper.readValue(in, clazz);
	}
	
	public static <T> T toObject(String in, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		if (in == null) return null;
		return mapper.readValue(in, clazz);
	}
	
	public static String serialize(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
		if (obj == null) return null;
		return mapper.writeValueAsString(obj);
	}
	
	public static void serialize(Object obj, Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
		if (obj == null) return;
		mapper.writeValue(writer, obj);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(InputStream in) throws JsonParseException, JsonMappingException, IOException {
		if (in == null) return null;
		return mapper.readValue(in, Map.class);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String in) throws JsonParseException, JsonMappingException, IOException {
		if (in == null) return null;
		return mapper.readValue(in, Map.class);
	}
	
	public static void main(String[] args) {
		try {
			Map<String, Object> map = toMap(new FileInputStream("/Users/yezhou/temp/emr/python/parser_im_bdtl.json"));
			System.out.println(map);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
