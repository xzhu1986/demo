package au.com.isell.rlm.module.user.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.user.domain.Permission;
import au.com.isell.rlm.module.user.service.PermissionPreloadingService;
import au.com.isell.rlm.module.user.service.PermissionService;
import au.com.isell.rlm.module.user.vo.PermissionGroup;
import au.com.isell.rlm.module.user.vo.PermissionPackage;

@Service
public class PermissionServiceImpl implements PermissionService {
	@Autowired
	private PermissionPreloadingService permissionPreloadingService;

	@Override
	public List<String> filterCountryOfSupplierPerm(List<String> countries) {
		if (CollectionUtils.isEmpty(countries))
			return countries;
		List<String> list = new ArrayList<String>();
		for (String country : countries) {
			if (isAllowedCountryOfSupplierPerm(country)) {
				list.add(country);
			}
		}
		return list;
	}

	@Override
	public boolean isAllowedCountryOfSupplierPerm(String countryCode) {
		if (SecurityUtils.getSubject().isPermitted("supplier:when_country_" + countryCode + ":view"))
			return true;
		return false;
	}

	@Override
	public List<UUID> filterAgentOfResellerPerm(List<UUID> agentIds) {
		if (CollectionUtils.isEmpty(agentIds))
			return agentIds;
		List<UUID> list = new ArrayList<UUID>();
		for (UUID id : agentIds) {
			if (isAllowedAgentOfResellerPerm(id)) {
				list.add(id);
			}
		}
		return list;
	}

	@Override
	public boolean isAllowedAgentOfResellerPerm(UUID id) {
		if (SecurityUtils.getSubject().isPermitted("reseller:when_agent_" + id + ":view"))
			return true;
		return false;
	}

	private Pattern agentPattern = Pattern.compile("reseller:when_agent_(.+):view");
	private Pattern countryPattern = Pattern.compile("supplier:when_country_(.+):view");
	
	private Pattern rpJobPattern = Pattern.compile("resellerportal:(.+)_job");

	@Override
	public List<String> getAgentsOfResellerPerm() {
		List<String> list = new ArrayList<String>();
		for (String perm : GlobalAttrManager.getCurrentUser().getPermissions()) {
			Matcher matcher = agentPattern.matcher(perm);
			if (matcher.find()) {
				list.add(matcher.group(1));
			}
		}
		return list;
	}

	@Override
	public List<String> getCountriesOfSupplierPerm() {
		List<String> list = new ArrayList<String>();
		for (String perm : GlobalAttrManager.getCurrentUser().getPermissions()) {
			Matcher matcher = countryPattern.matcher(perm);
			if (matcher.find()) {
				list.add(matcher.group(1));
			}
		}
		return list;
	}

	@Override
	public Set<String> getResellerPortalPermissions() {
		Set<String> set = new HashSet<String>();
		Set<PermissionPackage> perPackages = permissionPreloadingService.getResellerPermissionPackages();
		for (PermissionPackage perPackage : perPackages) {
			for (PermissionGroup permissionGroup : perPackage.getPermissionGroups()) {
				for (Permission permission : permissionGroup.getPermissions()) {
					set.add(permission.getKey());
				}
			}
		}
		return set;
	}
	
	@Override
	public Set<String> getResellerPortalJobPermissions() {
		Set<String> set = new HashSet<String>();
		Set<PermissionPackage> perPackages = permissionPreloadingService.getResellerPermissionPackages();
		for (PermissionPackage perPackage : perPackages) {
			for (PermissionGroup permissionGroup : perPackage.getPermissionGroups()) {
				for (Permission permission : permissionGroup.getPermissions()) {
					Matcher matcher = rpJobPattern.matcher(permission.getKey());
					if (matcher.find()) {
						set.add(permission.getKey());
					}
				}
			}
		}
		return set;
	}
}
