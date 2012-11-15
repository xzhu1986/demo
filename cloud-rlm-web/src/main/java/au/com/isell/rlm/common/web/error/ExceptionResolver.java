package au.com.isell.rlm.common.web.error;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import au.com.isell.remote.ws.common.model.Result;
import au.com.isell.rlm.common.exception.BizException;

/**
 * @author frankw 28/11/2011
 */
public class ExceptionResolver implements HandlerExceptionResolver {
	private static final Logger logger = LoggerFactory.getLogger(ExceptionResolver.class);

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		String viewName = getViewName(request);
		String msg = getMsg(ex);
		if(msg==null) msg=ex.toString();
		if ("html".equals(viewName)) {// not ajax
			try {
				String msgCode = null;
				Object[] mscCodeParams = null;
				if (BizException.class.isAssignableFrom(ex.getClass())) {
					msgCode = ((BizException) ex).getMsgCode();
					mscCodeParams = ((BizException) ex).getParams();
					ModelAndView modelAndView = new ModelAndView("common/bizError");
					ErrorMsg errorMsg =new ErrorMsg(null,msgCode, mscCodeParams, ex.getClass().toString(), msg, request.getRequestURI(), ex); 
					modelAndView.addObject("error",errorMsg);
					return modelAndView;
				}else{
					ErrorMsg errorMsg =new ErrorMsg(null,msgCode, mscCodeParams, ex.getClass().toString(), msg, request.getRequestURI(), ex); 
					request.setAttribute("errorMsg", errorMsg);
					// forward to centeral error handler
					request.getRequestDispatcher("/error").forward(request, response);
				}

			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), ex);
			}
			return null;
		} else {// ajax
			logger.error(ex.getMessage(), ex);
			ModelAndView modelAndView = new ModelAndView(getViewName(request));
			modelAndView.addObject(new Result(msg, false));
			return modelAndView;
		}
	}

	public String getMsg(Throwable e) {
		if(e.getCause()!=null){
			return getMsg(e.getCause());
		}
		return e.getMessage();
	}

	private String getViewName(HttpServletRequest request) {
		String reqUri = request.getRequestURI().toLowerCase();
		String viewName = reqUri.endsWith(".xml") ? "xml" : (reqUri.endsWith("json") ? "json" : "html");// jstl
		return viewName;
	}

}
