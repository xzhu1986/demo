$(document).ready(function() {
	if($('#dataSavedFlag').val()){
		if(window.opener){
			window.opener.flushFollowupList();
		}
		window.close();
		return false;
	}
	
	$(":date").dateinput({
		format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
		speed		: 'fast', // calendar reveal speed
		firstDay	: 1
	});
});