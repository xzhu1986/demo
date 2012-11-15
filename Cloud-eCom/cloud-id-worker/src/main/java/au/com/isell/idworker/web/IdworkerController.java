package au.com.isell.idworker.web;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.util.Hashtable;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.idworker.IDWorker;
import au.com.isell.idworker.IndexId;
import au.com.isell.idworker.RegKey;
import au.com.isell.idworker.RegKeyWorker;

@Controller
@RequestMapping("/idgen")
public class IdworkerController implements ApplicationContextAware {
	
	private Map<String, IDWorker> workers = new Hashtable<String, IDWorker>();
	private RegKeyWorker regKeyWorker;
	private ApplicationContext ctx;
	
	public IdworkerController() {
		System.out.println("IdworkerController");
		IndexHelper.getInstance().registerType(IndexId.class);
		IndexHelper.getInstance().registerType(RegKey.class);
	}

	@RequestMapping(value = "/int/{type}/get", method = RequestMethod.GET)
	public Map<String, Object> generateIntId(@PathVariable("type") String type, 
			@RequestParam(value="count", required=false) Integer count, ModelMap map) {
		IDWorker worker = workers.get(type);
		if (worker == null) {
			worker = new IDWorker(type, ctx);
			workers.put(type, worker);
		}
		if (count == null) count = 1;
		map.put("id", worker.nextId(count.intValue()));
		return map;
	}
	
	@RequestMapping(value = "/int/{type}/set", method = RequestMethod.GET)
	public Map<String, Object> setIntId(@PathVariable("type") String type, 
			@RequestParam(value="number") Integer number, ModelMap map) {
		IDWorker worker = workers.get(type);
		if (worker == null) {
			worker = new IDWorker(type, ctx);
			workers.put(type, worker);
		}
		worker.setLastNumber(number);
		map.put("lastNumber", number);
		return map;
	}
	@RequestMapping(value = "/regkey/{type}/set", method = RequestMethod.GET)
	public Map<String, Object> setRegKey(@PathVariable("type") String type, @RequestParam("serialNo") int serialNo,
			@RequestParam(value="key") String key, ModelMap map) {
		regKeyWorker.setKey(type, serialNo, key);
		map.put("serialNo", serialNo);
		map.put("key", key);
		return map;
	}
	@RequestMapping(value = "/regkey/{type}/get", method = RequestMethod.GET)
	public Map<String, Object> getRegKey(@PathVariable("type") String type, @RequestParam("serialNo") int serialNo, ModelMap map) {
		String key = regKeyWorker.generateRegKey(type, serialNo);
		map.put("serialNo", serialNo);
		map.put("key", key);
		return map;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		ctx = applicationContext;
		QueryBuilder builder = matchAllQuery();
		String[] types = IndexHelper.getInstance().queryKeys(IndexId.class, new QueryParams(builder, null).withPaging(1, 500).withRoutings(new String[0]));
		for (String type : types) {
			workers.put(type, new IDWorker(type, ctx));
		}
		regKeyWorker = new RegKeyWorker(ctx);
	}
}