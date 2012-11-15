package au.com.isell.rlm.importing.flow;

import au.com.isell.rlm.importing.aggregator.common.CheckChangedAggregator;
import au.com.isell.rlm.importing.aggregator.common.MergeAggregator;
import au.com.isell.rlm.importing.constant.Columns;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.constant.reseller.ResellerFilePath;
import au.com.isell.rlm.importing.filter.EqualsFilter;
import au.com.isell.rlm.importing.function.common.CSVSplitter;
import au.com.isell.rlm.importing.function.common.SearchAddressStateIDFunction;
import au.com.isell.rlm.importing.function.common.SetUUIDFunction;
import au.com.isell.rlm.importing.function.reseller.ResellerStoreFunction;
import au.com.isell.rlm.importing.function.reseller.ResellerUserStoreFunction;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowListener;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.CountBy;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

public class ResellerFlow extends BaseFlow {
	private Fields fields = new Fields(ResellerColumns.SerialNumber,ResellerColumns.CreationDate,ResellerColumns.Version,ResellerColumns.Status,
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

	private Fields resellerUserFields = new Fields(ResellerColumns.ResellerUserOldID,ResellerColumns.ResellerUserWebUserName,ResellerColumns.ResellerUserAccountId,ResellerColumns.ResellerUserContactId,
			ResellerColumns.ResellerUserAccountNumber,ResellerColumns.ResellerUserFirstName,ResellerColumns.ResellerUserLastName,ResellerColumns.ResellerUserJobTitle,ResellerColumns.ResellerUserEmail,ResellerColumns.ResellerUserPassword,ResellerColumns.ResellerUserisPrimary);

	public ResellerFlow(FlowConnector flowConnector, String commonInputPath, String commonOutputPath, String commonOldDataPath,String commonOldUUIDPath,String onlyUpdateChange) {
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath,commonOldUUIDPath,onlyUpdateChange);
	}

	@Override
	public Flow createFlow() {

		Pipe sourceResellerInfoPipe = createFormatEmptyStringToNullPipe(createSourceResellerInfoPipe(commonInputPath + ResellerFilePath.SOURCE_RESELLER_FILE));
		Pipe sourceResellerBreakPipe = createFormatEmptyStringToNullPipe(createSourceResellerBreakPipe(commonInputPath + ResellerFilePath.SOURCE_RESELLER_BREAK_FILE));
		Pipe resellerBreakMapCountPipe = createResellerBreakMapCountPipe(sourceResellerBreakPipe);
		Pipe sourceOldResellerInfoPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldResellerInfoPipe = createFormatEmptyStringToNullPipe(createSourceOldResellerInfoPipe(commonOldDataPath + ResellerFilePath.OLD_RESELLER_PATH));
		}
		Pipe sourceOldPrimaryResellerUserUUIDPipe = createFormatEmptyStringToNullPipe(createSourceOldPrimaryResellerUserUUIDPipe(commonOldUUIDPath + ResellerFilePath.OLD_PRIMARY_RESELLER_USER_UUID_PATH));
		Pipe[] tempGroupResellerInfoPipes = createSinkTempGroupResellerInfoPipes(sourceResellerInfoPipe,sourceOldResellerInfoPipe,resellerBreakMapCountPipe,sourceOldPrimaryResellerUserUUIDPipe);
		Pipe sinkResellerInfoPipe = createSinkResellerInfoPipe(commonOutputPath + ResellerFilePath.ALL_RESELLER_PATH,tempGroupResellerInfoPipes);
		Pipe sinkPrimaryResellerUserUUIDPipe = createSinkPrimaryResellerUserUUIDPipe(commonOutputPath + ResellerFilePath.UUID_PRIMARY_RESELLER_USER_PATH,tempGroupResellerInfoPipes);
		Pipe sinkChangedResellerInfoPipe = createSinkChangedResellerInfoPipe(commonOutputPath + ResellerFilePath.CHANGED_RESELLER_PATH,tempGroupResellerInfoPipes);
		
		Pipe sourceResellerUserPipe = createFormatEmptyStringToNullPipe(createSourceResellerUserPipe(commonInputPath + ResellerFilePath.SOURCE_RESELLER_USER_FILE));
		Pipe sourceOldResellerUserPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldResellerUserPipe = createFormatEmptyStringToNullPipe(createSourceOldResellerUserPipe(commonOldDataPath + ResellerFilePath.OLD_RESELLER_USER_PATH));
		}
		Pipe sourceOldResellerUserUUIDPipe = createFormatEmptyStringToNullPipe(createSourceOldResellerUserUUIDPipe(commonOldUUIDPath + ResellerFilePath.OLD_RESELLER_USER_UUID_PATH));
		Pipe[] tempGroupResellerUserPipes = createSinkTempGroupResellerUserPipes(sourceResellerUserPipe,sourceOldResellerUserPipe,sourceOldResellerUserUUIDPipe);
		
		Pipe sinkResellerUserPipe = createSinkResellerUserPipe(commonOutputPath + ResellerFilePath.ALL_RESELLER_USER_PATH,tempGroupResellerUserPipes);
		Pipe sinkResellerUserUUIDPipe = createSinkResellerUserUUIDPipe(commonOutputPath + ResellerFilePath.UUID_RESELLER_USER_PATH,tempGroupResellerUserPipes);
		Pipe sinkChangedResellerUserPipe = createSinkChangedResellerUserPipe(commonOutputPath + ResellerFilePath.CHANGED_RESELLER_USER_PATH,tempGroupResellerUserPipes);
		
		
		Flow flow = flowConnector.connect(sourceTaps, sinkTaps
				, createFormatNullPipe(sinkResellerInfoPipe)
				, createFormatNullPipe(sinkChangedResellerInfoPipe)
				, createFormatNullPipe(sinkPrimaryResellerUserUUIDPipe)
				, createFormatNullPipe(sinkResellerUserPipe)
				, createFormatNullPipe(sinkResellerUserUUIDPipe)
				, createFormatNullPipe(sinkChangedResellerUserPipe));
		
		flow.addListener(new FlowListener() {
			@Override
			public boolean onThrowable(Flow arg0, Throwable arg1) {
				return false;
			}
			@Override
			public void onStopping(Flow arg0) {}
			
			@Override
			public void onStarting(Flow arg0) {
				createDir(commonOldDataPath + ResellerFilePath.OLD_RESELLER_PATH);
				createDir(commonOldUUIDPath + ResellerFilePath.OLD_PRIMARY_RESELLER_USER_UUID_PATH);
				createDir(commonOldDataPath + ResellerFilePath.OLD_RESELLER_USER_PATH);
				createDir(commonOldUUIDPath + ResellerFilePath.OLD_RESELLER_USER_UUID_PATH);
			}
			
			@Override
			public void onCompleted(Flow arg0) {
				// move file
				copyFile(commonOutputPath + ResellerFilePath.ALL_RESELLER_PATH,commonOldDataPath + ResellerFilePath.OLD_RESELLER_PATH);
				copyFile(commonOutputPath + ResellerFilePath.UUID_PRIMARY_RESELLER_USER_PATH,commonOldUUIDPath + ResellerFilePath.OLD_PRIMARY_RESELLER_USER_UUID_PATH);
				copyFile(commonOutputPath + ResellerFilePath.ALL_RESELLER_USER_PATH,commonOldDataPath + ResellerFilePath.OLD_RESELLER_USER_PATH);
				copyFile(commonOutputPath + ResellerFilePath.UUID_RESELLER_USER_PATH,commonOldUUIDPath + ResellerFilePath.OLD_RESELLER_USER_UUID_PATH);
			}
		});
		return flow;

	}
	
	private Pipe createSourceResellerUserPipe(String inputPath) {
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceResellerUserPipe", new CSVSplitter(resellerUserFields,",","\"",true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOldResellerUserPipe(String inputPath) {
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldResellerUserPipe", new RegexSplitter(resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID))), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceOldResellerUserUUIDPipe(String inputPath) {
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldResellerUserUUIDPipe", new RegexSplitter(new Fields(ResellerColumns.ResellerUserWebUserName,ResellerColumns.ResellerUserID)), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe[] createSinkTempGroupResellerUserPipes(Pipe sourceResellerUserPipe, Pipe sourceOldResellerUserPipe,Pipe resellerUserUUIDPipe) {
		Pipe pipe = new CoGroup(Pipe.pipes(sourceResellerUserPipe, resellerUserUUIDPipe), Fields.fields(new Fields(ResellerColumns.ResellerUserWebUserName), new Fields(ResellerColumns.ResellerUserWebUserName)),
				resellerUserFields.append(new Fields(Columns.Temp+ResellerColumns.ResellerUserWebUserName,ResellerColumns.ResellerUserID)), new LeftJoin());
		
		pipe = new Each(pipe,resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID)),new Identity(),Fields.RESULTS);
		pipe = new Each(pipe, new Fields(ResellerColumns.ResellerUserID), new SetUUIDFunction(new Fields(ResellerColumns.ResellerUserID)), Fields.REPLACE);
		
		if(sourceOldResellerUserPipe==null){
			return Pipe.pipes(pipe);
		}else{
			Pipe newPipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldResellerUserPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
		
	}
	
	private Pipe createSinkResellerUserPipe(String outputPath,Pipe[] tempGroupResellerUserPipes) {
		Pipe pipe = null;
		if(tempGroupResellerUserPipes.length>1){
			pipe = new GroupBy(tempGroupResellerUserPipes, new Fields(ResellerColumns.ResellerUserWebUserName), new Fields(Columns.SortNum));
			pipe = new Every(pipe,resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID)), new MergeAggregator(resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID)), false), Fields.RESULTS);
		}else{
			pipe = tempGroupResellerUserPipes[0];
		}
		pipe = new Pipe("SinkResellerUserPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkChangedResellerUserPipe(String outputPath, Pipe[] tempGroupResellerUserPipes) {
		Pipe pipe = null;
		if(tempGroupResellerUserPipes.length>1){
			pipe = new GroupBy(tempGroupResellerUserPipes, new Fields(ResellerColumns.ResellerUserWebUserName), new Fields(Columns.SortNum));
			pipe = new Every(pipe, new CheckChangedAggregator(resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID))), Fields.RESULTS);
		}else{
			pipe = tempGroupResellerUserPipes[0];
		}
		// save to db code
		pipe = new Each(pipe, resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID)),new ResellerUserStoreFunction(resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID))), Fields.RESULTS);
		 
		pipe = new Pipe("SinkChangedResellerUserPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkResellerUserUUIDPipe(String outputPath,Pipe[] tempGroupResellerUserPipes) {
		Pipe pipe = null;
		if(tempGroupResellerUserPipes.length>1){
			pipe = new GroupBy(tempGroupResellerUserPipes, new Fields(ResellerColumns.ResellerUserWebUserName), new Fields(Columns.SortNum));
			pipe = new Every(pipe,resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID)), new MergeAggregator(resellerUserFields.append(new Fields(ResellerColumns.ResellerUserID)), false), Fields.RESULTS);
		}else{
			pipe = tempGroupResellerUserPipes[0];
		}
		pipe = new Each(pipe,new Fields(ResellerColumns.ResellerUserWebUserName,ResellerColumns.ResellerUserID),new Identity(),Fields.RESULTS);
		pipe = new Pipe("SinkResellerUserUUIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		
		return pipe;
	}

	private Pipe createSourceResellerInfoPipe(String inputPath) {
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceResellerInfoPipe", new CSVSplitter(fields,",","\"",true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createSourceOldResellerInfoPipe(String inputPath) {
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldResellerInfoPipe", new RegexSplitter(fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount))), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createSourceOldPrimaryResellerUserUUIDPipe(String inputPath) {
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldPrimaryResellerUserUUIDPipe", new RegexSplitter(new Fields(ResellerColumns.SerialNumber,ResellerColumns.ResellerUserID)), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe[] createSinkTempGroupResellerInfoPipes(Pipe sourceResellerInfoPipe, Pipe sourceOldResellerInfoPipe,Pipe resellerBreakMapCountPipe,Pipe resellerUserUUIDPipe) {
		Fields tempFields = fields.append(new Fields(ResellerColumns.SerialNumber+Columns.Temp,ResellerColumns.ResellerUserID));
		Pipe pipe = new CoGroup(Pipe.pipes(sourceResellerInfoPipe, resellerUserUUIDPipe), Fields.fields(new Fields(ResellerColumns.SerialNumber), new Fields(ResellerColumns.SerialNumber)),tempFields, new LeftJoin());
		tempFields = tempFields.append(new Fields(ResellerColumns.SerialNumber+Columns.Temp+Columns.Temp,ResellerColumns.ResellerBreakMapCount));
		pipe = new CoGroup(Pipe.pipes(pipe, resellerBreakMapCountPipe), Fields.fields(new Fields(ResellerColumns.SerialNumber), new Fields(ResellerColumns.SerialNumber)),tempFields, new LeftJoin());
		
		pipe = new Each(pipe,fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount)),new Identity(),Fields.RESULTS);
		pipe = new Each(pipe, new Fields(ResellerColumns.ResellerUserID), new SetUUIDFunction(new Fields(ResellerColumns.ResellerUserID)), Fields.REPLACE);
		
		if(sourceOldResellerInfoPipe==null){
			return Pipe.pipes(pipe);
		}else{
			Pipe newPipe = new Each(pipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldResellerInfoPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
		
	}
	
	private Pipe createSinkResellerInfoPipe(String outputPath,Pipe[] tempGroupResellerInfoPipes) {
		Pipe pipe = createSinkTempResellerInfoPipe(tempGroupResellerInfoPipes);
		pipe = new Pipe("SinkResellerInfoPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSinkPrimaryResellerUserUUIDPipe(String outputPath,Pipe[] tempGroupResellerInfoPipes) {
		Pipe pipe = createSinkTempResellerInfoPipe(tempGroupResellerInfoPipes);
		pipe = new Each(pipe,new Fields(ResellerColumns.SerialNumber,ResellerColumns.ResellerUserID),new Identity(),Fields.RESULTS);
		pipe = new Pipe("SinkPrimaryResellerUserIdPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		
		return pipe;
	}

	private Pipe createSinkTempResellerInfoPipe(Pipe[] tempGroupResellerInfoPipes) {
		Pipe pipe = null;
		if(tempGroupResellerInfoPipes.length>1){
			pipe = new GroupBy(tempGroupResellerInfoPipes, new Fields(ResellerColumns.SerialNumber), new Fields(Columns.SortNum));
			pipe = new Every(pipe, fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount)), new MergeAggregator(fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount)), false), Fields.RESULTS);
		}else{
			pipe = tempGroupResellerInfoPipes[0];
		}
		return pipe;
	}
	
	private Pipe createSinkChangedResellerInfoPipe(String outputPath, Pipe[] tempGroupResellerInfoPipes) {
		Pipe pipe = null;
		if(tempGroupResellerInfoPipes.length>1){
			pipe = new GroupBy(tempGroupResellerInfoPipes, new Fields(ResellerColumns.SerialNumber), new Fields(Columns.SortNum));
			pipe = new Every(pipe, new CheckChangedAggregator(fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount))), Fields.RESULTS);
		}else{
			pipe = tempGroupResellerInfoPipes[0];
		}
		pipe = new Each(pipe,fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount)),new Identity(),Fields.RESULTS);
		pipe = new Each(pipe,new Fields(ResellerColumns.CountryCode,ResellerColumns.State),new SearchAddressStateIDFunction(Fields.ARGS),Fields.REPLACE);
		// save to db code
		pipe = new Each(pipe, fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount)),new ResellerStoreFunction(fields.append(new Fields(ResellerColumns.ResellerUserID,ResellerColumns.ResellerBreakMapCount))), Fields.RESULTS);
		 
		pipe = new Pipe("SinkChangedResellerInfoPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceResellerBreakPipe(String inputPath) {
		Fields resellerBreakFields = new Fields(ResellerColumns.ResellerID,ResellerColumns.SupplierID,ResellerColumns.BreakID,ResellerColumns.BreakName,ResellerColumns.SerialNumber,ResellerColumns.TransmissionType,
				ResellerColumns.CurrentCustomer,ResellerColumns.AccountNo,ResellerColumns.DateGranted);
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceResellerBreakPipe", new CSVSplitter(resellerBreakFields,",","\"",true), Fields.RESULTS);
//		pipe = new Each(pipe,new Debug());
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}
	private Pipe createResellerBreakMapCountPipe(Pipe sourceResellerBreakPipe) {
		Pipe pipe = new Each(sourceResellerBreakPipe,new Fields(ResellerColumns.TransmissionType),new EqualsFilter("4"));
//		pipe = new Each(pipe,new Debug());
		pipe = new CountBy(pipe,new Fields(ResellerColumns.SerialNumber),new Fields(ResellerColumns.ResellerBreakMapCount));
		
		return pipe;
	}
}
