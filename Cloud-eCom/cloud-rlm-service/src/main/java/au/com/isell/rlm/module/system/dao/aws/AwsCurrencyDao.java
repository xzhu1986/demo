package au.com.isell.rlm.module.system.dao.aws;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;

@Repository
public class AwsCurrencyDao extends DAOSupport implements CurrencyDao {
	private static Logger logger = LoggerFactory.getLogger(AwsCurrencyDao.class);
	private IndexHelper indexHelper = IndexHelper.getInstance();

	private static FieldMapper currencyMapper = new FieldMapper();
	private ESFilterMaker currencyMaker;

	@Override
	public void saveCurrency(Currency currency) {
		super.save(currency);
	}

	@Override
	public Currency getCurrency(String code) {
		Currency currency = new Currency();
		currency.setCode(code);
		return super.get(currency);
	}

	@Override
	public void deleteCurrency(String code) {
		super.delete(this.getCurrency(code));
	}
	
	@Override
	public Pair<Long, List<Currency>> searchCurrency(FilterItem filterItem, Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0]=currencyMaker.makeSortItem("name", true);
		return indexHelper.queryBeans(Currency.class,new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public FilterMaker getCurrencyMaker() {
		if (currencyMaker != null) return currencyMaker;
		currencyMaker = new ESFilterMaker();
		currencyMaker.setType( Currency.class);
		currencyMaker.setFieldMapper(currencyMapper);
		return currencyMaker;
	}

}
