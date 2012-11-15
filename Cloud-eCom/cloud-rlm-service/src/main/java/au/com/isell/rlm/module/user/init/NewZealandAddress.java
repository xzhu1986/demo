package au.com.isell.rlm.module.user.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;
@Component
public class NewZealandAddress {
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private CurrencyDao currencyDao;
	
	public void init(){
		AddressItem country = new AddressItem("nz", "Country", "New Zealand", "NZ", null);
		country.setPhoneAreaCodeBind(64);
		country.setCurrency("NZD");
		AddressItem nsw = new AddressItem("7d16a9b0-7492-11e1-b0c4-0800200c9a66", "Island", "North Island", "North", "nz");
		nsw.setPhoneAreaCodeBind(2);
		AddressItem vic = new AddressItem("7d16a9b1-7492-11e1-b0c4-0800200c9a66", "Island", "South Island", "South", "nz");
		vic.setPhoneAreaCodeBind(3);
	
		IndexHelper helper = IndexHelper.getInstance();
		addressDao.saveAddressItem(country,nsw,vic);
		helper.indexValues(country, nsw, vic);
		Currency currency = new Currency();
		currency.setCode("NZD");
		currency.setMinorUnit(2);
		currency.setSymbol("$");
		currency.setName("New Zealand Dollar");
		currencyDao.saveCurrency(currency);
	}
}