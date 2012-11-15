package au.com.isell.rlm.module.address.constant;

import java.util.HashMap;
import java.util.Map;

import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.GeneralAddress;

/**
 * @author frankw 13/02/2012
 */
public class CountryCodeAddressMapping {
	public static Map<String, Class<? extends Address>> mapping=new HashMap<String, Class<? extends Address>>();
	static{
		mapping.put("au", GeneralAddress.class);
		mapping.put("nz", GeneralAddress.class);
	}
	public static Class<? extends Address> getAddressCls(String countryCode){
		return GeneralAddress.class;
	}
}
