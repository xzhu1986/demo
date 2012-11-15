package au.com.isell.rlm.common.services;

import java.util.List;

public interface FinancialDataService {
//	List<Currency> getCurrencies();
	
	List getGstVals();

	List getGstValsByCountry(String countryCode);
}