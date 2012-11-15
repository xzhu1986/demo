package au.com.isell.rlm.common.utils;

import javax.servlet.http.HttpServletRequest;

public class SessionAttrManager {

	public static void setTimezone(HttpServletRequest request, String userTimeZone) {
		request.getSession().setAttribute("userTimeZone", userTimeZone);
	}
	
	public static String getTimezone(HttpServletRequest request) {
		return (String)request.getSession().getAttribute("userTimeZone");
	}

}
