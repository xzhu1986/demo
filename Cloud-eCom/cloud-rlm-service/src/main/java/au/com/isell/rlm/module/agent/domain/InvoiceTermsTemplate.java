package au.com.isell.rlm.module.agent.domain;

import au.com.isell.common.model.AbstractModel;

public class InvoiceTermsTemplate  extends AbstractModel {
	public static final String DEFAULE_INVOICE_TERMS="Your Annual Software Maintainence Licence Expires: ${licenseExpiryDate}\n\n" +
			   "This licence covers your site for ${totalUsers} named/concurrent users which you may extend by contacting iSell at sales@isell.com.au.\n\n" +
			   "You are entitled to all software upgrades during this period excluding additional payable features that may be offered from time to time.\n\n" +
			   "The licence does not entitle you to re-publish supplier price files without the permission of iSell Pty Ltd. Fees may apply.\n\n" +
			   "Additional Licence details can be found in the software's help menu, or by contacting iSell though sales@isell.com.au.";
	
	private String content;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
