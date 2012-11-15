package au.com.isell.rlm.module.user.vo;

import java.util.List;

import au.com.isell.rlm.module.user.domain.Permission;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
@XStreamAlias("permissionGroup")
public class PermissionGroup {
	@XStreamAsAttribute
	private String display;
	
	/**
	 * mark sub permisson element as dynamic,support one param at most
	 */
	@XStreamAsAttribute
	private boolean dynamic;
	@XStreamAsAttribute
	private String dataService;
	@XStreamAsAttribute
	private String dataMethod;
	@XStreamAsAttribute
	private String dataParam;
	
	
	@XStreamImplicit
	private List<Permission> permissions;
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public List<Permission> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}
	public boolean isDynamic() {
		return dynamic;
	}
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	public String getDataService() {
		return dataService;
	}
	public void setDataService(String dataService) {
		this.dataService = dataService;
	}
	public String getDataMethod() {
		return dataMethod;
	}
	public void setDataMethod(String dataMethod) {
		this.dataMethod = dataMethod;
	}
	public String getDataParam() {
		return dataParam;
	}
	public void setDataParam(String dataParam) {
		this.dataParam = dataParam;
	}
	@Override
	public String toString() {
		return "PermissionGroup [display=" + display + ", dynamic=" + dynamic + ", dataService=" + dataService + ", dataMethod=" + dataMethod
				+ ", dataParam=" + dataParam + ", permissions=" + permissions + "]";
	}

	
	
}
