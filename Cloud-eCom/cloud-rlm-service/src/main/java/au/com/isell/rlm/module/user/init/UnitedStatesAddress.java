package au.com.isell.rlm.module.user.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;
@Component
public class UnitedStatesAddress {
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private CurrencyDao currencyDao;
	
	public void init(){
		
		AddressItem us = new AddressItem("us", "Country", "United States", "US", null);
		us.setPhoneAreaCodeBind(1);
		us.setCurrency("USD");
		us.setPostcodeName("Zip");
		us.setRegionName("State");
		addressDao.saveAddressItem(us);
		
		Currency currency = new Currency();
		currency.setCode("USD");
		currency.setMinorUnit(2);
		currency.setSymbol("$");
		currency.setName("US Dollar");
		currencyDao.saveCurrency(currency);
	}
}