package au.com.isell.rlm.module.report.constant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.Assert;

import au.com.isell.common.aws.S3Manager;

/**
 * @author frankw 10/05/2012
 */
public enum ReportType {
	Invoice {
		@Override
		protected void init() {
			tplNameMap.put(ReportFormat.PDF, "agent/${agentId}/invoice.doc");
			tplInfoCloudKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/invoice.doc.info");
			renderFileCloudKeyKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/${invoiceNumber}/invoice.pdf");
			outputNameMap.put(ReportFormat.PDF, "Invoice - Reseller.pdf");
		}

	},

	InvoiceDirectDebitBank {
		@Override
		protected void init() {
			tplNameMap.put(ReportFormat.PDF, "agent/${agentId}/invoiceDirectDebitBank.doc");
			tplInfoCloudKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/invoiceDirectDebitBank.doc.info");
			renderFileCloudKeyKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/${invoiceNumber}/invoiceDirectDebitBank.pdf");
			outputNameMap.put(ReportFormat.PDF, "Direct Debit - Bank.pdf");
		}

	},

	InvoiceDirectDebitCreditCard {
		@Override
		protected void init() {
			tplNameMap.put(ReportFormat.PDF, "agent/${agentId}/invoiceDirectDebitCreditCard.doc");
			tplInfoCloudKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/invoiceDirectDebitCreditCard.doc.info");
			renderFileCloudKeyKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/${invoiceNumber}/invoiceDirectDebitCreditCard.pdf");
			outputNameMap.put(ReportFormat.PDF, "Direct Debit - Credit Card.pdf");
		}

	},

	InvoiceForcast12Month {
		@Override
		protected void init() {
			tplNameMap.put(ReportFormat.PDF, "agent/${agentId}/statistics/invoiceForcast12Month.doc");
			tplInfoCloudKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/statistics/invoiceForcast12Month.doc.info");
			renderFileCloudKeyKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/statistics/invoiceForcast12Month.pdf");
			outputNameMap.put(ReportFormat.PDF, "Report - 12 Month Forecast.pdf");
			outputNameMap.put(ReportFormat.CSV, "Report - 12 Month Forecast.csv");
		}

	},

	InvoiceAgentInvoiceSummary {
		@Override
		protected void init() {
			tplNameMap.put(ReportFormat.PDF, "agent/${agentId}/statistics/invoiceAgentInvoiceSummary.doc");
			tplInfoCloudKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/statistics/invoiceAgentInvoiceSummary.doc.info");
			renderFileCloudKeyKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/statistics/invoiceAgentInvoiceSummary.pdf");
			outputNameMap.put(ReportFormat.PDF, "Report - Invoice Summary.pdf");
			outputNameMap.put(ReportFormat.CSV, "Report - Invoice Summary.csv");
		}

	},

	InvoicePaymentsDueSixWeeks {
		@Override
		protected void init() {
			tplNameMap.put(ReportFormat.PDF, "agent/${agentId}/statistics/invoiceInvoicePaymentsDueSixWeeks.doc");
			tplInfoCloudKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/statistics/invoiceInvoicePaymentsDueSixWeeks.doc.info");
			renderFileCloudKeyKeyMap.put(ReportFormat.PDF, "data/report/agent/${agentId}/statistics/invoiceInvoicePaymentsDueSixWeeks.pdf");
			outputNameMap.put(ReportFormat.PDF, "Report - Invoice Payments Due.pdf");
			outputNameMap.put(ReportFormat.CSV, "Report - Invoice Payments Due.csv");
		}

	};

	protected Map<ReportFormat, String> tplNameMap = new HashMap<ReportFormat, String>();
	protected Map<ReportFormat, String> tplInfoCloudKeyMap = new HashMap<ReportFormat, String>();
	protected Map<ReportFormat, String> renderFileCloudKeyKeyMap = new HashMap<ReportFormat, String>();
	protected Map<ReportFormat, String> outputNameMap = new HashMap<ReportFormat, String>();

	private ReportType() {
		tplNameMap = new HashMap<ReportFormat, String>();
		tplInfoCloudKeyMap = new HashMap<ReportFormat, String>();
		renderFileCloudKeyKeyMap = new HashMap<ReportFormat, String>();
		tplInfoCloudKeyMap = new HashMap<ReportFormat, String>();
		init();
	}

	protected abstract void init();

	private static String tplNamePrefix = null;
	static {
		try {
			Properties bundle = new Properties();
			bundle.load(S3Manager.class.getResourceAsStream("/settings.properties"));
			tplNamePrefix = bundle.getProperty("report.template.prefix");
		} catch (IOException ex) {
			throw new RuntimeException("Load config file failed", ex);
		}
	}

	public String getTplName(ReportFormat format) {
		return tplNamePrefix + "/" + tplNameMap.get(format);
	}

	public String getTplInfoCloudKey(ReportFormat format) {
		return tplInfoCloudKeyMap.get(format);
	}

	public String getRenderFileCloudKey(ReportFormat format) {
		return renderFileCloudKeyKeyMap.get(format);
	}

	public String getOutputName(ReportFormat format) {
		String otputName = outputNameMap.get(format);
		Assert.hasText(otputName, format + " file has not set output name");
		return otputName;
	}

}