package au.com.isell.ecom.common.freemarker;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.ext.servlet.IncludePage;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.SimpleHash;

public class CustomFreeMarkerView extends FreeMarkerView {

	private TaglibFactory taglibFactory;

	private ServletContextHashModel servletContextHashModel;

	@Override
	protected void doRender(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Expose model to JSP tags (as request attributes).
		exposeModelAsRequestAttributes(model, request);

		// Expose all standard FreeMarker hash models.
		model.put(FreemarkerServlet.KEY_JSP_TAGLIBS, this.taglibFactory);
		model.put(FreemarkerServlet.KEY_APPLICATION, this.servletContextHashModel);
		model.put(FreemarkerServlet.KEY_SESSION, buildSessionModel(request, response));
		model.put(FreemarkerServlet.KEY_REQUEST, new HttpRequestHashModel(request, response, getObjectWrapper()));
		model.put(FreemarkerServlet.KEY_REQUEST_PARAMETERS, new HttpRequestParametersHashModel(request));
		// ======== modified here 2012-1-10 frankw ========
		model.put(FreemarkerServlet.KEY_INCLUDE, new IncludePage(request, response));

		if (logger.isDebugEnabled()) {
			logger.debug("Rendering FreeMarker template [" + getUrl() + "] in FreeMarkerView '" + getBeanName() + "'");
		}
		// Grab the locale-specific version of the template.
		Locale locale = RequestContextUtils.getLocale(request);
		processTemplate(getTemplate(locale), new SimpleHash(model), response);
	}

	private HttpSessionHashModel buildSessionModel(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return new HttpSessionHashModel(session, getObjectWrapper());
		} else {
			return new HttpSessionHashModel(null, request, response, getObjectWrapper());
		}
	}

}