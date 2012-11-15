package au.com.isell.epd.comparator;

import java.io.Serializable;
import java.util.Comparator;

public class IgnoreCaseComparator implements Comparator<String>,Serializable {
	private static final long serialVersionUID = -3720017654120470487L;

	@Override
	public int compare(String a, String b) {
		if(a==null && b==null) return 0;
		else if(a==null && b!=null) return -1;
		else if(a!=null && b==null) return 1;
		else return  a.compareToIgnoreCase(b);
	}
}
