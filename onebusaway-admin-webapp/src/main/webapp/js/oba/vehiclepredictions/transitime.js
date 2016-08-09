/*
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
function createTransitimeModule(title, urlinput, timestamp, latlon, trip, route, block, schdev, nextstopid, 
		nextstoppred, finalstopid, finalstoppred, nexttripid, status, age, mapName) {

	var transitimeWeb = OBA.config.transitimeUrl || "gtfsrt.dev.wmata.obaweb.org:8080";
	urlinput.val(transitimeWeb);
	
	title.html("Transitime Position")
	
	// unavailable info
	nexttripid.html("N/A");

	var avlAge = null;
	
	var module = {};

	module.refresh = function(vehicleAgencyId, tripAgencyId, stopAgencyId, beginDate, numDays, vehicleId, beginTime) {
		var agencyId = stopAgencyId;
		transitimeWeb = urlinput.val();
		
		var avlAttrs = {}
		
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
				queryFinalPrediction(attrs);
				avlAttrs = attrs;
			},
			fail: function() {
				route.html("...");
				schdev.html("...");
				block.html("...");
				trip.html("...");
				nextstopid.html("...");
				
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
	}
	
	// helper functions
	
	function parseAvlData(jsonData) {
		var attrs = new Object();
		// For each AVL report...
	    var vehicle;
	    
	    if (jsonData.data.length == 0) {
	    	return attrs;
	    }
	    
	    for (var i=0; i<jsonData.data.length; ++i) {
	    	var avl = jsonData.data[i];
			
	    	// parse date string -> number
	    	avl.timestamp = Date.parse(avl.time.replace(/-/g, '/').slice(0,-2))
	    	setAge(avl.timestamp);
	    	timestamp.html(formatTime(new Date(avl.timestamp)));
			// we only want the most recent
			latLngLeaflet = L.latLng(avl.lat, avl.lon);
	    	attrs['latLng'] = latLngLeaflet;
	    	latlon.html(avl.lat + ", " + avl.lon);
	    }
	    return attrs;
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
				route.html(v.routeId);
				attrs["schedev"] = v.schAdhStr;
				schdev.html(v.schAdhStr);
				attrs["blockAlpha"] = v.block;
				block.html(v.block);
				attrs["tripId"] = v.trip;
				trip.html(v.trip);
				attrs["nextStopId"] = v.nextStopId;
				nextstopid.html(v.nextStopId);
			},
			fail: function() {
				route.html("...");
				schdev.html("...");
				block.html("...");
				trip.html("...");
				nextstopid.html("...");
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
									nextstoppred.html(formatTime(new Date(predvalue.time * 1000)));
									return;
								} 
							});
						});
					});
				},
				fail: function() {
					nextstoppred.html("...");
				}
			});
		} else {
			console.log("vehicleDetails did not return route/stop, returning");
			route.html("...");
			schdev.html("...");
			block.html("...");
			trip.html("...");
			nextstopid.html("...");
			nextstoppred.html("...");
			finalstopid.html("...");
			finalstoppred.html("...");
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
				finalstopid.html(s[s.length-1].stopId);
			},
			fail: function() {
				finalstopid.html("...");
			}
		});
		
		if (attrs["finalStopId"] == null) {
			finalstoppred.html("...");
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
								finalstoppred.html(formatTime(new Date(pred.time * 1000)));
								return;
							}
						}
					}
				}
			},
			fail: function() {
				finalstoppred.html("...");
			}
		});
		
	}
	
	function loadAvlMap(latLng1, latLng2) {
		if (latLng2 != null && latLng2 != null) {
			loadMap(latLng1, latLng2, mapName);
		} else if (latLng != null) {
			loadMap(latLng1, mapName);
		}
	}
	
	function setAge(d) {
		avlAge = new Date(d).getTime();
		module.updateAge();
	}
	
	module.updateAge = function() {
		updateAge(age, avlAge)
	}
	
	return module;
}

