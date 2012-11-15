package au.com.isell.rlm.common.utils;

import javax.servlet.ServletContext;

/**
 * @author frankw 10/02/2012
 */
public class ContextManager {
	private static ServletContext context;

	public static ServletContext getContext() {
		return context;
	}

	public static void setContext(ServletContext context) {
		if(ContextManager.context==null){
			ContextManager.context = context;
		
		}
	}
	
}
