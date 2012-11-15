package au.com.isell.rlm.module.reseller.service;

import java.util.List;
import java.util.Map;

import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.VersionHistory;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;

public interface LicenseService {

	void saveLicense(Map<LicenseType, LicModule> licenses,Reseller formReseller);

	void createNewLicense(int serialNo);
	
	List<VersionHistory> getVersionHistory(int serialNo);
}
