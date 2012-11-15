$(document).ready(function() {
	var $detailForm = $('#detail-form');
	// mask all at begin
	$detailForm.disableForm();

	$('#email').click(function(e) {
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
	var password$ = $('[name="resellerUser.password"]');
	if( password$.val() == $('#confirmpassword').val() ){
		password$.validate_callback(null,"sucess");	
	}else{
		password$.validate_callback("confirm password must same as password","failed");
		return false;
	}
}