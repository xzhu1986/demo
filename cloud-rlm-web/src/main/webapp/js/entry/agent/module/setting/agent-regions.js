$(document).ready(function() {
	var $detailForm = $('#detail-form');

	// del event
	$('.del-btn').on('click', function(e) {
		e.preventDefault();
		var agentId = $('#agent-data').attr('data-agentid');
		var codes=[];
		$(':checkbox[checked="checked"]').each(function(){
			var regionCode=$(this).attr('data-regionCode');
			if(regionCode && regionCode!='')
				codes.push(regionCode);
		});
		if(codes.length==0) return;
		var confirmv = confirm("Continue this operation [delete this item] ?")
		if (!confirmv)
			return;
		var url = '{0}/settings/Agents/{1}/region/delete.json'.format(basePath, agentId);
		$.ajax({
			url		: url,
			data : {
				regionCodes : JsUtils.encode(codes)
			},
			success	: function(data, textStatus, jqXHR) {
				if (data && data.result)
					alert(data.result.msg);
				window.location.reload();
			},
			error	: function(jqXHR, textStatus, errorThrown) {
				alert(errorThrown)
			},
			type	: 'POST',
			async :false
		});
		
	});

});