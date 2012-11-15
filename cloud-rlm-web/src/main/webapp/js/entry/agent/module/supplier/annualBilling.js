$(document).ready(function() {
	var $detailForm = $('#billing-form');
	// mask all at begin
	$detailForm.disableForm();

	$(":date").dateinput({
		format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
		// selectors : true, // whether month/year dropdowns are shown
		// offset : [5, 10], // tweak the position of the calendar
		speed		: 'fast', // calendar reveal speed
		firstDay	: 1
		// which day starts a week. 0 = sunday, 1 = monday etc..
	});
});