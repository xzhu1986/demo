package au.com.isell.rlm.module.system.vo;

import au.com.isell.rlm.module.system.constant.SettingEntryPath;

public enum SettingCategorys {
	Users(SettingEntryPath.users), Agents(SettingEntryPath.agents), Countries(SettingEntryPath.countries), 
	Currencies(SettingEntryPath.currencies), EmailTemplates("Email Templates");

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private SettingCategorys(String display) {
		this.display = display;
	}

}
