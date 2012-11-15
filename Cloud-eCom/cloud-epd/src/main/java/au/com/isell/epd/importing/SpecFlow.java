package au.com.isell.epd.importing;

import au.com.isell.epd.aggregator.CheckChangedAggregator;
import au.com.isell.epd.aggregator.MergeAggregator;
import au.com.isell.epd.aggregator.MergeNumberFieldsAggregator;
import au.com.isell.epd.enums.Columns;
import au.com.isell.epd.function.SetNullValueFunction;
import au.com.isell.epd.function.SetUUIDFunction;
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
import cascading.pipe.cogroup.InnerJoin;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class SpecFlow extends BaseProductsFlow{
	
	public SpecFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath);
	}

	public Flow createFlow() {
		sourceTaps.clear();
		sinkTaps.clear();
		Pipe tempProductsPipe = createFormatEmptyStringToNullPipe(createTempProductsPipe(commonOutputPath + "temp/product/"));
		Pipe sourceCompAttributesPipe = createSourceCompAttributesPipe(commonInputPath + "CompAttributes.txt");
		Pipe tempCompAttributesPipe = createTempCompAttributesPipe(sourceCompAttributesPipe, tempProductsPipe);
		
		Pipe sourceOldSpecDefPipe = createFormatEmptyStringToNullPipe(createSourceOldSpecDefPipe(commonOldDataPath + "specdef/"));
		Pipe[] tempGroupSpecDefPipes = createSinkTempGroupSpecDefPipes(tempCompAttributesPipe,sourceOldSpecDefPipe);
		Pipe sinkSpecDefPipe = createSinkSpecDefPipe(commonOutputPath + "all/specdef/",tempGroupSpecDefPipes);
		Pipe sinkChangedSpecDefPipe = createSinkChangedSpecDefPipe(commonOutputPath + "changed/specdef/", tempGroupSpecDefPipes);
		
		Pipe sourceOldSpecPipe = createFormatEmptyStringToNullPipe(createSourceOldSpecPipe(commonOldDataPath + "spec/"));
		Pipe[] tempGroupSpecPipes = createSinkTempGroupSpecPipes(tempCompAttributesPipe, sinkSpecDefPipe,sourceOldSpecPipe);
		Pipe sinkSpecPipe = createSinkSpecPipe(commonOutputPath + "all/spec/", tempGroupSpecPipes);
		Pipe sinkChangedSpecPipe = createSinkChangedSpecPipe(commonOutputPath + "changed/spec/",tempGroupSpecPipes);
		
		return flowConnector.connect(sourceTaps, sinkTaps, createFormatNullPipe(sinkSpecDefPipe),
				createFormatNullPipe(sinkChangedSpecDefPipe),
				createFormatNullPipe(sinkSpecPipe), 
				createFormatNullPipe(sinkChangedSpecPipe));

	}

	private Pipe createSourceOldSpecDefPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.SpecId, Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.Status);
		Pipe pipe = new Each("SourceOldSpecDefPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldSpecPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.SpecId, Columns.ProductUUID, Columns.Value, Columns.Status);
		Pipe pipe = new Each("SourceOldSpecPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createSourceCompAttributesPipe(String inputPath) {
		Tap tap = new Hfs(skipHeaderScheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.Name, Columns.Value, Columns.Group, Columns.Sequence, Columns.Level, Columns.Overview);
		Pipe pipe = new Each("SourceCompAttributesPipe", new RegexSplitter(fields, "\\|"));
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createTempCompAttributesPipe(Pipe sourceCompAttributesPipe, Pipe tempProductsPipe){
		Pipe pipe_temp = new Each(tempProductsPipe, new Fields(Columns.ProductUUID,Columns.ProductID, Columns.UNSPSC), new Identity(), Fields.RESULTS);
		Fields fields = new Fields(Columns.ProductID, Columns.Name, Columns.Value, Columns.Group, Columns.Sequence, Columns.Level, Columns.Overview);
		Pipe pipe = new CoGroup(Pipe.pipes(sourceCompAttributesPipe, pipe_temp), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields.append(new Fields(Columns.ProductUUID,Columns.ProductID + Columns.Temp, Columns.UNSPSC)), new LeftJoin());
		fields = new Fields(Columns.ProductUUID,Columns.UNSPSC,Columns.Name, Columns.Value, Columns.Group, Columns.Sequence, Columns.Level, Columns.Overview);
		pipe = new Each(pipe,fields,new Identity());
		return pipe;
	}
	
	private Pipe[] createSinkTempGroupSpecDefPipes(Pipe tempCompAttributesPipe, Pipe sourceOldSpecDefPipe) {
		Pipe pipe = new GroupBy(tempCompAttributesPipe, new Fields(Columns.UNSPSC, Columns.Group, Columns.Name));
		pipe = new Every(pipe, new Fields(Columns.Sequence, Columns.Level, Columns.Overview), new MergeNumberFieldsAggregator(new Fields(Columns.Sequence, Columns.Level, Columns.Overview), new int[] { MergeNumberFieldsAggregator.Min, MergeNumberFieldsAggregator.Min, MergeNumberFieldsAggregator.Max }));
		Fields fields = new Fields(Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.SpecId, Columns.UNSPSC + Columns.Temp, Columns.Group + Columns.Temp, Columns.Name + Columns.Temp, Columns.Sequence + Columns.Temp, Columns.Level + Columns.Temp, Columns.Overview + Columns.Temp, Columns.Status);
		pipe = new CoGroup(Pipe.pipes(pipe, sourceOldSpecDefPipe), Fields.fields(new Fields(Columns.UNSPSC, Columns.Group, Columns.Name), new Fields(Columns.UNSPSC, Columns.Group, Columns.Name)), fields, new LeftJoin());
		
		fields = new Fields(Columns.SpecId, Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.Status);
		
		pipe = new Each(pipe,fields,new Identity());
		pipe = new Each(pipe,new Fields(Columns.Status),new SetNullValueFunction(new Fields(Columns.Status), 0), Fields.REPLACE);
		pipe = new Each(pipe, new Fields(Columns.SpecId), new SetUUIDFunction(new Fields(Columns.SpecId)), Fields.REPLACE);
		
		pipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);// new
		Pipe oldPipe = new Each(sourceOldSpecDefPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		return Pipe.pipes(pipe,oldPipe);
	}
	
	private Pipe createSinkSpecDefPipe(String outputPath, Pipe[] tempGroupSpecDefPipes) {
		Fields fields = new Fields(Columns.SpecId, Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.Status);
		Pipe pipe = new GroupBy(tempGroupSpecDefPipes, new Fields(Columns.UNSPSC, Columns.Group, Columns.Name), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		pipe = new Pipe("SinkSpecDefPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkChangedSpecDefPipe(String outputPath, Pipe[] tempGroupSpecDefPipes) {
		Fields fields = new Fields(Columns.SpecId, Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.Status);
		Pipe pipe = new GroupBy(tempGroupSpecDefPipes, new Fields(Columns.SpecId), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedSpecDefPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	
	private Pipe[] createSinkTempGroupSpecPipes(Pipe tempCompAttributesPipe, Pipe sinkSpecDefPipe, Pipe sourceOldSpecPipe) {
		Fields fields = new Fields(Columns.ProductUUID,Columns.UNSPSC,Columns.Name, Columns.Value, Columns.Group, Columns.Sequence, Columns.Level, Columns.Overview);
		fields = fields.append(new Fields(Columns.SpecId, Columns.UNSPSC + Columns.Temp, Columns.Group + Columns.Temp, Columns.Name + Columns.Temp, Columns.Sequence + Columns.Temp, Columns.Level + Columns.Temp, Columns.Overview + Columns.Temp, Columns.Status));
		Pipe pipe = new CoGroup(Pipe.pipes(tempCompAttributesPipe, sinkSpecDefPipe), Fields.fields(new Fields(Columns.UNSPSC, Columns.Group, Columns.Name), new Fields(Columns.UNSPSC, Columns.Group, Columns.Name)), fields, new InnerJoin());
		fields = new Fields(Columns.SpecId, Columns.ProductUUID, Columns.Value, Columns.Status);
		pipe = new Each(pipe, fields, new Insert(new Fields(Columns.SortNum), 1), fields.append(new Fields(Columns.SortNum)));
		
		Pipe oldPipe = new Each(sourceOldSpecPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		return Pipe.pipes(pipe, oldPipe);
	}
	
	private Pipe createSinkSpecPipe(String outputPath, Pipe[] tempGroupSpecPipes) {
		Fields fields = new Fields(Columns.SpecId, Columns.ProductUUID, Columns.Value, Columns.Status);
		Pipe pipe = new GroupBy(tempGroupSpecPipes, new Fields(Columns.SpecId, Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		pipe = new Pipe("SinkSpecPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkChangedSpecPipe(String outputPath, Pipe[] tempGroupSpecPipes) {
		Fields fields = new Fields(Columns.SpecId, Columns.ProductUUID, Columns.Value, Columns.Status);
		Pipe pipe = new GroupBy(tempGroupSpecPipes, new Fields(Columns.SpecId, Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedSpecPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

}
