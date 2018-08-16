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
var shapeUrlPre = "/onebusaway-api-webapp/api/where/shape/";
var shapeUrlPost = ".json?key=" + apiKey + "&version=2";
var stopUrlPre = "/onebusaway-api-webapp/api/where/stop/";
var stopUrlPost = ".json?key=" + apiKey + "&version=2";


var vehicleId = getParameterByName("vehicleId", "1");
var agencyId = vehicleId.split("_")[0];
var vehicleStr = vehicleId.split("_")[1];
// drop the map marker arbitrarily
var errorLatLng = new google.maps.LatLng(38.905216, -77.06301);
var lengendInit = false;
var markerGtfsr;
var markerBlockLocation;
var shape;
var markerStop;
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
            document.getElementById("block").innerHTML = "unavailable";
            document.getElementById("trip").innerHTML = "unavailable";
            document.getElementById("nextStopId").innerHTML = "unavailable";
            document.getElementById("nextStop").innerHTML = "unavailable";
            document.getElementById("scheduled").innerHTML = "unavailable";
            document.getElementById("predicted").innerHTML = "unavailable";
            document.getElementById("deviation").innerHTML = "unavailable";
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
            document.getElementById("nextStopId").innerHTML = activity.MonitoredVehicleJourney.MonitoredCall.StopPointRef;
            document.getElementById("nextStop").innerHTML = activity.MonitoredVehicleJourney.MonitoredCall.StopPointName;
            if (typeof(activity.MonitoredVehicleJourney.MonitoredCall.AimedArrivalTime) !== "undefined") {
                console.log("aimed=" + activity.MonitoredVehicleJourney.MonitoredCall.AimedArrivalTime);
                document.getElementById("scheduled").innerHTML
                    = activity.MonitoredVehicleJourney.MonitoredCall.AimedArrivalTime.split("T")[1].split("-")[0].split(".")[0];
            }   else {
                document.getElementById("scheduled").innerHTML = "unavailable";
            }
            if (typeof(activity.MonitoredVehicleJourney.MonitoredCall.AimedArrivalTime) !== "undefined") {
                document.getElementById("predicted").innerHTML
                    = activity.MonitoredVehicleJourney.MonitoredCall.ExpectedArrivalTime.split("T")[1].split("-")[0].split(".")[0];
            } else {
                document.getElementById("predicted").innerHTML = "unavailable";
            }
            updateShape(activity.MonitoredVehicleJourney.JourneyPatternRef);
            updateStop(activity.MonitoredVehicleJourney.MonitoredCall.StopPointRef);


            blockLocation.lat = latitude;
            blockLocation.lng = longitude;
            console.log("lat/lng set");

            if (markerBlockLocation == null) {
                markerBlockLocation = new google.maps.Marker({
                    position: blockLocation,
                    map: map,
                    title: 'Calculated Position: ' + vehicleId
                });
            } else {
                markerBlockLocation.setPosition(blockLocation);
            }
        }
    });

    var gtfsUrl;
    if (typeof(OBA.Config) == "undefined" || typeof(OBA.Config.apiBaseUrl) == "undefined") {
        gtfsrUrl = rawUrlPre + vehicleId + rawUrlPost;
    } else {
        gtfsrUrl = OBA.Config.apiBaseUrl + rawUrlPre + vehicleId + rawUrlPost;
    }
    console.log("making raw call = " + gtfsrUrl);

    jQuery.getJSON(gtfsrUrl, {}, function(data) {
        var code = data.code;
        if (code == null || code == 404 || typeof data.data == "undefined") {
            console.log("no raw position");
            document.getElementById("age").innerHTML = "unavailable";
            document.getElementById("timestamp").innerHTML = "unavailable";
            return null;
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
                icon: 'img/icons/green-dot.png',
                position: gtfsrLocation,
                map: map,
                title: 'Raw Position: ' + vehicleId
            });
        } else {
            markerGtfsr.setPosition(gtfsrLocation);
        }


    });
    if (lengendInit == false) {
        createLegend(map);
    }
    updateDistance();
    updatePan();
    setTimeout(function() {
        update();
    }, (refreshSeconds * 1000));

}

function updateShape(shapeId) {
    var shapeUrl = shapeUrlPre + shapeId + shapeUrlPost;
    console.log("making shape call = " + shapeUrl);
    jQuery.getJSON(shapeUrl, {}, function(data) {
        var encodedShape = data.data.entry.points;

        if (shape != null) {
            shape.setMap(null);
        }
        var points = OBA.Util.decodePolyline(encodedShape);

        var latlngs = jQuery.map(points, function(x) {
            return new google.maps.LatLng(x[0], x[1]);
        });

        var color = "999999";
        var options = {
            path: latlngs,
            strokeColor: "#" + color,
            strokeOpacity: 1.0,
            strokeWeight: 3,
            clickable: false,
            map: map,
            zIndex: 2
        };

        shape = new google.maps.Polyline(options);
        shape.setMap(map);

    });
}

function updateStop(stopId) {
    var stopUrl = stopUrlPre + stopId + stopUrlPost;
    console.log("making stop call = " + stopUrl);
    jQuery.getJSON(stopUrl, {}, function (data) {
        stoplat = data.data.entry.lat;
        stoplon = data.data.entry.lon;

        var stopLocation = {lat: parseFloat(stoplat), lng: parseFloat(stoplon)};
        if (markerStop == null) {
            markerStop = new google.maps.Marker({
                icon: 'img/realtime/stop/stop-unknown.png',
                position: stopLocation,
                map: map,
                title: 'Stop ' + stopId
            });
        } else {
            markerStop.setPosition(stopLocation);
        }

    });
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
        document.getElementById("distance").innerHTML = "unavailable";
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
	update();


 