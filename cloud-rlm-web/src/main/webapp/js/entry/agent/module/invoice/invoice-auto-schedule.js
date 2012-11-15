$(document).ready(function() {
	var $detailForm = $('#auto-schedule-detail-form');
	$('#errorMsg').hide();
	if($('#dataSavedFlag').val()){
		if(window.opener){
			window.opener.flushScheduleList();
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
var isExtendsValidate = true;	
function extendsValidate(){	
	var $detailForm = $('#auto-schedule-detail-form');
	var invoiceTotalAmount = $detailForm.find('input[name="invoiceTotalAmount"]');
	var firstScheduledAmount = $detailForm.find('input[name="firstScheduledAmount"]');
	var eachSecheduleAmount = $detailForm.find('input[name="eachSecheduleAmount"]');
	var scheduleds = $detailForm.find('input[name="scheduleds"]').val();
	
	var totalScheduleAmount = Number(firstScheduledAmount.val())+Number(eachSecheduleAmount.val()*(scheduleds-1));
	if( invoiceTotalAmount.val() == totalScheduleAmount){	
		//invoiceTotalAmount.validate_callback(null,"sucess");	
		//firstScheduledAmount.validate_callback(null,"sucess");
		//eachSecheduleAmount.validate_callback(null,"sucess");
		$('#errorMsg').text('');
		$('#errorMsg').hide();
	}else{
		//invoiceTotalAmount.validate_callback(null,"failed");
		//firstScheduledAmount.validate_callback(null,"failed");
		//eachSecheduleAmount.validate_callback(null,"failed");
		var msg = 'Total Schedule Amount({0}) must equals Invoice Total Amount({1})!'.format(totalScheduleAmount,invoiceTotalAmount.val());
		$('#errorMsg').text(msg);
		$('#errorMsg').show();
		return false;
	}
}