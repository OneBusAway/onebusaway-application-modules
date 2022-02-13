/*
 * Copyright (c) 2011 Metropolitan Transportation Authority
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

OBA.RouteMap = function(mapNode, initCallbackFn, serviceAlertCallbackFn) {	

	var initialized = false;

	var map = null;

	var locationMarker = null;
	var disambiguationMarkers = [];

	var vehiclesByRoute = {};
	var vehiclesById = {};
	var polylinesByRoute = {};
	var hoverPolylinesByRoute = {};
	var stopsById = {};

	var siriVMRequestsByRouteId = {};
	var stopsWithinBoundsRequest = null;
	
	// when hovering over a route in "region" view
	var hoverPolyline = null;
	
	// when hovering over a stop in route view
	var highlightedStop = null;

	// icons for disambiguation markers
	var locationIconArrays = OBA.Config.loadLocationIcons();
	var locationIcons = locationIconArrays[0], activeLocationIcons = locationIconArrays[1], iconShadow = locationIconArrays[2];    
	var normalLocationIcon = locationIcons[0], activeLocationIcon = activeLocationIcons[0];

	// POLYLINE
	function removePolylines(routeId) {
		if(typeof polylinesByRoute[routeId] !== 'undefined') {
			var hoverPolyines = hoverPolylinesByRoute[routeId];

			jQuery.each(hoverPolyines, function(_, polyline) {
				polyline.setMap(null);
			});

			var polylines = polylinesByRoute[routeId];

			jQuery.each(polylines, function(_, polyline) {
				polyline.setMap(null);
			});
			
			delete polylinesByRoute[routeId];
			delete hoverPolylinesByRoute[routeId];
		}
	}
	
	function addPolylines(routeId, encodedPolylines, color) {
		if(typeof polylinesByRoute[routeId] === 'undefined') {
			polylinesByRoute[routeId] = [];
			hoverPolylinesByRoute[routeId] = [];
		}

		jQuery.each(encodedPolylines, function(_, encodedPolyline) {
			var points = OBA.Util.decodePolyline(encodedPolyline);
		
			var latlngs = jQuery.map(points, function(x) {
				return new google.maps.LatLng(x[0], x[1]);
			});

			var options = {
				path: latlngs,
				strokeColor: "#" + color,
				strokeOpacity: 1.0,
				strokeWeight: 3,
				clickable: false,
				map: map,
				zIndex: 2
			};
			
			var shape = new google.maps.Polyline(options);

			var hoverOptions = {
					path: latlngs,
					strokeColor: "#" + color,
					strokeOpacity: 0.6,
					strokeWeight: 10,
					clickable: false,
					visible: false,
					map: map,
					zIndex: 1
			};

			var hoverShape = new google.maps.Polyline(hoverOptions);
		
			polylinesByRoute[routeId].push(shape);
			hoverPolylinesByRoute[routeId].push(hoverShape);
		});	
	}

	// STOPS
	function removeStops(preserveStopsInView) {
		jQuery.each(stopsById, function(_, marker) {
			var stopId = marker.stopId;
				
			if(stopId === OBA.Popups.getPopupStopId()) {
				return true;
			}
			
			if(preserveStopsInView && map.getBounds().contains(marker.getPosition())) {
				return true;
			}
				
			delete stopsById[stopId];				
			marker.setMap(null);
		});
	}
	
	function addStop(stop, successFn) {
		var stopId = stop.id;

		if(typeof stopsById[stopId] !== 'undefined') {
			var marker = stopsById[stopId];
			
	        if(typeof successFn !== 'undefined' && successFn !== null) {
	        	successFn(marker);
	        }
	        
			return marker;
		}
		
		// if we get here, we're adding a new stop marker:
		var name = stop.name;
		var latitude = stop.latitude;
		var longitude = stop.longitude;
		var direction = stop.stopDirection;		
		var directionKey = direction;

		if(directionKey === null) {
			directionKey = "unknown";
		}

		var stopsOnRoute = [];
		if (typeof jQuery("body").data("savedData") === 'undefined' && jQuery("body").data("savedData") != null)
			stopsOnRoute = jQuery("body").data("savedData").stops;

        var onRoute = false;
		jQuery.each(stopsOnRoute, function(_, stopOnRoute) {
			if (String(stopOnRoute) === String(stop.id)) {
				onRoute = true;
			}
        });

		if (onRoute === true) {
            var icon = {
                url: OBA.Config.urlPrefix + "img/realtime/stop/stop-" + directionKey + ".png",
                size: new google.maps.Size(31, 31),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(15, 15),
                scaledSize: new google.maps.Size(33, 33)
            }
        } else {
            var icon = {
            	url: OBA.Config.urlPrefix + "img/realtime/stop/stop-off-route-" + directionKey + ".png",
				size: new google.maps.Size(21, 21),
				origin: new google.maps.Point(0,0),
				anchor: new google.maps.Point(10, 10)
			}
		}

		var defaultVisibility = (map.getZoom() < 16) ? false : true;
		var markerOptions = {
				position: new google.maps.LatLng(latitude, longitude),
				icon: icon,
				zIndex: 1,
				title: name,
				stopId: stopId,
				map: map,
				visible: defaultVisibility
				};

        var marker = new google.maps.Marker(markerOptions);
        
        google.maps.event.addListener(marker, "click", function(mouseEvent, routeFilter) {
    	   var stopIdParts = stopId.split("_");
    	   var stopIdWithoutAgency = stopIdParts[1];
    	   
    	   OBA.Config.analyticsFunction("Stop Marker Click", stopIdWithoutAgency);
    	   
    	   OBA.Popups.showPopupWithContentFromRequest(map, this, OBA.Config.stopForId, 
    			   { stopId: stopId },
    			   OBA.Popups.getStopContentForResponse, 
    			   routeFilter);
    	  
    	});

        stopsById[stopId] = marker;		
        
        if(typeof successFn !== 'undefined' && successFn !== null) {
        	successFn(marker);
        }
        
        return marker;
	}
	
	// VEHICLES
	function updateVehicles(routeId) {
		if(typeof vehiclesByRoute[routeId] === 'undefined') {
			vehiclesByRoute[routeId] = {};
		}

		var underscoreIndex = routeId.indexOf("_");//only the first "_" should be considered
		var agencyId = routeId.substr(0, underscoreIndex);
		var routeIdWithoutAgency = routeId.substr(underscoreIndex + 1);
		
		var params = { OperatorRef: agencyId, LineRef: routeIdWithoutAgency };		

		if(OBA.Config.time !== null) {
			params.time = OBA.Config.time;
		}
		
		if(typeof siriVMRequestsByRouteId[routeId] !== 'undefined' && siriVMRequestsByRouteId[routeId] !== null) {
			siriVMRequestsByRouteId[routeId].abort();
		}
		siriVMRequestsByRouteId[routeId] = jQuery.getJSON(OBA.Config.siriVMUrl + "&callback=?", params, 
		function(json) {
			// service alerts
			if(typeof serviceAlertCallbackFn === 'function') {
				if(typeof json.Siri !== 'undefined'
					&& typeof json.Siri.ServiceDelivery.SituationExchangeDelivery !== 'undefined'
					&& json.Siri.ServiceDelivery.SituationExchangeDelivery.length > 0) {
					serviceAlertCallbackFn(routeId, 
						json.Siri.ServiceDelivery.SituationExchangeDelivery[0].Situations.PtSituationElement);
				}
			}
			
			// service delivery
			var vehiclesByIdInResponse = {};
			if (json.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity != undefined) {
                jQuery.each(json.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity, function (_, activity) {
                    var latitude = activity.MonitoredVehicleJourney.VehicleLocation.Latitude;
                    var longitude = activity.MonitoredVehicleJourney.VehicleLocation.Longitude;
                    var orientation = activity.MonitoredVehicleJourney.Bearing;
                    var headsign = activity.MonitoredVehicleJourney.DestinationName;
                    var routeName = activity.MonitoredVehicleJourney.PublishedLineName;
                    var hasRealtime = activity.MonitoredVehicleJourney.Monitored;
                    var vehicleType = activity.MonitoredVehicleJourney.VehicleMode[0];

                    var tripId = activity.MonitoredVehicleJourney.FramedVehicleJourneyRef.DatedVehicleJourneyRef;

                    var vehicleId = activity.MonitoredVehicleJourney.VehicleRef;
                    var vehicleIdParts = vehicleId.split("_");
                    var vehicleIdWithoutAgency = vehicleIdParts[1];
                    var marker = vehiclesById[vehicleId];
                    var newMarker = false;
                    var markerImage = OBA.Config.urlPrefix + 'img/realtime/' + vehicleType + '/' + vehicleType + '-';

                    // has route been removed while in the process of updating?
                    if (typeof vehiclesByRoute[routeId] === 'undefined') {
                        return false;
                    }

                    // create marker if it doesn't exist
                    if (typeof marker === 'undefined' || marker === null) {
                        var markerOptions = {
                            zIndex: 3,
                            map: map,
                            title: "Vehicle " + vehicleIdWithoutAgency + ", " + routeName + " to " + headsign,
                            vehicleId: vehicleId,
                            routeId: routeId
                        };

                        newMarker = true;
                        marker = new google.maps.Marker(markerOptions);

                        google.maps.event.addListener(marker, "click", function (mouseEvent) {
                            OBA.Config.analyticsFunction("Vehicle Marker Click", vehicleIdWithoutAgency);

                            OBA.Popups.showPopupWithContentFromRequest(map, this, OBA.Config.siriVMUrl + "&callback=?",
                                {
                                    OperatorRef: agencyId,
                                    VehicleRef: vehicleIdWithoutAgency,
                                    MaximumNumberOfCallsOnwards: "3",
                                    VehicleMonitoringDetailLevel: "calls",
                                    TripId: tripId
                                },
                                OBA.Popups.getVehicleContentForResponse, null);
                        });
                    }

                    // change marker image depending on whether realtime data is available
                    if (typeof hasRealtime === 'undefined' || hasRealtime === null || hasRealtime == false) {
                        markerImage = OBA.Config.urlPrefix + 'img/scheduled/' + vehicleType + '/' + vehicleType + '-';
                    }

                    // icon
                    var orientationAngle = "unknown";
                    if (orientation !== null && orientation !== 'NaN') {
                        orientationAngle = Math.floor(orientation / 5) * 5;
                    }

                    var icon = new google.maps.MarkerImage(markerImage + orientationAngle + ".png",
                        new google.maps.Size(51, 51),
                        new google.maps.Point(0, 0),
                        new google.maps.Point(25, 25));

                    marker.setIcon(icon);


                    var position = new google.maps.LatLng(latitude, longitude);

                    if (newMarker) {
                        marker.setPosition(position);
                    }
                    else {

                        // try shape animation, otherwise revert to straight-line
                        if (!tryAnimateAlongShape(vehicleId, routeId, marker, marker.getPosition(), position)) {
                            marker.animateTo(position);
                        }
                    }

                    // (mark that this vehicle is still in the response)
                    vehiclesByIdInResponse[vehicleId] = true;

                    // maps used to keep track of marker
                    vehiclesByRoute[routeId][vehicleId] = marker;
                    vehiclesById[vehicleId] = marker;
                });
            }
			
			// remove vehicles from map that are no longer in the response, for all routes in the query
			jQuery.each(vehiclesById, function(vehicleOnMap_vehicleId, vehicleOnMap) {
				if(typeof vehiclesByIdInResponse[vehicleOnMap_vehicleId] === 'undefined') {
					var vehicleOnMap_routeId = vehicleOnMap.routeId;
					
					// the route of the vehicle on the map wasn't in the query, so don't check it.
					if(routeId !== vehicleOnMap_routeId) {
						return;
					}
					
					vehicleOnMap.setMap(null);
					delete vehiclesById[vehicleOnMap_vehicleId];
					delete vehiclesByRoute[vehicleOnMap_routeId][vehicleOnMap_vehicleId];
				}
			});
		});
	}

    function tryAnimateAlongShape(vehicleId, routeId, marker, oldPosition, newPosition) {
        if (typeof polylinesByRoute[routeId] === 'undefined')
            return false;

        var polylines = polylinesByRoute[routeId];
        if (polylines.length == 0)
            return false;

        for (var i = 0; i < polylines.length; i++) {
            var polyline = polylines[i];

            // if we don't find a result within this threshold, we can't trust the results
            var startIndex = findClosestPoint(oldPosition, polyline.getPath(), 10);
            var endIndex = findClosestPoint(newPosition, polyline.getPath(), 10);

            if (startIndex >= 0 && endIndex >= 0 && endIndex > startIndex) {
                // animate startIndex up to endIndex
                var index = startIndex;

                var distances = [0];
                var maxDistance = 0;
                for (var j = startIndex + 1; j < endIndex; j++) {
                    var dist = haversine(polyline.getPath().getAt(j - 1), polyline.getPath().getAt(j));
                    maxDistance += dist;
                    distances.push(dist);
                }
                var durations = distances.map(function (d) {
                    return (d / maxDistance) * 1000
                });

                function step() {
                    if (index > endIndex)
                        return;
                    var pos = polyline.getPath().getAt(index);
                    var duration = durations[index - startIndex];
                    marker.animateTo(pos, {"duration": duration, "complete": step, "easing": "linear"});
                    index++;
                }

                index++;
                step();

                //make sure marker ends at the correct final position no matter what happened along the way
                marker.setPosition(newPosition);

                return true;
            } else if (startIndex >= 0 && endIndex >= 0 && endIndex < startIndex) {
                // animate startIndex downto endIndex
                var index = startIndex;

                var distances = [0];
                var maxDistance = 0;
                for (var j = startIndex - 1; j > endIndex; j--) {
                    var dist = haversine(polyline.getPath().getAt(j - 1), polyline.getPath().getAt(j));
                    maxDistance += dist;
                    distances.push(dist);
                }
                var durations = distances.map(function (d) {
                    return (d / maxDistance) * 1000
                });

                function step() {
                    if (index < endIndex)
                        return;
                    var pos = polyline.getPath().getAt(index);
                    var duration = durations[index - startIndex];
                    marker.animateTo(pos, {"duration": duration, "complete": step, "easing": "linear"});
                    index--;
                }

                index--;
                step();

                //make sure marker ends at the correct final position no matter what happened along the way
                marker.setPosition(newPosition);

                return true;

			}

        }

        return false;
    }

    function findClosestPoint(point, line, threshold) {

        var minDistance = Number.MAX_VALUE;
        var closestIndex = -1;

        line.forEach(function (p, i) {
            var distance = haversine(point, p);
            if (distance < threshold && distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        });

        return closestIndex;
    }

    // adapted from https://github.com/njj/haversine
    function haversine(start, end) {
        var R = 6371000; // radius of the earth in meters

        var toRad = function (num) {
            return num * Math.PI / 180
        };

        var dLat = toRad(end.lat() - start.lat());
        var dLon = toRad(end.lng() - start.lng());
        var lat1 = toRad(start.lat());
        var lat2 = toRad(end.lng());

        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

	function removeVehicles(routeId) {
		if(typeof vehiclesByRoute[routeId] !== 'undefined') {
			var vehicles = vehiclesByRoute[routeId];
			delete vehiclesByRoute[routeId];
			
			jQuery.each(vehicles, function(_, marker) {
				var vehicleId = marker.vehicleId;
				
				marker.setMap(null);
				delete vehiclesById[vehicleId];
			});
		}
	}
	
	// MISC	
	function removeDisambiguationMarkers() {
		jQuery.each(disambiguationMarkers, function(_, marker) {
			marker.setMap(null);
		});
	}
	
	function removeHoverPolyline() {
		if(hoverPolyline !== null) {
			jQuery.each(hoverPolyline, function(_, polyline) {
				polyline.setMap(null);
			});
		}
		
		hoverPolyline = null;
	}
	
	function unhighlightStop() {
		if(highlightedStop !== null) {
			var previousVisibility = highlightedStop.previousVisibility;
			if(OBA.Popups.getPopupStopId() !== highlightedStop.stopId) {
				highlightedStop.setVisible(previousVisibility);
				highlightedStop.previousVisibility = null;
			}
			
			highlightedStop.setIcon(highlightedStop.previousIcon);
			highlightedStop.previousIcon = null;
		}
		highlightedStop = null;
	}

	function showLegend(map) {
	 	var iconBase = OBA.Config.urlPrefix + 'img/';
	        var icons = {
	          realtime: {
	            name: 'Real-Time',
	            icon: iconBase + 'realtime/bus/bus.png'
	          },
	          scheduled: {
	            name: 'Scheduled',
	            icon: iconBase + 'scheduled/bus/bus.png'
	          }
	        };
	        
	        var legend = document.getElementById('legend');
	        for (var key in icons) {
	          var type = icons[key];
	          var name = type.name;
	          var icon = type.icon;
	          var div = document.createElement('div');
	          div.innerHTML = '<img src="' + icon + '"> ' + '<span>' + name + '</span>';
	          if (legend) legend.appendChild(div);
	        }
	

	        map.controls[google.maps.ControlPosition.RIGHT_TOP].push(legend);
	}
		
	//////////////////// CONSTRUCTOR /////////////////////

	map = new OBA.GoogleMapWrapper(document.getElementById("map"));
	
	// If there is no configured map center and zoom...
	// Zoom/pan the map to the area specified from our configuration Javascrit that gets its
	// values from the server dynamically on page load.
	if (!OBA.Config.mapCenterLat || !OBA.Config.mapCenterLon || !OBA.Config.mapZoom) {
		var swCorner = new google.maps.LatLng(OBA.Config.mapBounds.swLat, OBA.Config.mapBounds.swLon);
		var neCorner = new google.maps.LatLng(OBA.Config.mapBounds.neLat, OBA.Config.mapBounds.neLon);
		var bounds = new google.maps.LatLngBounds(swCorner, neCorner);
		map.fitBounds(bounds);
	}
	
	// when map is idle ("ready"), initialize the rest of the google maps stuff, if we haven't already.
	// otherwise, refresh the stops on the map after the user is done panning.
	google.maps.event.addListener(map, "idle", function() {
		// start adding things to map once it's ready...
		if(initialized === false) {
			initialized = true;
			
		showLegend(map);


			
			if(typeof initCallbackFn === 'function') {
				initCallbackFn();
			}
		}
		
		// request list of stops in viewport when user stops moving map
		if(map.getZoom() < 16) {
			removeStops(false);
		} else {	
			if(stopsWithinBoundsRequest !== null) {
				stopsWithinBoundsRequest.abort();
			}
			stopsWithinBoundsRequest = jQuery.getJSON(OBA.Config.stopsWithinBoundsUrl + "?callback=?", { bounds: map.getBounds().toUrlValue() }, 
			function(json) {
				removeStops(true);
				
				jQuery.each(json.stops, function(_, stop) {
					addStop(stop, null);					
				});
			});
		}
	});
	
	// timer to update data periodically 
	setInterval(function() {
		jQuery.each(vehiclesByRoute, function(routeId, vehicles) {
			updateVehicles(routeId);
		});
	}, OBA.Config.refreshInterval);

	//////////////////// PUBLIC INTERFACE /////////////////////
	return {
		// STOP HOVER 
		highlightStop: function(stopResult, usePin) {
			unhighlightStop();
			
			var stopMarker = stopsById[stopResult.id];
			if(typeof stopMarker === 'undefined') {
				stopMarker = addStop(stopResult, null);
			}

			var direction = stopResult.stopDirection;		
			var directionKey = direction;

			if(directionKey === null) {
				directionKey = "unknown";
			}

			var highlightedIcon;
			if (usePin) {
				highlightedIcon = new google.maps.MarkerImage(OBA.Config.urlPrefix + "img/icons/red-dot.png");
			}
			else {
                highlightedIcon = new google.maps.MarkerImage(OBA.Config.urlPrefix + "img/realtime/stop/stop-" + directionKey + "-active.png",
                    new google.maps.Size(31, 31),
                    new google.maps.Point(0, 0),
                    new google.maps.Point(15, 15));
                highlightedIcon.scaledSize = new google.maps.Size(33, 33);
            }
		
			stopMarker.previousIcon = stopMarker.getIcon();
			stopMarker.setIcon(highlightedIcon);
			
			stopMarker.previousVisibility = stopMarker.getVisible();
			stopMarker.setVisible(true);
			
			highlightedStop = stopMarker;
		},
		
		unhighlightStop: unhighlightStop,
		
		// ROUTE HOVER

		// these methods are for routes that are *not* on the map yet
		removeHoverPolyline: removeHoverPolyline,
		
		showHoverPolyline: function(encodedPolylines, color) {
			hoverPolyline = [];
			jQuery.each(encodedPolylines, function(_, encodedPolyline) {
				var points = OBA.Util.decodePolyline(encodedPolyline);
			
				var latlngs = jQuery.map(points, function(x) {
					return new google.maps.LatLng(x[0], x[1]);
				});

				var shape = new google.maps.Polyline({
					path: latlngs,
					strokeColor: "#" + color,
					strokeOpacity: 0.7,
					strokeWeight: 3,
					map: map
				});

				var hoverShape = new google.maps.Polyline({
					path: latlngs,
					strokeColor: "#" + color,
					strokeOpacity: 0.6,
					strokeWeight: 10,
					map: map
				});

				hoverPolyline.push(shape);
				hoverPolyline.push(hoverShape);
			});
		},
		
		// these methods are for routes *already on* the map
		highlightRoute: function(routeId) {
			var polylines = hoverPolylinesByRoute[routeId];
			
			if(polylines !== null) {
				jQuery.each(polylines, function(_, polyline) {
					polyline.setVisible(true);
				});
			}
		},
		
		unhighlightRoute: function(routeId) {
			var polylines = hoverPolylinesByRoute[routeId];
			
			if(polylines !== null) {
				jQuery.each(polylines, function(_, polyline) {
					polyline.setVisible(false);
				});
			}
		},
		
		// ROUTE/STOP DISPLAY
		addStop: addStop,

		addRoute: function(routeResult) {
			// already on map
			if(typeof polylinesByRoute[routeResult.id] !== 'undefined') {
				return;
			}

			jQuery.each(routeResult.directions, function(_, direction) {
				addPolylines(routeResult.id, direction.polylines, routeResult.color);
			});

			updateVehicles(routeResult.id);
		},
		
		reset: function() {
			OBA.Popups.reset();
			
			removeHoverPolyline();
			removeDisambiguationMarkers();
			
			if(locationMarker !== null) {
				locationMarker.setMap(null);
			}
			
			jQuery.each(polylinesByRoute, function(routeAndAgencyId, _) {
				if(routeAndAgencyId === null) {
					return;
				}

				removePolylines(routeAndAgencyId);
				removeVehicles(routeAndAgencyId);
				removeStops(false);
			});
		},
		
		panToRoute: function(routeId) {
			var polylines = polylinesByRoute[routeId];

			if(polylines === null
				|| typeof polylines === 'undefined'
				|| typeof polylines.length === 'undefined') {
				return;
			}
			
			var newBounds = new google.maps.LatLngBounds();
			jQuery.each(polylines, function(_, polyline) {
				if (typeof polyline !== 'undefined') { 
					var coordinates = polyline.getPath();

					for (var k=0; k < coordinates.length; k++) {
						var coordinate = coordinates.getAt(k);
						newBounds.extend(coordinate);
					}
				}
			});

			map.fitBounds(newBounds);
		},

		showPopupForStopId: function(stopId, routeFilter) {
			var stopMarker = stopsById[stopId];

			if(typeof stopMarker === 'undefined') {
				return false;
			}
			
			stopMarker.setVisible(true);

			google.maps.event.trigger(stopMarker, "click", null, routeFilter);
		},
		
		// LOCATION SEARCH
		addLocationMarker: function(latlng, address, neighborhood) {
			var markerOptions = {
				position: latlng,
		        icon: normalLocationIcon,
		        zIndex: 2,
		        title: address,
		        map: map,
		        shadow: iconShadow
			};
			
			locationMarker = new google.maps.Marker(markerOptions);

			google.maps.event.addListener(locationMarker, "click", function(mouseEvent) {
				var content = '<h3><b>' + address + '</b></h3>';

				if (neighborhood !== null) {
		    		content += neighborhood;
		    	}
	    		
				OBA.Popups.showPopupWithContent(map, locationMarker, content);
	    	});

			google.maps.event.addListener(locationMarker, "mouseover", function(mouseEvent) {
		    	locationMarker.setIcon(activeLocationIcon);
	    	});
	    	
			google.maps.event.addListener(locationMarker, "mouseout", function(mouseEvent) {
		    	locationMarker.setIcon(normalLocationIcon);
	    	});
		},
		
		showLocation: function(latlon) {
			map.panTo(latlon);			
			map.setZoom(16);
		},
		
		showBounds: function(bounds) {
			map.fitBounds(bounds);
		},
		
		// DISAMBIGUATION
		addDisambiguationMarker: function(latlng, address, neighborhood, i) {
			var locationIcon = (i !== undefined && i < 10) ? locationIcons[i] : normalLocationIcon;
			var markerOptions = {
					position: latlng,
		            icon: locationIcon,
		            zIndex: 2,
		            title: address,
		            map: map,
		            shadow: iconShadow
			};

		    var marker = new google.maps.Marker(markerOptions);
		    disambiguationMarkers.push(marker);

	    	google.maps.event.addListener(marker, "click", function(mouseEvent) {
	    		var content = '<h3><b>' + address + '</b></h3>';

	    		if(neighborhood !== null) {
	    			content += neighborhood;
	    		}

	    		OBA.Popups.showPopupWithContent(map, marker, content);
	    	});

	    	return marker;
		},
		
		highlightDisambiguationMarker: function(marker, i) {
			marker.setAnimation(google.maps.Animation.BOUNCE);

			if(i !== undefined) {
				marker.setIcon(activeLocationIcons[i]);
			} else {
				marker.setIcon(activeLocationIcon);
			}
		},

		unhighlightDisambiguationMarker: function(marker, i) {
			marker.setAnimation(null);

			if(i !== undefined) {
				marker.setIcon(locationIcons[i]);
			} else {
				marker.setIcon(normalLocationIcon);
			}
		},
		
		// WIZARD
		registerMapListener: function(listener, fx) {
			return google.maps.event.addListener(map, listener, fx);
		},

		unregisterMapListener: function(registeredName) {
			google.maps.event.removeListener(registeredName);
		},
		
		registerStopBubbleListener: function(obj, trigger) {
			return OBA.Popups.registerStopBubbleListener(obj, trigger);
		},

		unregisterStopBubbleListener: function() {
			return OBA.Popups.unregisterStopBubbleListener();
		}
	};
};