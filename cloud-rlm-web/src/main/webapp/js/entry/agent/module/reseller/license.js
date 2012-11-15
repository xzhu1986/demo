$(document).ready(function() {
	var $licenseForm = $('#license-form');
	// mask all at begin
	$licenseForm.disableForm();

	$(":date").dateinput({
		format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
		speed		: 'fast', // calendar reveal speed
		firstDay	: 1
	});
	
	$('#updateReview').click(function(){
		var url='{0}/resellers/next-review-date.json'.format(basePath);
		$.getJSON(url,function(data){
			var d=data.result.data;
			if(d){
				$('#nextReviewDate').val(d.nextReviewDate);
				$('#lastReviewUser').val(d.lastReviewUser.userId);
				$('#lastReviewDate').val(d.lastReviewDate);
				$('#lastReviewDateDisplay').val(d.lastReviewDate+' - '+d.lastReviewUser.username);
			}
		});
	});
});