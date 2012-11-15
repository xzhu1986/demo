package au.com.isell.rlm.module.agent.domain;

import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;

import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.constant.UserType;
import au.com.isell.rlm.module.user.domain.User;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("agent-user")
@ISellIndex(name = "users", type = "agent", checkParents=true)
public class AgentUser extends User implements BeanInitable<AgentUser>{
	private UUID agentId;
	private String jobPosition;
	@XStreamOmitField
	@JsonIgnore
	private transient String agentName;
	private String phone;
	private String mobile;
	private String altPhone;
	private boolean salesRepo;
	private int dataSkillLevel;
	
	public void setJobPosition(String jobPosition) {
		this.jobPosition = jobPosition;
	}

	@ISellIndexValue(wildcard=true)
	public String getJobPosition() {
		return jobPosition;
	}

	public AgentUser() {
		setType(UserType.Agent);
	}

	@ISellIndexValue
	public UUID getAgentId() {
		return agentId;
	}
	public void setAgentId(UUID agentId) {
		this.agentId = agentId;
	}
	@ISellIndexValue(wildcard=true)
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getAltPhone() {
		return altPhone;
	}
	public void setAltPhone(String altPhone) {
		this.altPhone = altPhone;
	}
	@Override
	public AgentUser init() {
		super.setStatus(UserStatus.ACTIVE);
		return this;
	}
	public void setSalesRepo(boolean salesRepo) {
		this.salesRepo = salesRepo;
	}
	@ISellIndexValue
	public boolean isSalesRepo() {
		return salesRepo;
	}

	public void setDataSkillLevel(int dataSkillLevel) {
		this.dataSkillLevel = dataSkillLevel;
	}

	public int getDataSkillLevel() {
		return dataSkillLevel;
	}

	@Override
	public String toString() {
		return "AgentUser [agentId=" + agentId + ", jobPosition=" + jobPosition + ", phone=" + phone + ", mobile=" + mobile + ", altPhone="
				+ altPhone + ", salesRepo=" + salesRepo + ", dataSkillLevel=" + dataSkillLevel + "]";
	}

	
}
