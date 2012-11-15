package au.com.isell.epd.importing;

import au.com.isell.epd.enums.Columns;
import au.com.isell.epd.function.FormatProductsFunction;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class TempProductsFlow extends BaseProductsFlow{
	
	public TempProductsFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath);
	}

	public Flow createFlow() {
		sourceTaps.clear();
		sinkTaps.clear();
		Pipe sourceProductsPipe = createFormatEmptyStringToNullPipe(createSourceProductsPipe(commonInputPath + "Products.txt"));
		Pipe sourceOldEPDIdsPipe = createFormatEmptyStringToNullPipe(createSourceOldEPDIdsPipe(commonOldDataPath + "epdids/"));
		Pipe tempSinkProductsPipe = createSinkTempProductsPipe(commonOutputPath + "temp/product/", sourceProductsPipe, sourceOldEPDIdsPipe);
		return flowConnector.connect(sourceTaps, sinkTaps,createFormatNullPipe(tempSinkProductsPipe));

	}

	private Pipe createSinkTempProductsPipe(String outputPath, Pipe sourceProductsPipe, Pipe sourceOldEPDIdsPipe) {
		Fields fields = new Fields(Columns.ProductID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status);
		// cogroup with EPDIdsPipe
		Pipe pipe = new CoGroup(Pipe.pipes(sourceProductsPipe, sourceOldEPDIdsPipe), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields.append(new Fields(Columns.ProductID + Columns.Temp, Columns.ProductUUID)), new LeftJoin());
		pipe = new Each(pipe, new Fields(Columns.ProductUUID, Columns.LastUpdated, Columns.Status), new FormatProductsFunction(), Fields.REPLACE);
		pipe = new Pipe("SinkTempProductsPipe", pipe);
		Tap tap = new Hfs(new SequenceFile(new Fields(Columns.ProductUUID).append(fields)), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	
	}

	private Pipe createSourceProductsPipe(String inputPath) {
		Tap tap = new Hfs(skipHeaderScheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status);
		Pipe pipe = new Each("SourceProductsPipe", new RegexSplitter(fields, "\\|"), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
}
