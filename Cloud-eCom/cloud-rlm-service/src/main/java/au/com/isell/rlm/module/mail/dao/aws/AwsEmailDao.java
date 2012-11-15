package au.com.isell.rlm.module.mail.dao.aws;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.mail.dao.EmailDao;
import au.com.isell.rlm.module.mail.domain.EmailNotifyHistory;
import au.com.isell.rlm.module.mail.domain.EmailTemplate;
import au.com.isell.rlm.module.user.service.PermissionService;

@Repository
public class AwsEmailDao extends DAOSupport implements EmailDao {
	private static Logger logger = LoggerFactory.getLogger(AwsEmailDao.class);
	private IndexHelper indexHelper = IndexHelper.getInstance();
	@Autowired
	private PermissionService permissionService;

	@Override
	public synchronized FilterMaker getMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(EmailTemplate.class);
		maker.setFieldMapper(new FieldMapper());
		return maker;
	}

	@Override
	public Pair<Long, List<EmailTemplate>> search(FilterItem filterItem, Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0] = getMaker().makeSortItem("typeName", true);
		return indexHelper.queryBeans(EmailTemplate.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}
	
	@Override
	public void deleteEmailTpl(UUID id) {
		EmailTemplate tpl = new EmailTemplate();
		tpl.setTypeId(id);
		super.delete(tpl);
	}

	@Override
	public EmailTemplate getEmailTpl(UUID id) {
		EmailTemplate tpl = new EmailTemplate();
		tpl.setTypeId(id);
		return super.get(tpl);
	}
	
	@Override
	public UUID saveEmailTpl(EmailTemplate template) {
		EmailTemplate dbObj = template;
		if(template.getTypeId()!=null){
			 dbObj= super.get(template);
			 BeanUtils.copyPropsExcludeNull(dbObj, template);
		}else{
			dbObj.setTypeId(UUID.randomUUID());
		}
		super.save(template);
		return template.getTypeId();
	}

	@Override
	public List<EmailNotifyHistory> queryNotifyHistory(FilterItem filterItem, Pair<String, Boolean>[] sorts) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.queryBeans(EmailNotifyHistory.class, new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts))
				.getValue();
	}
	
	@Override
	public FilterMaker getNotifyHistoryMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(EmailNotifyHistory.class);
		return maker;
	}
}
