package au.com.isell.rlm.module.user.domain;

import java.util.Date;
import java.util.UUID;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.model.AbstractModel;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath(value = "data/users/reset/${hash}.json")
@XStreamAlias("reset-passwd")
public class ResetPasswordRequest extends AbstractModel {
	private String hash;
	private UUID userId;
	private Date expireDate;

	@IsellPathField
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
}
