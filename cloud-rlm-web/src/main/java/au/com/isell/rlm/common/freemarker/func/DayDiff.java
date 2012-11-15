package au.com.isell.rlm.common.freemarker.func;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import au.com.isell.common.bean.DateValueConverter;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * date1+appendVal(days) - date2= days
 * 
 * @author frankw 21/02/2012
 */
public class DayDiff implements TemplateMethodModel {

	@Override
	public Object exec(List args) throws TemplateModelException {
		String para1 = (String) args.get(0);
		String para2 = (String) args.get(1);
		String para3 = (String) args.get(2);

		Date d1 = DateUtils.addDays((Date) (new DateValueConverter().convert(Date.class, para1)), Integer.valueOf(para2));
		Date d2 = (Date) (new DateValueConverter().convert(Date.class, para3));
		boolean gt = d1.compareTo(d2) > 0 ? true : false;
		long betweenDays = (long) ((d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24.0) + (gt ? 0.5 : -0.5));
		return betweenDays;
	}

}