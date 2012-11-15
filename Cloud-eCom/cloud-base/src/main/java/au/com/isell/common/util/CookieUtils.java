package au.com.isell.common.util;

import javax.servlet.http.Cookie;

public class CookieUtils {
	private static final int rememberPeriod = 7 * 24 * 3600;
	
	public static Cookie createCookie(String name,String value){
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(rememberPeriod);// second
//		cookie.setSecure(true);
//		cookie.setPath(path);
//		cookie.setDomain(pattern)
		return cookie;
	}
	
	public static String getCookeValue(String name,Cookie[] cookies){
		if(cookies!=null){
			for(Cookie cookie:cookies){
				if(cookie.getName().equalsIgnoreCase(name)) return cookie.getValue();
			}
		}
		return null;
	}
	
	public static Cookie createDelCookie(String name) {
		Cookie cookie = CookieUtils.createCookie(name, "");
		cookie.setMaxAge(0);
		return cookie;
	}
}
