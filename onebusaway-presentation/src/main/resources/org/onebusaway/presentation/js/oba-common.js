/*
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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