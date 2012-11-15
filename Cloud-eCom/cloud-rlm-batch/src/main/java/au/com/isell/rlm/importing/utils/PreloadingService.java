package au.com.isell.rlm.importing.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import au.com.isell.rlm.module.user.service.PermissionPreloadingService;

@Service
public class PreloadingService extends ApplicationObjectSupport implements InitializingBean {
	@Autowired
	private PermissionPreloadingService permissionPreloadingService;
	@Override
	public void afterPropertiesSet() throws Exception {
//		Preload.preload();
		permissionPreloadingService.preload(getApplicationContext());
	}

}
