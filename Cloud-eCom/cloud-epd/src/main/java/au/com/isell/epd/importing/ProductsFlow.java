package au.com.isell.epd.importing;

import au.com.isell.epd.aggregator.CheckChangedAggregator;
import au.com.isell.epd.aggregator.JoinAggregator;
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

public class ProductsFlow extends BaseProductsFlow{
	
	public ProductsFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath);
	}

	public Flow createFlow() {
		sourceTaps.clear();
		sinkTaps.clear();
		Pipe sourceFeaturesPipe = createSourceFeaturesPipe(commonInputPath + "Features.txt");
		Pipe sourceOldProductsPipe = createFormatEmptyStringToNullPipe(createSourceOldProductsPipe(commonOldDataPath + "product/"));
		Pipe tempProductsPipe = createFormatEmptyStringToNullPipe(createTempProductsPipe(commonOutputPath + "temp/product/"));
		Pipe[] tempGroupProductsPipes = createSinkTempGroupProductsPipes(tempProductsPipe, sourceFeaturesPipe, sourceOldProductsPipe);
		Pipe sinkProductsPipe = createSinkProductsPipe(commonOutputPath + "all/product/",tempGroupProductsPipes);
		Pipe sinkChangedProductsPipe = createSinkChangedProductsPipe(commonOutputPath + "changed/product/",tempGroupProductsPipes);
		return flowConnector.connect(sourceTaps, sinkTaps, createFormatNullPipe(sinkProductsPipe),createFormatNullPipe(sinkChangedProductsPipe));
	}

	private Pipe createSourceFeaturesPipe(String inputPath) {
		Tap tap = new Hfs(skipHeaderScheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.Description, Columns.Sequence);
		Pipe pipe = new Each("SourceFeaturesPipe", new RegexSplitter(fields, "\\|"), Fields.RESULTS);
		// group and sort
		pipe = new GroupBy(pipe, new Fields(Columns.ProductID), new Fields(Columns.Sequence));
		pipe = new Every(pipe, new Fields(Columns.Description), new JoinAggregator(new Fields(Columns.Description), " "));
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createSourceOldProductsPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.ProductUUID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status, Columns.Description);
		Pipe pipe = new Each("SourceOldProductsPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe[] createSinkTempGroupProductsPipes(Pipe tempProductsPipe, Pipe sourceFeaturesPipe, Pipe sourceOldProductsPipe) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.ProductID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status);
		// cogroup with FeaturesPipe
		Pipe newPipe = new CoGroup(Pipe.pipes(tempProductsPipe, sourceFeaturesPipe), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields.append(new Fields(Columns.ProductID + Columns.Temp, Columns.Description)), new LeftJoin());

		fields = new Fields(Columns.ProductUUID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status, Columns.Description);
		newPipe = new Each(newPipe, fields, new Identity(), Fields.RESULTS);
		newPipe = new Each(newPipe, fields, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);

		Pipe oldPipe = new Each(sourceOldProductsPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		return Pipe.pipes(oldPipe, newPipe);
	}
	
	private Pipe createSinkProductsPipe(String outputPath, Pipe[] tempGroupProductsPipes) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status, Columns.Description);
		Pipe pipe = new GroupBy(tempGroupProductsPipes, new Fields(Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		pipe = new Pipe("SinkProductsPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkChangedProductsPipe(String outputPath, Pipe[] tempGroupProductsPipes) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status, Columns.Description);
		Pipe pipe = new GroupBy(tempGroupProductsPipes, new Fields(Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedProductsPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
}
