$(document).ready(function() {
	// notify
	var rowCheckStates = new Set();
	$('#viewHistory').click(function(e) {
		e.preventDefault();
		var this$ = $(this);
		var url = $(this).attr('data-url');
		this$.mask();
		var notifyInnerWin = isell.InnerWindow({
			// url : url,
			containerId	: 'opeartion-history',
			winConfig	: {// http://www.planeart.cn/demo/artDialog/_doc/API.html
				fixed	: false,
				follow	: '#viewHistory'
			},
			callback	: function() {
				this$.unmask();
				var NOTIFY_GRID = isell.LocalPagingGrid({
					tableId			: 'history-table',
					dataRowTpl		: '<tr><td>{userName}</td> <td>{ipAddr}</td><td>{date}</td>  </tr>',
					url				: this$.attr('data-url'),
					root			: 'result.data',
					// rowCheckboxCls : 'row-checkbox',
					pageSize		: 20,
					// onRowSelect : function(row$, rowData, selected) {
					// var id = rowData.serialNo;
					// if (selected) {
					// rowCheckStates.add(id);
					// } else {
					// rowCheckStates.remove(id);
					// }
					// },
					rowRenderer		: function(row$, rowData) {
						var id = rowData.id;
						var serialNo=rowData.targetId;
						row$.on('click', function() {
							var url='{0}/resellers/{1}/detail?historyId={2}'.format(basePath,serialNo,id);
							window.open(url);
							
						});

					},
					afterPageShow	: function() {
						notifyInnerWin.relocate();
					}
				});
				NOTIFY_GRID.load();
			}
		});
	});

});