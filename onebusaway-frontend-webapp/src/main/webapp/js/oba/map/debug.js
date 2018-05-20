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
var lengendInit = false;
var markerGtfsr;
var markerBlockLocation;
var map;
var refreshSeconds = getParameterByName("refresh", "5");

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

function createLegend(map) {
    var legend = document.getElementById('legend');
    if (typeof(legend) == "undefined" || legend == null || typeof(legend.style.display) == "undefined") {
        console.log("delaying legend init");
    } else {
        console.log("legend init successful");
        legend.style.display = "";
        map.controls[google.maps.ControlPosition.RIGHT_TOP].push(legend);
        lengendInit = true;
    }

}

function round(num) {
    return Math.round(num * 100) / 100;
}

function distance(lat1, lon1, lat2, lon2)
{
    var R = 6371; // km
    var dLat = toRad(lat2-lat1);
    var dLon = toRad(lon2-lon1);
    var lat1 = toRad(lat1);
    var lat2 = toRad(lat2);

    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c;
    return round(d * 1000);
}
//-- Define middle point function
function middlePoint(lat1, lng1, lat2, lng2) {

    //-- Longitude difference
    var dLng = (toRad(lng2 - lng1))

    //-- Convert to radians
    lat1 = toRad(lat1);
    lat2 = toRad(lat2);
    lng1 = toRad(lng1);

    var bX = Math.cos(lat2) * Math.cos(dLng);
    var bY = Math.cos(lat2) * Math.sin(dLng);
    var lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + bX) * (Math.cos(lat1) + bX) + bY * bY));
    var lng3 = lng1 + Math.atan2(bY, Math.cos(lat1) + bX);

    //-- Return result
    return [toDeg(lat3), toDeg(lng3),];
// Converts numeric degrees to radians
}

function toRad(Value)
{
    return Value * Math.PI / 180;
}
function toDeg(Value)
{
    return Value * (180 / Math.PI);
}
function update() {

    var smParams = { OperatorRef: agencyId, VehicleRef: vehicleStr, MaximumNumberOfCallsOnwards: "1", VehicleMonitoringDetailLevel: "calls" }
    console.log("smurl=" + smurl);
    jQuery.getJSON(smurl, smParams, function(r) {
        var activity = r.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity[0];
        if(typeof(activity) == "undefined" || activity === null || activity.MonitoredVehicleJourney === null) {
            console.log("no activity for vehicle " + vehicleStr);
            // var infoWindow = new google.maps.InfoWindow(
            //     {
            //         content: "unable to find SIRI vehicle " + vehicleStr,
            //         position: errorLatLng
            //     });
            // infoWindow.open(map);
            return null;
        } else {
            console.log("have activity for vehicle " + vehicleStr);
            var latitude = activity.MonitoredVehicleJourney.VehicleLocation.Latitude;
            var longitude = activity.MonitoredVehicleJourney.VehicleLocation.Longitude;
            var extensions = activity.MonitoredVehicleJourney.MonitoredCall.Extensions;
            if (typeof(extensions) !== "undefined") {
                document.getElementById("deviation").innerHTML = extensions.Deviation;
                console.log("deviation=" + deviation.value);
            }
            document.getElementById("block").innerHTML = activity.MonitoredVehicleJourney.BlockRef;
            document.getElementById("trip").innerHTML = activity.MonitoredVehicleJourney.FramedVehicleJourneyRef.DatedVehicleJourneyRef;

            blockLocation.lat = latitude;
            blockLocation.lng = longitude;
            console.log("lat/lng set");
            updatePan();
            if (markerBlockLocation == null) {
                markerBlockLocation = new google.maps.Marker({
                    position: blockLocation,
                    map: map,
                    title: 'Vehicle Position'
                });
            } else {
                markerBlockLocation.setPosition(blockLocation);
            }
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
        console.log("have raw data= " + data.data.timeOfLocationUpdate);
        lat = data.data.currentLocation.lat;
        lng = data.data.currentLocation.lon;
        console.log("lat=" + lat + ", lon=" + lng);
        document.getElementById("age").innerHTML = round((new Date() / 1000) - data.data.timeOfLocationUpdate);
        var dateStr = new Date(data.data.timeOfLocationUpdate * 1000).toString();
        document.getElementById("timestamp").innerHTML = dateStr.substring(0, dateStr.length-14).split(" ")[4];
        console.log("record is " + document.getElementById("age").innerHTML + "s old");
        var gtfsrLocation = {lat: parseFloat(lat), lng: parseFloat(lng)};
        if (markerGtfsr == null) {
            markerGtfsr = new google.maps.Marker({
                icon: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
                position: gtfsrLocation,
                map: map,
                title: 'GTFS-R!'
            });
        } else {
            markerGtfsr.setPosition(gtfsrLocation);
        }
        updatePan();

    });
    if (lengendInit == false) {
        createLegend(map);
    }
    updateDistance();

    setTimeout(function() {
        update();
    }, (refreshSeconds * 1000));

}

function updatePan() {
    if (typeof(markerGtfsr) !== "undefined"
        && typeof(markerBlockLocation) !== "undefined"
        &&  typeof(markerGtfsr.getPosition().lat()) !== "undefined"
        && typeof(markerBlockLocation.getPosition().lat() !== "undefined")) {
        console.log("case 1: both");
        var mp = middlePoint(markerGtfsr.getPosition().lat(),
            markerGtfsr.getPosition().lng(),
            markerBlockLocation.getPosition().lat(),
            markerBlockLocation.getPosition().lng());
        console.log("middelpoint " + mp[0] + "," + mp[1] + " from "
            + markerGtfsr.getPosition().lat() + ","
        + markerGtfsr.getPosition().lng() + " and "
        + markerBlockLocation.getPosition().lat() + ","
        + markerBlockLocation.getPosition().lng());

        map.panTo({lat: parseFloat(mp[0]), lng: parseFloat(mp[1])});
    } else if (typeof(markerGtfsr) !== "undefined") {
        console.log("case 2: raw");
        map.panTo({lat: markerGtfsr.getPosition().lat(), lng: markerGtfsr.getPosition().lng()});
    } else if (typeof(markerBlockLocation) !== "undefined") {
        console.log("case 3: block");
        map.panTo({lat: markerBlockLocation.getPosition().lat(), lng: markerBlockLocation.getPosition().lng()});
    } else {
        console.log("case 4: error");
        map.panTo({lat: errorLatLng.lat(), lng: errorLatLng.lng()});
    }
}
function updateDistance() {
    if (typeof(markerGtfsr) !== "undefined"
        && typeof(markerBlockLocation) !== "undefined"
        &&  typeof(markerGtfsr.getPosition().lat()) !== "undefined"
        && typeof(markerBlockLocation.getPosition().lat() !== "undefined")) {
        var delta = distance(markerGtfsr.getPosition().lat(),
            markerGtfsr.getPosition().lng(),
            markerBlockLocation.getPosition().lat(),
            markerBlockLocation.getPosition().lng());
        document.getElementById("distance").innerHTML = delta;
        if (delta > 1000) {
            map.setZoom(14);
        } else {
            map.setZoom(16);
        }

    } else {
        console.log("skipping distance");
    }
}
function initialize() {
    console.log("looking for vehicleId=" + vehicleId);
    map = new google.maps.Map(document.getElementById("map"), {
        zoom: 16,
    });

    update();
    setTimeout(function() {
        update();
    }, (refreshSeconds * 1000));
};

    createLegend(map);
	google.maps.event.addDomListener(window, 'load', initialize);


 