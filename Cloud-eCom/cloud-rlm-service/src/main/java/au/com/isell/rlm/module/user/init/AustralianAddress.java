package au.com.isell.rlm.module.user.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
import au.com.isell.rlm.module.system.domain.Currency;

@Component
public class AustralianAddress  {
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private CurrencyDao currencyDao;
	
	public void init() {
		AddressItem au = new AddressItem("au", "Country", "Australia", "AU", null);
		au.setPhoneAreaCodeBind(61);
		au.setCurrency("AUD");
		au.setPostcodeName("Post Code");
		au.setRegionName("State");
		AddressItem nsw = new AddressItem("bd6fb480-7491-11e1-b0c4-0800200c9a66", "State", "New South Wales", "NSW", "au");
		nsw.setPhoneAreaCodeBind(2);
		AddressItem vic = new AddressItem("bd6fb481-7491-11e1-b0c4-0800200c9a66", "State", "Victoria", "VIC", "au");
		vic.setPhoneAreaCodeBind(3);
		AddressItem qld = new AddressItem("bd6fb482-7491-11e1-b0c4-0800200c9a66", "State", "Queensland", "QLD", "au");
		qld.setPhoneAreaCodeBind(7);
		AddressItem sa = new AddressItem("bd6fb483-7491-11e1-b0c4-0800200c9a66", "State", "South Australia", "SA", "au");
		sa.setPhoneAreaCodeBind(8);
		AddressItem wa = new AddressItem("bd6fb484-7491-11e1-b0c4-0800200c9a66", "State", "Western Australia", "WA", "au");
		wa.setPhoneAreaCodeBind(8);
		AddressItem act = new AddressItem("bd6fb485-7491-11e1-b0c4-0800200c9a66", "State", "Australian Capital Territory", "ACT", "au");
		act.setPhoneAreaCodeBind(2);
		AddressItem nt = new AddressItem("bd6fb486-7491-11e1-b0c4-0800200c9a66", "State", "Northern Territory", "NT", "au");
		nt.setPhoneAreaCodeBind(8);
		AddressItem tas = new AddressItem("bd6fb487-7491-11e1-b0c4-0800200c9a66", "State", "Tasmania", "TAS", "au");
		tas.setPhoneAreaCodeBind(3);
		addressDao.saveAddressItem(au,nsw,vic,qld,sa,wa,act,nt,tas);
		Currency currency = new Currency();
		currency.setCode("AUD");
		currency.setMinorUnit(2);
		currency.setSymbol("$");
		currency.setName("Australian Dollar");
		currencyDao.saveCurrency(currency);
	}
}