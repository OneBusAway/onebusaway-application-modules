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

var blockLocation = {lat:null, lng:null};
var apiKey = getParameterByName("key", "TEST");
var smurl = "/onebusaway-api-webapp/siri/vehicle-monitoring?key=" + apiKey + "&type=json";
var rawUrlPre = "/onebusaway-api-webapp/api/where/vehicle-position-for-vehicle/";
var rawUrlPost = ".json?key=" + apiKey + "&version=2";
var vehicleId = getParameterByName("vehicleId", "1");
var agencyId = vehicleId.split("_")[0];
var vehicleStr = vehicleId.split("_")[1];
// drop the map marker arbitrarily
var errorLatLng = new google.maps.LatLng(38.905216, -77.06301);
var age = -999;
var deviation = -999;
var block = "";
var trip = "";
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
function showLegend(map) {
    var div = document.createElement('div');
    console.log("showing legend with " + Math.round(age * 100) / 100 + ", " + deviation);
    div.innerHTML = '<b>Age: </b> ' + Math.round(age * 100) / 100 + 's<br/>';
    div.innerHTML += '<b>Deviation: </b> ' + deviation + 'min<br/>'
	div.innerHTML += '<b>Block: </b> ' + block + '<br/>';
    div.innerHTML += 'Trip: ' + trip + '<br/>&nbsp;&nbsp;';
    var legend = document.getElementById('legend');
    legend.appendChild(div);

    map.controls[google.maps.ControlPosition.RIGHT_TOP].push(legend);
}

function initialize() {
	console.log("looking for vehicleId=" + vehicleId);
	map = new google.maps.Map(document.getElementById("map"), {
		zoom: 16,
	});


	
   var smParams = { OperatorRef: agencyId, VehicleRef: vehicleStr, MaximumNumberOfCallsOnwards: "1", VehicleMonitoringDetailLevel: "calls" }
   console.log("smurl=" + smurl);
	jQuery.getJSON(smurl, smParams, function(r) {
		var activity = r.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity[0];
		if(typeof(activity) == "undefined" || activity === null || activity.MonitoredVehicleJourney === null) {
			console.log("no activity for vehicle " + vehicleStr);
			var infoWindow = new google.maps.InfoWindow(
				{
					content: "unable to find SIRI vehicle " + vehicleStr,
					position: errorLatLng
				});
			infoWindow.open(map);
			return null;
		} else {
            console.log("have activity for vehicle " + vehicleStr);
            var latitude = activity.MonitoredVehicleJourney.VehicleLocation.Latitude;
            var longitude = activity.MonitoredVehicleJourney.VehicleLocation.Longitude;
            var extensions = activity.MonitoredVehicleJourney.MonitoredCall.Extensions;
            if (typeof(extensions) !== "undefined") {
                deviation = extensions.Deviation;
                console.log("deviation=" + deviation);
            }
            block = activity.MonitoredVehicleJourney.BlockRef;
            trip = activity.MonitoredVehicleJourney.FramedVehicleJourneyRef.DatedVehicleJourneyRef;
            showLegend(map);
            blockLocation.lat = latitude;
            blockLocation.lng = longitude;
            console.log("lat/lng set");
            map.panTo(blockLocation);
            var markerBlockLocation = new google.maps.Marker({
                position: blockLocation,
                map: map,
                title: 'Vehicle Position'
            });
        }
	});

    var gtfsrUrl = rawUrlPre + vehicleId + rawUrlPost;
    console.log("making raw call = " + gtfsrUrl);
   jQuery.getJSON(gtfsrUrl, {}, function(data) {
	   var code = data.code;
	   if (code == null || code == 404 || typeof data.data == "undefined") {
		   console.log("no raw position");
           var infoWindow = new google.maps.InfoWindow(
           	{
				content: "unable to find raw position for vehicle " + vehicleStr,
				position: errorLatLng
           	});
           infoWindow.open(map);

           return;
	   }
	   console.log("have raw data= " + data.data);
	   lat = data.data.currentLocation.lat;
	   lng = data.data.currentLocation.lon;
	   console.log("lat=" + lat + ", lon=" + lng);
	   age = (new Date() / 1000) - data.data.timeOfLocationUpdate;
	   console.log("record is " + age + "s old");
       var gtfsrLocation = {lat: parseFloat(lat), lng: parseFloat(lng)};
       var markerGtfsr = new google.maps.Marker({
           icon: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
           position: gtfsrLocation,
           map: map,
           title: 'GTFS-R!'
       });

       map.panTo(gtfsrLocation);

	});

}	

	google.maps.event.addDomListener(window, 'load', initialize);

 