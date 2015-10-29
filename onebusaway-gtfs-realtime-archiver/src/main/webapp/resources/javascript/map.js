var map = L.map('map').setView([51.505, -0.09], 13);
L.control.scale({metric: false}).addTo(map);

// Using transitime tile layer
L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
 attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
 maxZoom: 19
}).addTo(map);

var vehicleGroup = L.layerGroup().addTo(map);
var routeGroup = L.layerGroup().addTo(map);
var animationGroup = L.layerGroup().addTo(map);

/* For drawing the route and stops, taken from transitTime avlMap.jsp */
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


populateSelect("#vehicles", "/vehicleIds");
populateSelect("#agencies", "/agency", function(evt) {
	$("#routes option").remove();
	populateSelect("#routes", "/routes/" + evt.target.value);
});

$("#routes").on("change", function() {
	var agency = $("#agencies")[0].value,
	route = $("#routes")[0].value;
	$.getJSON(contextPath + "/route/" + agency + "/" + route, drawRoute)
		.fail(function() { alert("No data found for this route."); });
});

$(".datetime").datetimepicker();

$(".datetime").on("change", function(evt) {
	evt.target.timeStamp = evt.timeStamp;
	getVehiclePositions();
})

$("#vehicles").on("change", getVehiclePositions);

function getVehiclePositions() {
	var vehicleId = $("#vehicles")[0].value,
		startTime = $("#startTime")[0].timeStamp,
		endTime = $("#endTime")[0].timeStamp;
	
	var path = contextPath + "/vehiclePositions?vehicleId=" + vehicleId;
	
	if (startTime != undefined)
		path += "&startTime=" + startTime;
	if (endTime != undefined)
		path += "&endTime=" + endTime;
		
	$.getJSON(path, drawVehiclePositions);
}
