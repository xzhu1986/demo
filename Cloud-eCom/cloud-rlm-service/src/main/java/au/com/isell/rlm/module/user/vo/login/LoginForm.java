package au.com.isell.rlm.module.user.vo.login;

import org.apache.commons.lang.StringUtils;

public abstract class LoginForm {
	private String password;
    private String jumpto;
    private String remember;
    
    public abstract String getToken(); 
    
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getJumpto() {
		return jumpto;
	}
	public void setJumpto(String jumpto) {
		this.jumpto = jumpto;
	}

	public String getRemember() {
		return remember;
	}

	public void setRemember(String remember) {
		this.remember = remember;
	}
	
	public boolean shouldRemeber(){
		return StringUtils.isNotBlank(remember) &&( remember.equalsIgnoreCase("on")||remember.equalsIgnoreCase("true"));
	}
	
    
}
