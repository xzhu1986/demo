package au.com.isell.rlm.module.invoice.vo;

import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.module.report.constant.ReportType;

public enum InvoiceStatisticReportType {
	@EnumMsgCode("invoice.report.statistic.invoice_payments_due_six_weeks")
	INVOICE_PAYMENTS_DUE_SIX_WEEKS {
		@Override
		public ReportType getReportPathType() {
			return ReportType.InvoicePaymentsDueSixWeeks;
		}
	},
	@EnumMsgCode("invoice.report.statistic.agent_invoice_summary")
	AGENT_INVOICE_SUMMARY {
		@Override
		public ReportType getReportPathType() {
			return ReportType.InvoiceAgentInvoiceSummary;
		}
	},
	@EnumMsgCode("invoice.report.statistic.forcast_12_month")
	FORCAST_12_MONTH {
		@Override
		public ReportType getReportPathType() {
			return ReportType.InvoiceForcast12Month;
		}
	};
	
	public abstract ReportType getReportPathType();
}
