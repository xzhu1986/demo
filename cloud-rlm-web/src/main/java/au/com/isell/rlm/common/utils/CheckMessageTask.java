package au.com.isell.rlm.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.com.isell.common.message.MessageChecker;

@Component
public class CheckMessageTask implements ApplicationContextAware {
	private static Logger logger = LoggerFactory.getLogger(CheckMessageTask.class);
	
	@Scheduled(cron="0 0/2 * * *  ?")
	public void execute() {
		try {
//			logger.info("Start checking messages.");
			MessageChecker.getInstance("main").checkMessages();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		MessageChecker.setApplicationContext(applicationContext);
	}
}
