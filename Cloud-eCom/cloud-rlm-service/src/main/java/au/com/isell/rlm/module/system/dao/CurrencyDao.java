package au.com.isell.rlm.module.system.dao;

import java.util.List;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.system.domain.Currency;

public interface CurrencyDao {
	void saveCurrency(Currency currency);
	Currency getCurrency(String code);
	Pair<Long, List<Currency>> searchCurrency(FilterItem filterItem, Integer pageSize, Integer pageNo); 
	FilterMaker getCurrencyMaker();
	void deleteCurrency(String code);
}
