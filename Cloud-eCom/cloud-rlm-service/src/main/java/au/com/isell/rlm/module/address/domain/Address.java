package au.com.isell.rlm.module.address.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import au.com.isell.rlm.module.address.service.AddressService;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(value = { @Type(value = GeneralAddress.class, name = "GeneralAddress")})
public abstract class Address {
	public static final String TXT_COUNTRY = "address.country";

	protected Map<String, String> data;

	public Address() {
		data = new HashMap<String, String>();
	}

	public Address(String countryCode) {
		data = new HashMap<String, String>();
		setCountryCode(countryCode);
	}

	public Map<String, String> getAddressItems() {
		if (data == null) {
			data = new HashMap<String, String>();
		}
		return data;
	}

	public abstract void setAddressItems(Map<String, String> items);
	@JsonIgnore
	public abstract List<String> getDefaultItemSort();

	public abstract String getCity();

	public abstract String getRegion();

	public void setCountryCode(String countryCode) {
		data.put(TXT_COUNTRY, countryCode);
	}

	public String getCountryCode() {
		return data.get(TXT_COUNTRY);
	}
	@JsonIgnore
	public abstract List<String> getLevels();

	public abstract String toReportAddress(AddressService service);
	
	public abstract String toSingleLineAddress(AddressService service);

}
