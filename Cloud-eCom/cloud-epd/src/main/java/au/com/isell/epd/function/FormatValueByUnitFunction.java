package au.com.isell.epd.function;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

import au.com.isell.common.util.StringUtils;
import au.com.isell.epd.enums.Columns;
import au.com.isell.epd.enums.Constants;
import au.com.isell.epd.utils.UnitUtils;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class FormatValueByUnitFunction extends BaseOperation implements Function {

	private int flag = 0;;

	@ConstructorProperties({ "fieldDeclaration","flag" })
	public FormatValueByUnitFunction(Fields fieldDeclaration,int flag) {
		super(fieldDeclaration);
		this.flag = flag;
	}
	@ConstructorProperties({ "fieldDeclaration"})
	public FormatValueByUnitFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}
	
	public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		Tuple output = new Tuple();
		if(StringUtils.validFloat(arguments.getString(Columns.Value)) && StringUtils.isNotEmpty(arguments.getString(Columns.Unit)) && StringUtils.isNotEmpty(arguments.getString(Columns.UnitTypeId))){
			BigDecimal value = BigDecimal.valueOf(arguments.getDouble(Columns.Value));
			BigDecimal preca = BigDecimal.valueOf(arguments.getDouble(Columns.Preca));
			BigDecimal cf = BigDecimal.valueOf(arguments.getDouble(Columns.Cf));
			int scale = arguments.getInteger(Columns.Scale);
			BigDecimal postca = BigDecimal.valueOf(arguments.getDouble(Columns.Postca));
			int prec = arguments.getInteger(Columns.Prec);
			if(flag==Constants.ConvertToBaseValueFlag){
				output.add(UnitUtils.convertToBaseValue(value,preca,cf,scale,postca).toString());
			}else if(flag==Constants.ConvertFromBaseUnitFlag){
				output.add(UnitUtils.convertFromBaseUnit(value,preca,cf,scale,postca,prec).toString());
			}
		}else{
			output.add(null);
		}
		functionCall.getOutputCollector().add(output);
	}
}
