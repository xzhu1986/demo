<#import "/common/main.ftl" as main>
<@main.frame "test page">
	${main.basePath}<br/>
	hello ${username!'no user'}<br/>
	${'abc'?matches('\\w+')?string('right','wrong')}<br/>
	${.url_escaping_charset!"default"}
</@main.frame>