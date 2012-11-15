package au.com.isell.rlm.common.freemarker.func;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * show enum display value : return i18n key if exists
 * @author frankw 31/03/2012
 */
public class EnumDislplayValue implements TemplateMethodModel {

	@Override
	public Object exec(List args) throws TemplateModelException {
		String enumClsName=((String) args.get(0)).trim();
		String enumKey=((String) args.get(1)).trim();
		if(StringUtils.isEmpty(enumKey)) return "";
		Assert.notNull(enumClsName, "enum class name can not be null");
		Assert.notNull(enumKey, "enum key can not be null");
		Class<?> c=EnumHelper.getEnumClass(enumClsName);
		return EnumHelper.getDisplayCodeOrMsg(c, (Enum)BeanUtils.getEnum(c, enumKey));
	}

}