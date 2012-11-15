package au.com.isell.rlm.importing.constant.supplier;

import org.apache.hadoop.fs.Path;

public class SupplierFilePath {
	public static final String SOURCE_SUPPLIER_FILE = "Supplier.csv";
	public static final String OLD_SUPPLIER_PATH = "supplier"+Path.SEPARATOR;
	public static final String OLD_SUPPLIER_BREAK_UUID_PATH = "supplierbreak"+Path.SEPARATOR;
	public static final String OLD_SUPPLIER_USER_SCHED_UUID_PATH = "supplierusersched"+Path.SEPARATOR;
	public static final String ALL_SUPPLIER_PATH = "all"+Path.SEPARATOR+"supplier"+Path.SEPARATOR;
	public static final String CHANGED_SUPPLIER_PATH = "changed"+Path.SEPARATOR+"supplier"+Path.SEPARATOR;
	public static final String UUID_SUPPLIER_BREAK_PATH = "uuid"+Path.SEPARATOR+"supplierbreak"+Path.SEPARATOR;
	public static final String UUID_SUPPLIER_USER_SCHED_PATH = "uuid"+Path.SEPARATOR+"supplierusersched"+Path.SEPARATOR;
	
	public static final String SOURCE_SUPPLIER_BRANCH_FILE = "SupBranch.csv";
	public static final String OLD_SUPPLIER_BRANCH_PATH = "supplierbranch"+Path.SEPARATOR;
	public static final String OLD_SUPPLIER_BRANCH_UUID_PATH = "supplierbranch"+Path.SEPARATOR;
	public static final String ALL_SUPPLIER_BRANCH_PATH = "all"+Path.SEPARATOR+"supplierbranch"+Path.SEPARATOR;
	public static final String CHANGED_SUPPLIER_BRANCH_PATH = "changed"+Path.SEPARATOR+"supplierbranch"+Path.SEPARATOR;
	public static final String UUID_SUPPLIER_BRANCH_PATH = "uuid"+Path.SEPARATOR+"supplierbranch"+Path.SEPARATOR;

	
	public static final String SOURCE_SUPPLIER_PRICEBREAK_FILE = "PriceBreak.csv";
	@Deprecated
	public static final String OLD_SUPPLIER_PRICEBREAK_PATH = "supplierpricebreak"+Path.SEPARATOR;
	@Deprecated
	public static final String ALL_SUPPLIER_PRICEBREAK_PATH = "all"+Path.SEPARATOR+"supplierpricebreak"+Path.SEPARATOR;
	@Deprecated
	public static final String CHANGED_SUPPLIER_PRICEBREAK_PATH = "changed"+Path.SEPARATOR+"supplierpricebreak"+Path.SEPARATOR;
	
	public static final String ALL_SUPPLIER_MAX_ID_PATH = "all"+Path.SEPARATOR+"suppliermaxid"+Path.SEPARATOR;
	
}
