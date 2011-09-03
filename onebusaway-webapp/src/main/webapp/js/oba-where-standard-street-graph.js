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
var oba_where_standard_street_graph = function() {

	var mapParams = {};
	mapParams.lat = 47.606828;
	mapParams.lon = -122.332505;
	mapParams.zoom = 12;

	var map = OBA.Maps.map(jQuery('#map'), mapParams);
	var infoWindow = new google.maps.InfoWindow();
	var overlays = [];
	var edgeOverlays = [];
	
	var unsetMap = function() {
		this.setMap(null);
	};
	
	var clearOverlays = function() {
		
		jQuery.each(overlays, unsetMap);
		overlays = [];
		
		jQuery.each(edgeOverlays, unsetMap);
		edgeOverlays = [];
	};
	
	var needsRefresh = false;
	
	google.maps.event.addListener(map, "bounds_changed", function() {
		needsRefresh = true;
	});
	
	google.maps.event.addListener(map, "idle", function() {
		
		if( ! needsRefresh )
			return;
		
		clearOverlays();

		if( map.getZoom() > 17) {
			OBA.Api.streetGraphForRegion(map.getBounds(),graphHandler);
		}
		
		needsRefresh = false;
	});
	
	var tagsHandler = function(element,tags) {
		for( var key in tags) {
			var li = jQuery('<li/>');
			li.text(key + ': ' + tags[key]);
			li.appendTo(element);
		}		
	};
	
	var highlightedEdgeHandler = function(edge) {
		
		var path = edge.path;
		
		if (! path)
			return;
			
		var points = OBA.Maps.decodePolyline(path);

		var opts = {
			path : points,
			strokeColor : '#ff0000',
			strokeWeight : 3,
			strokeOpacity : 1.0,
			zIndex : 6
		};

		var line = new google.maps.Polyline(opts);
		line.setMap(map);
		edgeOverlays.push(line);
		
		google.maps.event.addListener(line, 'click', function() {
	    	var content = jQuery('.edgeTemplate').clone();
	    	tagsHandler(content.find('ul'),edge.tags);
	    	content.removeClass('edgeTemplate');
	    	content.addClass('edge');
	    	infoWindow.setContent(content.show().get(0));
	    	var pos = new google.maps.MVCObject();
	    	pos.set('position',points[points.length-1]);
	    	infoWindow.open(map,pos);
	    });
	};
	
	var vertexClickHandler = function(vertex, marker, edgesByFromVertex) {
		
    	var content = jQuery('.vertexTemplate').clone();
	    content.find("h3").text(vertex.id);
	    tagsHandler(content.find('ul'),vertex.tags);
    	content.removeClass('vertexTemplate');
    	content.addClass('vertex');
	    infoWindow.setContent(content.show().get(0));
	    infoWindow.open(map,marker);
	    
	    // Clear any existing edge overlays
		jQuery.each(edgeOverlays, unsetMap);
		edgeOverlays = [];
	    
		// Add highlighted edges
	    var edges = edgesByFromVertex[vertex.id] || [];	    
	    jQuery.each(edges, function() {
	    	highlightedEdgeHandler(this);
	    });
	};
	
	var vertexHandler = function(vertex, edgesByFromVertex) {
		
		var loc = vertex.location;
		var point = new google.maps.LatLng(loc.lat,loc.lon);
		var icon = OBA.Maps.getWhiteCircle14MarkerImage();
		var marker = new google.maps.Marker({position: point, icon: icon, map: map, zIndex: 10});
		overlays.push(marker);
		
	    google.maps.event.addListener(marker, 'click', function() {
	    	vertexClickHandler(vertex, marker, edgesByFromVertex);
	    });
	};
	
	var edgeHandler = function(edge, edgesByFromVertex) {
		
		var fromId = edge.fromId;
		
		var edgesForId = edgesByFromVertex[fromId]
		if( ! edgesForId ) {
			edgesForId = [];
			edgesByFromVertex[fromId] = edgesForId;
		}
		
		edgesForId.push(edge);
		
		var path = edge.path;
		
		if (path) {
			
			var points = OBA.Maps.decodePolyline(path);
			
			var opts = {
				path: points,
				strokeColor: '#000000',
				strokeWeight: 2,
				strokeOpacity: 1.0,
				zIndex: 5,
				clickable: false
			};
			
			var line = new google.maps.Polyline(opts);
			line.setMap(map);
			overlays.push(line);
		}	
	};
	
	var graphHandler = function(entry) {
		
		var edges = entry.edges || [];
		var edgesByFromVertex = {};
		
		jQuery.each(edges, function() {
			edgeHandler(this, edgesByFromVertex);
		});
		
		var vertices = entry.vertices || [];
		jQuery.each(vertices, function() {
			vertexHandler(this, edgesByFromVertex);
		});
		
		
	};
};