package au.com.isell.rlm.module.user.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.util.WebUtils;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.UserService;
import au.com.isell.rlm.module.user.vo.ResetPwdBean;

@Controller
@RequestMapping(value = ModulePath.SECURITY)
public class SecurityController {
	@Autowired
	private UserService userService;

	@RequestMapping("/password/ask-reset/{userId}")
	public Result askReset(@PathVariable String userId, HttpServletRequest request) {
		User user=userService.getUser(userId);
		ResetPwdBean bean=new ResetPwdBean(user.getFirstName()+" "+user.getLastName(), user.getUserId(), user.getEmail());
		String msg = userService.sendResetPasswordEmail(bean, WebUtils.getBasePath(request));
		return new Result(msg, true);
	}

	@RequestMapping(value = "/password/reset", method = RequestMethod.GET)
	public String toReset(String hash, HttpServletRequest request,ModelMap modelMap) {
		userService.isValidHash(hash);
		modelMap.put("hash", hash);
		return "module/login/reset-pwd";
	}
	
	@RequestMapping(value = "/password/reset", method = RequestMethod.POST)
	public Result updatePwd(String hash,String pwd, HttpServletRequest request) {
		userService.updatePwdByHash(hash, pwd);
		return new Result("Your password has been updated", true);
	}
	
	@RequestMapping("/password/forget-pwd")
	public Result forgetPwd(String email,String username, HttpServletRequest request) {
		User user=userService.getUserByEmail(email,username);
		Assert.notNull(user, "No user matches with your inputs.");
		ResetPwdBean bean=new ResetPwdBean(user.getFirstName()+" "+user.getLastName(), user.getUserId(), user.getEmail());
		String msg = userService.sendResetPasswordEmail(bean, WebUtils.getBasePath(request));
		return new Result(msg, true);
	}
	
	@RequestMapping("/password/forget-reseller-pwd")
	public Result forgetResellerPwd(String email,String serialNo, HttpServletRequest request) {
		ResellerUser user=(ResellerUser)userService.getResellerUserByEmail(email,serialNo);
		Assert.notNull(user, "No user matches with your inputs.");
		ResetPwdBean bean=new ResetPwdBean(user.getFirstName()+" "+user.getLastName(), user.getUserId(), user.getEmail());
		String msg = userService.sendResetPasswordEmail(bean, WebUtils.getBasePath(request));
		return new Result(msg, true);
	}
}
