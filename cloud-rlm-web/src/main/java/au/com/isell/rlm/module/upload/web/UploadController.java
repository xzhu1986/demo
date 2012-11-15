package au.com.isell.rlm.module.upload.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.util.ContentType;
import au.com.isell.common.util.WebUtils;
import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.module.upload.service.FileService;

/**
 * @author frankw 22/03/2012
 */
@Controller
@RequestMapping(value=ModulePath.UPLOAD)
public class UploadController {
	@Autowired
	private FileService uploadService;
	
	@RequestMapping(method=RequestMethod.GET)
	public String getUploadPage(HttpServletRequest request,String contentType,String callbackUrl,ModelMap modelMap) {
		ContentType type=null;
		if(StringUtils.isNotBlank(contentType)){
			type = (ContentType)BeanUtils.getEnum(ContentType.class, contentType);
		}
		if(callbackUrl!=null && !callbackUrl.startsWith("http")){
			callbackUrl=WebUtils.getBasePath(request)+callbackUrl;
		}
		modelMap.put("data", uploadService.constructFileBean(type,callbackUrl));
		return "module/upload/upload";
	}
	
	@RequestMapping(value="/info",method=RequestMethod.GET)
	public Result getUploadPage(String key) {
		return new Result(key);
	}
}
