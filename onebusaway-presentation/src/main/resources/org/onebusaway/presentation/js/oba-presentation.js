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
 * Presentation Methods
 ******************************************************************************/

OBA.Presentation = function() {
	
	var that = {};
	
	that.nameAsString = function(name) {
		var result = '';
		jQuery.each(name.names, function() {
			if( result.length > 0)
				result += ' ';
			result += this;
		});
		return result;		
	};
	
	that.getNameForRoute = function(route) {
		var name = route.shortName;
		if( name == undefined || name == null || name.length == 0)
			name = route.longName;
		if( name == undefined || name == null || name.length == 0)
			name = route.id;
		return name;
	};
	
	that.applyStopNameToElement = function(stop,content) {
	    
		content.find(".stopName").text(stop.name);
	    
	    var stopCodeSpan = content.find(".stopCode");
	    var stopCodeText = OBA.L10n.format(stopCodeSpan.text(), stop.code);
	    stopCodeSpan.text(stopCodeText);
	    
	    var stopDirectionSpan = content.find(".stopDirection");
	    
	    if( stop.direction ) {
		    var stopDirectionText = OBA.L10n.format(stopDirectionSpan.text(), stop.direction);
		    stopDirectionSpan.text(stopDirectionText);
	    }
	    else {
	    	stopDirectionSpan.hide();
	    }	    
	};
	
	that.createStopInfoWindow = function(stop, params) {
		
		params = params || {};
		
		var templateClassName = params.templateClassName || 'StopInfoWindowTemplate';
		var content = jQuery('.' + templateClassName).clone();
		
		content.removeClass('StopInfoWindowTemplate');
		content.addClass('StopInfoWindow');
		
		that.applyStopNameToElement(stop,content);

	    configureStopInfoWindowRoutes(stop, content, params);

	    return content;
	};
	
	/****
	 * Private Methods
	 ****/
	
	var configureStopInfoWindowRoutes = function(stop, content, params) {
	    
		var routes = stop.routes;
	    var routesSection = content.find('.routesSection');
	    var routeClickHandler = params.routeClickHandler;
	    
	    if( routes == undefined || routes.length == 0 || params.hideRoutes) {
	    	routesSection.hide();
	    	return
	    }

	    var shortNameRoutes = [];
	    var longNameRoutes = [];
	    
	    jQuery.each(routes, function() {
	    	var name = that.getNameForRoute(this);
	    	if( name.length > 3)
	    		longNameRoutes.push(this);
	    	else
	    		shortNameRoutes.push(this);
	    });

	    if( shortNameRoutes.length > 0) {
	    	
	    	var shortNameRoutesPanel = jQuery('<div/>');
	    	shortNameRoutesPanel.addClass('routesSubSection');
	    	shortNameRoutesPanel.appendTo(routesSection);

	    	jQuery.each(shortNameRoutes, function() {
	    		var w = jQuery('<span/>');
	    		w.addClass('routeShortName');
	    		w.text(that.getNameForRoute(this));
	    		w.appendTo(shortNameRoutesPanel);
	    		if( routeClickHandler ) {
	    			w.click(function() {
	    				routeClickHandler(route,stop);
	    			});
	    		}
	    	});
	    }
	    
	    if( longNameRoutes.length > 0) {
	    	
	    	var longNameRoutesPanel = jQuery('<div/>');
	    	longNameRoutesPanel.addClass('routesSubSection');
	    	longNameRoutesPanel.appendTo(routesSection);

	    	jQuery.each(longNameRoutes, function() {
	    		var name = that.getNameForRoute(this);
	    		var w = jQuery('<div/>');
	    		w.addClass('routeLongName');
	    		if( name.length > 10 )
	    			w.addClass('routeReallyLongName');
	    		w.text(name);
	    		w.appendTo(longNameRoutesPanel);
	    		if( routeClickHandler ) {
	    			w.click(function() {
	    				routeClickHandler(route,stop);
	    			});
	    		}
	    	});
	    }
	};
	
	return that;
}();