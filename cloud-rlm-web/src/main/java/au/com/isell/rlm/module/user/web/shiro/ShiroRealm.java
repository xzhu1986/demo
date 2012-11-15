package au.com.isell.rlm.module.user.web.shiro;

import java.util.UUID;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import au.com.isell.common.util.SecurityUtils;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.User;

@Component
public class ShiroRealm extends AuthorizingRealm {
	protected UserDAO userDAO;

	@Autowired
	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
		setAuthorizationCache(userDAO.getAuthInfoCache());
	}

	public ShiroRealm() {
		setName("ShiroRealm"); // This name must match the name in the User class's getPrincipals() method
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher("SHA-256");
		matcher.setStoredCredentialsHexEncoded(false);
		setCredentialsMatcher(matcher);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		String username = token.getUsername();
		Assert.hasText(username, "user name is blank");
		User user = userDAO.getUserByToken(token.getUsername());
		if (user != null) {
			return new SimpleAuthenticationInfo(user.getUserId(), user.getPassword(), getName());
		}
		return null;
	}

	public static void main(String[] args) {
		String token="6100007___ru1@ru.com";
		String pwd="ru3";
		System.out.println(SecurityUtils.digestPassword(token, pwd));
		System.out.println(new Sha256Hash(SecurityUtils.salt(token, pwd)).toBase64());
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		UUID userId = (UUID) principals.fromRealm(getName()).iterator().next();
		User user = userDAO.getUserById(userId);
		if (user != null) {
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			if (user.getPermissions() != null) {
				info.addStringPermissions(user.getPermissions());
			}
			// info.addRole(role.getName());
			return info;
		} else {
			return null;
		}
	}

}
