package au.com.isell.rlm.importing.function.invoices;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import au.com.isell.common.util.StringUtils;
import au.com.isell.rlm.common.constant.PaymentMethod;
import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.invoices.InvoicesColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.importing.utils.DateUtils;
import au.com.isell.rlm.module.invoice.domain.InvoiceSchedule;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class InvoicesSchedStoreFunction extends BaseOperation<InvoicesSchedStoreFunction.Context> implements Function<InvoicesSchedStoreFunction.Context> {
	
	private static final long serialVersionUID = 1089526440864934362L;
	
	protected static class Context {
		List<InvoiceSchedule> invoiceSchedules;
		
		public Context() {
			invoiceSchedules = new ArrayList<InvoiceSchedule>(Constants.MAX_OBJECTS_PER_ADD);
		}
		public Context reset() {
			invoiceSchedules.clear();
			return this;
		}
	}
	
	@ConstructorProperties({ "fieldDeclaration" })
	public InvoicesSchedStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<InvoicesSchedStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		if (context != null){
			context.reset();
		}else{
			operationCall.setContext(new Context());
		}
		
	}
	
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<InvoicesSchedStoreFunction.Context> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);
		
		String dueDate = arguments.getString(InvoicesColumns.DueDate);
		if(StringUtils.isNotEmpty(dueDate)){
			int invoiceNumber = arguments.getInteger(InvoicesColumns.InvoiceNumber);
			double scheduledAmount = arguments.getDouble(InvoicesColumns.ScheduledAmount);
			String paymentReference = arguments.getString(InvoicesColumns.PaymentReference);
			String paymentReceived = arguments.getString(InvoicesColumns.PaymentReceived);
			String paymentReceivedDate = arguments.getString(InvoicesColumns.PaymentReceivedDate);
			int paymentType = arguments.getInteger(InvoicesColumns.PaymentType);
			
			Context context = functionCall.getContext();
			
			InvoiceSchedule invoiceSchedule = makeInvoiceSchedule(invoiceNumber,dueDate,paymentReceivedDate,paymentReference,paymentType,scheduledAmount);
			context.invoiceSchedules.add(invoiceSchedule);
			flushInputObjects(context,false);;
		}
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<InvoicesSchedStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		flushInputObjects(context,true);
	}
	

	private InvoiceSchedule makeInvoiceSchedule(int invoiceNumber,String dueDate,String paymentReceivedDate,String paymentReference,int paymentType,double scheduledAmount) {
		InvoiceSchedule invoiceSchedule = new InvoiceSchedule();
		invoiceSchedule.setInvoiceNumber(invoiceNumber);
		invoiceSchedule.setPaymentReference(paymentReference);
		invoiceSchedule.setPaymentType(PaymentMethod.values()[paymentType-1]);
		invoiceSchedule.setScheduledAmount(BigDecimal.valueOf(scheduledAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
		
		try {
			invoiceSchedule.setPaidDatetime(DateUtils.parseDate(DateUtils.DF_SHORT,paymentReceivedDate));
			invoiceSchedule.setDueDate(DateUtils.parseDate(DateUtils.DF_SHORT,dueDate));
		} catch (ParseException e) {
		}
		return invoiceSchedule;
	}

	private void flushInputObjects(Context context, boolean force) {
		if ((force && (context.invoiceSchedules.size() > 0)) || (context.invoiceSchedules.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(context.invoiceSchedules.toArray(new InvoiceSchedule[context.invoiceSchedules.size()]));

			context.reset();
		}
	}
}
