package au.com.isell.rlm.common.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import au.com.isell.common.util.WebUtils;
import au.com.isell.rlm.common.constant.ControllerConstant;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.common.utils.SessionAttrManager;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.vo.ClientInfo;

/**
 * @author frankw 19/03/2012
 */
public class AttrRetrieveInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		ClientInfo clientInfo=new ClientInfo();
		clientInfo.setTimezone(SessionAttrManager.getTimezone(request));
		clientInfo.setLocale(getLocale(request));
		clientInfo.setWebappBaseUrl(WebUtils.getBasePath(request));
		clientInfo.setIpAddress(getIpAddr(request));
		GlobalAttrManager.setClientInfo(clientInfo);
		
		GlobalAttrManager.setCurrentUser((User)request.getSession().getAttribute(ControllerConstant.USERNAME_IN_SESSION));
		return true;
	}
	
	private String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		String locale = getLocale(request).toString();
		request.setAttribute("locale", locale);
		super.postHandle(request, response, handler, modelAndView);
	}

	private Locale getLocale(HttpServletRequest request) {
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		return localeResolver.resolveLocale(request);
	}

}
