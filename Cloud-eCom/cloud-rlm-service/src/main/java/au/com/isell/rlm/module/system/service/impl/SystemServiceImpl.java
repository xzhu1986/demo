package au.com.isell.rlm.module.system.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.preloading.Preload;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;
import au.com.isell.rlm.module.system.service.SystemService;
import au.com.isell.rlm.module.system.vo.CountrySearchVals;
import au.com.isell.rlm.module.system.vo.CurrencySearchVals;

@Service
public class SystemServiceImpl implements SystemService {

	private static Logger logger = LoggerFactory.getLogger(SystemServiceImpl.class);

	@Autowired
	private AddressDao countryDao;
	@Autowired
	private CurrencyDao currencyDao;

	@Override
	public void deleteCountry(String code) {
		countryDao.deleteAddressItem(code);
	}
	
	@Override
	public Pair<Long, List<AddressItem>> searchCountry(CountrySearchVals searchVals, boolean sortShortName, Integer pageSize, Integer pageNo){
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = countryDao.getAddressMaker();
		Pair<String, Boolean>[] sorts = new Pair[1];
		if (sortShortName)
			sorts[0]=maker.makeSortItem("shortName", true);
		else
			sorts[0]=maker.makeSortItem("name", true);
		Pair<Long, List<AddressItem>> r = countryDao.searchItems(searchVals.getFilter(maker), sorts, pageSize, pageNo);
		return r;
	}
	
	@Override
	public void saveCurrency(Currency currency) {
		currencyDao.saveCurrency(currency);
	}
	@Override
	public Currency getCurrency(String code) {
		return currencyDao.getCurrency(code);
	}
	@Override
	public void deleteCurrency(String code) {
		currencyDao.deleteCurrency(code);
	}
	@Override
	public Pair<Long, List<Currency>> searchCurrency(CurrencySearchVals searchVals, Integer pageSize, Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = currencyDao.getCurrencyMaker();
		Pair<Long, List<Currency>> r = currencyDao.searchCurrency(searchVals.getFilter(maker), pageSize, pageNo);
		return r;
	}
	@Override
	public List<Currency> getCurrencies() {
		FilterItem item = currencyDao.getCurrencyMaker().makeAllQuery();
		Pair<Long, List<Currency>> result = currencyDao.searchCurrency(item, null, null);
		if (result == null) return null;
		return result.getValue();
	}
	@Override
	public List<String[]> getDefaultCountries() {
		return Preload.getCountryList();
	}
	@Override
	public List<String[]> getDefaultCurrencies() {
		return Preload.getCurrencyList();
	}
}
