$(document).ready(function() {
	var $detailForm = $('#detail-form');
	// init
	$('.perm-element[data-dependson=""]').each(function() {
		castPermissionCheckEvent($(this));
	});
	// mask all at begin
	$detailForm.disableForm();

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

	$('#email').click(function(e) {
		e.preventDefault();
		var userid = $(this).attr('data-userid')
		$.get('{0}/security/password/ask-reset/{1}.json'.format(basePath, userid), function(data) {
			if (data && data.result.msg) {
				alert(data.result.msg);
			}
		});
	});

	$detailForm.submit(function() {
		var dataField = $('#permissionDatas');
		var datas = [];
		$('.permission-setting .perm-element:checked:enabled').each(function() {
			var key = $(this).attr('data-key');
			datas.push(key);
		});
		dataField.val(JsUtils.encode(datas));
	});

	function disablePerm($perm) {
		$perm.attr('disabled', 'disabled').addClass('unmodified');
	}

	function enablePerm($perm) {
		$perm.removeAttr('disabled').removeClass('unmodified');
	}
	// defaults btn
	$('#defaults').click(function() {
		PERMISSION_DIALOG = art.dialog({
			content	: document.getElementById('defaults-permission-container')
			// left : opts.left,
			// top : opts.top
		});
	});

	$('#defaults-permission-sel').change(function() {
		var perms$ = $('.perm-element');
		var val = $(this).val();
		var url = '{0}/users/role-permissions/{1}.json'.format(basePath, val);
		$.getJSON(url, function(data) {
			if (!data.result.data)
				return false;
			perms$.removeAttr('checked');
			disablePerm(perms$);

			var dataArr = data.result.data;
			$.each(dataArr, function(i) {
				var target$=$('.perm-element[data-key^="{0}"]'.format(dataArr[i]));
				if(target$.length>0)
					enablePerm(target$);
					target$.attr('checked','checked');
					target$.trigger('change');
			});

			PERMISSION_DIALOG.close();
		});
	});
});

(function() {
	// only for default role display
	var arr = [];
	$('.perm-element').not('[data-key^="supplier:when_country"]')
		.not('[data-key^="reseller:when_agent"]')
		.not('[data-key^="supplier:when_country"]')
		.each(function() {
			arr.push($(this).attr('data-key'));
		});
	console.log('admin default permission:')
	console.log(JsUtils.encode(arr));
	/////////////////////////////////
	arr = [];
	$('.perm-element').not('[data-key^="supplier:when_country"]').not('[data-key^="reseller:when_agent"]')
		.not('[data-key^="reseller:delete"]').not('[data-key^=setting]')
		.not('[data-key="supplier:dataRequest:edit"]')
		.not('[data-key^="supplier:priceBreak:"]')
		.not('[data-key^="supplier:branch:"]')
		.not('[data-key^="priceupdates:"]')
		.each(function() {
			arr.push($(this).attr('data-key'));
		});
	arr.push('supplier:priceBreak:view');
	arr.push('supplier:branch:view');
	console.log('sale manager default permission:')
	console.log(JsUtils.encode(arr));
	/////////////////////////////////
	arr = [];
	$('.perm-element[data-key^="reseller:"],.perm-element[data-key^="supplier:"],.perm-element[data-key^="priceupdates:"]')
		.not('[data-key^="reseller:delete"]')
		.not('[data-key^="supplier:when_country"]')
		.not('[data-key^="reseller:when_agent"]')
		.each(function() {
			arr.push($(this).attr('data-key'));
		});
	console.log('data manager default permission:')
	console.log(JsUtils.encode(arr));
	/////////////////////////////////
	arr = [];
	$('.perm-element[data-key^="reseller:"]')
		.not('[data-key^="reseller:delete"]')
		.not('[data-key^="reseller:summary"]')
		.not('[data-key^="reseller:when_agent"]')
		.each(function() {
			arr.push($(this).attr('data-key'));
		});
	arr.push('supplier:view');
	arr.push('supplier:priceBreak:view');
	arr.push('supplier:branch:view');
	console.log('support user default permission:')
	console.log(JsUtils.encode(arr));
	/////////////////////////////////
	arr = [];
//	$('.perm-element').find('[data-key^="supplier:"],[data-key^="priceupdates:"]')
//		.not('[data-key^="reseller:delete"]')
//		.not('[data-key^="reseller:summary"]')
//		.each(function() {
//			arr.push($(this).attr('data-key'));
//		});
	arr.push('supplier:view');
	arr.push('supplier:priceBreak:view');
	arr.push('supplier:branch:view');
	arr.push('priceupdates:view');
	arr.push('priceupdates:edit');
	console.log('data user default permission:')
	console.log(JsUtils.encode(arr));
})