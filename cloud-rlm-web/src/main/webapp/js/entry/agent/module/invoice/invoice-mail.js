$(document).ready(function() {
	$('#preview-send-email').click(function(e) {
		e.preventDefault();
		var this$=$(this);
		this$.mask();
		var type = $('#mailType').val();
		var url = $(this).attr('data-url');
		$('#mail-preview-container').load(url.format(type), function(responseText, textStatus, XMLHttpRequest) {
			if (XMLHttpRequest.status != 200) {
				alert(XMLHttpRequest.statusText + ". Make sure you have fill the email template.");

			} else {
				art.dialog({
					content	: document.getElementById('mail-preview-container')
					// left : opts.left,
					// top : opts.top
				});
			}
			this$.unmask();
		});
	});

	$('#previewAttachment').live('click', function(e) {
		e.preventDefault();
		var this$ = $(this);
		$(this).mask();
		$.getJSON($('#reportPreviewContainer').attr('data-url'), {
			reportPathType:$('.mailPreview').find('#reportPathType').val()
		},function(data) {
			this$.unmask();
			if(!data.result.data){
				alert(data.result.msg);
				return;
			}
			$('#reportPreviewContainer').find('embed').attr('src', data.result.data);
			art.dialog({
				content	: document.getElementById('reportPreviewContainer')
				// left : opts.left,
				// top : opts.top
			});
		});
	});
	// $('#mailPreviewForm').ajaxForm(function() {
	// alert('Mail Send');
	// });

	$('#sendEmail').live('click', function(e) {
		e.preventDefault();
		var mf$ = $('#mailPreviewForm');
		mf$.mask("sending ...");
		mf$.ajaxSubmit(function(data) {
			mf$.unmask();
			var msg="Mail Send!";
			if(data&&data.result.msg){
				msg=data.result.msg;
			}
			alert(msg);
		});
	});
});

