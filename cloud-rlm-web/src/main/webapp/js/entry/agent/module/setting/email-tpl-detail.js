$(document).ready(function() {
	// mail html editor
	$('#mailBody').tinymce({
		// Location of TinyMCE script
		script_url		                  : basePath + '/js/plugin/tinymce/jscripts/tiny_mce/tiny_mce.js',
		// General options
		theme		                      : "advanced",
		plugins		                      : "autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,advlist",
		// Theme options
		theme_advanced_buttons1		      : "bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect",
		theme_advanced_buttons2		      : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
		theme_advanced_buttons3		      : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,iespell,advhr,|,print,|,ltr,rtl,|,fullscreen",
		theme_advanced_buttons4		      : "insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak",
		theme_advanced_toolbar_location		: "top",
		theme_advanced_toolbar_align		: "left",
		theme_advanced_statusbar_location	: "bottom",
		theme_advanced_resizing		      : true,
		// Example content CSS (should be your site CSS)
		content_css		                  : resourceBasePath + "/style/screen.css"
		// Drop lists for link/image/media/template dialogs
		// template_external_list_url : "lists/template_list.js",
		// external_link_list_url : "lists/link_list.js",
		// external_image_list_url : "lists/image_list.js",
		// media_external_list_url : "lists/media_list.js",
		// Replace values for the template plugin
		// template_replace_values : {
		// username : "Some User",
		// staffid : "991234"
		// }
	});

	$('#detail-form').disableForm(function($form) {
		$('#mailBodyContainer').mask();
	});
	$('#detail-form .edit-btn').click(function() {
		$('#mailBodyContainer').unmask();
	});

	$('#addAttachment').click(function() {
		var url = $(this).attr('data-url');
		isell.InnerWindow({
			url			: url,
			containerId	: 'tpl-upload',
			winConfig	: {
				// left : '10%',
				// top : '75%',
				follow	: '#addAttachment'
			},
			callback	: function(innerWin) {

				$('#tpl-upload').find('form').submit(function(e) {
					e.preventDefault();
					var form$ = $(this);
					var data = JsUtils.serializeForm(form$);
					delete data.file;
					$.ajaxFileUpload({
						url				: form$.attr('action'),
						secureuri		: false,
						fileElementId	: 'fileToUpload',
						dataType		: 'json',
						data			: data,
						success			: function(data, status) {
							if (data.result.data) {
								insertNewAttach(data.result.data);
								innerWin.close();
							}
						},
						error			: function(data, status, e) {
							console.error(e);
						}
					})
				});
			}
		});
	});

	var athTpl = '<li class="attachment-row"><a href="{basePath}/file?key={key}" target="_blank" data-key="{key}" class="linkWithUnderline attach-link">{key}'
	        + '</a><input type="button" class="link-btn delete-btn red" alt="delete this attachment" value="X" data-url="{basePath}/file/delete.json?key={key}"></li>';
	function insertNewAttach(key) {
		$('#uploaded-attachments').append(athTpl.format({
			basePath	: basePath,
			key			: key
		}))
	}

	$('#uploaded-attachments .delete-btn ').live('click', function(e) {
		if (!confirm('Delete it?'))
			return;
		e.preventDefault();
		var this$ = $(this);
		$.getJSON($(this).attr('data-url'), function(data) {
			if (data && data.result.msg) {
				this$.parents('.attachment-row').remove();
				alert(data.result.msg);
			}
		});
	});

	$('#detail-form').submit(function(e) {
		var arr = [];
		$('#uploaded-attachments').find('a.attach-link').each(function() {
			arr.push($(this).attr('data-key'));
		});
		$('#allAttaches').val(JsUtils.encode(arr));
		return true;
	});

	$('#paramTarget').change(function() {
		var container = $('#targetParamsDef');
		var v = $(this).val();
		if (!v){
			container.empty();
			return;
		}
		var url = '{0}/email-template/targetTypeParams/{1}.json'.format(basePath, v);
		$.getJSON(url, function(data) {
			if (data.result.data) {
				var arr = data.result.data;
				var htmlArr = [];
				$.each(arr, function(i) {
					htmlArr.push('<li>&#36;{{0}}</li>'.format(arr[i]));
				});
				container.empty();
				container.append(htmlArr.join(''));
			}
		});
	});

});
