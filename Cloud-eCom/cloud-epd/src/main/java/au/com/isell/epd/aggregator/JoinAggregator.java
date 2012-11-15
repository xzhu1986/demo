package au.com.isell.epd.aggregator;

import java.beans.ConstructorProperties;

import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class JoinAggregator extends BaseOperation<JoinAggregator.Context> implements Aggregator<JoinAggregator.Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6082831261197645061L;
	/** Field FIELD_NAME */
	public static final String FIELD_NAME = "join";
	private String delimiter="\t";
	public static class Context {
		String value = "";
	}
	public JoinAggregator() {
		super(new Fields(FIELD_NAME));
	}

	public JoinAggregator(String delimiter) {
		super(new Fields(FIELD_NAME));
		this.delimiter = delimiter;
	}
	
	@ConstructorProperties({ "fieldDeclaration" })
	public JoinAggregator(Fields fieldDeclaration) {
		super(1, fieldDeclaration);

		if (!fieldDeclaration.isSubstitution() && fieldDeclaration.size() != 1)
			throw new IllegalArgumentException("fieldDeclaration may only declare 1 field, got: " + fieldDeclaration.size());
	}

	@ConstructorProperties({ "fieldDeclaration" })
	public JoinAggregator(Fields fieldDeclaration,String delimiter) {
		super(1, fieldDeclaration);
		this.delimiter = delimiter;
		if (!fieldDeclaration.isSubstitution() && fieldDeclaration.size() != 1)
			throw new IllegalArgumentException("fieldDeclaration may only declare 1 field, got: " + fieldDeclaration.size());
	}
	
	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		if (aggregatorCall.getContext() != null)
			aggregatorCall.getContext().value = "";
		else
			aggregatorCall.setContext(new Context());
	}

	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		// get the current argument values
		TupleEntry arguments = aggregatorCall.getArguments();
		// get the context for this grouping
		Context context = aggregatorCall.getContext();
		for (int i = 0; i < arguments.getFields().size(); i++) {
			context.value += arguments.getString(i)+delimiter;
		}
	}

	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		// create a Tuple to hold our result values
		// insert some values into the result Tuple based on the context
		// return the result Tuple
		aggregatorCall.getOutputCollector().add(new Tuple(context.value));
	}
}