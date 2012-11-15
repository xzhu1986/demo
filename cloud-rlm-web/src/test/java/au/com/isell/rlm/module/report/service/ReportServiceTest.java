package au.com.isell.rlm.module.report.service;

import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.com.isell.rlm.module.report.constant.ReportFormat;
import au.com.isell.rlm.module.report.constant.ReportPath;
import au.com.isell.rlm.module.report.constant.ReportType;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:applicationContext.xml" })
// @Transactional
// @TransactionConfiguration(transactionManager = "transactionManager",defaultRollback = true)
public class ReportServiceTest {
	@Autowired
	private ReportService reportService;

	@Test
	public void testUploadTpl() throws IOException {
		InputStream in=new FileInputStream("C:/Users/frankw.ALS/Desktop/Invoice Template - Schedules.doc");
		ReportPath reportPath=new ReportPath(ReportType.Invoice,ReportFormat.PDF).addParam("agentId", "ae7b1620-7740-11e1-b0c4-0800200c9a66");
		reportService.uploadTpl(in, reportPath.getTplName());
	}

	@Test
	public void testDownloadTpl() {
		fail("Not yet implemented");
	}

	@Test
	public void testRenderTpl() {
		
	}

}
