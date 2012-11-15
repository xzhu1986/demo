package au.com.isell.epd.eunomia.dao.aws;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.exi.EXIDriver;
import au.com.isell.epd.eunomia.dao.ProductDAO;
import au.com.isell.epd.eunomia.domain.Option;
import au.com.isell.epd.eunomia.domain.Product;
import au.com.isell.remote.common.model.Pair;

import com.thoughtworks.xstream.XStream;

public class AWSProductDAO implements ProductDAO {
	
	private static final String BASE_FOLDER = "EPD";
	private static XStream xstream = new XStream(new EXIDriver());
	
	static {
		xstream.alias("product", Product.class);
		xstream.alias("option", Option.class);
	}

	@Override
	public void saveProduct(Product product) {
		S3Manager manager = S3Manager.getInstance();
		manager.putObject(getProductInfoPath(product.getVendor(), product.getVendorPart()), xstream.toXML(product), null, null);
	}

	@Override
	public void saveOptions(String vendorName, String vendorPart, List<Option> options) {
		S3Manager manager = S3Manager.getInstance();
		manager.putObject(getProductOptionPath(vendorName, vendorPart), xstream.toXML(options), null, null);
	}

	@Override
	public Product getProduct(String vendorName, String vendorPart) {
		S3Manager manager = S3Manager.getInstance();
		InputStream in = manager.getStream(getProductInfoPath(vendorName, vendorPart));
		if (in == null) return null;
		return (Product) xstream.fromXML(in);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Option> getOptions(String vendorName, String vendorPart) {
		S3Manager manager = S3Manager.getInstance();
		InputStream in = manager.getStream(getProductOptionPath(vendorName, vendorPart));
		if (in == null) return null;
		return (List<Option>) xstream.fromXML(in);
	}

	@Override
	public Pair<String, String> getProductKeyByApolloVendor(
			String apolloVendorName, String vendorPart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair<String, String> getProductKeyByApolloVendorId(
			int apolloVendorId, String vendorPart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair<String, String> getProductKeyById(UUID productId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveProductKeys(String[]... keyStructures) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveVendorMappings(String[]... vendorMappings) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getVendor(String apolloVendorName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVendor(int apolloVendorId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static String getProductFolder(String vendorName, String vendorPart) {
		return BASE_FOLDER + '/'+vendorName+'/'+vendorPart+'/';
	}
	
	private static String getProductInfoPath(String vendorName, String vendorPart) {
		return getProductFolder(vendorName, vendorPart) + "prod.info.json";
	}
	
	private static String getProductOptionPath(String vendorName, String vendorPart) {
		return getProductFolder(vendorName, vendorPart) + "options.info.json";
	}

}
