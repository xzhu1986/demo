$(document).ready(function() {
	art.dialog({
		content	: document.getElementById('invoiceStatisticReportMailPreview')
	});
	
	window.onunload=null;
	
	(function send(){
		var mf$ = $('#mailPreviewForm');
		mf$.mask("Generating report. This may take several minutes.");
		mf$.ajaxSubmit(function(data) {
			mf$.unmask();
			if(data.result.success){
				window.close();
			}else{
				alert(data.result.msg);
			}
		});
	})();
	
//	$('#sendEmail').live('click', function(e) {
//		e.preventDefault();
//	});
});
