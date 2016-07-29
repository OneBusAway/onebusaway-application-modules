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
function createLinkAvlModule(title, urlinput, timestamp, latlon, tripVal, route, block, schdev, nextstopid, 
		nextstoppred, finalstopid, finalstoppred, nexttripid, age, mapName) {

	var linkWeb = OBA.config.linkUrl || "localhost:9764";
	urlinput.val(linkWeb);
	
	title.html("LINK AVL")
	
	// unavailable info
	route.html("N/A");
	block.html("N/A");
	nexttripid.html("N/A");
	
	var avlAge = null;
	
	var module = {};

	module.refresh = function(agencyId, beginDate, numDays, vehicleId, beginTime) {
	
		linkWeb = urlinput.val();
		
		var linkUrl = "http://" + linkWeb + 
			"/services/tss_lab/GetOnScheduleTrains?TimeInterval=5";
	
		jQuery.ajax({
			url: linkUrl,
			type: "GET",
			dataType: 'xml',
			success: function(response) {
				var d = xml2js(response);
				if (d.Trip == null || d.Trip.length == undefined) {
					console.log("webservice attempt failed for " + linkUrl);
					fail();
					return;
				}
				for (var i = 0; i < d.Trip.length; i++) {
					if (d.Trip[i].VehicleId == vehicleId) {
						processLinkTrip(d.Trip[i]);
						return;
					}
				}
				console.log("could not find vehicle");
				fail();
			},
			fail: fail
		})
		
		function fail() {
			route.html("...");
			schdev.html("...");
			block.html("...");
			trip.html("...");
			nextstopid.html("...");	
		}
	}
	
	function processLinkTrip(trip) {
		
		formatStopUpdates(trip);
		
		var date = new Date(trip.LastUpdatedDate);
		timestamp.html(formatTime(date));
		setAge(date);
		
		latlon.html(trip.Lat + ", " + trip.Lon);
		tripVal.html(trip.TripId);
		
		var nextStopUpdate = findBestStop(trip.StopUpdates.Update, date);
		if (nextStopUpdate == null) {
			console.log("could not find stop in future.")
			return;
		}
		
		var schadh = nextStopUpdate.ArrivalTime.Estimated - nextStopUpdate.ArrivalTime.Scheduled;
		schdev.html(formatScheduleDeviation(schadh/1000));
		
		nextstopid.html(nextStopUpdate.StopId);
		nextstoppred.html(formatTime(nextStopUpdate.ArrivalTime.Estimated));
		
		var finalStopUpdate = trip.StopUpdates.Update[trip.StopUpdates.Update.length - 1];
		finalstopid.html(finalStopUpdate.StopId);
		finalstoppred.html(formatTime(finalStopUpdate.ArrivalTime.Estimated));
		
		var latLng = L.latLng(trip.Lat, trip.Lon);
		setTimeout(loadLinkMap, 1000, latLng);
		
	}
	
	function formatStopUpdates(trip) {
		trip.StopUpdates.Update.forEach(function(d) {
			d.ArrivalTime.Scheduled = new Date(d.ArrivalTime.Scheduled);
			d.ArrivalTime.Estimated = new Date(d.ArrivalTime.Estimated);
		})
		
		trip.StopUpdates.Update.sort(function(a, b) {
			return a.ArrivalTime.Scheduled - b.ArrivalTime.Scheduled;
		})
	}
	
	// find first stop update after timestamp.
	// stopUpdates must be sorted
	function findBestStop(stopUpdates, timestamp) {
		for (var i = 0; i < stopUpdates.length; i++) {
			if (stopUpdates[i].ArrivalTime.Estimated > timestamp) {
				return stopUpdates[i];
			}
		}
	}
	
	function xml2js(xml) {
		if (xml.childNodes == undefined || xml.childNodes.length == 0) {
			return xml.textContent;
		}
		else if (xml.childNodes.length == 1) {
			return xml2js(xml.childNodes[0]);
		}
		else {
			var obj = {};
			for (var i in xml.childNodes) {
				var node = xml.childNodes[i];
				if (!node || !node.nodeName) {
					continue;
				}
				var d = xml2js(node);
				if(obj[node.nodeName] == undefined) {
					obj[node.nodeName] = d;
				}
				else if (Array.isArray(obj[node.nodeName])) {
					obj[node.nodeName].push(d);
				}
				else {
					var tmp = obj[node.nodeName];
					obj[node.nodeName] = [tmp, d];
				}
			}
			return obj;
		}
		
	}
	
	function loadLinkMap(latLng) {
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