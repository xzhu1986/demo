package au.com.isell.rlm.importing.constant.invoices;

import org.apache.hadoop.fs.Path;

public class InvoicesFilePath {
	public static final String SOURCE_INV_FILE = "V6_Inv.csv";
	public static final String OLD_INV_PATH = "inv"+Path.SEPARATOR;
	public static final String ALL_INV_PATH = "all"+Path.SEPARATOR+"inv"+Path.SEPARATOR;
	public static final String CHANGED_INV_PATH = "changed"+Path.SEPARATOR+"inv"+Path.SEPARATOR;
	
	public static final String SOURCE_INV_SCHED_FILE = "V6_InvSched.csv";
	public static final String OLD_INV_SCHED_PATH = "invsched"+Path.SEPARATOR;
	public static final String ALL_INV_SCHED_PATH = "all"+Path.SEPARATOR+"invsched"+Path.SEPARATOR;
	public static final String CHANGED_INV_SCHED_PATH = "changed"+Path.SEPARATOR+"invsched"+Path.SEPARATOR;
	
	public static final String SOURCE_INV_FOLLOW_FILE = "V6_InvFollow.csv";
	public static final String OLD_INV_FOLLOW_PATH = "invfollow"+Path.SEPARATOR;
	public static final String ALL_INV_FOLLOW_PATH = "all"+Path.SEPARATOR+"invfollow"+Path.SEPARATOR;
	public static final String CHANGED_INV_FOLLOW_PATH = "changed"+Path.SEPARATOR+"invfollow"+Path.SEPARATOR;
	
	public static final String ALL_INV_MAX_ID_PATH = "all"+Path.SEPARATOR+"invmaxid"+Path.SEPARATOR;
	
}
