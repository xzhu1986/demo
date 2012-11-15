package au.com.isell.rlm.user.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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

import au.com.isell.common.storage.NotFoundException;
import au.com.isell.common.storage.StorageException;
import au.com.isell.common.util.SecurityUtils;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.domain.ResetPasswordRequest;
import au.com.isell.rlm.module.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:applicationContext.xml" })
// @Transactional
// @TransactionConfiguration(transactionManager = "transactionManager",
// defaultRollback = true)
public class TestUserDAO {
	// private static UserDAO dao;
	@Autowired
	private UserDAO dao;
	@Autowired
	private ResellerDao resellerDao;
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
	public void testOpHistory(){
		OperationHistory history=dao.getHistory(UUID.fromString("00a9e28a-b5cd-428b-9495-15fd24e421dd"));
		System.out.println(history);
		System.err.println(history.getTarget().getClass());
	}
	
	@Test
	public final void testGetUserById() {
		// test normal get
		try {
			UUID uid = UUID.fromString("17405813-01b4-46b4-b8bf-1dcf5468187f");
			User user = dao.getUserById(uid);
			assertEquals("userId", uid, user.getUserId());
			assertEquals("username", "johnsmith", user.getUsername());
			assertEquals("password", SecurityUtils.digestPassword(user.getUsername(), "12345"), user.getPassword());
			assertEquals("firstName", "John", user.getFirstName());
			assertEquals("lastName", "Smith", user.getLastName());
			assertEquals("email", "john.smith@isell.com.au", user.getEmail());
			assertEquals("status", UserStatus.ACTIVE, user.getStatus());
			assertEquals("loginFailureCount", 1, user.getLoginFailureCount());
		} catch (StorageException e) {
			fail(e.getMessage());
		}
		// test not found
		try {
			UUID uid = UUID.fromString("c28de507-96e0-4dd3-a2dd-23906b61ff86");
			User user = dao.getUserById(uid);
			if (user != null)
				fail("user " + uid + " exists");
		} catch (StorageException e) {
			fail(e.getMessage());
		}
		// test storage error
		if (testDBError)
			try {
				UUID uid = UUID.fromString("60f037e3-db49-47b9-8a63-dbacd6bbccca");
				dao.getUserById(uid);
				fail("user " + uid + " get success");
			} catch (StorageException e) {

			}
	}

	@Test
	public final void testGetUserByUserName() {
		// test normal get
		try {
			String username = "johnsmith";
			User user = dao.getUserByUserName(username);
			assertEquals("userId", UUID.fromString("17405813-01b4-46b4-b8bf-1dcf5468187f"), user.getUserId());
			assertEquals("type", 2, user.getType());
			assertEquals("username", "johnsmith", user.getUsername());
			assertEquals("password", SecurityUtils.digestPassword(user.getUsername(), "12345"), user.getPassword());
			assertEquals("firstName", "John", user.getFirstName());
			assertEquals("lastName", "Smith", user.getLastName());
			assertEquals("email", "john.smith@isell.com.au", user.getEmail());
			assertEquals("status", 1, user.getStatus());
			assertEquals("loginFailureCount", 1, user.getLoginFailureCount());
		} catch (StorageException e) {
			fail("ErrorMsg: " + e.getMessage());
		}
		// test not found
		try {
			String username = "johnsmith.notexist";
			User user = dao.getUserByUserName(username);
			assertNull("user " + username + " exists", user);
		} catch (StorageException e) {
			fail("Should raise NotFoundException but actual different error: " + e.getMessage());
		}
		// test storage error
		if (testDBError)
			try {
				String username = "johnsmith.loaderr";
				dao.getUserByUserName(username);
				fail("user " + username + " get success");
			} catch (StorageException e) {

			}
	}

	@Test
	public final void testGenerateResetPasswordHash() {
		// test normal get
		try {
			UUID userId = UUID.fromString("c28de507-96e0-4dd3-a2dd-23906b61ff87");
			String hash = dao.generateResetPasswordHash(userId, "test@isell.com.au");
			ResetPasswordRequest req = dao.getResetpasswordRequest(hash);
			assertEquals("Test generate reset password request: ", userId, req.getUserId());
			assertEquals("Test generate reset password request: ", hash, req.getHash());
			assertEquals("Test generate reset password request: ", System.currentTimeMillis() / 10000 + 7 * 24 * 60 * 6, req.getExpireDate()
					.getTime() / 10000);
		} catch (NotFoundException e) {
			fail("Test generate reset password request: " + e.getMessage());
		} catch (StorageException e) {
			fail("Test generate reset password request: " + e.getMessage());
		}
		// test not found
		try {
			UUID userId = UUID.fromString("c28de507-96e0-4dd3-a2dd-23906b61ff86");
			String hash = dao.generateResetPasswordHash(userId, "test@isell.com.au");
			fail("Test generate reset password request: " + userId + " generate success");
		} catch (NotFoundException e) {

		} catch (StorageException e) {
			fail("Test generate reset password request: Should raise NotFoundException but actual different error: " + e.getMessage());
		}
		// test storage error
		if (testDBError) {
			try {
				UUID userId = UUID.fromString("60f037e3-db49-47b9-8a63-dbacd6bbccca");
				String hash = dao.generateResetPasswordHash(userId, "test@isell.com.au");
				fail("Test generate reset password request: " + userId + " generate success");
			} catch (NotFoundException e) {
				fail("Test generate reset password request: Should raise StorageException but actual different error: " + e.getMessage());
			} catch (StorageException e) {

			}
			try {
				UUID userId = UUID.fromString("17405813-01b4-46b4-b8bf-1dcf5468187f");
				String hash = dao.generateResetPasswordHash(userId, "test@isell.com.au");
				dao.getResetpasswordRequest(hash);
				fail("Test get request: user " + hash + " get success");
			} catch (NotFoundException e) {
				fail("Test get request: Should raise StorageException but actual different error: " + e.getMessage());
			} catch (StorageException e) {

			}
		}
	}

	@Test
	public final void testDeleteUserById() {
		// test normal get
		try {
			UUID uid = UUID.fromString("17405813-01b4-46b4-b8bf-1dcf5468187f");
			dao.deleteUserById(uid);
		} catch (StorageException e) {
			fail(e.getMessage());
		}
		// test storage error
		if (testDBError)
			try {
				UUID uid = UUID.fromString("60f037e3-db49-47b9-8a63-dbacd6bbccca");
				dao.deleteUserById(uid);
				fail("user " + uid + " get success");
			} catch (StorageException e) {

			}
	}

}
