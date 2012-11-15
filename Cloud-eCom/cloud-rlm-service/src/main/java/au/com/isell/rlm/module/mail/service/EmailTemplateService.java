package au.com.isell.rlm.module.mail.service;

import java.util.List;
import java.util.UUID;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;
import au.com.isell.rlm.module.mail.vo.EmailTplSearchVals;

public interface EmailTemplateService {

	Pair<Long, List<EmailTemplate>> search(EmailTplSearchVals searchVals, Integer pageSize, Integer pageNo);

	EmailTemplate getEmailTpl(String id);

	UUID saveEmailTpl(EmailTemplate template,List<String> attachments);

	void deleteEmailTpl(String id);

	List<EmailTemplate> queryNotifyEmailTemplates(String query,String type);
	
}
