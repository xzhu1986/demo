package au.com.isell.common.index.sdb;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import au.com.isell.common.aws.SDBManager;
import au.com.isell.common.index.IndexException;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexRouting;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.remote.common.model.Pair;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectResult;

public class SDBHelper {
	
	private static SDBHelper instance;
	private SDBManager manager;
	
	private SDBHelper() {
		manager = SDBManager.getInstance();
	}
	
	public synchronized static SDBHelper getInstance() {
		if (instance == null) {
			instance = new SDBHelper();
		}
		return instance;
	}
	
	public void registerType(Class<?> clazz) throws IndexException {
		if (!clazz.isAnnotationPresent(ISellIndex.class)) return;
		ISellIndex indexDef = clazz.getAnnotation(ISellIndex.class);
		manager.checkDomain(indexDef.name());
	}

	public void indexValues(Object... objs) throws IndexException {
		if (objs == null || objs.length == 0) return;
		Map<String, List<ReplaceableItem>> itemMap = new HashMap<String, List<ReplaceableItem>>();
		boolean hasData = false;
		for (Object obj : objs) {
			if (obj == null) continue;
			ISellIndex indexDef = getIndexDef(obj.getClass());
			if (indexDef==null||indexDef.manual()) continue;
			Pair<String[], Map<String, Object>> indexData = getIndexValues(obj);
			if (indexData==null) continue;
			else hasData=true;
			if (!itemMap.containsKey(indexDef.name())) itemMap.put(indexDef.name(), new ArrayList<ReplaceableItem>());
			String[] index = indexData.getKey();
			ReplaceableItem item = new ReplaceableItem();
			item.setName(index[1]+"|"+index[2]);
			List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
			attrs.add(new ReplaceableAttribute("type", index[1], true));
			attrs.add(new ReplaceableAttribute("id", index[2], true));
			for (Map.Entry<String, Object> entry : indexData.getValue().entrySet()) {
				attrs.add(new ReplaceableAttribute(entry.getKey(), formatValue(entry.getValue(), false), true));
			}
			item.setAttributes(attrs);
			itemMap.get(indexDef.name()).add(item);
		}
		if (hasData) {
			for (Map.Entry<String, List<ReplaceableItem>> entry : itemMap.entrySet()) {
				manager.batchPutAttributes(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public Pair<String[], Map<String, Object>> getIndexValues(Object obj) {
		Class<?> clazz = obj.getClass();
		ISellIndex indexDef = getIndexDef(clazz);
		if (indexDef == null) return null;
		String[] index = new String[4];
		index[0] = indexDef.name();
		index[1] = indexDef.type();
		Map<String, Object> values = new HashMap<String, Object>();
		try {
			checkIndexFields(obj, clazz, index, values, indexDef.checkParents(), null);
		} catch (IntrospectionException e) {
			throw new RuntimeException("Register "+clazz.getName()+" error: "+e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Class define error: can't get key value "+clazz, e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Class define error: can't get key value "+clazz, e);
		}
		return new Pair<String[], Map<String, Object>>(index, values);
	}

	/**
	 * 
	 * @param obj
	 * @param clazz
	 * @param index   {index_name, index_type, index_id (not set), index_routing (not set)}
	 * @param values
	 * @param checkSupperClass   flag to tell if needs to check annotations in supplier classes
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void checkIndexFields(Object obj, Class<?> clazz, String[] index,
			Map<String, Object> values, boolean checkSupperClass, String parentField) throws IntrospectionException,
			IllegalAccessException, InvocationTargetException {
		BeanInfo info = Introspector.getBeanInfo(clazz);
		for (PropertyDescriptor prop : info.getPropertyDescriptors()) {
			Method method = prop.getReadMethod();
			if(method==null) continue;
			if (method.isAnnotationPresent(ISellIndexKey.class)) {
				Object key = method.invoke(obj);
				Assert.notNull(key, "No key of object "+obj.toString());
				index[2] = formatValue(key, false);
			} else if (method.isAnnotationPresent(ISellIndexValue.class)) {
				ISellIndexValue value = method.getAnnotation(ISellIndexValue.class);
				String name = value.name();
				if (name.length() == 0) name = prop.getName();
				if (parentField != null) name=parentField+'.'+name;
				Object valValue = method.invoke(obj);
				if (valValue == null) continue;
				if (isNest(valValue.getClass())) {
					checkIndexFields(valValue, valValue.getClass(), index, values, checkSupperClass, name);
				} else {
					values.put(name, formatValue(valValue, value.lowercase()));
				}
			} else if (method.isAnnotationPresent(ISellIndexRouting.class)) {
				Object valValue = method.invoke(obj);
				Assert.notNull(valValue, "No value for "+obj.toString()+"."+prop.getName());
				index[3] = formatValue(valValue, false);
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (checkSupperClass && superClass != null) checkIndexFields(obj, superClass, index, values, checkSupperClass, parentField);
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
		} else if (type.isArray()) {
			return false;
		} else if (type.isEnum()) {
			return false;
		} else {
			return true;
		}
	}
	
	private ISellIndex getIndexDef(Class<?> clazz) {
		Class<?> c = clazz;
		while (!c.isAnnotationPresent(ISellIndex.class)) {
			c = c.getSuperclass();
			if (c == null) return null;
		}
		ISellIndex indexDef = c.getAnnotation(ISellIndex.class);
		return indexDef;
	}
	
	private String formatValue(Object value, boolean lowercase) {
		if (value.getClass().isEnum()) {
			return String.valueOf(((Enum<?>) value).ordinal());
		}
		String val = value.toString();
		if (lowercase) val = val.toLowerCase();
		return val;
	}

	public List<Map<String, String>> queryObjects(Class<?> clazz, String conditions) {
		ISellIndex indexDef = getIndexDef(clazz);
		if (indexDef == null) return null;
		StringBuilder sb = new StringBuilder("SELECT * FROM ");
		sb.append(indexDef.name()).append(" WHERE ").append(conditions);
		SelectResult selResult = null;
		String nextToken = null;
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		do {
			System.out.println(sb.toString());
			selResult = manager.select(sb.toString(), null);
			List<Item> items = selResult.getItems();
			for (Item item : items) {
				List<Attribute> attrs = item.getAttributes();
				Map<String, String> result = new HashMap<String, String>();
				for (Attribute attr : attrs) {
					result.put(attr.getName(), attr.getValue());
				}
				results.add(result);
			}
			nextToken = selResult.getNextToken();
		} while (nextToken != null);
		return results;
	} 
}
