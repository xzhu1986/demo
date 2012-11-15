package au.com.isell.common.index.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface ISellIndex {
	String name();
	/**
	 * if type is "" that means the class can only be search by index name across all types
	 * @return
	 */
	String type();
	boolean checkParents() default true;
	boolean manual() default false;
}
