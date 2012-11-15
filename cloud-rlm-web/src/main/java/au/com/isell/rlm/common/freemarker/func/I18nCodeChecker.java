package au.com.isell.rlm.common.freemarker.func;

import java.util.List;

import au.com.isell.rlm.common.utils.I18NUtils;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * test if i18n msg code exists in property file
 * 
 * @author frankw 09/04/2012
 */
public class I18nCodeChecker implements TemplateMethodModel {
	@Override
	public Object exec(List args) throws TemplateModelException {
		String msgCode = (String) args.get(0);
		return I18NUtils.exists(msgCode);
	}

}