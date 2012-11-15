package au.com.isell.rlm.common.exception;

import au.com.isell.rlm.common.utils.I18NUtils;

/**
 * Throw this kind of exception if need to give client messages.Support both ajax
 * or none ajax way.
 * 
 * @author frankw 20/03/2012
 */
public class BizException extends RuntimeException {
	private static final long serialVersionUID = 3760597868772422941L;

	private String msgCode;
	private String[] params;
	/**
	 * use i18n message code
	 */
	public BizException(String msgCode, String... params) {
		super(I18NUtils.getMsg(msgCode, params));
		this.msgCode = msgCode;
		this.params = params;
	}
	
	public BizException(String msgCode,Throwable cause, String... params) {
		super(I18NUtils.getMsg(msgCode, params),cause);
		this.msgCode = msgCode;
		this.params = params;
	}

	public String getMsgCode() {
		return msgCode;
	}

	public String[] getParams() {
		return params;
	}


}
