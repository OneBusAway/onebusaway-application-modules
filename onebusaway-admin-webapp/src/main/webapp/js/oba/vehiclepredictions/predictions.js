/**
 * Copyright (c) 2016 Cambridge Systematics, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var maps = [];
var transitimeWeb="gtfsrt.dev.wmata.obaweb.org:8080";
var obaWeb="app.dev.wmata.obaweb.org"
var avlAttrs = new Object();
var obaAttrs = new Object();
var autoRefresh = false;

jQuery(function() {
	startup();
});

function refresh() {
	doSearch();
}

function refreshAttrs() {
	displayAttrs("#avlData", avlAttrs);
	displayAttrs("#obaData", obaAttrs);
}
function startup() {
	
	// stuff to do on load
	jQuery("#display_vehicle").click(onSearchClick);
	
	jQuery("#autorefresh").click(onRefreshClick);
}

function loadMap(latLng, mapName) {
	var map;
	if (maps[mapName] != undefined) {
		map = maps[mapName];
	} else {
		map = L.map(mapName);
		maps[mapName] = map;
		L.control.scale({metric: false}).addTo(map);
		L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
			attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
			maxZoom: 19
		}).addTo(map);
	}

	// center map around marker
	map.setView(latLng, 17);
	var marker = L.marker(latLng).addTo(map);
	
}

function loadAvlMap(latLng) {
	if (latLng != null) {
		loadMap(latLng, 'avlMap');
	}
}
function loadObaMap(latLng) {
	if (latLng != null) {
		loadMap(latLng, 'obaMap');
	}
}

function onRefreshClick() {
	if (jQuery("#autorefresh").is(":checked")) {
		autoRefresh = true;
		doSearch();
	} else {
		autoRefresh = false;
	}
}

function onSearchClick() {
	jQuery("#maps").show();
	doSearch();
}

function doSearch() {
	if (autoRefresh) {
		setTimeout(refresh, 15000);
	}
	setTimeout(refreshAttrs, 3000); // this is a hack for async calls below
	setTimeout(refreshAttrs, 5000);

	var vehicleId = jQuery("#vehicleId").val();
	var agencyId="1";
	var now = new Date();
	var oneMinuteAgo = new Date(now.getTime() - (1 * 60 * 1000));
	var beginDate=formatDate(oneMinuteAgo);
	var numDays=1;
	var beginTime=formatTime(oneMinuteAgo);
	var avlUrl= "http://" + transitimeWeb + "/web/reports/avlJsonData.jsp?a="
	+ agencyId + "&beginDate=" + beginDate + "&numDays=" + numDays + "&v=" + vehicleId + 
	"&beginTime=" + encodeURI(beginTime);
	jQuery.ajax({
		url: avlUrl,
		type: "GET",
		async: false,
		success: function(response) {
			var attrs = parseAvlData(response);
			loadAvlMap(attrs['latLng']);
			queryPredictionValues(attrs, vehicleId);
			queryFinalPrediction(attrs)
			displayAttrs("#avlData", attrs);
			avlAttrs = attrs;
		}
	});
	var obaUrl = "http://" + obaWeb + "/onebusaway-api-webapp/siri/vehicle-monitoring?key=OBAKEY&OperatorRef="
	+ agencyId + "&VehicleRef=" + vehicleId +  "&type=json";
	jQuery.ajax({
		url: obaUrl,
		jsonp: "callback",
		dataType: "jsonp",
		type: "GET",
		async: false,
		success: function(response) {
			var attrs = parseSiri(response)
			loadObaMap(attrs['latLng']);
			queryOBAApiValues(attrs, agencyId, vehicleId);
			queryOBAFinalStop(attrs, agencyId, vehicleId);
			displayAttrs("#obaData", attrs);
			obaAttrs = attrs;
		}
	});

}

function queryOBAApiValues(attrs, agencyId, vehicleId) {

	var apiUrl = "http://" + obaWeb 
	+ "/onebusaway-api-webapp/api/where/trip-for-vehicle/"
	+ agencyId + "_" + vehicleId + ".json?key=OBAKEY";

	jQuery.ajax({
		url: apiUrl,
		type: "GET",
		jsonp: "callback",
		dataType: "jsonp",
		async: false,
		success: function(response) {
			attrs["obaapicall"] = "false";
			
			if (response.data != undefined && response.data.entry != undefined 
					&& response.data.entry.status != undefined) {
				var now = new Date(response.currentTime);	
				var e = response.data.entry;
				var s = e.status;
				attrs["schedDev"] = (s.scheduleDeviation/60) + " min"
				  + (s.scheduleDeviation < 0? " (early)":" (late)" );
				attrs["tdsNextStopId"] = s.nextStop.split("_")[1];
				attrs["tdsLastUpdate"] = new Date(s.lastUpdateTime);
				attrs["tdsNextStopOffset"] = s.nextStopTimeOffset;
				attrs["tdsNextPrediction"] = new Date(now.getTime() + (s.nextStopTimeOffset * 1000));
				attrs["obaapicall"] = "true";
			}
		}
	});	
}

function queryOBAFinalStop(attrs, agencyId, vehicleId) {
	var tripId = attrs["tripId"];
	var scheduleUrl = "http://" + obaWeb
		+ "/onebusaway-api-webapp/api/where/trip-details/"
		+ agencyId + "_" + tripId + ".json?key=OBAKEY";
	
	jQuery.ajax({
		url: scheduleUrl,
		type: "GET",
		jsonp: "callback",
		dataType: "jsonp",
		async: false,
		success: function(response) {
			var s = response.data.entry.schedule.stopTimes;
			attrs["tdsFinalStopId"] = s[s.length-1].stopId.split("_")[1];
			queryOBAFinalPrediction(attrs, agencyId, attrs["tdsFinalStopId"], vehicleId)
		}
	});	
}

function queryOBAFinalPrediction(attrs, agencyId, stopId, vehicleId) {
	
	var tripId = attrs["tripId"];
	// Setting service date to today so will not work for trips that span a day.
	var serviceDate = new Date(); 
	serviceDate.setHours(0,0,0,0);
	var lastStopPredUrl = "http://" + obaWeb 
		+ "/onebusaway-api-webapp/api/where/arrival-and-departure-for-stop/"
		+ agencyId + "_" + stopId + ".json?key=OBAKEY&tripId="
		+ agencyId + "_" + tripId + "&vehicleId=" + agencyId + "_" + vehicleId 
		+ "&serviceDate=" + serviceDate.getTime();

	jQuery.ajax({
		url: lastStopPredUrl,
		type: "GET",
		jsonp: "callback",
		dataType: "jsonp",
		async: false,
		success: function(response) {
			var pred = response.data.entry.predictedArrivalTime;
			attrs["tdsFinalStopPred"] = new Date(pred);
		}
	});
	
}

function queryPredictionValues(attrs, vehicleId) {
	var vehicleDetailsUrl = "http://" + transitimeWeb 
	+ "/api/v1/key/4b248c1b/agency/1/command/vehiclesDetails?v=" 
	+ vehicleId + "&format=json";
	jQuery.ajax({
		url: vehicleDetailsUrl,
		type: "GET",
		async: false,
		success: function(response) {
			var v = response.vehicles[0];
			attrs["routeId"] = v.routeId;
			attrs["schedDev"] = v.schAdhStr;
			attrs["blockAlpha"] = v.block;
			attrs["tripId"] = v.trip;
			attrs["nextStopId"] = v.nextStopId;
		}
	});
	
	if (attrs["routeId"] != undefined && attrs["nextStopId"] != undefined) {
	
		var predictionUrl = "http://" + transitimeWeb
		+ "/api/v1/key/4b248c1b/agency/1/command/predictions?rs=" 
		+ attrs["routeId"] + "|" + attrs["nextStopId"] 
		+ "&numPreds=20&format=json";
		jQuery.ajax({
			url: predictionUrl,
			type: "GET",
			async: false,
			success: function(response) {
				jQuery.each(response.predictions, function( pindex, pvalue){
					jQuery.each(pvalue.dest, function( dindex, dvalue){
						jQuery.each(dvalue.pred, function( predindex, predvalue){
							if (predvalue.vehicle == vehicleId) {
								attrs["nextPrediction"] = new Date(predvalue.time * 1000);
								return;
							} else {
								console.log("skipping vehicle=" + predvalue.vehicle);
							}
						});
					});
				});
			}
		});
	} else {
		console.log("vehicleDetails did not return route/stop, returning")
	}

}

function queryFinalPrediction(attrs) {
	var tripId = attrs["tripId"];
	var scheduleUrl = "http://" + transitimeWeb 
		+ "/api/v1/key/4b248c1b/agency/1/command/trip?tripId=" 
		+ tripId + "&format=json";
	
	jQuery.ajax({
		url: scheduleUrl,
		type: "GET",
		async: false,
		success: function(response) {
			var s = response.schedule;
			attrs["finalStopId"] = s[s.length-1].stopId;
		}
	});
	
	if (attrs["finalStopId"] == null)
		return;
	
	var routeId = attrs["routeId"];
	var stopId = attrs["finalStopId"];
	var vehicleId = attrs["vehicleId"];
	var lastStopPredUrl = "http://" + transitimeWeb 
		+ "/api/v1/key/4b248c1b/agency/1/command/predictions?rs="
		+ routeId + "|" + stopId;
	jQuery.ajax({
		url: lastStopPredUrl,
		type: "GET",
		async: false,
		success: function(response) {
			for (var i = 0; i < response.predictions.length; i++) {
				var dests = response.predictions[i].dest;
				for (var j = 0; j < dests.length; j++) {
					var preds = dests[j].pred;
					for (var k = 0; k < preds.length; k++) {
						var pred = preds[k];
						if (pred.vehicleId == vehicleId) {
							attrs["finalStopPred"] = new Date(pred.time * 1000);
							return;
						}
					}
				}
			}
		}
	});
	
}


function parseSiri(siri) {
	var attrs = new Object();
	var latLng = null;
	var sd = siri.Siri.ServiceDelivery;
	if (sd != undefined) {
		var vmd = sd.VehicleMonitoringDelivery;
		var va = vmd[0].VehicleActivity;
		if (va != undefined && va.length > 0) {
			var mvj = va[0].MonitoredVehicleJourney;
			if (mvj != undefined) {
				latLng = L.latLng(mvj.VehicleLocation.Latitude, mvj.VehicleLocation.Longitude);
				attrs['pretty_timestamp'] = new Date(sd.ResponseTimestamp);
				attrs["latLng"] = latLng
				attrs["routeId"] = mvj.LineRef.split("_")[1];
				attrs["tripId"] = mvj.FramedVehicleJourneyRef.DatedVehicleJourneyRef.split("_")[1];
				attrs["siriNextStopId"] = mvj.MonitoredCall.StopPointRef.split("_")[1];
				attrs["siriNextPrediction"] = mvj.MonitoredCall.ExpectedArrivalTime;
				attrs["siriMonitored"] = mvj.Monitored;
			} else {
				console.log("MonitoringVehicleJourney missing");
			}
		} else {
			console.log("VehiceActivity missing:" + siri.Siri.ServiceDelivery.VehicleMonitoringDelivery);
		}
	}
	return attrs;
}

function formatDate(date) {
	var local = new Date(date);
    local.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return local.toJSON().slice(0, 10);
}

function formatTime(date) {
	var local = new Date(date);
    local.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return local.toJSON().slice(11, 19);
	
}

function parseAvlData(jsonData) {
	var attrs = new Object();
	// For each AVL report...
    var vehicle;
    
    if (jsonData.data.length == 0) {
    	console.log("no data: " + jsonData);
    }
    
    for (var i=0; i<jsonData.data.length; ++i) {
    	var avl = jsonData.data[i];
		
    	// parse date string -> number
    	avl.timestamp = Date.parse(avl.time.replace(/-/g, '/').slice(0,-2))
  	    attrs['timestamp'] = avl.timestamp;
    	attrs['pretty_timestamp'] = new Date(avl.timestamp);
		// we only want the most recent
		latLng = L.latLng(avl.lat, avl.lon);
    	attrs['latLng'] = latLng;
    }
    return attrs;
}



function displayAttrs(divName, attrs) {
	var html = "";
	var div = jQuery(divName);
	jQuery.each(attrs, function( index, value ) {
		html = html + "<p>" + lookupTitle(index) + ":&nbsp" + value + "</p>";
	});
	div.html(html);
	
}


function lookupTitle(s) {
	var txt;
    switch (s) {
    	case "timetamp":
    		txt = "Raw Timestamp";
    		break;
    	case "pretty_timestamp":
  		  txt = "Timestamp";
  		  break;
    	case "latLng":
    		txt = "Lat/Lon";
    		break;
	    default:
	      txt = s;
  }
  return "<b>" + txt + "</b> ";
}
