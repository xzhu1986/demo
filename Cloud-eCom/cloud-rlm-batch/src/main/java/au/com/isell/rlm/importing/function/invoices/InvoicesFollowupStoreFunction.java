package au.com.isell.rlm.importing.function.invoices;

import java.beans.ConstructorProperties;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.isell.common.util.StringUtils;
import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.invoices.InvoicesColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.importing.utils.DateUtils;
import au.com.isell.rlm.importing.utils.SpringUtils;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups.FollowupResult;
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

public class InvoicesFollowupStoreFunction extends BaseOperation<InvoicesFollowupStoreFunction.Context> implements Function<InvoicesFollowupStoreFunction.Context> {
	
	private static final long serialVersionUID = 1089526440864934362L;
	
	protected static class Context {
		Map<String, UUID> userMap;
		List<InvoiceFollowups> invoiceFollowups;
		
		public Context(Map<String, UUID> userM) {
			invoiceFollowups = new ArrayList<InvoiceFollowups>(Constants.MAX_OBJECTS_PER_ADD);
			userMap = Collections.unmodifiableMap(userM);
		}
		public Context reset() {
			invoiceFollowups.clear();
			return this;
		}
	}
	
	@ConstructorProperties({ "fieldDeclaration" })
	public InvoicesFollowupStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<InvoicesFollowupStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		if (context != null){
			context.reset();
		}else{
			Map<String, UUID> userM = new HashMap<String, UUID>();
			UserService userService = SpringUtils.getBean("userServiceImpl", UserServiceImpl.class);
			List<User> users = userService.getAgentUserList();
			if (users != null) {
				for (User user : users) {
					userM.put(user.getUsername().toLowerCase(), user.getUserId());
				}
			}
			operationCall.setContext(new Context(userM));
		}
		
	}
	
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<InvoicesFollowupStoreFunction.Context> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);
		
		String followup = arguments.getString(InvoicesColumns.Followup);
		if(StringUtils.isNotEmpty(followup)){
			int invoiceNumber = arguments.getInteger(InvoicesColumns.InvoiceNumber);
			String userID = arguments.getString(InvoicesColumns.UserID);
			int results = arguments.getInteger(InvoicesColumns.Results);
			String addtionalNotes = arguments.getString(InvoicesColumns.AddtionalNotes);
			String emailSubject = arguments.getString(InvoicesColumns.EmailSubject);
			String emailBody = arguments.getString(InvoicesColumns.EmailBody);
			
			Context context = functionCall.getContext();
			
			InvoiceFollowups invoiceFollowups = makeInvoiceFollowup(invoiceNumber,followup,addtionalNotes,emailSubject,emailBody,results,context.userMap.get(userID==null?null:userID.toLowerCase()));
			context.invoiceFollowups.add(invoiceFollowups);
			flushInputObjects(context,false);;
		}
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<InvoicesFollowupStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		flushInputObjects(context,true);
	}
	

	private InvoiceFollowups makeInvoiceFollowup(int invoiceNumber,String followup,String addtionalNotes,String emailSubject,String emailBody,int results,UUID userId) {
		InvoiceFollowups invoiceFollowup = new InvoiceFollowups();
		invoiceFollowup.setInvoiceNumber(invoiceNumber);
		invoiceFollowup.setAdditionalNotes(addtionalNotes);
		invoiceFollowup.setEmailBody(emailBody);
		invoiceFollowup.setEmailSubject(emailSubject);
		
		invoiceFollowup.setResults(FollowupResult.values()[results-1]);
		invoiceFollowup.setUserId(userId);
		try {
			invoiceFollowup.setFollowupDate(DateUtils.parseDate(DateUtils.DF_SHORT,followup));
		} catch (ParseException e) {
		}
		return invoiceFollowup;
	}

	private void flushInputObjects(Context context, boolean force) {
		if ((force && (context.invoiceFollowups.size() > 0)) || (context.invoiceFollowups.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(context.invoiceFollowups.toArray(new InvoiceFollowups[context.invoiceFollowups.size()]));

			context.reset();
		}
	}
}
