package au.com.isell.rlm.module.invoice.domain;

import java.util.Date;
import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

@XStreamAlias("inv-followup")
@IsellPath(value = "data/invoices/${invoiceNumber}/followups/${followupDate}.info.json")
@ISellIndex(name = "invoices", type = "followups")
public class InvoiceFollowups extends AbstractModel {

	public static enum FollowupResult {
		@EnumMsgCode("invoice.followup.result.emailed")
		Emailed, 
		@EnumMsgCode("invoice.followup.result.phoned_message")
		Phoned_Message,
		@EnumMsgCode("invoice.followup.result.phoned_unsuccess")
		Phoned_Unsuccess, 
		@EnumMsgCode("invoice.followup.result.phoned_success")
		Phoned_Success
	}

	private int invoiceNumber;
	private UUID userId;
	private Date followupDate;
	private FollowupResult results;
	private String additionalNotes;
	private String emailSubject;
	private String emailBody;
	
	@ISellIndexKey
	public String getId() {
		return invoiceNumber + "_" + (followupDate.getTime()/1000);
	}

	@IsellPathField
	@ISellIndexValue
	public int getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(int invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	@IsellPathField
	@ISellIndexValue
	public Date getFollowupDate() {
		return followupDate;
	}

	public void setFollowupDate(Date followupDate) {
		this.followupDate = followupDate;
	}

	public FollowupResult getResults() {
		return results;
	}

	public void setResults(FollowupResult results) {
		this.results = results;
	}

	public String getAdditionalNotes() {
		return additionalNotes;
	}

	public void setAdditionalNotes(String additionalNotes) {
		this.additionalNotes = additionalNotes;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}
}
