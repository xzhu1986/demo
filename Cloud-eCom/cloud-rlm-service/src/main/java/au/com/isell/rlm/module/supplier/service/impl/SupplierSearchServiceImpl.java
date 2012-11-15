package au.com.isell.rlm.module.supplier.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.supplier.dao.SupplierDao;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.service.SupplierSearchService;
import au.com.isell.rlm.module.supplier.vo.SupplierSearchFilters;
import au.com.isell.rlm.module.supplier.vo.SupplierSearchVals;
import au.com.isell.rlm.module.user.service.PermissionService;

@Service
public class SupplierSearchServiceImpl implements SupplierSearchService {
	@Autowired
	SupplierDao supplierDao;
	@Autowired
	PermissionService permissionService;

	@Override
	public Pair<Long, List<SupplierSearchBean>> query(SupplierSearchVals searchVals, String filter, Integer pageSize, Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = supplierDao.getSupplierSearchMaker();

		FilterItem item = searchVals.getFilter(maker);
		if (StringUtils.isNotBlank(filter)) {
			SupplierSearchFilters filters = (SupplierSearchFilters) BeanUtils.getEnum(SupplierSearchFilters.class, filter);
			item = maker.linkWithAnd(item, filters.getFilterItem(maker));
		}
		List<String> countries = permissionService.getCountriesOfSupplierPerm();
		if (CollectionUtils.isNotEmpty(countries)) {
			List<FilterItem> countryFilters = new ArrayList<FilterItem>();
			for (String code : countries) {
				countryFilters.add(maker.makeNameFilter("country", TextMatchOption.Is, code));
			}
			item = maker.linkWithAnd(item, maker.linkWithOr(countryFilters.toArray(new FilterItem[countryFilters.size()])));
		}

		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = maker.makeSortItem("name", true);
		Pair<Long, List<SupplierSearchBean>> r = supplierDao.query(item, sorts, pageSize, pageNo);
		return r;
	}
}
