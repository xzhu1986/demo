$(document).ready(function() {
	var $detailForm = $('#detail-form');
	$('.currency-item select').combobox();

	// mask all at begin
	function maskAllFiels() {
		if ($('.edit-btn').length > 0) {
			$detailForm.disableForm();
		}
	}
	var agentId=$('#detail-form').find('input[name="agentId"]').val();
	/*if(agentId && agentId!=''){
		maskAllFiels();
	}*/
	$detailForm.disableForm();
	$('.currency-item select').on('change', function(e) {
		var $sel = $(this);
		var currencyCode = $sel.find("option:selected").attr('data-currency-code');
		var countryUnit = $sel.find("option:selected").attr('data-currency-unit');
		$detailForm.find('input[name="currency.code"]').val(currencyCode);
		$detailForm.find('input[name="currency.minorUnit"]').val(countryUnit);
	});

	// add rate event
	$('.taxRateItem .add-rate').live('click', function(e) {
		e.preventDefault();
		var $parentItem = $(this).parents('.taxRateItem');
		var $copy = $parentItem.clone();
		$copy.find('input[data-name="taxRate.taxRateId"]').val('');
		$copy.find('input[data-name="taxRate.rate"]').val('');
		$parentItem.after($copy);
	});

	// del rate event
	$('.taxRateItem .del-rate').live('click', function(e) {
		e.preventDefault();
		var $parentItem = $(this).parents('.taxRateItem');
		var id = $parentItem.find('input[data-name="taxRate.taxRateId"]').val();
		if (id && id != '') {
			// notify server
			var url = '{0}/settings/Agents/tax/{1}.json'.format(basePath, id);
			$.ajax({
				url		: url,
				success	: function(data, textStatus, jqXHR) {
					if(data && data.result)
						alert(data.result.msg);
				},
				error : function(jqXHR, textStatus, errorThrown){
					alert(errorThrown)
				},
				type : 'DELETE'
			});
		}
		$parentItem.remove();
	});

	$detailForm.submit(function() {
		var dataField = $('#taxRateDatas');
		var datas = [];
		$('.taxRateItem').each(function() {
			var id = $(this).find('input[data-name="taxRate.taxRateId"]').val();
			var val = $(this).find('input[data-name="taxRate.rate"]').val();
			datas.push({
				id		: id,
				rate	: val
			});
		});
		if (datas.length > 0) {
			console.log(JsUtils.encode(datas));
			dataField.val(JsUtils.encode(datas));
		}
	});
});
