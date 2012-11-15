package au.com.isell.rlm.module.user.init;

import java.util.List;
import java.util.UUID;

import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.common.util.SecurityUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.PermissionPreloadingService;

@Component
public class InitUser extends DAOSupport {

	public static final String ADMIN_USERNAME = "isell";

	@Autowired
	private PermissionPreloadingService permissionService;
	@Autowired
	private UserDAO userDAO;

	UUID initAgentuser(String username, String password, String firstName, String lastName, String email, boolean salesRepo, boolean admin) {
		AgentUser user = new AgentUser();
		user.setUsername(username);
		user.setPassword(SecurityUtils.digestPassword(username, password));
		user.setAgentId(UUID.fromString("ae7b1620-7740-11e1-b0c4-0800200c9a66"));
		user.setAgentName("iSell");
		user.setDataSkillLevel(10);
		user.setSalesRepo(salesRepo);

		Pair<Long, List<AgentUser>> users = IndexHelper.getInstance().queryBeans(AgentUser.class,
				new QueryParams(QueryBuilders.termQuery("username", username), null).withPaging(1, 1).withRoutings(new String[0]));
		if (users.getKey() == 0) {
			setAdditional(firstName, lastName, email, salesRepo, user);
		} else {
			AgentUser dbUser = users.getValue().get(0);
			UUID userId = dbUser.getUserId();
			dbUser = (AgentUser) userDAO.getUserById(userId);
			if (dbUser != null) {
				BeanUtils.copyPropsExcludeNull(dbUser, user);
				user = dbUser;
			} else {
				setAdditional(firstName, lastName, email, salesRepo, user);
			}
			user.setUserId(userId);
		}
		if (admin) {
			for (String permission : permissionService.getPermissions()) {
				user.addPermission(permission);
			}
		}

		userDAO.saveUser(user);
		return user.getUserId();
	}

	private void setAdditional(String firstName, String lastName, String email, boolean salesRepo, AgentUser user) {
		user.setUserId(UUID.randomUUID());
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setStatus(UserStatus.ACTIVE);
		user.setLoginFailureCount(1);
	}

	public void init() {
		try {
			initAgentuser(ADMIN_USERNAME, "isell", "iSell", "Admin", "apollo@isell.com.au", false, true);
			initAgentuser("richardb", "richardb", "Richard", "Beresford", "richardb@isell.com.au", true, true);
			initAgentuser("garyh", "garyh", "Gary", "Henderson", "garyh@isell.com.au", true, false);
			initAgentuser("brucez", "brucez", "Bruce", "Zhou", "brucez@isell.com.au", false, false);
			initAgentuser("gregw", "gregw", "Greg", "Weaver", "gregw@isell.com.au", false, false);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void reindex() {
		FilterMaker maker = userDAO.getUserMaker();
		FilterItem item = maker.makeAllQuery();
		for (User iUser : userDAO.queryUser(User.class, item, null, null).getValue()) {
			User u = userDAO.getUserById(iUser.getUserId());
			if (u != null)
				indexHelper.indexValues(u);
			else
				indexHelper.deleteByObj(iUser);
		}
	}
}
