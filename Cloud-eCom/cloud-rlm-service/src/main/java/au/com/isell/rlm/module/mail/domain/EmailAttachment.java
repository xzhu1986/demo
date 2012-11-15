package au.com.isell.rlm.module.mail.domain;

public class EmailAttachment {

	private String name;
	private String cloudPath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCloudPath() {
		return cloudPath;
	}

	public void setCloudPath(String cloudPath) {
		this.cloudPath = cloudPath;
	}

	public EmailAttachment() {
		super();

	}

	public EmailAttachment(String name, String cloudPath) {
		super();
		this.name = name;
		this.cloudPath = cloudPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cloudPath == null) ? 0 : cloudPath.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		EmailAttachment other = (EmailAttachment) obj;
		if (cloudPath == null) {
			if (other.cloudPath != null)
				return false;
		} else if (!cloudPath.equals(other.cloudPath))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}