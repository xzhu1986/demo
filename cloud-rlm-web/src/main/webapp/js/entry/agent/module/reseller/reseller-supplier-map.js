$(document).ready(function() {
	
	$('.supplier-reseller-a').click(function(e) {
		e.preventDefault();
		mapWindow = isell.InnerWindow({
			url			: $(this).attr('href'),
			containerId	: 'reseller-map-container',
			callback	: function(innerWin) {
				init();
			}
		});
	});
	
	$('.supplier-reseller-btn').click(function(e) {
		e.preventDefault();
		mapWindow = isell.InnerWindow({
			url			: $(this).attr('data-url'),
			containerId	: 'reseller-map-container',
			callback	: function(innerWin) {
				init();
			}
		});
	});
	
	function init(){
		$('#supplier').combobox();
		if($('#supplier') && $('#supplier').val()!=""){
			loadSingle($('#priceBreak'), $('#supplier').val());
			changeHtmlShow($('#supplier').val());
		}
		$(":date").dateinput({
			format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
			speed		: 'fast', // calendar reveal speed
			firstDay	: 1
		});
		$('#map-form').find('.edit-btn').click(function(e) {
			e.preventDefault();
			$('#map-form').enableForm();

		});
		$('#map-form').find('.cancel-btn').click(function(e) {
			e.preventDefault();
			mapWindow.close();
		});
		$('#supplier').change(function() {
			loadSingle($('#priceBreak'), $(this).val());
			changeHtmlShow($(this).val());
		});
		
		$('.save-btn').click(function(e) {
			e.preventDefault();
			var $form = $('#map-form');
			if(validate($('#supplier'))){
				$form.mask("update ...");
				$.post($form.attr('action'),JsUtils.serializeForm($form),function(data) {
					if(data&&data.result.msg){
						alert(data.result.msg);
					}
					$('#map-form').unmask();
					mapWindow.close();
					window.location.reload();
				});
			}
		});
		$('#map-form').find('select[name="status"]').change(function(e) {
			e.preventDefault();
			if(this.value=="Approved"){
				$('#map-form').find('input[name="approvalDate"]').data("dateinput").today();
			}else{
				$('#map-form').find('input[name="approvalDate"]').val('');
			}
		});
		$('#map-form').find('.clear-calendar-btn').click(function(e) {
			var $obj = $(this).prev(":date");
			if($obj.attr('disabled') != 'disabled'){
				$obj.val('');
			}
		});
		
		$('#map-form').disableForm();
	}
	function changeHtmlShow(supplierId){
		// url
		var url = basePath+"/supplier/{0}/supplier-datarequest.json".format(supplierId);
		// load
		$.get(url, function(data) {
			if (data && data.result.data) {
				var dataRequest = data.result.data;
				if(dataRequest.requireLoginName){
					$('#requireLoginName').show();
					$('#map-form').find('input[name="supplierUsername"]').val('');
				}else{
					$('#requireLoginName').hide();
					$('#map-form').find('input[name="supplierUsername"]').val('');
				}

				if(dataRequest.requireLoginPassword){
					$('#requireLoginPassword').show();
					$('#map-form').find('input[name="supplierPassword"]').val('');
				}else{
					$('#requireLoginPassword').hide();
					$('#map-form').find('input[name="supplierPassword"]').val('');
				}
			}
		});
	}
	
	var optionFmt = '<option value="{0}">{1}</option>';
	function loadSingle($sel, parentVal) {
		if($sel.length<=0) return;
		if (!$sel)
			return;
		$sel.empty();
		$sel.append('<option value=""></option>');
		var selVal = $sel.val() ? $sel.val() : $sel.attr('data-value');
		// url
		var url = $sel.attr('data-url').format(parentVal);
		// load
		$.get(url, function(data) {
			if (data && data.result.data) {
				var dlist = data.result.data;
				$.each(dlist, function(i) {
					var optItem = optionFmt.format(dlist[i].priceBreakId, dlist[i].name);
					if (dlist[i].priceBreakId == selVal) {
						$sel.append($(optItem).attr('selected', 'selected'));
					} else {
						$sel.append(optItem);
					}
				});
			}
		});
	}

});
