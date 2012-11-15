package au.com.isell.rlm.importing.flow;

import au.com.isell.rlm.importing.aggregator.common.CheckChangedAggregator;
import au.com.isell.rlm.importing.aggregator.common.MergeAggregator;
import au.com.isell.rlm.importing.constant.Columns;
import au.com.isell.rlm.importing.constant.invoices.InvoicesColumns;
import au.com.isell.rlm.importing.constant.invoices.InvoicesFilePath;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.constant.reseller.ResellerFilePath;
import au.com.isell.rlm.importing.filter.EqualsFilter;
import au.com.isell.rlm.importing.function.common.CSVSplitter;
import au.com.isell.rlm.importing.function.common.SearchAddressStateIDFunction;
import au.com.isell.rlm.importing.function.invoices.InvoicesFollowupStoreFunction;
import au.com.isell.rlm.importing.function.invoices.InvoicesSchedStoreFunction;
import au.com.isell.rlm.importing.function.invoices.InvoicesStoreFunction;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowListener;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.filter.FilterNull;
import cascading.operation.filter.Not;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.SumBy;
import cascading.pipe.cogroup.LeftJoin;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;


public class InvoicesFlow extends BaseFlow {
	private Fields invoicesFields = new Fields(InvoicesColumns.ResellerSerialNumber, InvoicesColumns.ResellerCompanyName, InvoicesColumns.InvoiceNumber, InvoicesColumns.Status, InvoicesColumns.CreatedDate, 
						InvoicesColumns.InvoiceDate, InvoicesColumns.FollowupDate,InvoicesColumns.FullyPaidDate, InvoicesColumns.PaymentTerms, InvoicesColumns.InvoiceReportID, InvoicesColumns.AgentID, InvoicesColumns.SalesRepID, 
						InvoicesColumns.ToFirstName, InvoicesColumns.ToLastName, InvoicesColumns.ToPhoneNumber,InvoicesColumns.ToMobileNumber, InvoicesColumns.ToEmailAddress, InvoicesColumns.ToEmailAddressCC,
						InvoicesColumns.AmountITQuoter, InvoicesColumns.AmountEcommerce, InvoicesColumns.AmountEPD,	InvoicesColumns.AmountServices,InvoicesColumns.AmountOther, InvoicesColumns.TotalAmount,
						InvoicesColumns.TotalAgentCommissions, InvoicesColumns.TotalPayments, InvoicesColumns.CurrencyName, InvoicesColumns.GstRate, InvoicesColumns.GstName, InvoicesColumns.InternalNotes,
						InvoicesColumns.InvoiceDetails, InvoicesColumns.InvoiceTerms,InvoicesColumns.NewBusiness);

	private Fields invoicesFollowFields = new Fields(InvoicesColumns.InvoiceNumber, InvoicesColumns.UserID, InvoicesColumns.Followup, 
			InvoicesColumns.Results, InvoicesColumns.AddtionalNotes,InvoicesColumns.EmailSubject, InvoicesColumns.EmailBody);
	
	private Fields invoicesSchedFields = new Fields(InvoicesColumns.InvoiceNumber, InvoicesColumns.ScheduledAmount, InvoicesColumns.DueDate, 
			InvoicesColumns.PaymentReceived, InvoicesColumns.PaymentReceivedDate,InvoicesColumns.PaymentType, InvoicesColumns.PaymentReference);
	
	public InvoicesFlow(FlowConnector flowConnector, String commonInputPath, String commonOutputPath, String commonOldDataPath,String commonOldUUIDPath,String onlyUpdateChange) {
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath,commonOldUUIDPath,onlyUpdateChange);
	}

	@Override
	public Flow createFlow() {

		Pipe sourceInvFollowPipe = createFormatEmptyStringToNullPipe(createSourceInvFollowPipe(commonInputPath + InvoicesFilePath.SOURCE_INV_FOLLOW_FILE));
		Pipe sourceOldInvFollowPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldInvFollowPipe = createFormatEmptyStringToNullPipe(createSourceOldInvFollowPipe(commonOldDataPath + InvoicesFilePath.OLD_INV_FOLLOW_PATH));
		}		
		Pipe[] tempGroupInvFollowPipes = createSinkTempGroupInvFollowPipes(sourceInvFollowPipe, sourceOldInvFollowPipe);
		Pipe sinkInvFollowPipe = createSinkInvFollowPipe(commonOutputPath + InvoicesFilePath.ALL_INV_FOLLOW_PATH, tempGroupInvFollowPipes);
		Pipe sinkChangedInvFollowPipe = createSinkChangedInvFollowPipe(commonOutputPath + InvoicesFilePath.CHANGED_INV_FOLLOW_PATH, tempGroupInvFollowPipes);

		Pipe sourceInvSchedPipe = createFormatEmptyStringToNullPipe(createSourceInvSchedPipe(commonInputPath + InvoicesFilePath.SOURCE_INV_SCHED_FILE));
		Pipe sourceOldInvSchedPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldInvSchedPipe = createFormatEmptyStringToNullPipe(createSourceOldInvSchedPipe(commonOldDataPath + InvoicesFilePath.OLD_INV_SCHED_PATH));
		}
		Pipe[] tempGroupInvSchedPipes = createSinkTempGroupInvSchedPipes(sourceInvSchedPipe, sourceOldInvSchedPipe);
		Pipe sinkInvSchedPipe = createSinkInvSchedPipe(commonOutputPath + InvoicesFilePath.ALL_INV_SCHED_PATH, tempGroupInvSchedPipes);
		Pipe sinkChangedInvSchedPipe = createSinkChangedInvSchedPipe(commonOutputPath + InvoicesFilePath.CHANGED_INV_SCHED_PATH, tempGroupInvSchedPipes);

		Pipe sourceResellerInfoPipe = createFormatEmptyStringToNullPipe(createSourceResellerInfoPipe(commonInputPath + ResellerFilePath.SOURCE_RESELLER_FILE));
		Pipe sourceInvInfoPipe = createFormatEmptyStringToNullPipe(createSourceInvInfoPipe(commonInputPath + InvoicesFilePath.SOURCE_INV_FILE));
		Pipe sourceOldInvInfoPipe = null;
		if(onlyUpdateChange=="true"){
			sourceOldInvInfoPipe = createFormatEmptyStringToNullPipe(createSourceOldInvInfoPipe(commonOldDataPath + InvoicesFilePath.OLD_INV_PATH));
		}
		Pipe invoicesSchedTotalPaidPipe = createInvoicesSchedTotalPaidPipe(sourceInvSchedPipe);
		Pipe[] tempGroupInvInfoPipes = createSinkTempGroupInvInfoPipes(sourceInvInfoPipe,invoicesSchedTotalPaidPipe,sourceOldInvInfoPipe);
		Pipe sinkInvInfoPipe = createSinkInvInfoPipe(commonOutputPath + InvoicesFilePath.ALL_INV_PATH, tempGroupInvInfoPipes);
		Pipe sinkChangedInvInfoPipe = createSinkChangedInvInfoPipe(commonOutputPath + InvoicesFilePath.CHANGED_INV_PATH, tempGroupInvInfoPipes,sourceResellerInfoPipe);

		Flow flow = flowConnector.connect(sourceTaps, sinkTaps, 
						createFormatNullPipe(sinkInvInfoPipe), 
						createFormatNullPipe(sinkChangedInvInfoPipe),
						createFormatNullPipe(sinkInvFollowPipe), 
						createFormatNullPipe(sinkChangedInvFollowPipe),
						createFormatNullPipe(sinkInvSchedPipe), 
						createFormatNullPipe(sinkChangedInvSchedPipe));

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
				createDir(commonOldDataPath + InvoicesFilePath.OLD_INV_FOLLOW_PATH);
				createDir(commonOldDataPath + InvoicesFilePath.OLD_INV_PATH);
				createDir(commonOldDataPath + InvoicesFilePath.OLD_INV_SCHED_PATH);
			}

			@Override
			public void onCompleted(Flow arg0) {
				// move file
				copyFile(commonOutputPath + InvoicesFilePath.ALL_INV_PATH, commonOldDataPath + InvoicesFilePath.OLD_INV_PATH);
				copyFile(commonOutputPath + InvoicesFilePath.ALL_INV_FOLLOW_PATH, commonOldDataPath + InvoicesFilePath.OLD_INV_FOLLOW_PATH);
				copyFile(commonOutputPath + InvoicesFilePath.ALL_INV_SCHED_PATH, commonOldDataPath + InvoicesFilePath.OLD_INV_SCHED_PATH);
			}
		});
		return flow;

	}

	private Pipe createSourceInvInfoPipe(String inputPath) {
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceInvInfoPipe", new CSVSplitter(invoicesFields, ",", "\"", true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldInvInfoPipe(String inputPath) {
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldInvInfoPipe", new RegexSplitter(invoicesFields.append(new Fields(InvoicesColumns.TotalPaid))), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createInvoicesSchedTotalPaidPipe(Pipe sourceInvSchedPipe){
		Pipe pipe = new Each(sourceInvSchedPipe,new Fields(InvoicesColumns.PaymentReceived),new Not(new EqualsFilter("1")));
		pipe = new SumBy(pipe,new Fields(InvoicesColumns.InvoiceNumber),new Fields(InvoicesColumns.ScheduledAmount),new Fields(InvoicesColumns.TotalPaid),Double.class);
		return pipe;
	}
	private Pipe[] createSinkTempGroupInvInfoPipes(Pipe sourceInvInfoPipe,Pipe invoicesSchedTotalPaidPipe, Pipe sourceOldInvInfoPipe) {
		Pipe newPipe = new CoGroup(Pipe.pipes(sourceInvInfoPipe,invoicesSchedTotalPaidPipe),Fields.fields(new Fields(InvoicesColumns.InvoiceNumber),new Fields(InvoicesColumns.InvoiceNumber)),invoicesFields.append(new Fields(InvoicesColumns.InvoiceNumber+Columns.Temp,InvoicesColumns.TotalPaid)),new LeftJoin());
		newPipe = new Each(newPipe,invoicesFields.append(new Fields(InvoicesColumns.TotalPaid)),new Identity());
		if(sourceOldInvInfoPipe==null){
			return Pipe.pipes(newPipe);
		}else{
			newPipe = new Each(newPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldInvInfoPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
	}
	private Pipe createSinkInvInfoPipe(String outputPath,Pipe[] tempGroupInvInfoPipes) {
		Pipe pipe = null;
		if(tempGroupInvInfoPipes.length>1){
			pipe = new GroupBy(tempGroupInvInfoPipes, new Fields(InvoicesColumns.InvoiceNumber), new Fields(Columns.SortNum));
			pipe = new Every(pipe, invoicesFields.append(new Fields(InvoicesColumns.TotalPaid)), new MergeAggregator(Fields.ARGS), Fields.RESULTS);
		}else{
			pipe = tempGroupInvInfoPipes[0];
		}
		pipe = new Pipe("SinkInvInfoPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkChangedInvInfoPipe(String outputPath, Pipe[] tempGroupInvInfoPipes,Pipe sourceResellerInfoPipe) {
		Pipe pipe = null;
		if(tempGroupInvInfoPipes.length>1){
			pipe = new GroupBy(tempGroupInvInfoPipes,new Fields(InvoicesColumns.InvoiceNumber), new Fields(Columns.SortNum));
			pipe = new Every(pipe,new CheckChangedAggregator(invoicesFields.append(new Fields(InvoicesColumns.TotalPaid))), Fields.RESULTS);
		}else{
			pipe = tempGroupInvInfoPipes[0];
		}
		//resellerId region
		Fields resellerFields = new Fields(ResellerColumns.SerialNumber,ResellerColumns.Address1,ResellerColumns.Address2,ResellerColumns.City,ResellerColumns.State,ResellerColumns.Postcode,ResellerColumns.CountryCode,ResellerColumns.PhoneCountryCode);
		Pipe resellerPipe = new Each(sourceResellerInfoPipe,resellerFields,new Identity());
		resellerPipe = new Each(resellerPipe,new Fields(ResellerColumns.CountryCode,ResellerColumns.State),new SearchAddressStateIDFunction(Fields.ARGS),Fields.REPLACE);
		pipe = new CoGroup(Pipe.pipes(pipe,resellerPipe),Fields.fields(new Fields(InvoicesColumns.ResellerSerialNumber),new Fields(ResellerColumns.SerialNumber)),invoicesFields.append(new Fields(InvoicesColumns.TotalPaid)).append(resellerFields),new LeftJoin());
		// save to db code
		pipe = new Each(pipe,invoicesFields.append(new Fields(InvoicesColumns.TotalPaid)).append(resellerFields.subtract(new Fields(ResellerColumns.SerialNumber))),new InvoicesStoreFunction(invoicesFields.append(new Fields(InvoicesColumns.TotalPaid)).append(resellerFields.subtract(new Fields(ResellerColumns.SerialNumber)))), Fields.RESULTS);
		pipe = new Pipe("SinkChangedInvInfoPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	
	private Pipe createSourceInvFollowPipe(String inputPath) {
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceInvFollowPipe", new CSVSplitter(invoicesFollowFields, ",", "\"", true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldInvFollowPipe(String inputPath) {
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldInvFollowPipe", new RegexSplitter(invoicesFollowFields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe[] createSinkTempGroupInvFollowPipes(Pipe sourceInvFollowPipe, Pipe sourceOldInvFollowPipe) {
		Pipe newPipe = new Each(sourceInvFollowPipe,new Fields(InvoicesColumns.Followup),new FilterNull());
		if(sourceOldInvFollowPipe==null){
			return Pipe.pipes(newPipe);
		}else{
			newPipe = new Each(sourceInvFollowPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldInvFollowPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
	}

	private Pipe createSinkInvFollowPipe(String outputPath, Pipe[] tempGroupInvFollowPipes) {
		Pipe pipe = null;
		if(tempGroupInvFollowPipes.length>1){
			pipe = new GroupBy(tempGroupInvFollowPipes, new Fields(InvoicesColumns.InvoiceNumber,InvoicesColumns.Followup), new Fields(Columns.SortNum));
			pipe = new Every(pipe, invoicesFollowFields, new MergeAggregator(invoicesFollowFields), Fields.RESULTS);
		}else{
			pipe = tempGroupInvFollowPipes[0];
		}
		pipe = new Pipe("SinkInvFollowPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkChangedInvFollowPipe(String outputPath, Pipe[] tempGroupInvFollowPipes) {
		Pipe pipe = null;
		if(tempGroupInvFollowPipes.length>1){
			pipe = new GroupBy(tempGroupInvFollowPipes, new Fields(InvoicesColumns.InvoiceNumber,InvoicesColumns.Followup), new Fields(Columns.SortNum));
			pipe = new Every(pipe, new CheckChangedAggregator(invoicesFollowFields), Fields.RESULTS);
		}else{
			pipe = tempGroupInvFollowPipes[0];
		}
		// save to db code
		pipe = new Each(pipe,invoicesFollowFields, new InvoicesFollowupStoreFunction(invoicesFollowFields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedInvFollowPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	
	private Pipe createSourceInvSchedPipe(String inputPath) {
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceInvSchedPipe", new CSVSplitter(invoicesSchedFields, ",", "\"", true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceOldInvSchedPipe(String inputPath) {
		TextLine scheme = new TextLine(new Fields(Columns.Line));
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceOldInvSchedPipe", new RegexSplitter(invoicesSchedFields), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe[] createSinkTempGroupInvSchedPipes(Pipe sourceInvSchedPipe, Pipe sourceOldInvSchedPipe) {
		Pipe newPipe = new Each(sourceInvSchedPipe,new Fields(InvoicesColumns.DueDate),new FilterNull());
		if(sourceOldInvSchedPipe==null){
			return Pipe.pipes(newPipe);
		}else{
			newPipe = new Each(newPipe, new Insert(new Fields(Columns.SortNum), 1), Fields.ALL);
			Pipe oldPipe = new Each(sourceOldInvSchedPipe, new Insert(new Fields(Columns.SortNum), 0), Fields.ALL);// old
			return Pipe.pipes(oldPipe, newPipe);
		}
	}

	private Pipe createSinkInvSchedPipe(String outputPath, Pipe[] tempGroupInvSchedPipes) {
		Pipe pipe = null;
		if(tempGroupInvSchedPipes.length>1){
			pipe = new GroupBy(tempGroupInvSchedPipes, new Fields(InvoicesColumns.InvoiceNumber,InvoicesColumns.DueDate), new Fields(Columns.SortNum));
			pipe = new Every(pipe, invoicesSchedFields, new MergeAggregator(invoicesSchedFields), Fields.RESULTS);
		}else{
			pipe = tempGroupInvSchedPipes[0];
		}
		pipe = new Pipe("SinkInvSchedPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkChangedInvSchedPipe(String outputPath, Pipe[] tempGroupInvSchedPipes) {
		Pipe pipe = null;
		if(tempGroupInvSchedPipes.length>1){
			pipe = new GroupBy(tempGroupInvSchedPipes, new Fields(InvoicesColumns.InvoiceNumber,InvoicesColumns.DueDate), new Fields(Columns.SortNum));
			pipe = new Every(pipe, new CheckChangedAggregator(invoicesSchedFields), Fields.RESULTS);
		}else{
			pipe = tempGroupInvSchedPipes[0];
		}
		// save to db code
		pipe = new Each(pipe,invoicesSchedFields, new InvoicesSchedStoreFunction(invoicesSchedFields), Fields.RESULTS);
		pipe = new Pipe("SinkChangedInvSchedPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
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
}
