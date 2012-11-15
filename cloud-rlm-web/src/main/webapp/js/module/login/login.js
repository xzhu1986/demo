$(document).ready(function() {
	$('#forget-pwd').click(function(e) {
		e.preventDefault();
		DLG = art.dialog({
			content	: document.getElementById('forgetPwdForm')
		});

	});

	$('#forgetPwdForm').submit(function(e) {
		e.preventDefault();
		var this$ = $(this);
		this$.mask('Wait a meoment');
		this$.ajaxSubmit(function(data) {
			this$.unmask();
			var msg = "Mail sent!";
			if (data && data.result.msg) {
				msg = data.result.msg;
			}
			DLG.close();
			alert(msg);
		});
	});

	var userAgent = navigator.userAgent.toLowerCase();
	if (userAgent.indexOf("msie") > -1) {
		var msieN = /(msie \d)/.exec(userAgent)[0];
		if (msieN <= 'msie 8') {
			var alternateContent=$('#check-browser-fail').clone();
			$('body').empty();
			$('body').append(alternateContent);
		}
	}
});