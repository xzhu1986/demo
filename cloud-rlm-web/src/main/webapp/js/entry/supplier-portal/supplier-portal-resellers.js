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
	
	var NOTIFY_GRID = isell.LocalPagingGrid({
		tableId			: 'supplier-portal-resellers-table',
		dataRowTpl		: '<tr><td><a class="supplier-portal-reseller-btn" href="'+basePath+'/supplier-portal/reseller-map/{serialNo}">{company}</a></td>' +
							  '<td>{account}</td>' +
							  '<td>{contact}</td>' +
							  '<td>{phone}</td>' +
							  '<td>{status}</td>' +
							  '<td>{breakName}</td>' +
							  '<td>{dateGranted}</td></tr>',
		url				: basePath + '/supplier-portal/resellers.json',
		root			: 'result.data',
		pageSize		: 20
	});

	var url = basePath + '/supplier-portal/resellers.json?' + $form.serialize();
	NOTIFY_GRID.load(url);
	
	
	$('.supplier-portal-reseller-btn').live('click',function(e) {
		e.preventDefault();
		isell.InnerWindow({
			url			: $(this).attr('href'),
			containerId	: 'reseller-map-container'
		});
	});
	
	
	$('.approval-btn').live('click',function(e) {
		e.preventDefault();
		var $form = $('#map-form');
		$form.find('input[name="optionUpdate"]').val('approval');
		$form.mask("update ...");
		$.post($form.attr('action') + '?' + $form.serialize(),function(data) {
			if(data&&data.result.msg){
				alert(data.result.msg);
			}
			$('#map-form').unmask();
			window.location.reload();
		});
	});

	$('.decline-btn').live('click',function(e) {
		e.preventDefault();
		var $form = $('#map-form');
		$form.find('input[name="optionUpdate"]').val('decline');
		commentDialog = art.dialog({
		    title: 'Comment',
		    content: '<div><ul><li>Comment:</li>' +
		    		'<li><textarea rows="4" cols="20" tip="not blank"></textarea></li>' +
		    		'<li><button class="link-btn save-comment-btn" style="float:right;" type="button">Save</button></li></ul></div>',
			init: function () {
			        textarea = this.DOM.content.find('textarea')[0];
					textarea.select();
					textarea.focus();
			    }
		});
		
	});
	
	$('.save-comment-btn').live('click',function(e) {
		e.preventDefault();
		var $form = $('#map-form');
		var input = $form.find('input[name="supplierDisableComment"]');
    	if(!textarea.value){
    		return;
    	}
    	if (input) input.val(textarea.value);
    	commentDialog.close();
    	$form.mask("update ...");
    	$.post($form.attr('action') + '?' + $form.serialize(),function(data) {
			if(data&&data.result.msg){
				alert(data.result.msg);
			}
			$('#map-form').unmask();
			window.location.reload();
		});
	});	
	$('.cancel-comment-btn').live('click',function(e) {
		e.preventDefault();
    	commentDialog.close();
	});	
});