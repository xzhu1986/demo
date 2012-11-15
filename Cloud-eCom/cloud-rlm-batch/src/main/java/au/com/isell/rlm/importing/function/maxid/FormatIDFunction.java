package au.com.isell.rlm.importing.function.maxid;

import java.beans.ConstructorProperties;

import au.com.isell.rlm.importing.constant.Constants;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class FormatIDFunction extends BaseOperation implements Function {
	/** Field filter */
	private String type = "";

	@ConstructorProperties({ "fieldDeclaration", "type" })
	public FormatIDFunction(Fields fieldDeclaration, String type) {
		super(fieldDeclaration);
		this.type = type;
	}

	@Override
	public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		Tuple output = new Tuple();
		String id=arguments.getString(0);
		if("Supplier".equals(type)){
			output.add(Integer.parseInt(id.substring(id.length()-4)));
			output.add(Constants.CountryMap.get(id.substring(0, id.length()-4)));
		}else if("Reseller".equals(type)){
			output.add(Integer.parseInt(id.substring(id.length()-5)));
			output.add(Constants.CountryMap.get(arguments.getString(1)));
		}
		functionCall.getOutputCollector().add(output);
	}
}
