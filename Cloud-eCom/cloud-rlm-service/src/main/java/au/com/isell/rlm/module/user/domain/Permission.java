package au.com.isell.rlm.module.user.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("permission")
public class Permission {
	@XStreamAsAttribute
	private String key;
	@XStreamAsAttribute
	private String display;
	@XStreamAsAttribute
	private String dependsOn;
	
	public Permission(String key, String display, String dependsOn) {
		super();
		this.key = key;
		this.display = display;
		this.dependsOn = dependsOn;
	}
	
	public Permission() {
		super();
		
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public String getDependsOn() {
		return dependsOn;
	}
	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}
	
	
}

