package au.com.isell.epd.aggregator;


import java.util.ArrayList;
import java.util.List;

import au.com.isell.common.util.StringUtils;
import au.com.isell.epd.enums.Columns;
import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class MergeUnitsAggregator extends BaseOperation<MergeUnitsAggregator.Context> implements Aggregator<MergeUnitsAggregator.Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1572839021429120586L;
	/** Field FIELD_NAME */
	public static final Fields FIELDS = new Fields(Columns.UnitTypeId,Columns.Units);
	private String delimiter="#";
	
	public static class Context {
		boolean flag = true;
		String unitTypeId = null;
		List<String> units = null;
		
		public Context() {
			super();
			this.flag = true;
			this.unitTypeId = null;
			this.units = new ArrayList<String>();
		}

		public Context reset() {
			this.flag = true;
			this.unitTypeId = null;
			this.units.clear();
			return this;
		}
	}

	public MergeUnitsAggregator() {
		super(FIELDS);
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
		String unit = arguments.getString(Columns.Unit);
		if(StringUtils.isNotEmpty(unit) && !context.units.contains(unit)){
			context.units.add(unit);
		}
		if(context.flag){
			String tempUnitTypeId = arguments.getString(Columns.UnitTypeId);
			if(StringUtils.isEmpty(context.unitTypeId) && StringUtils.isNotEmpty(tempUnitTypeId)){
				context.unitTypeId = arguments.getString(Columns.UnitTypeId);
			}else if(StringUtils.isNotEmpty(context.unitTypeId) && StringUtils.isNotEmpty(tempUnitTypeId) && !context.unitTypeId.equals(tempUnitTypeId)){
				context.unitTypeId = null;
				context.flag = false;
			}
		}
	}

	public void complete(FlowProcess flowProcess, AggregatorCall<Context> aggregatorCall) {
		Context context = aggregatorCall.getContext();
		Tuple output = new Tuple();
		output.add(context.unitTypeId);
		String unitsString = "";
		for(int i=0;context.units!=null && i<context.units.size();i++){
			if(i==0){
				unitsString += delimiter;
			}
			unitsString += context.units.get(i)+delimiter;
		}
		output.add(StringUtils.isEmpty(unitsString)?null:unitsString);
		aggregatorCall.getOutputCollector().add(output);
	}
}