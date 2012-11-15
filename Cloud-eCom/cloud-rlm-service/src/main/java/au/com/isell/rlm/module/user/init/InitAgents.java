package au.com.isell.rlm.module.user.init;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.agent.dao.AgentDao;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.agent.domain.Agent.Status;
import au.com.isell.rlm.module.agent.domain.AgentRegion;
import au.com.isell.rlm.module.agent.domain.AgentRegion.AgentRegionStatus;
import au.com.isell.rlm.module.agent.domain.InvoiceTermsTemplate;
import au.com.isell.rlm.module.system.dao.CurrencyDao;
@Component
public class InitAgents {
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private CurrencyDao currencyDao;
	@Autowired
	private AgentDao agentDao;
	
	private Agent initAgent() {
		Agent agent = new Agent();
		agent.setAddress(new GeneralAddress("Suite 12", "88 Pitt Street", "", "Sydney", "bd6fb480-7491-11e1-b0c4-0800200c9a66", "au", "2000"));
		agent.setAgentId(UUID.fromString("ae7b1620-7740-11e1-b0c4-0800200c9a66"));
		agent.setCompany("iSell Pty Ltd");
		agent.setName("iSell");
		agent.setStatus(Status.Active);
		agent.setCountry("au");
		InvoiceTermsTemplate invoiceTermsTemplate = new InvoiceTermsTemplate();
		invoiceTermsTemplate.setContent(InvoiceTermsTemplate.DEFAULE_INVOICE_TERMS);
		agent.setInvoiceTermsTemplate(invoiceTermsTemplate);
		agentDao.saveAgent(agent);
		return agent;
	}
	
	private AgentRegion initAgentRegion(UUID agentId, String regionCode, BigDecimal tax) {
		AgentRegion region = new AgentRegion();
		region.setAgentId(agentId);
		region.setCurrency("AUD");
		region.setRegionCode(regionCode);
		region.setTaxRate(new BigDecimal("10"));
		region.setStatus(AgentRegionStatus.ESTABLISHED);
		region.setEstablishedDate(new Date());
		return region;
	}
	
	public void init()  {
		try {
			Agent agent=initAgent();
			AgentRegion nsw = initAgentRegion(agent.getAgentId(), "bd6fb480-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion vic = initAgentRegion(agent.getAgentId(), "bd6fb481-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion qld = initAgentRegion(agent.getAgentId(), "bd6fb482-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion sa = initAgentRegion(agent.getAgentId(), "bd6fb483-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion wa = initAgentRegion(agent.getAgentId(), "bd6fb484-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion act = initAgentRegion(agent.getAgentId(), "bd6fb485-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion nt = initAgentRegion(agent.getAgentId(), "bd6fb486-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion tas = initAgentRegion(agent.getAgentId(), "bd6fb487-7491-11e1-b0c4-0800200c9a66", new BigDecimal("10"));
			AgentRegion north = initAgentRegion(agent.getAgentId(), "7d16a9b0-7492-11e1-b0c4-0800200c9a66", new BigDecimal("0"));
			AgentRegion south = initAgentRegion(agent.getAgentId(), "7d16a9b1-7492-11e1-b0c4-0800200c9a66", new BigDecimal("0"));
			agentDao.saveAgentRegion(nsw, vic, qld, sa, wa, act, nt, tas, north, south);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
