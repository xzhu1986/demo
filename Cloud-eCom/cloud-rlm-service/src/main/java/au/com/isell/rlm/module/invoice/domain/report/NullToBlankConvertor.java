package au.com.isell.rlm.module.invoice.domain.report;

public class NullToBlankConvertor {
	public static String convert(String v){
		return v==null?"":v;
	}
}
