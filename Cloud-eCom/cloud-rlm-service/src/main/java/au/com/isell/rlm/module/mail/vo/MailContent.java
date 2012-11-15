package au.com.isell.rlm.module.mail.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import au.com.isell.remote.common.model.Pair;

/**
 * 
 * @author frankw 17/04/2012
 */
public class MailContent {

	private String subject;
	private boolean htmlMail = true;
	private String body;
	private String bodyTemplate;

	private Map bodyTplData;
	private InternetAddress from;
	private List<InternetAddress> to;
	private List<InternetAddress> cc;

	private List<InternetAddress> bcc;

	private List<Pair<String, File>> attachments;

	public MailContent(String subject, String body, boolean htmlMail) {
		this.subject = subject;
		this.body = body;
		this.htmlMail = htmlMail;
	}

	/**
	 * freemarker template loccation
	 */
	public MailContent(String subject, String bodyTemplate, Map fillData) {
		this.subject = subject;
		this.bodyTemplate = bodyTemplate;
		this.bodyTplData = fillData;
	}

	private List<InternetAddress> convertArray(MailAddress... user) {
		if (user == null || user.length == 0)
			return null;
		List<InternetAddress> list = new ArrayList<InternetAddress>(user.length);
		for (MailAddress mailUser : user) {
			list.add(mailUser.getInternetAddress());
		}
		return list;
	}

	public List<Pair<String, File>> getAttachments() {
		return attachments;
	}

	public List<InternetAddress> getBcc() {
		return bcc;
	}

	public String getBody() {
		return body;
	}

	public String getBodyTemplate() {
		return bodyTemplate;
	}

	public Map getBodyTplData() {
		return bodyTplData;
	}

	public List<InternetAddress> getCc() {
		return cc;
	}

	public InternetAddress getFrom() {
		return from;
	}

	public String getSubject() {
		return subject;
	}

	public List<InternetAddress> getTo() {
		return to;
	}

	public boolean isHtmlMail() {
		return htmlMail;
	}

	public MailContent withAttachment(Pair<String, File>... attachment) {
		this.attachments = Arrays.asList(attachment);
		return this;
	}

	public MailContent withBcc(MailAddress... bcc) {
		this.bcc = convertArray(bcc);
		return this;
	}

	public MailContent withCc(MailAddress... cc) {
		this.cc = convertArray(cc);
		return this;
	}

	public MailContent withFrom(MailAddress from) {
		this.from = from.getInternetAddress();
		return this;
	}

	public MailContent withTo(MailAddress... to) {
		this.to = convertArray(to);
		return this;
	}
}
