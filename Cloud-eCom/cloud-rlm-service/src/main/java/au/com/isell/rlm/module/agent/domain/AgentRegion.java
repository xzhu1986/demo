package au.com.isell.rlm.module.agent.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.BeanInitable;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@ISellIndex(name = "agents", type = "region")
@IsellPath("data/agents/${agentId}/${regionCode}.info.json")
@XStreamAlias("agent-region")
public class AgentRegion  extends AbstractModel implements BeanInitable<AgentRegion> {
	public static enum AgentRegionStatus {
		@EnumMsgCode("agent.region.status.planning")
		PLANNING,
		@EnumMsgCode("agent.region.status.established")
		ESTABLISHED, 
		@EnumMsgCode("agent.region.status.stopped")
		STOPPED
	}

	private UUID agentId;
	private String regionCode;
	private String currency;
	private BigDecimal taxRate;
	private String taxName;
	private Date establishedDate;
	private Date stoppedDate;
	private AgentRegionStatus status;

	@ISellIndexKey
	public String getKey() {
		return agentId + "|" + regionCode;
	}

	@ISellIndexValue
	@IsellPathField
	public UUID getAgentId() {
		return agentId;
	}

	public void setAgentId(UUID agentId) {
		this.agentId = agentId;
	}

	@ISellIndexValue
	@IsellPathField
	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	@ISellIndexValue
	public Date getEstablishedDate() {
		return establishedDate;
	}

	public void setEstablishedDate(Date establishedDate) {
		this.establishedDate = establishedDate;
	}

	@ISellIndexValue
	public Date getStoppedDate() {
		return stoppedDate;
	}

	public void setStoppedDate(Date stoppedDate) {
		this.stoppedDate = stoppedDate;
	}

	@ISellIndexValue
	public AgentRegionStatus getStatus() {
		return status;
	}

	public void setStatus(AgentRegionStatus status) {
		this.status = status;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@ISellIndexValue
	public String getCurrency() {
		return currency;
	}

	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}

	@ISellIndexValue
	public BigDecimal getTaxRate() {
		return taxRate;
	}

	@Override
	public AgentRegion init() {
		status = AgentRegionStatus.PLANNING;
		return this;
	}

	public String getTaxName() {
		return taxName;
	}

	public void setTaxName(String taxName) {
		this.taxName = taxName;
	}

}
