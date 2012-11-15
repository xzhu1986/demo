package au.com.isell.rlm.importing.function.reseller;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import au.com.isell.common.util.SecurityUtils;
import au.com.isell.common.util.StringUtils;
import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.importing.utils.DateUtils;
import au.com.isell.rlm.importing.utils.SpringUtils;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.agent.service.impl.AgentServiceImpl;
import au.com.isell.rlm.module.reseller.domain.BillingInfo;
import au.com.isell.rlm.module.reseller.domain.LicenseType;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.domain.license.EComLicense;
import au.com.isell.rlm.module.reseller.domain.license.EComLicense.EComType;
import au.com.isell.rlm.module.reseller.domain.license.EPDLicense;
import au.com.isell.rlm.module.reseller.domain.license.EPDLicense.Level;
import au.com.isell.rlm.module.reseller.domain.license.ITQLicense;
import au.com.isell.rlm.module.reseller.domain.license.LicModule;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.PermissionService;
import au.com.isell.rlm.module.user.service.UserService;
import au.com.isell.rlm.module.user.service.impl.PermissionServiceImpl;
import au.com.isell.rlm.module.user.service.impl.UserServiceImpl;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class ResellerStoreFunction extends BaseOperation<ResellerStoreFunction.Context> implements Function<ResellerStoreFunction.Context> {
	
	private static final long serialVersionUID = 1089526440864934362L;
	
	protected static class Context {
		Map<String, UUID> agentMap;
		Map<String, UUID> salesRepMap;
		Set<String> rpPermissions;
		
		List<Reseller> resellers;
		List<ResellerUser> resellerUsers;
		List<ResellerSearchBean> resellerSearchBeans;
		
		public Context(Map<String, UUID> agentM,Map<String, UUID> salesRepM,Set<String> rpPerms) {
			resellerUsers = new ArrayList<ResellerUser>(Constants.MAX_OBJECTS_PER_ADD);
			resellers = new ArrayList<Reseller>(Constants.MAX_OBJECTS_PER_ADD);
			resellerSearchBeans = new ArrayList<ResellerSearchBean>(Constants.MAX_OBJECTS_PER_ADD);
			agentMap = Collections.unmodifiableMap(agentM);
			salesRepMap = Collections.unmodifiableMap(salesRepM);
			rpPermissions = Collections.unmodifiableSet(rpPerms);
		}
		public Context reset() {
			resellerUsers.clear();
			resellers.clear();
			resellerSearchBeans.clear();
			return this;
		}
	}
	
	@ConstructorProperties({ "fieldDeclaration" })
	public ResellerStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<ResellerStoreFunction.Context> operationCall) {
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
			
			PermissionService permissionService = SpringUtils.getBean("permissionServiceImpl", PermissionServiceImpl.class);
			
			operationCall.setContext(new Context(agentM,salesRepM,permissionService.getResellerPortalPermissions()));
		}
		
	}
	
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<ResellerStoreFunction.Context> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);
		String serialNumber = arguments.getString(ResellerColumns.SerialNumber);
		String creationDate = arguments.getString(ResellerColumns.CreationDate);
		String version = arguments.getString(ResellerColumns.Version);
		int status = arguments.getInteger(ResellerColumns.Status);
		int type = arguments.getInteger(ResellerColumns.Type);
		String agency = arguments.getString(ResellerColumns.Agency);
		String salesRep = arguments.getString(ResellerColumns.SalesRep);
		String companyName = arguments.getString(ResellerColumns.CompanyName);
		String lastName = arguments.getString(ResellerColumns.LastName);
		String firstName = arguments.getString(ResellerColumns.FirstName);
		String jobPosition = arguments.getString(ResellerColumns.JobPosition);
		String emailAddress = arguments.getString(ResellerColumns.EmailAddress);
		String address1 = arguments.getString(ResellerColumns.Address1);
		String address2 = arguments.getString(ResellerColumns.Address2);
		String city = arguments.getString(ResellerColumns.City);
		String state = arguments.getString(ResellerColumns.State);
		String postcode = arguments.getString(ResellerColumns.Postcode);
		String countryCode = arguments.getString(ResellerColumns.CountryCode);
		String phone = arguments.getString(ResellerColumns.Phone);
		String mobile = arguments.getString(ResellerColumns.Mobile);
		String billingFirstName = arguments.getString(ResellerColumns.BillingFirstName);
		String billingLastName = arguments.getString(ResellerColumns.BillingLastName);
		String billingPhone = arguments.getString(ResellerColumns.BillingPhone);
		String billingEmailAddress = arguments.getString(ResellerColumns.BillingEmailAddress);
		String billingEmailAddressCC = arguments.getString(ResellerColumns.BillingEmailAddressCC);
		String billingCurrency = arguments.getString(ResellerColumns.BillingCurrency);
		int billingGST = arguments.getInteger(ResellerColumns.BillingGST);
		String billingComments = arguments.getString(ResellerColumns.BillingComments);
		String iTQuoterRenewalDate = arguments.getString(ResellerColumns.ITQuoterRenewalDate);
		int iTQuoterAnnualFee = arguments.getInteger(ResellerColumns.ITQuoterAnnualFee);
		int iTQuoterFullAccessUsers = arguments.getInteger(ResellerColumns.ITQuoterFullAccessUsers);
		int iTQuoterCrmUsers = arguments.getInteger(ResellerColumns.ITQuoterCrmUsers);
		int iTQUoterTempUsers = arguments.getInteger(ResellerColumns.ITQUoterTempUsers);
		String iTQuoterTempUserExpiry = arguments.getString(ResellerColumns.ITQuoterTempUserExpiry);
		String ecomRenewalDate = arguments.getString(ResellerColumns.EcomRenewalDate);
		int ecomAnnualFee = arguments.getInteger(ResellerColumns.EcomAnnualFee);
		int ecomType = arguments.getInteger(ResellerColumns.EcomType);
		String ecomMultiCatalogues = arguments.getString(ResellerColumns.EcomMultiCatalogues);
		String ecomExportCatalogues = arguments.getString(ResellerColumns.EcomExportCatalogues);
		int ecomWebPortals = arguments.getInteger(ResellerColumns.EcomWebPortals);
		String ePDRenewalDate = arguments.getString(ResellerColumns.EPDRenewalDate);
		int ePDAnnualFee = arguments.getInteger(ResellerColumns.EPDAnnualFee);
		int ePDITQuoterUsage = arguments.getInteger(ResellerColumns.EPDITQuoterUsage);
		int ePDEcomUsage = arguments.getInteger(ResellerColumns.EPDEcomUsage);
		String ePDAgency = arguments.getString(ResellerColumns.EPDAgency);
		String licenseImageFeeds = arguments.getString(ResellerColumns.LicenseImageFeeds);
		String licenseBidImporter = arguments.getString(ResellerColumns.LicenseBidImporter);
		String licenseApiSupport = arguments.getString(ResellerColumns.LicenseApiSupport);
		String licenseForeignCurrency = arguments.getString(ResellerColumns.LicenseForeignCurrency);
		String licenseSilentUpgrades = arguments.getString(ResellerColumns.LicenseSilentUpgrades);
		String licenseServiceManager = arguments.getString(ResellerColumns.LicenseServiceManager);
		String licenseWarehouseModule = arguments.getString(ResellerColumns.LicenseWarehouseModule);
		String resellerUsername = arguments.getString(ResellerColumns.ResellerUsername);
		String resellerPassword = arguments.getString(ResellerColumns.ResellerPassword);
		String nextReviewDate = arguments.getString(ResellerColumns.NextReviewDate);
		
		UUID resellerUserID = UUID.fromString(arguments.getString(ResellerColumns.ResellerUserID));
		int resellerBreakMapCount = arguments.getInteger(ResellerColumns.ResellerBreakMapCount);
		
		Context context = functionCall.getContext();
		ResellerUser resellerUser = makeResellerUser(serialNumber,companyName, firstName, lastName,jobPosition, emailAddress,resellerUserID,phone,mobile,resellerPassword,context.rpPermissions);
		context.resellerUsers.add(resellerUser);
		
		Reseller reseller = makeReseller(serialNumber,companyName, resellerUser.getUserId(),countryCode, address1,address2,city,state,postcode,creationDate,status,version,type,context.agentMap.get(agency==null?null:agency.toLowerCase()),context.salesRepMap.get(salesRep==null?null:salesRep.toLowerCase()),nextReviewDate);
		reseller.setBillingInfo(makeBillInfo(billingFirstName,billingLastName,billingPhone,billingEmailAddress,billingEmailAddressCC,billingCurrency,billingGST,billingComments));
		reseller.setLicenses(makeLicenses(iTQuoterRenewalDate,iTQuoterAnnualFee,iTQuoterFullAccessUsers,iTQuoterCrmUsers,iTQUoterTempUsers,iTQuoterTempUserExpiry,
				licenseApiSupport,licenseForeignCurrency,licenseSilentUpgrades,licenseServiceManager,licenseWarehouseModule,licenseImageFeeds,
				licenseBidImporter,ecomRenewalDate,ecomAnnualFee,ecomType,ecomMultiCatalogues,ecomExportCatalogues,ecomWebPortals,
				ePDRenewalDate,ePDAnnualFee,ePDITQuoterUsage,ePDEcomUsage,context.agentMap.get(ePDAgency==null?null:ePDAgency.toLowerCase())));
		context.resellers.add(reseller);
		
		ResellerSearchBean resellerSearchBean = new ResellerSearchBean(reseller,null,arguments.getString(ResellerColumns.PhoneCountryCode));
		resellerSearchBean.setSupplierApprovalRemain(resellerBreakMapCount);
		
		context.resellerSearchBeans.add(resellerSearchBean);
		
		flushInputObjects(context,false);;
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<ResellerStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		flushInputObjects(context,true);
	}
	

	private Reseller makeReseller(String serialNumber,String companyName, UUID resellerUserID, String countryCode, String address1, String address2,String city,
									String state,String postcode,String creationDate,int status,String version,int type,UUID agencyId,UUID salesRepID,String nextReviewDate) {
		Reseller reseller = new Reseller();
		
		if("uk".equalsIgnoreCase(countryCode)){
			reseller.setCountry("gb");
		}else{
			reseller.setCountry(StringUtils.isNotEmpty(countryCode)?countryCode.toLowerCase():null);
		}
		reseller.setCompany(companyName);
		reseller.setAddress(new GeneralAddress(address1,address2,null,city,state,reseller.getCountry(),postcode));
		try {
			reseller.setCreatedDatetime(DateUtils.parseDate(DateUtils.DF_SHORT, creationDate));
			reseller.setNextReviewDate(DateUtils.parseDate(DateUtils.DF_SHORT, nextReviewDate));
		} catch (ParseException e) {
		}
		reseller.setSalesRepID(salesRepID);
		reseller.setSerialNo(Integer.parseInt(serialNumber));
		reseller.setStatus(ResellerStatus.values()[status-1]);
		reseller.setType(ResellerType.values()[type-1]);
		reseller.setVersion(version);
		reseller.setUserId(resellerUserID);
		reseller.setAgencyId(agencyId);
		
		return reseller;
	}

	private ResellerUser makeResellerUser(String serialNumber,String companyName, String firstName, String lastName,String jobPosition, String emailAddress,
			UUID resellerUserID,String phone,String mobile,String resellerPassword,Set<String> rpPermissions) {
		ResellerUser user = new ResellerUser();
		user.setResellerName(companyName);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(emailAddress);
		user.setStatus(UserStatus.ACTIVE);
		user.setLoginFailureCount(0);
		user.setUserId(resellerUserID);
		user.setSerialNo(Integer.parseInt(serialNumber));
		user.setPhone(phone);
		user.setMobile(mobile);
		user.setJobPosition(jobPosition);
		user.setPrimary(true);
		user.setPermissions(rpPermissions);
		user.setUsername(user.getToken());
		user.setPassword(SecurityUtils.digestPassword(user.getToken(), resellerPassword));
		return user;
	}

	private BillingInfo makeBillInfo(String billingFirstName,String billingLastName,String billingPhone,String billingEmailAddress,
			String billingEmailAddressCC,String billingCurrency,int billingGST,String billingComments) {
		BillingInfo billingInfo = new BillingInfo();
		billingInfo.setEmail(billingEmailAddress);
		billingInfo.setFirstName(billingFirstName);
		billingInfo.setLastName(billingLastName);
		billingInfo.setPhone(billingPhone);
		billingInfo.setAdditionalEmail(billingEmailAddressCC);
		billingInfo.setCurrency(StringUtils.isNotEmpty(billingCurrency)?billingCurrency.toUpperCase():null);
		billingInfo.setGst(BigDecimal.valueOf(billingGST).setScale(2, BigDecimal.ROUND_HALF_UP));
		billingInfo.setComments(billingComments);
		return billingInfo;
	}

	private Map<LicenseType, LicModule> makeLicenses(String iTQuoterRenewalDate,int iTQuoterAnnualFee,int iTQuoterFullAccessUsers,int iTQuoterCrmUsers,int iTQUoterTempUsers,String iTQuoterTempUserExpiry,
					String licenseApiSupport,String licenseForeignCurrency,String licenseSilentUpgrades,String licenseServiceManager,String licenseWarehouseModule,String licenseImageFeeds,
					String licenseBidImporter,String ecomRenewalDate,int ecomAnnualFee,int ecomType,String ecomMultiCatalogues,String ecomExportCatalogues,int ecomWebPortals,
					String ePDRenewalDate,int ePDAnnualFee,int ePDITQuoterUsage,int ePDEcomUsage,UUID ePDAgencyID) {
		Map<LicenseType, LicModule> licenses = new HashMap<LicenseType, LicModule>();
		ITQLicense itqLicense = new ITQLicense();
		itqLicense.setAnnualFee(BigDecimal.valueOf(iTQuoterAnnualFee).setScale(2, BigDecimal.ROUND_HALF_UP));
		itqLicense.setItqApiSupport(Constants.BooleanMap.get(licenseApiSupport));
		itqLicense.setItqCrmUsers(iTQuoterCrmUsers);
		itqLicense.setItqForeignCurrency(Constants.BooleanMap.get(licenseForeignCurrency));
		
		itqLicense.setItqFullAccessUsers(iTQuoterFullAccessUsers);
		itqLicense.setItqTempUsers(iTQUoterTempUsers);
		itqLicense.setItqServiceManger(Constants.BooleanMap.get(licenseServiceManager));
		itqLicense.setItqSilentUpgrades(Constants.BooleanMap.get(licenseSilentUpgrades));
		try {
			itqLicense.setItqTempExpiryDate(DateUtils.parseDate(DateUtils.DF_SHORT,iTQuoterTempUserExpiry));
			itqLicense.setRenewalDate(DateUtils.parseDate(DateUtils.DF_SHORT,iTQuoterRenewalDate));
		} catch (ParseException e) {
		}
		itqLicense.setItqWarehouseModule(Constants.BooleanMap.get(licenseWarehouseModule));
		itqLicense.setBidImporter(Constants.BooleanMap.get(licenseBidImporter));
		itqLicense.setItqImageFeeds(Constants.BooleanMap.get(licenseImageFeeds));
		licenses.put(LicenseType.ITQLicense, itqLicense);

		EComLicense eComLicense = new EComLicense();
		eComLicense.setAnnualFee(BigDecimal.valueOf(ecomAnnualFee).setScale(2, BigDecimal.ROUND_HALF_UP));
		eComLicense.setEcomExportCatalogues(Constants.BooleanMap.get(ecomExportCatalogues));
		
		eComLicense.setEcomMultiCatalogues(Constants.BooleanMap.get(ecomMultiCatalogues));
		
		eComLicense.setEcomWebPortals(ecomWebPortals);
		eComLicense.setEcomType(EComType.values()[ecomType-1]);
		try {
			eComLicense.setRenewalDate(DateUtils.parseDate(DateUtils.DF_SHORT,ecomRenewalDate));
		} catch (ParseException e) {
		}
		licenses.put(LicenseType.EComLicense, eComLicense);

		EPDLicense epdLicense = new EPDLicense();
		epdLicense.setAnnualFee(BigDecimal.valueOf(ePDAnnualFee).setScale(2, BigDecimal.ROUND_HALF_UP));
		epdLicense.setEpdAgencyID(ePDAgencyID);
		epdLicense.setEpdEcomUsage(Level.values()[ePDEcomUsage]);//not -1
		epdLicense.setEpdITQuoterUsage(Level.values()[ePDITQuoterUsage]);//not -1
		try {
			epdLicense.setRenewalDate(DateUtils.parseDate(DateUtils.DF_SHORT,ePDRenewalDate));
		} catch (ParseException e) {
		}
		licenses.put(LicenseType.EPDLicense, epdLicense);

		return licenses;
	}
	

	private void flushInputObjects(Context context, boolean force) {
		if ((force && (context.resellers.size() > 0)) || (context.resellers.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(context.resellers.toArray(new Reseller[context.resellers.size()]));
			StoreHelper.save(context.resellerUsers.toArray(new ResellerUser[context.resellerUsers.size()]));
			StoreHelper.indexValues(context.resellerSearchBeans.toArray(new ResellerSearchBean[context.resellerSearchBeans.size()]));

			context.reset();
		}
	}
}
