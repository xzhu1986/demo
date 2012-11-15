package au.com.isell.rlm.module.user.vo;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.rlm.module.user.dao.UserDAO;

import com.google.common.cache.CacheBuilder;

@Component
public class AuthInfoCache implements Cache<Object, AuthorizationInfo> {
	@Autowired
	protected UserDAO userDAO;

	private com.google.common.cache.Cache<Object, Object> userAuthCacher = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(30, TimeUnit.MINUTES).build();

	@Override
	public AuthorizationInfo get(Object key) throws CacheException {
		return (AuthorizationInfo)userAuthCacher.getIfPresent(key);
	}

	@Override
	public AuthorizationInfo put(Object key, AuthorizationInfo value) throws CacheException {
		userAuthCacher.put(key, value);
		return value;
	}

	@Override
	public AuthorizationInfo remove(Object key) throws CacheException {
		userAuthCacher.invalidate(key);
		return null;
	}

	@Override
	public void clear() throws CacheException {
		userAuthCacher.invalidateAll();
	}

	@Override
	public int size() {
		return ((Long)userAuthCacher.size()).intValue();
	}

	@Override
	public Set<Object> keys() {
		return userAuthCacher.asMap().keySet();
	}

	@Override
	public Collection<AuthorizationInfo> values() { 
		Collection r=userAuthCacher.asMap().values();
		return r;
	}

}
