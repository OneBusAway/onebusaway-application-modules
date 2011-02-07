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
			// Come on now 1
			for( var name in otherParams) {
				params[name] = otherParams[name];
			}
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
	
	var createEntryHandler = function(callback, errorCallback, postProcess) {
		var entryHandler = function(json) {
			// Eventually, we will do smart handling of references here
			var references = processReferences(json.references);
			var entry = json.entry;
			if( postProcess )
				postProcess(entry, references);
			callback(entry);
		};

		return createHandler(entryHandler, errorCallback);
	};

	var createListHandler = function(callback, errorCallback, postProcess) {
		var listHandler = function(json) {
			// Eventually, we will do smart handling of references here
			var references = processReferences(json.references);
			var list = json.list;
			if( postProcess ) {
				for ( var i = 0; i < list.length; i++) {
					postProcess(list[i], references);
				}
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
		
		jQuery.each(references.routes, function(){
			processRoute(this, references);
		});
		
		jQuery.each(references.stops, function(){
			processStop(this, references);
		});
		
		jQuery.each(references.trips, function(){
			processTrip(this, references);
		});
		
		return references;
	};

	var processAgencyWithCoverage = function(awc, references) {
		awc.agency = references.agenciesById[awc.agencyId];
	};
	
	var processArrivalAndDepartureForStop = function(entry, references) {
		
	};
	
	var processItineraries = function(entry, references) {
		var itineraries = entry.itineraries || [];
		jQuery.each(itineraries,function() {
			var legs = this.legs || [];
			jQuery.each(legs, function() {
				
				var transitLeg = this.transitLeg;
				
				if( ! transitLeg )
					return;
				
				transitLeg.fromStop = references.stopsById[transitLeg.fromStopId];
				transitLeg.toStop = references.stopsById[transitLeg.toStopId];
				transitLeg.trip = references.tripsById[transitLeg.tripId];  
			});
		});
	}
	
	var processItinerary = function(entry, references) {
		
	}
	
	var processRoute = function(route, references ) {
		var agency = references.agenciesById[route.agencyId];
		if( agency )
			route.agency = agency;
	};
	
	var processStop = function(stop, references) {
		var routes = new Array();
		stop.routes = routes;
		jQuery.each(stop.routeIds,function() {
			var route = references.routesById[this];
			if( route )
				routes.push(route);
		});
	};
	
	var processStopsForRoute = function(stopsForRoute, references) {
		
	};
	
	var processStreetGraphForRegion = function(entry, references) {
		
		var verticesById = {};
		var vertices = entry.vertices || [];
		
		jQuery.each(vertices,function() {
			verticesById[this.id] = this;
		});
		
		var edges = entry.edges || [];
		
		jQuery.each(edges,function() {
			var from = verticesById[this.fromId];
			if( from )
				this.from = from;
			var to = verticesById[this.toId];
			if( to )
				this.to = to;
		});
		
		
	};
	
	var processTrip = function(trip, references) {
		var route = references.routesById[trip.routeId];
		if( route )
			trip.route = route;
	};
	
	var processTripDetails = function(tripDetails, references) {
		var trip = references.tripsById[tripDetails.tripId];
		if( trip )
			tripDetails.trip = trip;
		if( tripDetails.schedule )
			processTripSchedule( tripDetails.schedule, references);
		if( tripDetails.status )
			processTripStatus( tripDetails.status );
	};
	
	var processTripSchedule = function(schedule, references) {
		
		var stopTimes = schedule.stopTimes;
		if( stopTimes ) {
			jQuery.each(stopTimes, function() {
				processTripStopTime(this,references);
			});
		}
		var prevTrip = references.tripsById[schedule.previousTripId];
		if( prevTrip )
			schedule.previousTrip = prevTrip;
		
		var nextTrip = references.tripsById[schedule.nextTripId];
		if( nextTrip )
			schedule.nextTrip = nextTrip;
	};
	
	var processTripStopTime = function(stopTime, references) {
		var stop = references.stopsById[stopTime.stopId];
		if( stop )
			stopTime.stop = stop;
	};
	
	var processTripStatus = function(tripStatus, references) {
		
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
	
	that.arrivalAndDepartureForStop = function(userParams, callback, errorCallback) {
		var url = createUrl('/where/arrival-and-departure-for-stop/' + userParams.stopId + '.json');
		var params = createParams(userParams);
		var handler = createEntryHandler(callback, errorCallback,
				processArrivalAndDepartureForStop);
		jQuery.getJSON(url, params, handler);
	};
	
	that.planTrip = function(params, callback, errorCallback) {
		var url = createUrl('/where/plan-trip.json');
		params = createParams(params);
		var handler = createEntryHandler(callback, errorCallback, processItineraries);
		jQuery.getJSON(url, params, handler);
	};
	
	that.shape = function(shapeId, callback, errorCallback) {
		var url = createUrl('/where/shape/' + shapeId + '.json');
		var params = createParams();
		var handler = createEntryHandler(callback, errorCallback);
		jQuery.getJSON(url, params, handler);
	};
	
	that.stop = function(stopId, callback, errorCallback) {
		var url = createUrl('/where/stop/' + stopId + '.json');
		var params = createParams();
		var handler = createEntryHandler(callback, errorCallback, processStop);
		jQuery.getJSON(url, params, handler);
	};
	
	that.stopsForRoute = function(routeId, callback, errorCallback) {
		var url = createUrl('/where/stops-for-route/' + routeId + '.json');
		var params = createParams();
		var handler = createEntryHandler(callback, errorCallback,
				processStopsForRoute);
		jQuery.getJSON(url, params, handler);
	};
	
	that.streetGraphForRegion = function(bounds, callback, errorCallback) {
		
		if( bounds.isEmpty() )
			return;
		
		var ne = bounds.getNorthEast();
		var sw = bounds.getSouthWest();
		
		var params = {};
		params.latFrom = sw.lat();
		params.lonFrom = sw.lng();
		params.latTo = ne.lat();
		params.lonTo = ne.lng();
		params = createParams(params);
		
		var url = createUrl('/where/street-graph-for-region.json');
		
		var handler = createEntryHandler(callback, errorCallback,
				processStreetGraphForRegion);
		
		jQuery.getJSON(url, params, handler);
	};
	
	that.tripDetails = function(params, callback, errorCallback) {
		var url = createUrl('/where/trip-details/' + params.tripId + '.json');
		var params = createParams(params);
		var handler = createEntryHandler(callback, errorCallback, processTripDetails);
		jQuery.getJSON(url, params, handler);
	};
	
	return that;
};

OBA.Api = obaApiFactory();