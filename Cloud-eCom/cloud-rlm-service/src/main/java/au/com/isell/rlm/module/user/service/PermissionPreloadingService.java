package au.com.isell.rlm.module.user.service;

import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import au.com.isell.rlm.module.user.domain.Permission;
import au.com.isell.rlm.module.user.vo.PermissionPackage;



public interface PermissionPreloadingService {
	Permission getPermission(String key);
	
	Set<PermissionPackage> getAgentPermissionPackages();
	
	Set<PermissionPackage> getResellerPermissionPackages();
	
	Iterable<String> getPermissions();

	void updatePermissionCache(Resource permissionDefineFile);

	void preload(ApplicationContext applicationContext);
}
