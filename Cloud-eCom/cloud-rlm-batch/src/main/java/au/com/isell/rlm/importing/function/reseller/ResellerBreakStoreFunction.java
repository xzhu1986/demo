package au.com.isell.rlm.importing.function.reseller;

import java.beans.ConstructorProperties;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.isell.common.util.StringUtils;
import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.constant.supplier.SupplierColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.importing.utils.DateUtils;
import au.com.isell.rlm.importing.utils.SpringUtils;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.agent.service.impl.AgentServiceImpl;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMapHistory;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class ResellerBreakStoreFunction extends BaseOperation<ResellerBreakStoreFunction.Context> implements Function<ResellerBreakStoreFunction.Context> {
	private static final long serialVersionUID = 1089526440864934362L;
	
	protected static class Context {
		Map<String, UUID> agentMap;
		
		List<ResellerSupplierMap> resellerSupplierMapList;
		List<ResellerSupplierMapHistory> resellerSupplierMapHistoryList;
		
		public Context(Map<String, UUID> agentM) {
			resellerSupplierMapList = new ArrayList<ResellerSupplierMap>(Constants.MAX_OBJECTS_PER_ADD);
			resellerSupplierMapHistoryList = new ArrayList<ResellerSupplierMapHistory>(Constants.MAX_OBJECTS_PER_ADD);
			agentMap = Collections.unmodifiableMap(agentM);
		}
		public Context reset() {
			resellerSupplierMapList.clear();
			resellerSupplierMapHistoryList.clear();
			return this;
		}
	}
	
	@ConstructorProperties({ "fieldDeclaration" })
	public ResellerBreakStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}


	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<ResellerBreakStoreFunction.Context> operationCall) {
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
			operationCall.setContext(new Context(agentM));
		}
		
	}
	
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<ResellerBreakStoreFunction.Context> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);
		Context context = functionCall.getContext();
		
		String supplierID = arguments.getString(SupplierColumns.SupplierID);
		String supplierName = arguments.getString(SupplierColumns.SupplierName);
		String breakName = arguments.getString(SupplierColumns.BreakName);
		UUID breakUUID = UUID.fromString(arguments.getString(SupplierColumns.BreakUUID));
		int serialNo = arguments.getInteger(ResellerColumns.SerialNumber);
		String companyName = arguments.getString(ResellerColumns.CompanyName);
		int transmissionType = arguments.getInteger(ResellerColumns.TransmissionType);
		int currentCustomer = arguments.getInteger(ResellerColumns.CurrentCustomer);
		String accountNo = arguments.getString(ResellerColumns.AccountNo);
		String dateGranted = arguments.getString(ResellerColumns.DateGranted);
		String countryCode = arguments.getString(ResellerColumns.CountryCode);
		String billingPhone = arguments.getString(ResellerColumns.BillingPhone);
		String billingEmailAddress = arguments.getString(ResellerColumns.BillingEmailAddress);
		String agency = arguments.getString(ResellerColumns.Agency);
		String approvalEmailAddress = arguments.getString(SupplierColumns.ApprovalEmailAddress);
		
		ResellerSupplierMap rsMap = new ResellerSupplierMap().init();
		if("uk".equalsIgnoreCase(countryCode)){
			rsMap.setResCountry("gb");
		}else{
			rsMap.setResCountry(StringUtils.isNotEmpty(countryCode)?countryCode.toLowerCase():null);
		}
		rsMap.setResEmail(billingEmailAddress);
		rsMap.setResPhone(billingPhone);
		rsMap.setResAgent(context.agentMap.get(agency==null?null:agency.toLowerCase()));
		rsMap.setCompany(companyName);
		
		rsMap.setBreakName(breakName);
		rsMap.setSupplierId(Integer.parseInt(supplierID));
		rsMap.setSupplierName(supplierName);
		rsMap.setSupplierCountry(Constants.CountryMap.get(supplierID.substring(0, supplierID.length()-4)));
		rsMap.setSupEmail(approvalEmailAddress);
//		rsMap.setSupPhone(supPhone);
		
		rsMap.setPriceBreakId(breakUUID);
		rsMap.setSerialNo(serialNo);
		if(currentCustomer==1){
			rsMap.setExistingCustomer(true);
		}else{
			rsMap.setExistingCustomer(false);
		}
		try {
			rsMap.setApprovalDate(DateUtils.parseDate(DateUtils.DF_SHORT,dateGranted));
		} catch (ParseException e) {
		}
		
		if(transmissionType==4){
			rsMap.setStatus(ResellerSupplierMap.ApprovalStatus.Approved);
		}else{
			if(rsMap.getSupplierId()==610004 && StringUtils.isNotEmpty(dateGranted)){
				rsMap.setStatus(ResellerSupplierMap.ApprovalStatus.Approved);
			}else{
				rsMap.setStatus(ResellerSupplierMap.ApprovalStatus.Pending);
			}
			rsMap.setStatus(ResellerSupplierMap.ApprovalStatus.Disabled);
		}
		rsMap.setSupplierAccountNumber(accountNo);
		
		context.resellerSupplierMapList.add(rsMap);
		
		if(ApprovalStatus.Approved.equals(rsMap.getStatus())){
			ResellerSupplierMapHistory history = new ResellerSupplierMapHistory();
			history.setDate(new Date(0));//import one  history
			history.setOperate("Approval");
			history.setSupplierId(rsMap.getSupplierId());
			history.setSerialNo(rsMap.getSerialNo());
			history.setVia("Fax sent to +61 2 9475 4065");
			history.setFormImport(true);
			context.resellerSupplierMapHistoryList.add(history);
		}
		
		flushInputObjects(context, false);
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<ResellerBreakStoreFunction.Context> operationCall) {
		flushInputObjects(operationCall.getContext(), true);
	}

	private void flushInputObjects(Context context, boolean force) {
		if ((force && (context.resellerSupplierMapList.size() > 0)) || (context.resellerSupplierMapList.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(context.resellerSupplierMapList.toArray(new ResellerSupplierMap[context.resellerSupplierMapList.size()]));
			StoreHelper.save(context.resellerSupplierMapHistoryList.toArray(new ResellerSupplierMapHistory[context.resellerSupplierMapHistoryList.size()]));
			
			context.reset();
		}
	}
}
