package au.com.isell.rlm.module.system.service;

import java.util.List;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.domain.Currency;
import au.com.isell.rlm.module.system.vo.CountrySearchVals;
import au.com.isell.rlm.module.system.vo.CurrencySearchVals;

public interface SystemService {
	List<String[]> getDefaultCountries();

	Pair<Long, List<AddressItem>> searchCountry(CountrySearchVals searchVals, boolean sortShortName, Integer pageSize, Integer pageNo);

	void deleteCountry(String code);

	void saveCurrency(Currency currency);

	Currency getCurrency(String code);

	List<String[]> getDefaultCurrencies();

	Pair<Long, List<Currency>> searchCurrency(CurrencySearchVals searchVals, Integer pageSize, Integer pageNo);

	void deleteCurrency(String code);

	List<Currency> getCurrencies();
	
	

}