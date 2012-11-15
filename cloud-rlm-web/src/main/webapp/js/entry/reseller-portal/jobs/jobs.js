$(document).ready(function() {
	var $form = $('#search-form');

	$('#query-filters').find('a').click(function(e) {
		e.preventDefault();
		if ($(this).parent('li').is('.selected')) {
			$form.find('input[name="filter"]').val('');
		} else {
			$form.find('input[name="filter"]').val($(this).attr('data-val'));

		}
		$form.submit();
	});

	$form.find('#cancel').click(function(e) {
		e.preventDefault();
		$form.find(':text,:hidden,:selected').val('');
		$form.find("#search").click();
	});

});
