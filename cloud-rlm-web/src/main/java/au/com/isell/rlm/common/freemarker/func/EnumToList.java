package au.com.isell.rlm.common.freemarker.func;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * use enum class name to generate a list like [{key:0,name:Type},{..}] <br>
 * arg1: enum class name <br>
 * arg2: if use ordinal as key <br>
 * arg3: if use 1 based ordinal  <br>
 * if need to display different name ,add method: String display(){ ...}
 * 
 * @author frankw 09/02/2012
 */
public class EnumToList implements TemplateMethodModel {

	@Override
	public Object exec(List args) throws TemplateModelException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			Class c = EnumHelper.getEnumClass(((String) args.get(0)).trim());
			Object[] enums = c.getEnumConstants();
			boolean useOridnalAsKey = testKeyUsage(args);
			boolean oneBasedOrdinal = testOneBasedOrdinal(args);
			for (Object en : enums) {
				Enum e = (Enum) en;
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("key", useOridnalAsKey ? (oneBasedOrdinal ? e.ordinal() + 1 : e.ordinal()) : e.name());

				String name = EnumHelper.getDisplayCodeOrMsg(c, e);
				map.put("name", name);
				list.add(map);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return list;
	}

	private boolean testKeyUsage(List args) {
		boolean useOridnalAsKey = false;
		if (args.size() > 1 && Boolean.TRUE.toString().equals(args.get(1))) {
			useOridnalAsKey = true;
		}
		return useOridnalAsKey;
	}

	private boolean testOneBasedOrdinal(List args) {
		boolean r = false;
		if (args.size() > 2 && Boolean.TRUE.toString().equals(args.get(2))) {
			r = true;
		}
		return r;
	}

}