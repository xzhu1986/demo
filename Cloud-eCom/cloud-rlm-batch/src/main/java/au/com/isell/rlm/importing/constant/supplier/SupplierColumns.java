package au.com.isell.rlm.importing.constant.supplier;

public interface SupplierColumns {
	//Supplier csv 
	public static final String SupplierID = "Supplier ID";
	public static final String SupplierName = "Supplier Name";
	public static final String ShortName = "Short Name";
	public static final String ActiveStates = "Active States";
	public static final String UpdatingComments = "Updating Comments";
	public static final String UpdateRegularity = "Update Regularity";
	public static final String SupplierStability = "Supplier Stability";
	public static final String QikPriceStartup = "QikPriceStartup";
	public static final String ShowStockInfo = "Show Stock Info";
	public static final String ShowStockDate = "Show Stock Date";
	public static final String WEBAddress = "WEBAddress";
	public static final String ApprovalTo = "Approval To";
	public static final String ApprovalMethod = "Approval Method";
	public static final String ApprovalEmailAddress = "Approval Email Address";
	//supplier branch csv
	//public static final String "Supplier ID";
	public static final String BranchName = "Branch Name";
	public static final String Phone="Phone";
	public static final String Fax="Facsimile";
	public static final String Address1 = "Address1";
	public static final String Address2 = "Address2";
	public static final String Address3 = "Address3";
	public static final String City = "City";
	public static final String State = "State";
	public static final String Zip = "Zip";
	public static final String SupplierBranchCountry = "Supplier Banrch Country";
	public static final String SupplierBranchCountryCode = "Supplier Banrch Country Code";
	public static final String PostalAddress1 = "Postal Address1";
	public static final String PostalAddress2 = "Postal Address2";
	public static final String PostalAddress3 ="Postal Address3";
	public static final String PostalCity="Postal City";
	public static final String PostalState="Postal State";
	public static final String PostalZip="Postal Zip";
	public static final String WarehouseAddress1="Warehouse Address1";
	public static final String WarehouseAddress2="Warehouse Address2";
	public static final String WarehouseAddress3="Warehouse Address3";
	public static final String WarehouseCity = "Warehouse City";
	public static final String WarehouseState = "Warehouse State";
	public static final String WarehouseZip = "Warehouse Zip";
	//
	//PriceBreak csv 
	public static final String BreakID = "BreakID";
	//public static final String "Supplier ID";
	public static final String Disabled = "Disabled";
	public static final String BreakName = "BreakName";
	public static final String BreakUUID = "BreakUUID";
	
	//Supplier Object
	public static final String CountryCode = "Country Code";
	public static final String ResellerCount = "ResellerCount";
	
	//Supplier User Object
	public static final String SupplierUserID = "SupplierUserID";
	
	//Supplier Branch Object
	public static final String SupplierBranchID = "SupplierBranchID";
	
	//Supplier Schedule Object
	public static final String ScheduleUUID = "ScheduleUUID";
}
