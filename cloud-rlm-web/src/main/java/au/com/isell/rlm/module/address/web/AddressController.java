package au.com.isell.rlm.module.address.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.constant.ModulePath;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;

/**
 * @author frankw 07/02/2012
 */
@Controller
@RequestMapping(value = ModulePath.AGENT_ADDRESS)
public class AddressController {
	@Autowired
	private AddressService addressService;
	
	
	@RequestMapping("/sub-addresses/{parentCode}")
	public Result subAddresses(@PathVariable String parentCode) {
		List<AddressItem> list=addressService.getSubItems(parentCode);
		return new Result("success", list!=null?list.size():0, list);
	}
	
	@RequestMapping("/address-html")
	public String addressHtml(String countryCode,String addrFieldName,ModelMap modelMap) {
		modelMap.put("countryCode", countryCode);
		modelMap.put("addrFieldName", addrFieldName);
		return "module/address/switch-address";
	}
	
	@RequestMapping("/address-item-cascading/{code}")
	public String addressCascadingHtml(@PathVariable String code,String addrFieldName,ModelMap modelMap) {
		List<String[]> codes = addressService.getAddressParents(code);
		modelMap.put("codes", codes);
		return "module/address/address-item-cascading";
	}
	
	@RequestMapping(value="/address-item/{code}",method=RequestMethod.GET)
	public Result getAddressItem(@PathVariable String code) {
		return new Result("ok", 1, addressService.getAddressItem(code));
	}
	
	@RequestMapping(value="/address-item/{code}",method=RequestMethod.DELETE)
	public Result delAddressItem(@PathVariable String code) {
		addressService.deleteAddressItem(code);
		return new Result("Operation Finished", true);
	}
}
