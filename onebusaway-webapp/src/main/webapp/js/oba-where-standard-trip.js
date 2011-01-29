var oba_where_standard_trip = function(data) {
	
	var map = OBA.Maps.map(data.mapElement);
	
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
	
	var tripDetailsHandler = function(tripDetails) {
		if( tripDetails.trip) {
			var trip = tripDetails.trip;
			if( trip.shapeId )
				OBA.Api.shape(trip.shapeId, shapeHandler);
		}
	};
	
	var params = {};
	
	params.tripId = data.tripId;
	params.serviceDate = data.serviceDate;
	params.time = data.time;
	if( data.vehicleId )
		params.vehicleId = data.vehicleId;
	params.includeTrip = true;
	params.includeStatus = true;
	params.includeSchedule = true;
	
	OBA.Api.tripDetails(params, tripDetailsHandler);
	
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