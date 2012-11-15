package au.com.isell.rlm.module.user.domain;

import java.util.Date;
import java.util.UUID;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.model.AbstractModel;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/users/${userId}/${loginDate}.info.json")
@XStreamAlias("login-history")
public class LoginHistory extends AbstractModel {
	private UUID userId;
	private Date loginDate;
	private boolean success;
	private String ipAddress;
	private String comments;
	@IsellPathField
	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	@IsellPathField
	public Date getLoginDate() {
		return loginDate;
	}
	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
}
