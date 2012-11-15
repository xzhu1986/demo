package au.com.isell.rlm.module.upload.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.module.upload.service.FileService;

/**
 * @author frankw 22/03/2012
 */
@Controller
@RequestMapping(value=ModulePath.FILE)
public class FileController {
	@Autowired
	private FileService fileService;
	
	@RequestMapping(method=RequestMethod.GET)
	public String download(String key) {
		return "redirect:"+fileService.getFileUrl(key);
	}
	
	@RequestMapping(value="/delete",method=RequestMethod.GET)
	public Result delete(String key) {
		fileService.deleteFile(key);
		return new Result();
	}
}
