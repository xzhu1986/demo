package au.com.isell.rlm.module.mail.dao;

import java.util.List;
import java.util.UUID;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.mail.domain.EmailNotifyHistory;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;

public interface EmailDao {

	Pair<Long, List<EmailTemplate>> search(FilterItem filterItem, Integer pageSize, Integer pageNo);

	FilterMaker getMaker();

	EmailTemplate getEmailTpl(UUID id);

	UUID saveEmailTpl(EmailTemplate template);

	List<EmailNotifyHistory> queryNotifyHistory(FilterItem filterItem, Pair<String, Boolean>[] sorts);

	FilterMaker getNotifyHistoryMaker();

	void deleteEmailTpl(UUID id);

}
