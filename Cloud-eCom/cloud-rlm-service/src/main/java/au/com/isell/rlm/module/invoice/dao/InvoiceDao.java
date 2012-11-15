package au.com.isell.rlm.module.invoice.dao;

import java.util.List;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.invoice.domain.Invoice;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups;
import au.com.isell.rlm.module.invoice.domain.InvoiceSchedule;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;

public interface InvoiceDao {
	Pair<Long, List<InvoiceSearchBean>> searchInvoice(FilterItem filterItem, Pair<String, Boolean>[] sorts,Integer pageSize, Integer pageNo);

	FilterMaker getFilterMaker();

	Invoice getInvoice(int invoiceNumber);

	void saveAgent(Invoice invoice);

	List<InvoiceSchedule> getInvoiceSchedules(int invoiceNumber);

	List<InvoiceFollowups> getinInvoiceFollowups(int invoiceNumber);

	int saveInvoice(Invoice invoice);
	
	InvoiceSchedule getInvoiceSchedule(int invoiceNumber, long dueDate);
	
	void saveInvoiceSchedules(int invoiceNumber, InvoiceSchedule... invoiceSchedule);
	
	void deleteInvoiceSchedule(int invoiceNumber,String... dueDate);
	
	InvoiceFollowups getInvoiceFollowup(int invoiceNumber, long followupDate);
	
	void saveInvoiceFollowup(InvoiceFollowups followup);
	
	void deleteInvoiceFollowups(int invoiceNumber,String... followupDate);
	
	Iterable<InvoiceSearchBean> queryInvoice(FilterItem filterItem);
}
