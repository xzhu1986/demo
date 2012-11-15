package au.com.isell.rlm.module.address.dao;

import java.util.List;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.address.domain.AddressItem;


public interface AddressDao {
	
	List<AddressItem> getSubItems(String parentCode, Pair<String, Boolean>[] sorts);
	
	AddressItem getAddressItem(String code) ;
	
	void saveAddressItem(AddressItem... addressItem) ;
	
	void deleteAddressItem(AddressItem... addressItem) ;

	FilterMaker getAddressMaker();
	
	Pair<Long, List<AddressItem>> searchItems(FilterItem condition, Pair<String, Boolean>[] sorts, Integer size, Integer pageNo);

	void deleteAddressItem(String... code);
	
}