var OBA = window.OBA || {};

/*******************************************************************************
 * Stops For Region Service
 ******************************************************************************/

OBA.StopsForRegionService = function() {

	const
	MAX_STOP_COUNT_PER_REGION = 200;

	var pendingOps = [];
	var callback = undefined;
	var cache = {};
	var sentToClient = {};
	var latStep = undefined;
	var lonStep = undefined;

	var that = {};

	/***************************************************************************
	 * Public Methods
	 **************************************************************************/

	that.stopsForRegion = function(bounds, handler) {

		pendingOps = [];
		sentToClient = {};
		callback = handler;

		/**
		 * Make sure we've set our latStep and lonStep properly
		 */
		checkSteps(bounds);

		/**
		 * We attempt to load the visible region directly at first
		 */
		exploreVisibleRegion(bounds);

		/**
		 * We follow up by pre-fetching the buffered region
		 */
		exploreBufferedRegion(bounds);

		/**
		 * Events queued up, we fire off any pending events
		 */
		checkPending();
	};

	/***************************************************************************
	 * Private Methods
	 **************************************************************************/

	var checkSteps = function(bounds) {

		if (latStep != undefined && lonStep != undefined)
			return;

		var p = bounds.getSouthWest();
		var steps = OBA.Maps.boundsForPointAndRadius(p, 200);
		var sw = steps.getSouthWest();
		var ne = steps.getNorthEast();

		latStep = snap(ne.lat() - sw.lat(), 1e3);
		lonStep = snap(ne.lng() - sw.lng(), 1e3);
	};

	var exploreVisibleRegion = function(bounds) {

		var min = bounds.getSouthWest();
		var max = bounds.getNorthEast();

		var minLat = floor(min.lat(), latStep);
		var maxLat = floor(max.lat(), latStep);
		var minLon = floor(min.lng(), lonStep);
		var maxLon = floor(max.lng(), lonStep);

		var dirtyRegion = new google.maps.LatLngBounds();
		var dirtyRegions = [];

		for ( var lat = minLat; lat <= maxLat; lat += latStep) {
			for ( var lon = minLon; lon <= maxLon; lon += lonStep) {
				var region = snapBounds(lat, lon, lat + latStep, lon + lonStep);
				var regionCache = cache[hashRegion(region)];
				if (regionCache == undefined) {
					dirtyRegion.union(region);
					dirtyRegions.push(region);
				} else {
					handleRegion(region, regionCache);
				}
			}
		}

		if (dirtyRegions.length > 0) {
			var op = new MultiRegionOp(dirtyRegion, dirtyRegions);
			addOp(op);
		}
	};

	var exploreBufferedRegion = function(bounds) {

		var min = bounds.getSouthWest();
		var max = bounds.getNorthEast();

		var minLat = floor(min.lat() - latStep, latStep);
		var maxLat = floor(max.lat() + latStep, latStep);
		var minLon = floor(min.lng() - lonStep, lonStep);
		var maxLon = floor(max.lng() + lonStep, lonStep);

		for ( var lat = minLat; lat <= maxLat; lat += latStep) {
			for ( var lon = minLon; lon <= maxLon; lon += lonStep) {
				var region = snapBounds(lat, lon, lat + latStep, lon + lonStep);
				checkRegion(region);
			}
		}
	};

	var checkRegion = function(region) {
		var regionCache = cache[hashRegion(region)];
		if (regionCache == undefined) {
			var regionOp = RegionOp(region);
			addOp(regionOp);
		} else {
			handleRegion(region, regionCache);
		}
	};

	var addOp = function(op) {
		pendingOps.push(op);
	};

	var handleRegion = function(region, regionCache) {

		var key = hashRegion(region);

		// Only send stops to client if we haven't already
		if (sentToClient[key])
			return;

		// There were too many stops in the region, so jump to sub-regions
		if (regionCache.hasOverflow()) {
			jQuery.each(splitRegion(region), function() {
				checkRegion(this);
			});
			return;
		}

		var stopsInView = [];

		jQuery.each(regionCache.getStops(), function() {
			stopsInView.push(this);
		});

		sentToClient[key] = true;

		// And only if we have stops to show
		if (stopsInView.length > 0)
			callback(stopsInView);
	};

	var checkPending = function() {

		if (pendingOps.length == 0)
			return;

		var regionOp = pendingOps.shift();
		var region = regionOp.getRequestRegion();

		var regionCache = cache[hashRegion(region)];
		if (regionCache != undefined) {
			handleRegion(region, regionCache);
			return;
		}

		var min = region.getSouthWest();
		var max = region.getNorthEast();

		var params = {};
		params.lat = (min.lat() + max.lat()) / 2;
		params.lon = (min.lng() + max.lng()) / 2;
		params.latSpan = max.lat() - min.lat();
		params.lonSpan = max.lng() - min.lng();
		params.maxCount = MAX_STOP_COUNT_PER_REGION;

		OBA.Api.stopsForLocation(params, function(stops) {
			stopHandler(stops, regionOp);
		});
	};

	/***************************************************************************
	 * Private Static Methods
	 **************************************************************************/

	var splitRegion = function(region) {

		var splits = [];

		var min = region.getSouthWest();
		var max = region.getNorthEast();

		var minLat = min.lat();
		var maxLat = max.lat();
		var minLon = min.lng();
		var maxLon = max.lng();
		var centerLat = (minLat + maxLat) / 2;
		var centerLon = (minLon + maxLon) / 2;

		splits.push(snapBounds(minLat, minLon, centerLat, centerLon));
		splits.push(snapBounds(minLat, centerLon, centerLat, maxLon));
		splits.push(snapBounds(centerLat, minLon, maxLat, centerLon));
		splits.push(snapBounds(centerLat, centerLon, maxLat, maxLon));

		return splits;
	};

	var floor = function(value, step) {
		return Math.floor(value / step) * step;
	};

	var snapBounds = function(latMin, lonMin, latMax, lonMax) {
		latMin = snap(latMin, 1e5);
		lonMin = snap(lonMin, 1e5);
		latMax = snap(latMax, 1e5);
		lonMax = snap(lonMax, 1e5);
		var p1 = new google.maps.LatLng(latMin, lonMin);
		var p2 = new google.maps.LatLng(latMax, lonMax);
		return new google.maps.LatLngBounds(p1, p2);
	};

	var snap = function(latOrLon, factor) {
		return Math.round(latOrLon * factor) / factor;
	};

	var hashRegion = function(region) {
		return region.toUrlValue();
	};

	var stopHandler = function(stops, regionOp) {

		if (stops.limitExceeded) {

			var requestRegion = regionOp.getRequestRegion();
			var key = hashRegion(requestRegion);
			cache[key] = RegionCache([], true);
			jQuery.each(regionOp.getSplitRegions(), function() {
				checkRegion(this);
			});

		} else {

			var stopsByRegion = getStopsByActualRegion(stops, regionOp);
			jQuery.each(stopsByRegion, function() {
				var region = this.region;
				var stops = this.stops;
				var regionCache = RegionCache(stops, false);
				var key = hashRegion(region);
				cache[key] = regionCache;
				handleRegion(region, regionCache);
			});
		}

		checkPending();
	};

	var getStopsByActualRegion = function(stops, regionOp) {

		var stopsByBounds = {};

		var actualRegions = regionOp.getActualRegions();

		// Make sure each region has a stop list, even if it ultimately has no
		// stops (want to cache that fact too)
		jQuery.each(actualRegions, function() {
			var key = hashRegion(this);
			stopsByBounds[key] = {
				region : this,
				stops : []
			};
		});

		jQuery.each(stops.list, function() {
			var stop = this;
			var p = new google.maps.LatLng(stop.lat, stop.lon);
			jQuery.each(actualRegions, function() {
				if (this.contains(p)) {
					var key = hashRegion(this);
					var record = stopsByBounds[key];
					var stopsForRegion = record.stops;
					stopsForRegion.push(stop);
				}
			});
		});

		var asList = [];
		jQuery.each(stopsByBounds, function(k, v) {
			asList.push(v);
		})
		return asList;
	};

	var RegionOp = function(region) {

		var that = {};

		that.getRequestRegion = function() {
			return region;
		};

		that.getActualRegions = function() {
			return [ region ];
		};

		that.getSplitRegions = function() {
			return splitRegion(region);
		};

		return that;
	};

	var MultiRegionOp = function(region, actualRegions) {

		var that = RegionOp(region);

		that.getActualRegions = function() {
			return actualRegions;
		};

		that.getSplitRegions = function() {
			return actualRegions;
		};

		return that;
	};

	var RegionCache = function(stops, overflow) {

		var that = {};

		that.getStops = function() {
			return stops;
		};

		that.hasOverflow = function() {
			return overflow;
		};

		return that;
	};

	return that;
};

/*******************************************************************************
 * Transit Map Methods
 ******************************************************************************/

OBA.TransitMap = function(map, params) {

	var _needsRefresh = false;
	var _visibleStopsById = {};
	var markerManager = OBA.Maps.markerManager(map);
	var stopsForRegionService = OBA.StopsForRegionService();
	var _stopClickHandler = params.stopClickHandler;
	
	var _showStopsInCurrentView = true;
	var _stopIdsToAlwaysShow = {};
	var _otherOverlays = [];
	var _selectedRoute = undefined;

	google.maps.event.addListener(map, "bounds_changed", function() {
		_needsRefresh = true;
	});

	google.maps.event.addListener(map, "idle", function() {

		if (!_needsRefresh)
			return;

		refresh();

		_needsRefresh = false;
	});
	
	var reset = function(showStopsInCurrentView, stopsToAlwaysShow) {

	    _showStopsInCurrentView = showStopsInCurrentView;

	    _stopIdsToAlwaysShow = {};
	    jQuery.each(stopsToAlwaysShow, function() {
	    	_stopIdsToAlwaysShow[this.id] = true;
	    });

	    jQuery.each(_otherOverlays, function() {
	    	this.setMap(null);
	    });
	    _otherOverlays = [];
	    
	    jQuery.each(_visibleStopsById, function(stopId, stopAndOverlays) {
	    	jQuery.each(stopAndOverlays.overlays, function() {
				markerManager.removeMarker(this);
			});
		});
	    _visibleStopsById = {};

	    jQuery.each(stopsToAlwaysShow, function() {
	    	stopHandler(this);
	    });

	    _selectedRoute = null;

	    refresh();
	  }

	var refresh = function() {

		if( _showStopsInCurrentView ) { 
			refreshStopsInCurrentView();
		}
		else {
			
			var toRemove = [];
			
			jQuery.each(_visibleStopsById, function(stopId, stopAndOverlays) {
				if( stopId in _stopIdsToAlwaysShow)
					return;
		    	jQuery.each(stopAndOverlays.overlays, function() {
					markerManager.removeMarker(this);
				});
		    	toRemove.push(stopId);
			});
			
			jQuery.each(toRemove, function() {
				delete _visibleStopsById[this];
			});
		}
	};
	
	var refreshStopsInCurrentView = function() {
		
		if (map.getZoom() < 17) {
			return;
		}

		var bounds = map.getBounds();

		var toRemove = [];

		jQuery.each(_visibleStopsById, function(stopId, stopAndOverlays) {
			var stop = stopAndOverlays.stop;
			var p = new google.maps.LatLng(stop.lat, stop.lon);
			if (!bounds.contains(p)) {
				toRemove.push(stop.id);
				jQuery.each(stopAndOverlays.overlays, function() {
					markerManager.removeMarker(this);
				});
			}
		});

		jQuery.each(toRemove, function() {
			delete _visibleStopsById[this];
		});

		stopsForRegionService.stopsForRegion(bounds, function(stops) {
			jQuery.each(stops, function() {
				stopHandler(this);
			});
		});
	};

	var stopHandler = function(stop) {

		if (_visibleStopsById[stop.id])
			return;

		var stopAndOverlays = {};
		stopAndOverlays.stop = stop;
		stopAndOverlays.overlays = OBA.Maps.addStopToMarkerManager(stop,
				markerManager);
		_visibleStopsById[stop.id] = stopAndOverlays;

		jQuery.each(stopAndOverlays.overlays, function() {
			if (_stopClickHandler != undefined) {
				google.maps.event.addListener(this, 'click', function() {
					_stopClickHandler(stop);
				});
			}
		});
	};
	
	var showStopsAndPaths = function(stops, polylines) {
		
		reset(false, stops);
		
		var bounds = new google.maps.LatLngBounds();
		
		jQuery.each(polylines, function() {
			var points = OBA.Maps.decodePolyline(this.points);
			var opts = {
				path : points,
				strokeColor : '#4F64BA',
				strokeWeight : 3,
				strokeOpacity : 1.0,
				zIndex : 1
			};
			var line = new google.maps.Polyline(opts);
			line.setMap(map);

			jQuery.each(points, function() {
				bounds.extend(this);
			});
		});

		jQuery.each(stops, function() {
			bounds.extend(new google.maps.LatLng(this.lat,this.lon));
		});
		
		if( ! bounds.isEmpty() )
			map.fitBounds(bounds);
	};

	/***************************************************************************
	 * Public Methods
	 **************************************************************************/

	var that = {};
	
	that.showStopsInCurrentView = function() {
		reset(true,[]);
	}
	
	that.showStopsForRoute = function(stopsForRoute) {
		var stops = stopsForRoute.stops || [];
		var polylines = stopsForRoute.polylines || [];
		showStopsAndPaths(stops, polylines);
		_selectedRoute = stopsForRoute.route;
	};
	
	that.showStopsForRouteAndDirection = function(stopsForRoute, directionId) {
		
		var stopGroupings = stopsForRoute.stopGroupings || [];
		jQuery.each(stopGroupings, function() {
			if( this.type != 'direction')
				return;
			jQuery.each(this.stopGroups,function() {
				var stopGroup = this;
				if( stopGroup.id != directionId )
					return;
				var stops = stopGroup.stops || [];
				var polylines = stopGroup.polylines || [];
				showStopsAndPaths(stops, polylines);
				_selectedRoute = stopsForRoute.route;
			});
		});
	};

	that.showStopsForRouteId = function(routeId) {
		OBA.Api.stopsForRoute(routeId,function(stopsForRoute){
			that.showStopsForRoute(stopsForRoute);
		});
	};

	that.showStopsForRouteAndDirectionId = function(routeId, directionId) {
		OBA.Api.stopsForRoute(routeId,function(){
			that.showStopsForRouteAndDirection(stopsForRoute, directionId);
		});
	};

	return that;
};