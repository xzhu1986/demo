package au.com.isell.rlm.entry.reseller.web;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.EntryPath;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.jbe.service.JobService;
import au.com.isell.rlm.module.jbe.vo.JobAddBean;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.user.service.UserService;

@Controller
@RequestMapping(value = EntryPath.RESELLER + "/jobs")
public class RsJobController {
	@Autowired
	private JobService jobService;
	@Autowired
	private UserService userService;

	@RequiresPermissions({ "resellerportal:create_edit_job"})
	@RequestMapping(value = "/create",method=RequestMethod.GET)
	public String create(ModelMap model) {
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		model.put("contact", user.getFirstName()+" "+user.getLastName());
		model.put("phone", user.getPhone());
		
		return "reseller-portal/jobs/resellerJob-add";
	}
	
	@RequiresPermissions({ "resellerportal:create_edit_job"})
	@RequestMapping(value = "/create",method=RequestMethod.POST)
	public String saveNewJob(ModelMap model,@ModelAttribute JobAddBean addBean) {
		String jobId = jobService.createJob(addBean);
		return String.format("redirect:/reseller-portal/jobs/%s/detail",jobId);
	}

	@RequiresPermissions({ "resellerportal:view_job"})
	@RequestMapping(value = "/{jobId}/detail")
	public String detail(ModelMap model, @PathVariable String jobId) {
		model.put("jobDetail", jobService.getDetail(jobId));
		model.put("tasks", jobService.getTasks(jobId));
		model.put("notes", jobService.getNotes(jobId));
		return "reseller-portal/jobs/resellerJobDetails";
	}
	
	@RequiresPermissions({ "resellerportal:view_job"})
	@RequestMapping(value = "/{jobId}/note",method=RequestMethod.GET)
	public String note(ModelMap model, @PathVariable String jobId) {
		model.put("jobId", jobId);
		return "reseller-portal/jobs/resellerJobNote-add";
	}
	
	@RequestMapping(value = "/{jobId}/note",method=RequestMethod.POST)
	public Result saveNote(ModelMap model, @PathVariable String jobId,String note) {
		jobService.createNote(jobId, note);
		return new Result();
	}

}
