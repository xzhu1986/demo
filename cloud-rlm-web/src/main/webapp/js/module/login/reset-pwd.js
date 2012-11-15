function focusPwd() {
	document.getElementById("password").focus();
}
focusPwd();

$(document).ready(function() {
	$("#update-pwd-form").submit(function(e) {
		e.preventDefault();
		var pwd1 = $('#password').val();
		var pwd2 = $('#password-repeat').val();
		if (pwd1 !== pwd2) {
			alert('Two password are not equal!');
			return false;
		}
		$.post(basePath + '/security/password/reset.json', {
			hash	: $('#hash').val(),
			pwd		: pwd1
		}, function(data) {
			if (data && data.result.msg) {
				alert(data.result.msg);
				if (!data.result.success){
					focusPwd();
				}else{
					window.location.href=basePath+'/login';
				}
			}
		});
	});
});