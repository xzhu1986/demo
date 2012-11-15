$(document).ready(function() {
	$('#invoicesTable').find('tr').not(':first-child').each(function(){
		var tr$=$(this);
		var blance=tr$.find('.blance').text();
		if(parseFloat(blance)!=0){
			tr$.css('color','red');
			tr$.find('a').css('color','red');
		}
	});
	
	
});