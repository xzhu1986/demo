JsUtils.namespace('isell');
isell.InnerWindow = function(options) {
	var opts = $.extend(true, {
		containerId	: '',
		url		    : null,// optional
		callback	: function(innerWin) {
		},
		winConfig	: {// http://www.planeart.cn/demo/artDialog/_doc/API.html
			fixed	: false,
			follow	: null// #id
			// left : '50%',
			// top : '38.2%'
		}

	}, options);

	var containerId = opts.containerId;
	var dialogId = 'InnerWindow_' + containerId;

	var container$ = $('#' + containerId);
	if (container$.length == 0) {
		$('<div id="{0}" style="display:none;"/>'.format(containerId)).appendTo($('body'));
	}
	if (opts.url) {
		$('#' + containerId).load(opts.url, function(responseText, textStatus, XMLHttpRequest) {
			if (XMLHttpRequest.status != 200) {
				alert(XMLHttpRequest.statusText);
			} else {
				if (window[dialogId]) {
					try {
						window[dialogId].close();
					} catch (err) {
						console.log(err);
					}
				}

				window[dialogId] = art.dialog($.extend({
					content	: document.getElementById(containerId),
					close	: function() {
						delete window[dialogId];
					}
				}, opts.winConfig));
			}
			if (opts.callback && JsUtils.isFunction(opts.callback))
				opts.callback(window[dialogId]);
		});
	} else {
		window[dialogId] = art.dialog($.extend({
			content	: document.getElementById(containerId),
			close	: function() {
				delete window[dialogId];
			}
		}, opts.winConfig));
		if (opts.callback && JsUtils.isFunction(opts.callback))
			opts.callback(window[dialogId]);
	}

	return {
		close		: function() {
			var dlg = window[dialogId];
			if (dlg)
				dlg.close();
			delete window[dialogId];
		},
		relocate	: function() {
			var dlg = window[dialogId];
			if (dlg)
				dlg._reset();
		}
	};

}