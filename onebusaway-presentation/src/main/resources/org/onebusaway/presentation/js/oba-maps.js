/*
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
var OBA = window.OBA || {};

/*******************************************************************************
 * Map Methods
 ******************************************************************************/

var obaMapMarkerManagerFactory = function(map) {
	
	var that = {};
	
	var markersByZoomLevel = {};
	var prevZoomLevel = map.getZoom();
	
	/****
	 * Private Methods
	 ****/

	var refreshMarkerEntry = function() {
		var zoomLevel = map.getZoom();
		var marker = this.marker;
		
		var shouldBeVisible = this.zoomFrom <= zoomLevel && zoomLevel < this.zoomTo && ! marker.remove;
		var isVisible = marker.getVisible();
		
		if( isVisible == undefined )
			isVisible = false;
		
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
		
		marker.obaMarkerManagerEntry = entry;
		
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
	
	that.removeMarker = function(marker) {
		
		var entry = marker.obaMarkerManagerEntry;
		
		if( entry == undefined )
			return;
		
		delete marker.obaMarkerManagerEntry;
		
		/**
		 * Removal of the entry from the 'markersByZoomLevel' object is delayed
		 * until the next iteration of a zoom level.  See 'refreshEntries()' for
		 * details.
		 */
		entry.remove = true;
		entry.refreshMarkerEntry();
	};
	
	that.refresh = function() {
		
		var zoomLevel = map.getZoom();
		
		console.log('zoom level changed=' + zoomLevel + ' prevZoomLevel=' + prevZoomLevel);
		
		var markersForPrevZoomLevel = markersByZoomLevel[prevZoomLevel];
		
		if( markersForPrevZoomLevel) {
			console.log('  markersForPrevZoomLevel=' + markersForPrevZoomLevel.length);
			refreshEntries(markersForPrevZoomLevel);
		}
		
		var markersForZoomLevel = markersByZoomLevel[zoomLevel];
		
		if( markersForZoomLevel) {
			console.log('  markersForZoomLevel=' + markersForZoomLevel.length);
			refreshEntries(markersForZoomLevel);
		}
		
		prevZoomLevel = zoomLevel;
	};
	
	google.maps.event.addListener(map, 'zoom_changed', function(){
	    that.refresh();
    });
	
	var refreshEntries = function(entries) {
		var writeIndex = 0;
		for( var readIndex=0; readIndex < entries.length; readIndex++) {
			
			var entry = entries[readIndex];

			/**
			 * If the entry is marked for removal, we skip it 
			 */
			if( entry.remove )
				continue;
			
			entry.refreshMarkerEntry();
			
			/**
			 * Do we need to copy an entry forward?
			 */
			if( readIndex != writeIndex) {
				entries[writeIndex] = entry;
			}
			
			writeIndex++;
		}
		
		// Remove any trailing elements
		if( writeIndex < entries.length ) {
			entries.splice(writeIndex,entries.length - writeIndex);
		}
	};
	
	return that;
};

var obaMapFactory = function() {

	var that = {};
	
	const RADIUS_OF_EARTH_IN_KM = 6371.01;
	
	/****
	 * Public Methods
	 ****/
	
	that.markerManager = obaMapMarkerManagerFactory;

	that.map = function(element, params) {
		
		params = params || {};
		
		if( element instanceof jQuery )
			element = element.get(0);
		
		var lat = params.lat || OBA.Config.centerLat || 47.606828;
		var lon = params.lon || OBA.Config.centerLon || -122.332505;
		var zoom = params.zoom || 10;
		
		var mapCenter = new google.maps.LatLng(lat, lon);
		var mapOptions = {
			zoom : zoom,
			center : mapCenter,
			gestureHandling: 'greedy',
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		
		var map = new google.maps.Map(element, mapOptions);

		if( ! (params.lat || params.lon || params.zoom ) && OBA.Config.spanLat && OBA.Config.spanLon) {
			var spanLat = OBA.Config.spanLat;
			var spanLon = OBA.Config.spanLon;
			var bounds = new google.maps.LatLngBounds();
			bounds.extend(new google.maps.LatLng(lat-spanLat/2,lon-spanLon/2));
			bounds.extend(new google.maps.LatLng(lat+spanLat/2,lon+spanLon/2));
			map.fitBounds(bounds);
		};
		
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
	
	var encodeNumber = function(num) {
		var encodeString = '';
	    while (num >= 0x20) {
	      var nextValue = (0x20 | (num & 0x1f)) + 63;
	      encodeString += (String.fromCharCode(nextValue))
	      num >>= 5;
	    }

	    num += 63;
	    encodeString += (String.fromCharCode(num))

	    return encodeString;
	};
	
	var encodedSignedNumber = function(num) {
	    var sgn_num = num << 1;
	    if (num < 0) {
	      sgn_num = ~(sgn_num);
	    }
	    return (encodeNumber(sgn_num));
	};
	
	that.encodePolyline = function(points) {
		
		var encoded = '';
		var pLat = 0;
		var pLng = 0;
		
		for( var index = 0; index<points.length; index++) {
			var point = points[index];
			var late5 = point.lat() * 1e5;
			var lnge5 = point.lng() * 1e5;
			
		    var dlat = late5 - pLat;
		    var dlng = lnge5 - pLng;
		    
		    pLat = late5;
		    pLng = lnge5;
		    
		    encoded += encodedSignedNumber(dlat);
		    encoded += encodedSignedNumber(dlng);
		}

		return encoded;
	};

	that.getPointsAsBounds = function(points) {
		var bounds = new google.maps.LatLngBounds();
		for ( var i = 0; i < points.length; i++) {
			bounds.extend(points[i]);
		}
		return bounds;
	};
	
	that.boundsForPointAndRadius = function(point, radius) {
		return that.boundsForLatLonAndRadius(point.lat(), point.lng(), radius, radius);
	};
	
	that.boundsForLatLonAndRadius = function(lat, lon, latDistance, lonDistance) {

		var radiusOfEarth = RADIUS_OF_EARTH_IN_KM * 1000;

		var latRadians = toRadians(lat);
		var lonRadians = toRadians(lon);

		var latRadius = radiusOfEarth;
		var lonRadius = Math.cos(latRadians) * radiusOfEarth;

		var latOffset = latDistance / latRadius;
		var lonOffset = lonDistance / lonRadius;

		var latFrom = toDegrees(latRadians - latOffset);
		var latTo = toDegrees(latRadians + latOffset);

		var lonFrom = toDegrees(lonRadians - lonOffset);
		var lonTo = toDegrees(lonRadians + lonOffset);

		var p1 = new google.maps.LatLng(latFrom,lonFrom);
		var p2 = new google.maps.LatLng(latTo,lonTo);
		return new google.maps.LatLngBounds(p1,p2);
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
	
	that.getWhiteCircle14MarkerImage = function() {
		var url = OBA.Resources.Map['WhiteCircle14.png'];
		var anchor = new google.maps.Point(7, 7);
		return new google.maps.MarkerImage(url, null, null, anchor);
	};
	
	/****
	 * Private Methods
	 ****/
	
	var toRadians = function(angdeg) {
		return angdeg / 180.0 * Math.PI;
	};
	
	var toDegrees = function(angrad) {
		return angrad * 180.0 / Math.PI;
	};
	
	var iconSizes = {
		'large': '22',
		'medium': '17',
		'small': '14',
		'tiny': '10'
	};
	
	var iconAnchors = {};
	
	if( window.google ) {
		iconAnchors = {
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
	}
	
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
				return new google.maps.Marker({position: point, icon: icon, visible: false});
			}
		}
		
		var urlName = "MapIcon-" + iconType + "-" + iconPixelSize + ".png";
		var url = OBA.Resources.Map[urlName];
			
		var anchor = iconAnchors[size]['default'];
		var icon = new google.maps.MarkerImage(url, null, null, anchor);
		return new google.maps.Marker({position: point, icon: icon, visible: false});
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
