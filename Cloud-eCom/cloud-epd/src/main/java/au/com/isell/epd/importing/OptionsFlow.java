package au.com.isell.epd.importing;

import au.com.isell.epd.aggregator.CheckOptionsAggregator;
import au.com.isell.epd.aggregator.MergeAggregator;
import au.com.isell.epd.enums.Columns;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class OptionsFlow extends BaseProductsFlow{
	
	public OptionsFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath);
	}

	public Flow createFlow() {
		sourceTaps.clear();
		sinkTaps.clear();
		Pipe tempProductsPipe = createFormatEmptyStringToNullPipe(createTempProductsPipe(commonOutputPath + "temp/product/"));
		Pipe sourceOptionsPipe = createFormatEmptyStringToNullPipe(createSourceOptionsPipe(commonInputPath + "Options.txt"));
		
		Pipe sourceOldOptionsPipe = createFormatEmptyStringToNullPipe(createSourceOldOptionsPipe(commonOldDataPath + "option/"));
		Pipe[] tempGroupOptionsPipes = createSinkTempGroupOptionsPipes(sourceOptionsPipe, tempProductsPipe, sourceOldOptionsPipe);
		Pipe sinkOptionsPipe = createSinkOptionsPipe(commonOutputPath + "all/option/",tempGroupOptionsPipes);
		Pipe checkOptionsPipe = createSinkChangedOptionsPipe(commonOutputPath + "changed/option/",tempGroupOptionsPipes);
		return flowConnector.connect(sourceTaps, sinkTaps, createFormatNullPipe(sinkOptionsPipe),createFormatNullPipe(checkOptionsPipe));
	
	}

	private Pipe createSourceOldOptionsPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp);
		Pipe pipe = new Each("SourceOldOptionsPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOptionsPipe(String inputPath) {
		Tap tap = new Hfs(skipHeaderScheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.PartNo, Columns.OptionID);
		Pipe pipe = new Each("SourceOptionsPipe", new RegexSplitter(fields, "\\|"), new Fields(Columns.ProductID, Columns.OptionID));
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe[] createSinkTempGroupOptionsPipes(Pipe sourceOptionsPipe, Pipe tempProductsPipe, Pipe sourceOldOptionsPipe) {
		Pipe pipe_temp = new Each(tempProductsPipe, new Fields(Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.ProductID), new Identity(), Fields.RESULTS);

		Fields fields = new Fields(Columns.ProductID, Columns.OptionID, Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.ProductID + Columns.Temp);
		Pipe pipe = new CoGroup(Pipe.pipes(sourceOptionsPipe, pipe_temp), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields, new LeftJoin());
		fields = fields.append(new Fields(Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp, Columns.ProductID + Columns.Temp + Columns.Temp));
		pipe = new CoGroup(Pipe.pipes(pipe, pipe_temp), Fields.fields(new Fields(Columns.OptionID), new Fields(Columns.ProductID)), fields, new LeftJoin());

		fields = new Fields(Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp);

		pipe = new Each(pipe, fields, new Insert(new Fields(Columns.SortNum), 1), fields.append(new Fields(Columns.SortNum)));
		Pipe oldPipe = new Each(sourceOldOptionsPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old

		return Pipe.pipes(pipe, oldPipe);
	}
	
	private Pipe createSinkOptionsPipe(String outputPath, Pipe[] tempGroupOptionsPipes) {
		Fields fields = new Fields(Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp);
		Pipe pipe = new GroupBy(tempGroupOptionsPipes, new Fields(Columns.ProductUUID, Columns.ProductUUID + Columns.Temp), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);

		pipe = new Pipe("SinkOptionsPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	
	private Pipe createSinkChangedOptionsPipe(String outputPath, Pipe[] tempGroupOptionsPipes) {
		Pipe pipe = new GroupBy(tempGroupOptionsPipes, new Fields(Columns.Manufacturer, Columns.PartNo), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckOptionsAggregator(), Fields.RESULTS);
		pipe = new Pipe("SinkChangedOptionsPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
}
