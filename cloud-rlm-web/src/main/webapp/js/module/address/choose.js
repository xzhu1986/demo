$(document).ready(function() {
	var optionFmt = '<option value="{0}">{1}</option>';
	var loadHtmlUrlFmt = basePath + '/address/address-html?countryCode={0}&addrFieldName={1}';

	// init from country
//	$('.address-item').find('select[name$="countryCode"]').each(function() {
	$('.address-item').find('.countryCodeDisp').each(function() {
		loadSingle($(this));
	});
	
	$('.address-item select').on('change', function(e) {
		var $sel = $(this);
		var name = $sel.attr('name');
		var countryCode = $sel.val();
		var addrFieldName = name.substring(0, name.indexOf('.'));
		if (/countryCodeDisp$/.test(name)) {
			//set hidden value
			$(this).parents('.address-item').find('input[name$="countryCode"]').val($(this).val());
			// load new html
			$.get(loadHtmlUrlFmt.format(countryCode, addrFieldName), function(data) {
				$('.address-item').find('[name^="' + addrFieldName + '"]').parents('tr.address-item').not($sel.parents('tr.address-item')).remove();
				$sel.parents('tr.address-item').replaceWith(data);
				// reload data
				loadSingle($('select[name="' + name + '"]'));
			});
			//change phoneAreaCodeBind,tax/gst
			var dataUrl='{0}/address/address-item/{1}.json';
			$.get(dataUrl.format(basePath,countryCode),function(data){
				if(data.result.data){
					$('.phone-prefix').val(data.result.data.phoneAreaCodeBind);
					$('select[name$="gst"]').val(data.result.data.tax);
				}
			});
		} else {//load child
			if ($sel.attr('data-cascading')) {
				var childName = $sel.attr('data-cascading');
				if (childName) {
					var $child = $('select[name="' + childName + '"]');
					loadSingle($child, countryCode);
				}
			}
		}
	});

	function loadSingle($sel, parentVal) {
		if (!$sel)
			return;
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
		//child
		if ($sel.attr('data-cascading')) {
			var childName = $sel.attr('data-cascading');
			if (childName) {
				var $child = $('select[name="' + childName + '"]');
				loadSingle($child, selVal);
			}
		}
	}
});
