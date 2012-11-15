$(document).ready(function() {
	var $detailForm = $('#detail-form');
			// init
	$('.perm-element[data-dependson=""]').each(function() {
		castPermissionCheckEvent($(this));
	});
	
	if($detailForm.find('input[name="userId"]').val()!=""){
		$detailForm.disableForm();
	}else{
		$detailForm.enableForm();
	}
	
	$detailForm.find('.cancel-btn').click(function(e) {
		//$detailForm.reset(); 
		$detailForm.disableForm(function($form) {
			$detailForm.find('.edit-btn').show();
		});
	});

	
	// permission cascading
	$('.perm-element').change(function(e) {
		castPermissionCheckEvent($(this));
	});

	function castPermissionCheckEvent($self) {
		var isChecked = $self.attr('checked') === 'checked';
		var parentKey = $self.attr('data-key');
		$('[data-dependson="{0}"]'.format(parentKey)).each(function() {
			if (isChecked) {
				enablePerm($(this));
			} else {
				$(this).removeAttr('checked');
				disablePerm($(this));
			}
			castPermissionCheckEvent($(this));
		});
	}
	
	function disablePerm($perm) {
		$perm.attr('disabled', 'disabled').addClass('unmodified');
	}

	function enablePerm($perm) {
		$perm.removeAttr('disabled').removeClass('unmodified');
	}
	
	$detailForm.find('.reset-password-btn').click(function(e) {
		e.preventDefault();
		var userid = $detailForm.find('input[name="userId"]').val();
		$.get('{0}/security/password/ask-reset/{1}.json'.format(basePath, userid), function(data) {
			if (data && data.result.msg) {
				alert(data.result.msg);
			}
		});
	});
	
	$detailForm.submit(function() {
		$detailForm.find('input[name="username"]').val($detailForm.find('input[name="email"]').val());
		var dataField = $('#permissionDatas');
		var datas = [];
		$('.permission-setting .perm-element:checked:enabled').each(function() {
			var key = $(this).attr('data-key');
			datas.push(key);
		});
		dataField.val(JsUtils.encode(datas));
	});
});