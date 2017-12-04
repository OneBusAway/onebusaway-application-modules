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
var OBA = window.OBA || {};

OBA.GoogleMapWrapper = function(mapNode) {	
	
	var lat = OBA.Config.mapCenterLat;
	var lon = OBA.Config.mapCenterLon;
	var zoom = OBA.Config.mapZoom;
    
	if (!lat || !lon || !zoom) {
		// These will get overridden by the bundle bounds after the map initially loads.
		lat = 40.639228;
		lon = -74.081154;
		zoom = 11;
	}
	
	var defaultMapOptions = {
			zoom: zoom,
			mapTypeControl: false,
			streetViewControl: false,
			zoomControl: true,
			zoomControlOptions: {
				style: google.maps.ZoomControlStyle.LARGE
			},
			minZoom: 9, 
			maxZoom: 19,
			navigationControlOptions: { style: google.maps.NavigationControlStyle.DEFAULT },
			center: new google.maps.LatLng(lat,lon)
	};

	var map = new google.maps.Map(mapNode, defaultMapOptions);
	
	// CUSTOM STYLED BASEMAP
	var mutedTransitStylesArray = 
		[{
			featureType: "road.arterial",
			elementType: "geometry",
			stylers: [
			          { saturation: -80 },
			          { lightness: 60 },
			          { visibility: "on" },
			          { hue: "#0011FF" }
			          ]
		},{
			featureType: "road.arterial",
			elementType: "labels",
			stylers: [
			          { saturation: -80 },
			          { lightness: 40 },
			          { visibility: "on" },
			          { hue: "#0011FF" }
			          ]
		},{
			featureType: "road.highway",
			elementType: "geometry",
			stylers: [
			          { saturation: -80 },
			          { lightness: 60 },
			          { visibility: "on" },
			          { hue: "#0011FF" }
			          ]
		},{
			featureType: "road.highway",
			elementType: "labels",
			stylers: [
			          { lightness: 60 },
			          { saturation: -70 },
			          { hue: "#0011FF" },
			          { visibility: "on" }
			          ]
		},{
			featureType: "road.local",
			elementType: "all",
			stylers: [
			          { saturation: -100 },
			          { lightness: 32 }
			          ]
		},{ 
			featureType: "administrative.locality", 
			elementyType: "labels",
			stylers: [ { visibility: "on" }, 
			           { lightness: 50 },
			           { saturation: -80 }, 
			           { hue: "#ffff00" } ] 
		},{ 
			featureType: "administrative.neighborhood", 
			elementyType: "labels",
			stylers: [ { visibility: "on" }, 
			           { lightness: 50 },
			           { saturation: -80 }, 
			           { hue: "#ffffff" } ] 
		},{
			featureType: 'landscape',
			elementType: 'labels',
			stylers: [ {'visibility': 'on'},
			           { lightness: 50 },
			           { saturation: -80 },
			           { hue: "#0099ff" }
			           ]
		},{
			featureType: 'poi',
			elementType: 'labels',
			stylers: [ {'visibility': 'on'},
			           { lightness: 50 },
			           { saturation: -80 },
			           { hue: "#0099ff" }
			           ]
		},{
			featureType: 'water',
			elementType: 'labels',
			stylers: [ {'visibility': 'off'}
			]
		},{
			featureType: 'transit.station.bus',
			elementType: 'labels',
			stylers: [ {'visibility': 'off'}
			]
		}];
	
	var transitStyledMapType = new google.maps.StyledMapType(mutedTransitStylesArray, {name: "Transit"});
	map.mapTypes.set('Transit', transitStyledMapType);
	map.setMapTypeId('Transit');
	
	// BING MAPS TILES
	var bingMapsMapType = new google.maps.ImageMapType({
		getTileUrl: function(coord, zoom) {
			if(!(zoom >= this.minZoom && zoom <= this.maxZoom)) {
				return null;
			}

		    // if not, calculate the quadtree value and request the graphic
			var quad = "", i;
		    for(i = zoom; i > 0; i--) {
		        var mask = 1 << (i - 1); 
		        var cell = 0; 
		        if ((coord.x & mask) != 0) {
		            cell++; }
		        if ((coord.y & mask) != 0) {
		            cell += 2; }
		        quad += cell; 
		    } 
		    
		    return "http://ecn.t0.tiles.virtualearth.net/tiles/r" + quad + ".jpeg?g=914&shading=hill";
		},
		tileSize: new google.maps.Size(256, 256),
		opacity: 1.0,
		maxZoom: 21,
		minZoom: 1,
		name: 'Bing Maps',
		isPng: false,
		alt: ''
	});
	
//	map.mapTypes.set('Bing Maps', bingMapsMapType);
//	map.setMapTypeId('Bing Maps');

	// RETURN OBJECT BACK TO CALLER
	return map;
};