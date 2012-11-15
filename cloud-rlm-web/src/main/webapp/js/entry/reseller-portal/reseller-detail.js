$(document).ready(function() {
	var $detailForm = $('#detail-form');
	$detailForm.find('[name$=".countryCode"]').addClass('disabled');
	// mask all at begin
	$detailForm.disableForm();

	$detailForm.find('.cancel-btn').click(function(e) {
		$detailForm.disableForm(function($form) {
			$detailForm.find('.edit-btn').show();
		});
	});
	
	$('#reset-password').click(function(e) {
		e.preventDefault();
		var userid = $(this).attr('data-userid')
		$.get('{0}/security/password/ask-reset/{1}.json'.format(basePath, userid), function(data) {
			if (data && data.result.msg) {
				alert(data.result.msg);
			}
		});
	});

	$('#agentId').on('change', function(e) {
		var $sel = $(this);
		var agentId = $sel.val();
		if (agentId) {
			loadSingle($('#salesRepId'), agentId, "userId", "username");
			
			initGstSelect();
		}
	});

	function initGstSelect() {
		var agentId = $('#agentId').val();
		if(!agentId)return;
		var countryCode = $detailForm.find('[name$=".countryCode"]').val();
		$.getJSON('{0}/agent/info/{1}.json'.format(basePath, agentId), function(data) {
			if (countryCode == data.result.data.country) {
				var gst$=$('#gst');
				gst$.val(gst$[0].lastElementChild.value);
			}
		});
	}
	
	if(!$('#serialNo').val()){
		initGstSelect();
	}

	if ($('#agentId').val()) {
		loadSingle($('#salesRepId'), $('#agentId').val(), "userId", "username");
	}

	var optionFmt = '<option value="{0}">{1}</option>';
	function loadSingle($sel, parentVal, keyName, valueName) {
		if (!$sel)
			return;
		$sel.empty();
		$sel.append('<option value=""></option>');
		var selVal = $sel.val() ? $sel.val() : $sel.attr('data-value');
		// url
		var url = $sel.attr('data-url');
		if (url.indexOf('{0}') > -1 && parentVal) {
			url = url.format(parentVal)
		} else if (url.indexOf('{0}') > -1) {
			return;
		}
		// load
		$.get(url, function(data) {
			if (data && data.result.data) {
				var dlist = data.result.data;
				$.each(dlist, function(i) {
					var optItem = optionFmt.format(dlist[i][keyName], dlist[i][valueName]);
					if (dlist[i][keyName] == selVal) {
						$sel.append($(optItem).attr('selected', 'selected'));
					} else {
						$sel.append(optItem);
					}
				});
			}
		});
	}

});

var isExtendsValidate = true;
function extendsValidate(){	
	var $detailForm = $('#detail-form');
	var email = $detailForm.find('input[name="resellerUser.email"]');
	if( email.val().substring(email.val().indexOf('@')) == email.attr("data-old-val").substring(email.attr("data-old-val").indexOf('@'))){
		email.validate_callback(null,"sucess");	
	}else{
		email.validate_callback("you can't change your email domain!","failed");
		return false;
	}
	
	var billingInfoEmail = $detailForm.find('input[name="billingInfo.email"]');
	if( billingInfoEmail.val().substring(billingInfoEmail.val().indexOf('@')) == billingInfoEmail.attr("data-old-val").substring(billingInfoEmail.attr("data-old-val").indexOf('@'))){
		billingInfoEmail.validate_callback(null,"sucess");
	}else{
		billingInfoEmail.validate_callback("you can't change your email domain!","failed");	
		return false;
	}
	
	// check username
	var username$ = $('[name="resellerUser.username"]');
	if (username$.attr('data-old') && username$.attr('data-old') != username$.val()) {
		var url = '{0}/users/check-duplicate-username.json?userid={1}&username={2}'.format(basePath, $('[name="resellerUser.userId"]').val(), username$.val());
		var success = false;
		$.ajax(url, {
			async	: false,
			success	: function(data) {
				success = data.result.success;
			}
		});
		if (!success) {
			username$.validate_callback("duplicate username", "failed");
			return false;
		} else {
			username$.validate_callback(null, "success");
		}
	}
	
	var password$ = $('[name="resellerUser.password"]');
	if( password$.val() == $('#confirmpassword').val() ){
		password$.validate_callback(null,"sucess");
	}else{
		password$.validate_callback("confirm password must same as password","failed");
		return false;
	}
	
}