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
var expandAlerts = false;

var siriVMRequestsByRouteId = {};
var blockLocation = {lat:null, lng:null};
var smurl = "/onebusaway-api-webapp/siri/vehicle-monitoring?key=OBA&type=json";
var gtfsrUrl = "/onebusaway-enterprise-acta-webapp/api/gtfsrt-proxy";
var vehicleId = getParameterByName("vehicleId", "2233");

var map;

function getParameterByName(name, defaultValue) {
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(window.location.href);
	if(results === null) {
		return defaultValue;
	} else {
		return decodeURIComponent(results[1].replace(/\+/g, " "));
	}
}

function initialize() {
	console.log("looking for vehicleId=" + vehicleId);
	map = new google.maps.Map(document.getElementById("map"), {
		zoom: 16,
	});

	
   var smParams = { OperatorRef: "USF BullRunner", VehicleRef: vehicleId, MaximumNumberOfCallsOnwards: "1", VehicleMonitoringDetailLevel: "calls" } 
	jQuery.getJSON(smurl, smParams, function(r) {
		var activity = r.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity[0];
		if(typeof(activity) == "undefined" || activity === null || activity.MonitoredVehicleJourney === null) {
			console.log("no activity");
			return null;
		}
		console.log("have activity");
		var latitude = activity.MonitoredVehicleJourney.VehicleLocation.Latitude;
		var longitude = activity.MonitoredVehicleJourney.VehicleLocation.Longitude;
		blockLocation.lat = latitude;
		blockLocation.lng = longitude;
		console.log("lat/lng set");
		map.panTo(blockLocation);
		var markerBlockLocation = new google.maps.Marker({
		    position: blockLocation,
		    map: map,
		    title: 'Vehicle Position'
		  });

	});
	
   
   jQuery.getJSON(gtfsrUrl, {}, function(data) {
//	   console.log("gtfsrt data=" + data);
	   var entities = data.results.split(/entity/);
	   if (entities == null || entities.length == 0) {
		   console.log("no entities");
		   return;
	   }
	   jQuery.each(entities, function(i, entity) {
		   if (entity.indexOf("id: \"" + vehicleId + "\"") > -1) {
//			   console.log("found vehicle " + vehicleId + " in entity=" + entity);
			   var latStart = entity.indexOf("latitude:");
			   var lngStart = entity.indexOf("longitude:");
			   if (latStart > 0 && lngStart > 0) {
				   var latStop = entity.indexOf("\n", latStart);
				   var lngStop = entity.indexOf("\n", lngStart);
//				   console.log("lat " + latStart + "->" + latStop);
//				   console.log("lon " + lngStart + "->" + lngStop);
			   }
			   if (latStop > 0 && lngStop > 0) {
				   lat = entity.substring(latStart+"latitude: ".length, latStop);
				   lng = entity.substring(lngStart+"longitude: ".length, lngStop);
//				   console.log("lat=" + lat + ", lng=" + lng);
				   var gtfsrLocation = {lat: parseFloat(lat), lng: parseFloat(lng)};
					var markerGtfsr = new google.maps.Marker({
						icon: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
					    position: gtfsrLocation,
					    map: map,
					    title: 'GTFS-R!'
					  });
					
					map.panTo(gtfsrLocation);
			   } else {
//				   console.log("missing latStop")
			   }
		   } else {
			   // missing vehicle
//			   console.log("skipping");
			   }
		   });
});
   
   
	
//	if (blockLocation.lat != null) {
//		console.log("valid lat");
//		var markerBlockLocation = new google.maps.Marker({
//		    position: blockLocation,
//		    map: map,
//		    title: 'Hello World!'
//		  });
//		
//	} else {
//		console.log("invalid lat");
//	}
	
}	

	google.maps.event.addDomListener(window, 'load', initialize);

 