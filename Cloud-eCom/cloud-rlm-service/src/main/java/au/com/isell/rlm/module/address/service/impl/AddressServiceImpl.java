package au.com.isell.rlm.module.address.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;


/**
 * @author frankw 08/02/2012
 */
@Service
public class AddressServiceImpl implements AddressService {
	@Autowired
	private AddressDao addressDao;
	
	@Override
	public List<AddressItem> getSubItems(String parentCode) {
		if (StringUtils.isBlank(parentCode))
			throw new RuntimeException("parentCode is blank");
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0]=new Pair<String, Boolean>("name", true);
		return addressDao.getSubItems(parentCode, sorts);
	}

	@Override
	public AddressItem getAddressItem(String code) {
		if (code== null || code.length() == 0) return null;
		return addressDao.getAddressItem(code);
	}

	@Override
	public void saveAddressItem(AddressItem formAddrItem) {
		addressDao.saveAddressItem(formAddrItem);
	}

	@Override
	public void deleteAddressItem(String code) {
		Assert.notNull(code);
		AddressItem addressItem = new AddressItem();
		addressItem.setCode(code);
		addressDao.deleteAddressItem(addressItem);
	}

	@Override
	public List<String[]> getAddressParents(String code) {
		List<String[]> r = new ArrayList<String[]>();
		putParentsCodes(code, r);
		Collections.reverse(r);
		return r;
	}

	@Override
	public List<AddressItem> getAddressParentObjs(String code) {
		List<String[]> codes=getAddressParents(code);
		List<AddressItem> r=new ArrayList<AddressItem>(codes.size());
		for(String[] item:codes){
			r.add(getAddressItem(item[0]));
		}
		return r;
	}

	public void putParentsCodes(String code, List<String[]> r) {
		AddressItem item = addressDao.getAddressItem(code);
		if (item == null)
			return;
		String parentCode = item.getParent();
		if (StringUtils.isEmpty(parentCode)) {// country level
			r.add(new String[] { code, "root" });
			return;
		} else {
			r.add(new String[] { code, parentCode });
			putParentsCodes(parentCode, r);
		}
	}

	@Override
	public void saveRegion4Country(AddressItem countryItem, String regionDatas) {
		List<Map> datas= JsonUtils.decode(regionDatas, List.class);
		for(Map m:datas){
			if(m.size()==0) continue;
			AddressItem formItem=BeanUtils.constructBean(m, AddressItem.class,null);
			Assert.notNull(formItem.getName());
			formItem.setParent(countryItem.getCode());
			formItem.setType(countryItem.getRegionName());
			addressDao.saveAddressItem(formItem);
		}
	}

	@Override
	public List<AddressItem> findAddress(String text, String parent, boolean sortShortName, Integer pageNo, Integer size) {
		FilterMaker maker = addressDao.getAddressMaker();
		FilterItem itemName = maker.makeNameFilter("name", TextMatchOption.Is, text);
		FilterItem itemShortName = maker.makeNameFilter("shortName", TextMatchOption.Is, text);
		if (parent == null) parent = AddressItem.DEFAULT_PARENT_CODE;
		FilterItem itemParent = maker.makeNameFilter("parent", TextMatchOption.Is, parent);
		FilterItem item = maker.linkWithAnd(itemParent, maker.linkWithOr(itemName, itemShortName));
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		if (sortShortName)
			sorts[0]=maker.makeSortItem("shortName", true);
		else
			sorts[0]=maker.makeSortItem("name", true);
		return addressDao.searchItems(item, sorts, size, pageNo).getValue();
	}

	@Override
	public String getRegionDisplayNames(String regionCode) {
		List<AddressItem> parents=getAddressParentObjs(regionCode);
		StringBuilder builder=new StringBuilder();
		for(AddressItem item:parents){
			if(builder.length()>0) builder.append(",");
			builder.append(item.getShortName());
		}
		return builder.toString();
	}
	
}
