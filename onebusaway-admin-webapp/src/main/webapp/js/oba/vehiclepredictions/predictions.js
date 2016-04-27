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

var maps = new Object();;
var transitimeWeb="gtfsrt.dev.wmata.obaweb.org:8080";
var obaWeb="app.dev.wmata.obaweb.org:8080";
var avlAttrs = new Object();
var obaAttrs = new Object();
var autoRefresh = false;
var obaAge = null;
var avlAge = null;
var forceBounds = false;

jQuery(function() {
	startup();
	setTimeout(refreshTimers, 1000);
});

function refresh() {
	doSearch();
}

function refreshAttrs() {
}

function refreshTimers() {
	updateAvlAge();
	updateObaAge();
	triggers();
	setTimeout(refreshTimers, 1000);
}

function triggers() {
	
	var avlNextStopPredStyle = styleForPrediction("#avl_nextstoppred");
	jQuery("#avl_nextstoppred").css('background-color', avlNextStopPredStyle);
	
	var obatdsNextStopPredStyle = styleForPrediction("#oba_tdsnextstoppred");
	jQuery("#oba_tdsnextstoppred").css('background-color', obatdsNextStopPredStyle);
	
	var obasiriNextStopPredStyle = styleForPrediction("#oba_sirinextstoppred");
	jQuery("#oba_sirinextstoppred").css('background-color', obasiriNextStopPredStyle);

}

function styleForPrediction(field) {
	var now = new Date();
	var fieldVal = jQuery(field).html();
	var fieldPred = new Date();
	fieldPred.setHours(fieldVal.split(":")[0]);
	fieldPred.setMinutes(fieldVal.split(":")[1]);
	fieldPred.setSeconds(fieldVal.split(":")[2]);

	//if prediction is in future that's great
	if (fieldPred.getTime() >= now.getTime()) {
		return "#33FFCC" // green == good
	} else if (fieldPred.getTime() < now.getTime()
			&& fieldPred.getTime() > now.getTime() - 90000) {
		// if prediction is within 90 seconds past that's ok
		return "yellow"; // yellow == warning
	} else {
	    // prediction is in past
		return "#FFCCCC"; // red == bad

	}
}

function startup() {
	
	// stuff to do on load
	jQuery("#display_vehicle").click(onSearchClick);
	jQuery("#autorefresh").click(onRefreshClick);
	jQuery("#advanced").click(onAdvancedClick);
	jQuery("#clear_map").click(onClearMapClick);
	jQuery("#transitimeWeb").val(transitimeWeb);
	jQuery("#obaWeb").val(obaWeb);
	
	jQuery('html').bind('keypress', function(e){
		if (e.keyCode == 13) {
			onSearchClick();
			return false;
		}
	})
}

function onClearMapClick() {
	jQuery.each(maps, function(index,value) {
		console.log("clearing layers for map " + index + ":" + value);
		if (value != null) {
			console.log("clearing map " + index);
			value.eachLayer(function(layer){
				value.removeLayer(layer);
			})
			// TODO refactor
			L.control.scale({metric: false}).addTo(value);
			L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
				attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
				maxZoom: 19
			}).addTo(value);

		} else {
			console.log("null markers for" + index);
		}
	});
}

function loadMap(latLng1, latLng2, mapName) {
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
	
	// if latLng2 is present, its transitime's guess at vehicle position
	if (latLng2 == null || latLng2 == undefined || latLng2.lat == 0 && latLng2.lng == 0) {
		// center map around single marker
		map.setView(latLng1, 17);
		var marker = L.marker(latLng1);
		map.addLayer(marker);
	} else if (latLng1 == null || latLng1 == undefined) {
		//console.log(mapName + " missing input");
	} else {
		console.log(mapName + " " + latLng1 + " vs " + latLng2);
		// show two marks and center between intermediate position
		var redIcon = L.Icon.extend({
			  options: {
			      iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png'
			  }
			});
		
		var marker1 = L.marker(latLng1);
		map.addLayer(marker1);

		var marker2 = L.marker(latLng2, {icon: new redIcon});
		map.addLayer(marker2);
		
		var group = new L.featureGroup([marker1, marker2]);
		// this doesn't work as well as it should so make it optional
		if (forceBounds == true) {
			var centerLat = (latLng1.lat + latLng2.lat) /2;
			var centerLon = (latLng1.lng + latLng2.lng) /2;
			map.setView(L.latLng(centerLat, centerLon), 17);
			map.fitBounds(group.getBounds());
		} else {
			map.setView(latLng1, 17);
		}
	}	
}

function loadAvlMap(latLng1, latLng2) {
	if (latLng2 != null && latLng2 != null) {
		loadMap(latLng1, latLng2, 'avlMap');
	} else if (latLng != null) {
		loadMap(latLng1, 'avlMap');
	}
}
function loadObaMap(latLng) {
	if (latLng != null) {
		loadMap(latLng, null, 'obaMap');
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

function onAdvancedClick() {
	if (jQuery("#advanced").is(":checked")) {
		jQuery("#transitimeWeb").show();
		jQuery("#obaWeb").show();
	} else {
		jQuery("#transitimeWeb").hide();
		jQuery("#obaWeb").hide();
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
	transitimeWeb = jQuery("#transitimeWeb").val();
	obaWeb = jQuery("#obaWeb").val();
	
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
			queryPredictionValues(attrs, vehicleId);
			queryFinalPrediction(attrs)
			avlAttrs = attrs;
		},
		fail: function() {
			jQuery("#avl_route").html("...");
			jQuery("#avl.schedDev").html("...");
			jQuery("#avl_block").html("...");
			jQuery("#avl_trip").html("...");
			setAvlNextStopId("...");
			
		}
	});
	
	var predictionLocationUrl = "http://" + transitimeWeb 
	+ "/api/v1/key/4b248c1b/agency/1/command/vehicleLocation?v=" + vehicleId
	+ "&format=json"
	jQuery.ajax({
		url: predictionLocationUrl,
		type: "GET",
		async: false,
		success: function(response) {
			avlAttrs["predictionLocation"] = L.latLng(response[0], response[1]);
			loadAvlMap(avlAttrs['latLng'], avlAttrs["predictionLocation"]);

		},
		fail: function() {
			loadAvlMap(avlAttrs['latLng'], null);
			console.log("no predictionLocation");
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
			obaAttrs = attrs;
		},
		fail: function() {
			jQuery("#oba_schdev").html("...");
			jQuery("#oba_tdsnextstopid").html("...");
			jQuery("#oba_tdsnextstoppred").html("...");
			jQuery("#oba_finalstopid").html("...");
			jQuery("#oba_tdsnexttripid").html("...");
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
			if (response.data != undefined && response.data.entry != undefined 
					&& response.data.entry.status != undefined) {
				var now = new Date(response.currentTime);	
				var e = response.data.entry;
				var s = e.status;
				attrs["schedDev"] = formatScheduleDeviation(s.scheduleDeviation);
				jQuery("#oba_schdev").html(formatScheduleDeviation(s.scheduleDeviation))
				attrs["tdsNextStopId"] = s.nextStop.split("_")[1];
				jQuery("#oba_tdsnextstopid").html(s.nextStop.split("_")[1].toString());
				attrs["tdsLastUpdate"] = new Date(s.lastUpdateTime);
				setObaAge(s.lastUpdateTime);
				attrs["tdsNextPrediction"] = new Date(now.getTime() + (s.nextStopTimeOffset * 1000));
				jQuery("#oba_tdsnextstoppred").html(formatTime(new Date(now.getTime() + (s.nextStopTimeOffset * 1000))));
			} else {
				jQuery("#oba_schdev").html("...");
				jQuery("#oba_tdsnextstopid").html("...");
				jQuery("#oba_tdsnextstoppred").html("...");
			}
		},
		fail: function() {
			jQuery("#oba_schdev").html("...");
			jQuery("#oba_tdsnextstopid").html("...");
			jQuery("#oba_tdsnextstoppred").html("...");
		}
	});	
}

function formatScheduleDeviation(s) {
	var r = s;
	if (s < 120 && s > -120) {
		r = s + " s";
	} else {
		r = Math.round(s/60.0*10)/10 + " min"
	}
	if (s == 0) {
		r = r + " (ontime)";
	} else if (s > 0) {
		r = r + " (late)";
	} else {
		r = r + " (early)";
	}
	return r;
}

function queryOBAFinalStop(attrs, agencyId, vehicleId) {
	var tripId = attrs["tripId"];
	if (tripId == undefined) {
		console.log("missing trip id");
		jQuery("#oba_finalstopid").html("...");
		jQuery("#oba_finalstoppred").html("...");
		jQuery("#oba_tdsnexttripid").html("...");
		return
	}
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
			var schedule = response.data.entry.schedule;
			var s = response.data.entry.schedule.stopTimes;
			attrs["tdsFinalStopId"] = s[s.length-1].stopId.split("_")[1];
			jQuery("#oba_finalstopid").html(s[s.length-1].stopId.split("_")[1]);
			
			if (schedule != undefined && schedule.nextTripId != undefined) {
				jQuery("#oba_tdsnexttripid").html(schedule.nextTripId.split("_")[1]);
			} else {
				jQuery("#oba_tdsnexttripid").html("end of block");
			}

			
			queryOBAFinalPrediction(attrs, agencyId, attrs["tdsFinalStopId"], vehicleId)
		},
		fail: function() {
			jQuery("#oba_finalstopid").html("...");
			jQuery("#oba_tdsnexttripid").html("...");
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
			jQuery("#oba_finalstoppred").html(formatTime(new Date(pred)));
		},
		fail: function() {
			jQuery("#oba_finalstoppred").html("...");
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
			if (v == undefined) {
				console.log("empty vehicleDetails");
				return;
			}
			attrs["routeId"] = v.routeId;
			jQuery("#avl_route").html(v.routeId);
			attrs["schedev"] = v.schAdhStr;
			jQuery("#avl_schdev").html(v.schAdhStr);
			attrs["blockAlpha"] = v.block;
			jQuery("#avl_block").html(v.block);
			jQuery("#oba_block").html(v.block);
			attrs["tripId"] = v.trip;
			jQuery("#avl_trip").html(v.trip);
			attrs["nextStopId"] = v.nextStopId;
			setAvlNextStopId(v.nextStopId);
		},
		fail: function() {
			jQuery("#avl_route").html("...");
			jQuery("#avl_schdev").html("...");
			jQuery("#avl_block").html("...");
			jQuery("#avl_trip").html("...");
			setAvlNextStopId("...");
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
								setAvlNextStopPred(formatTime(new Date(predvalue.time * 1000)));
								return;
							} 
						});
					});
				});
			},
			fail: function() {
				setAvlNextStopPred("...");
			}
		});
	} else {
		console.log("vehicleDetails did not return route/stop, returning");
		jQuery("#avl_route").html("...");
		jQuery("#avl_schdev").html("...");
		jQuery("#avl_block").html("...");
		jQuery("#avl_trip").html("...");
		setAvlNextStopId("...");
		setAvlNextStopPred("...");
		jQuery("#avl_finalstopid").html("...");
		jQuery("#avl_finalstoppred").html("...");
	}

}

function queryFinalPrediction(attrs) {
	var tripId = attrs["tripId"];
	if (tripId == undefined || tripId == null) {
		console.log("missing tripId");
		return;
	}
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
			jQuery("#avl_finalstopid").html(s[s.length-1].stopId);
		},
		fail: function() {
			jQuery("#avl_finalstopid").html("...");
		}
	});
	
	if (attrs["finalStopId"] == null) {
		jQuery("#avl_finalstoppred").html("...");
		return;
	}
	
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
							jQuery("#avl_finalstoppred").html(formatTime(new Date(pred.time * 1000)));
							return;
						}
					}
				}
			}
		},
		fail: function() {
			jQuery("#avl_finalstoppred").html("...");
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
				jQuery("#oba_timestamp").html(formatTime(new Date(sd.ResponseTimestamp)));
				attrs["latLng"] = latLng
				jQuery("#oba_latlon").html(mvj.VehicleLocation.Latitude + ", " + mvj.VehicleLocation.Longitude)
				attrs["routeId"] = mvj.LineRef.split("_")[1];
				jQuery("#oba_route").html(mvj.LineRef.split("_")[1])
				attrs["tripId"] = mvj.FramedVehicleJourneyRef.DatedVehicleJourneyRef.split("_")[1];
				jQuery("#oba_trip").html(mvj.FramedVehicleJourneyRef.DatedVehicleJourneyRef.split("_")[1])
				// BlockRef is missing
				//jQuery("#oba_block").html(mvj.BlockRef.split("_")[1]);
				if (mvj.MonitoredCall.StopPointRef != undefined) {
					attrs["siriNextStopId"] = mvj.MonitoredCall.StopPointRef.split("_")[1];
					jQuery("#oba_sirinextstopid").html(mvj.MonitoredCall.StopPointRef.split("_")[1]);
				} else {
					attrs["siriNextStopId"] = undefined;
					jQuery("#oba_sirinextstopid").html("...");
				}
				attrs["siriNextPrediction"] = mvj.MonitoredCall.ExpectedDepartureTime;
				jQuery("#oba_sirinextstoppred").html(formatTime(new Date(mvj.MonitoredCall.ExpectedDepartureTime)));
				attrs["siriMonitored"] = mvj.Monitored;
			} else {
				console.log("MonitoringVehicleJourney missing");
			}
		} else {
			console.log("VehiceActivity missing:" + siri.Siri.ServiceDelivery.VehicleMonitoringDelivery);
			jQuery("#oba_timestamp").html("...");
			jQuery("#oba_latlon").html("...");
			jQuery("#oba_route").html("...");
			jQuery("#oba_trip").html("...");
			jQuery("#oba_block").html("...");
			jQuery("#oba_schdev").html("...");
			jQuery("#oba_tdsnextstopid").html("...");
			jQuery("#oba_tdsnextstoppred").html("...");
			jQuery("#oba_sirinextstopid").html("...");
			jQuery("#oba_sirinextstoppred").html("...");
		}
	} else {
		jQuery("#oba_timestamp").html("...");
		jQuery("#oba_latlon").html("...");
		jQuery("#oba_route").html("...");
		jQuery("#oba_trip").html("...");
		jQuery("#oba_block").html("...");
		jQuery("#oba_schdev").html("...");
		jQuery("#oba_tdsnextstopid").html("...");
		jQuery("#oba_tdsnextstoppred").html("...");
		jQuery("#oba_sirinextstopid").html("...");
		jQuery("#oba_sirinextstoppred").html("...");
	}
	return attrs;
}

function formatDate(date) {
	var local = new Date(date);
    local.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return local.toJSON().slice(0, 10);
}

function formatTime(date) {
	if (date == null) return null;
	var local = new Date(date);
    local.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    if (local.toJSON() == null) return null; // invalid date
    return local.toJSON().slice(11, 19);
	
}

function parseAvlData(jsonData) {
	var attrs = new Object();
	// For each AVL report...
    var vehicle;
    
    if (jsonData.data.length == 0) {
    	console.log("no data: " + jsonData.toString());
    	return attrs;
    }
    
    for (var i=0; i<jsonData.data.length; ++i) {
    	var avl = jsonData.data[i];
		
    	// parse date string -> number
    	avl.timestamp = Date.parse(avl.time.replace(/-/g, '/').slice(0,-2))
    	setAvlAge(avl.timestamp);
    	jQuery("#avl_timestamp").html(formatTime(new Date(avl.timestamp)));
		// we only want the most recent
		latLng = L.latLng(avl.lat, avl.lon);
    	attrs['latLng'] = latLng;
    	jQuery("#avl_latlon").html(avl.lat + ", " + avl.lon);
    }
    return attrs;
}


function setObaAge(d) {
	obaAge = new Date(d).getTime();
	updateObaAge();
}

function setAvlAge(d) {
	avlAge = new Date(d).getTime();
	updateAvlAge();
}
function updateObaAge() {
	var time = Math.round((new Date().getTime() - obaAge)/1000);
	jQuery("#obaAge").html("<b>Age</b>: " + time + " s");
	jQuery("#delta_age").html(new Date(jQuery("#avl_timestamp").val()).getTime() - new Date(obaAttrs["tdsLastUpdate"]).getTime());
 
}

function updateAvlAge() {
	var time = Math.round((new Date().getTime() - avlAge)/1000);
	jQuery("#avlAge").html("<b>Age</b>: " + time + " s");
}

function setAvlNextStopId(val) {
	jQuery("#avl_nextstopid").html(val);
}
function setAvlNextStopPred(val) {
	jQuery("#avl_nextstoppred").html(val);
}


function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
	  var R = 6371000; // Radius of the earth in m
	  var dLat = deg2rad(lat2-lat1);  // deg2rad below
	  var dLon = deg2rad(lon2-lon1); 
	  var a = 
	    Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
	    Math.sin(dLon/2) * Math.sin(dLon/2)
	    ; 
	  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	  var d = R * c; // Distance in km
	  return d;
	}

	function deg2rad(deg) {
	  return deg * (Math.PI/180)
	}
