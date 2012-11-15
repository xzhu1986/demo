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
		$('#serialNo').combobox();
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
		$('#map-form').find('select[name="status"]').change(function(e) {
			e.preventDefault();
			if(this.value=="Approved"){
				$('#map-form').find('input[name="approvalDate"]').data("dateinput").today();
			}else{
				$('#map-form').find('input[name="approvalDate"]').val('');
			}
		});
		
		
		$('.save-btn').click(function(e) {
			e.preventDefault();
			var $form = $('#map-form');
			if(validate($('#serialNo'))){
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
		
		$('#map-form').find('.clear-calendar-btn').click(function(e) {
			var $obj = $(this).prev(":date");
			if($obj.attr('disabled') != 'disabled'){
				$obj.val('');
			}
		});
		
		$('#map-form').disableForm();
	}
});
