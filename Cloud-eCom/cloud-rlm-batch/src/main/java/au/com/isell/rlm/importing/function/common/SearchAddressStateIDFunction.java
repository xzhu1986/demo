package au.com.isell.rlm.importing.function.common;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.isell.common.util.StringUtils;
import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.utils.SpringUtils;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.address.service.impl.AddressServiceImpl;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class SearchAddressStateIDFunction extends BaseOperation<Map<String, String>> implements Function<Map<String, String>> {
	private static final long serialVersionUID = 2913729735141112366L;

	@ConstructorProperties({ "fieldDeclaration" })
	public SearchAddressStateIDFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);
	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<Map<String, String>> operationCall) {
		Map<String, String> map = new HashMap<String, String>();
		AddressService addressService = (AddressService) SpringUtils.getBean("addressServiceImpl", AddressServiceImpl.class);
		List<AddressItem> parents = addressService.getSubItems("root");
		List<AddressItem> childs = null;
		if (parents != null) {
			for (AddressItem parent : parents) {
				childs = addressService.getSubItems(parent.getCode());
				if (childs != null) {
					for (AddressItem child : childs) {
						map.put(parent.getCode().toLowerCase() + "_" + child.getShortName(), child.getCode());
					}
				}
			}
		}
		operationCall.setContext(map);
	}

	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<Map<String, String>> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		Tuple output = new Tuple();
		String countryCode = arguments.getString(0);
		output.add(countryCode);
		
		String key = null;
		for(int i=1;i<arguments.size();i++){
			key = arguments.getString(i);
			if (StringUtils.isEmpty(countryCode)) {
				output.add(null);
			}else if("64".equals(countryCode) || "nz".equals(countryCode.toLowerCase())){//nz
				output.add("7d16a9b0-7492-11e1-b0c4-0800200c9a66");
			}else {
				if(StringUtils.isEmpty(key)){
					output.add(null);
				}else{
					if(StringUtils.validNumber(countryCode)){
						output.add(functionCall.getContext().get(Constants.CountryMap.get(countryCode) + "_" + key.toUpperCase()));
					}else{
						output.add(functionCall.getContext().get(countryCode.toLowerCase() + "_" + key.toUpperCase()));
					}
				}
				
			}
		}
		
		functionCall.getOutputCollector().add(output);
	}
}
