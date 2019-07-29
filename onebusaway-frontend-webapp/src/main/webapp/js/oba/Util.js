/*
 * Copyright (c) 2011 Metropolitan Transportation Authority
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

OBA.Util = (function() {
	
	// From http://delete.me.uk/2005/03/iso8601.html
	var ISO8601StringToDate = function(str) {	    	
    	var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
    	"(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?" +
    	"(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";

    	var d = str.match(new RegExp(regexp));

    	var offset = 0;
    	var date = new Date();
    	date.setFullYear(d[1]);

    	if (d[3]) { date.setMonth(d[3] - 1); }
    	if (d[5]) { date.setDate(d[5]); }
    	if (d[7]) { date.setHours(d[7]); }
    	if (d[8]) { date.setMinutes(d[8]); }
    	if (d[10]) { date.setSeconds(d[10]); }
    	if (d[12]) { date.setMilliseconds(Number("0." + d[12]) * 1000); }
    	if (d[14]) {
    		offset = (Number(d[16]) * 60) + Number(d[17]);
    		offset *= ((d[15] == '-') ? 1 : -1);
    	}

    	offset -= date.getTimezoneOffset();
    	
    	var time = (Number(date) + (offset * 60 * 1000));
    	var ret = new Date();

    	ret.setTime(Number(time));
    	
    	return ret;
    };
    
    // djb2 from http://erlycoder.com/49/javascript-hash-functions-to-convert-string-into-integer-hash-
    String.prototype.hashCode = function() {
    	var i;
    	var hash = 5381;
        for (i = 0; i < this.length; i++) {
            char = this.charCodeAt(i);
            hash = ((hash << 5) + hash) + char; /* hash * 33 + c */
        }
        return hash;
    };
	
	return {
		log: function(s) {
			if(OBA.Config.debug === true && typeof console !== 'undefined' && typeof console.log !== 'undefined') {
				console.log(s);
			}
		},
		decodePolyline: function(encoded) {
			var len = encoded.length;
			var index = 0;
			var array = [];
			var lat = 0;
			var lng = 0;

			while (index < len) {
				var b;
				var shift = 0;
				var result = 0;
				do {
					b = encoded.charCodeAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				var dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
				lat += dlat;

				shift = 0;
				result = 0;
				do {
					b = encoded.charCodeAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				var dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				array.push([lat * 1e-5, lng * 1e-5]);
			}

			return array;
		},
		ISO8601StringToDate: ISO8601StringToDate,
		getArrivalEstimateForISOString: function(predictionDateString, referenceDateObj, minutesText) {
			if(typeof predictionDateString === 'undefined' || predictionDateString === null) {
				return null;
			}
			
			var predictionDateObj = ISO8601StringToDate(predictionDateString);

			var minutesAway = Math.floor((predictionDateObj - referenceDateObj) / 60 / 1000);

			return minutesAway + " " + (minutesText || "minute") + ((Math.abs(minutesAway) === 1) ? "" : "s");
		},
		getArrivalEstimateForISOStringWithCheck: function(predictionDateString, referenceDateObj, minutesText, distanceAway) {
			if(typeof predictionDateString === 'undefined' || predictionDateString === null) {
				return null;
			}

			var predictionDateObj = ISO8601StringToDate(predictionDateString);

			var minutesAway = Math.floor((predictionDateObj - referenceDateObj) / 60 / 1000);
			if (minutesAway === 0 && distanceAway > 1000 /* 1000 m*/) {
				// prediction and distance disagree, hide prediction
			    return null;
            }
			if (minutesAway < 0) {
				//prediction is in the past
				return null;
			}
			return minutesAway + " " + (minutesText || "minute") + ((Math.abs(minutesAway) === 1) ? "" : "s");
		},
		debugTime: function(timestamp) {
			if (typeof timestamp === 'undefined' || timestamp === null) {
				return "nSt";
			}
			return ISO8601StringToDate(timestamp).toLocaleTimeString('it-IT');
		},
		displayTime: function(secondsAgo) {
			secondsAgo = Math.floor(secondsAgo);
			if(secondsAgo < 60) {
				return secondsAgo + " second" + ((secondsAgo === 1) ? "" : "s") + " ago";
			} else {
				var minutesAgo = Math.floor(secondsAgo / 60);
				secondsAgo = secondsAgo - (minutesAgo * 60);
				
				var s = minutesAgo + " minute" + ((minutesAgo === 1) ? "" : "s");
				if(secondsAgo > 0) {
					s += ", " + secondsAgo + " second" + ((secondsAgo === 1) ? "" : "s");
				}
				s += " ago";
				return s;
			}
		},
		displayStopId: function(s) {
			if (OBA.Config.useAgencyId == 'true') {
				return s;
			}
			return s.match(/\d*$/);
		}
	};
})();
