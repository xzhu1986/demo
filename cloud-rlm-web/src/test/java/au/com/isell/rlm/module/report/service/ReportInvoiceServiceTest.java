package au.com.isell.rlm.module.report.service;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.com.isell.rlm.module.invoice.service.InvoiceReportService;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:applicationContext.xml" })
// @Transactional
// @TransactionConfiguration(transactionManager = "transactionManager",defaultRollback = true)
public class ReportInvoiceServiceTest {
	@Autowired
	private InvoiceReportService invoiceReportService;

	public void testInvoiceForcast12MonthReport() throws IOException {
		invoiceReportService.generateCSVReportInvoiceForcast12Month();
		System.out.println(invoiceReportService.generatePDFReportInvoiceForcast12Month());
	}

	@Test
	public void testInvoicePaymentsDueSixWeeksReport() throws IOException {
		invoiceReportService.generateCSVReportInvoicePaymentsDueSixWeeks();
		System.out.println(invoiceReportService.generatePDFReportInvoicePaymentsDueSixWeeks());
	}

}
