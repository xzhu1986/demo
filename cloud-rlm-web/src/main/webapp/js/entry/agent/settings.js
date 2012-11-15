$(document).ready(function() {
	var $form = $('#setting-search-form');

	$form.find('#cancel').click(function(e) {
		e.preventDefault();
		$form.find(':text,:selected').val('');
		$form.find("#search").click();
	});

	$("#dialog-select-country").dialog({
		autoOpen	: false,
		height		: 200,
		width		: 300,
		modal		: true,
		buttons		: {
			Confirm	: function() {
				var selVal=$("#form-select-country").find("#country option:selected").attr("value");
				var url="{0}?country={1}".format($("#createAgent").attr('href'),selVal);
				window.open(url);
				$(this).dialog("close");
			},
			Cancel	: function() {
				$(this).dialog("close");
			}
		},
		close		: function() {
		}
	});

	$("#createAgent").click(function(e) {
		e.preventDefault();
		$("#dialog-select-country").dialog("open");
	});

	$form.find('.delete-country').click(function(e) {
		e.preventDefault();
		if(confirm("Delete this country?")){
			$.ajax({
				   url: $(this).attr('data-url'),
				   type: 'DELETE',
				   success: function( data ) {
					   if(data && data.result.success){
						   window.location.reload();
						}
				   }
			});
		}
	});
	
	$form.find('.delete-currency').click(function(e) {
		e.preventDefault();
		if(confirm("Delete this currency?")){
			$.ajax({
				   url: $(this).attr('data-url'),
				   type: 'DELETE',
				   success: function( data ) {
					   if(data && data.result.success){
						   window.location.reload();
						}
				   }
			});
		}
	});
	
	$form.find('.delete-email-tpl').click(function(e) {
		e.preventDefault();
		if(confirm("Delete this item?")){
			$.ajax({
				   url: $(this).attr('href'),
				   type: 'DELETE',
				   success: function( data ) {
					   if(data && data.result.success){
						   window.location.reload();
						}
				   }
			});
		}
	});
});
