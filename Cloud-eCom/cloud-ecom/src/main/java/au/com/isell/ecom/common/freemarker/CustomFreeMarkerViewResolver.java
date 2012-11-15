package au.com.isell.ecom.common.freemarker;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

public class CustomFreeMarkerViewResolver extends FreeMarkerViewResolver {

	public CustomFreeMarkerViewResolver() {
		setViewClass(requiredViewClass());
	}

	/**
	 * Requires {@link FreeMarkerView}.
	 */
	@Override
	protected Class requiredViewClass() {
		return CustomFreeMarkerView.class;
	}
}