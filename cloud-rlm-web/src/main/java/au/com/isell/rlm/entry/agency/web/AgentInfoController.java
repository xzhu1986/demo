package au.com.isell.rlm.entry.agency.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.module.agent.service.AgentService;

@Controller
@RequestMapping(value = EntryPath.AGENT + "/info/")
public class AgentInfoController {
	@Autowired
	private AgentService agentService;

	@RequestMapping(value = "/{id}")
	public Result getAgentInfo(@PathVariable String id) {
		return new Result(agentService.getAgent(id));
	}
}
