package au.com.isell.common.index.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface ISellIndexKey {
	String name() default "";
	boolean wildcard() default false;
}
