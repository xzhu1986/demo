package au.com.isell.rlm.module.agent.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.xml.JsonUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.agent.dao.AgentDao;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.AgentEmailTemplate;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.domain.AgentRegion.AgentRegionStatus;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.agent.service.AgentService;
import au.com.isell.rlm.module.agent.vo.AgentSearchVals;
import au.com.isell.rlm.module.report.constant.ReportFormat;
import au.com.isell.rlm.module.report.constant.ReportPath;
import au.com.isell.rlm.module.report.constant.ReportType;
import au.com.isell.rlm.module.report.service.ReportService;
import au.com.isell.rlm.module.user.dao.UserDAO;

import com.amazonaws.util.json.JSONArray;

/**
 * @author frankw 10/02/2012
 */
@Service
public class AgentServiceImpl implements AgentService {
	private Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);
	@Autowired
	private AgentDao agentDao;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private ReportService reportService;

	@Override
	public List<Agent> getAgencyList() {
		return agentDao.searchAgents(agentDao.getAgentMaker().makeAllQuery(), null, null).getValue();
	}

	// @Override
	// public List<Agent> getAgentList(String countryCode) {
	// FilterMaker maker=agentDao.getAgentMaker();
	// return agentDao.searchAgents(maker.makeNameFilter("country", TextMatchOption.Is, countryCode), null,null
	// ).getValue();
	// }

	@Override
	public Pair<Long, List<Agent>> search(AgentSearchVals searchVals, Integer pageSize, Integer pageNo) {
		Assert.notNull(pageSize, "pageSize should not be null");
		Assert.notNull(pageNo, "pageNo should not be null");
		FilterMaker maker = agentDao.getAgentMaker();
		Pair<Long, List<Agent>> r = agentDao.searchAgents(searchVals.getFilter(maker), pageSize, pageNo);
		return r;
	}

	@Override
	public Agent getAgent(String agentId) {
		return agentDao.getAgent(UUID.fromString(agentId));
	}

	@Override
	public UUID saveAgent(Agent agent) {
		UUID id = agent.getAgentId();
		if (id == null) {
			id = UUID.randomUUID();
			agent.setAgentId(id);
			agentDao.saveAgent(agent);
		} else {
			Agent dbAgent = agentDao.getAgent(agent.getAgentId());
			BeanUtils.copyPropsExcludeNull(dbAgent, agent);
			agentDao.saveAgent(dbAgent);
		}
		return id;
	}

	@Override
	public UUID saveAgentUser(AgentUser formUser, String permissionDatas) {
		Assert.notNull(formUser.getAgentId());
		Agent agent = agentDao.getAgent(formUser.getAgentId());
		formUser.setAgentName(agent.getName());
		if (StringUtils.isNotBlank(permissionDatas)) {
			formUser.getPermissions().clear();
			try {
				JSONArray permArray=new JSONArray(permissionDatas);
				for(int i=0;i<permArray.length();i++){
					formUser.addPermission(permArray.getString(i));
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return userDAO.saveUser(formUser);
	}

	@Override
	public List<AgentRegion> getAgentRegions(String agentId) {
		FilterMaker maker = agentDao.getAgentRegionMaker();
		ESFilterItem item = (ESFilterItem) maker.makeNameFilter("agentId", TextMatchOption.Is, agentId);
		return agentDao.getAgentRegions(item);
	}

	@Override
	public void deleteAgentRegions(String agentId, String regionCodes) {
		List<String> regionCodeArr = JsonUtils.decode(regionCodes, List.class);
		if (CollectionUtils.isNotEmpty(regionCodeArr)) {
			for (String regionCode : regionCodeArr) {
				agentDao.deleteAgentRegion(agentId, regionCode);
			}
		}
	}

	@Override
	public void saveAgentRegion(AgentRegion formRegion, String oldRegionCode) {
		Assert.notNull(formRegion.getAgentId());
		Assert.notNull(formRegion.getRegionCode());
		UUID agentId = formRegion.getAgentId();

		if (StringUtils.isNotBlank(oldRegionCode)) {
			AgentRegion oldRegion = agentDao.getAgentRegion(agentId, oldRegionCode);
			if (oldRegion.getStatus() == AgentRegionStatus.PLANNING && formRegion.getStatus() == AgentRegionStatus.ESTABLISHED) {
				formRegion.setEstablishedDate(new Date());
			}

			if (oldRegion.getStatus() != AgentRegionStatus.STOPPED && formRegion.getStatus() == AgentRegionStatus.STOPPED) {
				formRegion.setStoppedDate(new Date());
			}
			BeanUtils.copyPropsExcludeNull(oldRegion, formRegion);
			formRegion = oldRegion;
			if (!oldRegionCode.equals(formRegion.getRegionCode())) {
				agentDao.deleteAgentRegion(agentId.toString(), oldRegionCode);
			}
		}
		agentDao.saveAgentRegion(formRegion);
	}

	@Override
	public AgentRegion getAgentRegion(UUID agentId, String regionCode) {
		return agentDao.getAgentRegion(agentId, regionCode);
	}

	@Override
	public AgentEmailTemplate getAgentEmailTemplate(UUID agentId, AgentEmailTemplate.Type type) {
		return agentDao.getAgentEmailTemplate(agentId, type);
	}

	@Override
	public void saveAgentEmailTemplate(AgentEmailTemplate emailTemplate) {
		agentDao.saveAgentEmailTemplate(emailTemplate);
	}

	@Override
	public void uploadAgentReportTpl(MultipartFile file, ReportType reportType, UUID agentId) {
		ReportPath reportPath = new ReportPath(reportType,ReportFormat.PDF);
		reportPath.addParam("agentId", agentId.toString());
		try {
			String tplName = reportPath.getTplName();
			logger.info("upload report template to {}", tplName);
			reportService.uploadTpl(file.getInputStream(), tplName);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void downloadTpl(ReportType reportType, UUID agentId, OutputStream outputStream) {
//		if (reportType == ReportType.Invoice) {
			ReportPath reportPath = new ReportPath(reportType,ReportFormat.PDF);
			reportPath.addParam("agentId", agentId.toString());
			reportService.downloadTpl(reportPath.getTplName(), outputStream);
//		}else{
//			throw new RuntimeException("No report type matches");
//		}
	}

	@Override
	public List<Map> getReportTplUploadFormDatas(UUID agentId) {
		List<Map> list = new ArrayList<Map>();
		ReportPath reportPath = new ReportPath(ReportType.Invoice,ReportFormat.PDF);
		reportPath.addParam("agentId", agentId.toString());
		list.add(getTplFormData(agentId, reportPath, "Invoice - Reseller"));
				
		reportPath = new ReportPath(ReportType.InvoiceDirectDebitBank,ReportFormat.PDF);
		reportPath.addParam("agentId", agentId.toString());
		list.add(getTplFormData(agentId, reportPath, "Direct Debit - Bank"));
		
		reportPath = new ReportPath(ReportType.InvoiceDirectDebitCreditCard,ReportFormat.PDF);
		reportPath.addParam("agentId", agentId.toString());
		list.add(getTplFormData(agentId, reportPath, "Direct Debit - Credit Card"));

		reportPath = new ReportPath(ReportType.InvoiceForcast12Month,ReportFormat.PDF);
		reportPath.addParam("agentId", agentId.toString());
		list.add(getTplFormData(agentId, reportPath, "Report - 12 Month Forecast"));
		
		reportPath = new ReportPath(ReportType.InvoiceAgentInvoiceSummary,ReportFormat.PDF);
		reportPath.addParam("agentId", agentId.toString());
		list.add(getTplFormData(agentId, reportPath, "Report - Invoice Summary"));
		
		reportPath = new ReportPath(ReportType.InvoicePaymentsDueSixWeeks,ReportFormat.PDF);
		reportPath.addParam("agentId", agentId.toString());
		list.add(getTplFormData(agentId, reportPath, "Report - Invoice Payments Due"));
		
		return list;
	}

	private Map getTplFormData(UUID agentId, ReportPath reportPath, String displayName) {
		Map m = new HashMap();
		m.put("title", displayName);
		m.put("type", reportPath.getType().toString());
		m.put("refreshDate", reportService.getLastTplUpdateDate(reportPath.getTplName()));
		return m;
	}

}
