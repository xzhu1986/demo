$(document).ready(function() {
	var $form = $('#reseller-search-form');

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

	var optionFmt = '<option value="{0}">{1}</option>';
	var $region = $('#region');
	$('select#country').on('change', function(e) {
		var $sel = $(this);
		var name = $sel.attr('name');
		var countryCode = $sel.val();
		$region.val('');
		loadSingle($region, countryCode);
	});

	
	if($region.attr('data-value')){
		loadSingle($region,$('select#country').val());
	}
	
	function loadSingle($sel, parentVal) {
		if (!$sel)
			return;
		$sel.empty();
		$sel.append('<option value=""></option>');
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
					var optItem = optionFmt.format(dlist[i].code, dlist[i].name);
					if (dlist[i].code == selVal) {
						$sel.append($(optItem).attr('selected', 'selected'));
					} else {
						$sel.append(optItem);
					}
				});
			}
		});
		// child
//		if ($sel.attr('data-cascading')) {
//			var childName = $sel.attr('data-cascading');
//			if (childName) {
//				var $child = $('select[name="' + childName + '"]');
//				loadSingle($child, selVal);
//			}
//		}
	}
	
	$( "#dialog-select-country" ).dialog({
		autoOpen: false,
		height: 200,
		width: 300,
		modal: true,
		buttons: {
			Confirm: function() {
				window.open(basePath+"/resellers/-1/detail?reseller.country="+$("#form-select-country").find("#country option:selected").attr("value"),"_blank");
				$( this ).dialog( "close" );
			},
			Cancel: function() {
				$( this ).dialog( "close" );
			}
		},
		close: function() {
		}
	});

	$( "#select-country" ).click(function() {
		$( "#dialog-select-country" ).dialog( "open" );
	});

});
