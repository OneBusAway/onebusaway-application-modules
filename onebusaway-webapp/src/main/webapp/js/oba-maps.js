var OBA = window.OBA || {};

OBA.map = function(element) {
	var mapCenter  = new google.maps.LatLng(47.606828, -122.332505);
	var mapOptions = {
			zoom: 13,
			center: mapCenter,
			mapTypeId : google.maps.MapTypeId.ROADMAP
	};
	return new google.maps.Map(element, mapOptions);
};

OBA.decodePolyline = function(encoded) {
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

    array.push(new google.maps.LatLng(lat * 1e-5,lng * 1e-5));
  }

  return array;
};

OBA.getPointsAsBounds = function(points) {
    var bounds = new google.maps.LatLngBounds();
    for(var i=0; i<points.length; i++) {
      bounds.extend(points[i]);
    }
    return bounds;
};


