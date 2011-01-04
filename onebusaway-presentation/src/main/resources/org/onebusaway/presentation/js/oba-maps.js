var OBA = window.OBA || {};

/*******************************************************************************
 * Map Methods
 ******************************************************************************/

var obaMapFactory = function() {

	var that = {};

	that.map = function(element) {
		
		if( element instanceof jQuery )
			element = element.get(0);

		var lat = OBA.Config.centerLat || 47.606828;
		var lon = OBA.Config.centerLon || -122.332505;
		var mapCenter = new google.maps.LatLng(lat, lon);
		var mapOptions = {
			zoom : 10,
			center : mapCenter,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		
		var map = new google.maps.Map(element, mapOptions);

		if( OBA.Config.spanLat && OBA.Config.spanLon) {
			var spanLat = OBA.Config.spanLat;
			var spanLon = OBA.Config.spanLon;
			var bounds = new google.maps.LatLngBounds();
			bounds.extend(new google.maps.LatLng(lat-spanLat/2,lon-spanLon/2));
			bounds.extend(new google.maps.LatLng(lat+spanLat/2,lon+spanLon/2));
			map.fitBounds(bounds);
		}
		
		return map;
	};

	that.decodePolyline = function(encoded) {
		
		var len = encoded.length;
		var index = 0;
		var array = [];
		var lat = 0;
		var lng = 0;
		
		while (index < len) {
			var b;
			var shift = 0;
			var result = 0;
			do {
				b = encoded.charCodeAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			var dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charCodeAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			var dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			var point = new google.maps.LatLng(lat * 1e-5, lng * 1e-5);
			array.push(point);
		}

		return array;
	};

	that.getPointsAsBounds = function(points) {
		var bounds = new google.maps.LatLngBounds();
		for ( var i = 0; i < points.length; i++) {
			bounds.extend(points[i]);
		}
		return bounds;
	};

	return that;
};

OBA.Maps = obaMapFactory();
