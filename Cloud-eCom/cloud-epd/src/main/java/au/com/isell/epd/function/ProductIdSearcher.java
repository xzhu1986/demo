package au.com.isell.epd.function;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.util.UUID;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.epd.pojo.ProductIdMapping;

public class ProductIdSearcher {
	
	public static enum FileSource {China, Uk};

	private static IndexHelper helper = IndexHelper.getInstance();

	public static void register() {
		helper.registerType(ProductIdMapping.class);
	}
	
	public static void indexObject(ProductIdMapping... mappings) {
		Object[] objs = mappings;
		helper.indexValues(objs);
	}
	
	public static ProductIdMapping getProductIdByORId(int openrangeId, FileSource source) {
		SearchRequestBuilder builder = helper.getClient().prepareSearch("prod_ids").setTypes("ids").addField("unspsc").addField("manufacturer").addField("partNo");
		if (source == FileSource.China) {
			QueryBuilder qb = termQuery("cnID", openrangeId);
			builder.setQuery(qb);
		} else {
			QueryBuilder qb = termQuery("ukID", openrangeId);
			builder.setQuery(qb);
		}
		SearchResponse resp = builder.execute().actionGet();
		SearchHits hits = resp.getHits();
		for (SearchHit hit : hits.getHits()) {
			ProductIdMapping mapping = new ProductIdMapping();
			mapping.setProductUUID(UUID.fromString(hit.getId()));
			if (source == FileSource.China) {
				mapping.setCnID(openrangeId);
			} else {
				mapping.setUkID(openrangeId);
			}
			mapping.setUnspsc(Integer.parseInt(hit.field("unspsc").getValue().toString()));
			mapping.setManufacturer((String) hit.field("manufacturer").getValue());
			mapping.setPartNo((String) hit.field("partNo").getValue());
			return mapping;
		}
		return null;
	}
	
	public static ProductIdMapping getProductIdByVendorPart(String vendor, String partNo) {
		SearchRequestBuilder builder = helper.getClient().prepareSearch("prod_ids").setTypes("ids").addField("unspsc").addField("cnID").addField("ukID");
		QueryBuilder qb = termQuery("partNoKey", (vendor == null? "": vendor.toLowerCase())+"|"+(partNo == null? "": partNo.toLowerCase()));
		builder.setQuery(qb);
		SearchResponse resp = builder.execute().actionGet();
		SearchHits hits = resp.getHits();
		for (SearchHit hit : hits.getHits()) {
			ProductIdMapping mapping = new ProductIdMapping();
			mapping.setProductUUID(UUID.fromString(hit.getId()));
			mapping.setCnID(Integer.parseInt(hit.field("cnID").getValue().toString()));
			mapping.setUkID(Integer.parseInt(hit.field("ukID").getValue().toString()));
			mapping.setUnspsc(Integer.parseInt(hit.field("unspsc").getValue().toString()));
			mapping.setManufacturer(vendor);
			mapping.setPartNo(partNo);
			return mapping;
		}
		return null;
	}
	
}