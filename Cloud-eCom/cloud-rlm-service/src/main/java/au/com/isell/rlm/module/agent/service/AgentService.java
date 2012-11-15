package au.com.isell.rlm.module.agent.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate;
import au.com.isell.rlm.module.agent.vo.AgentSearchVals;
import au.com.isell.rlm.module.report.constant.ReportType;

public interface AgentService {
	/* for freemarker invoking */
	List<Agent> getAgencyList();
	/* for freemarker invoking */
//	List<Agent> getAgentList(String countryCode);
	
	Pair<Long, List<Agent>> search(AgentSearchVals searchVals, Integer pageSize, Integer pageNo);

	Agent getAgent(String agentId);

	UUID saveAgent(Agent agent);

	UUID saveAgentUser(AgentUser formUser,String permissionDatas);
	
	List<AgentRegion> getAgentRegions(String agentId);
	
	AgentRegion getAgentRegion(UUID agentId,String regionCode);

	void deleteAgentRegions(String agentId, String regionCode);

	void saveAgentRegion(AgentRegion formRegion,String oldRegionCode);
	
//	List<AgentRegionDesc> getAgentRegionDescs(String agentId);
	
	AgentEmailTemplate getAgentEmailTemplate(UUID agentId, AgentEmailTemplate.Type type);
	
	void saveAgentEmailTemplate(AgentEmailTemplate emailTemplate);
	
	void uploadAgentReportTpl(MultipartFile file,ReportType reportType,UUID agentId) ;
	
	void downloadTpl(ReportType reportType, UUID agentId,OutputStream outputStream);
	
	List<Map> getReportTplUploadFormDatas(UUID agentId);
}
