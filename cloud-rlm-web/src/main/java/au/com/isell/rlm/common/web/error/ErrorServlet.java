package au.com.isell.rlm.common.web.error;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ErrorServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// from ExceptionResolver
		ErrorMsg errorMsg = (ErrorMsg) request.getAttribute("errorMsg");
		if (errorMsg == null) {
			String statusCode = null, message = null, type = null, uri = null;

			Object codeObj = request.getAttribute("javax.servlet.error.status_code");
			Object messageObj = request.getAttribute("javax.servlet.error.message");
			Object typeObj = request.getAttribute("javax.servlet.error.exception_type");
			Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
			uri = (String) request.getAttribute("javax.servlet.error.request_uri");

			// Convert the attributes to string values
			statusCode = codeObj != null ? codeObj.toString() : statusCode;
			message = messageObj != null ? messageObj.toString() : message;
			type = typeObj != null ? typeObj.toString() : type;
			uri = uri != null ? uri : request.getRequestURI();

			errorMsg = new ErrorMsg(statusCode,null, null, type, message, uri, throwable);
		}
		
		//resource like js/css/image
		if (errorMsg.getStatusCode()!=null&&errorMsg.getStatusCode().equals("404") && errorMsg.getUri().indexOf(".") > -1) {
			response.sendError(404, "can not find the resource");
		} else {
			request.setAttribute("errorMsg", errorMsg);
			request.getRequestDispatcher("/error.jsp").forward(request, response);
		}
	}

}
