package au.com.isell.rlm.importing.flow;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.mapred.JobConf;

import au.com.isell.common.util.IDServerClient;
import au.com.isell.rlm.importing.constant.Columns;
import au.com.isell.rlm.importing.constant.invoices.InvoicesColumns;
import au.com.isell.rlm.importing.constant.invoices.InvoicesFilePath;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.constant.reseller.ResellerFilePath;
import au.com.isell.rlm.importing.constant.supplier.SupplierColumns;
import au.com.isell.rlm.importing.constant.supplier.SupplierFilePath;
import au.com.isell.rlm.importing.function.common.CSVSplitter;
import au.com.isell.rlm.importing.function.maxid.FormatIDFunction;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowListener;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.aggregator.Max;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntryIterator;

public class SetMaxIDFlow extends BaseFlow {
	public SetMaxIDFlow(FlowConnector flowConnector, String commonInputPath, String commonOutputPath, String commonOldDataPath, String commonOldUUIDPath,String onlyUpdateChange) {
		super(flowConnector, commonInputPath, commonOutputPath, commonOldDataPath,commonOldUUIDPath,onlyUpdateChange);
	}

	@Override
	public Flow createFlow() {

		Pipe sourceResellerInfoPipe = createFormatEmptyStringToNullPipe(createSourceResellerInfoPipe(commonInputPath + ResellerFilePath.SOURCE_RESELLER_FILE));
		Pipe sinkMaxResellerIDPipe = createSinkMaxResellerIDPipe(commonOutputPath + ResellerFilePath.ALL_RESELLER_MAX_ID_PATH, sourceResellerInfoPipe);

		Pipe sourceSupplierInfoPipe = createFormatEmptyStringToNullPipe(createSourceSupplierInfoPipe(commonInputPath + SupplierFilePath.SOURCE_SUPPLIER_FILE));
		Pipe sinkMaxSupplierIDPipe = createSinkMaxSupplierIDPipe(commonOutputPath + SupplierFilePath.ALL_SUPPLIER_MAX_ID_PATH, sourceSupplierInfoPipe);

		Pipe sourceInvPipe = createFormatEmptyStringToNullPipe(createSourceInvPipe(commonInputPath + InvoicesFilePath.SOURCE_INV_FILE));
		Pipe sinkMaxInvIDPipe = createSinkMaxInvIDPipe(commonOutputPath + InvoicesFilePath.ALL_INV_MAX_ID_PATH, sourceInvPipe);

		Flow flow = flowConnector.connect(sourceTaps, sinkTaps,
									createFormatNullPipe(sinkMaxResellerIDPipe),
									createFormatNullPipe(sinkMaxSupplierIDPipe),
									createFormatNullPipe(sinkMaxInvIDPipe));
		flow.addListener(new FlowListener() {

			@Override
			public boolean onThrowable(Flow arg0, Throwable arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			@Override
			public void onStopping(Flow arg0) {
			}

			@Override
			public void onStarting(Flow arg0) {
			}

			@Override
			public void onCompleted(Flow flow) {
				try {
					Map<String, Tap> sinks = flow.getSinks();
					String line = null;
					String[] values = null;
					for (String key : sinks.keySet()) {
						if ("SinkMaxResellerIDPipe".equals(key)) {
							TupleEntryIterator ts = sinks.get(key).openForRead(new JobConf());
							while (ts.hasNext()) {
								line = ts.next().getString("line");
								if (StringUtils.isNotBlank(line)) {
									values = line.split("\t");
									IDServerClient.setIntId("reseller_" + values[0], Integer.parseInt(values[1]));
								}

							}

						} else if ("SinkMaxSupplierIDPipe".equals(key)) {
							TupleEntryIterator ts = sinks.get(key).openForRead(new JobConf());
							while (ts.hasNext()) {
								line = ts.next().getString("line");
								if (StringUtils.isNotBlank(line)) {
									values = line.split("\t");
									IDServerClient.setIntId("supplier_" + values[0], Integer.parseInt(values[1]));
								}
							}

						} else if ("SinkMaxInvIDPipe".equals(key)) {
							TupleEntryIterator ts = sinks.get(key).openForRead(new JobConf());
							while (ts.hasNext()) {
								line = ts.next().getString("line");
								if (StringUtils.isNotBlank(line)) {
									IDServerClient.setIntId("invoice", Integer.parseInt(line));
								}
							}

						}
					}
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		});
		return flow;

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
		Pipe pipe = new Each("SourceResellerInfoPipe", new CSVSplitter(fields, ",", "\"", true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkMaxResellerIDPipe(String outputPath, Pipe sourceResellerInfoPipe) {
		Pipe pipe = new Each(sourceResellerInfoPipe, new Fields(ResellerColumns.SerialNumber,ResellerColumns.CountryCode), new Identity(), Fields.RESULTS);
		pipe = new Each(pipe,new Fields(ResellerColumns.SerialNumber,ResellerColumns.CountryCode),new FormatIDFunction(Fields.ARGS,"Reseller"),Fields.REPLACE);
		
		pipe = new GroupBy(pipe, new Fields(ResellerColumns.CountryCode));
		pipe = new Every(pipe, new Fields(ResellerColumns.SerialNumber), new Max(new Fields(ResellerColumns.SerialNumber)), Fields.ALL);
//		pipe = new Each(pipe,new Debug(true));
		pipe = new Pipe("SinkMaxResellerIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSourceSupplierInfoPipe(String inputPath) {
		Fields fields = new Fields(SupplierColumns.SupplierID, SupplierColumns.SupplierName, SupplierColumns.ShortName, SupplierColumns.ActiveStates, SupplierColumns.UpdatingComments, SupplierColumns.UpdateRegularity, SupplierColumns.SupplierStability, SupplierColumns.QikPriceStartup, SupplierColumns.ShowStockInfo, SupplierColumns.ShowStockDate, SupplierColumns.WEBAddress, SupplierColumns.ApprovalTo, SupplierColumns.ApprovalMethod, SupplierColumns.ApprovalEmailAddress);

		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceSupplierInfoPipe", new CSVSplitter(fields, ",", "\"", true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkMaxSupplierIDPipe(String outputPath, Pipe sourceSupplierInfoPipe) {
		Pipe pipe = new Each(sourceSupplierInfoPipe, new Fields(SupplierColumns.SupplierID), new Identity(), Fields.RESULTS);
		pipe = new Each(pipe, new Insert(new Fields(SupplierColumns.CountryCode), 0), Fields.ALL);
		pipe = new Each(pipe,new Fields(SupplierColumns.SupplierID,SupplierColumns.CountryCode),new FormatIDFunction(Fields.ARGS,"Supplier"),Fields.REPLACE);
		pipe = new GroupBy(pipe, new Fields(SupplierColumns.CountryCode));
		pipe = new Every(pipe, new Fields(SupplierColumns.SupplierID), new Max(new Fields(SupplierColumns.SupplierID)), Fields.ALL);
		pipe = new Pipe("SinkMaxSupplierIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
	
	private Pipe createSourceInvPipe(String inputPath) {
		Fields fields = new Fields(InvoicesColumns.ResellerSerialNumber, InvoicesColumns.ResellerCompanyName, InvoicesColumns.InvoiceNumber, InvoicesColumns.Status, InvoicesColumns.CreatedDate, 
				InvoicesColumns.InvoiceDate, InvoicesColumns.FollowupDate,InvoicesColumns.FullyPaidDate, InvoicesColumns.PaymentTerms, InvoicesColumns.InvoiceReportID, InvoicesColumns.AgentID, InvoicesColumns.SalesRepID, 
				InvoicesColumns.ToFirstName, InvoicesColumns.ToLastName, InvoicesColumns.ToPhoneNumber,InvoicesColumns.ToMobileNumber, InvoicesColumns.ToEmailAddress, InvoicesColumns.ToEmailAddressCC,
				InvoicesColumns.AmountITQuoter, InvoicesColumns.AmountEcommerce, InvoicesColumns.AmountEPD,	InvoicesColumns.AmountServices,InvoicesColumns.AmountOther, InvoicesColumns.TotalAmount,
				InvoicesColumns.TotalAgentCommissions, InvoicesColumns.TotalPayments, InvoicesColumns.CurrencyName, InvoicesColumns.GstRate, InvoicesColumns.GstName, InvoicesColumns.InternalNotes,
				InvoicesColumns.InvoiceDetails, InvoicesColumns.InvoiceTerms,InvoicesColumns.NewBusiness);
		
		TextLine scheme = new TextDelimited(new Fields(Columns.Line), true, ",");// skipHeader
		Tap tap = new Hfs(scheme, inputPath);
		Pipe pipe = new Each("SourceInvPipe", new CSVSplitter(fields, ",", "\"", true), Fields.RESULTS);
		sourceTaps.put(pipe.getName(), tap);
		return pipe;
	}

	private Pipe createSinkMaxInvIDPipe(String outputPath, Pipe sourceInvPipe) {
		Pipe pipe = new Each(sourceInvPipe, new Fields(InvoicesColumns.InvoiceNumber), new Identity(), Fields.RESULTS);
		pipe = new Each(pipe, new Insert(new Fields(Columns.Temp), 0), Fields.ALL);
		pipe = new GroupBy(pipe, new Fields(Columns.Temp));
		pipe = new Every(pipe, new Fields(InvoicesColumns.InvoiceNumber), new Max(new Fields(InvoicesColumns.InvoiceNumber)), Fields.RESULTS);
		pipe = new Pipe("SinkMaxInvIDPipe", pipe);
		Tap tap = new Hfs(new TextLine(), outputPath, SinkMode.REPLACE);
		sinkTaps.put(pipe.getName(), tap);
		return pipe;
	}
}
