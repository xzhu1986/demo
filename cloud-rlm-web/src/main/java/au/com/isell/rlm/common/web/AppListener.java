package au.com.isell.rlm.common.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import au.com.isell.common.aws.resource.StaticResourceUtils;
import au.com.isell.common.aws.resource.StaticResourceUtils.ResourceFolder;
import au.com.isell.rlm.common.utils.ContextManager;

/**
 * Application Lifecycle Listener implementation class SessionListener
 * 
 */
public class AppListener implements ServletContextListener {

	private static final String RESOURCE_PREFIX = "resourcePrefix";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ContextManager.setContext(sce.getServletContext());

		if (!StaticResourceUtils.isTestEnv()) {
			sce.getServletContext().setAttribute(RESOURCE_PREFIX, StaticResourceUtils.getStaticResourceUrlPrefix(ResourceFolder.RLM));
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
