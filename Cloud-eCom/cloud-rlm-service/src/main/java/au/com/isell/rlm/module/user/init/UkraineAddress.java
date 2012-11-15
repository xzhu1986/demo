package au.com.isell.rlm.module.user.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;

@Component
public class UkraineAddress {
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private CurrencyDao currencyDao;
	
	public void init(){
		
		AddressItem ua = new AddressItem("ua", "Country", "Ukraine", "UA", null);
		ua.setPhoneAreaCodeBind(380);
		ua.setCurrency("UAH");
	
		addressDao.saveAddressItem(ua);
		Currency currency = new Currency();
		currency.setCode("UAH");
		currency.setMinorUnit(2);
		currency.setSymbol("\u20b4");
		currency.setName("Hryvnia");
		currencyDao.saveCurrency(currency);
	}
}