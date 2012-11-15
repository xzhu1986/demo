package au.com.isell.rlm.common.web.error;

public class ErrorMsg {
	private String statusCode;
	private String msgCode;
	private Object[] msgParams;
	private String type;
	private String message;
	private String uri;
	private Throwable throwable;

	public ErrorMsg(String statusCode,String msgCode, Object[] msgParams, String type, String message, String uri, Throwable throwable) {
		super();
		this.msgCode = msgCode;
		this.msgParams = msgParams;
		this.type = type;
		this.message = message;
		this.uri = uri;
		this.throwable = throwable;
		this.statusCode=statusCode;
	}

	public String getMsgCode() {
		return msgCode;
	}

	public Object[] getMsgParams() {
		return msgParams;
	}

	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public String getUri() {
		return uri;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getStatusCode() {
		return statusCode;
	}

}
