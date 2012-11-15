$(document).ready(function() {
	if($('#dataSavedFlag').val()){
		if(window.opener){
			window.opener.flushScheduleList();
		}
		window.close();
		return false;
	}
	
	if($("#isPaid").attr("checked")=="checked"){
		$(':input[name="paidDatetime"]').removeAttr('disabled');
		$(':input[name="paymentReference"]').removeAttr('disabled');
	}
	
	$(":date").dateinput({
		format		: 'yyyy-mm-dd',// 'dddd dd, mmmm yyyy', // the format displayed for the user
		speed		: 'fast', // calendar reveal speed
		firstDay	: 1
	});
	
	$("#isPaid").change(function(e) {
		e.preventDefault();
		if(this.checked==true){
			$(':input[name="paidDatetime"]').removeAttr('disabled');
			$(':input[name="paymentReference"]').removeAttr('disabled');
			if(!/^.+$/.test($(':input[name="paidDatetime"]').val())){
				$(':input[name="paidDatetime"]').data("dateinput").today();
			}
		}else{
			//$(':input[name="paidDatetime"]').data("dateinput").getInput().val('');
			$(':input[name="paidDatetime"]').val('');
		//	$(':input[name="paymentReference"]').val('');
			$(':input[name="paidDatetime"]').attr("disabled","disabled");
			$(':input[name="paymentReference"]').attr("disabled","disabled");
		}
	});
});

var isExtendsValidate = true;
function extendsValidate(){
	if($("#isPaid").attr("checked")=="checked" && !/^.+$/.test($(':input[name="paidDatetime"]').val())){
		$(':input[name="paidDatetime"]').validate_callback(null,"failed");
		return false;
	}else{
		$(':input[name="paidDatetime"]').validate_callback(null,"sucess");
	}
}