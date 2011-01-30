var oba_where_standard_trip = function(data) {
	
	var map = OBA.Maps.map(data.mapElement);
	var markerManager = OBA.Maps.markerManager(map);
	var infoWindow = new google.maps.InfoWindow();
	
	/****
	 * Draw a shape on the map in response to an api call
	 */
	
	var shapeHandler = function(shapeEntry ) {
		if( ! shapeEntry.points )
			return;
		
		var path = OBA.Maps.decodePolyline(shapeEntry.points);
		
        var opts = {path: path, strokeColor: '#000000'};
        var line = new google.maps.Polyline(opts);
        line.setMap(map);
		
		var bounds = OBA.Maps.getPointsAsBounds(path);
        
		if( ! bounds.isEmpty() )
			map.fitBounds(bounds);
	};
	
	/**
	 * Create map markers for a stop and a pop-up info window for display when clicked
	 */
	var stopHandler = function(stop, stopSequence) {
		
		var markers = OBA.Maps.addStopToMarkerManager(stop, markerManager);
		
		var infoWindowContent = 
		
		jQuery.each(markers,function(){
			google.maps.event.addListener(this, 'click', function() {
				
				var content = jQuery('.stopInfoWindowTemplate').clone();
			    content.find("h3").text(stop.name);
			    
			    var stopCodeSpan = content.find("span.stopCode");
			    var stopCodeText = OBA.L10n.format(stopCodeSpan.text(), stop.code);
			    stopCodeSpan.text(stopCodeText);
			    
			    var stopDirectionSpan = content.find("span.stopDirection");
			    
			    if( stop.direction ) {
				    var stopDirectionText = OBA.L10n.format(stopDirectionSpan.text(), stop.direction);
				    stopDirectionSpan.text(stopDirectionText);
			    }
			    else {
			    	stopDirectionSpan.hide();
			    }
			    
			    var anchor = content.find("p>a");
			    var url = anchor.attr("href");
			    
				var params = {};
				
				params.stopId = data.stopId;
				params.tripId = data.tripId;
				params.serviceDate = data.serviceDate;
				params.stopSequence = stopSequence;
				if( data.vehicleId )
					params.vehicleId = data.vehicleId;
				
				url += OBA.Common.buildUrlQueryString(params);
			    
				anchor.attr("href",url);
			    
			    infoWindow.setContent(content.show().get(0));
			    infoWindow.open(map,this);
		     });	
		});
	};
	
	/**
	 * Trip details handler that displays the shape of the trip, if applicable, and creates
	 * markers for all the stops along the trip
	 */
	var tripDetailsHandler = function(tripDetails) {
		if( tripDetails.trip) {
			var trip = tripDetails.trip;
			if( trip.shapeId )
				OBA.Api.shape(trip.shapeId, shapeHandler);
		}
		
		var schedule = tripDetails.schedule;
		var stopTimes = schedule.stopTimes;
		
		jQuery.each(stopTimes, function(index) {
			stopHandler(this.stop, index);
		});
	};
	
	/****
	 * Request the full trip details
	 ****/
	
	var params = {};
	
	params.tripId = data.tripId;
	params.serviceDate = data.serviceDate;
	params.time = data.time;
	if( data.vehicleId )
		params.vehicleId = data.vehicleId;
	params.includeTrip = true;
	params.includeStatus = true;
	params.includeSchedule = true;
	
	OBA.Maps.mapReady(map,function(){
		OBA.Api.tripDetails(params, tripDetailsHandler);
	});
	
	/****
	 * Show a circle around the active stop, if applicable
	 ****/
	
	var selectedStopHandler = function(stop) {
		var point = new google.maps.LatLng(stop.lat,stop.lon);
		var url = OBA.Resources.Map['SelectionCircle36.png'];
		var anchor = new google.maps.Point(18, 18);
		var icon = new google.maps.MarkerImage(url, null, null, anchor);
		new google.maps.Marker({position: point, map: map, icon: icon, clickable: false});
	};
	
	if( data.stopId ) {
		OBA.Api.stop(data.stopId, selectedStopHandler);
	}
};