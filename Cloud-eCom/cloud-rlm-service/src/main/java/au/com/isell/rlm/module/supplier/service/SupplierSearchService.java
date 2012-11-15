package au.com.isell.rlm.module.supplier.service;

import java.util.List;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.vo.SupplierSearchVals;

public interface SupplierSearchService {
	Pair<Long, List<SupplierSearchBean>> query(SupplierSearchVals searchVals, String filter, Integer pageSize, Integer pageNo);
}
