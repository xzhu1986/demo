package au.com.isell.rlm.reseller;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.com.isell.rlm.module.reseller.service.LicenseService;
import au.com.isell.rlm.module.reseller.service.ResellerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:applicationContext.xml" })
// @Transactional
// @TransactionConfiguration(transactionManager = "transactionManager",
// defaultRollback = true)
public class TestResellerService {
	// private static UserDAO dao;
	@Autowired
	private ResellerService resellerService;

	@Autowired
	private LicenseService licenseService;
	private static final boolean testDBError = true;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// dao = new MockUserDAO();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public final void testResellerManualPaths() {
		resellerService.getResellerManualFiles("standard/");
	}

}
