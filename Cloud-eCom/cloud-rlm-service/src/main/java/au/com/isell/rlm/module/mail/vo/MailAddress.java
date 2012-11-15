package au.com.isell.rlm.module.mail.vo;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.InternetAddress;

import au.com.isell.rlm.module.mail.service.impl.MailServiceImpl;

/**
 * @author frankw 17/04/2012
 */
public class MailAddress {
	private String email;
	private String username;

	public MailAddress(String email, String username) {
		this.email = email;
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public InternetAddress getInternetAddress() {
		try {
			return new InternetAddress(email, username, MailServiceImpl.CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
