package au.com.isell.rlm.user.dao;

import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:applicationContext.xml" })
// @Transactional
// @TransactionConfiguration(transactionManager = "transactionManager",
// defaultRollback = true)
public class TestDaoSupport {
	// private static UserDAO dao;
	@Autowired
	private UserDAO dao;
	@Autowired
	private AddressDao addressDao;

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
	public final void testUser() {
		UUID userid= UUID.randomUUID();
		User user=new AgentUser();
		user.setUserId(userid);
		user.setUsername("testuser");
		user.setPassword("12345");
		user.setEmail("wj@12.com");
		dao.saveUser(user);
		System.out.println(dao.getUserById(userid));
	}

}
