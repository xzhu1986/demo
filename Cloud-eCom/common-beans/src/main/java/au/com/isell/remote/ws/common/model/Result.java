package au.com.isell.remote.ws.common.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * wrap data and msg ,then send to client
 * 
 * @author Frank Wu 23/09/2011
 */
@XStreamAlias("result")
public class Result implements Serializable{
	private static final long serialVersionUID = 6920105849582836361L;
	
	private String msg="Operation Finished";
	private boolean success = true;
	private Integer total;
	private Object data;

	public Result() {
		super();
		
	}

	public Result(String msg, boolean success) {
		super();
		this.msg = msg;
		this.success = success;
	}
	public Result(Object data) {
		this.data=data;
	}

	public Result(String msg, Integer total, Object data) {
		super();
		this.msg = msg;
		this.total = total;
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public Integer getTotal() {
		return total;
	}

	public Object getData() {
		return data;
	}

}
