package au.com.isell.epd.aggregator;

import java.util.ArrayList;
import java.util.List;

import au.com.isell.epd.enums.Columns;
import au.com.isell.epd.pojo.Option;
import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class CheckOptionsAggregator extends BaseOperation<CheckOptionsAggregator.Context> implements Aggregator<CheckOptionsAggregator.Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4199413417787968259L;
	/** Field FIELD_NAME */
	public static final String[] FIELD_NAMES = new String[] { Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp };

	public static class Context {
		List<Option> oldOptions = null;
		List<Option> newOptions = null;

		public Context() {
			super();
			this.oldOptions = new ArrayList<Option>();
			this.newOptions = new ArrayList<Option>();
		}

		public Context reset() {
			this.oldOptions = new ArrayList<Option>();
			this.newOptions = new ArrayList<Option>();
			return this;
		}
	}

	/**
	 * Constructor Count creates a new Count instance using the defalt field
	 * declaration of name 'count'.
	 */
	public CheckOptionsAggregator() {
		super(new Fields(FIELD_NAMES));
	}

	public void start(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		if (aggregatorCall.getContext() != null)
			aggregatorCall.getContext().reset();
		else
			aggregatorCall.setContext(new Context());
	}

	public void aggregate(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		TupleEntry arguments = aggregatorCall.getArguments();
		Context context = aggregatorCall.getContext();

		Option o = new Option();
		o.setManufacturer(arguments.getString(FIELD_NAMES[0]));
		o.setPartNo(arguments.getString(FIELD_NAMES[1]));
		o.setUuid(arguments.getString(FIELD_NAMES[2]));
		o.setO_manufacturer(arguments.getString(FIELD_NAMES[3]));
		o.setO_partNo(arguments.getString(FIELD_NAMES[4]));
		o.setO_uuid(arguments.getString(FIELD_NAMES[5]));
		
		if (arguments.getInteger(Columns.SortNum) == 0) {
			context.oldOptions.add(o);
		} else if (arguments.getInteger(Columns.SortNum) == 1) {
			context.newOptions.add(o);
		}
	}

	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		boolean isChange = false;
		Context context = aggregatorCall.getContext();
		for (Option newOption : context.newOptions) {
			if (!context.oldOptions.contains(newOption)) {
				isChange = true;
				break;
			}
		}
		if (isChange) {
			for (Option newOption : context.newOptions) {
				aggregatorCall.getOutputCollector().add(new Tuple(newOption.getManufacturer(), newOption.getPartNo(), newOption.getUuid(), newOption.getO_manufacturer(), newOption.getO_partNo(), newOption.getO_uuid()));
			}
		}
	}
}