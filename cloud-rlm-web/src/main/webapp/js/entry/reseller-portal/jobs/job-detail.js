$(document).ready(function() {
	$('#add-note').click(function() {
		var this$ = $(this);
		this$.mask();

		notifyInnerWin = isell.InnerWindow({
			url			: this$.attr('data-url'),
			containerId	: 'add-note-container',
			callback	: function() {
				this$.unmask();

				$('#add-note-form').submit(function() {
					var mf$ = $(this);
					mf$.mask("Wait a moment ...");
					mf$.ajaxSubmit(function(data) {
						mf$.unmask();
						//alert(data.result.msg);
						notifyInnerWin.close();
						
						window.location.reload();
					});
					
					return false;
				});
			}
		});

	});

});
