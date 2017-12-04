/*
 * Copyright (c) 2011 Metropolitan Transportation Authority
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

var vehicleIconFilePrefix = "vehicle";
var vehicleIconFileType = "png";
	
jQuery(document).ready(function() { createMaps(); });

function loadCreateMaps() {
	window.setTimeout(createMaps, 4000);
}

function createMaps() {
	
	// content in the position div is parsed and passed to gmaps
	jQuery(".position").each(function(_, el) {
	el = jQuery(el);

	var data = el.text().split(',');
	
	var contents = el.html();
	if(contents === null) {
		return null;
	}
	
	var lat, lng = null;
	try {
		lat = parseFloat(data[0]);
		lng = parseFloat(data[1]);

		if (isNaN(lat) || isNaN(lng)) {
			return;
		}
	} catch (e) {
		//alert("could not parse lat/lon");
		return;
	}

	var orientation = null;
	try {
		orientation = Math.floor(data[2] / 5) * 5;

		if (isNaN(orientation)) {
			orientation = "unknown";
		};
	} catch (e) {
		orientation = "unknown";
	}
	var mapDivWrapper = jQuery("<div></div>").addClass("map-location-wrapper");

	var mapContainer = jQuery("<div></div>").addClass("map-location").appendTo(
			mapDivWrapper);

	// add appropriate bus icon on the map
	jQuery("<img></img>").addClass("marker").appendTo(mapDivWrapper).attr(
			"src",
			"../../css/img/vehicle/" + vehicleIconFilePrefix + '-' + orientation + '.'
					+ vehicleIconFileType);

	el.find("p").hide(); // hide the raw text we just parsed
	el.append(mapDivWrapper);

	var options = {
		zoom : 15,
		mapTypeControl : false,
		streetViewControl : false,
		center : new google.maps.LatLng(lat, lng),
		mapTypeId : google.maps.MapTypeId.ROADMAP,
		disableDefaultUI : true,
		draggable : false,
		zoomControl : false,
		scrollwheel : false
	};
	
	try {
		new google.maps.Map(mapContainer.get(0), options);
	} catch (e) {
		//alert("call to google failed" + e);
		return null;
	}

	});
}
	