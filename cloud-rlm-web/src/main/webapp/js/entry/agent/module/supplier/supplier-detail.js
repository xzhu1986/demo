$(document).ready(function() {
	var $detailForm = $('#detail-form');
	// mask all at begin
	$detailForm.disableForm();

	$('#email').click(function(e) {
		e.preventDefault();
		var userid = $(this).attr('data-userid')
		$.get('{0}/security/password/ask-reset/{1}.json'.format(basePath, userid), function(data) {
			if (data && data.result.msg) {
				alert(data.result.msg);
			}
		});
	});
	// ------- reseller notify start -------//
	$('.notify-btn').click(function(e) {
		e.preventDefault();
		var this$ = $(this);
		var url = $(this).attr('data-url');
		this$.mask();
		isell.InnerWindow({
			url			: url,
			containerId	: 'notify-container',
			callback	: function() {
				this$.unmask();
			}
		});
	});

	$('#notify-filter form').live('submit', function(e) {
		e.preventDefault();
		var url = $(this).attr('action') + '?' + $(this).serialize();
		isell.InnerWindow({
			url			: url,
			containerId	: 'notify-container'
		});
	})
	$('#notify-filter #previewEmail').live('click', function(e) {
		var url = $(this).attr('data-url') + '?' + $('#notify-filter form').serialize();

		isell.InnerWindow({
			url			: url,
			containerId	: 'notify-container',
			callback	: function() {
				var tools = 'Blocktag,Fontface,FontSize,Bold,Italic,Underline,Strikethrough,FontColor,BackColor,|,Align,List,Outdent,Indent,|,Link,Img';
				$('.mailPreview #mailBody').xheditor({
					tools	: tools
				});
			}
		});
	})
	$('#rsMapMailForm').live('submit', function(e) {
		e.preventDefault();
		var mf$ = $(this);
		mf$.mask("sending ...");
		mf$.ajaxSubmit(function(data) {
			mf$.unmask();
			var msg = "Mail Send!";
			if (data && data.result.msg) {
				msg = data.result.msg;
			}
			alert(msg);
		});
	});
	// ------- reseller notify end -------//
});