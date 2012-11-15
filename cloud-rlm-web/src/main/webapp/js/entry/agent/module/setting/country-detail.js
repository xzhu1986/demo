$(document).ready(function() {
	var $detailForm = $('#detail-form');

	$('.country-item select').combobox();
	// $('.country-item select').toggle();
	// mask all at begin
	$detailForm.disableForm();

	var $showRegion = $detailForm.find('input[name="showRegion"]');
	var $regionType = $detailForm.find('input[name="regionType"]');
	if ($showRegion.attr("checked") == "checked") {
		$regionType.attr("reg", "^.+$");
		$regionType.attr("tip", "not blank");
		$('#regionDiv').show();
	} else {
		$regionType.removeAttr("reg");
		$regionType.removeAttr("tip");
		$('#regionDiv').hide();
	}

	$showRegion.click(function() {
		if ($(this).attr("checked") == "checked") {
			$regionType.attr("reg", "^.+$");
			$regionType.attr("tip", "not blank");
			$('#regionDiv').show();
		} else {
			$regionType.removeAttr("reg");
			$regionType.removeAttr("tip");
			$('#regionDiv').hide();
		}
	})

	$('.country-item select').on('change', function(e) {
		var $sel = $(this);
		var countryCode = $sel.find("option:selected").attr('data-country-code');
		var currencyCode = $sel.find("option:selected").attr('data-currency-code');
		$detailForm.find('input[name="country.code"]').val(countryCode);

		if (currencyCode && currencyCode != '' && confirm("Use the default currency?")) {
			var valid = false;
			$('.currency-item select').children("option").each(function() {
				if ($(this).val().match(currencyCode)) {
					this.selected = valid = true;
				}
			});
			// load
			if (!valid) {
				$.get('{0}/settings/Currencies/{1}/default.json'.format(basePath, currencyCode), function(data) {
					if (data && data.result.msg == 'ok' && data.result.data) {
						$('.currency-item select').empty();
						$('.currency-item select').append('<option value=""></option>');
						var optionFmt = '<option value="{0}">{1}</option>';
						var dlist = data.result.data;
						$.each(dlist, function(i) {
							var optItem = optionFmt.format(dlist[i].code, dlist[i].code);
							if (dlist[i].code == currencyCode) {
								$('.currency-item select').append($(optItem).attr('selected', 'selected'));
							} else {
								$('.currency-item select').append(optItem);
							}
						});
					}
				});
			}
		}
	});

	var rowTpl = "<tr class='regionRow'><input type='hidden' data-name='code' /><td><input type='text' class='form-field3' reg='^.+$' tip='not blank' data-name='name'/></td>"
	        + "<td><input type='text' class='form-field3' data-name='shortName'/></td><td><a href='' class='delete-region-btn'>Delete</a></td></tr>";
	$detailForm.find('.add-region-btn').click(function(e) {
		e.preventDefault();
		// if is disabled ,return
		if ($detailForm.find('.save-btn').is(":hidden"))
			return;
		enableBtn($(this));
		$("#RegionTable").append(rowTpl);
	});

	// save
	$detailForm.submit(function() {
		var dataField = $('#regionDatas');
		var datas = [];
		$('.regionRow').each(function() {
			var item = {};
			$(this).find(':input').each(function() {
				var name = $(this).attr('data-name');
				if (name) {
					var value = $(this).val();
					value = value ? value : '';
					item[name] = value;
				}
			});
			datas.push(item);
		});
		if (datas.length > 0) {
//			 console.log(JsUtils.encode(datas));
			dataField.val(JsUtils.encode(datas));
		}
	});

	// delete
	$('.regionRow .delete-region-btn').live('click', function(e) {
		e.preventDefault();
		var confirmv = confirm("Continue this operation [delete this item] ?")
		if (!confirmv)
			return;
		var $parentItem = $(this).parents('.regionRow');
		var code = $parentItem.find('input[data-name="code"]').val();
		if (code && code != '') {
			// notify server
			var url = '{0}/address/address-item/{1}.json'.format(basePath, code);
			$.ajax({
				url		: url,
				success	: function(data, textStatus, jqXHR) {
					if (data && data.result)
						alert(data.result.msg);
				},
				error	: function(jqXHR, textStatus, errorThrown) {
					alert(errorThrown)
				},
				type	: 'DELETE'
			});
		}
		$parentItem.remove();
	});
});

var isExtendsValidate = true; //important
function extendsValidate() { //important  name  same as item
	var regionName$=$('#regionName');
	if($('#hasRegions').is(':checked') && !regionName$.val()){
		regionName$.validate_callback("should not be blank", "failed");
		return false;
	}
	regionName$.validate_callback(null, "sucess");
}
