<%@page import="java.io.PrintWriter"%>
<%@ page isELIgnored="false"%>
<%@page import="au.com.isell.rlm.common.web.error.ErrorMsg"%>
<%@page import="java.util.Date"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>iData v6.0</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="<c:url value="/style/screen.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/style/append.css"/>" />
</head>
<body>
	<div id="contentZone">
		<div id="headerDate">
			<p id="date">
				<fmt:formatDate value="<%=new java.util.Date()%>" pattern="EEEEEE dd MMM yyyy" timeZone="${userTimeZone}" />
			</p>
		</div>
		<div id="contentContainer">
			<div id="contentCommon">
				<h1>Oops , There is a problem...</h1>
				<p></p>
			</div>
			<div id="content" class="error-msg">
				<div class="error-title">
					<h3>
						<c:if test="${not empty errorMsg.msgCode}">
							<c:if test="${not empty errorMsg.msgParams}">
								<spring:message code="${errorMsg.msgCode}" arguments="${errorMsg.msgParams}"></spring:message>
							</c:if>
							<c:if test="${empty errorMsg.msgParams}">
								<spring:message code="${errorMsg.msgCode}"></spring:message>
							</c:if>
						</c:if>
						<c:if test="${empty errorMsg.msgCode}">
							${errorMsg.message}
						</c:if>
					</h3>
				</div>
				<div id='contentBody' class='error-content'>
					<table>
						<tr>
							<th>Error Type</th>
							<td>${errorMsg.type}</td>
						</tr>
						<tr>
							<td colspan="21">
								<div id="error-statcktrace">
									<pre>
									<%
										Throwable throwable = ((ErrorMsg) request.getAttribute("errorMsg")).getThrowable();
										throwable.printStackTrace(new PrintWriter(out));
									%>
									</pre>
								</div>
							</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
	</div>
</body>
</html>