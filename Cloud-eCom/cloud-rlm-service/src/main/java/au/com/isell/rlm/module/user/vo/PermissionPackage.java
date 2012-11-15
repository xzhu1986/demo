package au.com.isell.rlm.module.user.vo;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("permissionPackage")
public class PermissionPackage implements Comparable<PermissionPackage> {
	@XStreamAsAttribute
	private String display;
	@XStreamAsAttribute
	private Integer orderNo;
	@XStreamImplicit
	private List<PermissionGroup> permissionGroups;
	@XStreamOmitField
	@JsonIgnore
	private String permDefineFileName;

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public List<PermissionGroup> getPermissionGroups() {
		return permissionGroups;
	}

	public void setPermissionGroups(List<PermissionGroup> permissionGroups) {
		this.permissionGroups = permissionGroups;
	}

	public String getPermDefineFileName() {
		return permDefineFileName;
	}

	public void setPermDefineFileName(String permDefineFileName) {
		this.permDefineFileName = permDefineFileName;
	}

	@Override
	public int compareTo(PermissionPackage o) {
		return (getOrderNo() == null || o.getOrderNo() == null) ? 0 : getOrderNo().compareTo(o.getOrderNo());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((permDefineFileName == null) ? 0 : permDefineFileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PermissionPackage other = (PermissionPackage) obj;
		if (permDefineFileName == null) {
			if (other.permDefineFileName != null)
				return false;
		} else if (!permDefineFileName.equals(other.permDefineFileName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PermissionPackage [display=" + display + ", orderNo=" + orderNo + ", permissionGroups=" + permissionGroups + ", permDefineFileName="
				+ permDefineFileName + "]";
	}

	

}
