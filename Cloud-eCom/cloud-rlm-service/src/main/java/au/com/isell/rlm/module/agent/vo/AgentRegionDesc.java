package au.com.isell.rlm.module.agent.vo;

import java.util.UUID;

public class AgentRegionDesc {
	private UUID agentId;
	private String regionCode;
	private String nameDesc;

	public UUID getAgentId() {
		return agentId;
	}

	public void setAgentId(UUID agentId) {
		this.agentId = agentId;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getNameDesc() {
		return nameDesc;
	}

	public void setNameDesc(String nameDesc) {
		this.nameDesc = nameDesc;
	}

}
