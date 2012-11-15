package au.com.isell.remote.ws.parasearch.call;

import java.util.List;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.remote.common.solr.vo.GroupItem;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.remote.ws.parasearch.model.EpdProductGroup;
import au.com.isell.remote.ws.parasearch.model.ecom.Product;

public interface EcomParaSearchService {

	List<GroupItem> getEcomAttributeList(String unspsc,Integer catalogueId, String attributes, Double minPrice,Double maxPrice,String priceType);
	
	Pair<Integer,List<Product>>  getEcomProductList(String unspsc,Integer catalogueId, String attributes, Integer pageStart, Integer pageSize, Integer accountId, Double minPrice,
			Double maxPrice,String priceType);
	
	Result getProductCategories(Integer level, String tier1, Integer pageStart, Integer rows);
	
	List<Pair<String, List<EpdProductGroup>>> getAllProductCategories();
}
