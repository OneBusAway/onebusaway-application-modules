/*
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
// Populate a select element with options from an endpoint
// elem - querystring or DOM element to operate on
// path - path of endpoint
function populateSelect(elem, path) {
	if (onchange) thunk()
	$.getJSON(contextPath + path, function(data) {
		var select = $(elem);
		select.append("<option disabled selected />")
		for (var i = 0; i < data.length; i++) {
			var option = $("<option />").attr("value", data[i]).text(data[i]);
			select.append(option);
		}
	})
}

// Draw shape points for a route on the map.
// data: object where data.lats is a list of latitudes and data.lons is a list of longitudes
function drawRoute(data) {
	routeGroup.clearLayers();
	
	var latLngs = [];
	for (var i = 0; i < data.size; i++) {
		
		var latLng = L.latLng(data.lats[i], data.lons[i]);
		latLngs.push(latLng);
	
		// These are not stops.
		// L.circleMarker(latLng, stopOptions).addTo(map);
	}
	
	map.fitBounds(latLngs);
	
	L.polyline(latLngs, routeOptions).addTo(routeGroup)
}

// stops: list of {lat, lng} objects
// draw each stop as a circle on the map
function drawStops(stops) {
	stops.forEach(function(stop) {
		L.circleMarker(stop, stopOptions).addTo(routeGroup);
	});	
}

// data is a list of AVL objects. Each has {lat, lon, timestamp} fields.
// draw AVL positions on the map.
function drawVehiclePositions(data) {

	// Since data is an array of objects each of which have a lat and lon
	// property, we can just go ahead and treat them as LatLngs.
	
	vehicleGroup.clearLayers();
	map.fitBounds(data);
	L.polyline(data, routePolylineOptions).addTo(vehicleGroup)
	
	data.forEach(function(avl) {
		
		avl.time = new Date(avl.timestamp).toTimeString();
		avl.latlon = avl.lat + ", " + avl.lon
		
		var avlMarker = L.rotatedMarker(avl, {
	          icon: L.divIcon({
	        	  className: 'avlMarker_',
	        	  html: "<div class='avlTriangle' />",
	        	  iconSize: [7,7]
	          }),
	          angle: avl.bearing, // this doesn't actually seem to be used
	          title: avl.time
	      }).addTo(vehicleGroup);
		
		
		/* Create popup AVL information */
		
		var labels = ["Vehicle", "GPS Time", "Lat/Lon", "Speed", "Heading"],
			keys = ["vehicleId", "time", "latlon", "speed", "bearing"];
		
		var content = $("<table />").attr("class", "popupTable");
		
		for (var i = 0; i < labels.length; i++) {
			
			var label = $("<td />").attr("class", "popupTableLabel").text(labels[i]);
			var value = $("<td />").text(avl[keys[i]]);
			content.append( $("<tr />").append(label, value) )
		}
  		
  		avlMarker.bindPopup(content[0]);
	})
}