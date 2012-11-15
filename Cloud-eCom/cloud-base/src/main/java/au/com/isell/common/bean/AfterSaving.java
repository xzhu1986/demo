package au.com.isell.common.bean;

import org.springframework.context.ApplicationContext;

/**
 * @author frankw 14/05/2012
 */
public interface AfterSaving {
	public void operate(ApplicationContext context);
}
