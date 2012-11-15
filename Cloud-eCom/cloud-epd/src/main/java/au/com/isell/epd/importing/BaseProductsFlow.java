package au.com.isell.epd.importing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import au.com.isell.epd.enums.Columns;
import au.com.isell.epd.function.SetEmptyStringToNullFunction;
import au.com.isell.epd.function.SetNullValueFunction;
import au.com.isell.epd.utils.DateUtils;
import cascading.flow.FlowConnector;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.scheme.SequenceFile;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class BaseProductsFlow {
	protected Map<String, Tap> sourceTaps = new HashMap<String, Tap>();
	protected Map<String, Tap> sinkTaps = new HashMap<String, Tap>();
	protected TextLine skipHeaderScheme = new TextDelimited(new Fields(Columns.Line), true, "\\|");// skipHeader
	private String todayStr = DateUtils.format(Calendar.getInstance(), DateUtils.SHORT_DATE_ZMS);
	protected String commonOldDataPath = "s3n://isell.test/epd/old-data/";
	protected String commonInputPath = "s3n://isell.test/epd/import/pending/China/";
	protected String commonOutputPath = "s3n://isell.test/epd/output/"+todayStr+"/";
	protected FlowConnector flowConnector = null;

	protected BaseProductsFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath){
		this.flowConnector = flowConnector;
		this.commonInputPath = commonInputPath;
		this.commonOutputPath = commonOutputPath+todayStr+"/";
		this.commonOldDataPath = commonOldDataPath;
	}
	
	protected Pipe createSourceOldEPDIdsPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.ProductUUID);
		Pipe pipe = new Each("SourceOldEPDIdsPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	protected Pipe createTempProductsPipe(String inputPath) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.ProductID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status);
		Tap tap = new Hfs(new SequenceFile(fields), inputPath);
		Pipe pipe = new Pipe("SourceTempProductsPipe");
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	protected Pipe createFormatEmptyStringToNullPipe(Pipe pipe) {
		return new Each(pipe, new SetEmptyStringToNullFunction(Fields.ALL));
	}
	protected Pipe createFormatNullPipe(Pipe pipe) {
		return new Each(pipe, new SetNullValueFunction(Fields.ALL));
	}
}
