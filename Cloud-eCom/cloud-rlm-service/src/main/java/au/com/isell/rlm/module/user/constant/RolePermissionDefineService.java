package au.com.isell.rlm.module.user.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.common.freemarker.EnumMsgCode;
import au.com.isell.rlm.module.user.service.PermissionPreloadingService;

/**
 * @author frankw 03/05/2012
 */
@Component
public class RolePermissionDefineService {
	public enum DefaultRole {
		@EnumMsgCode("role.administrator")
		Administrator(true, true), @EnumMsgCode("role.sales_manager")
		SalesManager(true, true), @EnumMsgCode("role.data_manager")
		DataManager(true, true), @EnumMsgCode("role.support_user")
		SupportUser(false, true), @EnumMsgCode("role.data_user")
		DataUser(false, false);

		private boolean viewResellerFromAgent;
		private boolean showSupplierFromCountry;

		private DefaultRole(boolean viewResellerFromAgent, boolean showSupplierFromCountry) {
			this.viewResellerFromAgent = viewResellerFromAgent;
			this.showSupplierFromCountry = showSupplierFromCountry;
		}

		public boolean isViewResellerFromAgent() {
			return viewResellerFromAgent;
		}

		public void setViewResellerFromAgent(boolean viewResellerFromAgent) {
			this.viewResellerFromAgent = viewResellerFromAgent;
		}

		public boolean isShowSupplierFromCountry() {
			return showSupplierFromCountry;
		}

		public void setShowSupplierFromCountry(boolean showSupplierFromCountry) {
			this.showSupplierFromCountry = showSupplierFromCountry;
		}
	}

	@Autowired
	private PermissionPreloadingService permissionPreloadingService;

	private static Map<DefaultRole, List<String>> rolePermissions = new HashMap<RolePermissionDefineService.DefaultRole, List<String>>();
	static {
		String[] perms = new String[] { "reseller:view", "reseller:edit", "reseller:create", "reseller:delete", "reseller:summary", "invoice:view",
				"invoice:edit", "invoice:create", "invoice:report:view", "setting:view", "setting:edit", "setting:create", "setting:delete",
				"supplier:view", "supplier:edit", "supplier:create", "supplier:annualBilling:view", "supplier:annualBilling:edit",
				"supplier:dataRequest:view", "supplier:dataRequest:edit", "supplier:priceBreak:view", "supplier:priceBreak:edit",
				"supplier:priceBreak:create", "supplier:priceBreak:delete", "supplier:reseller:view", "supplier:reseller:edit",
				"supplier:reseller:create", "supplier:reseller:delete", "supplier:branch:view", "supplier:branch:edit", "supplier:branch:create",
				"supplier:branch:delete", "priceupdates:view", "priceupdates:edit" };
		rolePermissions.put(DefaultRole.Administrator, Arrays.asList(perms));

		perms = new String[] { "reseller:view", "reseller:edit", "reseller:create", "reseller:summary", "invoice:view", "invoice:edit",
				"invoice:create", "invoice:report:view", "supplier:view", "supplier:edit", "supplier:create", "supplier:annualBilling:view",
				"supplier:annualBilling:edit", "supplier:dataRequest:view", "supplier:reseller:view", "supplier:reseller:edit",
				"supplier:reseller:create", "supplier:reseller:delete", "supplier:priceBreak:view", "supplier:branch:view" };
		rolePermissions.put(DefaultRole.SalesManager, Arrays.asList(perms));

		perms = new String[] { "reseller:view", "reseller:edit", "reseller:create", "reseller:summary", "supplier:view", "supplier:edit",
				"supplier:create", "supplier:annualBilling:view", "supplier:annualBilling:edit", "supplier:dataRequest:view",
				"supplier:dataRequest:edit", "supplier:priceBreak:view", "supplier:priceBreak:edit", "supplier:priceBreak:create",
				"supplier:priceBreak:delete", "supplier:reseller:view", "supplier:reseller:edit", "supplier:reseller:create",
				"supplier:reseller:delete", "supplier:branch:view", "supplier:branch:edit", "supplier:branch:create", "supplier:branch:delete",
				"priceupdates:view", "priceupdates:edit" };
		rolePermissions.put(DefaultRole.DataManager, Arrays.asList(perms));

		perms = new String[] { "reseller:view", "reseller:edit", "reseller:create", "supplier:view", "supplier:priceBreak:view",
				"supplier:branch:view" };
		rolePermissions.put(DefaultRole.SupportUser, Arrays.asList(perms));

		perms = new String[] { "supplier:view", "supplier:priceBreak:view", "supplier:branch:view", "priceupdates:view", "priceupdates:edit" };
		rolePermissions.put(DefaultRole.DataUser, Arrays.asList(perms));
	}

	private List<String> viewResellerWhenAgent = new ArrayList<String>();

	private synchronized List<String> getResellerViewWhenAgentPerms() {
		if (viewResellerWhenAgent.size() == 0) {
			for (String perm : permissionPreloadingService.getPermissions()) {
				if (perm.matches("reseller:when_agent_.+:view")) {
					viewResellerWhenAgent.add(perm);
				}
			}
		}
		return viewResellerWhenAgent;
	}

	private List<String> supplierFromCountryPermss = new ArrayList<String>();

	private synchronized List<String> getSupplierFromCountryPerms() {
		if (supplierFromCountryPermss.size() == 0) {
			for (String perm : permissionPreloadingService.getPermissions()) {
				if (perm.matches("supplier:when_country_.+:view")) {
					supplierFromCountryPermss.add(perm);
				}
			}
		}
		return supplierFromCountryPermss;
	}

	public List<String> getRolePermission(DefaultRole defaultRole) {
		List<String> r = new ArrayList<String>();
		List<String> list = rolePermissions.get(defaultRole);
		r.addAll(list);
		if (defaultRole.isViewResellerFromAgent()) {
			r.addAll(getResellerViewWhenAgentPerms());
		}
		if (defaultRole.isShowSupplierFromCountry()) {
			r.addAll(getSupplierFromCountryPerms());
		}
		return r;
	}

	public static void main(String[] args) {
		// System.out.println("reseller:when_agentd:sdjl:view".matches("reseller:when_agent:\\w+:view"));
	}
}
