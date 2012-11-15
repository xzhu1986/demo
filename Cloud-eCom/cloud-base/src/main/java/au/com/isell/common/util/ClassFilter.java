package au.com.isell.common.util;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

public class ClassFilter {
	public static void doFilter(String basePackage, ClassMatcher classMatcher) {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(basePackage) + "/" + "**/*.class";
		try {
			Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
					match(metadataReader, classMatcher);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}

	private static void match(MetadataReader metadataReader, ClassMatcher classMatcher) throws ClassNotFoundException {
		Class c = Class.forName(metadataReader.getClassMetadata().getClassName());
		classMatcher.match(c);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ClassFilter.doFilter("au.com.isell", new ClassMatcher() {
			@Override
			public void match(Class cls) {
				System.out.println(cls);
			}
		});
	}
}