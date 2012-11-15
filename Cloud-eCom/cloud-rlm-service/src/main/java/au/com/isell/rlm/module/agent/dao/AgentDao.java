package au.com.isell.rlm.module.agent.dao;

import java.util.List;
import java.util.UUID;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate;

public interface AgentDao {
	Pair<Long, List<Agent>> searchAgents(FilterItem filterItem, Integer pageSize, Integer pageNo);

	FilterMaker getAgentMaker();
	
	FilterMaker getAgentRegionMaker();

	Agent getAgent(UUID agentId);

	void saveAgent(Agent agent);
	 
	 List<AgentRegion> getAgentRegions(ESFilterItem item) ;
	 
	 void deleteAgentRegion(String agentId, String regionCode) ;
	 
	 void saveAgentRegion(AgentRegion... region);
	 
	 AgentRegion getAgentRegion(UUID agentId, String regionCode);
	 
	 AgentEmailTemplate getAgentEmailTemplate(UUID agentId, AgentEmailTemplate.Type type);
	 
	 void saveAgentEmailTemplate(AgentEmailTemplate emailTemplate);
}
