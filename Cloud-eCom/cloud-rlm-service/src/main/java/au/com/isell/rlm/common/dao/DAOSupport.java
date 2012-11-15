package au.com.isell.rlm.common.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.aws.S3Utils;
import au.com.isell.common.bean.AfterSaving;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.common.xml.SerializeUtils;
import au.com.isell.remote.common.model.Pair;

/**
 * invoke from dao
 * 
 * @author frankw 18/01/2012
 */
public class DAOSupport extends ApplicationObjectSupport {
	private static ObjectMapper mapper=new ObjectMapper();
	static{
		SerializeUtils.configObjectMapper(mapper);
//		mapper.setDateFormat(buildDateFormat());
	}
	
	
//	private static DateFormat buildDateFormat() {
//		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
//		format.setTimeZone(TimeZone.getTimeZone("UTC"));
//		return format;
//	}
	
	public static ObjectMapper getMapper() {
		return mapper;
	}

	public static S3Manager s3Manager = S3Manager.getInstance();
	public static IndexHelper indexHelper = IndexHelper.getInstance();

	protected <T> T get(T object) {
		Reader in = null;
		try {
			InputStream input = s3Manager.getStream(S3Utils.getS3Key(object));
			if (input == null)
				return null;
			in = new InputStreamReader(new GZIPInputStream(input), "UTF-8");// JsonUtils.decode(in, object.getClass())
//			if (object instanceof AbstractModel) {
//				return (T) getGSon().fromJson(in, AbstractModel.class);
//			} else {
//				return (T) getGSon().fromJson(in, object.getClass());
//			}
			return (T)mapper.readValue(in, object.getClass());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}

		}
	}

	@SuppressWarnings("unchecked")
	protected <T> void save(T... objects) {
		for (T obj : objects) {
			saveSingleItem(obj);
			indexHelper.indexValues(obj);
			if (AfterSaving.class.isAssignableFrom(obj.getClass())) {
				ApplicationContext applicationContext = getApplicationContext();
				if (applicationContext != null) {
					((AfterSaving) obj).operate(applicationContext);
				}
			}
		}
	}

	public static <T> void saveSingleItem(T obj) {
		ByteArrayOutputStream out = null;
		Writer writer = null;
		try {
			out = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(new GZIPOutputStream(out), "UTF-8");
			mapper.writeValue(writer, obj);
//			if (obj instanceof AbstractModel) {
//				getGSon().toJson(obj, AbstractModel.class, writer);
//			} else {
//				getGSon().toJson(obj, writer);
//			}
			writer.close();
			Map<String, String> meta = new HashMap<String, String>();
			meta.put("Content-Encoding", "gzip");
			meta.put(S3Manager.META_CONTENTTYPE, "application/json");
			s3Manager.putObject(S3Utils.getS3Key(obj), out.toByteArray(), meta, null);

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected <T> void delete(T... object) {
		indexHelper.deleteByObj(object);
		String[] keys = new String[object.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = S3Utils.getS3Key(object[i]);
		}
		s3Manager.deleteObjects(keys);
	}

	/**
	 * query keys from index ,then fetch data from s3
	 */
	protected <T> List<T> query(Class<T> cls, QueryBuilder queryBuilder, Pair<String, Boolean>[] sorts) {
		Pair<Long, List<T>> keys = indexHelper.queryBeans(cls, new QueryParams(queryBuilder, sorts));
		if (keys == null)
			return null;
		List<T> r = new ArrayList<T>();
		for (T key : keys.getValue()) {
			T item = get(key);
			if (item != null)
				r.add(item);
		}
		return r;
	}

	//
	// private static class TimeZoneDeserializer implements JsonDeserializer<TimeZone> {
	// @Override
	// public TimeZone deserialize(JsonElement json, Type typeOfT,
	// JsonDeserializationContext context) throws JsonParseException {
	// return TimeZone.getTimeZone(json.getAsString());
	// }
	// }
	//
	// private static class TimeZoneSerializer implements JsonSerializer<TimeZone> {
	// @Override
	// public JsonElement serialize(TimeZone src, Type typeOfSrc, JsonSerializationContext context) {
	// return new JsonPrimitive(src.getID());
	// }
	// }

//	private static class AbstractModelDeserializer implements JsonDeserializer<AbstractModel> {
//		@Override
//		public AbstractModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//			JsonObject obj = json.getAsJsonObject();
//			JsonElement clazz = obj.get("clazzType");
//			if (clazz != null) {
//				try {
//					Class<?> clz = SerializeUtils.getClassByAlias(clazz.getAsString());
//					if (clz == null)
//						clz = Class.forName(clazz.getAsString());
//					return context.deserialize(json, clz);
//				} catch (ClassNotFoundException e) {
//					throw new RuntimeException("Class " + clazz.getAsString() + " doesn't exists.");
//				}
//			} else {
//				return context.deserialize(json, typeOfT);
//			}
//		}
//	}
//
//	private static class AbstractModelSerializer implements JsonSerializer<AbstractModel> {
//		@Override
//		public JsonElement serialize(AbstractModel src, Type typeOfSrc, JsonSerializationContext context) {
//			Class<?> clz = src.getClass();
//			if (clz.isAnnotationPresent(XStreamAlias.class)) {
//				XStreamAlias alias = clz.getAnnotation(XStreamAlias.class);
//				src.setClazzType(alias.value());
//			} else {
//				src.setClazzType(clz.getName());
//			}
//			return context.serialize(src, clz);
//		}
//	}

}
