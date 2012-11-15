$(document).ready(function() {
	var $form = $('#invoice-search-form');

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

	$form.find('#cancel').click(function(e) {
		e.preventDefault();
		$form.find(':text,:hidden,:selected').val('');
		$form.find("#search").click();
	});
	
});
