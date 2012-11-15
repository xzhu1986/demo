package au.com.isell.common.aws.annotation.s3;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * s3 path
 * @author frankw 18/01/2012
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IsellPath {
	/**
	 * path or path template
	 * @return
	 */
	String value() ;

	/**
	 * params description
	 * @return
	 */
	String[] paramNames() default {};
}
