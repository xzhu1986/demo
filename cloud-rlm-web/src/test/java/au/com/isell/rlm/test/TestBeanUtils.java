package au.com.isell.rlm.test;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierBillingInfo;


public class TestBeanUtils {
	public static void main(String[] args) throws Exception {
		Reseller reseller = new Reseller();
		reseller.setCompany("company");
		ResellerSupplierMap map=new ResellerSupplierMap();
		
		SupplierBillingInfo billingInfo=new SupplierBillingInfo();
		billingInfo.setEmail("my email");
		Supplier supplier=new Supplier();
		supplier.setBillingInfo(billingInfo);
		
		BeanUtils.copyFromMutliSources(map, reseller,supplier);
		System.out.println(map.getCompany());
		System.out.println(map.getSupEmail());
		
	}
}
