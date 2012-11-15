package au.com.isell.rlm.importing.aggregator.common;

import java.beans.ConstructorProperties;

import au.com.isell.rlm.importing.constant.Columns;
import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class CheckChangedAggregator extends BaseOperation<CheckChangedAggregator.Context> implements Aggregator<CheckChangedAggregator.Context> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1293462979177188230L;

	public static class Context {
		String[] oldValues = null;
		String[] newValues = null;

		public Context() {
		}

		public Context reset() {
			this.oldValues = null;
			this.newValues = null;
			return this;
		}
	}

	/**
	 * Constructor Count creates a new Count instance and returns a field with
	 * the given fieldDeclaration name.
	 * 
	 * @param fieldDeclaration
	 *            of type Fields
	 */
	@ConstructorProperties({ "fieldDeclaration" })
	public CheckChangedAggregator(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}

	@Override
	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		if (aggregatorCall.getContext() != null)
			aggregatorCall.getContext().reset();
		else
			aggregatorCall.setContext(new Context());
	}

	@Override
	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		// get the current argument values
		TupleEntry arguments = aggregatorCall.getArguments();
		// get the context for this grouping
		Context context = aggregatorCall.getContext();
		if (arguments.getInteger(Columns.SortNum) == 0) {
			context.oldValues = new String[arguments.getFields().size()-1];
			for (int i = 0; i < arguments.getFields().size()-1; i++)
				context.oldValues[i] = arguments.getString(i);

		} else if (arguments.getInteger(Columns.SortNum) == 1) {
			context.newValues = new String[arguments.getFields().size()-1];
			for (int i = 0; i < arguments.getFields().size()-1; i++)
				context.newValues[i] = arguments.getString(i);
		}

	}

	@Override
	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		
		Context context = aggregatorCall.getContext();
		if(context.newValues!=null && context.oldValues!=null){
			Tuple output = new Tuple();
			boolean isChange = false;
			for (int i=0;i<context.newValues.length;i++) {
				output.add(context.newValues[i]);
				if (context.newValues[i]!=null && !context.newValues[i].equals(context.oldValues[i])) {
					isChange = true;
				}else if(context.newValues[i]==null && context.oldValues[i]!=null){
					isChange = true;
				}
			}
			if (isChange) {
				aggregatorCall.getOutputCollector().add(output);
			}
		}else if(context.newValues!=null && context.oldValues==null){
			Tuple output = new Tuple();
			for (int i=0;i<context.newValues.length;i++) {
				output.add(context.newValues[i]);
			}
			aggregatorCall.getOutputCollector().add(output);
		}
		
	}
}