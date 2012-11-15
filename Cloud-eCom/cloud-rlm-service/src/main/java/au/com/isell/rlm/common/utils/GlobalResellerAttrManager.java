package au.com.isell.rlm.common.utils;

import org.apache.shiro.SecurityUtils;

import au.com.isell.rlm.module.jbe.domain.CustInfo;

public class GlobalResellerAttrManager {
	public static void setAccountInfo(CustInfo info) {
		SecurityUtils.getSubject().getSession().setAttribute("rsUserInfo", info);
	}

	public static CustInfo getAccountInfo() {
		return (CustInfo) SecurityUtils.getSubject().getSession().getAttribute("rsUserInfo");
	}
}
