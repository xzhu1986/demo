package au.com.isell.rlm.module.invoice.service.impl;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.invoice.service.InvoiceReportService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:applicationContext.xml" })
// @Transactional
// @TransactionConfiguration(transactionManager = "transactionManager",defaultRollback = true)
public class InvoiceReportServiceImplTest extends DAOSupport{
	@Autowired
	InvoiceReportService reportService;
	
	@Test
	public void testGeneratePdfReportData4AgentInvoiceSummary() {
		System.out.println(reportService.generatePdfReportData4AgentInvoiceSummary());
	}

	@Test
	public void testGenerateCsvReport4AgentInvoiceSummary() {
		File f=reportService.generateCsvReport4AgentInvoiceSummary();
		System.out.println(f.getAbsolutePath());
	}

}
