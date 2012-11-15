package au.com.isell.rlm.module.email.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.module.mail.domain.EmailTargetType;
import au.com.isell.rlm.module.mail.service.EmailTemplateService;

/**
 * @author frankw 24/05/2012
 */
@Controller
@RequestMapping(value = ModulePath.AGENT_EMAIL_TEMPLATE)
public class EmailTemplateController {
	@Autowired
	private EmailTemplateService emailTemplateService;
	
	@RequestMapping(value = "/filter", method = RequestMethod.GET)
	public Result detail(String query, HttpServletRequest request, ModelMap modelMap,String target) {
		Assert.hasText(target,"target should not be null");
		
		return new Result(emailTemplateService.queryNotifyEmailTemplates(query,target));
	}
	@RequestMapping(value = "/targetTypeParams/{target}", method = RequestMethod.GET)
	public Result getTargetTypeParams(@PathVariable String target, HttpServletRequest request, ModelMap modelMap) {
		EmailTargetType type=(EmailTargetType)BeanUtils.getEnum(EmailTargetType.class, target);
		return new Result(type.getParameterDef());
	}
	@RequestMapping(value = "/detail/{typeId}", method = RequestMethod.GET)
	public Result getEmailTemplate(@PathVariable String typeId, HttpServletRequest request, ModelMap modelMap) {
		return new Result(emailTemplateService.getEmailTpl(typeId));
	}
	
	
}
