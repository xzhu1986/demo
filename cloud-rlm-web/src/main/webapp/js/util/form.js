$(document).ready(function() {
	// edit btn event
	$('form').each(function() {
		var $form = $(this);
		$form.find('.edit-btn').click(function(e) {
			e.preventDefault();
			// if (isDisabledBtn($(this)))
			// return;
			$form.enableForm();

		});
	});

	// save btn event
	$('form').each(function() {
		var $form = $(this);
		$form.find('.save-btn').click(function(e) {
			if($(this).is('a')){
				e.preventDefault();
				if (isDisabledBtn($(this)))
					return;
			}
		});
	});

	// go back btn
	$('form').find('.go-back-btn').click(function(e) {
		e.preventDefault();
		history.go(-1);
	});

	// delete btn
	$('form').find('.delete-btn').click(function(e) {
		e.preventDefault();
		if (confirm($(this).attr('data-confirm-msg'))) {
			$.ajax({
				url		: $(this).attr('data-url'),
				type	: 'DELETE',
				success	: function(data) {
					if (data && data.result.success) {
						window.location.href = basePath + data.result.msg;
					}
				}
			});
		};
	});
	
	$('form').find('.clear-calendar-btn').click(function(e) {
		var $obj = $(this).prev(":date");
		if($obj.attr('disabled') != 'disabled'){
			$obj.val('');
		}
	});
});

function disableBtn($btn) {
	if (!$btn)
		return;
	if ($btn.is('a')) {
		$btn.removeAttr("href");
	} else {
		$btn.attr('disabled', 'disabled');
	}
}

function enableBtn($btn) {
	if (!$btn)
		return;
	if ($btn.is('a')) {
		$btn.attr("href", "#");
	} else if ($btn.is(':button')) {
		$btn.removeAttr('disabled');
	}
}

function isDisabledBtn($btn) {
	return ($btn.is('a') && !hasAttr($btn, 'href')) || ($btn.is(":button") && $btn.attr('disabled') == 'disabled');
}
// use function bellow to enable/disable input&button
$.fn.disableForm = function(otherOperation) {
	var otherOpt = otherOperation && (typeof otherOperation == 'function') ? otherOperation : function() {
	};
	return this.each(function() {
		// default operation
		var form$ = $(this);
		assertForm(form$);
		disableItem(form$.find(':input').not('[class*="-btn"]'));
		form$.find('[class*="-btn"]').not('.edit-btn').not('.not-hide-btn').hide();
		// customize your own
		otherOpt(form$);
	});
}
$.fn.enableForm = function(otherOperation) {
	var otherOpt = otherOperation && (typeof otherOperation == 'function') ? otherOperation : function() {
	};
	return this.each(function() {
		// default operation
		var form$ = $(this);
		assertForm(form$);
		enableItem(form$.find(':input').not('[class*="-btn"],.unmodified'));
		form$.find('[class*="-btn"]').not(('.edit-btn')).show();
		form$.find('.edit-btn').hide();
		// customize your own
		otherOpt(form$);
	});
}
function assertForm(form$) {
	if (!form$.is('form')) {
		console.error(form$ + ' is not a form');
		return;
	}
}
function disableItem(items$) {
	items$.each(function() {
		var item$ = $(this);
		if (item$.is('a')) {
			item$.removeAttr("href");
		} else {
			item$.attr('disabled', 'disabled');
		}
	});
}
function enableItem(items$) {
	items$.each(function() {
		var item$ = $(this);
		if (item$.is('a')) {
			item$.attr("href", "#");
		} else {
			item$.removeAttr('disabled');
		}
	});
}

