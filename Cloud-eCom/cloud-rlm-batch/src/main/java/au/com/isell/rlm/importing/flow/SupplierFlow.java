package au.com.isell.rlm.importing.flow;

import au.com.isell.rlm.importing.aggregator.common.CheckChangedAggregator;
import au.com.isell.rlm.importing.aggregator.common.MergeAggregator;
import au.com.isell.rlm.importing.aggregator.supplier.SupplierStoreAggregator;
import au.com.isell.rlm.importing.constant.Columns;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.constant.reseller.ResellerFilePath;
import au.com.isell.rlm.importing.constant.supplier.SupplierColumns;
import au.com.isell.rlm.importing.constant.supplier.SupplierFilePath;
import au.com.isell.rlm.importing.filter.NotEqualsFilter;
import au.com.isell.rlm.importing.function.common.CSVSplitter;
import au.com.isell.rlm.importing.function.common.SearchAddressStateIDFunction;
import au.com.isell.rlm.importing.function.common.SetUUIDFunction;
import au.com.isell.rlm.importing.function.reseller.ResellerBreakStoreFunction;
import au.com.isell.rlm.importing.function.supplier.SupplierBranchStoreFunction;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowListener;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.aggregator.First;
import cascading.operation.filter.FilterNull;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.CountBy;
import cascading.pipe.assembly.Unique;
import cascading.pipe.cogroup.InnerJoin;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class SupplierFlow extends BaseFlow{
	
	public SupplierFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath,String commonOldUUIDPath,String onlyUpdateChange){
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath,commonOldUUIDPath,onlyUpdateChange);
	}

	@Override
	public Flow createFlow() {
	
		Pipe sourceSupplierBranchPipe = createFormatEmptyStringToNullPipe(createSourceSupplierBranchPipe(commonInputPath + SupplierFilePath.SOURCE_SUPPLIER_BRANCH_FILE));
		Pipe sourceOldSupplierBranchPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldSupplierBranchPipe = createFormatEmptyStringToNullPipe(createSourceOldSupplierBranchPipe(commonOldDataPath + SupplierFilePath.OLD_SUPPLIER_BRANCH_PATH));
		}
		Pipe sourceOldSupplierBranchUUIDPipe = createFormatEmptyStringToNullPipe(createSourceOldSupplierBranchUUIDPipe(commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_BRANCH_UUID_PATH));
		Pipe[] tempGroupSupplierBranchPipes = createSinkTempGroupSupplierBranchPipes(sourceSupplierBranchPipe,sourceOldSupplierBranchPipe,sourceOldSupplierBranchUUIDPipe);
		Pipe sinkSupplierBranchPipe = createSinkSupplierBranchPipe(commonOutputPath + SupplierFilePath.ALL_SUPPLIER_BRANCH_PATH,tempGroupSupplierBranchPipes);
		Pipe sinkSupplierBranchUUIDPipe = createSinkSupplierBranchUUIDPipe(commonOutputPath + SupplierFilePath.UUID_SUPPLIER_BRANCH_PATH,tempGroupSupplierBranchPipes);
		Pipe sinkChangedSupplierBranchPipe = createSinkChangedSupplierBranchPipe(commonOutputPath + SupplierFilePath.CHANGED_SUPPLIER_BRANCH_PATH,tempGroupSupplierBranchPipes);
		
		Pipe sourceSupplierPriceBreakPipe = createFormatEmptyStringToNullPipe(createSourceSupplierPriceBreakPipe(commonInputPath + SupplierFilePath.SOURCE_SUPPLIER_PRICEBREAK_FILE));
		Pipe sourceResellerBreakPipe = createFormatEmptyStringToNullPipe(createSourceResellerBreakPipe(commonInputPath + ResellerFilePath.SOURCE_RESELLER_BREAK_FILE));
		
		Pipe resellerCountPipe = createResellerBreakMapCountBySupplierPipe(sourceResellerBreakPipe);
		Pipe sourceSupplierInfoPipe = createFormatEmptyStringToNullPipe(createSourceSupplierInfoPipe(commonInputPath + SupplierFilePath.SOURCE_SUPPLIER_FILE));
		Pipe sourceOldSupplierInfoPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldSupplierInfoPipe = createFormatEmptyStringToNullPipe(createSourceOldSupplierInfoPipe(commonOldDataPath + SupplierFilePath.OLD_SUPPLIER_PATH));
		}
		Pipe sourceOldSupplierUserSchedUUIDPipe=createFormatEmptyStringToNullPipe(createSourceOldSupplierUserSchedUUIDPipe(commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_USER_SCHED_UUID_PATH));
		Pipe sourceOldSupplierBreakUUIDPipe=createFormatEmptyStringToNullPipe(createSourceOldSupplierBreakUUIDPipe(commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_BREAK_UUID_PATH));
		
		Pipe[] tempGroupSupplierInfoPipes = createSinkTempGroupSupplierInfoPipes(sourceSupplierInfoPipe,sourceSupplierPriceBreakPipe,sourceOldSupplierInfoPipe,sourceOldSupplierUserSchedUUIDPipe,sourceOldSupplierBreakUUIDPipe,resellerCountPipe);
		Pipe sinkSupplierInfoPipe = createSinkSupplierInfoPipe(commonOutputPath + SupplierFilePath.ALL_SUPPLIER_PATH,tempGroupSupplierInfoPipes);
		Pipe sinkSupplierUserSchedUUIDPipe = createSinkSupplierUserSchedUUIDPipe(commonOutputPath + SupplierFilePath.UUID_SUPPLIER_USER_SCHED_PATH,tempGroupSupplierInfoPipes);
		Pipe sinkSupplierBreakUUIDPipe = createSinkSupplierBreakUUIDPipe(commonOutputPath + SupplierFilePath.UUID_SUPPLIER_BREAK_PATH,tempGroupSupplierInfoPipes);
		
		Pipe sinkChangedSupplierInfoPipe = createSinkChangedSupplierInfoPipe(commonOutputPath + SupplierFilePath.CHANGED_SUPPLIER_PATH,tempGroupSupplierInfoPipes,sinkSupplierBranchPipe);
		
		
		Pipe sourceResellerInfoPipe = createFormatEmptyStringToNullPipe(createSourceResellerInfoPipe(commonInputPath + ResellerFilePath.SOURCE_RESELLER_FILE));
		Pipe sourceOldResellerBreakPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldResellerBreakPipe = createFormatEmptyStringToNullPipe(createSourceOldResellerBreakPipe(commonOldDataPath + ResellerFilePath.OLD_RESELLER_BREAK_PATH));
		}
		Pipe resellerBreakInfo = createResellerBreakInfoPipe(sourceResellerBreakPipe,sinkSupplierInfoPipe,sourceResellerInfoPipe);
		Pipe[] tempGroupResellerBreakPipes = createSinkTempGroupResellerBreakPipes(resellerBreakInfo,sourceOldResellerBreakPipe);
		Pipe sinkResellerBreakPipe = createSinkResellerBreakPipe(commonOutputPath + ResellerFilePath.ALL_RESELLER_BREAK_PATH,tempGroupResellerBreakPipes);
		Pipe sinkChangedResellerBreakPipe = createSinkChangedResellerBreakPipe(commonOutputPath + ResellerFilePath.CHANGED_RESELLER_BREAK_PATH,tempGroupResellerBreakPipes);
		Flow flow = flowConnector.connect(sourceTaps, sinkTaps
				,createFormatNullPipe(sinkSupplierBranchPipe)
				,createFormatNullPipe(sinkSupplierBranchUUIDPipe)
				,createFormatNullPipe(sinkChangedSupplierBranchPipe)
				,createFormatNullPipe(sinkSupplierInfoPipe)
				,createFormatNullPipe(sinkSupplierUserSchedUUIDPipe)
				,createFormatNullPipe(sinkSupplierBreakUUIDPipe)
				,createFormatNullPipe(sinkChangedSupplierInfoPipe)
				,createFormatNullPipe(sinkResellerBreakPipe)
				,createFormatNullPipe(sinkChangedResellerBreakPipe));
		
		flow.addListener(new FlowListener() {
			@Override
			public boolean onThrowable(Flow arg0, Throwable arg1) {
				return false;
			}
			
			@Override
			public void onStopping(Flow arg0) {
			}
			
			@Override
			public void onStarting(Flow arg0) {
				createDir(commonOldDataPath + SupplierFilePath.OLD_SUPPLIER_BRANCH_PATH);
				createDir(commonOldDataPath + SupplierFilePath.OLD_SUPPLIER_PATH);
				createDir(commonOldDataPath + ResellerFilePath.OLD_RESELLER_BREAK_PATH);
				
				createDir(commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_BRANCH_UUID_PATH);
				createDir(commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_BREAK_UUID_PATH);
				createDir(commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_USER_SCHED_UUID_PATH);
			}
			
			@Override
			public void onCompleted(Flow arg0) {
				// move file
				copyFile(commonOutputPath + SupplierFilePath.ALL_SUPPLIER_BRANCH_PATH,commonOldDataPath + SupplierFilePath.OLD_SUPPLIER_BRANCH_PATH);
				copyFile(commonOutputPath + SupplierFilePath.ALL_SUPPLIER_PATH,commonOldDataPath + SupplierFilePath.OLD_SUPPLIER_PATH);
				copyFile(commonOutputPath + ResellerFilePath.ALL_RESELLER_BREAK_PATH,commonOldDataPath + ResellerFilePath.OLD_RESELLER_BREAK_PATH);
				
				copyFile(commonOutputPath + SupplierFilePath.UUID_SUPPLIER_BRANCH_PATH,commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_BRANCH_UUID_PATH);
				copyFile(commonOutputPath + SupplierFilePath.UUID_SUPPLIER_BREAK_PATH,commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_BREAK_UUID_PATH);
				copyFile(commonOutputPath + SupplierFilePath.UUID_SUPPLIER_USER_SCHED_PATH,commonOldUUIDPath + SupplierFilePath.OLD_SUPPLIER_USER_SCHED_UUID_PATH);
			}
		});
		return flow;

	}
	private Pipe createSourceSupplierInfoPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ShortName,SupplierColumns.ActiveStates,SupplierColumns.UpdatingComments
				,SupplierColumns.UpdateRegularity,SupplierColumns.SupplierStability,SupplierColumns.QikPriceStartup,SupplierColumns.ShowStockInfo,SupplierColumns.ShowStockDate
				,SupplierColumns.WEBAddress,SupplierColumns.ApprovalTo,SupplierColumns.ApprovalMethod,SupplierColumns.ApprovalEmailAddress);
		
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true,",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceSupplierInfoPipe", new CSVSplitter(fields,",","\"",true), Fields.RESULTS);
		
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOldSupplierInfoPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ShortName,SupplierColumns.ActiveStates,SupplierColumns.WEBAddress,
				SupplierColumns.ApprovalTo,SupplierColumns.ApprovalEmailAddress,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID,SupplierColumns.ResellerCount,
				SupplierColumns.BreakUUID,SupplierColumns.BreakID,SupplierColumns.Disabled,SupplierColumns.BreakName);
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldSupplierInfoPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOldSupplierUserSchedUUIDPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID);
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldSupplierUserSchedUUIDPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOldSupplierBreakUUIDPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.BreakUUID,SupplierColumns.BreakID);
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldSupplierBreakUUIDPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe[] createSinkTempGroupSupplierInfoPipes(Pipe sourceSupplierInfoPipe,Pipe sourceSupplierPriceBreakPipe, Pipe sourceOldSupplierInfoPipe, Pipe sourceOldSupplierUserSchedUUIDPipe, Pipe sourceOldSupplierBreakUUIDPipe,Pipe resellerCountPipe) {

		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ShortName,SupplierColumns.ActiveStates,SupplierColumns.UpdatingComments
				,SupplierColumns.UpdateRegularity,SupplierColumns.SupplierStability,SupplierColumns.QikPriceStartup,SupplierColumns.ShowStockInfo,SupplierColumns.ShowStockDate
				,SupplierColumns.WEBAddress,SupplierColumns.ApprovalTo,SupplierColumns.ApprovalMethod,SupplierColumns.ApprovalEmailAddress);
		
		Fields tempFields = fields.append(new Fields(SupplierColumns.SupplierID+Columns.Temp,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID));
		Pipe pipe = new CoGroup(Pipe.pipes(sourceSupplierInfoPipe, sourceOldSupplierUserSchedUUIDPipe), Fields.fields(new Fields(SupplierColumns.SupplierID), new Fields(SupplierColumns.SupplierID)), tempFields, new LeftJoin());
		
		tempFields = tempFields.append(new Fields(SupplierColumns.SupplierID+Columns.Temp+Columns.Temp,SupplierColumns.ResellerCount));
		pipe = new CoGroup(Pipe.pipes(pipe, resellerCountPipe), Fields.fields(new Fields(SupplierColumns.SupplierID), new Fields(SupplierColumns.SupplierID)),tempFields, new LeftJoin());
		fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ShortName,SupplierColumns.ActiveStates,SupplierColumns.WEBAddress,SupplierColumns.ApprovalTo,SupplierColumns.ApprovalEmailAddress,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID,SupplierColumns.ResellerCount);
		
		pipe = new Each(pipe,fields,new Identity(),Fields.RESULTS);
		pipe = new Each(pipe, new Fields(SupplierColumns.SupplierUserID), new SetUUIDFunction(new Fields(SupplierColumns.SupplierUserID)), Fields.REPLACE);
		pipe = new Each(pipe, new Fields(SupplierColumns.ScheduleUUID), new SetUUIDFunction(new Fields(SupplierColumns.ScheduleUUID)), Fields.REPLACE);
		
		Fields priceBreakFields = new Fields(SupplierColumns.BreakUUID,SupplierColumns.BreakID,SupplierColumns.SupplierID,SupplierColumns.Disabled,SupplierColumns.BreakName);
		Pipe supplierPriceBreakPipe = new CoGroup(Pipe.pipes(sourceSupplierPriceBreakPipe, sourceOldSupplierBreakUUIDPipe), Fields.fields(new Fields(SupplierColumns.BreakID), new Fields(SupplierColumns.BreakID)),priceBreakFields.subtract(new Fields(SupplierColumns.BreakUUID)).append(new Fields(SupplierColumns.BreakUUID,SupplierColumns.BreakID+Columns.Temp)), new LeftJoin());
		supplierPriceBreakPipe = new Each(supplierPriceBreakPipe,priceBreakFields,new Identity(),Fields.RESULTS);
		supplierPriceBreakPipe = new Each(supplierPriceBreakPipe, new Fields(SupplierColumns.BreakUUID), new SetUUIDFunction(new Fields(SupplierColumns.BreakUUID)), Fields.REPLACE);
		
		pipe = new CoGroup(Pipe.pipes(pipe,supplierPriceBreakPipe),Fields.fields(new Fields(SupplierColumns.SupplierID), new Fields(SupplierColumns.SupplierID)),fields.append(priceBreakFields.rename(new Fields(SupplierColumns.SupplierID), new Fields(SupplierColumns.SupplierID+Columns.Temp))),new LeftJoin());
		
		fields = fields.append(priceBreakFields.subtract(new Fields(SupplierColumns.SupplierID)));
		pipe = new Each(pipe,fields,new Identity(),Fields.RESULTS);
		
		if(sourceOldSupplierInfoPipe==null){
			return Pipe.pipes(pipe);
		}else{
			Pipe newPipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldSupplierInfoPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
	}
	
	private Pipe createSinkSupplierInfoPipe(String outputPath,Pipe[] tempGroupSupplierInfoPipes) {
		Pipe pipe = createSinkTempSupplierInfoPipe(tempGroupSupplierInfoPipes);
		pipe = new Pipe("SinkSupplierInfoPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkSupplierUserSchedUUIDPipe(String outputPath,Pipe[] tempGroupSupplierInfoPipes) {
		Pipe pipe = createSinkTempSupplierInfoPipe(tempGroupSupplierInfoPipes);
		pipe = new Each(pipe,new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID),new Identity(),Fields.RESULTS);
		pipe = new Unique(pipe,new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID));
		pipe = new Pipe("SinkSupplierUserSchedUUIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkSupplierBreakUUIDPipe(String outputPath,Pipe[] tempGroupSupplierInfoPipes) {
		Pipe pipe = createSinkTempSupplierInfoPipe(tempGroupSupplierInfoPipes);
		pipe = new Each(pipe,new Fields(SupplierColumns.BreakUUID,SupplierColumns.BreakID),new Identity(),Fields.RESULTS);
		pipe = new Unique(pipe,new Fields(SupplierColumns.BreakUUID,SupplierColumns.BreakID));
		pipe = new Each(pipe,new Fields(SupplierColumns.BreakUUID,SupplierColumns.BreakID),new FilterNull());
		
		pipe = new Pipe("SinkSupplierBreakUUIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkTempSupplierInfoPipe(Pipe[] tempGroupSupplierInfoPipes) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ShortName,SupplierColumns.ActiveStates,
				SupplierColumns.WEBAddress,SupplierColumns.ApprovalTo,SupplierColumns.ApprovalEmailAddress,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID,SupplierColumns.ResellerCount,
				SupplierColumns.BreakUUID,SupplierColumns.BreakID,SupplierColumns.Disabled,SupplierColumns.BreakName);
		
		Pipe pipe = null;
		if(tempGroupSupplierInfoPipes.length>1){
			pipe = new GroupBy(tempGroupSupplierInfoPipes, new Fields(SupplierColumns.SupplierID,SupplierColumns.BreakUUID), new Fields(Columns.SortNum));
			pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		}else{
			pipe = tempGroupSupplierInfoPipes[0];
		}
		return pipe;
	}
	
	private Pipe createSinkChangedSupplierInfoPipe(String outputPath, Pipe[] tempGroupSupplierInfoPipes,Pipe sinkSupplierBranchPipe) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ShortName,SupplierColumns.ActiveStates,
				SupplierColumns.WEBAddress,SupplierColumns.ApprovalTo,SupplierColumns.ApprovalEmailAddress,SupplierColumns.SupplierUserID,SupplierColumns.ScheduleUUID,SupplierColumns.ResellerCount,
				SupplierColumns.BreakUUID,SupplierColumns.BreakID,SupplierColumns.Disabled,SupplierColumns.BreakName);
		Pipe pipe = null;
		if(tempGroupSupplierInfoPipes.length>1){
			pipe = new GroupBy(tempGroupSupplierInfoPipes, new Fields(SupplierColumns.SupplierID,SupplierColumns.BreakUUID), new Fields(Columns.SortNum));
			pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		}else{
			pipe = tempGroupSupplierInfoPipes[0];
		}
		
		Fields supplierBranchFields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierBranchID,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax
				,SupplierColumns.Address1,SupplierColumns.Address2,SupplierColumns.Address3,SupplierColumns.City,SupplierColumns.State,SupplierColumns.Zip,SupplierColumns.SupplierBranchCountry,SupplierColumns.SupplierBranchCountryCode);
		
		Pipe supplierBranchPipe = new GroupBy(sinkSupplierBranchPipe,new Fields(SupplierColumns.SupplierID));
		supplierBranchPipe = new Every(supplierBranchPipe, supplierBranchFields, new First(), Fields.RESULTS);
		
		fields = fields.append(new Fields(SupplierColumns.SupplierID+Columns.Temp).append(supplierBranchFields.subtract(new Fields(SupplierColumns.SupplierID))));
		pipe = new CoGroup(Pipe.pipes(pipe, supplierBranchPipe), Fields.fields(new Fields(SupplierColumns.SupplierID), new Fields(SupplierColumns.SupplierID)),fields, new LeftJoin());
		
		fields = fields.subtract(new Fields(SupplierColumns.SupplierID+Columns.Temp,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax,SupplierColumns.SupplierBranchCountry));
		pipe = new Each(pipe,fields,new Identity(),Fields.RESULTS);
		
		pipe = new Each(pipe,new Fields(SupplierColumns.SupplierBranchCountryCode,SupplierColumns.State),new SearchAddressStateIDFunction(Fields.ARGS),Fields.REPLACE);
		
		// save to db code
		pipe = new GroupBy(pipe, fields.subtract(new Fields(SupplierColumns.BreakUUID,SupplierColumns.BreakID,SupplierColumns.Disabled,SupplierColumns.BreakName)));
		pipe = new Every(pipe, new SupplierStoreAggregator(fields), Fields.RESULTS);
		
		pipe = new Pipe("SinkChangedSupplierInfoPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceSupplierBranchPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax
				,SupplierColumns.Address1,SupplierColumns.Address2,SupplierColumns.Address3,SupplierColumns.City,SupplierColumns.State,SupplierColumns.Zip,SupplierColumns.SupplierBranchCountry,SupplierColumns.SupplierBranchCountryCode
				,SupplierColumns.PostalAddress1,SupplierColumns.PostalAddress2,SupplierColumns.PostalAddress3,SupplierColumns.PostalCity,SupplierColumns.PostalState,SupplierColumns.PostalZip
				,SupplierColumns.WarehouseAddress1,SupplierColumns.WarehouseAddress2,SupplierColumns.WarehouseAddress3,SupplierColumns.WarehouseCity,SupplierColumns.WarehouseState,SupplierColumns.WarehouseZip);
		
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true,",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceSupplierBranchPipe", new CSVSplitter(fields,",","\"",true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOldSupplierBranchUUIDPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName,SupplierColumns.SupplierBranchID);
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldSupplierBranchUUIDPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOldSupplierBranchPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierBranchID,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax
				,SupplierColumns.Address1,SupplierColumns.Address2,SupplierColumns.Address3,SupplierColumns.City,SupplierColumns.State,SupplierColumns.Zip,SupplierColumns.SupplierBranchCountry,SupplierColumns.SupplierBranchCountryCode
				,SupplierColumns.PostalAddress1,SupplierColumns.PostalAddress2,SupplierColumns.PostalAddress3,SupplierColumns.PostalCity,SupplierColumns.PostalState,SupplierColumns.PostalZip
				,SupplierColumns.WarehouseAddress1,SupplierColumns.WarehouseAddress2,SupplierColumns.WarehouseAddress3,SupplierColumns.WarehouseCity,SupplierColumns.WarehouseState,SupplierColumns.WarehouseZip);
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldSupplierBranchPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe[] createSinkTempGroupSupplierBranchPipes(Pipe sourceSupplierBranchPipe, Pipe sourceOldSupplierBranchPipe,Pipe sourceOldSupplierBranchUUIDPipe) {

		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierBranchID,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax
				,SupplierColumns.Address1,SupplierColumns.Address2,SupplierColumns.Address3,SupplierColumns.City,SupplierColumns.State,SupplierColumns.Zip,SupplierColumns.SupplierBranchCountry,SupplierColumns.SupplierBranchCountryCode
				,SupplierColumns.PostalAddress1,SupplierColumns.PostalAddress2,SupplierColumns.PostalAddress3,SupplierColumns.PostalCity,SupplierColumns.PostalState,SupplierColumns.PostalZip
				,SupplierColumns.WarehouseAddress1,SupplierColumns.WarehouseAddress2,SupplierColumns.WarehouseAddress3,SupplierColumns.WarehouseCity,SupplierColumns.WarehouseState,SupplierColumns.WarehouseZip);
		
		Pipe pipe = new CoGroup(Pipe.pipes(sourceSupplierBranchPipe, sourceOldSupplierBranchUUIDPipe), Fields.fields(new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName), new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName)), fields.subtract(new Fields(SupplierColumns.SupplierBranchID)).append(new Fields(SupplierColumns.SupplierID+Columns.Temp,SupplierColumns.BranchName+Columns.Temp,SupplierColumns.SupplierBranchID)), new LeftJoin());
		
		pipe = new Each(pipe,fields,new Identity(),Fields.RESULTS);
		pipe = new Each(pipe, new Fields(SupplierColumns.SupplierBranchID), new SetUUIDFunction(new Fields(SupplierColumns.SupplierBranchID)), Fields.REPLACE);
		
		if(sourceOldSupplierBranchPipe==null){
			return Pipe.pipes(pipe);
		}else{
			Pipe newPipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldSupplierBranchPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
	}
	
	private Pipe createSinkSupplierBranchPipe(String outputPath,Pipe[] tempGroupSupplierBranchPipes) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierBranchID,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax
				,SupplierColumns.Address1,SupplierColumns.Address2,SupplierColumns.Address3,SupplierColumns.City,SupplierColumns.State,SupplierColumns.Zip,SupplierColumns.SupplierBranchCountry,SupplierColumns.SupplierBranchCountryCode
				,SupplierColumns.PostalAddress1,SupplierColumns.PostalAddress2,SupplierColumns.PostalAddress3,SupplierColumns.PostalCity,SupplierColumns.PostalState,SupplierColumns.PostalZip
				,SupplierColumns.WarehouseAddress1,SupplierColumns.WarehouseAddress2,SupplierColumns.WarehouseAddress3,SupplierColumns.WarehouseCity,SupplierColumns.WarehouseState,SupplierColumns.WarehouseZip);
		
		Pipe pipe = null;
		if(tempGroupSupplierBranchPipes.length>1){
			pipe = new GroupBy(tempGroupSupplierBranchPipes, new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName), new Fields(Columns.SortNum));
			pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		}else{
			pipe = tempGroupSupplierBranchPipes[0];
		}
		
		pipe = new Pipe("SinkSupplierBranchPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkSupplierBranchUUIDPipe(String outputPath,Pipe[] tempGroupSupplierBranchPipes) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierBranchID,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax
				,SupplierColumns.Address1,SupplierColumns.Address2,SupplierColumns.Address3,SupplierColumns.City,SupplierColumns.State,SupplierColumns.Zip,SupplierColumns.SupplierBranchCountry,SupplierColumns.SupplierBranchCountryCode
				,SupplierColumns.PostalAddress1,SupplierColumns.PostalAddress2,SupplierColumns.PostalAddress3,SupplierColumns.PostalCity,SupplierColumns.PostalState,SupplierColumns.PostalZip
				,SupplierColumns.WarehouseAddress1,SupplierColumns.WarehouseAddress2,SupplierColumns.WarehouseAddress3,SupplierColumns.WarehouseCity,SupplierColumns.WarehouseState,SupplierColumns.WarehouseZip);
		
		Pipe pipe = null;
		if(tempGroupSupplierBranchPipes.length>1){
			pipe = new GroupBy(tempGroupSupplierBranchPipes, new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName), new Fields(Columns.SortNum));
			pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		}else{
			pipe = tempGroupSupplierBranchPipes[0];
		}
		pipe = new Each(pipe,new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName,SupplierColumns.SupplierBranchID),new Identity(),Fields.RESULTS);
		pipe = new Pipe("SinkSupplierBranchUUIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkChangedSupplierBranchPipe(String outputPath, Pipe[] tempGroupSupplierBranchPipes) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierBranchID,SupplierColumns.BranchName,SupplierColumns.Phone,SupplierColumns.Fax
				,SupplierColumns.Address1,SupplierColumns.Address2,SupplierColumns.Address3,SupplierColumns.City,SupplierColumns.State,SupplierColumns.Zip,SupplierColumns.SupplierBranchCountry,SupplierColumns.SupplierBranchCountryCode
				,SupplierColumns.PostalAddress1,SupplierColumns.PostalAddress2,SupplierColumns.PostalAddress3,SupplierColumns.PostalCity,SupplierColumns.PostalState,SupplierColumns.PostalZip
				,SupplierColumns.WarehouseAddress1,SupplierColumns.WarehouseAddress2,SupplierColumns.WarehouseAddress3,SupplierColumns.WarehouseCity,SupplierColumns.WarehouseState,SupplierColumns.WarehouseZip);
		
		Pipe pipe = null;
		if(tempGroupSupplierBranchPipes.length>1){
			pipe = new GroupBy(tempGroupSupplierBranchPipes, new Fields(SupplierColumns.SupplierID,SupplierColumns.BranchName), new Fields(Columns.SortNum));
			pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		}else{
			pipe = tempGroupSupplierBranchPipes[0];
		}
		pipe = new Each(pipe,new Fields(SupplierColumns.SupplierBranchCountryCode,SupplierColumns.State,SupplierColumns.PostalState,SupplierColumns.WarehouseState),new SearchAddressStateIDFunction(Fields.ARGS),Fields.REPLACE);
		// save to db code
		pipe = new Each(pipe, fields,new SupplierBranchStoreFunction(fields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedSupplierBranchPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceSupplierPriceBreakPipe(String inputPath) {
		//"BreakID","SupplierID","Disabled","BreakName"
		Fields fields = new Fields(SupplierColumns.BreakID,SupplierColumns.SupplierID,SupplierColumns.Disabled,SupplierColumns.BreakName);
		
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true,",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceSupplierPriceBreakPipe", new CSVSplitter(fields,",","\"",true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceResellerBreakPipe(String inputPath) {
		Fields resellerBreakFields = new Fields(ResellerColumns.ResellerID,SupplierColumns.SupplierID,SupplierColumns.BreakID,SupplierColumns.BreakName,ResellerColumns.SerialNumber,ResellerColumns.TransmissionType,
				ResellerColumns.CurrentCustomer,ResellerColumns.AccountNo,ResellerColumns.DateGranted);
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceResellerBreakPipe", new CSVSplitter(resellerBreakFields,",","\"",true), Fields.RESULTS);
//		pipe = new Each(pipe,new Debug());
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceResellerInfoPipe(String inputPath) {
		Fields fields = new Fields(ResellerColumns.SerialNumber,ResellerColumns.CreationDate,ResellerColumns.Version,ResellerColumns.Status,
				ResellerColumns.Type,ResellerColumns.Agency,ResellerColumns.SalesRep,ResellerColumns.CompanyName,ResellerColumns.LastName,ResellerColumns.FirstName,
				ResellerColumns.JobPosition,ResellerColumns.EmailAddress,ResellerColumns.Address1,ResellerColumns.Address2,ResellerColumns.City,ResellerColumns.State,
				ResellerColumns.Postcode,ResellerColumns.CountryCode,ResellerColumns.PhoneCountryCode,ResellerColumns.Phone,ResellerColumns.Mobile,ResellerColumns.BillingFirstName,
				ResellerColumns.BillingLastName,ResellerColumns.BillingPhone,ResellerColumns.BillingEmailAddress,ResellerColumns.BillingEmailAddressCC,ResellerColumns.BillingCurrency,ResellerColumns.BillingGST,
				ResellerColumns.BillingComments,ResellerColumns.ITQuoterRenewalDate,ResellerColumns.ITQuoterAnnualFee,ResellerColumns.ITQuoterFullAccessUsers,	ResellerColumns.ITQuoterCrmUsers,
				ResellerColumns.ITQUoterTempUsers,ResellerColumns.ITQuoterTempUserExpiry,ResellerColumns.EcomRenewalDate,ResellerColumns.EcomAnnualFee,ResellerColumns.EcomType,
				ResellerColumns.EcomMultiCatalogues,ResellerColumns.EcomExportCatalogues,ResellerColumns.EcomWebPortals,ResellerColumns.EPDRenewalDate,ResellerColumns.EPDAnnualFee,
				ResellerColumns.EPDITQuoterUsage,ResellerColumns.EPDEcomUsage,ResellerColumns.EPDAgency,ResellerColumns.LicenseImageFeeds,ResellerColumns.LicenseBidImporter,
				ResellerColumns.LicenseApiSupport,ResellerColumns.LicenseForeignCurrency,ResellerColumns.LicenseSilentUpgrades,ResellerColumns.LicenseServiceManager,
				ResellerColumns.LicenseWarehouseModule,ResellerColumns.ResellerUsername,ResellerColumns.ResellerPassword,ResellerColumns.NextReviewDate);
		
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceResellerInfoPipe", new CSVSplitter(fields,",","\"",true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createResellerBreakInfoPipe(Pipe sourceResellerBreakPipe,Pipe sinkSupplierInfoPipe,Pipe sourceResellerInfoPipe) {
		Fields tempSupplierFields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ApprovalEmailAddress,SupplierColumns.BreakID,SupplierColumns.BreakUUID,SupplierColumns.BreakName);
		Pipe pipe = new Each(sinkSupplierInfoPipe,tempSupplierFields,new Identity(),Fields.RESULTS);
		
		Fields tempResellerFields = new Fields(ResellerColumns.SerialNumber,ResellerColumns.CompanyName,ResellerColumns.CountryCode,ResellerColumns.BillingEmailAddress,ResellerColumns.BillingPhone,ResellerColumns.Agency);
		Pipe resellerPipe = new Each(sourceResellerInfoPipe,tempResellerFields,new Identity(),Fields.RESULTS);
		Fields resellerBreakFields = new Fields(ResellerColumns.ResellerID,SupplierColumns.BreakID,ResellerColumns.SerialNumber,ResellerColumns.TransmissionType,ResellerColumns.CurrentCustomer,ResellerColumns.AccountNo,ResellerColumns.DateGranted);
		Pipe resellerBreakPipe = new Each(sourceResellerBreakPipe,resellerBreakFields,new Identity(),Fields.RESULTS);
		
		Fields tempFields = resellerBreakFields.append(tempSupplierFields.rename(new Fields(SupplierColumns.BreakID), new Fields(SupplierColumns.BreakID+Columns.Temp)));
		pipe = new CoGroup(Pipe.pipes(resellerBreakPipe,pipe), Fields.fields(new Fields(SupplierColumns.BreakID), new Fields(SupplierColumns.BreakID)),tempFields,new InnerJoin());
		tempFields = tempFields.append(tempResellerFields.rename(new Fields(ResellerColumns.SerialNumber), new Fields(ResellerColumns.SerialNumber+Columns.Temp)));
		pipe = new CoGroup(Pipe.pipes(pipe,resellerPipe), Fields.fields(new Fields(ResellerColumns.SerialNumber), new Fields(ResellerColumns.SerialNumber)),tempFields,new InnerJoin());
		resellerBreakFields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ApprovalEmailAddress,SupplierColumns.BreakName,SupplierColumns.BreakUUID,
				ResellerColumns.SerialNumber,ResellerColumns.CompanyName,ResellerColumns.CountryCode,ResellerColumns.BillingEmailAddress,ResellerColumns.BillingPhone,ResellerColumns.Agency,
				ResellerColumns.TransmissionType,ResellerColumns.CurrentCustomer,ResellerColumns.AccountNo,ResellerColumns.DateGranted);
		
		pipe = new Each(pipe,resellerBreakFields,new Identity(),Fields.RESULTS);
		
		return pipe;
	}
	
	private Pipe createSourceOldResellerBreakPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ApprovalEmailAddress,SupplierColumns.BreakName,SupplierColumns.BreakUUID,
				ResellerColumns.SerialNumber,ResellerColumns.CompanyName,ResellerColumns.CountryCode,ResellerColumns.BillingEmailAddress,ResellerColumns.BillingPhone,ResellerColumns.Agency,
				ResellerColumns.TransmissionType,ResellerColumns.CurrentCustomer,ResellerColumns.AccountNo,ResellerColumns.DateGranted);
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldResellerBreakPipe", new RegexSplitter(fields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		
		return pipe;
	}
	
	private Pipe[] createSinkTempGroupResellerBreakPipes(Pipe resellerBreakInfo, Pipe sourceOldResellerBreakPipe) {
		if(sourceOldResellerBreakPipe==null){
			return Pipe.pipes(resellerBreakInfo);
		}else{
			Pipe newPipe = new Each(resellerBreakInfo, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldResellerBreakPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
	}
	
	private Pipe createSinkResellerBreakPipe(String outputPath,Pipe[] tempGroupResellerBreakPipes) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ApprovalEmailAddress,SupplierColumns.BreakName,SupplierColumns.BreakUUID,
				ResellerColumns.SerialNumber,ResellerColumns.CompanyName,ResellerColumns.CountryCode,ResellerColumns.BillingEmailAddress,ResellerColumns.BillingPhone,ResellerColumns.Agency,
				ResellerColumns.TransmissionType,ResellerColumns.CurrentCustomer,ResellerColumns.AccountNo,ResellerColumns.DateGranted);
		Pipe pipe = null;
		if(tempGroupResellerBreakPipes.length>1){
			pipe = new GroupBy(tempGroupResellerBreakPipes, new Fields(SupplierColumns.BreakUUID,ResellerColumns.SerialNumber), new Fields(Columns.SortNum));
			pipe = new Every(pipe, fields, new MergeAggregator(fields, false), Fields.RESULTS);
		}else{
			pipe = tempGroupResellerBreakPipes[0];
		}
		pipe = new Pipe("SinkResellerBreakPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createSinkChangedResellerBreakPipe(String outputPath, Pipe[] tempGroupResellerBreakPipes) {
		Fields fields = new Fields(SupplierColumns.SupplierID,SupplierColumns.SupplierName,SupplierColumns.ApprovalEmailAddress,SupplierColumns.BreakName,SupplierColumns.BreakUUID,
				ResellerColumns.SerialNumber,ResellerColumns.CompanyName,ResellerColumns.CountryCode,ResellerColumns.BillingEmailAddress,ResellerColumns.BillingPhone,ResellerColumns.Agency,
				ResellerColumns.TransmissionType,ResellerColumns.CurrentCustomer,ResellerColumns.AccountNo,ResellerColumns.DateGranted);
		Pipe pipe = null;
		if(tempGroupResellerBreakPipes.length>1){
			pipe = new GroupBy(tempGroupResellerBreakPipes, new Fields(SupplierColumns.BreakUUID,ResellerColumns.SerialNumber), new Fields(Columns.SortNum));
			pipe = new Every(pipe, new CheckChangedAggregator(fields), Fields.RESULTS);
		}else{
			pipe = tempGroupResellerBreakPipes[0];
		}
		// save to db code
		pipe = new Each(pipe, fields,new ResellerBreakStoreFunction(fields), Fields.RESULTS);
		
		pipe = new Pipe("SinkChangedResellerBreakPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createResellerBreakMapCountBySupplierPipe(Pipe sourceResellerBreakPipe) {
		Pipe pipe = new Each(sourceResellerBreakPipe,new Fields(ResellerColumns.TransmissionType),new NotEqualsFilter("4"));
//		pipe = new Each(pipe,new Debug());
		pipe = new CountBy(pipe,new Fields(SupplierColumns.SupplierID),new Fields(SupplierColumns.ResellerCount));
		
		return pipe;
	}
}
