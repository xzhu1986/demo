package au.com.isell.rlm.common.freemarker.shiro;

/**
 * <p>Equivalent to {@link org.apache.shiro.web.tags.LacksPermissionTag}</p>
 */
public class LacksPermissionTag extends PermissionTag {
    @Override
	protected boolean showTagBody(String p) {
        return !isPermitted(p);
    }
}
