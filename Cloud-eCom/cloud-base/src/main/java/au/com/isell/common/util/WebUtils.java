package au.com.isell.common.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author frankw 24/03/2011
 */
public class WebUtils {

	/**
	 * get base path for request,e.g. http://www.aaa.com/
	 */
	public static String getBasePath(HttpServletRequest request) {
		String path = request.getContextPath();
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
		return basePath;
	}

	/**
	 * get spring ApplicationContext
	 */
	public static WebApplicationContext getApplicationContext(HttpServletRequest request) {
		return (WebApplicationContext) request.getSession().getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
	}

	public static String getWebAppBasePath() throws IOException {
		return new ClassPathResource("/").getFile().getParentFile().getParentFile().getAbsolutePath().replace("%20", " ");
	}

}
