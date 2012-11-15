package au.com.isell.rlm.importing.function.common;

import java.beans.ConstructorProperties;

import au.com.isell.common.util.StringUtils;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class SetEmptyStringToNullFunction extends BaseOperation implements Function {
	@ConstructorProperties({ "fieldDeclaration" })
	public SetEmptyStringToNullFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		Tuple output = new Tuple();
		for (int i = 0; i < arguments.getFields().size(); i++) {
			output.add(StringUtils.isEmpty(arguments.getString(i))? null : arguments.get(i));
		}
		functionCall.getOutputCollector().add(output);
	}
}
