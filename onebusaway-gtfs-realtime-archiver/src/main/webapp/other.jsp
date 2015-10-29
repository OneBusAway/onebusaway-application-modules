<!DOCTYPE html>
<head>
 <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.css" />
 <script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
 <script src="https://code.jquery.com/jquery-2.1.4.min.js"></script>
  <style>
  body {
    padding: 0;
    margin: 0;
  }
  html, body, #map {
	height: 100%;
    width: 100%;
  }
  /* For the params menu */
  #controls {
    background: lightgrey;
	position:absolute;
	top:10px;
	right:10px;
	border-radius: 25px;
	padding: 2%;
	border: 2px solid black;
  }
  </style>
</head>
<body>
<div id="map"></div>
<div id="controls">
  Routes: <select id="routes"></select> <br>
  Vehicles: <select id="vehicles"></select> <br>
  
</div>
</body>
<script>
var contextPath = "<%= request.getContextPath() %>";

var map = L.map('map').setView([51.505, -0.09], 13);
L.control.scale({metric: false}).addTo(map);

// Using transitime tile layer
L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
 attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
 maxZoom: 19
}).addTo(map);


// Populate a select element with options from an endpoint
// elem - querystring or DOM element to operate on
// path - path of endpoint
// mapfunc - if exists, run on each element in list to come up with option values
function populateSelect(elem, path, map) {
	$.getJSON(contextPath + path, function(data) {
		var select = $(elem);
		for (var i = 0; i < data.length; i++) {
			var d = map ? map(data[i]) : data[i];
			var option = $("<option />").attr("value", d).text(d);
			select.append(option);
		}
	})
}
populateSelect("#vehicles", "/vehicleIds");
populateSelect("#routes", "/routes", function(d) { return d.agencyId + "/" + d.id });

</script>