package au.com.isell.rlm.module.agent.domain;

import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.module.report.constant.ReportType;

@IsellPath("data/agents/${agentId}/template/${type}.info.json")
@XStreamAlias("email-template")
public class AgentEmailTemplate extends AbstractModel {

	public static enum Type {
		@EnumMsgCode("tpl.mail.invoice")
		Inoivce{
			@Override
			public au.com.isell.rlm.module.report.constant.ReportType getReportPathType() {
				return ReportType.Invoice;
			}
		}, 
		@EnumMsgCode("tpl.mail.followup")
		FollowUp{
			@Override
			public au.com.isell.rlm.module.report.constant.ReportType getReportPathType() {
				return ReportType.Invoice;
			}
		}, 
		@EnumMsgCode("tpl.mail.final_notice")
		FinalNotice{
			@Override
			public au.com.isell.rlm.module.report.constant.ReportType getReportPathType() {
				return ReportType.Invoice;
			}
		}, 
		@EnumMsgCode("tpl.mail.demand")
		Demand{
			@Override
			public au.com.isell.rlm.module.report.constant.ReportType getReportPathType() {
				return ReportType.Invoice;
			}
		}, 
		@EnumMsgCode("tpl.mail.debit_bank")
		DirectDebitBank{
			@Override
			public au.com.isell.rlm.module.report.constant.ReportType getReportPathType() {
				return ReportType.InvoiceDirectDebitBank;
			}
		}, 
		@EnumMsgCode("tpl.mail.debit_credit_card")
		DirectDebitCreditCard{
			@Override
			public au.com.isell.rlm.module.report.constant.ReportType getReportPathType() {
				return ReportType.InvoiceDirectDebitCreditCard;
			}
		};
		
		public static AgentEmailTemplate.Type calEmailTplType(String type) {
			AgentEmailTemplate.Type emType;
			if ("Debit".equals(type)) {
				emType = AgentEmailTemplate.Type.DirectDebitBank;
			} else {
				emType = AgentEmailTemplate.Type.valueOf(type);
			}
			return emType;
		}
		
		public abstract ReportType getReportPathType();
	}

	private UUID agentId;
	private Type type;
	private String fromName;
	private String fromEmail;
	private String bcc;
	private String subject;
	private String body;

	@IsellPathField
	public UUID getAgentId() {
		return agentId;
	}

	public void setAgentId(UUID agentId) {
		this.agentId = agentId;
	}

	@IsellPathField
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
