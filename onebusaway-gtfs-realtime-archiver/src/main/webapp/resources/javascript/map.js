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
var map = L.map('map')
L.control.scale({metric: false}).addTo(map);

// Using transitime tile layer
L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
 attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
 maxZoom: 19
}).addTo(map);

var vehicleGroup = L.layerGroup().addTo(map);
var routeGroup = L.layerGroup().addTo(map);
var animationGroup = L.layerGroup().addTo(map);

/* Leaflet display options for drawing the route and stops,
 * taken from transitTime avlMap.jsp */

var routeOptions = {
	color: '#00ee00',
	weight: 4,
	opacity: 0.4,
	lineJoin: 'round',
	clickable: false
};
				
 var stopOptions = {
    color: '#006600',
    opacity: 0.4,
    radius: 4,
    weight: 2,
    fillColor: '#006600',
    fillOpacity: 0.3,
};
 
var routePolylineOptions = {clickable: false, color: "#00f", opacity: 0.5, weight: 4};


// Fill agency dropdown menu from API
$.getJSON(contextPath + "/agency", function(data) {
	var select = $("#agencies");
	
	// create option item for each agency
	for (agency in data) {
		if (data.hasOwnProperty(agency)) {
			var option = $("<option />").attr("value", agency).text(agency);
			select.append(option);
		} 
	}
	
	select.on("change", function(evt) {	
		// zoom to agency boundaries
		var bdd = data[evt.target.value];
		map.fitBounds([[bdd.minLat, bdd.minLon], [bdd.maxLat, bdd.maxLon]]);
		
		// populate routes dropdown
		$("#routes option").remove();
		populateSelect("#routes", "/routes/" + evt.target.value);
	})
	
	// trigger change eventL: zoom to first agency.
	select.change(); 
});

// When a route is selected, draw a route polyline and circles for the stops.
$("#routes").on("change", function() {
	var agency = $("#agencies")[0].value,
	route = $("#routes")[0].value;
	
	$.getJSON(contextPath + "/route/" + agency + "/" + route, drawRoute)
		.fail(function() { alert("No data found for this route."); });
	
	$.getJSON(contextPath + "/stops/" + agency + "/" + route, drawStops);
});

// Set up the `vehicle' dropdown menu
populateSelect("#vehicles", "/vehicleIds");

// Datetime pickers come from a jquery plugin: http://keith-wood.name/datetimeEntry.html
$(".datetime")
	.datetimeEntry({
		spinnerImage: contextPath + "/resources/images/spinnerDefault.png",
		datetimeFormat: "O/D/Y h:M"
	})

// set some sane defaults. startTime = now() - 30 min, endTime = now()
$("#endTime").datetimeEntry("setDatetime", new Date());
$("#startTime").datetimeEntry("setDatetime", new Date(Date.now() - 1800000)); // 30 min

$("#submit").click(getVehiclePositions);


// Get the AVL positions from the API for a given vehicle, and draw on map.
function getVehiclePositions() {
	var vehicleId = $("#vehicles")[0].value;
	var startTime = $("#startTime")[0].value;
	var endTime = $("#endTime")[0].value;

	var path = contextPath + "/vehiclePositions?vehicleId=" + vehicleId;
	
	if (startTime != "")
		path += "&startDate=" + Date.parse(startTime);
	if (endTime != "")
		path += "&endDate=" + Date.parse(endTime);
	
	$.getJSON(path, function(data) {
		drawVehiclePositions(data);
		prepareAnimation(data);
	});
}

/* Animation controls */

var busIcon =  L.icon({
    iconUrl:  contextPath + "/resources/images/bus.png", 
    iconSize: [25,25]
});
var animate = avlAnimation(animationGroup, busIcon, $("#playbackTime")[0]);

var playButton = contextPath + "/resources/images/media-playback-start.svg",
	pauseButton = contextPath + "/resources/images/media-playback-pause.svg";

// Given a list of AVL positions, initialize the animation object.
function prepareAnimation(avlData) {
	
	// Fade in playback buttons
	$("#playbackContainer").animate({bottom: "5%"});

	// Make sure animation controls are in their initial state.
	$("#playbackPlay").attr("src", playButton);
	$("#playbackRate").text("1X");
	
	animate(avlData);

}

$("#playbackNext").on("click", animate.next);

$("#playbackPrev").on("click", animate.prev);

$("#playbackPlay").on("click", function() {
	
	if (!animate.paused()) {
		animate.pause();
		$("#playbackPlay").attr("src", playButton);
	}
	else { // need to start it
		animate.start();
		$("#playbackPlay").attr("src", pauseButton);
	}
	
});

$("#playbackFF").on("click", function() {
	var rate = animate.rate()*2;
	animate.rate(rate);
	$("#playbackRate").text(rate + "X");
});

$("#playbackRew").on("click", function() {
	var rate = animate.rate()/2;
	animate.rate(rate);
	$("#playbackRate").text(rate + "X");
});



