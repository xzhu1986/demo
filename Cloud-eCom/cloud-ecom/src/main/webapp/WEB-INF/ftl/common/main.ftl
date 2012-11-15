<#setting url_escaping_charset="UTF-8">

<#macro frame title>
<#assign basePath=req.contextPath/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>${title}</title>
<link rel="stylesheet" type="text/css" href="${basePath}/css/default.css">
<link rel="stylesheet" type="text/css" href="${basePath}/css/main.css">
</head>
<body>
	<div id="site-body">
		<div id="site-head">
			<@include_page path="/include.jsp"/>
		</div>
		<div id="site-content"><#nested></div>
		<div id="site-foot">This is foot</div>
	</div>
</body>
</html>
</#macro>
