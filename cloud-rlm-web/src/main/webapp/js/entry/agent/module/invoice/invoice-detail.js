$(document).ready(function() {
	var $detailForm = $('#detail-form');
	$(":date").dateinput({
		format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
		speed		: 'fast', // calendar reveal speed
		firstDay	: 1
	});
	// mask all at begin
	if ($('#invoiceNumber').val()) {
		$detailForm.disableForm(function($form) {
			showMailBtn();
			$detailForm.find('.invoiceChildBtn').hide();
		});
	}

	function showMailBtn() {
		var mailRegion$ = $detailForm.find('.emailBtn');
		mailRegion$.show();
		mailRegion$.find(':input').enable();
	}

	$detailForm.find('.edit-btn').click(function(e) {
		e.preventDefault();
		hideMailBtn();
		$detailForm.find('.invoiceChildBtn').show();
		$detailForm.find('#detailsDone').hide();
		$detailForm.find('#termsDone').hide();
	});

	var optionFmt = '<option value="{0}">{1}</option>';
	// agent change
	$('#agentId').on('change', function(e) {
		var $sel = $(this);
		var agentId = $sel.val();
		if (agentId) {
			// loadSingle($('#regionCode'), agentId, "regionCode", "nameDesc");
			loadSingle($('#salesRepId'), agentId, "userId", "username");
			changeTaxRateDisp();
		}
	});
	// region change
	function changeTaxRateDisp() {
		var agentId = $('#agentId').val();
		var regionCode = $('#regionCode').val();
		if (agentId && regionCode) {
			var url = '{0}/invoice/tax-rate-disp/{1}/{2}.json'.format(basePath, agentId, regionCode);
			$.getJSON(url, function(data) {
				if (data.result.data) {
					$('#taxRateDisp').val(data.result.data.value);
				}
			});
		}
	}

	// init
	if ($('#agentId').val()) {
		// loadSingle($('#regionCode'), $('#agentId').val(), "regionCode", "nameDesc");
		loadSingle($('#salesRepId'), $('#agentId').val(), "userId", "username");
//		changeTaxRateDisp();
	}

	function loadSingle($sel, parentVal, keyName, valueName) {
		if (!$sel)
			return;
		$sel.empty();
		// $sel.append('<option value=""></option>');
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

	isell.PopupFilter({
		elementId:'currency',
		url		          : basePath + "/settings/Currencies/query.json",
		root		      : 'result.data',
		winConfig : {
			follow:'#currency'
		},
		column		      : [{
			        name		: 'name',
			        headName	: 'Name'
		        }, {
			        name		: 'code',
			        headName	: 'Code'
		        }, {
			        name		: 'symbol',
			        headName	: 'Symbol'
		        }],
		rowChooseCallback	: function($this,rowData) {
			$this.val(rowData.code);
		}
	});

	// edit details and terms
	$('#createDetails').click(function() {
		$('#details').removeAttr('disabled');
	});
	$('#createTerms').click(function() {
		$('#terms').removeAttr('disabled');
	});

	// calculate amount
	$('[name^="amount"]').change(function() {
		var total = 0;
		var taxRate = pf($('#taxRateDisp').val()) / 100 + 1;
		$('[name^="amount"]').each(function() {
			var v = $(this).val();
			if (v && !isNaN(pf(v))) {
				total += pf(v);
			}
		});
		$('#totalAmount').val(total);
		$('#totalAmountInc').val(tf(taxRate * total));
	});

	totalAmountIncValidate();

	$('.editFollowup').live('click', function(e) {
		e.preventDefault();
		var invoiceNumber = $detailForm.find(':input[name="invoiceNumber"]').val();
		var followupDate = $(this).attr('data-followupdate');
		if ($detailForm.find('.save-btn').is(":hidden"))
			return;
		var url = "{0}/invoice/{1}/followup/{2}".format(basePath, invoiceNumber, followupDate);
		JsUtils.openWindow(url, "Edit Payment Followup", 600, 600);
	});

	$('.newSchedule').live('click', function(e) {
		e.preventDefault();
		var invoiceNumber = $detailForm.find(':input[name="invoiceNumber"]').val();
		if ($detailForm.find('.save-btn').is(":hidden"))
			return;
		var url = "{0}/invoice/{1}/schedule/new".format(basePath, invoiceNumber);
		JsUtils.openWindow(url, "New Payment Schedule", 600, 600);
	});

	$('.editSchedule').live('click', function(e) {
		e.preventDefault();
		var invoiceNumber = $detailForm.find(':input[name="invoiceNumber"]').val();
		var dueDate = $(this).attr('data-dueDate');
		
		if ($detailForm.find('.save-btn').is(":hidden"))
			return;
		var url = "{0}/invoice/{1}/schedule/{2}".format(basePath, invoiceNumber, dueDate);
		JsUtils.openWindow(url, "Edit Payment Schedule", 600, 600);
	});

	$('.autoGenerateSchedule').live('click', function(e) {
		e.preventDefault();
		var invoiceNumber = $detailForm.find(':input[name="invoiceNumber"]').val();
		if ($detailForm.find('.save-btn').is(":hidden"))
			return;
		var url = "{0}/invoice/{1}/autoschedules".format(basePath, invoiceNumber);
		JsUtils.openWindow(url, "Auto Generate Payment Schedule", 600, 600);
	});

	$('.deleteSchedule').live('click', function(e) {
		e.preventDefault();

		var invoiceNumber = $detailForm.find(':input[name="invoiceNumber"]').val();
		var dueDates = "";
		$detailForm.find('.del-schedule-checkbox').each(function() {
			if ($(this).attr("checked") == "checked") {
				dueDates += $(this).attr('data-dueDate') + ",";
			}
		});
		if (dueDates == '') {
			alert("Please select a item!");
			return;
		}
		var confirmv = confirm("Continue this operation [delete this item] ?")
		if (!confirmv)
			return;
		if (invoiceNumber && invoiceNumber != '' && invoiceNumber != 'new') {
			// notify server
			var url = '{0}/invoice/{1}/schedule/{2}'.format(basePath, invoiceNumber, dueDates);
			$.ajax({
				url			: url,
				success		: function(data, textStatus, jqXHR) {
					if (data && data.success) {
						flushScheduleList();
					}
				},
				error		: function(jqXHR, textStatus, errorThrown) {
					alert(errorThrown)
				},
				dataType	: "json",
				type		: 'DELETE'
			});
		}
	});
	
	//lock & unlock
	function switchLockStatus(hideBtn$,showBtn$,textArea$,disabled){
		hideBtn$.hide();
		showBtn$.show();
		if(disabled) textArea$.attr('disabled','disabled');
		else textArea$.removeAttr('disabled');
	}
	$('#detailsEdit').click(function(){
		switchLockStatus($(this),$('#detailsDone'),$('#details'),false);
	});
	$('#detailsDone').click(function(){
		switchLockStatus($(this),$('#detailsEdit'),$('#details'),true);
	});
	$('#termsEdit').click(function(){
		switchLockStatus($(this),$('#termsDone'),$('#terms'),false);
	});
	$('#termsDone').click(function(){
		switchLockStatus($(this),$('#termsEdit'),$('#terms'),true);
	});
	
	
});
function hideMailBtn() {
	var mailRegion$ = $('#detail-form').find('.emailBtn');
	mailRegion$.hide();
}
function flushScheduleList() {
	var invoiceNumber = $('#detail-form').find(':input[name="invoiceNumber"]').val();
	var url = '{0}/invoice/{1}/schedulelist'.format(basePath, invoiceNumber);
	$.ajax({
		url		: url,
		success	: function(data, textStatus, jqXHR) {
			if (data) {
				$("#invoice-schedule-list").html(data);
				flushTotalOwning();
			}
		},
		error	: function(jqXHR, textStatus, errorThrown) {
			alert(errorThrown)
		},
		type	: 'GET'
	});
}
function totalAmountIncValidate() {
	if (matchAmount()) {
		$('#totalAmountInc').validate_callback(null, "sucess"); 
	} else {
		$('#totalAmountInc').validate_callback("", "failed"); 
																
		hideMailBtn();
		// return false;
	}
}

function matchAmount(){
	return Number($('#totalAmountInc').val()) == Number($('#totalOwning').val()) + Number($('#totalPaid').val());
}

function flushTotalOwning() {
	if ($('#scheduleTotalOwning')) {
		$('#totalOwning').val($('#scheduleTotalOwning').val());
	} else {
		$('#totalOwning').val(0);
	}
	if ($('#scheduleTotalPaid')) {
		$('#totalPaid').val($('#scheduleTotalPaid').val());
	} else {
		$('#totalPaid').val(0);
	}

	totalAmountIncValidate();
}

function flushFollowupList() {
	var invoiceNumber = $('#detail-form').find(':input[name="invoiceNumber"]').val();
	var url = '{0}/invoice/{1}/followuplist'.format(basePath, invoiceNumber);
	$.ajax({
		url		: url,
		success	: function(data, textStatus, jqXHR) {
			if (data) {
				$("#invoice-followup-list").html(data);
			}
		},
		error	: function(jqXHR, textStatus, errorThrown) {
			alert(errorThrown)
		},
		type	: 'GET'
	});
}

