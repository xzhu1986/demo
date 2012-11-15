package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;
import java.util.UUID;

public class ManualFile {
	private String name;
	private String path;
	private Date lastModified;
	private String size;
	private String desc;
	private String type;
	private UUID id;
	private UUID parentId;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public UUID getParentId() {
		return parentId;
	}
	public void setParentId(UUID parentId) {
		this.parentId = parentId;
	}
	
}
