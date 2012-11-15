package au.com.isell.rlm.module.user.vo.login;


public class AgentLoginForm extends LoginForm{
	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getToken() {
		return username;
	}
	
}
