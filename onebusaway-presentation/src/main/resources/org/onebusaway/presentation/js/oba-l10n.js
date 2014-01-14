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
 * L10n Methods
 ******************************************************************************/

OBA.L10n = {};

OBA.L10n.ChoiceFormat = function(message) {
	
};

/*
 * Adapted from Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 */
OBA.L10n.DateFormat = function(format) {

	var pad = function (val, len) {
		val = String(val);
		len = len || 2;
		while (val.length < len) val = "0" + val;
		return val;
	};
	
	var	token = /d{1,4}|m{1,4}|yy(?:yy)?|([aAHhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g;
	var timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g;
	var timezoneClip = /[^-+\dA-Z]/g;

	var that = {};
	
	that.format = function(date) {

		var utc = false;
		
		var	_ = utc ? "getUTC" : "get";
		var vd = date[_ + "Date"]();
		var vD = date[_ + "Day"]();
		var vM = date[_ + "Month"]();
		var vy = date[_ + "FullYear"]();
		var vH = date[_ + "Hours"]();
		var vm = date[_ + "Minutes"]();
		var vs = date[_ + "Seconds"]();
		var vL = date[_ + "Milliseconds"]();
		var vo = utc ? 0 : date.getTimezoneOffset();
				
		var flags = {};

		flags.yy = String(vy).slice(2);
		flags.yyyy = vy;
		flags.M = vM + 1;
		flags.MM = pad(flags.M);
		flags.MMM = OBA.Resources.DateLibrary['shortMonths'][vM];
		flags.MMMM = OBA.Resources.DateLibrary['months'][vM];
		flags.d = vd;
		flags.dd = pad(vd);
		flags.h = vH % 12 || 12;
		flags.hh = pad(flags.h);
		flags.m = vm + 1,
		flags.mm = pad(flags.m);
		
		flags.AA = OBA.Resources.DateLibrary['amPm'][vH < 12 ? 0 : 1];
		flags.aa = flags.AA.toLowerCase();
		
		/*
			flags = {
				d:    d,
				dd:   pad(d),
				ddd:  dF.i18n.dayNames[D],
				dddd: dF.i18n.dayNames[D + 7],
				m:    m + 1,
				mm:   pad(m + 1),
				mmm:  dF.i18n.monthNames[m],
				mmmm: dF.i18n.monthNames[m + 12],
				yy:   String(y).slice(2),
				yyyy: y,
				H:    H,
				HH:   pad(H),
				K:    H % 12,
				KK:   pad(H % 12),
				M:    M,
				MM:   pad(M),
				s:    s,
				ss:   pad(s),
				l:    pad(L, 3),
				L:    pad(L > 99 ? Math.round(L / 10) : L),
				t:    H < 12 ? "a"  : "p",
				tt:   H < 12 ? "am" : "pm",
				T:    H < 12 ? "A"  : "P",
				TT:   H < 12 ? "AM" : "PM",
				Z:    utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
				o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
				S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
			};
			*/

		return format.replace(token, function ($0) {
			return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
		});
	};
	
	return that;	
};

OBA.L10n.MessageFormat = function(message) {
	
	var that = {};
	
	var parseSubFormat = function(type,args) {
		if( type == 'choice') {
			return OBA.L10n.ChoiceFormat(args);
		}
		else if( type == 'date') {
			return OBA.L10n.DateFormat(args);
		}
		else {
			return function(arg) { return arg; };	
		}
	};
	
	var parseMessage = function(message) {
		
		var parts = new Array();
		
		/**
		 * What depth of the format stack are we in:
		 * 
		 * 	"000 {111,222,333} 000 {111,222,333}"
		 * 
		 * 0 - basic text
		 * 1 - argument index
		 * 2 - format type
		 * 3 - format arguments
		 */
        var formatDepth = 0;
        
        /**
         * A stack of strings for each depth of the format message (0-3), where the latest
         * content for each depth is appended to the appropriate string
         */
        var formatStack = ['','','',''];
        
        var inQuote = false;
        var braceDepth = 0;
        
        for (var i = 0; i < message.length; i++) {
            
        	var ch = message.charAt(i);
            
            if (formatDepth == 0) {
                if (ch == '\'') {
                	/**
                	 * Allow escaping a single quote with ''
                	 */
                    if (i + 1 < message.length && message.charAt(i+1) == '\'') {
                        formatStack[formatDepth] += ch;
                        i++;
                    } else {
                        inQuote = !inQuote;
                    }
                } else if (ch == '{' && !inQuote) {
                	if( formatStack[0].length > 0) {
                		parts.push({'value':formatStack[0]});
                		formatStack[0] = '';                		
                	}
                    formatDepth = 1;
                } else {
                    formatStack[formatDepth] += ch;
                }
            } else if (inQuote) {
                formatStack[formatDepth] += ch;
                if (ch == '\'') {
                    inQuote = false;
                }
            } else {
                switch (ch) {
                case ',':
                    if (formatDepth < 3)
                        formatDepth += 1;
                    else
                        formatStack[formatDepth] += ch;
                    break;
                case '{':
                    braceDepth++;
                    formatStack[formatDepth] += ch;
                    break;
                case '}':
                    if (braceDepth == 0) {
                        formatDepth = 0;
                        var formatter = parseSubFormat(formatStack[2],formatStack[3]);
                        var argumentIndex = parseInt(formatStack[1]);
                        parts.push({'formatter':formatter,'argumentIndex':argumentIndex});
                        
                        // Reset everything
                		for (var j = 0; j < formatStack.length; ++j) {
                            formatStack[j] = '';
                		}
                    } else {
                        braceDeptch--;
                        formatStack[formatDepth] += ch;
                    }
                    break;
                case '\'':
                    inQuote = true;
                    // fall through, so we keep quotes in other parts
                default:
                    formatStack[formatDepth] += ch;
                    break;
                }
            }
        }

        if (braceDepth == 0 && formatDepth != 0)
            throw "expected closing brace in message format";
        
        if( formatStack[0].length > 0) {
    		parts.push({'value':formatStack[0]});
    		formatStack[0] = '';                		
    	}
        
        return parts;
	};

	var parts = parseMessage(message);
	
	
	that.format = function() {
		var result = '';
		
		for( var i=0; i < parts.length; i++) {
			var part = parts[i];
			if( part.value ) {
				result += part.value;
			}
			else if( part.formatter ) {
				if( part.argumentIndex >= arguments.length ) {
					result += '{' + part.argumentIndex + '}';
				}
				else {
					var arg = arguments[part.argumentIndex];
					result += part.formatter(arg);
				}
			}
		}
		
		return result;
	};
	
	return that;
};

OBA.L10n.format = function() {
	
	var slice = Array.prototype.slice;
	var args = slice.apply(arguments);
	var message = args.shift();
	if( !message )
		return '';
	var m = OBA.L10n.MessageFormat(message);
	return m.format.apply(m,args);
};

OBA.L10n.formatDate = function() {
	
	var slice = Array.prototype.slice;
	var args = slice.apply(arguments);
	var message = args.shift();
	if( !message )
		return '';
	var m = OBA.L10n.DateFormat(message);
	return m.format.apply(m,args);
};

