var oba_where_standard_plan = function(data) {

	var mapParams = {};
	mapParams.lat = 47.606828;
	mapParams.lon = -122.332505;
	mapParams.zoom = 12;
	
	var map = OBA.Maps.map(data.mapElement, mapParams);
	var infoWindow = new google.maps.InfoWindow();
	
	/****
	 * 
	 ****/
	
	var configureOptionsToggle = function() {
		
		var toggleElement = jQuery('#optionsToggleAnchor');
		var advancedOptionsElement = jQuery('#advancedSearchOptions');
		
		toggleElement.click(function() {
			var isVis = advancedOptionsElement.is(':visible');
			toggleElement.text(isVis ? 'Show Options' : 'Hide Options');
			advancedOptionsElement.slideToggle();
		});
	};
	
	configureOptionsToggle();
	
	/****
	 * 
	 ****/
	
	var fromElement = data.fromElement;
	var toElement = data.toElement;
	var focusedElement = null;
	
	fromElement.focusin(function() {
		focusedElement = fromElement;
	});
	fromElement.focusout(function() {
		focusedElement = null;
	});
	
	toElement.focusin(function() {
		focusedElement = toElement;
	});
	toElement.focusout(function() {
		focusedElement = null;
	});

	google.maps.event.addListener(map, "click", function(o) {
		var latlng = o.latLng;
		if (latlng && focusedElement) {
			var txt = latlng.lat() + ',' + latlng.lng();
			focusedElement.val(txt);
		}
	});
	
	/****
	 * 
	 ****/
	
	var parseLocation = function() {
		var params = {};
		var href = window.location.href;
		var index = href.indexOf('?');
		if( index == -1)
			return;
		href = href.slice(index+1);
		index = href.indexOf('#');
		if( index != -1)
			href.slice(0, index);
		var tokens = href.split('&');
		jQuery.each(tokens,function() {
			var kvp = this.split('=');
			var key = kvp[0];
			var values = params[key] || [];
			params[key] = values.concat(kvp.slice(1)); 				
		});
		
		return params;
	};
	
	var locationParams = parseLocation();
	
	if( locationParams.from )
		fromElement.val(locationParams.from);
	
	if( locationParams.to )
		toElement.val(locationParams.to);
	
	/****
	 * 
	 ****/
	
	var planOverlays = [];
	var resultsPanel = data.resultsPanel;	
	
	var clearExistingPlan = function() {
		jQuery.each(planOverlays, function() {
			this.setMap(null);
		});
		planOverlays = [];
		
		resultsPanel.empty();
	};
	
	var legHandler = function(leg, nextLeg) {
		
		if( leg.transitLeg ) {
			transitLegHandler(leg, leg.transitLeg);
		}
		
		var streetLegs = leg.streetLegs || [];
		if( streetLegs.length > 0 ) {
			streetLegsHandler(leg, streetLegs, nextLeg);			
		}
	};
	
	var transitLegHandler = function(leg, transitLeg) {
		
		var path = transitLeg.path;
		if( path ) {
			var points = OBA.Maps.decodePolyline(path);
			var opts = {path: points, strokeColor: '#0000FF'};
	        var line = new google.maps.Polyline(opts);
	        line.setMap(map);
	        planOverlays.push(line);
		}
		
		var content = jQuery('.transitLegTemplate').clone();
		content.removeClass('transitLegTemplate');
		content.addClass('transitLeg');
		
		var stopNameElement = content.find(".stopName");
		var fromStop = transitLeg.fromStop;
		if( fromStop ) {
			stopNameElement.text(fromStop.name);
		}
		else {
			stopNameElement.text("continues as");
			stopNameElement.removeClass('stopNameEmphasized');
		}
		
		var routeShortNameElement = content.find(".routeShortName");
		var routeShortName = transitLeg.routeShortName || transitLeg.trip.route.shortName;		
		routeShortNameElement.text(routeShortName);
		
		var tripHeadsignElement = content.find(".tripHeadsign");
		var tripHeadsign = transitLeg.tripHeadsign || transitLeg.trip.tripHeadsign;		
		tripHeadsignElement.text(tripHeadsign);
		
		var startTime = OBA.L10n.formatDate('hh:mm AA',new Date(leg.startTime));
		var startTimeElement = content.find(".startTime");
		startTimeElement.text(startTime);

		var endTime = OBA.L10n.formatDate('hh:mm AA',new Date(leg.endTime));
		var endTimeElement = content.find(".endTime");
		endTimeElement.text(endTime);

		var mins = Math.ceil((leg.endTime - leg.startTime) / (60 * 1000));
		var elapsedTimeElement = content.find(".elapsedTime");
		elapsedTimeElement.text('(' + mins + ' mins)');
		
		var legStatus = content.find(".legStatus");
		
		if( transitLeg.predictedDepartureTime != 0) {
			var delta = Math.ceil((transitLeg.predictedDepartureTime - transitLeg.scheduledDepartureTime) / (60 * 1000));
			legStatus.text('scheduleDeivation: ' + delta)
		}
		else {
			legStatus.hide();
		}
		
		content.show();
		content.appendTo(resultsPanel);
	};
	
	var streetLegsHandler = function(leg, streetLegs, nextLeg) {
		
		var content = jQuery('.streetLegTemplate').clone();
		content.removeClass('streetLegTemplate');
		content.addClass('streetLeg');
		
		var label = 'destination';
		if( nextLeg && nextLeg.transitLeg && nextLeg.transitLeg.fromStop)
			var label = nextLeg.transitLeg.fromStop.name;
		var locationElement = content.find(".walkToLocation");
		locationElement.text(OBA.L10n.format(locationElement.text(),label));
		
		var mins = Math.ceil((leg.endTime - leg.startTime) / (60*1000));
		var timeElement = content.find('.walkToTime');
		timeElement.text( OBA.L10n.format(timeElement.text(),mins));
		
		content.show();
		content.appendTo(resultsPanel);
		
		var points = [];
		
		jQuery.each(streetLegs, function() {
			if( this.path ) {
				var legPoints = OBA.Maps.decodePolyline(this.path);
				points = points.concat(legPoints);
			}
		});
		
		if( points.length > 0) {
			var opts = {path: points, strokeColor: '#000000'};
	        var line = new google.maps.Polyline(opts);
	        line.setMap(map);
	        planOverlays.push(line);
		}
	};
	
	var planHandler = function(entry) {
		
		clearExistingPlan();
		
		var itineraries = entry.itineraries || [];
		if( itineraries.length == 0 )
			return;
		var itinerary = itineraries[0];
		var legs = itinerary.legs || [];
		
		jQuery.each(legs, function(index) {
			var leg = this;
			var nextLeg = undefined;
			if( index + 1 < legs.length )
				nextLeg = legs[index+1];
			legHandler(leg,nextLeg);
		});
	};
	
	var directionsButton = data.directionsButton;
	
	directionsButton.click(function() {
		
		var from = fromElement.val().split(',');
		var to = toElement.val().split(',');
		var params = {};
		
		params.latFrom = from[0];
		params.lonFrom = from[1];
		params.latTo = to[0];
		params.lonTo = to[1];
		params.timeFrom = '2011-01-31_12-48-00';
		
		OBA.Api.planTrip(params, planHandler );
	});
};