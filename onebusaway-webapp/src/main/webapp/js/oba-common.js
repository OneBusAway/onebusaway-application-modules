var OBA = window.OBA || {};

/*******************************************************************************
 * API Methods
 ******************************************************************************/

var obaApiFactory = function() {

	var that = {};

	/***************************************************************************
	 * API URL Methods
	 **************************************************************************/

	var createUrl = function(url) {
		return OBA.Config.apiUrl + url;// blah
	};

	var createParams = function(otherParams) {

		var params = {
			key : OBA.Config.apiKey,
			version : 2
		};

		if (otherParams) {

		}

		return params;
	};

	/***************************************************************************
	 * API Callback Handler Methods
	 **************************************************************************/

	var createHandler = function(callback, errorCallback) {

		if (!errorCallback) {
			var errorCallback = function(textStatus) {
			};
		}

		return function(json) {

			if (json && json.code == 200) {
				callback(json.data);
				return;
			}

			errorCallback(textStatus);
		};
	};

	var createListHandler = function(callback, errorCallback, postProcess) {
		var listHandler = function(json) {
			// Eventually, we will do smart handling of references here
			var references = processReferences(json.references);
			var list = json.list;
			for ( var i = 0; i < list.length; i++) {
				postProcess(list[i], references);
			}
			callback(list);
		};

		return createHandler(listHandler, errorCallback);
	};

	/***************************************************************************
	 * JSON Post-Processing Methods
	 **************************************************************************/

	var processReferencesById = function(values) {
		var valuesById = {};
		if (values) {
			for ( var i = 0; i < values.length; i++) {
				var value = values[i];
				valuesById[value.id] = value;
			}
		}
		return valuesById;
	};

	var processReferences = function(references) {
		references.agenciesById = processReferencesById(references.agencies);
		references.routesById = processReferencesById(references.routes);
		references.stopsById = processReferencesById(references.stops);
		references.tripsById = processReferencesById(references.trips);
		references.situationsById = processReferencesById(references.situations);
		return references;
	};

	var processAgencyWithCoverage = function(awc, references) {
		awc.agency = references.agenciesById[awc.agencyId];
	};

	/***************************************************************************
	 * Public API Methods
	 **************************************************************************/

	that.agenciesWithCoverage = function(callback, errorCallback) {
		var url = createUrl('/where/agencies-with-coverage.json');
		var params = createParams();
		var handler = createListHandler(callback, errorCallback,
				processAgencyWithCoverage);
		jQuery.getJSON(url, params, handler);
	};

	return that;
};

OBA.Api = obaApiFactory();

/*******************************************************************************
 * Map Methods
 ******************************************************************************/

var obaMapFactory = function() {

	var that = {};

	that.map = function(element) {

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

			array.push(new google.maps.LatLng(lat * 1e-5, lng * 1e-5));
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
