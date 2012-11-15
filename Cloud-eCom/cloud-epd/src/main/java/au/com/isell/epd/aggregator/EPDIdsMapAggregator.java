package au.com.isell.epd.aggregator;

import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class EPDIdsMapAggregator extends BaseOperation<EPDIdsMapAggregator.Context> implements Aggregator<EPDIdsMapAggregator.Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8691016341064190031L;
	/** Field FIELD_NAME */
	public static final String[] FIELD_NAMES = new String[]{"Manufacturer","PartNo", "UUID", "CN_ProductID", "UK_ProductID"};

	public static class Context {
		String uuid = "";
		String productIds = "";
		String manufacturer = "";
		String partNo = "";

		public Context reset() {
			this.uuid = "";
			this.productIds = "";
			this.manufacturer = "";
			this.partNo = "";
			return this;
		}
	}

	/**
	 * Constructor Count creates a new Count instance using the defalt field
	 * declaration of name 'count'.
	 */
	public EPDIdsMapAggregator() {
		super(new Fields(FIELD_NAMES));
	}

	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		if (aggregatorCall.getContext() != null)
			aggregatorCall.getContext().reset();
		else
			aggregatorCall.setContext(new Context());
	}

	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		// get the current argument values
		TupleEntry arguments = aggregatorCall.getArguments();
		// get the context for this grouping
		Context context = aggregatorCall.getContext();
		context.productIds += arguments.getString("ProductID")+"\t";
		context.uuid = arguments.getString("UUID");
		context.manufacturer = arguments.getString("Manufacturer");
		context.partNo = arguments.getString("PartNo");
	}

	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		Tuple output = new Tuple();
		String[] priducts = context.productIds.split("\t",2);
		output.add(context.manufacturer);
		output.add(context.partNo);
		output.add(context.uuid);
		output.add(priducts[0]);
		output.add(priducts[1]);
		aggregatorCall.getOutputCollector().add(output);
	}
}