package au.com.isell.rlm.importing.function.invoices;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.invoices.InvoicesColumns;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.importing.utils.DateUtils;
import au.com.isell.rlm.importing.utils.SpringUtils;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.agent.service.impl.AgentServiceImpl;
import au.com.isell.rlm.module.invoice.domain.Invoice;
import au.com.isell.rlm.module.invoice.domain.Invoice.AmountType;
import au.com.isell.rlm.module.invoice.domain.Invoice.BusinessType;
import au.com.isell.rlm.module.invoice.domain.Invoice.InvoiceStatus;
import au.com.isell.rlm.module.invoice.domain.Invoice.PaymentProbaility;
import au.com.isell.rlm.module.invoice.domain.Invoice.PaymentTerm;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.UserService;
import au.com.isell.rlm.module.user.service.impl.UserServiceImpl;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class InvoicesStoreFunction extends BaseOperation<InvoicesStoreFunction.Context> implements Function<InvoicesStoreFunction.Context> {
	
	private static final long serialVersionUID = 1089526440864934362L;
	protected static class Context {
		Map<String, UUID> agentMap;
		Map<String, UUID> salesRepMap;
		
		List<Invoice> invoices;
		List<InvoiceSearchBean> invoiceSearchBeans;
		
		public Context(Map<String, UUID> agentM,Map<String, UUID> salesRepM) {
			invoices = new ArrayList<Invoice>(Constants.MAX_OBJECTS_PER_ADD);
			invoiceSearchBeans = new ArrayList<InvoiceSearchBean>(Constants.MAX_OBJECTS_PER_ADD);
			agentMap = Collections.unmodifiableMap(agentM);
			salesRepMap = Collections.unmodifiableMap(salesRepM);
		}
		public Context reset() {
			invoices.clear();
			invoiceSearchBeans.clear();
			return this;
		}
	}
	
	@ConstructorProperties({ "fieldDeclaration" })
	public InvoicesStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<InvoicesStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		if (context != null){
			context.reset();
		}else{
			Map<String, UUID> agentM = new HashMap<String, UUID>();
			AgentService agentService = SpringUtils.getBean("agentServiceImpl", AgentServiceImpl.class);
			List<Agent> agents = agentService.getAgencyList();
			if (agents != null) {
				for (Agent agent : agents) {
					agentM.put(agent.getName().toLowerCase(), agent.getAgentId());
				}
			}
			
			Map<String, UUID> salesRepM = new HashMap<String, UUID>();
			UserService userService = SpringUtils.getBean("userServiceImpl", UserServiceImpl.class);
			List<User> users = userService.getSalesRepList(null);
			if (users != null) {
				for (User user : users) {
					salesRepM.put(user.getUsername().toLowerCase(), user.getUserId());
				}
			}
			operationCall.setContext(new Context(agentM,salesRepM));
		}
		
	}
	
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<InvoicesStoreFunction.Context> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);
		
		String resellerSerialNumber = arguments.getString(InvoicesColumns.ResellerSerialNumber);
		String resellerCompanyName = arguments.getString(InvoicesColumns.ResellerCompanyName);
		int invoiceNumber = arguments.getInteger(InvoicesColumns.InvoiceNumber);
		int status = arguments.getInteger(InvoicesColumns.Status);
		String createdDate = arguments.getString(InvoicesColumns.CreatedDate);
		String invoiceDate = arguments.getString(InvoicesColumns.InvoiceDate);
		String followupDate = arguments.getString(InvoicesColumns.FollowupDate);
		String fullyPaidDate = arguments.getString(InvoicesColumns.FullyPaidDate);
		int paymentTerms = arguments.getInteger(InvoicesColumns.PaymentTerms);
		String invoiceReportID = arguments.getString(InvoicesColumns.InvoiceReportID);
		String agentID = arguments.getString(InvoicesColumns.AgentID);
		String salesRepID = arguments.getString(InvoicesColumns.SalesRepID);
		String toFirstName = arguments.getString(InvoicesColumns.ToFirstName);
		String toLastName = arguments.getString(InvoicesColumns.ToLastName);
		String toPhoneNumber = arguments.getString(InvoicesColumns.ToPhoneNumber);
		String toMobileNumber = arguments.getString(InvoicesColumns.ToMobileNumber);
		String toEmailAddress = arguments.getString(InvoicesColumns.ToEmailAddress);
		String toEmailAddressCC = arguments.getString(InvoicesColumns.ToEmailAddressCC);
		double amountITQuoter = arguments.getDouble(InvoicesColumns.AmountITQuoter);
		double amountEcommerce = arguments.getDouble(InvoicesColumns.AmountEcommerce);
		double amountEPD = arguments.getDouble(InvoicesColumns.AmountEPD);
		double amountServices = arguments.getDouble(InvoicesColumns.AmountServices);
		double amountOther = arguments.getDouble(InvoicesColumns.AmountOther);
		String totalAmount = arguments.getString(InvoicesColumns.TotalAmount);
		String totalAgentCommissions = arguments.getString(InvoicesColumns.TotalAgentCommissions);
		String totalPayments = arguments.getString(InvoicesColumns.TotalPayments);
		String currencyName = arguments.getString(InvoicesColumns.CurrencyName);
		double gstRate = arguments.getDouble(InvoicesColumns.GstRate);
		String gstName = arguments.getString(InvoicesColumns.GstName);
		String internalNotes = arguments.getString(InvoicesColumns.InternalNotes);
		String invoiceDetails = arguments.getString(InvoicesColumns.InvoiceDetails);
		String invoiceTerms = arguments.getString(InvoicesColumns.InvoiceTerms);
		String newBusiness = arguments.getString(InvoicesColumns.NewBusiness);
		
		double totalPaid = arguments.getDouble(InvoicesColumns.TotalPaid);

		String address1 = arguments.getString(ResellerColumns.Address1);
		String address2 = arguments.getString(ResellerColumns.Address2);
		String city = arguments.getString(ResellerColumns.City);
		String regionCode = arguments.getString(ResellerColumns.State);
		String postcode = arguments.getString(ResellerColumns.Postcode);
		String countryCode = arguments.getString(ResellerColumns.CountryCode);
		String phoneCountryCode = arguments.getString(ResellerColumns.PhoneCountryCode);
		
		Address address = new GeneralAddress(address1,address2,null,city,regionCode,countryCode,postcode);
		
		Map<AmountType, BigDecimal> amounts = new HashMap<AmountType, BigDecimal>();
		amounts.put(AmountType.ITQuoter, BigDecimal.valueOf(amountITQuoter).setScale(2, BigDecimal.ROUND_HALF_UP));
		amounts.put(AmountType.ECom, BigDecimal.valueOf(amountEcommerce).setScale(2, BigDecimal.ROUND_HALF_UP));
		amounts.put(AmountType.EPD, BigDecimal.valueOf(amountEPD).setScale(2, BigDecimal.ROUND_HALF_UP));
		amounts.put(AmountType.Serivce, BigDecimal.valueOf(amountServices).setScale(2, BigDecimal.ROUND_HALF_UP));
		amounts.put(AmountType.Other, BigDecimal.valueOf(amountOther).setScale(2, BigDecimal.ROUND_HALF_UP));
		
		
		Context context = functionCall.getContext();
		
		Invoice invoice = makeInvoice(resellerSerialNumber,resellerCompanyName,invoiceNumber,status,paymentTerms,createdDate,invoiceDate,followupDate,
				context.agentMap.get(agentID==null?null:agentID.toLowerCase()),context.salesRepMap.get(salesRepID==null?null:salesRepID.toLowerCase()),
				toFirstName,toLastName,toPhoneNumber,toMobileNumber,toEmailAddress,toEmailAddressCC,amounts,currencyName,internalNotes,
				invoiceDetails,invoiceTerms,gstRate,gstName,regionCode,address,newBusiness);
		context.invoices.add(invoice);
		
		Agent agent = new Agent();
		agent.setName(agentID);
		Date fullPaidDate = null;
		try {
			fullPaidDate = DateUtils.parseDate(DateUtils.DF_SHORT,fullyPaidDate);
		} catch (Exception e) {
		}
		context.invoiceSearchBeans.add(new InvoiceSearchBean(invoice, agent, BigDecimal.valueOf(totalPaid).setScale(2, BigDecimal.ROUND_HALF_UP),phoneCountryCode,fullPaidDate));
		flushInputObjects(context,false);
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<InvoicesStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		flushInputObjects(context,true);
	}
	

	private Invoice makeInvoice(String resellerSerialNumber,String resellerCompanyName,int invoiceNumber, int status, int paymentTerms, String createdDate, String invoiceDate,String followupDate,
									UUID agentId,UUID salesRepId,String toFirstName,String toLastName,String toPhoneNumber,
									String toMobileNumber,String toEmailAddress,String toEmailAddressCC,Map<AmountType, BigDecimal> amounts,String currencyName,
									String internalNotes,String invoiceDetails,String invoiceTerms,double gstRate,String gstName,String regionCode,Address address,String newBusiness) {
		Invoice invoice = new Invoice();
		invoice.setResellerSerialNo(Integer.parseInt(resellerSerialNumber.trim()));
		invoice.setCompanyName(resellerCompanyName);
		invoice.setCompanyAddress(address);
		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setStatus(InvoiceStatus.values()[status-1]);
		invoice.setPaymentTerm(PaymentTerm.values()[paymentTerms-1]);
		try {
			invoice.setCreatedDate(DateUtils.parseDate(DateUtils.DF_SHORT,createdDate));
			invoice.setInvoiceDate(DateUtils.parseDate(DateUtils.DF_SHORT,invoiceDate));
			invoice.setFollowupDate(DateUtils.parseDate(DateUtils.DF_SHORT,followupDate));
		} catch (ParseException e) {
		}
		invoice.setReportId(null);
		invoice.setAgentId(agentId);
		invoice.setSalesRepId(salesRepId);
		invoice.setToFirstName(toFirstName);
		invoice.setToLastName(toLastName);
		invoice.setToPhone(toPhoneNumber);
		invoice.setToMobile(toMobileNumber);
		invoice.setToEmail(toEmailAddress);
		invoice.setToEmailcc(toEmailAddressCC);
		invoice.setAmounts(amounts);
		invoice.setCurrency(currencyName==null?"":currencyName.toUpperCase());
		invoice.setNotes(internalNotes);
		invoice.setDetails(invoiceDetails==null?"":invoiceDetails.replaceAll("\\\\n","\r\n"));
		invoice.setTerms(invoiceTerms==null?"":invoiceTerms.replaceAll("\\\\n","\r\n"));
		invoice.setTaxName(gstName);
		invoice.setTaxRate(BigDecimal.valueOf(gstRate).setScale(2, BigDecimal.ROUND_HALF_UP));
		invoice.setRegionCode(regionCode);
		invoice.setProbability(PaymentProbaility.Unconfirmed);
		if("1".equals(newBusiness)){
			invoice.setBusinessType(BusinessType.NewBusiness);
		}else{
			invoice.setBusinessType(BusinessType.Renewal);
		}
		return invoice;
	}

	private void flushInputObjects(Context context, boolean force) {
		if ((force && (context.invoices.size() > 0)) || (context.invoices.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(context.invoices.toArray(new Invoice[context.invoices.size()]));
			StoreHelper.indexValues(context.invoiceSearchBeans.toArray(new InvoiceSearchBean[context.invoiceSearchBeans.size()]));
			context.reset();
		}
	}
}
