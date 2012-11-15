package au.com.isell.epd.importing;

import au.com.isell.epd.aggregator.CheckChangedAggregator;
import au.com.isell.epd.aggregator.MergeAggregator;
import au.com.isell.epd.enums.Columns;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Insert;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class UnspscFlow extends BaseProductsFlow{
	
	public UnspscFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath);
	}
	public Flow createFlow() {
		sourceTaps.clear();
		sinkTaps.clear();
		Pipe tempProductsPipe = createFormatEmptyStringToNullPipe(createTempProductsPipe(commonOutputPath + "temp/product/"));
		Pipe sourceOldUnspscPipe = createFormatEmptyStringToNullPipe(createSourceOldUnspscPipe(commonOldDataPath + "unspsc/"));
		Pipe[] tempGroupUnspscPipes = createSinkTempGroupUnspscPipes(tempProductsPipe,sourceOldUnspscPipe);
		Pipe sinkUnspscPipe = createSinkUnspscPipe(commonOutputPath + "all/unspsc/", tempGroupUnspscPipes);
		Pipe sinkChangedUnspscPipe = createSinkChangedUnspscPipe(commonOutputPath + "changed/unspsc/", tempGroupUnspscPipes);
		return flowConnector.connect(sourceTaps, sinkTaps, createFormatNullPipe(sinkUnspscPipe), createFormatNullPipe(sinkChangedUnspscPipe));
	}

	private Pipe[] createSinkTempGroupUnspscPipes(Pipe tempProductsPipe, Pipe sourceOldUnspscPipe) {
		Pipe pipe = new GroupBy(tempProductsPipe, new Fields(Columns.UNSPSC));
		Fields fields = new Fields(Columns.UNSPSC, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity);
		pipe = new Every(pipe, fields, new MergeAggregator(fields, true), Fields.RESULTS);
		pipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);

		Pipe oldPipe = new Each(sourceOldUnspscPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		return Pipe.pipes(oldPipe, pipe);
	}
	
	private Pipe createSinkUnspscPipe(String outputPath,Pipe[] tempGroupUnspscPipes) {
		Fields fields = new Fields(Columns.UNSPSC, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity);
		Pipe pipe = new GroupBy(tempGroupUnspscPipes, new Fields(Columns.UNSPSC), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);

		pipe = new Pipe("SinkUnspscPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkChangedUnspscPipe(String outputPath, Pipe[] tempGroupUnspscPipes) {
		Fields fields = new Fields(Columns.UNSPSC, au.com.isell.epd.enums.Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity);
		Pipe pipe = new GroupBy(tempGroupUnspscPipes, new Fields(Columns.UNSPSC), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		
		pipe = new Pipe("SinkChangedUnspscPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldUnspscPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.UNSPSC, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity);
		Pipe pipe = new Each("SourceOldUnspscPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
}
