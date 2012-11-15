package au.com.isell.rlm.module.invoice.dao.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.common.util.IDServerClient;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.agent.dao.AgentDao;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.invoice.dao.InvoiceDao;
import au.com.isell.rlm.module.invoice.domain.Invoice;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups;
import au.com.isell.rlm.module.invoice.domain.InvoiceSchedule;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.Reseller;

/**
 * @author frankw 13/02/2012
 */
@Repository
public class AwsInvoiceDao extends DAOSupport implements InvoiceDao {
	private static FieldMapper invoiceMapper = new FieldMapper();
	@Autowired
	private ResellerDao resellerDao;
	@Autowired
	private AgentDao agentDao;
	@Autowired
	private AddressDao addressDao;
	
	static {
	}

	@Override
	public FilterMaker getFilterMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(InvoiceSearchBean.class);
		maker.setFieldMapper(invoiceMapper);
		return maker;
	}

	@Override
	public Pair<Long, List<InvoiceSearchBean>> searchInvoice(FilterItem filterItem, Pair<String, Boolean>[] sorts,Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		
		return indexHelper.queryBeans(InvoiceSearchBean.class,new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public Invoice getInvoice(int invoiceNumber) {
		Invoice invoice = new Invoice();
		invoice.setInvoiceNumber(invoiceNumber);
		return super.get(invoice);
	}

	@Override
	public void saveAgent(Invoice agent) {
		super.save(agent); 
	}

	@Override
	public List<InvoiceSchedule> getInvoiceSchedules(int invoiceNumber) {
		Pair<String, Boolean>[] sorts=new Pair[]{new Pair("dueDate", Boolean.TRUE)};
		return super.query(InvoiceSchedule.class, termQuery("invoiceNumber", invoiceNumber), sorts);
	}

	@Override
	public List<InvoiceFollowups> getinInvoiceFollowups(int invoiceNumber) {
		Pair<String, Boolean>[] sorts=new Pair[]{new Pair("_id", Boolean.TRUE)};
		return super.query(InvoiceFollowups.class, termQuery("invoiceNumber", invoiceNumber), sorts);
	}

	@Override
	public int saveInvoice(Invoice invoice) {
		Integer invoiceNum=invoice.getInvoiceNumber();
		if(invoiceNum==null){
			invoiceNum=IDServerClient.getIntId("invoice", 1)[0];
			invoice.setInvoiceNumber(invoiceNum);
		}
		super.save(invoice);
		reindex(invoiceNum);
		return invoiceNum;
	}

	private void reindex(int invoiceNumber){
		Invoice invoice=getInvoice(invoiceNumber);
		Reseller reseller=resellerDao.getReseller(invoice.getResellerSerialNo());
		AddressItem country=addressDao.getAddressItem(reseller.getCountry());
		Agent agent=agentDao.getAgent(invoice.getAgentId());
		List<InvoiceSchedule> schedules=getInvoiceSchedules(invoiceNumber);
		List<InvoiceFollowups> followups=getinInvoiceFollowups(invoiceNumber);
		InvoiceSearchBean searchBean=new InvoiceSearchBean(invoice, agent, schedules, followups,String.valueOf(country.getPhoneAreaCodeBind()));
		indexHelper.indexValues(searchBean);
	}

	@Override
	public InvoiceSchedule getInvoiceSchedule(int invoiceNumber, long dueDate) {
		InvoiceSchedule invoiceSchedule = new InvoiceSchedule();
		invoiceSchedule.setDueDate(new Date(dueDate));
		invoiceSchedule.setInvoiceNumber(invoiceNumber);
		return super.get(invoiceSchedule);
	}

	@Override
	public void saveInvoiceSchedules(int invoiceNumber, InvoiceSchedule... invoiceSchedules) {
		if(invoiceSchedules!=null && invoiceSchedules.length>0){
			Integer invoiceNum=invoiceSchedules[0].getInvoiceNumber();
			super.save(invoiceSchedules);
			reindex(invoiceNum);
		}
	}

	@Override
	public void deleteInvoiceSchedule(int invoiceNumber, String... dueDate) {
		if(dueDate!=null){
			InvoiceSchedule[] invoiceSchedules = new InvoiceSchedule[dueDate.length];
			InvoiceSchedule invoiceSchedule = null;
			for(int i=0;i<dueDate.length;i++){
				invoiceSchedule = new InvoiceSchedule();
				invoiceSchedule.setInvoiceNumber(invoiceNumber);
				invoiceSchedule.setDueDate(new Date(Long.parseLong(dueDate[i])));
				invoiceSchedules[i] = invoiceSchedule;
			}
			super.delete(invoiceSchedules);
		}
		reindex(invoiceNumber);
	}

	@Override
	public InvoiceFollowups getInvoiceFollowup(int invoiceNumber, long followupDate) {
		InvoiceFollowups invoiceFollowup = new InvoiceFollowups();
		invoiceFollowup.setFollowupDate(new Date(followupDate));
		invoiceFollowup.setInvoiceNumber(invoiceNumber);
		return super.get(invoiceFollowup);
	}

	@Override
	public void saveInvoiceFollowup(InvoiceFollowups followup) {
		Integer invoiceNum=followup.getInvoiceNumber();
		super.save(followup);
		reindex(invoiceNum);
	}

	@Override
	public void deleteInvoiceFollowups(int invoiceNumber, String... followupDate) {
		if(followupDate!=null){
			InvoiceFollowups[] invoiceFollowups = new InvoiceFollowups[followupDate.length];
			InvoiceFollowups invoiceFollowup = null;
			for(int i=0;i<followupDate.length;i++){
				invoiceFollowup = new InvoiceFollowups();
				invoiceFollowup.setInvoiceNumber(invoiceNumber);
				invoiceFollowup.setFollowupDate(new Date(Long.parseLong(followupDate[i])));
				invoiceFollowups[i] = invoiceFollowup;
			}
			super.delete(invoiceFollowups);
		}
		reindex(invoiceNumber);
	}

	@Override
	public Iterable<InvoiceSearchBean> queryInvoice(FilterItem filterItem) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.iterateBeans(InvoiceSearchBean.class,new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), null));
	}
	
	public static void main(String[] args) {
		InvoiceDao invoiceDao=new AwsInvoiceDao();
		FilterItem filterItem=invoiceDao.getFilterMaker().makeDateRange("invoiceDate", DateUtils.addYears(Calendar.getInstance().getTime(), -2), Calendar.getInstance().getTime(), true, false);
		Iterable<InvoiceSearchBean> beans = invoiceDao.queryInvoice(filterItem);
		for(InvoiceSearchBean bean:beans){
			System.out.println(bean);
		}
	}
}
