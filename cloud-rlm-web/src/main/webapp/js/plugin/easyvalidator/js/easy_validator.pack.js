/*
 * Copyright (C) 2009 - 2012 WebSite: Http://wangking717.javaeye.com/ Author: wangking
 */
// $(function(){ //change by frankw
$(document).ready(function() {
	var xOffset = -20; // x distance from mouse
	var yOffset = 20; // y distance from mouse
	// jQuery 1.4.1 now supports "hover" for live() events, but only with one event handler function:
	// provide two functions, one for mouseenter and one for mouseleave:
	// input action
	$("[reg],[url]:not([reg]),[tip]").live({
		mouseenter	: function(e) {
			if ($(this).attr('tip') != undefined) {
				var top = (e.pageY + yOffset);
				var left = (e.pageX + xOffset);
				$('body').append('<p id="vtip"><img id="vtipArrow" src="{0}/js/plugin/easyvalidator/images/vtip_arrow.png"/>'.format(resourceBasePath)
				        + $(this).attr('tip') + '</p>');
				$('p#vtip').css("top", top + "px").css("left", left + "px");
				$('p#vtip').bgiframe();
			}
		},
		mouseleave	: function() {
			if ($(this).attr('tip') != undefined) {
				$("p#vtip").remove();
			}
		}
	}).live('mousemove', function(e) {
		if ($(this).attr('tip') != undefined) {
			var top = (e.pageY + yOffset);
			var left = (e.pageX + xOffset);
			$("p#vtip").css("top", top + "px").css("left", left + "px");
		}
	}).live('change', function() {
		if ($(this).attr("url") != undefined) {
			ajax_validate($(this));
		} else if ($(this).attr("reg") != undefined) {
			validate($(this));
		}
	});

	$("form").live('submit', function() {
		return formValid($(this));
	});
});
// });

function formValid(form$) {
	var isSubmit = true;
	form$.find("[reg],[url]:not([reg])").each(function() {
		if ($(this).attr("reg") == undefined) {
			if (!ajax_validate($(this))) {
				isSubmit = false;
			}
		} else {
			if (!validate($(this))) {
				isSubmit = false;
			}
		}
	});
	if (typeof(isExtendsValidate) != "undefined") {
		if (isSubmit && isExtendsValidate) {
			if (extendsValidate() == false)
				return false;
		}
	}
	if (isSubmit && form$.find('.save-btn').length > 0) {
		form$.mask("Wait a moment ...");
	}
	return isSubmit;
}

function validate(obj) {
	var reg = new RegExp(obj.attr("reg"));
	var objValue = obj.attr("value");

	if (!reg.test(objValue)) {
		change_error_style(obj, "add");
		change_tip(obj, null, "remove");
		return false;
	} else {
		if (obj.attr("url") == undefined) {
			change_error_style(obj, "remove");
			change_tip(obj, null, "remove");
			return true;
		} else {
			return ajax_validate(obj);
		}
	}
}

function ajax_validate(obj) {

	var url_str = obj.attr("url");
	if (url_str.indexOf("?") != -1) {
		url_str = url_str + "&" + obj.attr("name") + "=" + obj.attr("value");
	} else {
		url_str = url_str + "?" + obj.attr("name") + "=" + obj.attr("value");
	}
	url_str = encodeURI(url_str);
	url_str = encodeURI(url_str);

	var isSubmit = true;
	$.ajax({
		url		 : url_str,
		// contentType : "application/x-www-form-urlencoded;charset=UTF-8",
		cache		: false,
		async		: false,
		dataType	: 'json',
		success		: function(data) {
			if (data && data.result) {
				if (data.result.success) {
					change_error_style(obj, "remove");
					change_tip(obj, null, "remove");
					isSubmit = true;
				} else {
					var msg = data.result.msg;
					change_error_style(obj, "add");
					change_tip(obj, msg, "add");
					isSubmit = false;
				}
			}
		}
	});
	return isSubmit;
}

function change_tip(obj, msg, action_type) {
	if (obj.attr("tip") == undefined) {// 初始化判断TIP是否为空
		obj.attr("is_tip_null", "yes");
	}
	if (action_type == "add") {
		if (obj.attr("is_tip_null") == "yes") {
			obj.attr("tip", msg);
		} else {
			if (msg != null) {
				if (obj.attr("tip_bak") == undefined) {
					obj.attr("tip_bak", obj.attr("tip"));
				}
				obj.attr("tip", msg);
			}
		}
	} else {
		if (obj.attr("is_tip_null") == "yes") {
			obj.removeAttr("tip");
			obj.removeAttr("tip_bak");
		} else {
			obj.attr("tip", obj.attr("tip_bak"));
			obj.removeAttr("tip_bak");
		}
	}
	if (obj.data("combox-cascading")) {
		var inputobj = obj.data("combox-cascading");
		if (obj.attr("tip")) {
			inputobj.attr("tip", obj.attr("tip"));
		} else {
			inputobj.removeAttr("tip");
		}
	}
}

function change_error_style(obj, action_type) {
	if (action_type == "add") {
		obj.addClass("input_validation-failed");
	} else {
		obj.removeClass("input_validation-failed");
	}
	if (obj.data("combox-cascading")) {
		var inputobj = obj.data("combox-cascading");
		if (obj.is(".input_validation-failed")) {
			inputobj.addClass("input_validation-failed");
		} else {
			inputobj.removeClass("input_validation-failed");
		}
	}
}

$.fn.validate_callback = function(msg, action_type, options) {
	this.each(function() {
		if (action_type == "failed") {
			change_error_style($(this), "add");
			change_tip($(this), msg, "add");
		} else {
			change_error_style($(this), "remove");
			change_tip($(this), null, "remove");
		}
	});
};
