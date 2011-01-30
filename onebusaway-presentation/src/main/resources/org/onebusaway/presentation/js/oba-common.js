var OBA = window.OBA || {};

/*******************************************************************************
 * Common Methods
 ******************************************************************************/

var obaCommonFactory = function() {

	var that = {};

	that.buildUrlQueryString = function(params) {
		var queryString = '';
		var seenFirst = false;
		
		for( var key in params ) {
			var values = params[key];
			if( ! (values instanceof Array))
				values = [values];
			jQuery.each(values,function() {
				if( seenFirst ) {
					queryString += '&';
				}
				else {
					queryString += '?';
					seenFirst = true;
				}
				queryString += encodeURIComponent(key) + '=' + encodeURIComponent(this);
			});
		}
		
		return queryString;
	};
	
	return that;
};

OBA.Common = obaCommonFactory();