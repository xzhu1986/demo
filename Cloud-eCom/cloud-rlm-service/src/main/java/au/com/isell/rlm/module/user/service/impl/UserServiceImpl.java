package au.com.isell.rlm.module.user.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.aws.S3Manager;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.storage.NotFoundException;
import au.com.isell.common.storage.StorageException;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.common.exception.BizAssert;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.mail.service.MailService;
import au.com.isell.rlm.module.mail.vo.MailAddress;
import au.com.isell.rlm.module.mail.vo.MailContent;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.vo.ResellerUserSearchFilters;
import au.com.isell.rlm.module.reseller.vo.ResellerUserSearchVals;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.LoginHistory;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.domain.OperationHistory.TargetType;
import au.com.isell.rlm.module.user.domain.ResetPasswordRequest;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.UserService;
import au.com.isell.rlm.module.user.vo.AgentUserSearchVal;
import au.com.isell.rlm.module.user.vo.ResetPwdBean;
import au.com.isell.rlm.module.user.vo.login.LoginForm;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private MailService mailService;

	@Override
	public User getUserByName(String username) { 
		return userDAO.getUserByUserName(username);
	}
	@Override
	public ResellerUser getResellerUser(String serialNo,String email) { 
		FilterMaker maker=userDAO.getResellerUserMaker();
		FilterItem item1=maker.makeNameFilter("serialNo", TextMatchOption.Is, serialNo);
		FilterItem item2=maker.makeNameFilter("email", TextMatchOption.Is, email);
		List<ResellerUser> list=userDAO.queryUser(ResellerUser.class, maker.linkWithAnd(item1,item2), 1, 1).getValue();
		return CollectionUtils.isNotEmpty(list)?list.get(0):null;
	}
	
	@Override
	public boolean tryLogin(LoginForm loginForm,User user) {
		LoginHistory history = createLoginHistory(user);
		UsernamePasswordToken token = new UsernamePasswordToken(loginForm.getToken(), 
				au.com.isell.common.util.SecurityUtils.salt(loginForm.getToken(), loginForm.getPassword()), false);
		try {
			
			SecurityUtils.getSubject().login(token);
			GlobalAttrManager.setCurrentUser(user);

			history.setSuccess(true);
			userDAO.saveLoginHistory(history);
			return true;
		} catch (AuthenticationException e) {
			history.setSuccess(false);
			userDAO.saveLoginHistory(history);
			return false;
		}
	}
	
	private LoginHistory createLoginHistory(User user) {
		LoginHistory history = new LoginHistory();
		history.setUserId(user.getUserId());
		history.setIpAddress(GlobalAttrManager.getClientInfo().getIpAddress()); 
		history.setLoginDate(new Date());
		return history;
	}

	@Override
	public String sendResetPasswordEmail(ResetPwdBean user, String basePath) {
		if (user != null) {
			try {
				String title = "Reset your password(ISell)";
				String hash = userDAO.generateResetPasswordHash(user.getUserId(), user.getEmail());
				Map dataModel = new HashMap();
				dataModel.put("basePath", basePath);
				dataModel.put("modulePath", ModulePath.SECURITY);
				dataModel.put("hash", URLEncoder.encode(hash, "UTF-8"));
				dataModel.put("username", user.getIdentityName());
				MailContent mailContent=new MailContent(title, "common/mail/resetPasswordRequest.html",dataModel).withTo(new MailAddress(user.getEmail(), user.getIdentityName()));
				mailService.send(mailContent);
				return "Operation finished.Check the mail box to reset the password.";
			} catch (StorageException e) {
				return e.getMessage();
			} catch (NotFoundException e) {
				return e.getMessage();
			} catch (UnsupportedEncodingException e) {
				return e.getMessage();
			}
		}
		return "user not found";
	}

	@Override
	public void isValidHash(String hash) {
		ResetPasswordRequest request = userDAO.getResetpasswordRequest(hash);
		BizAssert.isTrue(request!=null&&request.getExpireDate().after(new Date()), "error.pwd.has_reset");
	}

	@Override
	public void updatePwdByHash(String hash, String pwd) {
		ResetPasswordRequest request = userDAO.getResetpasswordRequest(hash);
		User user = userDAO.getUserById(request.getUserId());
		user.setPassword(pwd);
		saveUser(user);
		userDAO.delete(request);

	}

	private UUID saveUser(User user) {
		String pwd=user.getPassword();
		BizAssert.hasText(pwd, "error.form.field.blank", "password");
		BizAssert.state(pwd.length()>=8, "error.pwd.tooshort");		
		BizAssert.state(pwd.matches(".*\\d.*"), "error.pwd.need_a_number");		
		BizAssert.state(pwd.matches(".*[a-z].*"), "error.pwd.need_lowercase_letter");		
		BizAssert.state(pwd.matches(".*[A-Z].*"), "error.pwd.need_uppercase_letter");		
		BizAssert.state(pwd.matches(".*[!@#$%].*"), "error.pwd.need_contains_letter");		
		BizAssert.state(!pwd.toLowerCase().contains(user.getEmail().toLowerCase())&&!pwd.toLowerCase().contains(user.getUsername().toLowerCase()), "error.pwd.must_not_contains");		
		return userDAO.saveUser(user);
	}
	
	@Override
	public Pair<Long, List<AgentUser>> queryAgentUser(AgentUserSearchVal searchVal, Integer pageSize, Integer pageNo) {
		FilterMaker maker = userDAO.getAgentUserMaker();
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		return userDAO.queryUser(AgentUser.class, searchVal.getFilter(maker), pageSize, pageNo);
	}

	@Override
	public Pair<Long, List<ResellerUser>> queryResellerUser(ResellerUserSearchVals searchVal,String filter,String serialNo,Boolean primary, Integer pageSize, Integer pageNo) {
		FilterMaker maker = userDAO.getResellerUserMaker();
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		
		FilterItem item1 = null;
		if(searchVal!=null){
			item1 = searchVal.getFilter(maker);
		}
		FilterItem item2 = null;
		if (StringUtils.isNotBlank(filter)) {
			ResellerUserSearchFilters filters = (ResellerUserSearchFilters) au.com.isell.common.bean.BeanUtils.getEnum(ResellerUserSearchFilters.class, filter);
			item2 = filters.getFilterItem(maker);
		}
		FilterItem item3 = maker.makeNameFilter("serialNo", TextMatchOption.Is,serialNo);
		
		FilterItem item4 = null;
		if(primary!=null){
			item4 = maker.makeNameFilter("primary", TextMatchOption.Is, String.valueOf(primary));
		}
		
		Pair<Long, List<ResellerUser>> r = userDAO.queryUser(ResellerUser.class,maker.linkWithAnd(item1, item2, item3,item4), pageSize, pageNo);
		return r;
	}
	@Override
	public <T extends User> T getUser(Class<T> cls, UUID userId) {
		return (T)userDAO.getUserById(userId);
	}


	@Override
	public UUID saveIsellUser(User formUser, String permissionDatas) {
		if (StringUtils.isNotBlank(permissionDatas)) {
			List<String> permissonList = JsonUtils.decode(permissionDatas, List.class);
			for (String key : permissonList) {
				formUser.addPermission(key);
			}
		}
		return saveUser(formUser);
	}

	@Override
	public Pair<String, String> getAccessAndSecretKeys() {// TODO use real logic later
//		GlobalAttrManager.getCurrentUser().getUserId()
		return new Pair<String, String>(S3Manager.getAccessKey(), S3Manager.getSecretKey());
	}

	@Override
	public List<User> getSalesRepList(String agentId) {
		FilterMaker maker = userDAO.getAgentUserMaker();
		FilterItem filterItem1 = maker.makeNameFilter("salesRepo", TextMatchOption.Is, "true");
		FilterItem filterItem2 = null;
		if (StringUtils.isNotBlank(agentId))
			maker.makeNameFilter("agentId", TextMatchOption.Is, agentId);
		return userDAO.queryUser(User.class, maker.linkWithAnd(filterItem1, filterItem2), null, null).getValue();
	}

	@Override
	public List<User> getAgentUserList() {
		List<User> users = new ArrayList<User>();
		users.addAll(userDAO.queryUser(AgentUser.class, new ESFilterItem(), null, null).getValue());
		return users;
	}

	@Override
	public User getUser(String userId) {
		return this.getUser(User.class, UUID.fromString(userId));
	}

	@Override
	public User getUserByEmail(String email,String username) {
		Assert.hasText(email, "Email can not be blank");
		Assert.hasText(username, "Username can not be blank");
		FilterMaker maker = userDAO.getAgentUserMaker();
		FilterItem filterItem = maker.makeNameFilter("email", TextMatchOption.Is, email);
		FilterItem filterItem2 = maker.makeNameFilter("username", TextMatchOption.Is, username);
		List<User> users=userDAO.queryUser(User.class, maker.linkWithAnd(filterItem,filterItem2), 1, 1).getValue();
		if(CollectionUtils.isNotEmpty(users)){
			return users.get(0);
		}
		return null;
	}
	
	@Override
	public User getResellerUserByEmail(String email,String serialNo) {
		Assert.hasText(email, "Email can not be blank");
		Assert.hasText(serialNo, "Serial No can not be blank");
		FilterMaker maker = userDAO.getResellerUserMaker();
		FilterItem filterItem = maker.makeNameFilter("email", TextMatchOption.Is, email);
		FilterItem filterItem2 = maker.makeNameFilter("serialNo", TextMatchOption.Is, serialNo);
		List<ResellerUser> users=userDAO.queryUser(ResellerUser.class, maker.linkWithAnd(filterItem,filterItem2), 1, 1).getValue();
		if(CollectionUtils.isNotEmpty(users)){
			return users.get(0);
		}
		return null;
	}
	
	@Override
	public void checkUsername(String username,String userid){
		User user=new User();
		user.setUserId(UUID.fromString(userid));
		user.setUsername(username);
		userDAO.checkDuplicateUsername(user);
	}
	
	@Override
	public List queryOperationHistory(TargetType historyType,String targetId) {
		FilterMaker maker = userDAO.getOptHistoryMaker();
		FilterItem item1=null;
		if(historyType!=null){
			item1=maker.makeNameFilter("targetType",TextMatchOption.Is, String.valueOf(historyType.ordinal()));
		}
		FilterItem item2=null;
		if(StringUtils.isNotBlank(targetId)){
			item2=maker.makeNameFilter("targetId",TextMatchOption.Is, targetId);
		}
		List r=new ArrayList();
		List<OperationHistory> list=userDAO.queryOperationHistory(maker.linkWithAnd(item1,item2));
		if(list!=null){
			try {
				for(OperationHistory history:list){
					Map m=BeanUtils.describe(history);
					m.put("date", DatePicker.getFormatedDateTime(history.getDate()));
					r.add(m);
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			} 
		}
		return r;
	}
	
	@Override
	public OperationHistory getOperationHistory(UUID historyId) {
		OperationHistory history=userDAO.getHistory(historyId);
		history.setUserName(this.getUser(history.getUserId().toString()).getUsername());
		return history;
	}
}
