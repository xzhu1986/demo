$(document).ready(function() {
	var $detailForm = $('#detail-form');
	$('.currency-item select').combobox();
	
	// mask all at begin
	$detailForm.disableForm();
	
	$('.currency-item select').on('change', function(e) {
		var $sel = $(this);
		var currencyCode = $sel.find("option:selected").attr('data-currency-code');
		var countryUnit = $sel.find("option:selected").attr('data-currency-unit');
		$detailForm.find('input[name="currency.code"]').val(currencyCode);
		$detailForm.find('input[name="currency.minorUnit"]').val(countryUnit);
	});
});