package au.com.isell.common.util;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
/**
 * @author frankw 27/03/2012
 */
public class ClassResourceFilter {
	public static Resource[] doFilter(String pattern) {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +  pattern;
		try {
			return resourcePatternResolver.getResources(packageSearchPath);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} 
	}

}