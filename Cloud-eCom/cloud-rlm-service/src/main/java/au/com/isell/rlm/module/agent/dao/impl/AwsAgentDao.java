package au.com.isell.rlm.module.agent.dao.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.agent.dao.AgentDao;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate.Type;
import au.com.isell.rlm.module.user.dao.UserDAO;

/**
 * @author frankw 13/02/2012
 */
@Repository
public class AwsAgentDao extends DAOSupport implements AgentDao {
	private static FieldMapper agentMapper = new FieldMapper();
	private static FieldMapper agentRegionMapper = new FieldMapper();
	@Autowired
	private UserDAO userDAO;
	private ESFilterMaker maker;
	
	private ESFilterMaker agentRegionMaker;
	
	static {
		agentMapper.addMap("name", "name_wildcard");
	}

	@Override
	public synchronized FilterMaker getAgentMaker() {
		if (maker != null) return maker;
		maker = new ESFilterMaker();
		maker.setType(Agent.class);
		maker.setFieldMapper(agentMapper);
		return maker;
	}

	@Override
	public Pair<Long, List<Agent>> searchAgents(FilterItem filterItem, Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0]=getAgentMaker().makeSortItem("name", true);
		return indexHelper.queryBeans(Agent.class,new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging( pageNo, pageSize));
	}

	@Override
	public Agent getAgent(UUID agentId) {
		Agent agent = new Agent();
		agent.setAgentId(agentId);
		return super.get(agent);
	}

	@Override
	public void saveAgent(Agent agent) {
		super.save(agent);
		// update agent user index
		updateAgentUserIndex(agent);
	}

	private void updateAgentUserIndex(Agent agent) {
		FilterMaker filterMaker = userDAO.getAgentUserMaker();
		FilterItem filterItem = filterMaker.makeNameFilter("agentId", TextMatchOption.Is, agent.getAgentId().toString());
		List<AgentUser> users = userDAO.queryUser(AgentUser.class, filterItem, null, null).getValue();
		if (users != null) {
			for (AgentUser user : users) {
				user.setAgentName(agent.getName());
				indexHelper.indexValues(user);
			}
		}
	}

	@Override
	public List<AgentRegion> getAgentRegions(ESFilterItem item) {
		return indexHelper.queryBeans(AgentRegion.class,new QueryParams(item.generateQueryBuilder(), null)).getValue();
	}

	@Override
	public void deleteAgentRegion(String agentId, String regionCode) {
		AgentRegion region = new AgentRegion();
		region.setAgentId(UUID.fromString(agentId));
		region.setRegionCode(regionCode);
		super.delete(region);
	}

	@Override
	public void saveAgentRegion(AgentRegion... region) {
		super.save(region);
	}

	@Override
	public AgentRegion getAgentRegion(UUID agentId, String regionCode) {
		AgentRegion agentRegion = new AgentRegion();
		agentRegion.setAgentId(agentId);
		agentRegion.setRegionCode(regionCode);
		return super.get(agentRegion);
	}

	@Override
	public AgentEmailTemplate getAgentEmailTemplate(UUID agentId, Type type) {
		AgentEmailTemplate emailTemplate = new AgentEmailTemplate();
		emailTemplate.setAgentId(agentId);
		emailTemplate.setType(type);
		return super.get(emailTemplate);
	}

	@Override
	public void saveAgentEmailTemplate(AgentEmailTemplate emailTemplate) {
		super.save(emailTemplate);
	}

	@Override
	public synchronized FilterMaker getAgentRegionMaker() {
		if (agentRegionMaker != null) return agentRegionMaker;
		agentRegionMaker = new ESFilterMaker();
		agentRegionMaker.setType(AgentRegion.class);
		agentRegionMaker.setFieldMapper(agentRegionMapper);
		return agentRegionMaker;
	}
}
