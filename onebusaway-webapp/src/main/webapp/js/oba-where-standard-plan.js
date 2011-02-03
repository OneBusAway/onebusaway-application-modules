var oba_where_standard_plan = function(data) {

	var mapParams = {};
	mapParams.lat = 47.606828;
	mapParams.lon = -122.332505;
	mapParams.zoom = 12;
	
	var map = OBA.Maps.map(data.mapElement, mapParams);
	var markerManager = OBA.Maps.markerManager(map);
	var infoWindow = new google.maps.InfoWindow();
	
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
	
	var planOverlays = [];
	var resultsPanel = data.resultsPanel;	
	
	var clearExistingPlan = function() {
		jQuery.each(planOverlays, function() {
			this.setMap(null);
		});
		planOverlays = [];
		
		resultsPanel.empty();
	};
	
	var legHandler = function() {
		
		if( this.transitLeg ) {
			transitLegHandler(this, this.transitLeg);
		}
		
		var streetLegs = this.streetLegs;
		if( streetLegs ) {
			streetLegsHandler(streetLegs);			
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
		
		var routeShortNameElement = content.find(".routeShortName");
		var routeShortName = transitLeg.routeShortName || transitLeg.trip.route.shortName;		
		routeShortNameElement.text(routeShortName);
		
		var tripHeadsignElement = content.find(".tripHeadsign");
		var tripHeadsign = transitLeg.tripHeadsign || transitLeg.trip.tripHeadsign;		
		tripHeadsignElement.text(tripHeadsign);
		
		var mins = Math.ceil((leg.endTime - leg.startTime) / (60 * 1000));
		var elapsedTimeElement = content.find(".elapsedTime");
		elapsedTimeElement.text('(' + mins + ' mins)');
		
		content.show();
		content.appendTo(resultsPanel);
	};
	
	var streetLegsHandler = function(streetLegs) {
		
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
		
		jQuery.each(legs, legHandler);
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