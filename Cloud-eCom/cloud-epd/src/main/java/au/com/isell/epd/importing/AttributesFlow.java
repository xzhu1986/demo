package au.com.isell.epd.importing;

import au.com.isell.epd.aggregator.CheckChangedAggregator;
import au.com.isell.epd.aggregator.DeleteDuplicatedUnitAggregator;
import au.com.isell.epd.aggregator.MergeAggregator;
import au.com.isell.epd.aggregator.MergeUnitsAggregator;
import au.com.isell.epd.comparator.IgnoreCaseComparator;
import au.com.isell.epd.enums.Columns;
import au.com.isell.epd.function.FormatValueByUnitFunction;
import au.com.isell.epd.function.SetUUIDFunction;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Debug;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.cogroup.InnerJoin;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class AttributesFlow extends BaseProductsFlow{
	
	public AttributesFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath);
	}

	public Flow createFlow() {
		sourceTaps.clear();
		sinkTaps.clear();
		Pipe tempProductsPipe = createFormatEmptyStringToNullPipe(createTempProductsPipe(commonOutputPath + "temp/product/"));
		Pipe sourceAttributesPipe = createFormatEmptyStringToNullPipe(createSourceAttributesPipe(commonInputPath + "Attributes.txt"));
		Pipe sourceUnitInfoPipe = createFormatEmptyStringToNullPipe(createSourceUnitInfoPipe(commonInputPath + "unit_info.txt"));
		Pipe tempAttributesPipe = createTempAttributesPipe(sourceAttributesPipe, tempProductsPipe,sourceUnitInfoPipe);
		
		Pipe sourceOldAttributeDefPipe = createFormatEmptyStringToNullPipe(createSourceOldAttributeDefPipe(commonOldDataPath + "attributedef/"));
		Pipe[] tempGroupAttributeDefPipse = createSinkTempGroupAttributeDefPipse(tempAttributesPipe, sourceOldAttributeDefPipe);
		Pipe sinkAttributeDefPipe = createSinkAttributeDefPipe(commonOutputPath + "all/attributedef/",tempGroupAttributeDefPipse);
		Pipe sinkChangedAttributeDefPipe = createSinkChangedAttributeDefPipe(commonOutputPath + "changed/attributedef/",tempGroupAttributeDefPipse);
		
		Pipe sourceOldAttributePipe = createFormatEmptyStringToNullPipe(createSourceOldAttributePipe(commonOldDataPath + "attribute/"));
		Pipe[] tempGroupAttributePipes = createSinkTempGroupAttributePipes(tempAttributesPipe, sinkAttributeDefPipe, sourceOldAttributePipe);
		Pipe sinkAttributePipe = createSinkAttributePipe(commonOutputPath + "all/attribute/", tempGroupAttributePipes);
		Pipe sinkChangedAttributePipe = createSinkChangedAttributePipe(commonOutputPath + "changed/attribute/", tempGroupAttributePipes);
		
		return flowConnector.connect(sourceTaps, sinkTaps, createFormatNullPipe(sinkAttributeDefPipe),
				createFormatNullPipe(sinkChangedAttributeDefPipe),
				createFormatNullPipe(sinkAttributePipe),
				createFormatNullPipe(sinkChangedAttributePipe));

	}

	private Pipe createSourceOldAttributeDefPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.AttributeDefId, Columns.UNSPSC, Columns.AttributeName, Columns.UnitTypeId, Columns.Units);
		Pipe pipe = new Each("SourceOldAttributeDefPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldAttributePipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.ProductUUID, Columns.AttributeDefId, Columns.Value, Columns.BaseValue, Columns.Unit);
		Pipe pipe = new Each("SourceOldAttributePipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createSourceAttributesPipe(String inputPath) {
		Tap tap = new Hfs(skipHeaderScheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.AttributeName, Columns.Value, Columns.Unit);
		Pipe pipe = new Each("SourceAttributesPipe", new RegexSplitter(fields, "\\|"));
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createSourceUnitInfoPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.Text,Columns.UnitName,Columns.UnitTypeId,Columns.BaseUnit,Columns.Preca,Columns.Cf,Columns.Scale,Columns.Postca,Columns.Prec);
		Pipe pipe = new Each("SourceUnitInfoPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createTempAttributesPipe(Pipe sourceAttributesPipe, Pipe tempProductsPipe,Pipe sourceUnitInfoPipe){
		Fields fields = new Fields(Columns.ProductID, Columns.AttributeName, Columns.Value, Columns.Unit);
		fields = fields.append(new Fields(Columns.Text,Columns.UnitName,Columns.UnitTypeId,Columns.BaseUnit,Columns.Preca,Columns.Cf,Columns.Scale,Columns.Postca,Columns.Prec));
		
		Fields unitField = new Fields(Columns.Unit);
		unitField.setComparators(new IgnoreCaseComparator());
		Fields textField = new Fields(Columns.Text);
		textField.setComparators(new IgnoreCaseComparator());
		Pipe pipe = new CoGroup(Pipe.pipes(sourceAttributesPipe, sourceUnitInfoPipe), Fields.fields(unitField,textField), fields, new LeftJoin());
		
		pipe = new GroupBy(pipe,new Fields(Columns.ProductID,Columns.AttributeName));
		pipe = new Every(pipe,fields,new DeleteDuplicatedUnitAggregator(fields),Fields.RESULTS);
		Pipe pipe_temp = new Each(tempProductsPipe, new Fields(Columns.ProductUUID,Columns.ProductID, Columns.UNSPSC, Columns.Manufacturer, Columns.PartNo), new Identity(), Fields.RESULTS);
		pipe = new CoGroup(Pipe.pipes(pipe, pipe_temp), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields.append(new Fields(Columns.ProductUUID,Columns.ProductID+Columns.Temp, Columns.UNSPSC, Columns.Manufacturer, Columns.PartNo)), new InnerJoin());
		fields = new Fields(Columns.ProductUUID, Columns.UNSPSC, Columns.Manufacturer, Columns.PartNo,Columns.AttributeName, Columns.Value, Columns.Unit,Columns.Text,Columns.UnitName,Columns.UnitTypeId,Columns.BaseUnit,Columns.Preca,Columns.Cf,Columns.Scale,Columns.Postca,Columns.Prec);
		pipe = new Each(pipe,fields,new Identity(),Fields.RESULTS);
		return pipe;
	}
	
	private Pipe[] createSinkTempGroupAttributeDefPipse(Pipe tempAttributesPipe, Pipe sourceOldAttributeDefPipe) {
		Pipe pipe = new GroupBy(tempAttributesPipe, new Fields(Columns.UNSPSC,Columns.AttributeName));
		pipe = new Every(pipe, new Fields(Columns.UnitTypeId,Columns.Unit), new MergeUnitsAggregator());
		
		Fields fields = new Fields(Columns.UNSPSC, Columns.AttributeName, Columns.UnitTypeId, Columns.Units, Columns.AttributeDefId, Columns.UNSPSC+Columns.Temp, Columns.AttributeName+Columns.Temp, Columns.UnitTypeId+Columns.Temp, Columns.Units+Columns.Temp);
		pipe = new CoGroup(Pipe.pipes(pipe, sourceOldAttributeDefPipe), Fields.fields(new Fields(Columns.UNSPSC,Columns.AttributeName), new Fields(Columns.UNSPSC,Columns.AttributeName)), fields, new LeftJoin());
		fields = new Fields(Columns.AttributeDefId, Columns.UNSPSC, Columns.AttributeName, Columns.UnitTypeId, Columns.Units);
		pipe = new Each(pipe,fields,new Identity());
		pipe = new Each(pipe, new Fields(Columns.AttributeDefId), new SetUUIDFunction(new Fields(Columns.AttributeDefId)), Fields.REPLACE);
		pipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);// new
		Pipe oldPipe = new Each(sourceOldAttributeDefPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		return Pipe.pipes(pipe, oldPipe);
	}
	
	private Pipe createSinkAttributeDefPipe(String outputPath, Pipe[] tempGroupAttributeDefPipse) {
		Fields fields = new Fields(Columns.AttributeDefId, Columns.UNSPSC, Columns.AttributeName, Columns.UnitTypeId, Columns.Units);
		Pipe pipe = new GroupBy(tempGroupAttributeDefPipse, new Fields(Columns.UNSPSC, Columns.AttributeName), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		pipe = new Pipe("SinkAttributeDefPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkChangedAttributeDefPipe(String outputPath, Pipe[] tempGroupAttributeDefPipse) {
		Fields fields =  new Fields(Columns.AttributeDefId, Columns.UNSPSC, Columns.AttributeName, Columns.UnitTypeId, Columns.Units);
		Pipe pipe = new GroupBy(tempGroupAttributeDefPipse, new Fields(Columns.UNSPSC, Columns.AttributeName), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedAttributeDefPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	
	private Pipe[] createSinkTempGroupAttributePipes(Pipe tempAttributesPipe, Pipe sinkAttributeDefPipe, Pipe sourceOldAttributePipe) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.UNSPSC, Columns.Manufacturer, Columns.PartNo,Columns.AttributeName, Columns.Value, Columns.Unit,Columns.Text,Columns.UnitName,Columns.UnitTypeId,Columns.BaseUnit,Columns.Preca,Columns.Cf,Columns.Scale,Columns.Postca,Columns.Prec);
		Pipe pipe = new Each(tempAttributesPipe,new FormatValueByUnitFunction(new Fields(Columns.BaseValue)),new Fields(Columns.ProductUUID,Columns.UNSPSC, Columns.AttributeName,Columns.Value, Columns.BaseValue, Columns.Unit));
		
		fields = new Fields(Columns.ProductUUID,Columns.UNSPSC, Columns.AttributeName,Columns.Value, Columns.BaseValue, Columns.Unit,Columns.AttributeDefId,Columns.UNSPSC+Columns.Temp, Columns.AttributeName+Columns.Temp,Columns.UnitTypeId,Columns.Units);
		pipe = new CoGroup(Pipe.pipes(pipe, sinkAttributeDefPipe), Fields.fields(new Fields(Columns.UNSPSC, Columns.AttributeName), new Fields(Columns.UNSPSC, Columns.AttributeName)), fields, new InnerJoin());

		fields = new Fields(Columns.ProductUUID, Columns.AttributeDefId, Columns.Value, Columns.BaseValue, Columns.Unit);
		pipe = new Each(pipe, fields, new Insert(new Fields(Columns.SortNum), 1), fields.append(new Fields(Columns.SortNum)));
		Pipe oldPipe = new Each(sourceOldAttributePipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		return Pipe.pipes(pipe, oldPipe);
	}
	
	private Pipe createSinkAttributePipe(String outputPath,Pipe[] tempGroupAttributePipes) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.AttributeDefId, Columns.Value, Columns.BaseValue, Columns.Unit);
		Pipe pipe = new GroupBy(tempGroupAttributePipes, new Fields( Columns.ProductUUID,Columns.AttributeDefId), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		pipe = new Pipe("SinkAttributePipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkChangedAttributePipe(String outputPath, Pipe[] tempGroupAttributePipes) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.AttributeDefId, Columns.Value, Columns.BaseValue, Columns.Unit);
		Pipe pipe = new GroupBy(tempGroupAttributePipes, new Fields(Columns.ProductUUID, Columns.AttributeDefId), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedAttributePipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
}
