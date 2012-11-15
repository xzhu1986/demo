$(document).ready(function() {
	var $detailForm = $('#detail-form');

	initAddressItems();

	// save
	$detailForm.submit(function(e) {
		var hasUnconfirmedData = $('.confirm-pending').not(':hidden').length > 0;
		if (hasUnconfirmedData) {
			alert("Please confirm all region code selections");
			e.preventDefault();
			e.stopPropagation();
			return false;
		}
	});

	// select region code,cascading
	var urlFmt = basePath + '/address/sub-addresses/{0}.json';
	var optionFmt = '<option value="{0}">{1}</option>';
	$('.regionCodeSels select').on('change', function(e) {
		// change flag
		resetData($(this).parents('.regionCodeCol'));
		var $next = $(this).next('select');
		var $previous = $(this);
		if ($next.length == 0) {
			$next = $('<select/>');
		}else{
			$next.empty();
		}
		var url = urlFmt.format($(this).val());
		// load options
		$.get(url, function(data) {
			var optionContainer = [];
			if (data && data.result.data) {
				var dlist = data.result.data;
				$.each(dlist, function(i) {
					var optItem = optionFmt.format(dlist[i].code, dlist[i].name);
					optionContainer.push(optItem);
				});
				if (optionContainer.length > 0) {
					$next.append('<option/>').append(optionContainer.join('')).insertAfter($previous);
				}
			}
		});
	});

	// confirm
	$('.regionCodeConfirm .confirm-pending').live('click', function(e) {
		e.preventDefault();
		confirmData($(this).parents('.regionCodeCol'));
	});

	function confirmData($regionCodeCol) {
		var $lastSel = $regionCodeCol.find('.regionCodeSels').find('select').last();
		var val = getLastValidVal($lastSel);
		$regionCodeCol.find('input[name="regionCode"]').val(val);
		$regionCodeCol.find('.confirm-ok').show();
		$regionCodeCol.find('.confirm-pending').hide();
	}
	function getLastValidVal($lastSel) {
		if ($lastSel.length == 0)
			return null;
		if ($lastSel.val()) {
			return $lastSel.val();
		}
		var $prev = $lastSel.prev('select');

		$lastSel.remove();// remove empty item
		return getLastValidVal($prev);
	}
	function resetData($regionCodeCol) {
		var $lastSel = $regionCodeCol.find('.regionCodeSels').find('select').last();
		var val = getLastValidVal($lastSel);
		$regionCodeCol.find('input[name="regionCode"]').val('');
		$regionCodeCol.find('.confirm-ok').hide();
		$regionCodeCol.find('.confirm-pending').show();
	}

	function maskAllFiels() {
		if ($('.edit-btn').length > 0) {
			$detailForm.disableForm();
		}
	}
	// init address cascading
	function initAddressItems() {
		$('.regionCodeCol').each(function() {
			var regionCode = $(this).find('input[name="regionCode"]').val();
			if (regionCode) {
				// load select item
				var $this = $(this);
				var url = '{0}/address/address-item-cascading/{1}'.format(basePath, regionCode);
				$this.find('.regionCodeSels').load(url, function() {
					// mask all at begin
					maskAllFiels();
				});
			}
		});
	}

});