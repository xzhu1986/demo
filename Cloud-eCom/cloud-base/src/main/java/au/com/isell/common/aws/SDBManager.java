package au.com.isell.common.aws;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.com.isell.remote.common.model.Pair;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

public class SDBManager {
	private AmazonSimpleDB sdb;
	private static SDBManager instance;
	private static final int BATCH_SIZE = 25;
	
	private SDBManager() throws IOException {
		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
				SDBManager.class.getResourceAsStream("/AwsCredentials.properties")));
	}

	public static synchronized SDBManager getInstance() {
		if (instance == null) {
			try {
				instance = new SDBManager();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public void checkDomain(String domain) {
		ListDomainsResult domains = sdb.listDomains();
		for (String domainName : domains.getDomainNames()) {
			if (domainName.equals(domain)) return;
		}
		sdb.createDomain(new CreateDomainRequest(domain));
	}
	
	public void deleteDomain(String domain) {
		sdb.deleteDomain(new DeleteDomainRequest(domain));
	}

	public SelectResult select(String expression, String nextToken) {
		SelectRequest selectRequest = new SelectRequest(expression);
		if (nextToken != null) {
			selectRequest.setNextToken(nextToken);
		}
		return sdb.select(selectRequest);
	}
	public Pair<List<Item>, String> simpleSelect(String expression, String nextToken) {
		SelectResult result = select(expression, nextToken);
		return new Pair<List<Item>, String>(result.getItems(), result.getNextToken());
	}
	
	public List<Attribute> selectByName(String domain, String name) {
		GetAttributesRequest req = new GetAttributesRequest(domain, name);
		GetAttributesResult result = sdb.getAttributes(req);
		return result.getAttributes();
	}
	
	public Pair<String, Map<String, String>> parseItem(Item item) {
		List<Attribute> attrs = item.getAttributes();
		Map<String, String> attrMap = new HashMap<String, String>();
		for (Attribute attr : attrs) {
			attrMap.put(attr.getName(), attr.getValue());
		}
		return new Pair<String, Map<String, String>>(item.getName(), attrMap);
	}

	public void batchPutAttributes(String domain, List<ReplaceableItem> items) {
		if (items.size() <= BATCH_SIZE) {
			sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, items));
		} else {
			int start = 0;
			while (start < items.size()) {
				int end = start+BATCH_SIZE;
				if (end > items.size()) end = items.size();
				sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, items.subList(start, end)));
				start = end;
			}
		}
	}
	
	public String uniqueSelect(String query) {
		SelectResult result = sdb.select(new SelectRequest(query));
		List<Item> items = result.getItems();
		if (items.size() == 0) return null;
		else {
			Item item = items.get(0);
			return item.getName();
		}
	}
	
	public void update(String domain, String itemName, ReplaceableAttribute... attrs) {
		update(domain, itemName, Arrays.asList(attrs));
	}
	
	public void update(String domain, String itemName, List<ReplaceableAttribute> attributes) {
		PutAttributesRequest req = new PutAttributesRequest();
		req.setAttributes(attributes);
		req.setDomainName(domain);
		req.setItemName(itemName);
		
		sdb.putAttributes(req);
	}

	public Iterable<Item> selectAll(String query) {
		return new ResultIterable(sdb, query);
	}
	
	private static class ResultIterable implements Iterable<Item>, Iterator<Item>{
		
		private String query;
		private AmazonSimpleDB sdb;
		private Iterator<Item> items;
		private String nextToken = null;

		public ResultIterable(AmazonSimpleDB sdb, String query) {
			this.sdb = sdb;
			this.query = query;
			SelectRequest selectRequest = new SelectRequest(query);
			SelectResult result = sdb.select(selectRequest);
			items = result.getItems().iterator();
			nextToken = result.getNextToken();
		}

		@Override
		public boolean hasNext() {
			return nextToken!= null || items.hasNext();
		}

		@Override
		public Item next() {
			if (!items.hasNext()) {
				if (nextToken == null) return null;
				SelectRequest selectRequest = new SelectRequest(query);
				selectRequest.setNextToken(nextToken);
				SelectResult result = sdb.select(selectRequest);
				items = result.getItems().iterator();
				nextToken = result.getNextToken();
			}
			return items.next();
		}

		@Override
		public void remove() {
		}

		@Override
		public Iterator<Item> iterator() {
			return this;
		}
		
	}
}
