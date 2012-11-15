package au.com.isell.rlm.module.supplier.domain;

import au.com.isell.common.bean.BeanInitable;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

public class DataRequest implements BeanInitable<DataRequest>{
	public static enum SignupMethod {
		@EnumMsgCode("datareq.signup.auto_approve")
		AutoApprove,
		@EnumMsgCode("datareq.signup.supplier_to_approve")
		SupplierToApprove, 
		@EnumMsgCode("datareq.signup.isell_approve")
		IsellApprove}
	private SignupMethod signupMethod;
	private boolean showStockData;
	private boolean useOriginalTiers;
	private boolean requireAccountNo;
	private boolean requireLoginName;
	private boolean requireLoginPassword;
	private String priceDownloadURL;
	private String detailsForReseller;
	private String internalNotes;

	public SignupMethod getSignupMethod() {
		return signupMethod;
	}
	public void setSignupMethod(SignupMethod signupMethod) {
		this.signupMethod = signupMethod;
	}
	public boolean isShowStockData() {
		return showStockData;
	}
	public void setShowStockData(boolean showStockData) {
		this.showStockData = showStockData;
	}
	public boolean isUseOriginalTiers() {
		return useOriginalTiers;
	}
	public void setUseOriginalTiers(boolean useOriginalTiers) {
		this.useOriginalTiers = useOriginalTiers;
	}
	public boolean isRequireAccountNo() {
		return requireAccountNo;
	}
	public void setRequireAccountNo(boolean requireAccountNo) {
		this.requireAccountNo = requireAccountNo;
	}
	public boolean isRequireLoginName() {
		return requireLoginName;
	}
	public void setRequireLoginName(boolean requireLoginName) {
		this.requireLoginName = requireLoginName;
	}
	public boolean isRequireLoginPassword() {
		return requireLoginPassword;
	}
	public void setRequireLoginPassword(boolean requireLoginPassword) {
		this.requireLoginPassword = requireLoginPassword;
	}
	public String getPriceDownloadURL() {
		return priceDownloadURL;
	}
	public void setPriceDownloadURL(String priceDownloadURL) {
		this.priceDownloadURL = priceDownloadURL;
	}
	public String getDetailsForReseller() {
		return detailsForReseller;
	}
	public void setDetailsForReseller(String detailsForReseller) {
		this.detailsForReseller = detailsForReseller;
	}
	public String getInternalNotes() {
		return internalNotes;
	}
	public void setInternalNotes(String internalNotes) {
		this.internalNotes = internalNotes;
	}
	@Override
	public DataRequest init() {
		// TODO Auto-generated method stub
		return this;
	}
	@Override
	public String toString() {
		return "DataRequest [signupMethod=" + signupMethod + ", showStockData="
				+ showStockData + ", useOriginalTiers=" + useOriginalTiers
				+ ", requireAccountNo=" + requireAccountNo
				+ ", requireLoginName=" + requireLoginName
				+ ", requireLoginPassword=" + requireLoginPassword
				+ ", priceDownloadURL=" + priceDownloadURL
				+ ", detailsForReseller=" + detailsForReseller
				+ ", internalNotes=" + internalNotes + "]";
	}
}
