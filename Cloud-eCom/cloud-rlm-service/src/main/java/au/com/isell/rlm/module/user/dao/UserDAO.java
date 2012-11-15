package au.com.isell.rlm.module.user.dao;

import java.util.List;
import java.util.UUID;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.storage.DuplicateDataException;
import au.com.isell.common.storage.NotFoundException;
import au.com.isell.common.storage.StorageException;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.user.domain.LoginHistory;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.domain.ResetPasswordRequest;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.vo.AuthInfoCache;

public interface UserDAO {
	/**
	 * 
	 * @param userId
	 * @return null user id will return null
	 */
	User getUserById(UUID userId) throws StorageException;

	/**
	 * Get User object by given username. To verify user login needs to get user
	 * by this method and compare hashed password(SHA-256) in User object which
	 * can tell the different of wrong username or wrong password.
	 * 
	 * @param username
	 * @return
	 */
	User getUserByUserName(String username) throws StorageException;

	/**
	 * Save an user info into database. It will check if the userId exist or not
	 * to select insert or update operation. It also need to check if the
	 * username is unique.
	 * 
	 * @param user
	 * @return the user Id. It will return a generated id if it's a new user
	 *         otherwise the existing id will be returned.
	 */
	UUID saveUser(User user) throws DuplicateDataException, StorageException;

	/**
	 * Create a reset password request and return the hash string which will
	 * attached in the reset password email as part of the reset password url.
	 * 
	 * @param userId
	 * @param email
	 * @return generated hash string for identify the reset password request
	 */
	String generateResetPasswordHash(UUID userId, String email) throws NotFoundException, StorageException;

	/**
	 * Get reset password request by hash string.
	 * 
	 * @param hash
	 * @return reset password request
	 */
	ResetPasswordRequest getResetpasswordRequest(String hash) throws StorageException;

	void deleteUserById(UUID userId) throws StorageException;

	void saveLoginHistory(LoginHistory history) throws StorageException;

	void delete(ResetPasswordRequest request);

	<T extends User> Pair<Long, List<T>> queryUser(Class<T> cls, FilterItem filterItem, Integer pageSize, Integer pageNo);

	FilterMaker getAgentUserMaker();

	FilterMaker getUserMaker();

	AuthInfoCache getAuthInfoCache();

	void checkDuplicateUsername(User user);

	FilterMaker getOptHistoryMaker();

	List<OperationHistory> queryOperationHistory(FilterItem filterItem);

	void saveOperationHistory(OperationHistory history);

	OperationHistory getHistory(UUID id);

	User getUserByToken(String token) throws StorageException;

	FilterMaker getResellerUserMaker();

}
