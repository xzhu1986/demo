package au.com.isell.epd.aggregator;

import java.beans.ConstructorProperties;

import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class MergeAggregator extends BaseOperation<MergeAggregator.Context> implements Aggregator<MergeAggregator.Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1572839021429120586L;
	/** Field FIELD_NAME */
	public static final String FIELD_NAME = "merge";
	private boolean isReplaceNull = true;

	public static class Context {
		String[] values = null;

		public Context(int length) {
			super();
			this.values = new String[length];
		}

		public Context reset(int length) {
			this.values = new String[length];
			return this;
		}
	}

	/**
	 * Constructor Count creates a new Count instance using the defalt field
	 * declaration of name 'count'.
	 */
	public MergeAggregator() {
		super(new Fields(FIELD_NAME));
	}

	/**
	 * Constructor Count creates a new Count instance and returns a field with
	 * the given fieldDeclaration name.
	 * 
	 * @param fieldDeclaration
	 *            of type Fields
	 */
	@ConstructorProperties({ "fieldDeclaration" })
	public MergeAggregator(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}

	/**
	 * Constructor Count creates a new Count instance and returns a field with
	 * the given fieldDeclaration name.
	 * 
	 * @param fieldDeclaration
	 *            of type Fields
	 */
	@ConstructorProperties({ "fieldDeclaration","isReplaceNull" })
	public MergeAggregator(Fields fieldDeclaration,boolean isReplaceNull) {
		super(fieldDeclaration);
		this.isReplaceNull = isReplaceNull;
	}
	
	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		int size = aggregatorCall.getArgumentFields().size();
		if (aggregatorCall.getContext() != null)
			aggregatorCall.getContext().reset(size);
		else
			aggregatorCall.setContext(new Context(size));
	}

	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		// get the current argument values
		TupleEntry arguments = aggregatorCall.getArguments();
		// get the context for this grouping
		Context context = aggregatorCall.getContext();
		for (int i = 0; i < arguments.getFields().size(); i++) {
			if(!isReplaceNull){
				context.values[i] = arguments.getString(i);
			}else{
				if (arguments.getString(i) != null && arguments.getString(i).trim().length() > 0) {
					context.values[i] = arguments.getString(i);
				}
			}
		}
		// update the context object
	}

	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		Tuple output = new Tuple();
		for (int i = 0; i < context.values.length; i++)
			output.add(context.values[i]);
		// create a Tuple to hold our result values
		// insert some values into the result Tuple based on the context
		// return the result Tuple
		aggregatorCall.getOutputCollector().add(output);
	}
}