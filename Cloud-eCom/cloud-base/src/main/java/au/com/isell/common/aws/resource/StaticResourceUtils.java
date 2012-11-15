package au.com.isell.common.aws.resource;

import java.io.IOException;
import java.util.Properties;

import au.com.isell.common.index.elasticsearch.IndexHelper;

/**
 * @author frankw 12/03/2012
 */
public class StaticResourceUtils {
	public enum ResourceFolder {
		RLM("rlm");
		
		private String folderName;

		public String getFolderName() {
			return folderName;
		}

		private ResourceFolder(String folderName) {
			this.folderName = folderName;
		}

	}

	private static String basepath;
	private static boolean isTestEnv=true;;
	static {
		try {
			Properties bundle = new Properties();
			bundle.load(IndexHelper.class.getResourceAsStream("/settings.properties"));
			basepath = bundle.getProperty("s3.resource.url");
			if(basepath!=null){
				basepath = basepath.endsWith("/") ? basepath : basepath + "/";
			}
		
			isTestEnv = basepath==null? true:false;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String getStaticResourceUrlPrefix(ResourceFolder resourceFolder) {
		return basepath + resourceFolder.getFolderName();
	}

	public static boolean isTestEnv() {
		return isTestEnv;
	}
}
