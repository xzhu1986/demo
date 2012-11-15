package au.com.isell.idworker;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import au.com.isell.common.index.elasticsearch.IndexHelper;

@Service
public class PreloadingService implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		IndexHelper.scanIndices();
	}
}