$(document).ready(function() {
	var $detailForm = $('#detail-form');
	// mask all at begin
	$detailForm.find('.div_edit').hide();
	$detailForm.find('.div_view').show();
	$detailForm.disableForm();
	
	
	$detailForm.find('.edit-btn').click(function(e) {
		$detailForm.find('.div_view').hide();
		$detailForm.find('.div_edit').show();
	});
	
	
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