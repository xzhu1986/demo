package au.com.isell.epd.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import au.com.isell.epd.enums.Columns;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class FormatProductsFunction extends BaseOperation implements Function {

	private static final Map<String, String> valueMap;
	private static final SimpleDateFormat SDF_FROM = new SimpleDateFormat("MMM d yyyy h:mma");
	private static final SimpleDateFormat SDF_TO = new SimpleDateFormat("yyyyMMddHHmmss");
	
	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("End", "1");
		map.put("Current", "0");
		valueMap = Collections.unmodifiableMap(map);
	}

	public FormatProductsFunction() {
		super(new Fields(Columns.ProductUUID,Columns.LastUpdated,Columns.Status));
	}

	public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		Tuple output = new Tuple();
		output.add(arguments.getObject(Columns.ProductUUID)==null ? UUID.randomUUID().toString() : arguments.getString(Columns.ProductUUID));
		try {
			output.add(SDF_TO.format(SDF_FROM.parse(arguments.getString(Columns.LastUpdated))));
		} catch (ParseException e) {
			output.add(null);
		}
		
		output.add(valueMap.get(arguments.getString(Columns.Status)));
		functionCall.getOutputCollector().add(output);
	}
}
