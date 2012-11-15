$(document).ready(function() {
	var $detailForm = $('#detail-form');
	// mask all at begin
	$detailForm.disableForm(function(){
		$('#request').show();
	});
});