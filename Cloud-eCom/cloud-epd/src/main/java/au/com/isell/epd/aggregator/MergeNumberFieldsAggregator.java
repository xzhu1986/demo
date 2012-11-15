package au.com.isell.epd.aggregator;

import java.beans.ConstructorProperties;

import au.com.isell.common.util.StringUtils;
import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class MergeNumberFieldsAggregator extends BaseOperation<MergeNumberFieldsAggregator.Context> implements Aggregator<MergeNumberFieldsAggregator.Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2998714251340998640L;
	/** Field FIELD_NAME */
	public static final String FIELD_NAME = "merge";
	private int[] operates = null;

	public static final int Min = 0;
	public static final int Max = 1;
	public static final int Sum = 2;
	public static final int Average = 3;
	public static final int Count = 4;

	public static class Context {
		int[] values = null;

		public Context(int length) {
			super();
			this.values = new int[length];
		}

		public Context reset(int length) {
			this.values = new int[length];
			return this;
		}
	}

	public MergeNumberFieldsAggregator() {
		super(new Fields(FIELD_NAME));
	}

	@ConstructorProperties({ "fieldDeclaration" })
	public MergeNumberFieldsAggregator(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}

	@ConstructorProperties({ "fieldDeclaration", "isReplaceNull" })
	public MergeNumberFieldsAggregator(Fields fieldDeclaration, int[] operates) {
		super(fieldDeclaration);
		this.operates = operates;
		if (fieldDeclaration.size() != operates.length)
			throw new IllegalArgumentException("fieldDeclaration and operates must be same size");
	}

	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		int size = aggregatorCall.getArgumentFields().size();
		if (aggregatorCall.getContext() != null)
			aggregatorCall.getContext().reset(size);
		else
			aggregatorCall.setContext(new Context(size));
	}

	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		TupleEntry entry = aggregatorCall.getArguments();
		Context context = aggregatorCall.getContext();
		for (int i = 0; i < entry.getFields().size(); i++) {
			String arg = entry.getString(i);
			Number rhs = null;
				
			if (StringUtils.validNumber(arg))
				rhs = Integer.parseInt(arg);
			else
				rhs = 0;

			if (operates == null) {// Min
				if (context.values[i] > rhs.intValue()) {
					context.values[i] = rhs.intValue();
				}
			} else if (operates[i] == Min) {
				if (context.values[i] > rhs.intValue()) {
					context.values[i] = rhs.intValue();
				}
			} else if (operates[i] == Max) {
				if (context.values[i] < rhs.intValue()) {
					context.values[i] = rhs.intValue();
				}
			} else if (operates[i] == Sum) {
				context.values[i] += rhs.intValue();
			} else if (operates[i] == Average) {

			} else if (operates[i] == Count) {
				context.values[i] += 1;
			}

		}
		// update the context object
	}

	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		Tuple output = new Tuple();
		for (int i = 0; i < context.values.length; i++)
			output.add(context.values[i]);
		aggregatorCall.getOutputCollector().add(output);
	}
}