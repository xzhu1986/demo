package au.com.isell.rlm.common.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.isell.rlm.common.services.FinancialDataService;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;

@Service
public class FinancialDataServiceImpl implements FinancialDataService {
	@Autowired
	private AddressService addressService;

	// @Override
	// public List<Currency> getCurrencies() {
	// List<Currency> currencyList = new ArrayList<Currency>();
	// currencyList.add(new Currency("AUD", "$", "Australia Dollars"));
	// currencyList.add(new Currency("NZD", "$", "New Zealand Dollars"));
	// currencyList.add(new Currency("GBP", "£", "United Kingdom Pounds"));
	// currencyList.add(new Currency("USD", "$", "United States Dollars"));
	// currencyList.add(new Currency("CNY", "￥", "China Yuan Renminbi"));
	// return currencyList;
	// }

	@Override
	public List getGstVals() {// TODO hardcode
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "0");
		map.put("value", "0%");
		list.add(map);
		map = new HashMap<String, String>();
		map.put("key", "10");
		map.put("value", "10%");// GST (AU)
		list.add(map);
		map = new HashMap<String, String>();
		map.put("key", "15");
		map.put("value", "15%");// GST (NZ)
		list.add(map);
		map = new HashMap<String, String>();
		map.put("key", "20");
		map.put("value", "20%");// VAT (UK)
		list.add(map);
		return list;
	}

	@Override
	public List getGstValsByCountry(String countryCode) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "0");
		map.put("value", "0%");
		list.add(map);
		
		AddressItem country = addressService.getAddressItem(countryCode);
		BigDecimal taxRate = country.getTaxRate();
		if (taxRate != null) {
			map = new HashMap<String, String>();
			map.put("key", taxRate.toString());
			map.put("value", taxRate.toString()+"%");
			list.add(map);
		}
		
		return list;
	}
}
