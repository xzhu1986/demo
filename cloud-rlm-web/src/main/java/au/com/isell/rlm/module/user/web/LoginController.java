package au.com.isell.rlm.module.user.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.util.CookieUtils;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.CodeName;
import au.com.isell.rlm.common.constant.ControllerConstant;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.common.utils.GlobalResellerAttrManager;
import au.com.isell.rlm.common.utils.I18NUtils;
import au.com.isell.rlm.common.utils.SessionAttrManager;
import au.com.isell.rlm.module.jbe.service.JbeService;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.constant.UserType;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.service.UserService;
import au.com.isell.rlm.module.user.vo.login.AgentLoginForm;
import au.com.isell.rlm.module.user.vo.login.ResellerLoginForm;
import au.com.isell.rlm.module.user.web.shiro.CustomAuthFilter;

@Controller
public class LoginController {
	@Autowired
	private UserService userService;
	@Autowired
	private JbeService jbeService;

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(ModelMap modelMap, String userType) {
		SecurityUtils.getSubject().logout();
		if (StringUtils.isNotEmpty(userType) && userType.equals(UserType.Reseller.name())) {
			return "redirect:/portal";
		} else {
			return "redirect:/login";
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(ModelMap modelMap,HttpServletRequest request) {
		modelMap.put("username", CookieUtils.getCookeValue("ag_username", request.getCookies()));
		modelMap.put("password", CookieUtils.getCookeValue("ag_password", request.getCookies()));
		modelMap.put("remember", CookieUtils.getCookeValue("ag_remember", request.getCookies()));
		return "module/login/login";
	}

	@RequestMapping(value = "/portal", method = RequestMethod.GET)
	public String resellerPortalLogin(ModelMap modelMap,HttpServletRequest request) {
		modelMap.put("userType", UserType.Reseller.name());
		modelMap.put("serialNo", CookieUtils.getCookeValue("rs_serialNo", request.getCookies()));
		modelMap.put("email", CookieUtils.getCookeValue("rs_email", request.getCookies()));
		modelMap.put("password", CookieUtils.getCookeValue("rs_password", request.getCookies()));
		modelMap.put("remember", CookieUtils.getCookeValue("rs_remember", request.getCookies()));
		return "module/login/reseller-login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String check(@ModelAttribute AgentLoginForm loginForm, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
		modelMap.put("loginForm", loginForm);
		if (StringUtils.isBlank(loginForm.getUsername())) {
			modelMap.put(CodeName.ERROR, I18NUtils.getMsg("error.form.field.blank", "username"));
			return login(modelMap,request);
		}

		User user = userService.getUserByName(loginForm.getUsername());
		if (user == null || !userService.tryLogin(loginForm, user) || !UserStatus.ACTIVE.equals(user.getStatus())) {
			modelMap.put(CodeName.ERROR, I18NUtils.getMsg("error.form.field.invalid"));
			return login(modelMap,request);
		}

		if (loginForm.shouldRemeber()) {
			response.addCookie(CookieUtils.createCookie("ag_username", loginForm.getUsername()));
			response.addCookie(CookieUtils.createCookie("ag_password", loginForm.getPassword()));
			response.addCookie(CookieUtils.createCookie("ag_remember", loginForm.getRemember()));
		} else {
			response.addCookie(CookieUtils.createDelCookie("ag_username"));
			response.addCookie(CookieUtils.createDelCookie("ag_password"));
			response.addCookie(CookieUtils.createDelCookie("ag_remember"));
		}

		return storeUserCache(request);
	}

	@RequestMapping(value = "/portal", method = RequestMethod.POST)
	public String checkResellerUser(@ModelAttribute ResellerLoginForm loginForm, ModelMap modelMap, HttpServletRequest request,
			HttpServletResponse response) {
		modelMap.put("loginForm", loginForm);
		if (StringUtils.isBlank(loginForm.getSerialNo()) || StringUtils.isBlank(loginForm.getEmail())) {
			modelMap.put(CodeName.ERROR, I18NUtils.getMsg("error.form.field.blank", "serial number and email"));
			return resellerPortalLogin(modelMap,request);
		}

		User user = userService.getResellerUser(loginForm.getSerialNo(), loginForm.getEmail());
		if (user == null || !userService.tryLogin(loginForm, user) || !UserStatus.ACTIVE.equals(user.getStatus())) {
			modelMap.put(CodeName.ERROR, I18NUtils.getMsg("error.form.field.invalid"));
			return resellerPortalLogin(modelMap,request);
		}
		// store info from jbe
		GlobalResellerAttrManager.setAccountInfo(jbeService.getResellerUserAccountInfo());
		if (loginForm.shouldRemeber()) {
			response.addCookie(CookieUtils.createCookie("rs_serialNo", loginForm.getSerialNo()));
			response.addCookie(CookieUtils.createCookie("rs_email", loginForm.getEmail()));
			response.addCookie(CookieUtils.createCookie("rs_password", loginForm.getPassword()));
			response.addCookie(CookieUtils.createCookie("rs_remember", loginForm.getRemember()));
		} else {
			response.addCookie(CookieUtils.createDelCookie("rs_serialNo"));
			response.addCookie(CookieUtils.createDelCookie("rs_email"));
			response.addCookie(CookieUtils.createDelCookie("rs_password"));
			response.addCookie(CookieUtils.createDelCookie("rs_remember"));
		}

		return storeUserCache(request);
	}

	private String storeUserCache(HttpServletRequest request) {
		// this session value currrently should only be used in freemarker tpl
		HttpSession session = request.getSession();
		session.setAttribute(ControllerConstant.USERNAME_IN_SESSION, GlobalAttrManager.getCurrentUser());
		session.setAttribute("userType", GlobalAttrManager.getCurrentUser().getType().name());
		String redirectTo = (String) session.getAttribute(CustomAuthFilter.JUMPTO);
		if (redirectTo != null && redirectTo.indexOf("reActivateSession") < 0) {
			session.removeAttribute(CustomAuthFilter.JUMPTO);
			return "redirect:" + redirectTo;
		}

		return home();
	}

	@RequestMapping("/home")
	public String home() {
		User user = GlobalAttrManager.getCurrentUser();
		return "redirect:" + user.getType().getEntryPath() + "/home";
	}

	@RequestMapping("/unauthorized")
	public String hounauthorizedme(ModelMap modelMap,HttpServletRequest request) {
		return login(modelMap,request);
	}

	@RequestMapping(value = "/timezone", method = RequestMethod.POST)
	public Result setTimeZone(HttpServletRequest request, int offset) {
		// String userTimeZone=TimeZone.getAvailableIDs(offset*1000*60)[0].toString();
		// SessionAttrManager.setTimezone(request, userTimeZone);
		SessionAttrManager.setTimezone(request, "Australia/Sydney");
		return new Result();
	}

	@RequestMapping(value = "/reActivateSession")
	public Result reActivateSession() {
		return new Result();
	}

}
