package au.com.isell.rlm.test;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilder;

import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.license.EComLicense;
import au.com.isell.rlm.module.reseller.domain.license.EPDLicense;
import au.com.isell.rlm.module.reseller.domain.license.ITQLicense;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;

public class TestGeneralSrchInterface {
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Reseller reseller = new Reseller();
		reseller.setSerialNo(6100001);
		reseller.setCompany("Test Company");
		reseller.setCountry("au");
		reseller.setAddress(new GeneralAddress("5 Elizabeth St", "", "", "Ashfield", "nsw", "au", "2131"));
		reseller.setStatus(ResellerStatus.Active);
		reseller.setVersion("v5.0");
		reseller.setLastSyncDate(sdf.parse("2012-2-22 13:00:00"));
		
		Map<LicenseType, LicModule> licenses = new HashMap<LicenseType, LicModule>();
		ITQLicense itqLic = new ITQLicense();
		licenses.put(LicenseType.ITQLicense,itqLic);
		itqLic.setRenewalDate(sdf.parse("2012-10-22 13:00:00"));
		EComLicense ecomLic=new EComLicense();
		licenses.put(LicenseType.EComLicense,ecomLic);
		ecomLic.setRenewalDate(sdf.parse("2012-10-21 13:00:00"));
		EPDLicense epdLic=new EPDLicense();
		licenses.put(LicenseType.EPDLicense,epdLic);
		epdLic.setRenewalDate(sdf.parse("2012-10-23 13:00:00"));
		reseller.setLicenses(licenses);
		ResellerSearchBean bean = new ResellerSearchBean(reseller, null,"");
		IndexHelper helper = IndexHelper.getInstance();
		helper.registerType(ResellerSearchBean.class);
		helper.indexValues(bean);
		
		FieldMapper mapper = new FieldMapper();
		mapper.addMap("serial_no", "serialNo");
		mapper.addMap("last_sync_date", "lastSyncDate");
		
		FilterMaker maker = new ESFilterMaker();
		maker.setFieldMapper(mapper);
		FilterItem item =maker.linkWithOr(maker.makeNameFilter("serial_no", TextMatchOption.Contains, "61000"),
				maker.makeDateRange("last_sync_date", sdf.parse("2012-2-21 13:00:00"), sdf.parse("2012-2-22 12:00:00"), false, false));
		
		QueryBuilder qb = ((ESFilterItem) item).generateQueryBuilder();
		System.out.println(qb.toString());
		String[] keys = helper.queryKeys(ResellerSearchBean.class, new QueryParams(qb, null));
		for (String key : keys) {
			System.out.println(key);
		}
	}
}
