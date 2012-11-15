package au.com.isell.epd.eunomia.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.isell.remote.common.model.Pair;

public final class Status {
	/**
	 * Active. This status can be used to generate specification or searchable.
	 */
	public static final int STATUS_ACTIVE=0;
	/**
	 * End of Life. Not in use anymore.
	 */
	public static final int STATUS_EOL=1;
	/**
	 * Preparing. Not in use yet.
	 */
	public static final int STATUS_PREPARING=2;
	/**
	 * Ignored. No use for current business. No need to spend time on that.
	 */
	public static final int STATUS_IGNORE=3;
	/**
	 * Program used status.
	 */
	public static final int STATUS_CHECK=99;

	public static final List<Pair<Integer, String>> statusList;
	
	static {
		List<Pair<Integer, String>> list = new ArrayList<Pair<Integer,String>>();
		list.add(new Pair<Integer, String>(STATUS_ACTIVE, "Active"));
		list.add(new Pair<Integer, String>(STATUS_EOL, "EOL"));
		list.add(new Pair<Integer, String>(STATUS_PREPARING, "Preparing"));
		list.add(new Pair<Integer, String>(STATUS_IGNORE, "Ignore"));
		statusList = Collections.unmodifiableList(list);
	}

	public static String getDisplayText(int status) {
		switch (status) {
		case STATUS_ACTIVE: return "Active";
		case STATUS_EOL: return "EOL";
		case STATUS_PREPARING: return "Preparing";
		case STATUS_IGNORE: return "Ignore";
		case STATUS_CHECK: return "Check";
		default: return "Invalid Status";
		}
	}

	public static List<Pair<Integer, String>> listStatus() {
		return statusList;
	}
}
