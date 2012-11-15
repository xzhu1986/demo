package au.com.isell.rlm.module.user.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionService {

	List<String> filterCountryOfSupplierPerm(List<String> countries);

	boolean isAllowedCountryOfSupplierPerm(String countryCode);

	List<UUID> filterAgentOfResellerPerm(List<UUID> agentIds);

	boolean isAllowedAgentOfResellerPerm(UUID id);

	List<String> getCountriesOfSupplierPerm();

	List<String> getAgentsOfResellerPerm();

	Set<String> getResellerPortalPermissions();

	Set<String> getResellerPortalJobPermissions();

}