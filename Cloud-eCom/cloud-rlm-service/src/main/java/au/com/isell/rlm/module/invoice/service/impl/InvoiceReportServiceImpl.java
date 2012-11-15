package au.com.isell.rlm.module.invoice.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.com.bytecode.opencsv.CSVWriter;
import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.util.DateUtil;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.invoice.DateRangeLocater;
import au.com.isell.rlm.module.invoice.dao.InvoiceDao;
import au.com.isell.rlm.module.invoice.domain.Invoice.BusinessType;
import au.com.isell.rlm.module.invoice.domain.Invoice.PaymentProbaility;
import au.com.isell.rlm.module.invoice.domain.InvoiceSchedule;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;
import au.com.isell.rlm.module.invoice.domain.report.AgentBusinessTypeDataGroup;
import au.com.isell.rlm.module.invoice.domain.report.AgentInvoiceSummary;
import au.com.isell.rlm.module.invoice.domain.report.AisRenderUnit;
import au.com.isell.rlm.module.invoice.domain.report.AisRowModel;
import au.com.isell.rlm.module.invoice.domain.report.Forcast12Month;
import au.com.isell.rlm.module.invoice.domain.report.PaymentsDue;
import au.com.isell.rlm.module.invoice.service.InvoiceReportService;
import au.com.isell.rlm.module.report.constant.ReportFormat;
import au.com.isell.rlm.module.report.constant.ReportPath;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;
import au.com.isell.rlm.module.user.service.UserService;

@Repository
public class InvoiceReportServiceImpl implements InvoiceReportService {
	@Autowired
	private InvoiceDao invoiceDao;
	@Autowired
	private ResellerDao resellerDao;
	@Autowired
	private AgentService agentService;
	@Autowired
	private UserService userService;

	private static final DecimalFormat DF = new DecimalFormat("###,###");

	private static Calendar getAuTime() {
		// Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
		Calendar calendar = Calendar.getInstance();
		DatePicker.resetTimeToZero(calendar);
		return calendar;
	}

	@Override
	public String generatePDFReportInvoicePaymentsDueSixWeeks() {
		Map<String, Object> data = new HashMap<String, Object>();
		Date today = getAuTime().getTime();
		Date firstWeekFriDay = createCurrentWeekFriDay(today);
		for (int i = 0; i < 6; i++) {
			data.put("week" + (i + 1), DateUtil.formatDate("EEE ','dd MMM", new Date(firstWeekFriDay.getTime() + 24 * 60 * 60 * 1000 * 7l * i)));
		}

		List<PaymentsDue> dataList = generateDataReportInvoicePaymentsDueSixWeeks(today, firstWeekFriDay);
		List<Map<String, Object>> scheduledRowMapList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> unconfirmedRowMapList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> confirmedRowMapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> unconfirmedMap = new HashMap<String, Object>();
		unconfirmedMap.put("type", "Unconfirmed");
		Map<String, Object> confirmedMap = new HashMap<String, Object>();
		confirmedMap.put("type", "Confirmed");
		Map<String, Object> scheduledMap = new HashMap<String, Object>();
		scheduledMap.put("type", "Scheduled");

		int size = 8;
		BigDecimal[] sumUnconfirmed = new BigDecimal[size], sumConfirmed = new BigDecimal[size], sumScheduled = new BigDecimal[size], total = new BigDecimal[size];
		for (PaymentsDue paymentsDue : dataList) {
			if ("Scheduled".equalsIgnoreCase(paymentsDue.getProbability())) {
				updatePaymentsDuePDFData(scheduledRowMapList, sumScheduled, total, paymentsDue);
			} else if ("Unconfirmed".equalsIgnoreCase(paymentsDue.getProbability())) {
				updatePaymentsDuePDFData(unconfirmedRowMapList, sumUnconfirmed, total, paymentsDue);
			} else if ("Confirmed".equalsIgnoreCase(paymentsDue.getProbability())) {
				updatePaymentsDuePDFData(confirmedRowMapList, sumConfirmed, total, paymentsDue);
			}
		}
		unconfirmedMap.put("resellers", unconfirmedRowMapList);
		confirmedMap.put("resellers", confirmedRowMapList);
		scheduledMap.put("resellers", scheduledRowMapList);
		String sumKeyTemp = null, totalKeyTemp = null;
		for (int i = 0; i < size; i++) {
			if (i == 0) {
				sumKeyTemp = "sumOverdue";
				totalKeyTemp = "totalOverdue";
			} else if (i == size - 1) {
				sumKeyTemp = "sumLater";
				totalKeyTemp = "totalLater";
			} else {
				sumKeyTemp = "sumWk" + i;
				totalKeyTemp = "totalWk" + i;
			}
			unconfirmedMap.put(sumKeyTemp, formatBigDecimal(sumUnconfirmed[i]));
			confirmedMap.put(sumKeyTemp, formatBigDecimal(sumConfirmed[i]));
			scheduledMap.put(sumKeyTemp, formatBigDecimal(sumScheduled[i]));
			data.put(totalKeyTemp, formatBigDecimal(total[i]));
		}
		List<Map<String, Object>> probabilityList = new ArrayList<Map<String, Object>>();
		probabilityList.add(unconfirmedMap);
		probabilityList.add(confirmedMap);
		probabilityList.add(scheduledMap);

		data.put("probability", probabilityList);

		return JsonUtils.encode(data);
	}

	private String formatBigDecimal(BigDecimal value) {
		if (value == null)
			return "-";
		else if (value.compareTo(BigDecimal.ZERO) == 0)
			return "-";
		else
			return DF.format(value.setScale(0, BigDecimal.ROUND_HALF_UP));
	}

	private void updatePaymentsDuePDFData(List<Map<String, Object>> rowMapList, BigDecimal[] sum, BigDecimal[] total, PaymentsDue paymentsDue) {
		Map<String, Object> rowMap = new HashMap<String, Object>();
		rowMap.put("name", paymentsDue.getName());
		rowMap.put("amtOverdue", formatBigDecimal(paymentsDue.getOverdue()));
		rowMap.put("amtLater", formatBigDecimal(paymentsDue.getLater()));

		int i = 1;
		for (BigDecimal value : paymentsDue.getDueAmount().values()) {
			rowMap.put("amtWk" + i, formatBigDecimal(value));
			i++;
		}
		rowMapList.add(rowMap);

		sum[0] = sum[0] == null ? paymentsDue.getOverdue() : sum[0].add(paymentsDue.getOverdue());
		total[0] = total[0] == null ? paymentsDue.getOverdue() : total[0].add(paymentsDue.getOverdue());

		i = 1;
		for (BigDecimal value : paymentsDue.getDueAmount().values()) {
			sum[i] = sum[i] == null ? value : sum[i].add(value);
			total[i] = total[i] == null ? value : total[i].add(value);
			i++;
		}
		sum[i] = sum[i] == null ? paymentsDue.getLater() : sum[i].add(paymentsDue.getLater());
		total[i] = total[i] == null ? paymentsDue.getLater() : total[i].add(paymentsDue.getLater());
	}

	@Override
	public File generateCSVReportInvoicePaymentsDueSixWeeks() {
		Date today = getAuTime().getTime();
		Date firstWeekFriDay = createCurrentWeekFriDay(today);

		List<PaymentsDue> dataList = generateDataReportInvoicePaymentsDueSixWeeks(today, firstWeekFriDay);
		String fileName = new ReportPath(au.com.isell.rlm.module.report.constant.ReportType.InvoicePaymentsDueSixWeeks, ReportFormat.CSV)
				.getOutputName();
		CSVWriter writer = null;
		try {
			File file = File.createTempFile(fileName, ".csv");
			writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), ',');
			String[] columns = generateInvoicePaymentsDueSixWeeksColumns(firstWeekFriDay);
			writer.writeNext(columns);
			for (PaymentsDue item : dataList) {
				writer.writeNext(item.toArray());
			}
			return file;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	private String[] generateInvoicePaymentsDueSixWeeksColumns(Date firstWeekFriDay) {
		String[] columns = new String[10];
		columns[0] = "Reseller";
		columns[1] = "Probability";
		columns[2] = "Overdue";
		for (int i = 0; i < 6; i++) {
			columns[i + 3] = DateUtil.formatDate("EEE ','dd MMM", new Date(firstWeekFriDay.getTime() + 24 * 60 * 60 * 1000 * 7l * i));
		}
		columns[columns.length - 1] = "Later";
		return columns;
	}

	private List<PaymentsDue> generateDataReportInvoicePaymentsDueSixWeeks(Date today, Date firstWeekFriDay) {
		Iterable<InvoiceSearchBean> unconfirmedInvoiceIterator = getInvoicesByProbability(PaymentProbaility.Unconfirmed);
		Map<String, PaymentsDue> unconfirmedMap = new HashMap<String, PaymentsDue>();
		PaymentsDue paymentsDue = null;
		Date compareDate = null;
		BigDecimal amount = null;
		for (InvoiceSearchBean invoiceSearchBean : unconfirmedInvoiceIterator) {
			compareDate = invoiceSearchBean.getPromisedPaymentDate()==null?invoiceSearchBean.getInvoiceDate():invoiceSearchBean.getPromisedPaymentDate();
			amount = invoiceSearchBean.getBalance();
			paymentsDue = createPaymentsDue(today, firstWeekFriDay, invoiceSearchBean,compareDate,amount,PaymentProbaility.Unconfirmed.toString());
			updatePaymentsDueMap(invoiceSearchBean.getCompany(), unconfirmedMap, paymentsDue);
		}

		Iterable<InvoiceSearchBean> confirmedInvoiceIterator = getInvoicesByProbability(PaymentProbaility.Confirmed);
		Map<String, PaymentsDue> confirmedMap = new HashMap<String, PaymentsDue>();
		Map<String, PaymentsDue> confirmedScheduledMap = new HashMap<String, PaymentsDue>();

		for (InvoiceSearchBean invoiceSearchBean : confirmedInvoiceIterator) {
			List<InvoiceSchedule> invoiceSchedules = invoiceDao.getInvoiceSchedules(invoiceSearchBean.getInvoiceNumber());
			if(invoiceSchedules!=null && invoiceSchedules.size()>0){
				for(InvoiceSchedule invoiceSchedule : invoiceSchedules){
					if(!invoiceSchedule.isPaid()){
						compareDate = invoiceSchedule.getDueDate();
						amount = invoiceSchedule.getScheduledAmount();
						paymentsDue = createPaymentsDue(today, firstWeekFriDay, invoiceSearchBean,compareDate,amount,"Scheduled");
						updatePaymentsDueMap(invoiceSearchBean.getCompany(), confirmedScheduledMap, paymentsDue);
					}
				}
			}else{
				compareDate = invoiceSearchBean.getPromisedPaymentDate()==null?invoiceSearchBean.getInvoiceDate():invoiceSearchBean.getPromisedPaymentDate();
				amount = invoiceSearchBean.getBalance();
				paymentsDue = createPaymentsDue(today, firstWeekFriDay, invoiceSearchBean,compareDate,amount,PaymentProbaility.Confirmed.toString());
				updatePaymentsDueMap(invoiceSearchBean.getCompany(), confirmedMap, paymentsDue);

			}
		}
		List<PaymentsDue> result = new ArrayList<PaymentsDue>();
		result.addAll(unconfirmedMap.values());
		result.addAll(confirmedMap.values());
		result.addAll(confirmedScheduledMap.values());
		return result;
	}

	public Iterable<InvoiceSearchBean> getInvoicesByProbability(PaymentProbaility probability) {
		FilterMaker maker = invoiceDao.getFilterMaker();
//		FilterItem filterItem1 = maker.makeDecimalRange("balance", BigDecimal.ZERO, null, false, false);
		FilterItem filterItem1 = maker.makeNameFilter("balance", TextMatchOption.IsNot, String.valueOf("0.00"));
		FilterItem filterItem2 = maker.makeNameFilter("probability", TextMatchOption.Is, String.valueOf(probability.ordinal()));
		return invoiceDao.queryInvoice(maker.linkWithAnd(filterItem1, filterItem2));
	}
	
	private PaymentsDue createPaymentsDue(Date today, Date firstWeekFriDay,InvoiceSearchBean invoiceSearchBean,Date compareDate,BigDecimal amount,String probaility) {
		PaymentsDue paymentsDue = new PaymentsDue(invoiceSearchBean.getCompany(),probaility);
		paymentsDue.setDueAmount(initDueAmount(firstWeekFriDay));
		BigDecimal amountEX = invoiceSearchBean.getTaxRate()==null?amount:amount.divide(invoiceSearchBean.getTaxRate().divide(new BigDecimal(100)).add(new BigDecimal(1)),BigDecimal.ROUND_HALF_UP);
		if(compareDate.getTime()<=today.getTime()){
			paymentsDue.setOverdue(amountEX);
		}else if(compareDate.getTime()>(firstWeekFriDay.getTime()+24*60*60*1000*7*6l)){
			paymentsDue.setLater(amountEX);
		}else{
			paymentsDue.setDueAmount(createDueAmount(paymentsDue.getDueAmount(),compareDate,firstWeekFriDay, amountEX));
		}
		return paymentsDue;
	}

	private Map<Date, BigDecimal> initDueAmount(Date firstWeekFriDay) {
		Map<Date, BigDecimal> dueAmount = new TreeMap<Date, BigDecimal>();
		for (int i = 0; i < 6; i++) {
			dueAmount.put(DateUtils.addWeeks(firstWeekFriDay, i), BigDecimal.ZERO);
		}
		return dueAmount;
	}

	private Map<Date, BigDecimal> createDueAmount(Map<Date, BigDecimal> dueAmount, Date date, Date firstWeekFriDay, BigDecimal amountEX) {
		Date key = createCurrentWeekFriDay(date);
		dueAmount.put(key, amountEX);
		return dueAmount;
	}

	private void updatePaymentsDueMap(String key, Map<String, PaymentsDue> map, PaymentsDue paymentsDue) {
		if (map.containsKey(key)) {
			map.put(key, paymentsDue.add(map.get(key)));
		} else {
			map.put(key, paymentsDue);
		}
	}

	private Date createCurrentWeekFriDay(Date date) {
		Calendar calendar = getAuTime();
		calendar.setTime(date);
		DatePicker.resetTimeToZero(calendar);
		if (Calendar.FRIDAY >= calendar.get(Calendar.DAY_OF_WEEK)) {
			calendar.add(Calendar.DATE, Calendar.FRIDAY - calendar.get(Calendar.DAY_OF_WEEK));
		} else {
			calendar.add(Calendar.DATE, 7 + Calendar.FRIDAY - calendar.get(Calendar.DAY_OF_WEEK));
		}
		return calendar.getTime();
	}

	@Override
	public String generatePDFReportInvoiceForcast12Month() {
		int arraySize = 14;
		Map<String, Object> data = new HashMap<String, Object>();
		Pair<Date[], Map<String, BigDecimal[]>[]> allData = generateDataReportInvoiceForcast12Month();
		Map<String, Object>[] columns = initMapArray(arraySize);
		for (int i = 0; i < allData.getKey().length; i++) {
			columns[i].put("v", DateUtil.formatDate("MMM yyyy", allData.getKey()[i]));
		}
		columns[columns.length - 2].put("v", "Total");
		columns[columns.length - 1].put("v", "Invoice Balances");
		data.put("cols", columns);

		Forcast12Month prev = null, current = null, total = null;
		List<Map<String, Object>> dtl = new ArrayList<Map<String, Object>>();
		;
		List<Map<String, Object>> agtSum = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> agtData = new ArrayList<Map<String, Object>>();
		Map<String, Object> item = null, agtSumItem = null, agtDataItem = new HashMap<String, Object>();
		int count = 0;
		for (Map.Entry<String, BigDecimal[]> entry : allData.getValue()[0].entrySet()) {
			count++;
			current = createDataItem4Forcast12Month(entry);
			item = new HashMap<String, Object>();
			item.put("amt", current.getFormatedAmounts());
			item.put("type", current.getType());
			dtl.add(item);

			if ((prev != null && !prev.getAgentId().equals(current.getAgentId()))
					|| (prev != null && count == allData.getValue()[0].entrySet().size())) {
				total = createTotalDataItem4Forcast12Month(allData.getValue()[1], prev);
				agtSumItem = new HashMap<String, Object>();
				agtSumItem.put("amt", total.getFormatedAmounts());
				agtSumItem.put("name", total.getAgentName());
				agtSum.add(agtSumItem);

				agtDataItem.put("name", total.getAgentName());
				agtDataItem.put("sum", total.getFormatedAmounts());
				agtDataItem.put("dtl", dtl);
				agtData.add(agtDataItem);
				dtl = new ArrayList<Map<String, Object>>();
			}
			prev = current;
		}
		data.put("agtData", agtData);
		data.put("agtSum", agtSum);
		BigDecimal[] allTotal = null;
		for (Map.Entry<String, BigDecimal[]> entry : allData.getValue()[1].entrySet()) {
			if (allTotal == null) {
				allTotal = new BigDecimal[entry.getValue().length];
			}
			for (int i = 0; i < allTotal.length; i++) {
				addMount(i, entry.getValue()[i], allTotal);
			}
		}
		Map<String, Object>[] allSum = initMapArray(arraySize);
		for (int i = 0; allTotal!=null && i < allTotal.length; i++) {
			allSum[i].put("v", allTotal[i] == null ? "0" : DF.format(allTotal[i].setScale(0, BigDecimal.ROUND_HALF_UP)));
		}
		data.put("allSum", allSum);

		return JsonUtils.encode(data);
	}

	private Map<String, Object>[] initMapArray(int size) {
		Map<String, Object>[] arrayMaps = new HashMap[size];
		for (int i = 0; i < size; i++) {
			arrayMaps[i] = new HashMap<String, Object>();
		}
		return arrayMaps;
	}

	@Override
	public File generateCSVReportInvoiceForcast12Month() {
		Pair<Date[], Map<String, BigDecimal[]>[]> rawData = generateDataReportInvoiceForcast12Month();
		String fileName = new ReportPath(au.com.isell.rlm.module.report.constant.ReportType.InvoiceForcast12Month, ReportFormat.CSV).getOutputName();
		CSVWriter writer = null;
		try {
			File file = File.createTempFile(fileName, ".csv");
			writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), ',');
			String[] columns = generateInvoiceForcast12MonthColumns(rawData.getKey());
			writer.writeNext(columns);
			Forcast12Month prevItem = null, item = null;
			for (Map.Entry<String, BigDecimal[]> entry : rawData.getValue()[0].entrySet()) {
				item = createDataItem4Forcast12Month(entry);
				if (prevItem != null && !prevItem.getAgentId().equals(item.getAgentId())) {
					writer.writeNext(createTotalDataItem4Forcast12Month(rawData.getValue()[1], prevItem).toArray());
				}
				writer.writeNext(item.toArray());
				prevItem = item;
			}
			if (prevItem != null) {
				writer.writeNext(createTotalDataItem4Forcast12Month(rawData.getValue()[1], prevItem).toArray());
			}
			return file;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	private Forcast12Month createTotalDataItem4Forcast12Month(Map<String, BigDecimal[]> rawData, Forcast12Month prevItem) {
		Forcast12Month totalItem = new Forcast12Month();
		totalItem.setAgentName(prevItem.getAgentName());
		totalItem.setType("Total");
		totalItem.setAmounts(rawData.get(prevItem.getAgentId()));
		return totalItem;
	}

	private String[] generateInvoiceForcast12MonthColumns(Date[] dates) {
		String[] columns = new String[16];
		columns[0] = "Agent";
		columns[1] = "Type";
		for (int i = 0; i < dates.length; i++) {
			columns[i + 2] = DateUtil.formatDate("MMM yyyy", dates[i]);
		}
		columns[columns.length - 2] = "Total";
		columns[columns.length - 1] = "Invoice Balances";
		return columns;
	}

	private Forcast12Month createDataItem4Forcast12Month(Map.Entry<String, BigDecimal[]> entry) {
		Forcast12Month item = new Forcast12Month();
		String[] keyArr = entry.getKey().split(spliterFlag);
		item.setAgentId(keyArr[0]);
		item.setAgentName(agentService.getAgent(item.getAgentId()).getName());
		item.setType(keyArr[1]);
		item.setAmounts(entry.getValue());
		return item;
	}

	private Pair<Date[], Map<String, BigDecimal[]>[]> generateDataReportInvoiceForcast12Month() {
		int arraySize = 14;
		Calendar calendar = getAuTime();
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		DateRangeLocater dateRangeLocater = new DateRangeLocater(calendar, Calendar.MONTH, 1, 12);
		Reseller reseller = null;
		Map<LicenseType, LicModule> licenses = null;
		Map<String, BigDecimal[]> rawDataMap = new HashMap<String, BigDecimal[]>();
		Map<String, BigDecimal[]> rawTotalDataMap = new HashMap<String, BigDecimal[]>();
		String key = null, totalKey = null;
		BigDecimal[] values = null, totalValues = null;
		int arrIndex = 0;
		Date renewalDate = null;
		BigDecimal amount = null, balances = null;
		for (String serialNo : resellerDao.queryResellerKeys(resellerDao.getResellerSearchMaker().makeAllQuery())) {
			reseller = resellerDao.getReseller(Integer.parseInt(serialNo));
			if (reseller == null) continue;
			licenses = reseller.getLicenses();
			for (LicenseType licenseType : LicenseType.values()) {
				LicModule licModule = licenses.get(licenseType);
				if (licModule != null && licModule.getRenewalDate() != null) {
					if (licModule.getRenewalDate().getTime() > getAuTime().getTimeInMillis()) {
						renewalDate = DateUtils.addYears(licModule.getRenewalDate(), 1);
					} else {
						renewalDate = licModule.getRenewalDate();
					}
					if (renewalDate.getTime() <= DateUtils.addYears(calendar.getTime(), 1).getTime()
							&& renewalDate.getTime() > calendar.getTimeInMillis()) {
						key = reseller.getAgencyId().toString() + spliterFlag + licenseType.toString();
						arrIndex = dateRangeLocater.locateAt(licModule.getRenewalDate());
						amount = licModule.getAnnualFee() == null ? BigDecimal.ZERO : licModule.getAnnualFee();

						values = getOrCreateKey(rawDataMap, key, arraySize);
						addMount(arrIndex, amount, values);
						addMount(values.length - 2, amount, values);
						balances = getInvoiceBalances(serialNo);
						addMount(values.length - 1, balances, values);
						rawDataMap.put(key, values);

						totalKey = reseller.getAgencyId().toString();
						totalValues = getOrCreateKey(rawTotalDataMap, totalKey, arraySize);
						addMount(arrIndex, amount, totalValues);
						addMount(values.length - 2, amount, totalValues);
						addMount(values.length - 1, balances, totalValues);
						rawTotalDataMap.put(totalKey, totalValues);
					}
				}
			}
		}
		Date[] rangeDef = dateRangeLocater.getRangeSpliters();
		return new Pair(rangeDef, new Map[] { new TreeMap(rawDataMap), rawTotalDataMap });
	}

	private BigDecimal getInvoiceBalances(String serialNo) {
		BigDecimal balances = BigDecimal.ZERO;
		FilterMaker maker = invoiceDao.getFilterMaker();
		FilterItem filterItem1 = maker.makeDecimalRange("balance", new BigDecimal(0), null, false, false);
		FilterItem filterItem2 = maker.makeNameFilter("resellerSerialNo", TextMatchOption.Is, serialNo);
		Iterable<InvoiceSearchBean> beans = invoiceDao.queryInvoice(maker.linkWithAnd(filterItem1, filterItem2));
		for (InvoiceSearchBean bean : beans) {
			balances = balances.add(bean.getBalance());
		}
		return balances;
	}

	@Override
	public String generatePdfReportData4AgentInvoiceSummary() {
		int arrySize = AisRowModel.arrySize;
		Result4Ais rawData = generateReportData4AgentInvoiceSummary();
		Map<String, BigDecimal[]> rawDataMap = rawData.getRawDataMap();
		Map<String, BigDecimal[]> rawDataSumMap = rawData.getRawDataSumMap();
		Map<String, BigDecimal[]> rawDataPreviousSumMap = rawData.getRawDataPreviousSumMap();
		Map<String, BigDecimal[]> rawDataAgentBizTypeSumMap = rawData.getRawDataAgentBizTypeSumMap();
		Map<String, Map<String, Map<String, BigDecimal[]>>> allMap = rawData.getAllMap();

		List<AisRenderUnit> list = new ArrayList<AisRenderUnit>();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", GlobalAttrManager.getClientInfo().getLocale());
		String[] dateHeaderStrArr = getDateHeaderStrArr(rawData.getDateHeaderDef(), sdf);

		BigDecimal[] total4All = rawData.getTotal4All();
		BigDecimal[] lastYearTotal4All = rawData.getLastYearTotal4All();
		getOrCreateArr(lastYearTotal4All);
		for (Map.Entry<String, Map<String, Map<String, BigDecimal[]>>> agentEntry : allMap.entrySet()) {
			AisRenderUnit renderUnit = new AisRenderUnit();
			String agentId = agentEntry.getKey();
			String agentName = agentService.getAgent(agentId).getName();

			renderUnit.setAgentName(agentName);
			renderUnit.setMonthColumnsDef(dateHeaderStrArr);

			BigDecimal[] total4Agent = rawDataSumMap.get(agentId);
			BigDecimal[] lastYeartotal4Agent = rawDataPreviousSumMap.get(agentId);
			lastYeartotal4Agent = getOrCreateArr(lastYeartotal4Agent);
			renderUnit.setTotal4Agent(new AisRowModel("Total for " + renderUnit.getAgentName(), total4Agent));
			renderUnit.setLastYear4AgentSum(new AisRowModel("Last Year for " + renderUnit.getAgentName(), lastYeartotal4Agent));
			// diff row
			setDiff(arrySize, renderUnit, total4Agent, lastYeartotal4Agent);
			// row data
			List<AgentBusinessTypeDataGroup> dataGroups = new ArrayList<AgentBusinessTypeDataGroup>();
			for (Map.Entry<String, Map<String, BigDecimal[]>> bizTypeEntry : agentEntry.getValue().entrySet()) {
				AgentBusinessTypeDataGroup dataGroup = new AgentBusinessTypeDataGroup();
				String bizType = bizTypeEntry.getKey();
				String bizTypeName = ((BusinessType) BeanUtils.getEnum(BusinessType.class, bizType)).name();
				dataGroup.setGroupName(String.format("Agent %s - %s", agentName, bizTypeName));
				dataGroup.setColMonthAmountsSum(new AisRowModel("", rawDataAgentBizTypeSumMap.get(agentId + spliterFlag + bizType)));
				List<AisRowModel> rowList = new ArrayList<AisRowModel>();
				for (Map.Entry<String, BigDecimal[]> entry : bizTypeEntry.getValue().entrySet()) {
					String saleRepName = userService.getUser(entry.getKey()).getName();
					rowList.add(new AisRowModel(saleRepName, entry.getValue()));
				}
				dataGroup.setRowData(rowList);
				dataGroups.add(dataGroup);
			}
			renderUnit.setDataGroups(dataGroups);
			list.add(renderUnit);
		}
		// total for all
		AisRenderUnit renderUnit = new AisRenderUnit();
		renderUnit.setMonthColumnsDef(dateHeaderStrArr);
		renderUnit.setAgentName("All Agents");
		renderUnit.setTotal4Agent(new AisRowModel("Total for All", total4All));
		renderUnit.setLastYear4AgentSum(new AisRowModel("Last Year for All", lastYearTotal4All));
		setDiff(arrySize, renderUnit, total4All, lastYearTotal4All);
		// list.add(renderUnit);
		Map m = new HashMap();
		m.put("datas", list);
		m.put("total", renderUnit);
		return JsonUtils.encode(m);
	}

	private BigDecimal[] getOrCreateArr(BigDecimal[] lastYeartotal4Agent) {
		lastYeartotal4Agent = lastYeartotal4Agent == null ? new BigDecimal[AisRowModel.arrySize] : lastYeartotal4Agent;
		return lastYeartotal4Agent;
	}

	private void setDiff(int arrySize, AisRenderUnit renderUnit, BigDecimal[] total4Agent, BigDecimal[] lastYeartotal4Agent) {
		BigDecimal[] diffAmmount = new BigDecimal[arrySize];
		String[] diffPercent = new String[arrySize];
		BigDecimal totalPercent = BigDecimal.ZERO;
		for (int i = 0; i < arrySize; i++) {
			BigDecimal current = total4Agent[i];
			current = current == null ? BigDecimal.ZERO : current;
			BigDecimal prev = lastYeartotal4Agent[i];
			prev = prev == null ? BigDecimal.ZERO : prev;
			BigDecimal diff = current.subtract(prev);
			diffAmmount[i] = diff;
			BigDecimal diffPerc = null;
			if (diff == null)
				diffPerc = null;
			else if (current != null && current!=BigDecimal.ZERO)
				diffPerc = diff.divide(current, 0, RoundingMode.HALF_UP);
			
			if (diffPerc != null) {
				totalPercent.add(diffPerc);
				diffPercent[i] = diffPerc.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toString() + "%";
			}
		}
		renderUnit.setDiffAmount(new AisRowModel("Difference Amount", diffAmmount));
		renderUnit.setDiffPercentage(new AisRowModel("Difference Percentage", diffPercent, totalPercent.multiply(new BigDecimal("100"))
				.setScale(0, RoundingMode.HALF_UP).toString()
				+ "%"));
	}

	private String[] getDateHeaderStrArr(Date[] monthColsDef, SimpleDateFormat sdf) {
		String[] dateHeaderStrArr = new String[monthColsDef.length];
		for (int i = 0; i < monthColsDef.length; i++) {
			dateHeaderStrArr[i] = sdf.format(monthColsDef[i]);
		}
		return dateHeaderStrArr;
	}

	@Override
	public File generateCsvReport4AgentInvoiceSummary() {
		Result4Ais rawData = generateReportData4AgentInvoiceSummary();
		Map<String, BigDecimal[]> rawDataMap = rawData.getRawDataMap();
		Map<String, BigDecimal[]> rawDataSumMap = rawData.getRawDataSumMap();
		Map<String, BigDecimal[]> rawDataPreviousSumMap = rawData.getRawDataPreviousSumMap();
		Map<String, BigDecimal[]> rawDataAgentBizTypeSumMap = rawData.getRawDataAgentBizTypeSumMap();
		Map<String, Map<String, Map<String, BigDecimal[]>>> allMap = rawData.getAllMap();

		CSVWriter csvWriter = null;
		try {
			File f = File.createTempFile("csvReport4Invoice", ".csv");
			Writer writer = new OutputStreamWriter(new FileOutputStream(f));
			csvWriter = new CSVWriter(writer, ',');
			// write header
			SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", GlobalAttrManager.getClientInfo().getLocale());
			String[] dateHeaderStrArr = getDateHeaderStrArr(rawData.getDateHeaderDef(), sdf);
			String[] colDefs = (String[]) ArrayUtils.addAll(new String[] { "Agent", "Type", "Sales Person" }, dateHeaderStrArr);
			csvWriter.writeNext(colDefs);
			// write data body
			AgentInvoiceSummary prevItem = null;
			Map<String, BigDecimal[]> map = rawDataMap;
			Map<String, BigDecimal[]> sumMap = rawDataSumMap;
			Map<String, BigDecimal[]> prevSumMap = rawDataPreviousSumMap;

			for (Map.Entry<String, BigDecimal[]> entry : map.entrySet()) {
				AgentInvoiceSummary item = createDataItem4InvoiceSummay(entry);
				if (prevItem != null && !prevItem.getAgentId().equals(item.getAgentId())) {
					addCsvGroupTotal(csvWriter, prevItem, sumMap, prevSumMap);
				}
				csvWriter.writeNext(item.toArray());

				prevItem = item;
			}
			addCsvGroupTotal(csvWriter, prevItem, sumMap, prevSumMap);
			return f;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				csvWriter.close();
			} catch (IOException e) {
			}
		}
	}

	private void addCsvGroupTotal(CSVWriter csvWriter, AgentInvoiceSummary prevItem, Map<String, BigDecimal[]> sumMap,
			Map<String, BigDecimal[]> prevSumMap) {
		if (prevItem == null)
			return;
		AgentInvoiceSummary totalSummary = new AgentInvoiceSummary();
		totalSummary.setAgentName(prevItem.getAgentName());
		totalSummary.setBusinessType("Total");
		totalSummary.setSalesRepName("Total");
		totalSummary.setAmounts(sumMap.get(prevItem.getAgentId()));
		csvWriter.writeNext(totalSummary.toArray());

		totalSummary = new AgentInvoiceSummary();
		totalSummary.setAgentName(prevItem.getAgentName());
		totalSummary.setBusinessType("Total Previous");
		totalSummary.setSalesRepName("Total Previous");
		totalSummary.setAmounts(prevSumMap.get(prevItem.getAgentId()));
		csvWriter.writeNext(totalSummary.toArray());
	}

	private AgentInvoiceSummary createDataItem4InvoiceSummay(Map.Entry<String, BigDecimal[]> entry) {
		AgentInvoiceSummary item = new AgentInvoiceSummary();

		String[] keyArr = entry.getKey().split(spliterFlag);
		item.setAgentId(keyArr[0]);
		item.setAgentName(agentService.getAgent(keyArr[0]).getName());

		String bizType = keyArr[1];
		String bizTypeName = ((BusinessType) BeanUtils.getEnum(BusinessType.class, bizType)).name();
		item.setBusinessType(bizTypeName);

		String saleRep = keyArr[2];
		String saleRepName = userService.getUser(saleRep).getName();
		item.setSalesRepName(saleRepName);

		item.setAmounts(entry.getValue());

		return item;
	}

	private String spliterFlag = "_";

	private class Result4Ais {
		private Date[] dateHeaders;
		private Map<String, BigDecimal[]> rawDataMap;
		private Map<String, BigDecimal[]> rawDataSumMap;
		private Map<String, BigDecimal[]> rawDataPreviousSumMap;
		private Map<String, BigDecimal[]> rawDataAgentBizTypeSumMap;
		private Map<String, Map<String, Map<String, BigDecimal[]>>> allMap;
		private BigDecimal[] total4All;
		private BigDecimal[] lastYearTotal4All;

		public Result4Ais(Date[] dateHeaders, Map<String, BigDecimal[]> rawDataMap, Map<String, BigDecimal[]> rawDataSumMap,
				Map<String, BigDecimal[]> rawDataPreviousSumMap, Map<String, BigDecimal[]> rawDataAgentBizTypeSumMap,
				Map<String, Map<String, Map<String, BigDecimal[]>>> allMap, BigDecimal[] total4All, BigDecimal[] lastYearTotal4All) {
			super();
			this.dateHeaders = dateHeaders;
			this.rawDataMap = rawDataMap;
			this.rawDataSumMap = rawDataSumMap;
			this.rawDataPreviousSumMap = rawDataPreviousSumMap;
			this.rawDataAgentBizTypeSumMap = rawDataAgentBizTypeSumMap;
			this.allMap = allMap;
			this.total4All = total4All;
			this.lastYearTotal4All = lastYearTotal4All;
		}

		public Date[] getDateHeaderDef() {
			return dateHeaders;
		}

		public Map<String, BigDecimal[]> getRawDataMap() {
			return rawDataMap;
		}

		public Map<String, BigDecimal[]> getRawDataSumMap() {
			return rawDataSumMap;
		}

		public Map<String, BigDecimal[]> getRawDataPreviousSumMap() {
			return rawDataPreviousSumMap;
		}

		public Map<String, BigDecimal[]> getRawDataAgentBizTypeSumMap() {
			return rawDataAgentBizTypeSumMap;
		}

		public Map<String, Map<String, Map<String, BigDecimal[]>>> getAllMap() {
			return allMap;
		}

		public BigDecimal[] getTotal4All() {
			return total4All;
		}

		public BigDecimal[] getLastYearTotal4All() {
			return lastYearTotal4All != null ? lastYearTotal4All : new BigDecimal[AisRowModel.arrySize];
		}

	}

	private Result4Ais generateReportData4AgentInvoiceSummary() {
		int arrySize = AisRowModel.arrySize;
		Calendar calendar = getAuTime();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1);

		FilterMaker filterMaker = invoiceDao.getFilterMaker();
		FilterItem filterItem = filterMaker.makeDateRange("invoiceDate", DateUtils.addYears(calendar.getTime(), -2), calendar.getTime(), true, false);
		Iterable<InvoiceSearchBean> beans = invoiceDao.queryInvoice(filterItem);

		DateRangeLocater normalLocater = new DateRangeLocater(calendar, Calendar.MONTH, -1, 12);
		DateRangeLocater beforeLastYearLocater = new DateRangeLocater(calendar, Calendar.MONTH, -1, 12);
		Date lastYearBegin = normalLocater.getRangeSpliters()[0];

		Map<String, BigDecimal[]> rawDataMap = new HashMap<String, BigDecimal[]>();
		Map<String, BigDecimal[]> rawDataSumMap = new HashMap<String, BigDecimal[]>();
		Map<String, BigDecimal[]> rawDataPreviousSumMap = new HashMap<String, BigDecimal[]>();
		Map<String, BigDecimal[]> rawDataAgentBizTypeSumMap = new HashMap<String, BigDecimal[]>();
		Map<String, Map<String, Map<String, BigDecimal[]>>> allMap = new TreeMap<String, Map<String, Map<String, BigDecimal[]>>>();
		BigDecimal[] total4All = new BigDecimal[arrySize];
		BigDecimal[] lastYearTotal4All = new BigDecimal[arrySize];
		for (InvoiceSearchBean searchBean : beans) {
			Date invoiceDate = searchBean.getInvoiceDate();
			BigDecimal amount = searchBean.getTotalAmount();
			amount = amount == null ? BigDecimal.ZERO : amount;
			String agentId = searchBean.getAgentId().toString();
			String bizType = searchBean.getBusinessType().name();
			String saleRepId = searchBean.getSalesRepId().toString();

			if (invoiceDate.before(lastYearBegin)) {
				String totalPreviousKey = agentId;
				BigDecimal[] arr = getOrCreateKey(rawDataPreviousSumMap, totalPreviousKey, arrySize);
				int arrIndex = beforeLastYearLocater.locateAt(invoiceDate);
				addMount(arrIndex, amount, arr);
				rawDataPreviousSumMap.put(totalPreviousKey, arr);
				// all
				addMount(arrIndex, amount, lastYearTotal4All);
			} else {
				int arrIndex = normalLocater.locateAt(invoiceDate);
				String key = searchBean.getAgentId() + spliterFlag + bizType + spliterFlag + saleRepId;
				BigDecimal[] rowAmounts = getOrCreateKey(rawDataMap, key, arrySize);
				addMount(arrIndex, amount, rowAmounts);
				rawDataMap.put(key, rowAmounts);
				// agent
				String totalKey = agentId;
				rowAmounts = getOrCreateKey(rawDataSumMap, totalKey, arrySize);
				addMount(arrIndex, amount, rowAmounts);
				rawDataSumMap.put(totalKey, rowAmounts);
				// agent+biz type
				String agentBizTypeKey = searchBean.getAgentId() + spliterFlag + bizType;
				rowAmounts = getOrCreateKey(rawDataAgentBizTypeSumMap, agentBizTypeKey, arrySize);
				addMount(arrIndex, amount, rowAmounts);
				rawDataAgentBizTypeSumMap.put(agentBizTypeKey, rowAmounts);
				// multi depth map
				addToAllMap(allMap, agentId, bizType, saleRepId, rowAmounts);
				// all
				addMount(arrIndex, amount, total4All);
			}
		}
		Date[] rangeDef = normalLocater.getRangeSpliters();
		return new Result4Ais(rangeDef, rawDataMap, rawDataSumMap, rawDataPreviousSumMap, rawDataAgentBizTypeSumMap, allMap, total4All,
				lastYearTotal4All);
		// return new Pair(rangeDef,new Map[]{new
		// TreeMap(rawDataMap),rawDataSumMap,rawDataPreviousSumMap,rawDataAgentBizTypeSumMap,allMap});
	}

	private void addToAllMap(Map<String, Map<String, Map<String, BigDecimal[]>>> allMap, String agentId, String bizType, String saleRepId,
			BigDecimal[] rowAmounts) {
		Map<String, Map<String, BigDecimal[]>> agentMap = allMap.get(agentId);
		if (agentMap == null)
			agentMap = new TreeMap<String, Map<String, BigDecimal[]>>();

		Map<String, BigDecimal[]> bizTypeMap = agentMap.get(bizType);
		if (bizTypeMap == null)
			bizTypeMap = new TreeMap<String, BigDecimal[]>();

		bizTypeMap.put(saleRepId, rowAmounts);
		agentMap.put(bizType, bizTypeMap);
		allMap.put(agentId, agentMap);
	}

	private void addMount(int arrIndex, BigDecimal amount, BigDecimal[] arr) {
		amount = amount == null ? BigDecimal.ZERO : amount;
		BigDecimal oldAmmount = arr[arrIndex];
		oldAmmount = oldAmmount == null ? BigDecimal.ZERO : oldAmmount;
		if (amount == null) {
			amount = BigDecimal.ZERO;
		}
		arr[arrIndex] = oldAmmount.add(amount);
	}

	private BigDecimal[] getOrCreateKey(Map<String, BigDecimal[]> stResult, String key, int size) {
		BigDecimal[] arr = stResult.get(key);
		if (arr == null) {
			arr = new BigDecimal[size];
		}
		return arr;
	}

}
