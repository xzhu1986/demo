package au.com.isell.rlm.module.user.vo;

import java.util.UUID;

public class ResetPwdBean {
	private String identityName;
	private UUID userId;
	private String email;

	public String getIdentityName() {
		return identityName;
	}

	public void setIdentityName(String identityName) {
		this.identityName = identityName;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ResetPwdBean(String identityName, UUID userId, String email) {
		super();
		this.identityName = identityName;
		this.userId = userId;
		this.email = email;
	}

}
