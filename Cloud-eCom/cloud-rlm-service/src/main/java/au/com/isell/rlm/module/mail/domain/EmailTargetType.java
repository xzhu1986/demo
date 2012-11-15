package au.com.isell.rlm.module.mail.domain;

public enum EmailTargetType {
	Resellers {
		@Override
		public String[] getParameterDef() {
			return new String[] { "custfirstname", "custlastname", "custfullname", "custserialnumber", "web.address"};
		}
	},
	Suppliers {
		@Override
		public String[] getParameterDef() {
			return new String[] { "resellers.count", "resellers.count.pending", "resellers.count.approved", "web.address"};
		}
	};

	public abstract String[] getParameterDef();
}
