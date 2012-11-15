package au.com.isell.rlm.importing.function.common;

import java.beans.ConstructorProperties;
import java.io.Serializable;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class SetNullValueFunction extends BaseOperation implements Function {
	/** Field filter */
	private Serializable value = "";

	@ConstructorProperties({ "fieldDeclaration" })
	public SetNullValueFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}

	@ConstructorProperties({ "fieldDeclaration", "value" })
	public SetNullValueFunction(Fields fieldDeclaration, Serializable value) {
		super(fieldDeclaration);
		this.value = value;
	}

	@Override
	public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		Tuple output = new Tuple();
		for (int i = 0; i < arguments.getFields().size(); i++) {
			output.add(arguments.get(i) == null ? value : arguments.get(i));
		}
		functionCall.getOutputCollector().add(output);
	}
}
