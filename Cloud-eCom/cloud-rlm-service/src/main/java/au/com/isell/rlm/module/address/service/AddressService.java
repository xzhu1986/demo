package au.com.isell.rlm.module.address.service;

import java.util.List;

import au.com.isell.rlm.module.address.domain.AddressItem;

public interface AddressService {
	List<AddressItem> getSubItems(String parentCode);
	
	AddressItem getAddressItem(String code);
	
	void saveAddressItem(AddressItem addressItem) ;
	
	void deleteAddressItem(String code) ;
	
	List<String[]> getAddressParents(String code);
	
	void saveRegion4Country(AddressItem countryItem,String regionDatas);
	
	List<AddressItem> getAddressParentObjs(String code);
	
	List<AddressItem> findAddress(String text, String parent, boolean sortShortName, Integer pageNo, Integer size);
	
	String getRegionDisplayNames(String regionCode) ;
}
