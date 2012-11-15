$(document).ready(function() {
	
	$('.supplier-reseller-a').click(function(e) {
		e.preventDefault();
		mapWindow = isell.InnerWindow({
			url			: $(this).attr('href'),
			containerId	: 'reseller-map-container'
		});
	});
});
