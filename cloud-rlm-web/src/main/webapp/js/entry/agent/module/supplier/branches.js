$(document).ready(function() {
	var $form = $('#branch-search-form');

	$('#query-filters').find('a').click(function(e) {
		e.preventDefault();
		if ($(this).parent('li').is('.selected')) {
			$(this).parent('li').removeClass('selected');
			$form.find('input[name="filter"]').val('');
		} else {
			$form.find('input[name="filter"]').val($(this).attr('data-val'));

		}
		$form.submit();
	});

	$(".is-head-office-radio").change(function(e) {
		e.preventDefault();
		if(this.checked==true){
			//var btn = confirm("Delete this ?");
			var dataUrl='{0}/supplier/{1}/branches/{2}/savesupplierdefaultbranch.json';
			$.get(dataUrl.format(basePath,$(this).attr('data-value'),$(this).val()),function(data){
				if(data.result.success){
					alert(data.result.msg);
				}
			});
		}
	});
});

