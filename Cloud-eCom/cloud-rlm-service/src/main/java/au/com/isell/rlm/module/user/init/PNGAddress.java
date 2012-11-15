package au.com.isell.rlm.module.user.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;
@Component
public class PNGAddress {
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private CurrencyDao currencyDao;
	
	public void init(){
		
		AddressItem pg = new AddressItem("pg", "Country", "Papua New Guinea", "PG", null);
		pg.setPhoneAreaCodeBind(675);
		pg.setCurrency("PGK");
	
		addressDao.saveAddressItem(pg);
		Currency currency = new Currency();
		currency.setCode("PGK");
		currency.setMinorUnit(2);
		currency.setSymbol("K");
		currency.setName("Kina");
		currencyDao.saveCurrency(currency);
	}
}