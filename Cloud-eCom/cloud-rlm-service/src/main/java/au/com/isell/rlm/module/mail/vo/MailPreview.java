package au.com.isell.rlm.module.mail.vo;

import java.util.ArrayList;
import java.util.List;


public class MailPreview {
	public static String mailSeperator=",";
	
	private String subject;
	private String body;
	private String to;
	private String cc;
	private String bcc;

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

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		if(to==null) return ;
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		if(cc==null) return ;
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		if(bcc==null) return ;
		this.bcc = bcc;
	}

	public static MailAddress[] splitAddress(String rawMailAddrs) {
		if (rawMailAddrs == null)
			return null;
		List<MailAddress> addresses = new ArrayList<MailAddress>();
		for (String addr : rawMailAddrs.split(MailPreview.mailSeperator)) {
			if(addr==null) continue;
			addresses.add(new MailAddress(addr, null));
		}
		return addresses.toArray(new MailAddress[addresses.size()]);
	}
}
