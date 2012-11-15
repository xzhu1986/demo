package au.com.isell.common.index.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface ISellIndexValue {
	String name() default "";
	boolean store() default false;
	boolean lowercase() default false;
	/**
	 * Only for single keyword field and it will use $ as reserve character. please don't use it when word contains $.
	 * On the other hand the field can only used for search but not be able to get value.
	 * @return
	 */
	boolean wildcard() default false;
	String analyzer() default "";
	/**
	 * no,analyzed,not_analyzed
	 */
	String index() default "not_analyzed";
}
