package au.com.isell.rlm.module.user.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.module.user.init.AustralianAddress;
import au.com.isell.rlm.module.user.init.InitAgents;
import au.com.isell.rlm.module.user.init.InitSupplier;
import au.com.isell.rlm.module.user.init.InitUser;
import au.com.isell.rlm.module.user.init.NewZealandAddress;
import au.com.isell.rlm.module.user.init.PNGAddress;
import au.com.isell.rlm.module.user.init.UkraineAddress;
import au.com.isell.rlm.module.user.init.UnitedKingdomAddress;
import au.com.isell.rlm.module.user.init.UnitedStatesAddress;

//TODO Remove this class at product env
@Controller
@RequestMapping("/init")
public class InitController {

	@Autowired
	private AustralianAddress australianAddress;
	@Autowired
	private InitUser initUser;
	@Autowired
	private NewZealandAddress newZealandAddress;
	@Autowired
	private UnitedKingdomAddress unitedKingdomAddress;
	@Autowired
	private UnitedStatesAddress unitedStatesAddress;
	@Autowired
	private UkraineAddress ukraineAddress;
	@Autowired
	private PNGAddress pngAddress;
	@Autowired
	private InitAgents initAgents;
	@Autowired
	private InitSupplier initSupplier;;

	@RequestMapping(method = RequestMethod.GET)
	public Result init(HttpServletRequest request) {
		australianAddress.init();
		newZealandAddress.init();
		unitedKingdomAddress.init();
		unitedStatesAddress.init();
		ukraineAddress.init();
		pngAddress.init();
		initAgents.init();
		initUser.init();
		return new Result("Init finished. Make sure to remove this function at product enviroment!", true);
	}

	@RequestMapping(value="/index/{target}",method=RequestMethod.GET)
	public Result reindex(@PathVariable String target) {
		Assert.hasText(target);
		if (target.equals("user")) {
			initUser.reindex();
		}else if (target.equals("supplier")) {
			initSupplier.reindex();
		} else {
			throw new RuntimeException("not valid target");
		}
		return new Result("Index finished. Make sure to remove this function at product enviroment!", true);
	}
}
