$(document).ready(function() {
	// link behiviour
	$("a[target='_blank']").on('click', function(e) {
		e.preventDefault();
		var url = $(this).attr('href');
		window.open(url);
	});
	// website link
	$("a.website-addr").live('click',function(){
		var href=$(this).attr('href');
		if(!/^http:.+/.test(href)){
			$(this).attr('href',"http://"+href);
		}
	});
	// close current window event,if has a form,reload parent window

	// close dialog when press ESC
	window.document.body.onkeydown = function(e) {
		if (e.keyCode == 27 && window.opener) {
			window.close();
		}
	}

	// reactivate session
	setInterval(function() {
		$.getJSON(basePath + '/reActivateSession.json');
	}, 1000 * 60 * 10);
});
