package au.com.isell.rlm.module.user.vo;

import java.util.Locale;
import java.util.TimeZone;

public class ClientInfo {
	private String ipAddress;
	private Locale locale;
	private String timezone;
	private String webappBaseUrl;

	public String getIpAddress() {
		return ipAddress;
	}

	public ClientInfo setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		return this;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getTimezone() {
		return timezone == null ? TimeZone.getDefault().toString() : timezone;// for non web container
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getWebappBaseUrl() {
		return webappBaseUrl;
	}

	public void setWebappBaseUrl(String webappBaseUrl) {
		this.webappBaseUrl = webappBaseUrl;
	}

}
