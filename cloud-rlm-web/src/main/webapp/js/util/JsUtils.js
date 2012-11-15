JsUtils = {
	isFunction	           : function(fn) {
		return !!fn && !fn.nodeName && fn.constructor != String && fn.constructor != RegExp && fn.constructor != Array && /function/i.test(fn + "");
	},
	round	               : function(number, fractionDigits) {
		with (Math) {
			return round(number * pow(10, fractionDigits)) / pow(10, fractionDigits);
		}
	},
	encode	               : function(object) {
		var type = typeof object;
		if ('object' == type) {
			if (Array == object.constructor)
				type = 'array';
			else if (RegExp == object.constructor)
				type = 'regexp';
			else
				type = 'object';
		}
		switch (type) {
			case 'undefined' :
			case 'unknown' :
				return;
			break;
			case 'function' :
			case 'boolean' :
			case 'regexp' :
				return object.toString();
			break;
			case 'number' :
				return isFinite(object) ? object.toString() : 'null';
			break;
			case 'string' :
				return '"' + object.replace(/(\\|\")/g, "\\$1").replace(/\n|\r|\t/g, function() {
					var a = arguments[0];
					return (a == '\n') ? '\\n' : (a == '\r') ? '\\r' : (a == '\t') ? '\\t' : ""
				}) + '"';
			break;
			case 'object' :
				if (object === null)
					return 'null';
				var results = [];
				for (var property in object) {
					var value = JsUtils.encode(object[property]);
					if (value !== undefined)
						results.push(JsUtils.encode(property) + ':' + value);
				}
				return '{' + results.join(',') + '}';
			break;
			case 'array' :
				var results = [];
				for (var i = 0; i < object.length; i++) {
					var value = JsUtils.encode(object[i]);
					if (value !== undefined)
						results.push(value);
				}
				return '[' + results.join(',') + ']';
			break;
		}
	},
	decode	               : function(jsonStr) {
		return eval("(" + jsonStr + ")");
	},
	openWindow	           : function(url, title, width, height) {
		if (window.showModalDialog != null) {
			var left = (screen.width - width) / 2;
			var top = (screen.height - height) / 2;
			window.showModalDialog(url, title, 'dialogWidth={0};dialogHeight={1};status=no;help=no;scroll=yes,resizable=false,dialogLeft:{2},dialogTop:{3}'.format(width, height, left, top));
		} else {
			window.open(URL, title, "width={0},height={1},top={1},left={2},toolbar=no,menubar=no,location=no,scrollbars=yes,directories=no".format(width, height, (screen.height - height)
			        / 2, (screen.width - width) / 2));

		}
		return false;

		// "width={0},height={1},top={1},left={2},toolbar=no,menubar=no,location=no,scrollbars=yes,directories=no".format(width,height,(screen.height
		// - height) / 2 ,(screen.width-width)/2));
		// based on screen
		// window.open(URL, '',
		// "width={0},height={1},top={1},left={2},toolbar=no,menubar=no,location=no,scrollbars=yes,directories=no".format(width,height,(screen.height
		// - height) / 2 ,(screen.width-width)/2));
		// based on page
		// window.open(URL,'','width='+width+',height='+height+',top='+(document.body.offsetHeight-height)/2+',left='+(document.body.offsetWidth-width)/2);
		// window.open(URL,'','width='+width+',height='+height+',top=250,left='+document.body.offsetWidth/3);
	},
	namespace	           : function() {
		var a = arguments, o = null, i, j, d;
		for (i = 0; i < a.length; i = i + 1) {
			d = a[i].split(".");
			o = window;
			for (j = 0; j < d.length; j = j + 1) {
				o[d[j]] = o[d[j]] || {};
				o = o[d[j]];
			}
		}
		return o;
	},
	serializeForm	       : function(form$) {
		if (!form$.is('form')) {
			throw new Error('not a form:' + form$);
		}
		var arr = form$.serializeArray();
		var data = {};
		$.each(arr, function(i, v) {
			data[v.name] = v.value;
		});
		return data;
	},
	reloadParentWinOnClose	: function() {
		window.onunload = function(e) {
			try {
				window.opener.location.reload();
			} catch (err) {
				console.log(err);
			}
		}
	}
};

/**
 * stirng to int
 */
function pi(value) {
	return ti(parseFloat(value));
}
/**
 * string to float
 */
function pf(value) {
	return tf(parseFloat(value));
}
/**
 * 
 * number to int
 */
function ti(value) {
	return Math.round(value);
}
/**
 * number to float
 */
function tf(value) {
	return JsUtils.round(value, 2);
}

String.prototype.format = function() {
	var args = arguments;
	if (typeof args[0] == 'string') {
		return this.replace(/\{(\d+)\}/g, function(m, i) {
			var r=args[i];
			return r==null?'':r;
		});
	} else {
		return this.replace(/\{(\w+)\}/g, function(m, name) {
			var r= args[0][name];
			return r==null?'':r;
		});
	}
}

function hasAttr($element, attrName) {
	return typeof($element.attr(attrName)) != "undefined";
}

(function() {
	function dcmAdd(arg1, arg2) {
		var r1, r2, m;
		try {
			r1 = arg1.toString().split(".")[1].length
		} catch (e) {
			r1 = 0
		}
		try {
			r2 = arg2.toString().split(".")[1].length
		} catch (e) {
			r2 = 0
		}
		m = Math.pow(10, Math.max(r1, r2));
		return (dcmMul(arg1, m) + dcmMul(arg2, m)) / m;
	}
	function dcmSbt(arg1, arg2) {
		return dcmAdd(arg1, -arg2);
	}
	function dcmMul(arg1, arg2) {
		var m = 0, s1 = arg1.toString(), s2 = arg2.toString();
		try {
			m += s1.split(".")[1].length
		} catch (e) {
		}
		try {
			m += s2.split(".")[1].length
		} catch (e) {
		}
		return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m);
	}
	function dcmDiv(arg1, arg2) {
		return dcmMul(arg1, 1 / arg2);
	}
	function round(number, fractionDigits) {
		with (Math) {
			return round(number * pow(10, fractionDigits)) / pow(10, fractionDigits);
		}
	}

	// add calculation methods to prototype of Number

	Number.prototype.round = function(precision) {
		var prec = precision ? precision : 2;
		return round(this, prec);
	};
	Number.prototype.add = function(arg) {
		return dcmAdd(this, arg);
	};
	Number.prototype.sub = function(arg) {
		return dcmSbt(this, arg);
	};
	Number.prototype.mul = function(arg) {
		return dcmMul(this, arg);
	};
	Number.prototype.div = function(arg) {
		return dcmDiv(this, arg);
	};

})();
// a simple hash set
Set = function() {
	var data = {};

	return {
		add		 : function(v) {
			data[v] = true;
		},
		remove		: function(v) {
			delete data[v];
		},
		contains	: function(v) {
			return v in data;
		},
		values		: function(v) {
			var r = [];
			for (var v in data) {
				r.push(v);
			}
			return r;
		}
	};
}
