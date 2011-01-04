function oba_admin_problems_trip_problem_reports(data) {
	var mapElement = jQuery("#tripProblemReportMap");
	var map = OBA.Maps.map(mapElement);
	var infoWindow = new google.maps.InfoWindow();
	var bounds = new google.maps.LatLngBounds();
	
	if( data.tripProblemReport ) {
		
		var report = data.tripProblemReport;

		if( report.userLat && report.userLon) {
			var point = new google.maps.LatLng(report.userLat,report.userLon);
			var url = OBA.Resources.Map['PersonMarker.png'];
			var marker = new google.maps.Marker({position: point, map: map, icon: url});
			
			bounds.extend(point);
		}
		
		if( report.vehicleLat && report.vehicleLon) {
			var point = new google.maps.LatLng(report.vehicleLat,report.vehicleLon);
			var url = OBA.Resources.Map['MapIcon-Bus-22.png'];
			var marker = new google.maps.Marker({position: point, map: map, icon: url});
			
			bounds.extend(point);
		}
		
		if ( report.stop ) {
			var stop = report.stop;
			var point = new google.maps.LatLng(stop.lat,stop.lon);
			var marker = new google.maps.Marker({position: point, map: map});
			
			bounds.extend(point);
		}
	}
	
	if( data.vehicleLocationRecords ) {
		var records = data.vehicleLocationRecords;
		for( var i=0; i<records.length; i++) {
			
			var record = records[i];
			
			if( record.currentLocation ) {

				var p = record.currentLocation;
				var point = new google.maps.LatLng(p.lat,p.lon);
				var url = OBA.Resources.Map['MapIcon-Bus-14.png'];
				var m = new google.maps.Marker({position: point, map: map, icon: url});

				var handler = function(record,marker) {
					var d = new Date(record.timeOfRecord);
					return function() {						
					    infoWindow.setContent('t=' + d.toTimeString() );
						infoWindow.open(map,marker);
					};
				}(record,m);
				
				google.maps.event.addListener(m, 'click', handler);
				
				bounds.extend(point);
			}
		}
	}
	
	if( ! bounds.isEmpty() )
		map.fitBounds(bounds);
};    