package au.com.isell.epd.importing;

import au.com.isell.epd.aggregator.MergeAggregator;
import au.com.isell.epd.enums.Columns;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class EPDIdsFlow extends BaseProductsFlow {

	public EPDIdsFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath);
	}
	
	public Flow createFlow() {
		sourceTaps.clear();
		sinkTaps.clear();
		Pipe tempProductsPipe = createFormatEmptyStringToNullPipe(createTempProductsPipe(commonOutputPath + "/temp/product/"));
		Pipe sourceOldEPDIdsPipe = createFormatEmptyStringToNullPipe(createSourceOldEPDIdsPipe(commonOldDataPath + "epdids/"));
		Pipe sinkEPDIDPipe = createSinkEPDIdsPipe(commonOutputPath + "/all/epdids/", sourceOldEPDIdsPipe, tempProductsPipe);
		return flowConnector.connect(sourceTaps, sinkTaps, createFormatNullPipe(sinkEPDIDPipe));
	}

	private Pipe createSinkEPDIdsPipe(String outputPath, Pipe sourceOldEPDIdsPipe, Pipe tempProductsPipe) {
		Fields fields = new Fields(Columns.ProductID, Columns.ProductUUID);
		Pipe newPipe = new Each(tempProductsPipe, fields, new Identity(), Fields.RESULTS);
		newPipe = new Each(newPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);

		Pipe oldPipe = new Each(sourceOldEPDIdsPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old

		Pipe pipe = new GroupBy(Pipe.pipes(oldPipe, newPipe), new Fields(Columns.ProductUUID, Columns.ProductID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);

		pipe = new Pipe("SinkEPDIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

}