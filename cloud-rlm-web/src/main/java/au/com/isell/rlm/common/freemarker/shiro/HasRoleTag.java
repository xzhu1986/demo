package au.com.isell.rlm.common.freemarker.shiro;

/**
 * <p>Equivalent to {@link org.apache.shiro.web.tags.HasRoleTag}</p>
 */
public class HasRoleTag extends RoleTag {
    @Override
	protected boolean showTagBody(String roleName) {
        return getSubject() != null && getSubject().hasRole(roleName);
    }
}
