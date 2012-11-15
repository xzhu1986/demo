package au.com.isell.rlm.module.user.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexRouting;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.supplier.domain.SupplierUser;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.constant.UserType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@ISellIndex(name = "users", type = "")
@IsellPath("data/users/${userId}/basic.info.json")
@XStreamAlias("user")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(value = { @Type(value = AgentUser.class, name = "AgentUser"), @Type(value = ResellerUser.class, name = "ResellerUser"),
		@Type(value = SupplierUser.class, name = "SupplierUser") })
public class User extends AbstractModel {
	public static final String tokenSpliter="___";

	private UUID userId;
	private UserType type;
	private String username;
	// SHA-256 digest
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private UserStatus status;
	private int loginFailureCount;
	private Set<String> permissions;

	public User() {
	}

	public User(UserType type) {
		setType(type);
	}
	
	@JsonIgnore
	@ISellIndexValue
	public String getToken(){
		return getUsername();
	}

	@ISellIndexKey
	@IsellPathField
	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	@ISellIndexValue
	public UserType getType() {
		return type;
	}

	public void setType(UserType type) {
		this.type = type;
	}

	@ISellIndexValue(wildcard = true)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@ISellIndexValue(wildcard = true)
	@JsonIgnore
	public String getName() {
		return firstName + " " + lastName;
	}
	@ISellIndexValue
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	@ISellIndexValue
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@ISellIndexValue
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@ISellIndexValue
	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	@ISellIndexValue
	public int getLoginFailureCount() {
		return loginFailureCount;
	}

	public void setLoginFailureCount(int loginFailureCount) {
		this.loginFailureCount = loginFailureCount;
	}

	@ISellIndexRouting
	@JsonIgnore
	public String getRouting() {
		return "isell";
	}

	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		User other = (User) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (loginFailureCount != other.loginFailureCount)
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (status != other.status)
			return false;
		if (type != other.type)
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	public Set<String> getPermissions() {
		return permissions == null ? new HashSet<String>() : permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	public void addPermission(String permission) {
		if (permissions == null) {
			permissions = new HashSet<String>();
		}
		permissions.add(permission);
	}

}
