package au.com.isell.rlm.importing.constant.reseller;

import org.apache.hadoop.fs.Path;

public class ResellerFilePath {
	public static final String SOURCE_RESELLER_FILE = "V6_Reseller.csv";
	public static final String OLD_RESELLER_PATH = "reseller"+Path.SEPARATOR;
	public static final String OLD_PRIMARY_RESELLER_USER_UUID_PATH = "primaryreselleruser"+Path.SEPARATOR;
	public static final String ALL_RESELLER_PATH = "all"+Path.SEPARATOR+"reseller"+Path.SEPARATOR;
	public static final String CHANGED_RESELLER_PATH = "changed"+Path.SEPARATOR+"reseller"+Path.SEPARATOR;
	public static final String UUID_PRIMARY_RESELLER_USER_PATH = "uuid"+Path.SEPARATOR+"primaryreselleruser"+Path.SEPARATOR;
	
	public static final String SOURCE_RESELLER_BREAK_FILE = "ResellerBreak.csv";
	public static final String OLD_RESELLER_BREAK_PATH = "resellerbreak"+Path.SEPARATOR;
	public static final String ALL_RESELLER_BREAK_PATH = "all"+Path.SEPARATOR+"resellerbreak"+Path.SEPARATOR;
	public static final String CHANGED_RESELLER_BREAK_PATH = "changed"+Path.SEPARATOR+"resellerbreak"+Path.SEPARATOR;
	
	
	public static final String SOURCE_RESELLER_USER_FILE = "ilive_users.csv";
	public static final String OLD_RESELLER_USER_PATH = "reselleruser"+Path.SEPARATOR;
	public static final String ALL_RESELLER_USER_PATH = "all"+Path.SEPARATOR+"reselleruser"+Path.SEPARATOR;
	public static final String CHANGED_RESELLER_USER_PATH = "changed"+Path.SEPARATOR+"reselleruser"+Path.SEPARATOR;
	public static final String OLD_RESELLER_USER_UUID_PATH = "reselleruser"+Path.SEPARATOR;
	public static final String UUID_RESELLER_USER_PATH = "uuid"+Path.SEPARATOR+"reselleruser"+Path.SEPARATOR;
	
	public static final String ALL_RESELLER_MAX_ID_PATH = "all"+Path.SEPARATOR+"resellermaxid"+Path.SEPARATOR;
	
}
