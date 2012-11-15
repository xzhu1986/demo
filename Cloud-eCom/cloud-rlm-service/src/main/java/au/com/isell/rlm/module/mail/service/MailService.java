package au.com.isell.rlm.module.mail.service;

import au.com.isell.rlm.module.mail.vo.MailContent;

/**
 * @author frankw 24/05/2011
 */
public interface MailService {

	void send(MailContent mailContent);
}
