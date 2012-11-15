$(document).ready(function() {
	var optionFmt = '<option value="{0}">{1}</option>';
	$('select#country').live('change', function(e) {
		var $sel = $(this);
		var name = $sel.attr('name');
		var countryCode = $sel.val();
		var $region = $('#region');
		$region.val('');
		loadSingle($region, countryCode);
	});

	function loadSingle($sel, parentVal) {
		if (!$sel)
			return;
		$sel.empty();
		$sel.append('<option value=""></option>');
		var selVal = $sel.val() ? $sel.val() : $sel.attr('data-value');
		// url
		var url = $sel.attr('data-url');
		if (url.indexOf('{0}') > -1 && parentVal) {
			url = url.format(parentVal)
		} else if (url.indexOf('{0}') > -1) {
			return;
		}
		// load
		$.get(url, function(data) {
			if (data && data.result.data) {
				var dlist = data.result.data;
				$.each(dlist, function(i) {
					var optItem = optionFmt.format(dlist[i].code, dlist[i].name);
					if (dlist[i].code == selVal) {
						$sel.append($(optItem).attr('selected', 'selected'));
					} else {
						$sel.append(optItem);
					}
				});
			}
		});
	}
	
	$('#emailType').live('change',function(e){
		var typeId=$(this).val();
		$.getJSON(basePath + "/email-template/detail/{0}.json".format(typeId),function(data){
			var d=data.result.data;
			if(d){
				$('#subject').val(d.subject);
				$('#senderEmail').val(d.defaultSender);
			}
		})
	});
	
//	isell.PopupFilter({
//		elementId		  : 'emailTypeName',
//		url		          : basePath + "/email-template/filter.json?target=Suppliers",
//		root		      : 'result.data',
//		column		      : [{
//			        name		: 'typeId',
//			        headName	: 'Id',
//			        hidden : true
//		        }, {
//			        name		: 'typeName',
//			        headName	: 'Name'
//		        }, {
//			        name		: 'subject',
//			        headName	: 'Subject'
//		        }, {
//			        name		: 'defaultSender',
//			        headName	: 'Sender',
//			        hidden : true
//		        }],
//		rowChooseCallback	: function($this, rowData) {
//			$this.val(rowData.typeName);
//			$('#emailTypeHidden').val(rowData.typeId);
//			$('#subject').val(rowData.subject);
//			$('#senderEmail').val(rowData.defaultSender);
//		}
//	});


	var rowCheckStates = new Set();
	$('.notify-btn').click(function(e) {
		e.preventDefault();
		var this$ = $(this);
		var url = $(this).attr('href');
		this$.mask();
		notifyInnerWin=isell.InnerWindow({
			url			: url,
			containerId	: 'notify-container',
			callback	: function() {
				this$.unmask();
				NOTIFY_GRID = isell.LocalPagingGrid({
					tableId			: 'notify-table',
					dataRowTpl		: '<tr> <td><input type="checkbox" class="row-checkbox"></td> <td>{shortName}</td> <td>{email}</td> </tr>',
					url				: basePath + '/supplier/notify-supplier/filter.json',
					root			: 'result.data',
					rowCheckboxCls	: 'row-checkbox',
					pageSize		: 20,
					onRowSelect		: function(row$, rowData, selected) {
						var id = rowData.supplierId;
						if (selected) {
							rowCheckStates.add(id);
						} else {
							rowCheckStates.remove(id);
						}
					},
					rowRenderer		: function(row$, rowData) {
						var id = rowData.supplierId;
						if (rowCheckStates.contains(id)) {
							row$.find('.row-checkbox').attr('checked', 'checked');
						}
						if (rowData.sentToday == true){
							row$.css('background', 'green');
							row$.attr('title', 'was sent today')
						}
					},
					afterPageShow : function(){
						notifyInnerWin.relocate();
					}
				});
			}
		});
	});

	$('form#notify-form').live('submit', function(e) {
		e.preventDefault();

		var url = $(this).attr('action') + '?' + $(this).serialize();
		rowCheckStates = new Set();
		NOTIFY_GRID.load(url,function() {
			rowCheckStates = new Set();
			var allData = NOTIFY_GRID.getData();
			$.each(allData, function(i) {
				rowCheckStates.add(allData[i].supplierId);
			});
		})
	});

	$('#sendNotifyEmail').live('click', function() {
		if (!NOTIFY_GRID) {
			alert('No datas are selected!');
			return false;
		}
		var includeIds = rowCheckStates.values();
		if (includeIds.length == 0) {
			alert('No item is selected!')
			return;
		}
		var allData = NOTIFY_GRID.getData();
		$('#notify-form').mask("sending ...");
		$.post($(this).attr('data-url') + '?' + $('form#notify-form').serialize(), {
			includeIds	: JsUtils.encode(includeIds),
			data		: JsUtils.encode(allData)
		}, function(data) {
			if(data&&data.result.msg){
				alert(data.result.msg);
			}
			$('#notify-form').unmask();
		});
	});
});