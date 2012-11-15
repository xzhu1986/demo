package au.com.isell.rlm.module.user.vo.login;

import au.com.isell.rlm.module.user.domain.User;


public class ResellerLoginForm  extends LoginForm{
	private String serialNo;
	private String email;
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String getToken() {
		return serialNo+User.tokenSpliter+email;
	}

	
}
