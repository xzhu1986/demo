package au.com.isell.rlm.entry.agency.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.paging.Paging2;
import au.com.isell.common.util.UrlUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.module.agent.domain.AgentUser;
import au.com.isell.rlm.module.system.constant.SettingEntryPath;
import au.com.isell.rlm.module.system.service.SystemService;
import au.com.isell.rlm.module.user.service.UserService;
import au.com.isell.rlm.module.user.vo.AgentUserSearchVal;

@Controller
@RequestMapping(value = EntryPath.AGENT + "/settings/"+SettingEntryPath.users)
public class UserSettingController {
	private static final int DEFAULT_FIRST_PAGE = 1;
	private static final int DEFAULT_PAGE_SIZE = 25;
	@Autowired
	private SystemService systemService;
	@Autowired
	private UserService userService;

	@RequestMapping(method = RequestMethod.GET)
	public String index() {
		return String.format("forward:%s/settings/%s/agent", EntryPath.AGENT, SettingEntryPath.users);
	}

	@RequestMapping(value = "/agent")
	public String getCurrencies(ModelMap model, Integer pageSize, Integer pageNo,
			HttpServletRequest request) {
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		pageNo = pageNo == null ? DEFAULT_FIRST_PAGE : pageNo;
		AgentUserSearchVal searchVals = BeanUtils.constructBean(request, AgentUserSearchVal.class,	null);

		Map requestMap = UrlUtils.getRequestMap(request);
		model.putAll(requestMap);

		String urlfmt = "%s/settings/%s/agent?%s";
		Pair<Long, List<AgentUser>> result = userService.queryAgentUser(searchVals, pageSize, pageNo);
		Paging2 paging = new Paging2(
				String.format(urlfmt, EntryPath.AGENT, SettingEntryPath.users, UrlUtils.joinParam4Url(requestMap, "pageNo")), pageNo, pageSize,
				result.getKey().intValue());

		model.put("category", SettingEntryPath.users);
		model.put("data", result.getValue());
		model.put("paging", paging);
		return "agent/setting-terms/user-terms/agent-users";
	}


}
