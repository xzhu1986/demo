JsUtils.namespace('isell');
isell.PopupFilter = function(options) {
	var opts = $.extend(true, {
		elementId		  : '',// bind target
		winConfig		  : {// http://www.planeart.cn/demo/artDialog/_doc/API.html
			fixed	: false,
			follow	: '#' + options.elementId// follow by default
			        // left : '50%',
			        // top : '38.2%'
		},
		url		          : '',
		data		      : {},
		root		      : '',
		column		      : [{
			        name		: '',
			        headName	: '',
			        hide		: false
		        }],
		rowChooseCallback	: function($this, rowData) {
			logError('not yet implemented');
		}
	}, options);

	$('#' + opts.elementId).live('click', (function(e) {
		if ($(this).attr('disabled'))
			return false;
		bindPopUp(opts, $(this));
	}));

	var tpl = '<div class="popup-filter" style="display: none;"><h3> <input type="text" class="query-input" /> </h3><table class="maxWidthTable"></table></div>';
	var tdTpl = '<td data-name="{0}">{1}</td>';
	var trTpl = '<tr>{0}</tr>';

	function bindPopUp(opts, original$) {
		var uid = getUid(original$);

		var popup$ = $('.' + uid);
		// get former popup div
		if (popup$.length == 0) {
			popup$ = createNewPopUp(opts, original$, uid);
		}
		// bind
		GLOBAL_ART_DIALOG = art.dialog($.extend({
			content	: document.getElementById(uid)
		}, opts.winConfig));
		popup$.find('.query-input').focus();
	}

	function createNewPopUp(opts, original$, uid) {
		// create new div
		var popup$ = $(tpl).attr('id', uid);
		var tds = [];
		// row headName
		$.each(opts.column, function(i) {
			if (!opts.column[i].hidden)
				tds.push(tdTpl.format(opts.column[i].name, opts.column[i].headName));
		});

		var thead$ = $('<thead/>').append(trTpl.format(tds.join('')));
		thead$.find('tr').attr('id', 'filterTitle');
		var tbl$ = popup$.find('table').append(thead$).append('<tbody/>');
		popup$.appendTo($('body'));
		// input change event
		var queryInput$ = popup$.find('.query-input');
		var dataBody = popup$.find('tbody');
		queryInput$.change(function(e) {
			$.getJSON(opts.url, $.extend({
				query	: queryInput$.val()
			}, opts.data), function(data) {
				dataBody.empty();
				if (data) {
					var newData = getRootData(data, opts.root);
					$.each(newData, function(i) {
						var tds = [];
						$.each(opts.column, function(j) {
							var colName = opts.column[j].name;
							var hidden = opts.column[j].hidden;
							if (!hidden)
								tds.push(tdTpl.format(colName, newData[i][colName]));
						});
						var tr$ = $(trTpl.format(tds.join('')));
						tr$.data('rowData', newData[i]);
						tr$.click(function() {
							opts.rowChooseCallback(original$, $(this).data('rowData'));
							GLOBAL_ART_DIALOG.close();
						});
						dataBody.append(tr$);
					});
				}
			});
		});
		// data row click event
		// popup$.find('tbody tr').live('click', function() {
		// opts.rowChooseCallback(original$, $(this).data('rowData'));
		// });

		return popup$;
	}

	function logError(msg) {
		if (window.console && window.console.error)
			window.console.error(msg);
	}

	function getUid(original$) {
		var uid = original$.data('uid');
		if (!uid) {
			uid = jQuery.iuid();
			original$.data('uid', uid);
		}
		return uid;
	}

	function getRootData(data, rootPath) {
		var arr = rootPath.split('.');
		var r = data;
		for (var i = 0; i < arr.length; i++) {
			r = r[arr[i]];
		}
		return r;
	}
}