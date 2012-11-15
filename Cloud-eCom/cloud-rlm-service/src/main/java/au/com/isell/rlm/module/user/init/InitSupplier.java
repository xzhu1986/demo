package au.com.isell.rlm.module.user.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.supplier.dao.SupplierDao;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;

@Component
public class InitSupplier extends DAOSupport {

	@Autowired
	private SupplierDao supplierDao;

	public void reindex() {
		FilterMaker maker = supplierDao.getSupplierSearchMaker();
		FilterItem item = maker.makeAllQuery();
		for (SupplierSearchBean searchBean: supplierDao.querySupplierBeans(item, null)) {
			supplierDao.indexSupplier(searchBean.getSupplierId());
		}
	}
}
