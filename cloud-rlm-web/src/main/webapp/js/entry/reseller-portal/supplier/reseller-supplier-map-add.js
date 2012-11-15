$(document).ready(function() {
	var url = basePath + "/supplier/{0}/supplier-datarequest.json".format($('#supplier').val());
	$.ajax(url, {
		dataType	: 'json',
		success		: function(data, textStatus, jqXHR) {
			DATA_REQUEST = data.result.data;
		}
	});

	$('#request').click(function(e) {
		e.preventDefault();
		mapWindow = isell.InnerWindow({
			url			: $(this).attr('data-url'),
			containerId	: 'reseller-map-container',
			callback	: function(innerWin) {
				init();
			}
		});
	});

	function init() {
		// $('#supplier').combobox();
		if ($('#supplier') && $('#supplier').val() != "") {
			loadSingle($('#priceBreak'), $('#supplier').val());
			changeHtmlShow($('#supplier').val());
		}
		$(":date").dateinput({
			format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
			speed		: 'fast', // calendar reveal speed
			firstDay	: 1
		});

		$('#map-form').submit(function(e) {
			e.preventDefault();
			var $form = $('#map-form');
			if(formValid($form)){
				$form.mask("Wait a moment ...");
				$.post($form.attr('action'),JsUtils.serializeForm($form),function(data) {
					if(data&&data.result.msg){
						alert(data.result.msg);
					}
					$('#map-form').unmask();
//					mapWindow.close();
				});
			}
			return false;
		});
	}
	function changeHtmlShow(supplierId) {
		var dataRequest = DATA_REQUEST;
		if (dataRequest.requireLoginName) {
			$('#requireLoginName').show();
			// $('#map-form').find('input[name="supplierUsername"]').val('');
		} else {
			$('#requireLoginName').hide();
			// $('#map-form').find('input[name="supplierUsername"]').val('');
		}

		if (dataRequest.requireLoginPassword) {
			$('#requireLoginPassword').show();
			// $('#map-form').find('input[name="supplierPassword"]').val('');
		} else {
			$('#requireLoginPassword').hide();
			// $('#map-form').find('input[name="supplierPassword"]').val('');
		}

		// if (dataRequest.requireAccountNo) {
		// $('#requireAccountNo').show();
		// // $('#map-form').find('input[name="supplierAccountNumber"]').val('');
		// } else {
		// $('#requireAccountNo').hide();
		// // $('#map-form').find('input[name="supplierAccountNumber"]').val('');
		// }
	}

	var optionFmt = '<option value="{0}">{1}</option>';
	function loadSingle($sel, parentVal) {
		if ($sel.length <= 0)
			return;
		if (!$sel)
			return;
		$sel.empty();
		$sel.append('<option value=""></option>');
		var selVal = $sel.val() ? $sel.val() : $sel.attr('data-value');
		// url
		var url = $sel.attr('data-url').format(parentVal);
		// load
		$.get(url, function(data) {
			if (data && data.result.data) {
				var dlist = data.result.data;
				$.each(dlist, function(i) {
					var optItem = optionFmt.format(dlist[i].priceBreakId, dlist[i].name);
					if (dlist[i].priceBreakId == selVal) {
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
function extendsValidate() {
	var isExisting = $('[name=isNewaccount]:checked').val() == 'existing';
	var supplierAccountNumber$ = $('[name=supplierAccountNumber]');
	var supplierUsername$ = $('[name=supplierUsername]');
	var supplierPassword$ = $('[name=supplierPassword]');
	var requrireAccountNumber = DATA_REQUEST.requireAccountNo;
	var requireLogingName = DATA_REQUEST.requireLoginName;
	var requireLoginPassword = DATA_REQUEST.requireLoginPassword;

	var r = assertExists(supplierAccountNumber$, requrireAccountNumber == true && isExisting, 'required');
	if (!r)
		return false;
	var r = assertExists(supplierUsername$, requireLogingName == true && isExisting, 'required');
	if (!r)
		return false;
	var r = assertExists(supplierPassword$, requireLoginPassword == true && isExisting, 'required');
	if (!r)
		return false;

}
function assertExists(field$, condition, msg) {
	if (condition && !field$.val()) {
		field$.validate_callback(msg, "failed");
		return false;
	} else {
		field$.validate_callback(null, "sucess");
		return true;
	}
}