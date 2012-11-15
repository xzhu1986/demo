package au.com.isell.epd.function;

import java.beans.ConstructorProperties;
import java.util.UUID;

import au.com.isell.epd.enums.Columns;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class SetUUIDFunction extends BaseOperation implements Function {

	public SetUUIDFunction() {
		super(new Fields(Columns.ProductUUID));
	}

	@ConstructorProperties({ "fieldDeclaration"})
	public SetUUIDFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}

	public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		Tuple output = new Tuple();
		for (int i = 0; i < arguments.getFields().size(); i++) {
			output.add(arguments.getObject(i) == null ? UUID.randomUUID().toString() : arguments.getString(i));
		}

		functionCall.getOutputCollector().add(output);
	}
}
