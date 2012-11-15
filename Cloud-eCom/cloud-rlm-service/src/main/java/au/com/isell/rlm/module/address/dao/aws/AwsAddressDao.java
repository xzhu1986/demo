package au.com.isell.rlm.module.address.dao.aws;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
@Repository
public class AwsAddressDao extends DAOSupport implements AddressDao {

	private static FieldMapper addressItemMapper = new FieldMapper();
	
	static {
	}
	
	@Override
	public List<AddressItem> getSubItems(String parentCode,Pair<String, Boolean>[] sorts){
		QueryBuilder queryBuilder=null;
		if(parentCode == null || parentCode.equals("root")){
			queryBuilder = termQuery("parent", AddressItem.DEFAULT_PARENT_CODE);
		}else{
			queryBuilder=termQuery("parent", parentCode);
		}
		Pair<Long, List<AddressItem>> reslut = indexHelper.queryBeans(AddressItem.class,new QueryParams(queryBuilder, sorts));
		if(reslut!=null ){
			return reslut.getValue();
		}
		return null;
	}
	
	@Override
	public AddressItem getAddressItem(String code) {
		if("_".equals(code) || StringUtils.isBlank(code)) return null;
		AddressItem sample=new AddressItem();
		sample.setCode(code);
		return get(sample);
	}

	@Override
	public void saveAddressItem(AddressItem... addressItems) {
		for (AddressItem addressItem : addressItems) {
			if(StringUtils.isBlank(addressItem.getParent())){
				addressItem.setParent(AddressItem.DEFAULT_PARENT_CODE);
			}
			if(StringUtils.isEmpty(addressItem.getCode())){
				addressItem.setCode(UUID.randomUUID().toString());
			}else{
				AddressItem dbAddressItem=super.get(addressItem);
				if(dbAddressItem!=null){
					BeanUtils.copyPropsExcludeNull(dbAddressItem, addressItem);
					addressItem=dbAddressItem;
				}
			}
			if (addressItem.getParent() == null || addressItem.getParent().trim().length() == 0) {
				addressItem.setParent(AddressItem.DEFAULT_PARENT_CODE);
			}
		}
		save(addressItems);
	}

	@Override
	public void deleteAddressItem(AddressItem... addressItem) {
		delete(addressItem);
	}
	
	@Override
	public void deleteAddressItem(String... codes) {
		AddressItem[] items = new AddressItem[codes.length];
		for (int i = 0; i < codes.length; i++) {
			items[i] = new AddressItem();
			items[i].setCode(codes[i]);
		}
		delete(items);
	}
	ESFilterMaker maker;
	@Override
	public synchronized FilterMaker getAddressMaker() {
		if (maker != null) return maker;
		maker = new ESFilterMaker();
		maker.setType(AddressItem.class);
		maker.setFieldMapper(addressItemMapper);
		return maker;
	}

	@Override
	public Pair<Long, List<AddressItem>> searchItems(FilterItem condition,
			Pair<String, Boolean>[] sorts, Integer size, Integer pageNo) {
		if (condition == null)
			condition = new ESFilterItem();
		Assert.isTrue(condition instanceof ESFilterItem);
		return IndexHelper.getInstance().queryBeans(AddressItem.class,new QueryParams(((ESFilterItem)condition).generateQueryBuilder(), sorts).withPaging(pageNo, size));
	}
}
