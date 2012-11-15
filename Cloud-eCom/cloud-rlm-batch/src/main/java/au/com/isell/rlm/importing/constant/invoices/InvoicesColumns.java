package au.com.isell.rlm.importing.constant.invoices;

public interface InvoicesColumns {
	// Invoices csv
	public static final String ResellerSerialNumber = "Reseller Serial Number";
	public static final String ResellerCompanyName = "Reseller Company Name";
	public static final String InvoiceNumber = "Invoice Number";
	public static final String Status = "Status";
	public static final String CreatedDate = "Created Date";
	public static final String InvoiceDate = "Invoice Date";
	public static final String FollowupDate = "Followup Date";
	public static final String FullyPaidDate = "Fully Paid Date";
	public static final String PaymentTerms = "Payment Terms";
	public static final String InvoiceReportID = "Invoice ReportID";
	public static final String AgentID = "Agent ID";
	public static final String SalesRepID = "Sales Rep ID";
	public static final String ToFirstName = "To First Name";
	public static final String ToLastName = "To Last Name";
	public static final String ToPhoneNumber = "To Phone Number";
	public static final String ToMobileNumber = "To Mobile Number";
	public static final String ToEmailAddress = "To Email Address";
	public static final String ToEmailAddressCC = "To Email Address CC";
	public static final String AmountITQuoter = "Amount ITQuoter";
	public static final String AmountEcommerce = "Amount Ecommerce";
	public static final String AmountEPD = "Amount EPD";
	public static final String AmountServices = "Amount Services";
	public static final String AmountOther = "Amount Other";
	public static final String TotalAmount = "Total Amount";
	public static final String TotalAgentCommissions = "Total Agent Commissions";
	public static final String TotalPayments = "Total Payments";
	public static final String CurrencyName = "Currency Name";
	public static final String GstRate = "Gst Rate";
	public static final String GstName = "Gst Name";
	public static final String InternalNotes = "Internal Notes";
	public static final String InvoiceDetails = "Invoice Details";
	public static final String InvoiceTerms = "Invoice Terms";
	public static final String NewBusiness = "New Business";

	// invoices follow csv
	public static final String UserID = "User ID";
	public static final String Followup = "Followup";
	public static final String Results = "Results";
	public static final String AddtionalNotes = "Addtional Notes";
	public static final String EmailSubject = "Email Subject";
	public static final String EmailBody = "Email Body";
	// invoices sched csv
	public static final String ScheduledAmount = "Scheduled Amount";
	public static final String DueDate = "Due Date";
	public static final String PaymentReceived = "Payment Received";
	public static final String PaymentReceivedDate = "Payment Received Date";
	public static final String PaymentType = "Payment Type";
	public static final String PaymentReference = "Payment Reference";

	//invoicesSearchbean  object
	public static final String TotalPaid = "TotalPaid";
}
