package au.com.isell.epd.importing;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import au.com.isell.epd.aggregator.CheckChangedAggregator;
import au.com.isell.epd.aggregator.CheckOptionsAggregator;
import au.com.isell.epd.aggregator.JoinAggregator;
import au.com.isell.epd.aggregator.MergeAggregator;
import au.com.isell.epd.aggregator.MergeNumberFieldsAggregator;
import au.com.isell.epd.enums.Columns;
import au.com.isell.epd.function.FormatProductsFunction;
import au.com.isell.epd.function.SetEmptyStringToNullFunction;
import au.com.isell.epd.function.SetNullValueFunction;
import au.com.isell.epd.function.SetUUIDFunction;
import au.com.isell.epd.utils.DateUtils;
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
import cascading.pipe.assembly.Unique;
import cascading.pipe.cogroup.InnerJoin;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

import com.amazonaws.auth.PropertiesCredentials;

public class EPDProcessor {
	private Map<String, Tap> sourceTaps = new HashMap<String, Tap>();
	private Map<String, Tap> sinkTaps = new HashMap<String, Tap>();
	private TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, "\\|");// skipHeader
	private String todayStr = DateUtils.format(Calendar.getInstance(), DateUtils.SHORT_DATE_ZMS);
	private Properties properties = new Properties();

	public EPDProcessor() throws IOException {
		PropertiesCredentials awsCredentials = new PropertiesCredentials(EPDProcessor.class.getResourceAsStream("/AwsCredentials.properties"));
		properties.setProperty("fs.s3n.awsAccessKeyId", awsCredentials.getAWSAccessKeyId());
		properties.setProperty("fs.s3n.awsSecretAccessKey", awsCredentials.getAWSSecretKey());
//		properties.put("cascading.tapcollector.partname", "result");
	}

	public void execute(String inputPath, String outputPath,String oldDataPath) {
		sourceTaps.clear();
		sinkTaps.clear();

		// source
		Pipe sourceFeaturesPipe = createSourceFeaturesPipe(inputPath + "Features.txt");
		Pipe sourceProductsPipe = createSourceProductsPipe(inputPath + "Products.txt");
		Pipe sourceOptionsPipe = createSourceOptionsPipe(inputPath + "Options.txt");
		Pipe sourceCompAttributesPipe = createSourceCompAttributesPipe(inputPath + "CompAttributes.txt");

		// old source
		Pipe sourceOldEPDIdsPipe = new Each(createSourceOldEPDIdsPipe(oldDataPath+ "epdids/"),new SetEmptyStringToNullFunction(Fields.ALL));
		Pipe sourceOldSpecDefPipe = new Each(createSourceOldSpecDefPipe(oldDataPath+ "specdef/"),new SetEmptyStringToNullFunction(Fields.ALL));
		Pipe sourceOldSpecPipe = new Each(createSourceOldSpecPipe(oldDataPath+ "spec/"),new SetEmptyStringToNullFunction(Fields.ALL));
		Pipe sourceOldOptionsPipe = new Each(createSourceOldOptionsPipe(oldDataPath+ "option/"),new SetEmptyStringToNullFunction(Fields.ALL));
		Pipe sourceOldUnspscPipe = new Each(createSourceOldUnspscPipe(oldDataPath+ "unspsc/"),new SetEmptyStringToNullFunction(Fields.ALL));
		Pipe sourceOldProductsPipe = new Each(createSourceOldProductsPipe(oldDataPath+ "product/"),new SetEmptyStringToNullFunction(Fields.ALL));

		// temp
		Pipe tempProductsPipe = createTempProductsPipe(sourceOldEPDIdsPipe, sourceProductsPipe);

		//
		Pipe sinkProductsPipe = createSinkProductsPipe(outputPath + todayStr + "/all/product/", tempProductsPipe,sourceFeaturesPipe, sourceOldProductsPipe);
		Pipe sinkEPDIDPipe = createSinkEPDIdsPipe(outputPath + todayStr + "/all/epdids/", sourceOldEPDIdsPipe, tempProductsPipe);
		Pipe sinkOptionsPipe = createSinkOptionsPipe(outputPath + todayStr + "/all/option/", sourceOptionsPipe, tempProductsPipe, sourceOldOptionsPipe);
		Pipe sinkUnspscPipe = createSinkUnspscPipe(outputPath + todayStr + "/all/unspsc/", tempProductsPipe, sourceOldUnspscPipe);
		Pipe sinkSpecDefPipe = createSinkSpecDefPipe(outputPath + todayStr + "/all/specdef/", sourceCompAttributesPipe, tempProductsPipe, sourceOldSpecDefPipe);
		Pipe sinkSpecPipe = createSinkSpecPipe(outputPath + todayStr + "/all/spec/", sourceCompAttributesPipe, tempProductsPipe, sinkSpecDefPipe, sourceOldSpecPipe);
		// check
		Pipe checkOptionsPipe = checkOptionsPipe(outputPath + todayStr + "/changed/option/", sinkOptionsPipe, sourceOldOptionsPipe);
		Pipe checkUnspscPipe = checkUnspscPipe(outputPath + todayStr + "/changed/unspsc/", sinkUnspscPipe, sourceOldUnspscPipe);
		Pipe checkProductsPipe = checkProductsPipe(outputPath + todayStr + "/changed/product/", sinkProductsPipe, sourceOldProductsPipe);
		Pipe checkSpecPipe = checkSpecPipe(outputPath + todayStr + "/changed/spec/", sinkSpecPipe, sourceOldSpecPipe);
		Pipe checkSpecDefPipe = checkSpecDefPipe(outputPath + todayStr + "/changed/specdef/", sinkSpecDefPipe, sourceOldSpecDefPipe);

		Pipe[] pipes = Pipe.pipes(new Each(sinkProductsPipe,new SetNullValueFunction(Fields.ALL)),
				new Each(sinkEPDIDPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(sinkUnspscPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(sinkOptionsPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(sinkSpecDefPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(sinkSpecPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(checkOptionsPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(checkUnspscPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(checkProductsPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(checkSpecPipe,new SetNullValueFunction(Fields.ALL)), 
				new Each(checkSpecDefPipe,new SetNullValueFunction(Fields.ALL)));
		FlowConnector.setApplicationJarClass(properties, EPDProcessor.class);
		Flow flow = new FlowConnector(properties).connect(sourceTaps, sinkTaps, pipes);
		flow.start();
		flow.complete();
	}

	private Pipe checkUnspscPipe(String outputPath, Pipe sinkUnspscPipe, Pipe sourceOldUnspscPipe) {
		Fields fields = new Fields(Columns.UNSPSC, au.com.isell.epd.enums.Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity);
		Pipe oldPipe = new Each(sourceOldUnspscPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		Pipe newPipe = new Each(sinkUnspscPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);// new
		Pipe pipe = new GroupBy(Pipe.pipes(oldPipe, newPipe), new Fields(Columns.UNSPSC), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);

		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe checkSpecDefPipe(String outputPath, Pipe sinkSpecDefPipe, Pipe sourceOldSpecDefPipe) {
		Fields fields = new Fields(Columns.SpecId, Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.Status);
		Pipe oldPipe = new Each(sourceOldSpecDefPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		Pipe newPipe = new Each(sinkSpecDefPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);// new
		Pipe pipe = new GroupBy(Pipe.pipes(oldPipe, newPipe), new Fields(Columns.SpecId), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);

		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe checkSpecPipe(String outputPath, Pipe sinkSpecPipe, Pipe sourceOldSpecPipe) {
		Fields fields = new Fields(Columns.SpecId, Columns.ProductUUID, Columns.Value, Columns.Status);
		Pipe oldPipe = new Each(sourceOldSpecPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		Pipe newPipe = new Each(sinkSpecPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);// new
		Pipe pipe = new GroupBy(Pipe.pipes(oldPipe, newPipe), new Fields(Columns.SpecId, Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);

		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe checkProductsPipe(String outputPath, Pipe sinkProductsPipe, Pipe sourceOldProductsPipe) {
		Fields fields = new Fields(Columns.ProductUUID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status, Columns.Description);
		Pipe oldPipe = new Each(sourceOldProductsPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		Pipe newPipe = new Each(sinkProductsPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);// new
		Pipe pipe = new GroupBy(Pipe.pipes(oldPipe, newPipe), new Fields(Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);

		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe checkOptionsPipe(String outputPath, Pipe sinkOptionsPipe, Pipe sourceOldOptionsPipe) {
		Pipe oldPipe = new Each(sourceOldOptionsPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
		Pipe newPipe = new Each(sinkOptionsPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);// new
		Pipe pipe = new GroupBy(Pipe.pipes(oldPipe, newPipe), new Fields(Columns.Manufacturer, Columns.PartNo), new Fields(Columns.SortNum));
		pipe = new Every(pipe, new CheckOptionsAggregator(), Fields.RESULTS);

		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceProductsPipe(String inputPath) {
		Tap tap = new Hfs(scheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status);
		Pipe pipe = new Each("SourceProductsPipe", new RegexSplitter(fields, "\\|"), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldEPDIdsPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.ProductUUID);
		Pipe pipe = new Each("SourceOldEPDIdsPipe", new RegexSplitter(fields), Fields.RESULTS);

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

	private Pipe createSourceOldUnspscPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.UNSPSC, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity);
		Pipe pipe = new Each("SourceOldUnspscPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldOptionsPipe(String inputPath) {
		Tap tap = new Hfs(new TextLine(new Fields(Columns.Line)), inputPath);
		Fields fields = new Fields(Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp);
		Pipe pipe = new Each("SourceOldOptionsPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
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

	private Pipe createSourceOptionsPipe(String inputPath) {
		Tap tap = new Hfs(scheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.PartNo, Columns.OptionID);
		Pipe pipe = new Each("SourceOptionsPipe", new RegexSplitter(fields, "\\|"), new Fields(Columns.ProductID, Columns.OptionID));
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceCompAttributesPipe(String inputPath) {
		Tap tap = new Hfs(scheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.Name, Columns.Value, Columns.Group, Columns.Sequence, Columns.Level, Columns.Overview);
		Pipe pipe = new Each("SourceCompAttributesPipe", new RegexSplitter(fields, "\\|"));
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceFeaturesPipe(String inputPath) {
		Tap tap = new Hfs(scheme, inputPath);
		Fields fields = new Fields(Columns.ProductID, Columns.Description, Columns.Sequence);
		Pipe pipe = new Each("SourceFeaturesPipe", new RegexSplitter(fields, "\\|"), Fields.RESULTS);
		// group and sort
		pipe = new GroupBy(pipe, new Fields(Columns.ProductID), new Fields(Columns.Sequence));
		pipe = new Every(pipe, new Fields(Columns.Description), new JoinAggregator(new Fields(Columns.Description)," "));
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkUnspscPipe(String outputPath, Pipe tempProductsPipe, Pipe sourceOldUnspscPipe) {
		Pipe pipe = new GroupBy(tempProductsPipe, new Fields(Columns.UNSPSC));
		Fields fields = new Fields(Columns.UNSPSC, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity);
		pipe = new Every(pipe, fields, new MergeAggregator(fields, true), Fields.RESULTS);
		pipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);

		Pipe oldPipe = new Each(sourceOldUnspscPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old

		pipe = new GroupBy(Pipe.pipes(oldPipe, pipe), new Fields(Columns.UNSPSC), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);

		pipe = new Pipe("SinkUnspscPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkSpecDefPipe(String outputPath, Pipe sourceCompAttributesPipe, Pipe sinkProductsPipe, Pipe sourceOldSpecDefPipe) {
		Pipe pipe_temp = new Each(sinkProductsPipe, new Fields(Columns.ProductID, Columns.UNSPSC), new Identity(), Fields.RESULTS);
		Fields fields = new Fields(Columns.ProductID, Columns.Name, Columns.Value, Columns.Group, Columns.Sequence, Columns.Level, Columns.Overview);

		Pipe pipe = new CoGroup(Pipe.pipes(sourceCompAttributesPipe, pipe_temp), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields.append(new Fields(Columns.ProductID + Columns.Temp, Columns.UNSPSC)), new LeftJoin());

		pipe = new GroupBy(pipe, new Fields(Columns.UNSPSC, Columns.Group, Columns.Name));
		pipe = new Every(pipe, new Fields(Columns.Sequence, Columns.Level, Columns.Overview), new MergeNumberFieldsAggregator(new Fields(Columns.Sequence, Columns.Level, Columns.Overview), new int[] { MergeNumberFieldsAggregator.Min, MergeNumberFieldsAggregator.Min, MergeNumberFieldsAggregator.Max }));

		fields = new Fields(Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.SpecId, Columns.UNSPSC + Columns.Temp, Columns.Group + Columns.Temp, Columns.Name + Columns.Temp, Columns.Sequence + Columns.Temp, Columns.Level + Columns.Temp, Columns.Overview + Columns.Temp, Columns.Status + Columns.Temp);
		pipe = new CoGroup(Pipe.pipes(pipe, sourceOldSpecDefPipe), Fields.fields(new Fields(Columns.UNSPSC, Columns.Group, Columns.Name), new Fields(Columns.UNSPSC, Columns.Group, Columns.Name)), fields, new LeftJoin());
		fields = new Fields(Columns.SpecId, Columns.UNSPSC, Columns.Group, Columns.Name, Columns.Sequence, Columns.Level, Columns.Overview, Columns.Status);
		pipe = new Each(pipe, new Insert(new Fields(Columns.Status), 0), fields);
		pipe = new Unique(Pipe.pipes(pipe, sourceOldSpecDefPipe), new Fields(Columns.UNSPSC, Columns.Group, Columns.Name));
		pipe = new Each(pipe, new Fields(Columns.SpecId), new SetUUIDFunction(new Fields(Columns.SpecId)), Fields.REPLACE);

		pipe = new Pipe("SinkSpecDefPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkSpecPipe(String outputPath, Pipe sourceCompAttributesPipe, Pipe sinkProductsPipe, Pipe sinkSpecDefPipe, Pipe sourceOldSpecPipe) {
		Pipe pipe_temp = new Each(sinkProductsPipe, new Fields(Columns.ProductUUID, Columns.ProductID, Columns.UNSPSC), new Identity(), Fields.RESULTS);
		Fields fields = new Fields(Columns.ProductUUID, Columns.ProductID, Columns.UNSPSC, Columns.ProductID + Columns.Temp, Columns.Name, Columns.Value, Columns.Group, Columns.Sequence, Columns.Level, Columns.Overview);
		Pipe pipe = new CoGroup(Pipe.pipes(pipe_temp, sourceCompAttributesPipe), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields, new LeftJoin());

		fields = fields.append(new Fields(Columns.SpecId, Columns.UNSPSC + Columns.Temp, Columns.Group + Columns.Temp, Columns.Name + Columns.Temp, Columns.Sequence + Columns.Temp, Columns.Level + Columns.Temp, Columns.Overview + Columns.Temp, Columns.Status));
		pipe = new CoGroup(Pipe.pipes(pipe, sinkSpecDefPipe), Fields.fields(new Fields(Columns.UNSPSC, Columns.Group, Columns.Name), new Fields(Columns.UNSPSC, Columns.Group, Columns.Name)), fields, new InnerJoin());

		fields = new Fields(Columns.SpecId, Columns.ProductUUID, Columns.Value, Columns.Status);

		pipe = new Each(pipe, fields, new Insert(new Fields(Columns.SortNum), 1), fields.append(new Fields(Columns.SortNum)));
		Pipe oldPipe = new Each(sourceOldSpecPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old

		pipe = new GroupBy(Pipe.pipes(pipe, oldPipe), new Fields(Columns.SpecId, Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);

		pipe = new Pipe("SinkSpecPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkOptionsPipe(String outputPath, Pipe sourceOptionsPipe, Pipe tempProductsPipe, Pipe sourceOldOptionsPipe) {
		Pipe pipe_temp = new Each(tempProductsPipe, new Fields(Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.ProductID), new Identity(), Fields.RESULTS);

		Fields fields = new Fields(Columns.ProductID, Columns.OptionID, Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.ProductID + Columns.Temp);
		Pipe pipe = new CoGroup(Pipe.pipes(sourceOptionsPipe, pipe_temp), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields, new LeftJoin());
		fields = fields.append(new Fields(Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp, Columns.ProductID + Columns.Temp + Columns.Temp));
		pipe = new CoGroup(Pipe.pipes(pipe, pipe_temp), Fields.fields(new Fields(Columns.OptionID), new Fields(Columns.ProductID)), fields, new LeftJoin());

		fields = new Fields(Columns.Manufacturer, Columns.PartNo, Columns.ProductUUID, Columns.Manufacturer + Columns.Temp, Columns.PartNo + Columns.Temp, Columns.ProductUUID + Columns.Temp);

		pipe = new Each(pipe, fields, new Insert(new Fields(Columns.SortNum), 1), fields.append(new Fields(Columns.SortNum)));
		Pipe oldPipe = new Each(sourceOldOptionsPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old

		pipe = new GroupBy(Pipe.pipes(pipe, oldPipe), new Fields(Columns.ProductUUID, Columns.ProductUUID + Columns.Temp), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);

		pipe = new Pipe("SinkOptionsPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
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

	private Pipe createTempProductsPipe(Pipe sourceEPDIdsPipe, Pipe sourceProductsPipe) {

		Fields fields = new Fields(Columns.ProductID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status);
		// cogroup with EPDIdsPipe
		Pipe pipe = new CoGroup(Pipe.pipes(sourceProductsPipe, sourceEPDIdsPipe), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields.append(new Fields(Columns.ProductID + Columns.Temp, Columns.ProductUUID)), new LeftJoin());

		pipe = new Each(pipe, new Fields(Columns.ProductUUID, Columns.LastUpdated, Columns.Status), new FormatProductsFunction(), Fields.REPLACE);
		return pipe;
	}

	private Pipe createSinkProductsPipe(String outputPath, Pipe tempProductsPipe, Pipe sourceFeaturesPipe, Pipe sourceOldProductsPipe) {
		Fields fields = new Fields(Columns.ProductUUID,Columns.ProductID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC_Segment, Columns.UNSPSC_Family, Columns.UNSPSC_Class, Columns.UNSPSC_Commodity, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status);
		// cogroup with FeaturesPipe
		Pipe newPipe = new CoGroup(Pipe.pipes(tempProductsPipe, sourceFeaturesPipe), Fields.fields(new Fields(Columns.ProductID), new Fields(Columns.ProductID)), fields.append(new Fields(Columns.ProductID + Columns.Temp,Columns.ProductID + Columns.Temp + Columns.Temp, Columns.Description)), new LeftJoin());
		
		fields = new Fields(Columns.ProductUUID, Columns.Manufacturer, Columns.PartNo, Columns.Model, Columns.Manufacturer_Family, Columns.Manufacturer_Family_Member, Columns.UNSPSC, Columns.Image, Columns.LastUpdated, Columns.Status, Columns.Description);
		newPipe = new Each(newPipe, fields, new Identity(), Fields.RESULTS);
		newPipe = new Each(newPipe, fields,new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);

		Pipe oldPipe = new Each(sourceOldProductsPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old

		Pipe pipe = new GroupBy(Pipe.pipes(oldPipe, newPipe), new Fields(Columns.ProductUUID), new Fields(Columns.SortNum));
		pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);

		pipe = new Pipe("SinkProductsPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private void deleteFiles(String path,String name) {
		try {
			File file = new File(path);
			File[] files = file.listFiles(filter( name));
			for (int i = 0; files != null && i < files.length; i++)
				files[i].delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FilenameFilter filter(final String type) throws Exception {
		return new FilenameFilter() {
			public boolean accept(File file, String path) {
				String filename = new File(path).getName();
				return filename.indexOf(type) != -1;
			}
		};
	}


}
