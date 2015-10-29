// Populate a select element with options from an endpoint
// elem - querystring or DOM element to operate on
// path - path of endpoint
// mapfunc - if exists, run on each element in list to come up with option values
function populateSelect(elem, path, onchange) {
	$.getJSON(contextPath + path, function(data) {
		var select = $(elem);
		select.append("<option disabled selected />")
		for (var i = 0; i < data.length; i++) {
			var option = $("<option />").attr("value", data[i]).text(data[i]);
			select.append(option);
		}
		if (onchange)
			select.on("change", onchange);
	})
}

function drawRoute(data) {
	routeGroup.clearLayers();
	
	var latLngs = [];
	for (var i = 0; i < data.size; i++) {
		
		var latLng = L.latLng(data.lats[i], data.lons[i]);
		latLngs.push(latLng);
	
		// These are not stops.
		// L.circleMarker(latLng, stopOptions).addTo(map);
	}
	
	map.fitBounds(latLngs);
	
	L.polyline(latLngs, routeOptions).addTo(routeGroup)
}

function drawVehiclePositions(data) {

	// Since data is an array of objects each of which have a lat and lon
	// property, we can just go ahead and treat them as LatLngs.
	
	vehicleGroup.clearLayers();
	map.fitBounds(data);
	L.polyline(data, routePolylineOptions).addTo(vehicleGroup)
	
	data.forEach(function(avl) {
		
		avl.time = new Date(avl.timestamp).toTimeString();
		
		var avlMarker = L.rotatedMarker(avl, {
	          icon: L.divIcon({
	        	  className: 'avlMarker_',
	        	  html: "<div class='avlTriangle' />",
	        	  iconSize: [7,7]
	          }),
	          angle: avl.bearing, // this doesn't actually seem to be used
	          title: avl.time
	      }).addTo(vehicleGroup);
	  	
		// TODO: make this not suck.
	  	var content = "<table class='popupTable'>" 
		+ "<tr><td class='popupTableLabel'>Vehicle:</td><td>" + avl.vehicleId + "</td></tr>" 
		+ "<tr><td class='popupTableLabel'>GPS Time:</td><td>" + avl.time + "</td></tr>" 
 		+ "<tr><td class='popupTableLabel'>Lat/Lon:</td><td>" + avl.lat + ", " + avl.lon + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Speed:</td><td>" + avl.speed + " kph</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Heading:</td><td>" + avl.bearing + "</td></tr>"
  		+ "</table>";
  		
  		avlMarker.bindPopup(content);
	})
}