package au.com.isell.rlm.module.reseller.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.VersionHistory;
import au.com.isell.rlm.module.reseller.domain.license.EComLicense;
import au.com.isell.rlm.module.reseller.domain.license.EPDLicense;
import au.com.isell.rlm.module.reseller.domain.license.ITQLicense;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;
import au.com.isell.rlm.module.reseller.service.LicenseService;

/**
 * @author frankw 13/02/2012
 */
@Service
public class LicenseServiceImpl implements LicenseService {
	@Autowired
	private ResellerDao resellerDao;

	@Override
	public void saveLicense(Map<LicenseType, LicModule> licenses,Reseller formReseller) {
		Reseller reseller = resellerDao.getReseller(formReseller.getSerialNo());
		BeanUtils.copyPropsExcludeNull(reseller, formReseller);
		Map<LicenseType, LicModule> dbLicenses = reseller.getLicenses();
		if (dbLicenses == null) {
			dbLicenses = new HashMap<LicenseType, LicModule>();
		}
		for(LicenseType type:LicenseType.values()){
			buildLicense(licenses, dbLicenses, type);
		}
		
		reseller.setLicenses(dbLicenses);
		resellerDao.save(true, reseller);
	}

	private void buildLicense(Map<LicenseType, LicModule> licenses, Map<LicenseType, LicModule> dbLicenses, LicenseType type) {
		LicModule dbObj = dbLicenses.get(type);
		LicModule newObj = licenses.get(type);
		
		if (dbObj != null) {
			BeanUtils.copyPropsExcludeNull(dbObj, newObj);
			newObj=dbObj;
		}
		dbLicenses.put(type, newObj);
	}

	@Override
	public void createNewLicense(int serialNo) {
		Reseller reseller = resellerDao.getReseller(serialNo);
		EPDLicense epdLicense = new EPDLicense().init();
		EComLicense eComLicense = new EComLicense().init();
		ITQLicense itqLicense = new ITQLicense().init();
		Map<LicenseType, LicModule> licenses = new HashMap<LicenseType, LicModule>();
		licenses.put(LicenseType.ITQLicense, itqLicense);
		licenses.put(LicenseType.EComLicense, eComLicense);
		licenses.put(LicenseType.EPDLicense, epdLicense);
		reseller.setLicenses(licenses);
		resellerDao.save(true, reseller);
	}

	@Override
	public List<VersionHistory> getVersionHistory(int serialNo) {
		Pair<Long, List<VersionHistory>> historys = resellerDao.getVersionHistory(serialNo, 1, 3);
		return historys.getValue();
	}
}
