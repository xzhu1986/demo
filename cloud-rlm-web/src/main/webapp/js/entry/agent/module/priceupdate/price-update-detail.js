$(document).ready(function() {
	JsUtils.reloadParentWinOnClose();
	
	var $detailForm = $('#detail-form');
	if($('#dataSavedFlag') && $('#dataSavedFlag').val()){
		window.close();
		return false;
	}
	
	// mask all at begin
	$detailForm.disableForm();
	$(":date").dateinput({
		format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
		speed		: 'fast', // calendar reveal speed
		firstDay	: 1
	});
	$('.openWebLink').click(function(e){
		e.preventDefault();
		var webLink=$detailForm.find('input[name="supplierUpdate.webLink"]').val();
		if(webLink && webLink!=''){
			if(webLink.substring(0,7)!="http://"){
				webLink = "http://"+webLink;
			}
			var a = $("<a href='"+webLink+"' target='_blank'>GO</a>").get(0);
            var e = document.createEvent('MouseEvents');
            e.initEvent( 'click', true, true );
            a.dispatchEvent(e);
		}
	});
	function showFrequencyDiv($obj){
		var value = $obj.val();
		if(value=='WeekDays'){
			$('#Week-of-Month').hide();
			$('#Day-of-Week').show();
			$('#Day-of-Month').hide();
		}else if(value=='WeeksOfMonth'){
			$('#Week-of-Month').show();
			$('#Day-of-Week').show();
			$('#Day-of-Month').hide();
		}else if(value=='DayOfMonth'){
			$('#Week-of-Month').hide();
			$('#Day-of-Week').hide();
			$('#Day-of-Month').show();
		}
	}
	var $frequency = $detailForm.find("select[name='schedule.frequency']");
	showFrequencyDiv($frequency);
	$frequency.on('change', function(e) {
		showFrequencyDiv($(this));
	});
});