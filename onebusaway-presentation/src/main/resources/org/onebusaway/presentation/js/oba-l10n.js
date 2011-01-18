var OBA = window.OBA || {};

/*******************************************************************************
 * L10n Methods
 ******************************************************************************/

OBA.L10n = {};

OBA.L10n.ChoiceFormat = function(message) {
	
};

OBA.L10n.MessageFormat = function(message) {
	
	var that = {};
	
	var parseSubFormat = function(type,args) {
		if( type == 'choice') {
			return OBA.L10n.ChoiceFormat(args);
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