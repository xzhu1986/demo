package au.com.isell.epd.eunomia.dao;

import java.util.List;
import java.util.UUID;

import au.com.isell.epd.eunomia.domain.Option;
import au.com.isell.epd.eunomia.domain.Product;
import au.com.isell.remote.common.model.Pair;

public interface ProductDAO {
	void saveProduct(Product product);
	void saveOptions(String vendorName, String vendorPart, List<Option> options);
	Product getProduct(String vendorName, String vendorPart);
	List<Option> getOptions(String vendorName, String vendorPart);
	
	
	// help functions
	Pair<String, String> getProductKeyByApolloVendor(String apolloVendorName, String vendorPart);
	Pair<String, String> getProductKeyByApolloVendorId(int apolloVendorId, String vendorPart);
	Pair<String, String> getProductKeyById(UUID productId);
	/**
	 * String[]{vendorPart, vendorName, String prodId}
	 * @param keyStructure
	 */
	void saveProductKeys(String[]... keyStructures);
	/**
	 * String[]{apolloVendorName, apolloVendorId}
	 * @param vendorMappings
	 */
	void saveVendorMappings(String[]... vendorMappings);
	String getVendor(String apolloVendorName);
	String getVendor(int apolloVendorId);
}
