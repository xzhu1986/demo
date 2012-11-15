package au.com.isell.rlm.module.invoice.service;

import java.io.File;
import java.io.IOException;


public interface InvoiceReportService {

	File generateCSVReportInvoicePaymentsDueSixWeeks();
	
	File generateCSVReportInvoiceForcast12Month();

	String generatePDFReportInvoiceForcast12Month();

	String generatePDFReportInvoicePaymentsDueSixWeeks();
	
	public String generatePdfReportData4AgentInvoiceSummary();
	
	public File generateCsvReport4AgentInvoiceSummary() ;
}
