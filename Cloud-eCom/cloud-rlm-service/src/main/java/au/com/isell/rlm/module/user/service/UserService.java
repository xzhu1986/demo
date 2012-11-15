package au.com.isell.rlm.module.user.service;

import java.util.List;
import java.util.UUID;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.vo.ResellerUserSearchVals;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.domain.OperationHistory.TargetType;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.vo.AgentUserSearchVal;
import au.com.isell.rlm.module.user.vo.ResetPwdBean;
import au.com.isell.rlm.module.user.vo.login.LoginForm;

public interface UserService {
	boolean tryLogin(LoginForm loginForm,User user);

	String sendResetPasswordEmail(ResetPwdBean user, String basePath);

	void updatePwdByHash(String hash, String pwd);

	void isValidHash(String hash);

	Pair<Long, List<AgentUser>> queryAgentUser(AgentUserSearchVal searchVal, Integer pageSize, Integer pageNo);
	
	<T extends User> T getUser(Class<T> cls,UUID userId);
	
	User getUser(String userId);
	
	User getUserByEmail(String email,String username);
	
	UUID saveIsellUser(User formUser,String permissionDatas);
	
	Pair<String, String> getAccessAndSecretKeys();
	
	List<User> getSalesRepList(String agentId);
	
	List<User> getAgentUserList();

	void checkUsername(String username, String userid);

	List<OperationHistory> queryOperationHistory(TargetType historyType, String targetId);

	OperationHistory getOperationHistory(UUID historyId);

	User getUserByName(String username);

	ResellerUser getResellerUser(String serialNo, String email);
	
	Pair<Long, List<ResellerUser>> queryResellerUser(ResellerUserSearchVals searchVal,String filter,String serialNo,Boolean primary, Integer pageSize, Integer pageNo);

	User getResellerUserByEmail(String email, String serialNo);
}
