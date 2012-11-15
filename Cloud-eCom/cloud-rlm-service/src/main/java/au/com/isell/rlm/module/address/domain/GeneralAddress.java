package au.com.isell.rlm.module.address.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.address.service.AddressService;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("address_general")
public class GeneralAddress extends Address {

	public static final String TXT_REGION = "address.region";
	public static final String TXT_CITY = "address.city";
	public static final String TXT_ADDRESS1 = "address.address1";
	public static final String TXT_ADDRESS2 = "address.address2";
	public static final String TXT_ADDRESS3 = "address.address3";
	public static final String TXT_POSTCODE = "address.postcode";
	@XStreamOmitField
	@JsonIgnore
	public static final List<String> items;
	@XStreamOmitField
	@JsonIgnore
	public static final List<String> levels;
	
	static {
		List<String> temp = new ArrayList<String>();
		temp.add(TXT_ADDRESS1);
		temp.add(TXT_ADDRESS2);
		temp.add(TXT_ADDRESS3);
		temp.add(TXT_CITY);
		temp.add(TXT_REGION);
		temp.add(TXT_COUNTRY);
		temp.add(TXT_POSTCODE);
		items = Collections.unmodifiableList(temp);
		temp = new ArrayList<String>();
		temp.add(TXT_REGION);
		temp.add(TXT_CITY);
		temp.add(TXT_ADDRESS1);
		temp.add(TXT_ADDRESS2);
		temp.add(TXT_ADDRESS3);
		levels = Collections.unmodifiableList(temp);
	}
	
	public GeneralAddress() {
	}
	
	public GeneralAddress(String address1,String address2,String address3, String city, String region, String countryCode, String postcode) {
		super(countryCode);
		setAddress1(address1);
		setAddress2(address2);
		setAddress3(address3);
		setCity(city);
		setRegion(region);
		setPostcode(postcode);
	}

	@Override
	public void setAddressItems(Map<String, String> items) {
		String temp = items.get(TXT_COUNTRY);
		if (temp != null) setCountryCode(temp);
		temp = items.get(TXT_REGION);
		if (temp != null) setRegion(temp);
		temp = items.get(TXT_CITY);
		if (temp != null) setCity(temp);
		temp = items.get(TXT_ADDRESS1);
		if (temp != null) setAddress1(temp);
		temp = items.get(TXT_POSTCODE);
		if (temp != null) setPostcode(temp);
	}

	@Override
	public List<String> getDefaultItemSort() {
		return items;
	}

	public void setRegion(String region) {
		data.put(TXT_REGION, region);
	}
	@Override
	@ISellIndexValue
	public String getRegion() {
		return data.get(TXT_REGION);
	}

	public void setPostcode(String postcode) {
		data.put(TXT_POSTCODE, postcode);
	}

	public String getPostcode() {
		return data.get(TXT_POSTCODE);
	}

	public void setCity(String city) {
		data.put(TXT_CITY, city);
	}
	@Override
	@ISellIndexValue
	public String getCity() {
		return data.get(TXT_CITY);
	}

	public void setAddress1(String address1) {
		data.put(TXT_ADDRESS1, address1);
	}

	public String getAddress1() {
		return data.get(TXT_ADDRESS1);
	}

	public void setAddress2(String address2) {
		data.put(TXT_ADDRESS2, address2);
	}

	public String getAddress2() {
		return data.get(TXT_ADDRESS2);
	}

	public void setAddress3(String address3) {
		data.put(TXT_ADDRESS3, address3);
	}

	public String getAddress3() {
		return data.get(TXT_ADDRESS3);
	}
	@Override
	public List<String> getLevels() {
		return levels;
	}

	@Override
	public String toString() {
		return "GeneralAddress [data=" + data + "]";
	}

	@Override
	public String toReportAddress(AddressService service) {
		StringBuilder sb = new StringBuilder();
		String item = data.get(TXT_ADDRESS1);
		if (item != null && item.length() > 0) sb.append(item).append("\r\n");
		item = data.get(TXT_ADDRESS2);
		if (item != null && item.length() > 0) sb.append(item).append("\r\n");
		item = data.get(TXT_ADDRESS3);
		if (item != null && item.length() > 0) sb.append(item).append("\r\n");
		item = data.get(TXT_CITY);
		if (item != null && item.length() > 0) sb.append(item).append("\r\n");
		item = data.get(TXT_REGION);
		if (item != null && item.length() > 0) {
			AddressItem region = service.getAddressItem(item);
			sb.append(region.getShortName() == null || region.getShortName().length() == 0? region.getName() : region.getShortName()).append(" ");
		}
		item = data.get(TXT_POSTCODE);
		if (item != null && item.length() > 0) sb.append(item);
		return sb.toString();
	}

	@Override
	public String toSingleLineAddress(AddressService service) {
		StringBuilder sb = new StringBuilder();
		String item = data.get(TXT_ADDRESS1);
		if (item != null && item.length() > 0) sb.append(item).append(" ");
		item = data.get(TXT_ADDRESS2);
		if (item != null && item.length() > 0) sb.append(item).append(" ");
		item = data.get(TXT_ADDRESS3);
		if (item != null && item.length() > 0) sb.append(item).append(" ");
		item = data.get(TXT_CITY);
		if (item != null && item.length() > 0) sb.append(item).append(" ");
		item = data.get(TXT_REGION);
		if (item != null && item.length() > 0) {
			AddressItem region = service.getAddressItem(item);
			sb.append(region.getShortName() == null || region.getShortName().length() == 0? region.getName() : region.getShortName()).append(" ");
		}
		item = data.get(TXT_POSTCODE);
		if (item != null && item.length() > 0) sb.append(item);
		return sb.toString().trim();
	}

}
