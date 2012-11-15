package au.com.isell.ecom.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {
	@RequestMapping("/test")
	public String test(ModelMap model) {
		
		model.put("username", "user-"+Math.random());
		return "test";
	}
}
