$(document).ready(function() {
	
	$('a.website-addr').each(function(){
		var url=$(this).attr('href');
		if(!/^http:/.test(url)){
			$(this).attr('href','http://'+url);
		}
	});
	
//	var table$ = $('#data-table');
//
//	var url = '{0}/reseller-portal/supplier/{id}/detail'.format(basePath);
//	var NOTIFY_GRID = isell.LocalPagingGrid({
//		tableId		: 'data-table',
//		dataRowTpl	: '<tr><th><a href="'
//		        + url
//		        + '" target="_blank">{name}</a></th><th>{overview}</th><th>{country}</th><th><a href="{website}" class="website-addr" target="_blank">{website}</a></th><th><a href="callto://{phone}">{phone}</a></th></tr>',
//		url		    : table$.attr('data-url'),
//		root		: 'result.data',
//		// rowCheckboxCls : 'row-checkbox',
//		pageSize	: 25,
//		rowRenderer	: function(row$, rowData) {
//			// var id = rowData.id;
//			// row$.find('a').click(function(e) {
//			// e.preventDefault();
//			// window.open('{0}/reseller-portal//supplier/{1}/detail'.format(basePath,-1));
//			// });
//		}
//	});
//	var fm$ = $('#supplier-add-form');
//	fm$.mask('loading ...');
//	NOTIFY_GRID.load(null, function() {
//		fm$.unmask();
//	});
//
//	fm$.submit(function() {
//		fm$.mask('loading ...');
//		NOTIFY_GRID.load(null, function() {
//			fm$.unmask();
//		},JsUtils.serializeForm(fm$));
//
//		return false;
//	});
});
