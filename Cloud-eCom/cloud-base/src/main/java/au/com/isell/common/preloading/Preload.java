package au.com.isell.common.preloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.util.NetworkUtils;
import au.com.isell.common.util.StringUtils;

public class Preload {
	private static Logger logger = LoggerFactory.getLogger(Preload.class);
	public static List<String[]> countryCodeList = new ArrayList<String[]>();
	public static List<String[]> currencyCodeList = new ArrayList<String[]>();

	public static void preload() throws Exception {
		Map<String, String[]> currencyMap = loadCurrencyInfo();
		loadCountryInfo(currencyMap);
		IndexHelper.scanIndices();
		logger.info("Finish loading country list. Got size :{}",countryCodeList.size());
		logger.info("Finish loading currency list. Got size :{}",currencyCodeList.size());
	}

	public static Map<String, String[]> loadCurrencyInfo() {
		Map<String, String[]> currencyMap = new LinkedHashMap<String, String[]>();
		InputStream in = null;
		try {
			Properties bundle = new Properties();
			bundle.load(Preload.class.getResourceAsStream("/settings.properties"));
			String url = bundle.getProperty("currency.url");
			in = NetworkUtils.makeHTTPGetRequest(url, null, null);
			loadCurrency(currencyMap, in);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			in = Preload.class.getResourceAsStream("/dl_iso_table_a1.xml");
			try {
				loadCurrency(currencyMap, in);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
			}
		}

		Map<String, String[]> currencyFilterMap = new LinkedHashMap<String, String[]>();
		for (String[] currency : currencyMap.values()) {
			currencyFilterMap.put(currency[0] + "-" + currency[1] + "-" + currency[2] + "-", currency);
		}
		currencyCodeList.addAll(currencyFilterMap.values());
		return currencyMap;
	}

	private static void loadCurrency(Map<String, String[]> currencyMap,
			InputStream in) throws XmlPullParserException, IOException {
		MXParser xpp = new MXParser();
		xpp.setInput(new BufferedReader(new InputStreamReader(in)));
		int eventType = xpp.getEventType();
		Stack<String> tagStack = new Stack<String>();
		String[] item = null;
		String key = null;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			eventType = xpp.getEventType();
			if (eventType == XmlPullParser.START_DOCUMENT) {
			} else if (eventType == XmlPullParser.END_DOCUMENT) {
				break;
			} else if (eventType == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
				tagStack.push(tag);
				String path = showStack(tagStack);
				if ("/ISO_CCY_CODES/ISO_CURRENCY".equals(path)) {
					item = new String[3];
					key = null;
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				String path = showStack(tagStack);
				if ("/ISO_CCY_CODES/ISO_CURRENCY".equals(path)) {
					currencyMap.put(key, item);
				}
				tagStack.pop();
			} else if (eventType == XmlPullParser.TEXT) {
				String path = showStack(tagStack);
				if ("/ISO_CCY_CODES/ISO_CURRENCY/ENTITY".equals(path)) {
					key = xpp.getText().toUpperCase();
				} else if ("/ISO_CCY_CODES/ISO_CURRENCY/CURRENCY".equals(path)) {
					item[0] = xpp.getText();
				} else if ("/ISO_CCY_CODES/ISO_CURRENCY/ALPHABETIC_CODE".equals(path)) {
					item[1] = xpp.getText().toUpperCase();
				} else if ("/ISO_CCY_CODES/ISO_CURRENCY/MINOR_UNIT".equals(path)) {
					item[2] = xpp.getText();
				}
			}
			xpp.next();
		}
	}

	public static void loadCountryInfo(Map<String, String[]> currencyMap) {
		countryCodeList.clear();
		InputStream in = null;
		try {
			Properties bundle = new Properties();
			bundle.load(Preload.class.getResourceAsStream("/settings.properties"));
			String url = bundle.getProperty("country.code.url");
			in = NetworkUtils.makeHTTPGetRequest(url, null, null);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
			loadCountries(currencyMap, br);
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
			in = Preload.class.getResourceAsStream("/list-en1-semic-3.txt");
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
				loadCountries(currencyMap, br);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
			}
		}
	}

	private static void loadCountries(Map<String, String[]> CurrencyMap,
			BufferedReader br) throws IOException {
		int count = 0;
		String[] tmp = null, currencyInfo = null;
		String line = null;
		while ((line=br.readLine())!=null) {
			count++;
			if (count > 2 && line.indexOf(";") > 0) {
				tmp = line.split(";");
				currencyInfo = CurrencyMap.get(tmp[0].toUpperCase());
				countryCodeList.add(new String[] { StringUtils.UpperCaseFirst(tmp[0]), tmp[1].toLowerCase(), currencyInfo == null ? null : currencyInfo[1] });
			}
		}
	}

	private static String showStack(Stack<String> stack) {
		StringBuilder sb = new StringBuilder();
		for (String str : stack) {
			sb.append('/').append(str);
		}
		return sb.toString();
	}

	/**
	 * 
	 * 
	 * @return String[] {country name, country code alpha-2, currency code}
	 */
	public static synchronized List<String[]> getCountryList() {
//		if (CountryCodeList.isEmpty()) {
//			Map<String, String[]> currencyMap = loadCurrencyInfo();
//			loadCountryInfo(currencyMap);
//		}
		return countryCodeList;
	}

	/**
	 * 
	 * @return String[] {currency name, currency code, minor unit}
	 */
	public static synchronized List<String[]> getCurrencyList() {
//		if (CurrencyCodeList.isEmpty()) {
//			loadCurrencyInfo();
//		}
		return currencyCodeList;
	}
}
