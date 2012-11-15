package au.com.isell.rlm.module.user.dao.aws;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.common.storage.DuplicateDataException;
import au.com.isell.common.storage.NotFoundException;
import au.com.isell.common.storage.StorageException;
import au.com.isell.common.util.SecurityUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.common.exception.BizException;
import au.com.isell.rlm.module.agent.dao.AgentDao;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.LoginHistory;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.domain.ResetPasswordRequest;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.vo.AuthInfoCache;

@Repository
public class AwsUserDao extends DAOSupport implements UserDAO {

	@Autowired
	private AgentDao agentDao;

	private IndexHelper indexHelper = IndexHelper.getInstance();
	private ESFilterMaker userMaker;
	private AuthInfoCache authInfoCache = new AuthInfoCache();

	@Override
	public AuthInfoCache getAuthInfoCache() {
		return authInfoCache;
	}

	@Override
	public FilterMaker getAgentUserMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(AgentUser.class);
		return maker;
	}

	@Override
	public FilterMaker getUserMaker() {
		if (userMaker != null)
			return userMaker;
		userMaker = new ESFilterMaker();
		userMaker.setType(User.class);
		return userMaker;
	}
	
	@Override
	public FilterMaker getResellerUserMaker() {
		FilterMaker userMaker = new ESFilterMaker();
		userMaker.setType(ResellerUser.class);
		return userMaker;
	}

	@Override
	public FilterMaker getOptHistoryMaker() {
		FilterMaker userMaker = new ESFilterMaker();
		userMaker.setType(OperationHistory.class);
		return userMaker;
	}

	@Override
	public User getUserById(UUID userId) throws StorageException {
		if (userId == null)
			return null;
		User sample = new User();
		sample.setUserId(userId);
		return super.get(sample);
	}

	@Override
	public User getUserByUserName(String username) throws StorageException {
		QueryBuilder builder = termQuery("username", username);
		Pair<Long, List<User>> datas = indexHelper.queryBeans(User.class, new QueryParams(builder, null));
		if (datas.getKey() > 0) {
			User user = datas.getValue().get(0);
			return super.get(user);
		}
		return null;
	}
	@Override
	public User getUserByToken(String token) throws StorageException {
		QueryBuilder builder = termQuery("token", token);
		Pair<Long, List<User>> datas = indexHelper.queryBeans(User.class, new QueryParams(builder, null));
		if (datas.getKey() > 0) {
			User user = datas.getValue().get(0);
			return super.get(user);
		}
		return null;
	}

	@Override
	public UUID saveUser(User user) throws DuplicateDataException, StorageException {
		Assert.hasText(user.getUsername());
		Assert.isTrue(!user.getUsername().contains(" "), "user name should not contains blank ");
		// Assert.hasText(user.getEmail());

		UUID userId = user.getUserId();
		User dbUser = getUserById(user.getUserId());
		if (dbUser != null) {
			BeanUtils.copyPropsExcludeNull(dbUser, user);
			user = dbUser;
		} else {
			if (userId == null) {
				userId = UUID.randomUUID();
				user.setUserId(userId);
			}
		}
		checkDuplicateUsername(user);
		digestPwd(user);
		if (user instanceof AgentUser) {
			Agent agent = agentDao.getAgent(((AgentUser) user).getAgentId());
			if (agent != null)
				((AgentUser) user).setAgentName(agent.getName());
		}
		save(user);
		this.authInfoCache.remove(user.getUserId());
		return userId;
	}

	private void digestPwd(User user) {
		if (user.getPassword() == null)
			user.setPassword("");
		if (user.getPassword().length() < SecurityUtils.DIGEST_LENGTH)
			user.setPassword(SecurityUtils.digestPassword(user.getToken(), user.getPassword()));
	}

	@Override
	public void checkDuplicateUsername(User user) {
		BoolQueryBuilder builder = QueryBuilders.boolQuery().must(termQuery("username", user.getUsername()));
		if (user.getUserId() != null) {
			builder = builder.mustNot(termQuery("_id", user.getUserId().toString()));
		}
		String[] keys = indexHelper.queryKeys(User.class, new QueryParams(builder, null));
		if (keys != null && keys.length > 0) {
			throw new BizException("error.user.name.duplicate", user.getUsername());
		}
	}

	@Override
	public String generateResetPasswordHash(UUID userId, String email) throws NotFoundException, StorageException {
		String hashFormat = "%s-%s-%s";
		String hash = SecurityUtils.digest(String.format(hashFormat, userId, email, System.currentTimeMillis()));
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setHash(hash);
		request.setUserId(userId);
		request.setExpireDate(DateUtils.addDays(new Date(), 15));
		save(request);
		return hash;
	}

	@Override
	public ResetPasswordRequest getResetpasswordRequest(String hash) throws StorageException {
		ResetPasswordRequest sample = new ResetPasswordRequest();
		sample.setHash(hash);
		return get(sample);
	}

	@Override
	public void deleteUserById(UUID userId) throws StorageException {
		if (userId == null)
			return;
		User user = new User();
		user.setUserId(userId);
		delete(user);
	}

	@Override
	public void saveLoginHistory(LoginHistory history) throws StorageException {
		save(history);
		if (!history.isSuccess()) {
			User sample = new User();
			sample.setUserId(history.getUserId());
			User user = get(sample);
			user.setLoginFailureCount(user.getLoginFailureCount() + 1);
			save(user);
		} else {
			User sample = new User();
			sample.setUserId(history.getUserId());
			User user = get(sample);
			user.setLoginFailureCount(0);
			save(user);
		}
	}

	@Override
	public void delete(ResetPasswordRequest request) {
		super.delete(request);
	}

	@Override
	public <T extends User> Pair<Long, List<T>> queryUser(Class<T> cls, FilterItem filterItem, Integer pageSize, Integer pageNo) {
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = getUserMaker().makeSortItem("username", true);
		return indexHelper.queryBeans(cls, new QueryParams(((ESFilterItem) filterItem).getQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public List<OperationHistory> queryOperationHistory(FilterItem filterItem) {
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = getOptHistoryMaker().makeSortItem("date", true);
		return indexHelper.queryBeans(OperationHistory.class, new QueryParams(((ESFilterItem) filterItem).getQueryBuilder(), sorts)).getValue();
	}

	@Override
	public void saveOperationHistory(OperationHistory history) {
		// delete one month ago
		Date oneMonthAgo = DateUtils.addDays(new Date(), -30);
		FilterMaker maker = getOptHistoryMaker();
		FilterItem item = maker.makeDateRange("date", null, oneMonthAgo, false, false);
		List<OperationHistory> list = queryOperationHistory(item);
		if (CollectionUtils.isNotEmpty(list)) {
			for (OperationHistory hist : list) {
				super.delete(hist);
			}
		}
		// save
		if (history.getId() == null)
			history.setId(UUID.randomUUID());
		super.save(history);
	}

	@Override
	public OperationHistory getHistory(UUID id) {
		OperationHistory history = new OperationHistory();
		history.setId(id);
		return super.get(history);
	}

}
