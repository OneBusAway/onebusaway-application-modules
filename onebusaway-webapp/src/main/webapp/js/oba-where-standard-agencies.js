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
function oba_where_standard_agencies() {
	
	var mapElement = jQuery("#agencies_map").get(0);
	var map = OBA.Maps.map(mapElement);
	var infoWindow = new google.maps.InfoWindow();
	var bounds = new google.maps.LatLngBounds();
	
	var handleAgencyWithCoverage = function(awc) {
		var agency = awc.agency;
		
		var point = new google.maps.LatLng(awc.lat,awc.lon);
		var markerOptions = {
		  position: point,
	      map: map
	    };

	    var marker = new google.maps.Marker(markerOptions);
	    
	    var p1 = new google.maps.LatLng(awc.lat + awc.latSpan / 2, awc.lon + awc.lonSpan / 2);
	    bounds.extend(p1);
	    var p2 = new google.maps.LatLng(awc.lat - awc.latSpan / 2, awc.lon - awc.lonSpan / 2);
	    bounds.extend(p2);
	    
	    var content = jQuery('.agencyInfoWindowTemplate').clone();
	    content.find("h3>a").text(agency.name);
	    content.find("h3>a").attr("href",agency.url);
	    
	    var mapUrl = "index.html#m(location)lat(" + awc.lat + ")lon(" + awc.lon + ")accuracy(4)";
	    content.find("p>a").attr("href",mapUrl);
	    
	    google.maps.event.addListener(marker, 'click', function() {
	      infoWindow.setContent(content.show().get(0));
	      infoWindow.open(map,marker);
	    });
	};

	OBA.Api.agenciesWithCoverage(function(entry) {

		jQuery.each(entry.list, function() {
			handleAgencyWithCoverage(this);
		});
		
		if( ! bounds.isEmpty() )
			map.fitBounds(bounds);
	});
};
    