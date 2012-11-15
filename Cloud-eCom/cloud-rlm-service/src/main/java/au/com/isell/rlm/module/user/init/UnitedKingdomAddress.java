package au.com.isell.rlm.module.user.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;

@Component
public class UnitedKingdomAddress {
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private CurrencyDao currencyDao;

	public void init() {

		AddressItem gb = new AddressItem("gb", "Country", "United Kingdom", "GB", null);
		gb.setPhoneAreaCodeBind(44);
		gb.setCurrency("GBP");
		gb.setPostcodeName("Post Code");
		addressDao.saveAddressItem(gb);
		Currency currency = new Currency();
		currency.setCode("GBP");
		currency.setMinorUnit(2);
		currency.setSymbol("\u00a3");
		currency.setName("Pound Sterling");
		currencyDao.saveCurrency(currency);
	}
}