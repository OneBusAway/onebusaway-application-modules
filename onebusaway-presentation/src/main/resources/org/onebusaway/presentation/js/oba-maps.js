var OBA = window.OBA || {};

/*******************************************************************************
 * Map Methods
 ******************************************************************************/

var obaMapMarkerManagerFactory = function(map) {
	
	var that = {};
	
	var markersByZoomLevel = {};
	var prevZoomLevel = -99;
	
	/****
	 * Private Methods
	 ****/

	var refreshMarkerEntry = function() {
		var zoomLevel = map.getZoom();
		var marker = this.marker;
		var isVisible = marker.getVisible();
		if( isVisible == undefined )
			isVisible = false;
		var shouldBeVisible = this.zoomFrom <= zoomLevel && zoomLevel < this.zoomTo;
		if( isVisible != shouldBeVisible )
			marker.setVisible(shouldBeVisible);
	};
	
	/****
	 * Public Methods
	 ****/
	
	that.addMarker = function(marker, zoomFrom, zoomTo) {
		
		if( zoomTo == undefined )
			zoomTo = 20;
		
		var entry = {
			'marker': marker,
			'zoomFrom': zoomFrom,
			'zoomTo': zoomTo,
			'refreshMarkerEntry': refreshMarkerEntry
		};
		
		var addMarkerEntryForZoomLevel = function(zoom,entry) {
			var markersForZoomLevel = markersByZoomLevel[zoom];
			if( ! markersForZoomLevel ) {
				markersForZoomLevel = new Array();
				markersByZoomLevel[zoom] = markersForZoomLevel;
			}
			markersForZoomLevel.push(entry);
		};
		
		for(var zoom=zoomFrom; zoom < zoomTo; zoom++) {
			addMarkerEntryForZoomLevel(zoom, entry);
		}
		
		marker.setMap(map);
		entry.refreshMarkerEntry();
	};
	
	
	that.refresh = function() {
		
		var markersForPrevZoomLevel = markersByZoomLevel[prevZoomLevel];
		
		if( markersForPrevZoomLevel) {
			jQuery.each(markersForPrevZoomLevel, refreshMarkerEntry);
		}
		
		var zoomLevel = map.getZoom();
		
		var markersForZoomLevel = markersByZoomLevel[zoomLevel];
		
		if( markersForZoomLevel) {
			jQuery.each(markersForZoomLevel, refreshMarkerEntry);
		}
		
		prevZoomLevel = zoomLevel;
	};
	
	google.maps.event.addListener(map, 'zoom_changed', function(){
	    that.refresh();
    });
	
	return that;
};

var obaMapFactory = function() {

	var that = {};
	
	/****
	 * Public Methods
	 ****/
	
	that.markerManager = obaMapMarkerManagerFactory;

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
	
	that.mapReady = function(map,callback) {
		
		if( map.getProjection() != null ) {
			callback();
			return;
		}
		
		var listener = google.maps.event.addListener(map, 'projection_changed', function() {
			google.maps.event.removeListener(listener);
			callback();
		});
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
	
	that.addStopToMarkerManager = function(stop, markerManager) {
		
		var iconType = getIconTypeForStop(stop);
		
		var largeMarker = getMarkerForStop(stop, iconType, 'large');
		markerManager.addMarker(largeMarker, 17);
		
		var mediumMarker = getMarkerForStop(stop, iconType, 'medium');
		markerManager.addMarker(mediumMarker, 16, 17);
		
		var smallMarker = getMarkerForStop(stop, iconType, 'small');
		markerManager.addMarker(smallMarker, 13, 16);
		
		var tinyMarker = getMarkerForStop(stop, iconType, 'tiny');
		markerManager.addMarker(tinyMarker, 9, 13);
		
		return new Array(largeMarker, mediumMarker, smallMarker, tinyMarker);
	};
	
	/****
	 * Private Methods
	 ****/
	
	var iconSizes = {
		'large': '22',
		'medium': '17',
		'small': '14',
		'tiny': '10'
	};
	
	var iconAnchors = {
		'large': {
			'default': new google.maps.Point(11, 11),
			'N': new google.maps.Point(11, 19),
			'NE': new google.maps.Point(11, 15),
			'E': new google.maps.Point(11, 11),
			'SE': new google.maps.Point(11, 11),
			'S': new google.maps.Point(11, 11),
			'SW': new google.maps.Point(15, 11),
			'W': new google.maps.Point(19, 11),
			'NW': new google.maps.Point(15, 15)
		},
		'medium': {
			'default': new google.maps.Point(9, 9),
			'N': new google.maps.Point(9, 14),
			'NE': new google.maps.Point(9, 11),
			'E': new google.maps.Point(9, 9),
			'SE': new google.maps.Point(9, 9),
			'S': new google.maps.Point(9, 9),
			'SW': new google.maps.Point(11, 9),
			'W': new google.maps.Point(14, 9),
			'NW': new google.maps.Point(11, 11)
		},
		'small': {
			'default': new google.maps.Point(7, 7)			
		},
		'tiny': {
			'default': new google.maps.Point(5, 5),
		}
	};
	
	var iconDirections = {
		'N': 'North',
		'NE': 'NorthEast',
		'E': 'East',
		'SE': 'SouthEast',
		'S': 'South',
		'SW': 'SouthWest',
		'W': 'West',
		'NW': 'NorthWest'
	};
	
	var getMarkerForStop = function(stop, iconType, size) {
		
		var iconPixelSize = iconSizes[size];
		var point = new google.maps.LatLng(stop.lat,stop.lon);
		var direction = iconDirections[stop.direction];
		
		if( direction ) {
			
			var urlName = "MapIcon-" + iconType + "-" + iconPixelSize + "-" + direction + ".png";
			var url = OBA.Resources.Map[urlName];
			
			if( url ) {				
				var anchor = iconAnchors[size][stop.direction];
				if( ! anchor )
					anchor = iconAnchors[size]['default'];
				var icon = new google.maps.MarkerImage(url, null, null, anchor);
				return new google.maps.Marker({position: point, icon: icon});
			}
		}
		
		var urlName = "MapIcon-" + iconType + "-" + iconPixelSize + ".png";
		var url = OBA.Resources.Map[urlName];
			
		var anchor = iconAnchors[size]['default'];
		var icon = new google.maps.MarkerImage(url, null, null, anchor);
		return new google.maps.Marker({position: point, icon: icon});
	};
	
	var getIconTypeForStop = function(stop) {
		var routeTypes = {};
		jQuery.each(stop.routes, function() {
			routeTypes[this.type] = true;
		});
		
	    // Ferry takes precedent
		if( routeTypes['4'])
	      return 'Ferry';
	    // Followed by heavy rail
	    else if (routeTypes['2'] )
	      return 'Rail';
	    // Followed by light-rail
	    else if (routeTypes['0'])
	      return 'LightRail';
	    // Bus by default
	    else
	      return 'Bus';
	};

	return that;
};

OBA.Maps = obaMapFactory();
