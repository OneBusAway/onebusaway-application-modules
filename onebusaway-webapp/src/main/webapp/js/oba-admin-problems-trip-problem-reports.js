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
function oba_admin_problems_trip_problem_reports(data) {
	
	var labelElement = jQuery("input#trip-problem-report_label");
	labelElement.autocomplete({
	    source: data.problemLabels
	});

	var mapElement = jQuery("#tripProblemReportMap");
	var map = OBA.Maps.map(mapElement);
	var infoWindow = new google.maps.InfoWindow();
	var bounds = new google.maps.LatLngBounds();
	
	if( data.tripProblemReport ) {
		
		var report = data.tripProblemReport;

		if( report.userLat && report.userLon) {
			var point = new google.maps.LatLng(report.userLat,report.userLon);
			var url = OBA.Resources.Map['PersonMarker.png'];
			new google.maps.Marker({position: point, map: map, icon: url});
			
			bounds.extend(point);
		}
		
		if( report.vehicleLat && report.vehicleLon) {
			var point = new google.maps.LatLng(report.vehicleLat,report.vehicleLon);
			var url = OBA.Resources.Map['MapIcon-Bus-22.png'];
			new google.maps.Marker({position: point, map: map, icon: url});
			
			bounds.extend(point);
		}
		
		if ( report.stop ) {
			var stop = report.stop;
			var point = new google.maps.LatLng(stop.lat,stop.lon);
			new google.maps.Marker({position: point, map: map});
			
			bounds.extend(point);
		}
	}
	
	if( data.vehicleLocationRecords ) {
		var records = data.vehicleLocationRecords;
		var rows = jQuery("#vehicleLocationRecordsTable tr");
		for( var i=0; i<records.length; i++) {
			var record = records[i];
			var row = rows.eq(i);
			
			if( record.currentLocation ) {

				var p = record.currentLocation;
				var point = new google.maps.LatLng(p.lat,p.lon);
				var url = OBA.Resources.Map['MapIcon-Bus-14.png'];
				var m = new google.maps.Marker({position: point, map: map, icon: url});

				var handler = function(record,marker) {
					var label = i + " @ " + row.find("td").eq(1).text(); 
					return function() {						
					    infoWindow.setContent(label);
						infoWindow.open(map,marker);
					};
				}(record,m);
				
				google.maps.event.addListener(m, 'click', handler);
				
				bounds.extend(point);

				add_highlight_location_on_hover(row, map, point);
			}
		}
	}
	
	if( ! bounds.isEmpty() )
		map.fitBounds(bounds);
};

function add_highlight_location_on_hover(el, map, p) {
	var selection_marker = null;
	var on_hover_in = function(obj) {
		on_hover_out();
		var icon_url = OBA.Resources.Map['SelectionCircle36.png'];
		var anchor = new google.maps.Point(18, 18);
		var icon = new google.maps.MarkerImage(icon_url, null, null, anchor);
		selection_marker = new google.maps.Marker({position: p, map: map, icon: icon, clickable: false});
	};
	var on_hover_out = function(obj) {
		if (selection_marker != null) {
			selection_marker.setMap(null);
			selection_marker = null;
		}
	};
	el.hover(on_hover_in, on_hover_out);

};