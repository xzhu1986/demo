package au.com.isell.epd.aggregator;

import java.beans.ConstructorProperties;

import au.com.isell.epd.enums.Columns;
import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class DeleteDuplicatedUnitAggregator extends BaseOperation<DeleteDuplicatedUnitAggregator.Context> implements Aggregator<DeleteDuplicatedUnitAggregator.Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1572839021429120586L;
	/** Field FIELD_NAME */
	public static final String FIELD_NAME = "merge";

	public static class Context {
		int count = 0;
		String[] values = null;

		public Context(int length) {
			super();
			this.values = new String[length];
			this.count = 0;
		}

		public Context reset(int length) {
			this.values = new String[length];
			this.count = 0;
			return this;
		}
	}

	/**
	 * Constructor Count creates a new Count instance using the defalt field
	 * declaration of name 'count'.
	 */
	public DeleteDuplicatedUnitAggregator() {
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
	public DeleteDuplicatedUnitAggregator(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}

	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		int size = aggregatorCall.getArgumentFields().size();
		if (aggregatorCall.getContext() != null)
			aggregatorCall.getContext().reset(size);
		else
			aggregatorCall.setContext(new Context(size));
	}

	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		context.count += 1L;
		TupleEntry arguments = aggregatorCall.getArguments();
		if (context.count > 1) {
			Fields fields = arguments.getFields();
			for (int i = 0; i < arguments.getFields().size(); i++) {
				if(fields.get(i).toString().equals(Columns.UnitTypeId)){
					context.values[i] = null;
				}else{
					context.values[i] = arguments.getString(i);
				}
				
			}
		}else{
			for (int i = 0; i < arguments.getFields().size(); i++) {
				context.values[i] = arguments.getString(i);
			}
		}
		
	}

	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		Tuple output = new Tuple();
		for (int i = 0; i < context.values.length; i++)
			output.add(context.values[i]);
		aggregatorCall.getOutputCollector().add(output);
	}
}