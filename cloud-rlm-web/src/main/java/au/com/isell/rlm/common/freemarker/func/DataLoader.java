package au.com.isell.rlm.common.freemarker.func;

import java.util.List;

import au.com.isell.rlm.common.utils.DataLoadUtils;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * use enum class name to generate a list like [{key:0,name:Type},{..}]
 * arg1: service class name
 * arg2: service method name
 * @author frankw 09/02/2012
 */
public class DataLoader implements TemplateMethodModel {
	@Override
	public Object exec(List args) throws TemplateModelException {
		String serviceClassName = (String)args.get(0);
		String methodClassName = (String)args.get(1);
		Object[] methodArgs=null;
		if(args.size()>2){
			methodArgs = args.subList(2, args.size()).toArray();
		}
		return DataLoadUtils.loadServiceData4Web(serviceClassName, methodClassName, methodArgs);
	}


}