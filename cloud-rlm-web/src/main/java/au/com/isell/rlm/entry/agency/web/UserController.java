package au.com.isell.rlm.entry.agency.web;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.user.constant.RolePermissionDefineService;
import au.com.isell.rlm.module.user.constant.RolePermissionDefineService.DefaultRole;
import au.com.isell.rlm.module.user.domain.OperationHistory.TargetType;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.UserService;

/**
 * @author frankw 14/03/2012
 */
@Controller
@RequestMapping(value=ModulePath.AGENT_USERS)
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private RolePermissionDefineService rolePermissionDefs;
	
	@RequestMapping(value="/agent/{userId}",method=RequestMethod.GET)
	public String getAgentUser(@PathVariable String userId,ModelMap modelMap) {
		Assert.notNull(userId);
		AgentUser user=null;
		if ("new".equals(userId)) {
			user = new AgentUser().init();
		} else {
			user=userService.getUser(AgentUser.class, UUID.fromString(userId));
			user.setAgentName(agentService.getAgent(user.getAgentId().toString()).getName());
		}
		modelMap.put("data", user);
		modelMap.put("userId", userId);
		return "agent/module/user/detail/agent-user";
	}
	
	@RequestMapping(value="/agent/{userId}",method=RequestMethod.POST)
	public String saveAgentUser(@PathVariable String userId,ModelMap modelMap,HttpServletRequest request,String permissionDatas) {
		AgentUser user= BeanUtils.constructBean(request, AgentUser.class, null);
		UUID savedId= agentService.saveAgentUser(user,permissionDatas);
		return String.format("redirect:%s/agent/%s", ModulePath.AGENT_USERS, savedId);
	}
	
	@RequestMapping(value="/sales-repos/{agentId}")
	public Result getSalesRepList(@PathVariable String agentId) {
		List<User> users=userService.getSalesRepList(agentId);	
		return new Result("success",users!=null?users.size():0,users);
	}
	
	@RequestMapping(value="/role-permissions/{roleName}")
	public Result getRolePermissions(@PathVariable String roleName) {
		DefaultRole role=(DefaultRole)BeanUtils.getEnum(DefaultRole.class, roleName);
		return new Result(rolePermissionDefs.getRolePermission(role));
	}
	
	@RequestMapping(value="/check-duplicate-username")
	public Result checkUsername(String username,String userid) {
		userService.checkUsername(username, userid);
		return new Result();
	}
	
	@RequestMapping(value = "/operation-history", method = RequestMethod.GET)
	public Result queryOperationHistory(ModelMap model, String targetId,String targetType,HttpServletRequest request) {
		Assert.hasText(targetId);
		Assert.hasText(targetType);
		TargetType type=(TargetType)BeanUtils.getEnum(TargetType.class, targetType);
		return new Result(userService.queryOperationHistory(type, targetId));
	}
}
