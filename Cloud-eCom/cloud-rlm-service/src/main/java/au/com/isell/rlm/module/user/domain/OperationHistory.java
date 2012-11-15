package au.com.isell.rlm.module.user.domain;

import java.util.Date;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.user.constant.UserType;

@IsellPath("data/history/${id}.json")
@ISellIndex(name = "history", type = "basic")
public class OperationHistory {
	public enum TargetType {
		Reseller, Supplier;
	}

	private UUID id;
	private UUID userId;
	@JsonIgnore
	private String userName;
	private UserType userType;
	private String ipAddr;
	private Date date;
	private Object target;
	private String targetId;
	private TargetType targetType;
	
	public OperationHistory(Object target, String targetId, TargetType targetType) {
		super();
		this.target = target;
		this.targetId = targetId;
		this.targetType = targetType;
		User cUser = GlobalAttrManager.getCurrentUser();
		setUserId(cUser.getUserId());
		setUserName(cUser.getUsername());
		setUserType(cUser.getType());
		setDate(new Date());
		setIpAddr(GlobalAttrManager.getClientInfo().getIpAddress());
	}

	public OperationHistory() {
		super();
		
	}

	@IsellPathField
	@ISellIndexKey
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@ISellIndexValue
	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	@ISellIndexValue
	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	@ISellIndexValue
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@JsonIgnore
	public <T> T getTargetWithType(Class<T> cls) {
		return target!=null? DAOSupport.getMapper().convertValue(target, cls):null;
	}
	
	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	@IsellPathField
	@ISellIndexValue
	public TargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(TargetType targetType) {
		this.targetType = targetType;
	}

	@ISellIndexValue
	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	@ISellIndexValue
	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	@ISellIndexValue
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


}

