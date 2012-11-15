package au.com.isell.rlm.module.user.web.shiro;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;

import au.com.isell.common.util.UrlUtils;
import au.com.isell.rlm.common.constant.ControllerConstant;

public class CustomAuthFilter extends PassThruAuthenticationFilter {
	public static final String JUMPTO = "jumpto";

	private static String[] acceptedUrls = new String[] { "/login", "/password/reset", "/timezone", "/init" };

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest req = (HttpServletRequest) request;
		String reqUrl = req.getRequestURI();
		if (Arrays.binarySearch(acceptedUrls, reqUrl) == -1) {
			// handle for login redirect
			setJumpToUrl(request, req, reqUrl);
		}
		return super.onAccessDenied(request, response);
	}

	private void setJumpToUrl(ServletRequest request, HttpServletRequest req, String reqUrl) {
		if (StringUtils.isNotEmpty(req.getContextPath())) {
			reqUrl = reqUrl.substring(reqUrl.indexOf(req.getContextPath()) + req.getContextPath().length());
			Map requestMap = UrlUtils.getRequestMap((HttpServletRequest) request);
			if (requestMap != null && requestMap.size() > 0) {
				reqUrl += "?" + UrlUtils.joinParam4Url(requestMap);
			}
		}
		req.getSession().setAttribute(JUMPTO, reqUrl);
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		HttpServletRequest request2 = (HttpServletRequest) request;
		return super.isAccessAllowed(request, response, mappedValue) && request2.getSession().getAttribute(ControllerConstant.USERNAME_IN_SESSION)!=null;
	}

}
