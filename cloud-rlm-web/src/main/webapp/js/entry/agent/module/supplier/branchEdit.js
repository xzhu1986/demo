$(document).ready(function() {
	var $detailForm = $('#branch-form');
	$detailForm.disableForm();
	// mask all at begin
	$detailForm.find('select[name="address.countryCode"]').attr('disabled', 'disabled');
	
	$detailForm.find('.postal-copy-btn').click(function(e) {
		e.preventDefault();
		$('input[name="postalAddress.address1"]').val($detailForm.find('input[name="address.address1"]').val());
		$('input[name="postalAddress.address2"]').val($detailForm.find('input[name="address.address2"]').val());
		$('input[name="postalAddress.address3"]').val($detailForm.find('input[name="address.address3"]').val());
		$('input[name="postalAddress.city"]').val($detailForm.find('input[name="address.city"]').val());
		$('input[name="postalAddress.postcode"]').val($detailForm.find('input[name="address.postcode"]').val());
	});
	
	$detailForm.find('.warehouse-copy-btn').click(function(e) {
		e.preventDefault();
		$('input[name="warehouseAddress.address1"]').val($detailForm.find('input[name="address.address1"]').val());
		$('input[name="warehouseAddress.address2"]').val($detailForm.find('input[name="address.address2"]').val());
		$('input[name="warehouseAddress.address3"]').val($detailForm.find('input[name="address.address3"]').val());
		$('input[name="warehouseAddress.city"]').val($detailForm.find('input[name="address.city"]').val());
		$('input[name="warehouseAddress.postcode"]').val($detailForm.find('input[name="address.postcode"]').val());
	});
	$detailForm.find('select[name="address.region"]').on('change', function(e) {
		$('input[name="postalAddress.region"]').val($(this).val());
		$('input[name="warehouseAddress.region"]').val($(this).val());
	});
	
	$('#query-filters').find('a').click(function(e) {
		e.preventDefault();
		if ($(this).parent('li').is('.selected')) {
			$(this).parent('li').removeClass('selected');
			$form.find('input[name="filter"]').val('');
		} else {
			$form.find('input[name="filter"]').val($(this).attr('data-val'));

		}
		$form.find("#search").click();
	});
	
});
