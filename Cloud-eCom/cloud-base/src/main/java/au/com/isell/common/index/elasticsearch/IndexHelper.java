package au.com.isell.common.index.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import au.com.isell.common.index.IndexException;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexRouting;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.util.ClassFilter;
import au.com.isell.common.util.ClassMatcher;
import au.com.isell.common.util.UTCDateUtils;
import au.com.isell.remote.common.model.Pair;

public class IndexHelper {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(IndexHelper.class);
	public static final String ELASTICSEARCH_ID = "elasticsearch.id";
	private static IndexHelper instance;
	private Client client;

	public static void scanIndices() {
		logger.info("scan and register types with [ISellIndex]");
		ClassFilter.doFilter("au.com.isell", new ClassMatcher() {
			@Override
			public void match(Class<?> cls) {
				if (cls.isAnnotationPresent(ISellIndex.class)) {
					IndexHelper.getInstance().registerType(cls);
				}
			}
		});
	}

	private IndexHelper() {
	}

	private void connect() {
		try {
			Properties bundle = new Properties();
			bundle.load(IndexHelper.class.getResourceAsStream("/settings.properties"));
			String env = bundle.getProperty("environment");
			if ("ec2".equals(env)) {
				connectEC2(bundle);
			} else if ("remote".equals(env)) {
				connectEC2Remote(bundle);
			} else {
				NodeBuilder builder = nodeBuilder();
				client = builder.clusterName(bundle.getProperty("search.cluster")).client(true).node().client();
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	private void connectEC2(Properties bundle) throws IOException {
		Builder settings = ImmutableSettings.settingsBuilder();
		settings.put("discovery.type", "ec2");
		Properties awsBundle = new Properties();
		awsBundle.load(IndexHelper.class.getResourceAsStream("/AwsCredentials.properties"));
		settings.put("cloud.aws.region", bundle.getProperty("aws.region"));
		settings.put("cloud.aws.access_key", awsBundle.getProperty("accessKey"));
		settings.put("cloud.aws.secret_key", awsBundle.getProperty("secretKey"));
		settings.put("discovery.ec2.groups", bundle.getProperty("search.group"));
		NodeBuilder builder = nodeBuilder().settings(settings).clusterName(bundle.getProperty("search.cluster"));
		if ("true".equals(bundle.getProperty("search.client"))) {
			builder.client(true);
		}
		Node node = builder.node();
		client = node.client();
		checkHealth();
	}

	public void checkHealth() {
		ClusterHealthResponse resp = client.admin().cluster().health(new ClusterHealthRequest()).actionGet();
		while (resp.status() == ClusterHealthStatus.RED) {
			try {
				Thread.sleep(5000);
				resp = client.admin().cluster().health(new ClusterHealthRequest()).actionGet();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void connectEC2Remote(Properties bundle) throws IOException {
		String[] gateways = bundle.getProperty("search.gateway").split(",");
		Builder settings = ImmutableSettings.settingsBuilder().put("cluster.name", bundle.getProperty("search.cluster"));
		TransportClient c = new TransportClient(settings);
		for (String gateway : gateways) {
			c.addTransportAddress(new InetSocketTransportAddress(gateway, 9300));
		}
		client = c;
		checkHealth();
	}

	public synchronized static IndexHelper getInstance() {
		if (instance == null) {
			while (true) {
				try {
					instance = new IndexHelper();
					break;
				} catch (Exception ex) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (instance.client == null)
			instance.connect();
		return instance;
	}

	public void registerType(Class<?> clazz) throws IndexException {
		if (!clazz.isAnnotationPresent(ISellIndex.class))
			return;
		ISellIndex indexDef = clazz.getAnnotation(ISellIndex.class);
		if (indexDef.type().equals(""))
			return; // type is "" is only for search
		System.out.println("client.admin():" + client.admin());
		System.out.println("client.admin().indices():" + client.admin().indices());
		IndicesExistsResponse respExist = client.admin().indices().prepareExists(indexDef.name()).execute().actionGet();
		if (!respExist.isExists()) {
			CreateIndexResponse respCreate = client.admin().indices().prepareCreate(indexDef.name()).execute().actionGet();
			if (!respCreate.acknowledged())
				throw new IndexException("Register type " + clazz.getName() + " failed because of request hasn't been acknowledged.");
		}
		PutMappingRequestBuilder builder = client.admin().indices().preparePutMapping(indexDef.name()).setType(indexDef.type());
		Map<String, Object> typeMap = new HashMap<String, Object>();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("properties", generatePropertiesMapping(clazz, null, indexDef.checkParents()));
		typeMap.put(indexDef.type(), properties);
		System.out.println(typeMap);
		PutMappingResponse respPut = builder.setSource(typeMap).execute().actionGet();
		if (respPut.acknowledged())
			return;
		throw new IndexException("Register type " + clazz.getName() + " failed because of request hasn't been acknowledged.");
	}

	private Map<String, Object> generatePropertiesMapping(Class<?> clazz, String parentField, boolean checkSuperClass) {
		Map<String, Object> propertiesMap = new HashMap<String, Object>();
		try {
			BeanInfo info = Introspector.getBeanInfo(clazz);
			for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
				Method method = prop.getReadMethod();
				if (method == null)
					continue;
				if (method.isAnnotationPresent(ISellIndexValue.class)) {
					ISellIndexValue value = method.getAnnotation(ISellIndexValue.class);
					String name = value.name();
					if (name.length() == 0)
						name = prop.getName();
					boolean store = value.store();
					Map<String, Object> setting = new HashMap<String, Object>();
					propertiesMap.put(name, setting);
					Map<String, Object> type = getFieldType(method.getReturnType(), name, checkSuperClass);
					setting.put("store", store ? "yes" : "no");
					setting.put("index", "not_analyzed");
					if ("date".equals(type.get("type")))
						setting.put("format", UTCDateUtils.storeDataFormat);
					setting.putAll(type);
					if (value.wildcard()) {
						setting = new HashMap<String, Object>();
						propertiesMap.put(name + "_wildcard", setting);
						Map<String, Object> dataType = new HashMap<String, Object>();
						dataType.put("type", "string");
						setting.put("store", "no");
						setting.put("index", "analyzed");
						setting.put("search_analyzer", "whitespace");
						setting.put("index_analyzer", "whitespace");
						if ("date".equals(type.get("type")))
							setting.put("format", UTCDateUtils.storeDataFormat);
						setting.putAll(dataType);
					}
					if ("analyzed".equals(value.index())) {
						setting = new HashMap<String, Object>();
						propertiesMap.put(name + "_analyzed", setting);
						Map<String, Object> dataType = new HashMap<String, Object>();
						dataType.put("type", "string");
						setting.put("store", "no");
						setting.put("index", "analyzed");
						if (value.analyzer().length() > 0) {
							setting.put("search_analyzer", value.analyzer());
							setting.put("index_analyzer", value.analyzer());
						}
						setting.putAll(dataType);
					}
				} else if (method.isAnnotationPresent(ISellIndexKey.class)) {
					ISellIndexKey value = method.getAnnotation(ISellIndexKey.class);
					String name = value.name();
					if (name.length() == 0)
						name = prop.getName();
					if (value.wildcard()) {
						Map<String, Object> setting = new HashMap<String, Object>();
						propertiesMap.put(name + "_wildcard", setting);
						Map<String, Object> type = new HashMap<String, Object>();
						type.put("type", "string");
						setting.put("index", "analyzed");
						setting.put("search_analyzer", "whitespace");
						setting.put("index_analyzer", "whitespace");
						if ("date".equals(type.get("type")))
							setting.put("format", UTCDateUtils.storeDataFormat);
						setting.putAll(type);
					}
				}
			}
			Class<?> superClass = clazz.getSuperclass();
			if (checkSuperClass && superClass != null)
				propertiesMap.putAll(generatePropertiesMapping(superClass, null, checkSuperClass));
		} catch (IntrospectionException e) {
			throw new RuntimeException("Register " + clazz.getName() + " error: " + e.getMessage(), e);
		}
		return propertiesMap;
	}

	private Map<String, Object> getFieldType(Class<?> type, String name, boolean checkSuperClass) {
		Map<String, Object> typeMap = new HashMap<String, Object>();
		if (String.class.isAssignableFrom(type)) {
			typeMap.put("type", "string");
		} else if (Integer.class.isAssignableFrom(type) || type.equals(Integer.TYPE)) {
			typeMap.put("type", "integer");
		} else if (Long.class.isAssignableFrom(type) || type.equals(Long.TYPE)) {
			typeMap.put("type", "long");
		} else if (Float.class.isAssignableFrom(type) || type.equals(Float.TYPE)) {
			typeMap.put("type", "float");
		} else if (Double.class.isAssignableFrom(type) || type.equals(Double.TYPE)) {
			typeMap.put("type", "double");
		} else if (Boolean.class.isAssignableFrom(type) || type.equals(Boolean.TYPE)) {
			typeMap.put("type", "boolean");
		} else if (Date.class.isAssignableFrom(type)) {
			typeMap.put("type", "date");
		} else if (UUID.class.isAssignableFrom(type)) {
			typeMap.put("type", "string");
		} else if (type.isArray()) {
			typeMap.putAll(getFieldType(type.getComponentType(), name, checkSuperClass));
		} else if (type.isEnum()) {
			typeMap.put("type", "integer");
		} else if (BigDecimal.class.equals(type)) {
			typeMap.put("type", "double");
		} else {
			// typeMap.put("type", "object");
			// typeMap.put("properties", generatePropertiesMapping(type, null,
			// checkSuperClass));
		}
		return typeMap;
	}

	private boolean isNest(Class<?> type) {
		if (String.class.isAssignableFrom(type)) {
			return false;
		} else if (Integer.class.isAssignableFrom(type) || type.equals(Integer.TYPE)) {
			return false;
		} else if (Long.class.isAssignableFrom(type) || type.equals(Long.TYPE)) {
			return false;
		} else if (Float.class.isAssignableFrom(type) || type.equals(Float.TYPE)) {
			return false;
		} else if (Double.class.isAssignableFrom(type) || type.equals(Double.TYPE)) {
			return false;
		} else if (Boolean.class.isAssignableFrom(type) || type.equals(Boolean.TYPE)) {
			return false;
		} else if (Date.class.isAssignableFrom(type)) {
			return false;
		} else if (UUID.class.isAssignableFrom(type)) {
			return false;
		} else if (type.isArray()) {
			return false;
		} else if (type.isEnum()) {
			return false;
		} else if (BigDecimal.class.equals(type)) {
			return false;
		} else {
			return true;
		}
	}

	public <T> void indexValues(T... objs) throws IndexException {
		if (objs == null)
			return;
		BulkRequestBuilder builder = client.prepareBulk();
		List<String> indices = new ArrayList<String>();
		boolean hasData = false;
		for (Object obj : objs) {
			if (obj == null)
				continue;
			ISellIndex indexDef = getIndexDef(obj.getClass());
			if (indexDef == null || indexDef.manual())
				continue;
			if (indexDef.type().equals(""))
				continue;
			Pair<String[], Map<String, Object>> indexData = getIndexValues(obj);
			if (indexData == null)
				continue;
			else
				hasData = true;
			if (!indices.contains(indexDef.name()))
				indices.add(indexDef.name());
			String[] index = indexData.getKey();
			IndexRequestBuilder ib = new IndexRequestBuilder(client, index[0]);
			if (index[3] != null)
				ib.setRouting(index[3]);
			ib.setType(index[1]);
			ib.setId(index[2]);
			ib.setSource(indexData.getValue());
			builder.add(ib);
		}
		if (hasData) {
			BulkResponse resp = builder.execute().actionGet();
			if (resp.hasFailures()) {
				throw new IndexException(resp.buildFailureMessage());
			}
			client.admin().indices().flush(new FlushRequest(indices.toArray(new String[indices.size()]))).actionGet();
		} else
			return;
	}

	public void indexValues(String index, String type, Map<String, Object>... datas) {
		BulkRequestBuilder builder = client.prepareBulk();
		boolean hasData = false;
		for (Map<String, Object> data : datas) {
			IndexRequestBuilder ib = new IndexRequestBuilder(client, index);
			ib.setType(type);
			ib.setId(data.remove(IndexHelper.ELASTICSEARCH_ID).toString());
			ib.setSource(data);
		}
		if (hasData) {
			BulkResponse resp = builder.execute().actionGet();
			if (resp.hasFailures()) {
				throw new IndexException(resp.buildFailureMessage());
			}
			client.admin().indices().flush(new FlushRequest(index)).actionGet();
		} else
			return;
	}

	public <T> void indexValuesWithRouting(String routing, T... objs) throws IndexException {
		BulkRequestBuilder builder = client.prepareBulk();
		List<String> indices = new ArrayList<String>();
		boolean hasData = false;
		for (Object obj : objs) {
			if (objs == null)
				continue;
			Pair<String[], Map<String, Object>> indexData = getIndexValues(obj);
			if (indexData == null)
				continue;
			else
				hasData = true;
			String[] index = indexData.getKey();
			if (!indices.contains(index[0]))
				indices.add(index[0]);
			IndexRequestBuilder ib = new IndexRequestBuilder(client, index[0]);
			if (routing != null)
				ib.setRouting(routing);
			ib.setType(index[1]);
			ib.setId(index[2]);
			ib.setSource(indexData.getValue());
			builder.add(ib);
		}
		if (hasData) {
			BulkResponse resp = builder.execute().actionGet();
			if (resp.hasFailures()) {
				throw new IndexException(resp.buildFailureMessage());
			}
			client.admin().indices().flush(new FlushRequest(indices.toArray(new String[indices.size()]))).actionGet();
		} else
			return;
	}

	public <T> void deleteByObj(T... objs) throws IndexException {
		BulkRequestBuilder builder = client.prepareBulk();
		boolean hasData = false;
		for (Object obj : objs) {
			if (objs == null)
				continue;
			Class<?> clazz = obj.getClass();
			ISellIndex indexDef = getIndexDef(clazz);
			if (indexDef == null)
				continue;
			else if (indexDef.type().equals(""))
				continue;
			else
				hasData = true;
			String[] index = new String[4];
			index[0] = indexDef.name();
			index[1] = indexDef.type();
			try {
				Class<?> c = clazz;
				while (c != null) {
					BeanInfo info = Introspector.getBeanInfo(c);
					for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
						Method method = prop.getReadMethod();
						if (method.isAnnotationPresent(ISellIndexKey.class)) {
							Object key = method.invoke(obj);
							if (key == null)
								throw new IllegalArgumentException("No key of object " + obj.toString());
							index[2] = formatValue(key, false);
						} else if (method.isAnnotationPresent(ISellIndexRouting.class)) {
							Object valValue = method.invoke(obj);
							if (valValue == null)
								throw new IllegalArgumentException("No value for " + obj.toString() + "." + prop.getName());
							index[3] = formatValue(valValue, false);
						}
					}
					c = c.getSuperclass();
				}
			} catch (IntrospectionException e) {
				throw new RuntimeException("Register " + clazz.getName() + " error: " + e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Class define error: can't get key value " + clazz, e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Class define error: can't get key value " + clazz, e);
			}
			DeleteRequestBuilder db = new DeleteRequestBuilder(client, index[0]);
			if (index[3] != null)
				db.setRouting(index[3]);
			db.setType(index[1]);
			db.setId(index[2]);
			builder.add(db);
		}
		if (hasData) {
			BulkResponse resp = builder.execute().actionGet();
			if (resp.hasFailures()) {
				throw new IndexException(resp.buildFailureMessage());
			}
		} else
			return;
	}

	private String formatValue(Object value, boolean lowercase) {
		if (value.getClass().isEnum() || value.getClass().getSuperclass().isEnum()) {
			return String.valueOf(((Enum<?>) value).ordinal());
		}
		String val = null;
		if (value instanceof BigDecimal) {
			val = ((BigDecimal) value).toPlainString();
		} else if (value instanceof Date) {
			val = UTCDateUtils.format((Date) value);
		} else {
			val = value.toString();
		}
		if (lowercase)
			val = val.toLowerCase();
		return val;
	}

	public void deleteByKeys(String routing, Pair<Class<?>, String>... keys) throws IndexException {
		BulkRequestBuilder builder = client.prepareBulk();
		boolean hasData = false;
		for (Pair<Class<?>, String> key : keys) {
			ISellIndex indexDef = getIndexDef(key.getKey());
			if (indexDef == null)
				continue;
			else
				hasData = true;
			String[] index = new String[4];
			index[0] = indexDef.name();
			index[1] = indexDef.type();
			index[2] = key.getValue();
			DeleteRequestBuilder db = new DeleteRequestBuilder(client, index[0]);
			if (routing != null)
				db.setRouting(routing);
			db.setType(index[1]);
			db.setId(index[2]);
			builder.add(db);
		}
		if (hasData) {
			BulkResponse resp = builder.execute().actionGet();
			if (resp.hasFailures()) {
				throw new IndexException(resp.buildFailureMessage());
			}
		} else
			return;
	}

	public Pair<String[], Map<String, Object>> getIndexValues(Object obj) {
		Class<?> clazz = obj.getClass();
		ISellIndex indexDef = getIndexDef(clazz);
		if (indexDef == null)
			return null;
		String[] index = new String[4];
		index[0] = indexDef.name();
		index[1] = indexDef.type();
		Map<String, Object> values = new HashMap<String, Object>();
		try {
			checkIndexFields(obj, clazz, index, values, indexDef.checkParents(), null);
		} catch (IntrospectionException e) {
			throw new RuntimeException("Register " + clazz.getName() + " error: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Class define error: can't get key value " + clazz, e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Class define error: can't get key value " + clazz, e);
		}
		return new Pair<String[], Map<String, Object>>(index, values);
	}

	private String makeWildcardValue(String keyword) {
		StringBuilder sb = new StringBuilder();
		boolean start = true;
		for (char ch : keyword.toCharArray()) {
			if (start)
				start = false;
			else
				sb.append(' ');
			if (ch == ' ')
				ch = '_';
			sb.append(Character.toLowerCase(ch));
		}
		return sb.toString() + " $ " + sb.toString();
	}

	/**
	 * 
	 * @param obj
	 * @param clazz
	 * @param index
	 *            {index_name, index_type, index_id (not set), index_routing (not set)}
	 * @param values
	 * @param checkSupperClass
	 *            flag to tell if needs to check annotations in supplier classes
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void checkIndexFields(Object obj, Class<?> clazz, String[] index, Map<String, Object> values, boolean checkSupperClass, String parentField)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		BeanInfo info = Introspector.getBeanInfo(clazz);
		for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
			Method method = prop.getReadMethod();
			if (method == null)
				continue;
			if (method.isAnnotationPresent(ISellIndexKey.class)) {
				Object key = method.invoke(obj);
				if (key == null)
					throw new IllegalArgumentException("No key of object " + obj.toString());
				index[2] = formatValue(key, false);
				ISellIndexKey value = method.getAnnotation(ISellIndexKey.class);
				if (value.wildcard()) {
					String name = value.name();
					if (name.length() == 0)
						name = prop.getName();
					if (parentField != null)
						name = parentField + '.' + name;
					values.put(name + "_wildcard", makeWildcardValue(formatValue(key, true)));
				}
			} else if (method.isAnnotationPresent(ISellIndexValue.class)) {
				ISellIndexValue value = method.getAnnotation(ISellIndexValue.class);
				String name = value.name();
				if (name.length() == 0)
					name = prop.getName();
				if (parentField != null)
					name = parentField + '.' + name;
				Object valValue = method.invoke(obj);
				if (valValue == null)
					continue;
				if (isNest(method.getReturnType())) {
					checkIndexFields(valValue, valValue.getClass(), index, values, checkSupperClass, name);
				} else {
					values.put(name, formatValue(valValue, value.lowercase()));
					if (value.wildcard()) {
						values.put(name + "_wildcard", makeWildcardValue(formatValue(valValue, value.lowercase())));
					}
					if ("analyzed".equals(value.index())) {
						values.put(name + "_analyzed", formatValue(valValue, true));
					}
				}
			} else if (method.isAnnotationPresent(ISellIndexRouting.class)) {
				Object valValue = method.invoke(obj);
				if (valValue == null)
					throw new IllegalArgumentException("No value for " + obj.toString() + "." + prop.getName());
				index[3] = formatValue(valValue, false);
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (checkSupperClass && superClass != null)
			checkIndexFields(obj, superClass, index, values, checkSupperClass, parentField);
	}

	private static ISellIndex getIndexDef(Class<?> clazz) {
		Class<?> c = clazz;
		while (!c.isAnnotationPresent(ISellIndex.class)) {
			c = c.getSuperclass();
			if (c == null)
				return null;
		}
		ISellIndex indexDef = c.getAnnotation(ISellIndex.class);
		return indexDef;
	}

	public static String getIndexName(Class<?> clazz) {
		ISellIndex iSellIndex = getIndexDef(clazz);
		if (iSellIndex != null && StringUtils.isNotEmpty(iSellIndex.name())) {
			return iSellIndex.name();
		}
		throw new RuntimeException(clazz.toString() + " is not annotated with isell index ? ");
	}

	public Map<String, int[]> deleteByQuery(Class<?> clazz, QueryBuilder qb, String... routing) throws IndexException {
		ISellIndex indexDef = getIndexDef(clazz);
		if (indexDef == null)
			return new HashMap<String, int[]>();
		DeleteByQueryRequestBuilder srb = client.prepareDeleteByQuery(indexDef.name());
		if (!indexDef.type().equals(""))
			srb = srb.setTypes(indexDef.type());
		if (routing != null && routing.length > 0)
			srb.setRouting(routing);
		DeleteByQueryResponse resp = srb.setQuery(qb).execute().actionGet();
		Map<String, int[]> results = new HashMap<String, int[]>();
		for (Map.Entry<String, IndexDeleteByQueryResponse> index : resp.indices().entrySet()) {
			IndexDeleteByQueryResponse result = index.getValue();
			int failedShards = result.failedShards();
			int successfulShards = result.successfulShards();
			results.put(result.getIndex(), new int[] { successfulShards, failedShards });
		}
		return results;
	}

	public String[] queryKeys(Class clazz, QueryParams queryParams) {
		ISellIndex indexDef = getIndexDef(clazz);
		if (indexDef == null)
			return null;
		SearchRequestBuilder srb = client.prepareSearch(indexDef.name()).setSearchType(SearchType.QUERY_THEN_FETCH);
		if (!indexDef.type().equals(""))
			srb = srb.setTypes(indexDef.type());
		if (queryParams.getRoutings() != null && queryParams.getRoutings().length > 0)
			srb.setRouting(queryParams.getRoutings());
		if (queryParams.getSorts() != null) {
			for (Pair<String, Boolean> sort : queryParams.getSorts()) {
				srb.addSort(sort.getKey(), sort.getValue() ? SortOrder.ASC : SortOrder.DESC);
			}
		}
		if (queryParams.getFilterBuilder() != null) srb.setFilter(queryParams.getFilterBuilder());
		if (queryParams.getPageNo() == null && queryParams.getPageSize() == null) {
			srb.setFrom(0).setSize(1000);
		} else {
			srb.setFrom((queryParams.getPageNo() - 1) * queryParams.getPageSize()).setSize(queryParams.getPageSize());
		}

		SearchResponse resp = srb.setQuery(queryParams.getQb()).execute().actionGet();
		SearchHit[] hits = resp.getHits().hits();
		String[] results = new String[hits.length];
		for (int i = 0; i < hits.length; i++) {
			results[i] = hits[i].getId();
		}
		return results;
	}

	public Iterable<String> iterateKeys(Class clazz, QueryParams queryParams) {
		return new KeyIterator(clazz, queryParams);
	}

	public <T> Iterable iterateBeans(Class<T> clazz, QueryParams queryParams) {
		return new BeanIterator(clazz, queryParams);
	}
	
	public  Iterable<Map> iterateMaps(Class<?> clazz, QueryParams queryParams) {
		return new MapIterator(clazz, queryParams);
	}

	/**
	 * query beans from index <br />
	 * warning : if no paging params,will return all data which may cause out of memory exception <br />
	 */
	public <T> Pair<Long, List<T>> queryBeans(Class<T> clazz, QueryParams qParams) {
		Pair<Long, List<T>> r = null;
		if (qParams.getPageNo() == null && qParams.getPageSize() == null) {
			// r = query(clazz, qParams.getQb(), qParams.getSorts(), -1, -1, new BeanDataExtractor<T>(clazz));
			List<T> list = new ArrayList<T>();
			Iterable<T> iterable = iterateBeans(clazz, qParams);
			for (T t : iterable) {
				list.add(t);
			}
			r = new Pair(Long.valueOf(list.size()), list);
		} else {
			Assert.isTrue(qParams.getPageNo() != null && qParams.getPageNo() > 0, "page no must greater than 0");
			Assert.isTrue(qParams.getPageSize() != null && qParams.getPageSize() > 0, "page size must greater thant 0");
			r = query(clazz, qParams.getQb(), qParams.getFilterBuilder(), qParams.getSorts(), (qParams.getPageNo() - 1) * qParams.getPageSize(),
					qParams.getPageSize(), new BeanDataExtractor<T>(clazz));
		}
		return r;
	}

	public Pair<Long, List<Map>> queryMaps(Class<?> clazz, QueryParams qParams) {

		Pair<Long, List<Map>> r = null;
		if (qParams.getPageNo() == null && qParams.getPageSize() == null) {
			// r = query(clazz, qParams.getQb(), qParams.getSorts(), -1, -1, new BeanDataExtractor<T>(clazz));
			List<Map> list = new ArrayList<Map>();
			Iterable<Map> iterable = iterateBeans(clazz, qParams);
			for (Map t : iterable) {
				list.add(t);
			}
			r = new Pair(Long.valueOf(list.size()), list);
		} else {
			Assert.isTrue(qParams.getPageNo() != null && qParams.getPageNo() > 0, "page no must greater than 0");
			Assert.isTrue(qParams.getPageSize() != null && qParams.getPageSize() > 0, "page size must greater thant 0");
			r = query(clazz, qParams.getQb(), qParams.getFilterBuilder(), qParams.getSorts(), (qParams.getPageNo() - 1) * qParams.getPageSize(),
					qParams.getPageSize(), new MapDataExtractor<Map>());
		}
		return r;
	}

	private <T> Pair<Long, List<T>> query(Class<?> clazz, QueryBuilder qb, FilterBuilder fb, Pair<String, Boolean>[] sorts, int from,
			int size, DataExtractor dataExtractor, String... routing) {
		ISellIndex indexDef = getIndexDef(clazz);
		if (indexDef == null)
			return null;
		Long total = 0l;
		List<T> data = null;
		SearchRequestBuilder srb = client.prepareSearch(indexDef.name()).setSearchType(SearchType.QUERY_THEN_FETCH);
		if (!indexDef.type().equals(""))
			srb = srb.setTypes(indexDef.type());
		if (routing != null && routing.length > 0)
			srb.setRouting(routing);
		SearchResponse resp = null;
		try {
			if (sorts != null) {
				for (Pair<String, Boolean> sort : sorts) {
					srb.addSort(sort.getKey(), sort.getValue() ? SortOrder.ASC : SortOrder.DESC);
				}
			}
			if (fb != null) srb = srb.setFilter(fb);
			// query data
			if (from >= 0 && size > 0) {
				srb = srb.setQuery(qb);
				resp = srb.execute().actionGet();
				total = resp.getHits().getTotalHits();
				srb.setFrom(from).setSize(size);
				resp = srb.setQuery(qb).execute().actionGet();

			} else {
				srb.setFrom(0).setSize(500);
				srb = srb.setQuery(qb);
				resp = srb.execute().actionGet();
				total = resp.getHits().getTotalHits();
			}
			data = dataExtractor.extractData(resp);
//			logger.info("query content: "+srb+"Total count: "+total+", page count: "+resp.getHits().hits().length+", extracted data count: "+data.size());
			return new Pair<Long, List<T>>(total, data);
		} catch (IndexMissingException e) {
			return new Pair<Long, List<T>>(0l, new ArrayList<T>());
		}
	}

	public Facets facet(Class<?> clazz, AbstractFacetBuilder[] facetBuilders, QueryBuilder qb, String... routing) {
		ISellIndex indexDef = getIndexDef(clazz);
		if (indexDef == null)
			return null;

		SearchRequestBuilder srb = client.prepareSearch(indexDef.name()).setSearchType(SearchType.QUERY_THEN_FETCH);
		if (!indexDef.type().equals(""))
			srb = srb.setTypes(indexDef.type());
		if (routing != null && routing.length > 0)
			srb.setRouting(routing);

		if (qb != null)
			srb.setQuery(qb);
		if (facetBuilders != null) {
			for (AbstractFacetBuilder facetBuilder : facetBuilders) {
				srb.addFacet(facetBuilder);
			}
		}

		try {
			SearchResponse resp = srb.execute().actionGet();
			return resp.getFacets();
		} catch (IndexMissingException e) {
			return null;
		}
	}

	public long count(Class<?> clazz, QueryBuilder qb, String... routing) {
		ISellIndex indexDef = getIndexDef(clazz);
		Assert.notNull(indexDef);

		SearchRequestBuilder srb = client.prepareSearch(indexDef.name()).setSearchType(SearchType.QUERY_THEN_FETCH);
		if (!indexDef.type().equals(""))
			srb = srb.setTypes(indexDef.type());
		srb.setSearchType(SearchType.COUNT);
		if (routing != null && routing.length > 0)
			srb.setRouting(routing);

		if (qb != null)
			srb.setQuery(qb);

		SearchResponse resp = srb.execute().actionGet();
		return resp.getHits().getTotalHits();
	}

	public GetResponse getRecord(Class<?> clazz, String key, String... fields) {
		ISellIndex indexDef = getIndexDef(clazz);
		GetResponse resp = client.prepareGet(indexDef.name(), indexDef.type(), key).setFields(fields).execute().actionGet();
		return resp;

	}

	public Client getClient() {
		return client;
	}

	public void close() throws Throwable {
		client.close();
		client = null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		client.close();
	}

}
