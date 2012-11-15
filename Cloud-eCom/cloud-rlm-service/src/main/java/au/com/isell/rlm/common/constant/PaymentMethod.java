package au.com.isell.rlm.common.constant;

import au.com.isell.rlm.common.freemarker.EnumMsgCode;

public enum PaymentMethod {
	@EnumMsgCode("pay.method.eft")
	EFT, 
	@EnumMsgCode("pay.method.international_eft")
	InternationalEFT, 
	@EnumMsgCode("pay.method.credit_card")
	CreditCard, 
	@EnumMsgCode("pay.method.cheque")
	Cheque, 
	@EnumMsgCode("pay.method.direct_debit")
	DirectDebit, 
	@EnumMsgCode("pay.method.pay_pal")
	PayPal, 
	@EnumMsgCode("pay.method.other")
	Other
}