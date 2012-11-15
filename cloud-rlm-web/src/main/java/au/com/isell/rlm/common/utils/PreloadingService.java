package au.com.isell.rlm.common.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import au.com.isell.common.preloading.Preload;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.init.InitUser;
import au.com.isell.rlm.module.user.service.PermissionPreloadingService;

@Service
public class PreloadingService extends ApplicationObjectSupport implements InitializingBean {
	@Autowired
	private PermissionPreloadingService permissionPreloadingService;
	@Autowired
	private UserDAO userDao;
	@Override
	public void afterPropertiesSet() throws Exception {
		Preload.preload();
		permissionPreloadingService.preload(getApplicationContext());
		GlobalAttrManager.setSystemUser(userDao.getUserByUserName(InitUser.ADMIN_USERNAME));
	}

}
