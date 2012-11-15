package au.com.isell.rlm.importing.aggregator.supplier;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;

import au.com.isell.common.util.SecurityUtils;
import au.com.isell.common.util.StringUtils;
import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.supplier.SupplierColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.module.address.domain.Address;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.supplier.domain.DataRequest;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierBillingInfo;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdate;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdateExecute;
import au.com.isell.rlm.module.supplier.domain.SupplierUser;
import au.com.isell.rlm.module.user.constant.UserStatus;
import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class SupplierStoreAggregator extends BaseOperation<SupplierStoreAggregator.Context> implements Aggregator<SupplierStoreAggregator.Context> {
	private static final long serialVersionUID = 1089526440864934362L;
	
	protected static class Context {
		List<Supplier> suppliers;
		List<SupplierUser> supplierUsers;
		List<SupplierSearchBean> supplierSearchBeans;
		List<SupplierUpdateExecute> supplierUpdateExecutes;
		List<Schedule> schedules;
		Map<UUID,PriceBreak> priceBreaks;
		List<PriceBreak> indexPriceBreaks;
		
		public Context() {
			supplierUsers = new ArrayList<SupplierUser>(Constants.MAX_OBJECTS_PER_ADD);
			suppliers = new ArrayList<Supplier>(Constants.MAX_OBJECTS_PER_ADD);
			supplierSearchBeans = new ArrayList<SupplierSearchBean>(Constants.MAX_OBJECTS_PER_ADD);
			supplierUpdateExecutes = new ArrayList<SupplierUpdateExecute>(Constants.MAX_OBJECTS_PER_ADD);
			schedules = new ArrayList<Schedule>(Constants.MAX_OBJECTS_PER_ADD);
			priceBreaks = new HashMap<UUID,PriceBreak>();
			indexPriceBreaks = new ArrayList<PriceBreak>();
		}

		public Context resetAll() {
			supplierUsers.clear();
			suppliers.clear();
			indexPriceBreaks.clear();
			supplierSearchBeans.clear();
			supplierUpdateExecutes.clear();
			schedules.clear();
			priceBreaks.clear();
			return this;
		}
		public Context resetPriceBreak() {
			priceBreaks.clear();
			return this;
		}
	}
	@ConstructorProperties({ "fieldDeclaration" })
	public SupplierStoreAggregator(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}
	
	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<Context> operationCall) {
		Context context = operationCall.getContext();
		if (context != null){
			context.resetAll();
		}else{
			operationCall.setContext(new Context());
		}
	}
	@Override
	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		context.resetPriceBreak();
	}

	@Override
	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		TupleEntry arguments = aggregatorCall.getArguments();

		String UUIDStr = arguments.getString(SupplierColumns.BreakUUID);
		if(StringUtils.isNotEmpty(UUIDStr)){
			UUID breakUUID = UUID.fromString(arguments.getString(SupplierColumns.BreakUUID));
			String breakID = arguments.getString(SupplierColumns.BreakID);
			String supplierID = arguments.getString(SupplierColumns.SupplierID);
			String disabled = arguments.getString(SupplierColumns.Disabled);
			String breakName = arguments.getString(SupplierColumns.BreakName);

			PriceBreak priceBreak = new PriceBreak().init();
			priceBreak.setCreateDate(new Date());
			priceBreak.setDisabledDate(DateUtils.addYears(new Date(), 1));
			priceBreak.setFullFeedDate(DateUtils.addYears(new Date(), 1));
			priceBreak.setIdataId(Integer.parseInt(breakID));
			priceBreak.setLastCacheDate(DateUtils.addYears(new Date(), 21));
			priceBreak.setCountry(Constants.CountryMap.get(supplierID.substring(0, supplierID.length() - 4)));
			priceBreak.setName(breakName);
			if ("0".equals(disabled)) {
				priceBreak.setStatus(PriceBreak.Status.Active);
			} else {
				priceBreak.setStatus(PriceBreak.Status.Disabled);
			}
			priceBreak.setSupplierId(Integer.parseInt(supplierID));
			priceBreak.setPriceBreakId(breakUUID);
			context.priceBreaks.put(priceBreak.getPriceBreakId(), priceBreak);
			context.indexPriceBreaks.add(priceBreak);
		}
	}
	@Override
	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		TupleEntry group = aggregatorCall.getGroup();
		String supplierID = group.getString(SupplierColumns.SupplierID);
		String supplierName = group.getString(SupplierColumns.SupplierName);
		String shortName = group.getString(SupplierColumns.ShortName);
		String activeStates = group.getString(SupplierColumns.ActiveStates);
		String webAddress = group.getString(SupplierColumns.WEBAddress);
		String approvalTo = group.getString(SupplierColumns.ApprovalTo);
		String approvalEmailAddress = group.getString(SupplierColumns.ApprovalEmailAddress);
		UUID supplierUserID = UUID.fromString(group.getString(SupplierColumns.SupplierUserID));
		String branchID = group.getString(SupplierColumns.SupplierBranchID);
		String scheduleUUID = group.getString(SupplierColumns.ScheduleUUID);
		Integer resellerCount = group.getInteger(SupplierColumns.ResellerCount);
		Address address = null;
		UUID supplierBranchID = null;
		if (StringUtils.isNotEmpty(branchID)) {
			supplierBranchID = UUID.fromString(branchID);
			String address1 = group.getString(SupplierColumns.Address1);
			String address2 = group.getString(SupplierColumns.Address2);
			String address3 = group.getString(SupplierColumns.Address3);
			String city = group.getString(SupplierColumns.City);
			String region = group.getString(SupplierColumns.State);
			String postcode = group.getString(SupplierColumns.Zip);
			String supplierBranchCountryCode = group.getString(SupplierColumns.SupplierBranchCountryCode);
			supplierBranchCountryCode = Constants.CountryMap.get(supplierBranchCountryCode);
			address = new GeneralAddress(address1, address2, address3, city, region, supplierBranchCountryCode, postcode);
		}
		
		SupplierUser supplierUser = makeSupplierUser(supplierID, supplierName, approvalTo, approvalEmailAddress, supplierUserID);
		context.supplierUsers.add(supplierUser);
		
		Schedule schedule = makeSchedule(scheduleUUID);
		context.schedules.add(schedule);
		
		Supplier supplier = makeSupplier(supplierID, supplierName, shortName, activeStates, webAddress,supplierUser.getUserId(), supplierBranchID,schedule);
		supplier.setDataRequest(new DataRequest());
		supplier.setBillingInfo(makeBillingInfo(supplierID, approvalTo, approvalEmailAddress));
		Map<UUID, PriceBreak> map = new HashMap<UUID, PriceBreak>();
		map.putAll(context.priceBreaks);
		supplier.setPriceBreaks(map);
		context.suppliers.add(supplier);
		context.supplierSearchBeans.add(new SupplierSearchBean(supplier, address,resellerCount));
		
		SupplierUpdateExecute supplierUpdateExecute = makeSupplierUpdateExecute(supplier,schedule);
		context.supplierUpdateExecutes.add(supplierUpdateExecute);
		flushInputObjects(context,false);
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<Context> operationCall) {
		Context context = operationCall.getContext();
		flushInputObjects(context,true);
	}
	private void flushInputObjects(Context context,boolean force) {
		if ((force && (context.suppliers.size() > 0)) || (context.suppliers.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(context.suppliers.toArray(new Supplier[context.suppliers.size()]));
			StoreHelper.save(context.supplierUsers.toArray(new SupplierUser[context.supplierUsers.size()]));
			StoreHelper.save(context.schedules.toArray(new Schedule[context.schedules.size()]));
			StoreHelper.indexValues(context.supplierSearchBeans.toArray(new SupplierSearchBean[context.supplierSearchBeans.size()]));
			StoreHelper.indexValues(context.supplierUpdateExecutes.toArray(new SupplierUpdateExecute[context.supplierUpdateExecutes.size()]));
			StoreHelper.indexValues(context.indexPriceBreaks.toArray(new PriceBreak[context.indexPriceBreaks.size()]));
			context.resetAll();
		}
	}
	
	private Schedule makeSchedule(String ScheduleUUID) {
		Schedule schedule = new Schedule();
		schedule.setScheduleId(UUID.fromString(ScheduleUUID));
		schedule.setEnabled(false);
		return schedule;
	}

	private Supplier makeSupplier(String supplierID, String supplierName, String shortName, String activeStates, String webAddress, UUID supplierUserID, UUID supplierBranchID,Schedule schedule) {
		Supplier supplier = new Supplier();
		supplier.setCountry(Constants.CountryMap.get(supplierID.substring(0, supplierID.length() - 4)));
		supplier.setCreateDate(new Date());
		supplier.setWebAddress(webAddress);
		supplier.setSupplierId(Integer.parseInt(supplierID));
		if (StringUtils.isNotEmpty(activeStates)) {
			supplier.setStatus(Supplier.Status.Active);
		} else {
			supplier.setStatus(Supplier.Status.Disabled);
		}
		supplier.setShortName(shortName);
		supplier.setName(supplierName);
		supplier.setUserId(supplierUserID);
		supplier.setDefaultBranchId(supplierBranchID);
		supplier.setLastImportDate(new Date());
		SupplierUpdate updateInfo = new SupplierUpdate();
		updateInfo.setScheduleId(schedule.getScheduleId());
		supplier.setUpdateInfo(updateInfo);
		return supplier;
	}

	private SupplierUser makeSupplierUser(String supplierID, String supplierName, String approvalTo, String approvalEmailAddress, UUID supplierUserID) {
		SupplierUser user = new SupplierUser();
		if(StringUtils.isNotEmpty(approvalEmailAddress)){
			user.setUsername(approvalEmailAddress);
		}else{
			user.setUsername(supplierUserID.toString().substring(0, 9)+supplierID);
		}
		
		user.setPassword(SecurityUtils.digestPassword(user.getUsername(),supplierUserID.toString()));
		if (StringUtils.isNotEmpty(approvalTo)) {
			String[] names = approvalTo.split(" ", 2);
			if (names.length == 2) {
				user.setFirstName(names[0]);
				user.setLastName(names[1]);
			} else {
				user.setFirstName(names[0]);
				user.setLastName(null);
			}
		}
		user.setEmail(approvalEmailAddress);
		user.setStatus(UserStatus.ACTIVE);
		user.setLoginFailureCount(0);
		user.setUserId(supplierUserID);
		user.setSupplierId(Integer.parseInt(supplierID));
		user.setSupplierName(supplierName);
		return user;
	}

	private SupplierBillingInfo makeBillingInfo(String supplierID, String approvalTo, String approvalEmailAddress) {
		SupplierBillingInfo billingInfo = new SupplierBillingInfo().init();
		billingInfo.setEmail(approvalEmailAddress);
		if (StringUtils.isNotEmpty(approvalTo)) {
			String[] names = approvalTo.split(" ", 2);
			if (names.length == 2) {
				billingInfo.setFirstName(names[0]);
				billingInfo.setLastName(names[1]);
			} else {
				billingInfo.setFirstName(names[0]);
				billingInfo.setLastName(null);
			}
		}
		return billingInfo;
	}

	private SupplierUpdateExecute makeSupplierUpdateExecute(Supplier supplier,Schedule schedule) {
		SupplierUpdateExecute supplierUpdateExecute = new SupplierUpdateExecute();
		supplierUpdateExecute.setScheduleId(schedule.getScheduleId());
		supplierUpdateExecute.setEnable(schedule.isEnabled());
		supplierUpdateExecute.setSupplierId(supplier.getSupplierId());
		supplierUpdateExecute.setSupplierName(supplier.getName());
		return supplierUpdateExecute;
	}
}
