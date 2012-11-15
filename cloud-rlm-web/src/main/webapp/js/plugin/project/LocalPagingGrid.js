JsUtils.namespace('isell');
/**
 * note: caller should provide table and table head ,if want select row,shoud provide the checkbox class name in the row
 * template
 */
isell.LocalPagingGrid = function(options) {
	var opts = $.extend({
		tableId		   : '',
		dataRowTpl		: '',
		url		       : '',
		root		   : 'result.data',
		rowCheckboxCls	: '',
		pageSize		: 20,
		displayPages	: 8,
		onRowSelect		: function(row$, rowData, selected) {
		},
		rowRenderer		: function(row$, rowData) {
		},
		afterPageShow : function(){
		}
	}, options);

	var currentPage = 1;
	var totalPage = 0;
	var totalItem = 0;
	var pageSize = opts.pageSize;
	var cacheData = [];

	var displayLength = opts.displayPages;
	var displayHalfLen = displayLength % 2 == 0 ? displayLength / 2 : parseInt(displayLength / 2) + 1;

	var pagingTpl = '<div class="pagination"></div>';
	// <span><a href="#" class="paging-prev">&lt;Prev</a></span>&nbsp;|&nbsp;<span><a href="#"
	// class="paging-next">Next&gt;</a></span>&nbsp; total pages:<span class="paging-total"></span>

	var table$ = $('#' + opts.tableId);
	var paging$;
	function init() {
		var body$ = table$.find('tbody');
		if (body$.length == 0)
			body$ = $('<tbody/>').appendTo(table$);
		body$.empty();

		totalItem = cacheData.length;
		totalPage = totalItem % pageSize == 0 ? totalItem / pageSize : parseInt(totalItem / pageSize) + 1;

		paging$ = table$.next('.pagination');
		if (paging$.length == 0)
			paging$ = $(pagingTpl).insertAfter(table$);
		bindNav(paging$);
	}

	function bindNav(paging$) {
		var prev$ = paging$.find('.paging-prev');
		prev$.click(function(e) {
			e.preventDefault();
			if ($(this).attr('href')) {
				showPage(currentPage - 1);
			}
		});
		var next$ = paging$.find('.paging-next');
		next$.click(function(e) {
			e.preventDefault();
			if ($(this).attr('href')) {
				showPage(currentPage + 1);
			}
		});
		paging$.find('.paging-total').text(totalPage);
	}

	function calcPaging() {
		// calculate navigate numbers
		var prevPage = currentPage == 1 ? -1 : currentPage - 1;
		var nextPage = currentPage != totalPage ? currentPage + 1 : -1;

		// displayStart & displayEnd
		var first = 1;
		var last = totalPage;
		var displayStart = (currentPage - displayHalfLen) >= first ? currentPage - displayHalfLen : first;
		var displayEnd = (displayStart + displayLength - 1) <= last ? displayStart + displayLength - 1 : last;
		if (displayEnd == last && displayStart > first) {
			displayStart = displayEnd - displayLength + 1;
		}

		// fill content
		paging$.empty();
		var prev$ = $('<a href="#" class="paging-prev red" data-index="{0}">&lt;Previous</a>').attr('data-index', prevPage);
		if (prevPage == -1) {
			prev$.removeAttr('href');
		}
		paging$.append(prev$);

		for (var i = displayStart; i <= displayEnd; i++) {
			var navItem$ = $('<a href="#" data-index="{0}">{0}</a>'.format(i.toString()));
			if (i == currentPage) {
				navItem$.removeAttr('href');
				navItem$.addClass('currentPage')
			}
			paging$.append(navItem$);
		}

		var next$ = $('<a href="#" class="paging-next red" data-index="{0}">Next&gt;</a>').attr('data-index', nextPage);
		if (nextPage == -1) {
			next$.removeAttr('href');
		}
		paging$.append(next$);

		paging$.find('a').click(function(e) {
			e.preventDefault();
			var this$ = $(this);
			var index = parseInt(this$.attr('data-index'));
			if (index == -1 || !this$.attr('href'))
				return;
			showPage(index);
		});
	}

	function showPage(pageNo) {
		var body$ = table$.find('tbody').empty();
		var start = (pageNo - 1) * pageSize;
		var end = pageNo * pageSize;
		end = end > totalItem ? totalItem : end;
		for (var i = start; i < end; i++) {
			var row$ = $(opts.dataRowTpl.format(cacheData[i]));
			// store row data
			row$.data('rowData', cacheData[i]).appendTo(body$);
			// render row
			opts.rowRenderer(row$, cacheData[i]);
			// bind row check event
			if (opts.rowCheckboxCls) {
				row$.find('.' + opts.rowCheckboxCls).change(function() {
					var currentRow$ = $(this).parents('tr');
					opts.onRowSelect(currentRow$, currentRow$.data('rowData'), $(this).is(":checked"));
				});
			}
		}

		currentPage = pageNo;
		calcPaging();
		opts.afterPageShow(); 
	}

	function getRootData(data, rootPath) {
		var arr = rootPath.split('.');
		var r = data;
		for (var i = 0; i < arr.length; i++) {
			r = r[arr[i]];
		}
		return r;
	}

	return {
		load	: function(newUrl, afterLoad,params) {
			opts.url = newUrl?newUrl:opts.url;
			var appendParams=$.extend({},params);
			$.getJSON(opts.url,appendParams, function(data) {
				if (data && data.result.data) {
					cacheData = getRootData(data, opts.root);
					init();
					if (afterLoad && JsUtils.isFunction(afterLoad)) {
						afterLoad();
					}
					showPage(1);
				} else {
					alert(data.result.msg);
				}
			});
		},
		getData	: function() {
			return cacheData;
		}
	};
}