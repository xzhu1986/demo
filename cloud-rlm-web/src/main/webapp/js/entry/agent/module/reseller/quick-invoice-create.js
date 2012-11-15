$(document).ready(function() {
	$('#newBusiness,#renewalInvoice').click(function(e) {
		e.preventDefault();
		var this$ = $(this);
		this$.mask();
		$('#invoiceQuickCreate').load($(this).attr('href'), function(responseText, textStatus, XMLHttpRequest) {
			this$.unmask();
			if (XMLHttpRequest.status != 200) {
				alert(XMLHttpRequest.statusText + ".");

			} else {
				$(":date").dateinput({
					format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
					speed		: 'fast', // calendar reveal speed
					firstDay	: 1
				});
				INVOICE_QUICK_DLG=art.dialog({
					content	: document.getElementById('invoiceQuickCreate')
					// left : opts.left,
					// top : opts.top
				});
				calTotal() ;
			}
		});
	});

	function calTotal() {
		var total = 0;
		$('#invoiceQuickCreate .data-row').find(':checked').each(function() {
			var textFiled = $(this).parents('.data-row').find('.annualFee');
			var v = parseFloat(textFiled.val());
			v = isNaN(v) ? 0 : v;
			total = total + v;
		});
		$('#totalAmount').text(total);
	}

	$('#invoiceQuickCreate .data-row :input').live('change', function() {
		calTotal();
	});

	$('#submitRenewalInvoice').live('click', function() {
		var nextRenewal = {};
		var nextRenewal$ = $('[name="nextRenewal"]');
		if (nextRenewal$.length > 0) {
			var val = nextRenewal$.val();
			var checked = nextRenewal$.parent('td').next('td').find(':checkbox').is(":checked");
			nextRenewal.val = val;
			nextRenewal.checked = checked;
		}

		var lic = {};
		var checkedItemCount=0;
		$('.data-row').each(function() {
			var annualFee$ = $(this).find('.annualFee');
			if (annualFee$.is(':enabled')) {
				var val = annualFee$.val();
				var checked = $(this).find('.annualFeeCheck').is(":checked");
				lic[$(this).find('.annualFee').attr('name')] = {
					val		: val,
					checked	: checked
				}
				if(checked) checkedItemCount++;
			}
		});
		
		if(checkedItemCount==0){
			alert('No item is checked.')
			return;
		}
		if(!$('#nextRenewalCheckBox').is(":checked")){
			alert('Please confirm next renewal date.');
			return;
		}
		
		$('#renewalInvoiceForm').mask('Creating ...');
		$.post($('#renewalInvoiceForm').attr('action'), {
			lics		: JsUtils.encode(lic),
			nextRenewal	: JsUtils.encode(nextRenewal)
		}, function(data, textStatus, jqXHR) {
			$('#renewalInvoiceForm').unmask();
			if(data.result.data){
				window.open(basePath+data.result.data);
				INVOICE_QUICK_DLG.close();
			}
		}, 'json');
	});
});