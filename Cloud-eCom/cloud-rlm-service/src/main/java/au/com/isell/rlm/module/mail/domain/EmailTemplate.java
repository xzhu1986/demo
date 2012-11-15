package au.com.isell.rlm.module.mail.domain;

import java.util.Set;
import java.util.UUID;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@ISellIndex(name = "emails", type = "template")
@IsellPath("data/emails/template/${typeId}.info.json")
@XStreamAlias("email-template")
public class EmailTemplate extends AbstractModel {

	private UUID typeId;
	private String typeName;
	private String subject;
	private String defaultSender;
	private String body;
	private EmailTargetType target;

	private Set<String> attachments;

	@ISellIndexKey
	@IsellPathField
	public UUID getTypeId() {
		return typeId;
	}

	public void setTypeId(UUID typeId) {
		this.typeId = typeId;
	}
	@ISellIndexValue(wildcard=true)
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	@ISellIndexValue(wildcard=true)
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	@ISellIndexValue
	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Set<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<String> attachments) {
		this.attachments = attachments;
	}

	public void setTarget(EmailTargetType target) {
		this.target = target;
	}
	@ISellIndexValue
	public EmailTargetType getTarget() {
		return target;
	}
	
}
