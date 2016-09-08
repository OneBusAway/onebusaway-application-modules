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
function createGtfsRtModule(title, urlinput, timestamp, latlon, tripVal, route, block, schdev, nextstopid, 
		nextstoppred, finalstopid, finalstoppred, nexttripid, status, age, mapName) {

	// This actually looks at GTFS-RT model classes proxied through from admin-webapp
	
	urlinput.val("unused");
	
	title.html("GTFS-RT")
	
	// Unavailable info
	block.html("N/A");
	
	var avlAge = null;
	
	var module = {};

	module.refresh = function(vehicleAgencyId, tripAgencyId, stopAgencyId, beginDate, numDays, vehicleId, beginTime) {
	
		status.html("loading...");
		setTimeout(checkRefresh, 4000);
		
		var gtfsrtUrl = OBA.config.gtfsRtUrl + "?source=" + createGtfsRtModule.source;
		
		jQuery.ajax({
			url: gtfsrtUrl,
			type: "GET",
			dataType: "json",
			success: function(response) {
				processTripUpdates(response.tripUpdates, vehicleId);
				processVehiclePositions(response.vehiclePositions, vehicleId);
			},
			fail: fail
		})
		
	}
	
	function fail() {
		route.html("...");
		console.log("trip=" + trip);
		if (trip == undefined || trip == null || trip.html == undefined) {
			status.html("could not determine trip for vehicle " + vehicleId);
		} else 	if (trip != undefined && trip != null && trip.html != undefined)
			trip.html("...");
		nextstopid.html("...");	
	}
	
	function checkRefresh() {
		if (status.html() == "loading...") {
			status.html("connection issue with GTFS RT");
		}
	}
	
	function processVehiclePositions(vehiclePositions, vehicleId) {
		for (var i = 0; i < vehiclePositions.length; i++) {
			var vp = vehiclePositions[i];
			
			if (vehicleId == vp.vehicleId) {
				
				latlon.html(vp.lat + ", " + vp.lon);
				var latLng = L.latLng(vp.lat, vp.lon);
				status.html("Update Complete");
				setTimeout(loadGtfsRtMap, 1000, latLng);
				
				status.html("Update complete.");
				return;
			}
		}
		console.log("Could not find vehicle in vehicle positions: " + vehicleId)
		status.html("Updates Complete.");
	}
	
	function processTripUpdates(tripUpdates, vehicleId) {
		formatTripUpdates(tripUpdates);

		var stus = [];
		
		var latestTime = new Date(tripUpdates[0].timestamp)
		
		tripUpdates.forEach(function(tu) {
			if (tu.vehicleId == vehicleId) {
				stus = stus.concat(tu.stopTimeUpdates);
			}
			var time = new Date(tu.timestamp);
			if (time > latestTime) {
				latestTime = time;
			}
		})
		
		setAge(latestTime);
		
		if (stus.length == 0) {
			console.log("could not find vehicle in trip update: " + vehicleId);
			return;
		}
		
		stus.sort(function(a, b) {
			return a.time - b.time;
		})
		
		for (var i = 1; i < stus.length; i++) {
			stus[i-1].next = stus[i];
		}
		
		var stopTimeUpdate = findBestStop(stus, latestTime); 
	
		var tripUpdate = stopTimeUpdate.tripUpdate;
		
		timestamp.html(formatTime(latestTime));
		
		tripVal.html(tripUpdate.tripId);
		route.html(tripUpdate.routeId);
		nextstopid.html(stopTimeUpdate.stopId);
		nextstoppred.html(formatTime(stopTimeUpdate.time));
		
		var finalStopTime = tripUpdate.stopTimeUpdates[tripUpdate.stopTimeUpdates.length - 1];
		finalstopid.html(finalStopTime.stopId);
		finalstoppred.html(formatTime(finalStopTime.time));
		
		nextTripId = findNextTripId(stopTimeUpdate) || "N/A";
		nexttripid.html(nextTripId);
		
		if (tripUpdate.delay) {
			schdev.html(formatScheduleDeviation(tripUpdate.delay));
		} else {
			schdev.html("N/A");
		}
		
		status.html("Trip updates complete.")
	}
	
	function findNextTripId(stopTimeUpdate) {
		var tripId = stopTimeUpdate.tripUpdate.tripId;
		
		for (var s = stopTimeUpdate.next; s != undefined; s = s.next) {
			if (s.tripUpdate.tripId != tripId) {
				return s.tripUpdate.tripId;
			}
		}
	}
	
	// Make sure response is sorted
	function formatTripUpdates(tripUpdates) {
		tripUpdates.forEach(function(tu) {
			tu.stopTimeUpdates.forEach(function(stu) {
				stu.tripUpdate = tu;
				var time = stu.departureTime != null ? stu.departureTime : stu.arrivalTime;
				stu.time = new Date(time);
			})
		});
	}


	// find first stop update after timestamp.
	// stopUpdates must be sorted
	function findBestStop(stopUpdates, timestamp) {
		for (var i = 0; i < stopUpdates.length; i++) {
			if (stopUpdates[i].time > timestamp) {
				return stopUpdates[i];
			}
		}
		// we fell through, return last stop
		return stopUpdates[stopUpdates.length-1];
	}
	
	function loadGtfsRtMap(latLng) {
		loadMap(latLng, null, mapName);
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