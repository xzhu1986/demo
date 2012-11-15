package au.com.isell.rlm.module.invoice.service;

import java.util.List;
import java.util.Map;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.invoice.domain.Invoice;
import au.com.isell.rlm.module.invoice.domain.InvoiceFollowups;
import au.com.isell.rlm.module.invoice.domain.InvoiceSchedule;
import au.com.isell.rlm.module.invoice.domain.InvoiceSearchBean;
import au.com.isell.rlm.module.invoice.vo.InvoiceAutoSchedulesDesc;
import au.com.isell.rlm.module.invoice.vo.InvoiceSearchVals;
import au.com.isell.rlm.module.mail.vo.MailPreview;
import au.com.isell.rlm.module.report.constant.ReportType;

public interface InvoiceService {
	Invoice getInvoice(int invoiceNumber);

	List<InvoiceSchedule> getInvoiceSchedules(int invoiceNumber);

	List<InvoiceFollowups> getinInvoiceFollowups(int invoiceNumber);

	int saveInvoice(Invoice invoice);

	Pair<Long, List<InvoiceSearchBean>> query(InvoiceSearchVals searchVals, String filter, Integer pageSize, Integer pageNo);

	Pair<Long, List<InvoiceSearchBean>> query4Reseller(String filter, int serialNo);

	InvoiceSchedule getInvoiceSchedule(int invoiceNumber, long dueDate);

	void createInvoiceScheduleByAutoDesc(Integer invoiceNumber, InvoiceAutoSchedulesDesc autoScheduleDesc);

	InvoiceAutoSchedulesDesc createBlankAutoScheduleDesc(Integer invoiceNumber);

	void saveInvoiceSchedules(int invoiceNumber, InvoiceSchedule... invoiceSchedules);

	void deleteInvoiceSchedule(int invoiceNumber, String... dueDate);

	InvoiceFollowups getInvoiceFollowup(int invoiceNumber, long followupDate);

	void saveInvoiceFollowup(InvoiceFollowups followup);

	void deleteInvoiceFollowups(int invoiceNumber, String... followupDate);

	Invoice initInvoice4Create(Invoice invoice,boolean addAmountFromReseller);

	MailPreview getInvoiceMailPreview(Integer invoiceNumber, String emailTplType);

	void sendInvoiceMail(Integer invoiceNumber, String emailTplType, MailPreview mailPreview);
	void sendInvoiceStatisticMail( ReportType type , MailPreview mailPreview);

	String getInoviceReportData(int invoiceNumber);

	String previewReport(Integer invoiceNumber,ReportType type );

	boolean hasInvoice(int serialNo);
	

	Integer quickCreateInvoice(Map nextRenewal, Map lics, Integer serialNo,String businessType);

}
