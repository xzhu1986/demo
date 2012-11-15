package au.com.isell.rlm.module.mail.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.mail.dao.EmailDao;
import au.com.isell.rlm.module.mail.domain.EmailTargetType;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;
import au.com.isell.rlm.module.mail.service.EmailTemplateService;
import au.com.isell.rlm.module.mail.vo.EmailTplSearchVals;

/**
 * 
 * @author frankw 21/05/2012
 */
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {
	@Autowired
	private EmailDao emailDao;

	@Override
	public Pair<Long, List<EmailTemplate>> search(EmailTplSearchVals searchVals, Integer pageSize, Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = emailDao.getMaker();
		Pair<Long, List<EmailTemplate>> r = emailDao.search(searchVals.getFilter(maker), pageSize, pageNo);
		return r;
	}

	@Override
	public EmailTemplate getEmailTpl(String id) {
		Assert.hasText(id, "id is null");
		return emailDao.getEmailTpl(UUID.fromString(id));
	}

	@Override
	public void deleteEmailTpl(String id) {
		Assert.hasText(id, "id is null");
		emailDao.deleteEmailTpl(UUID.fromString(id));
	}

	@Override
	public UUID saveEmailTpl(EmailTemplate template, List<String> attachments) {
		template.setAttachments(new HashSet<String>(attachments));
		return emailDao.saveEmailTpl(template);
	}

	@Override
	public List<EmailTemplate> queryNotifyEmailTemplates(String query,String target) {
		EmailTargetType type=(EmailTargetType)BeanUtils.getEnum(EmailTargetType.class, target);
		// filter by resuller supplier map
		FilterMaker tplMaker = emailDao.getMaker();
		FilterItem item=null;
		if(StringUtils.isNotBlank(query)){
			item=tplMaker.linkWithOr(tplMaker.makeNameFilter("typeName", TextMatchOption.Contains, query),tplMaker.makeNameFilter("subject", TextMatchOption.Contains, query));
		}
		FilterItem item3=tplMaker.makeNameFilter("target", TextMatchOption.Is, String.valueOf(type.ordinal()));
		item= item==null? item3:tplMaker.linkWithAnd(item,item3); 
		return emailDao.search(item ,50,1).getValue();
	}
}
