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
var oba_where_standard_plan = function(data) {

	var mapParams = {};
	mapParams.lat = 47.606828;
	mapParams.lon = -122.332505;
	mapParams.zoom = 12;

	var map = OBA.Maps.map(data.mapElement, mapParams);
	var infoWindow = new google.maps.InfoWindow();

	var boolParams = [ 'useRealTime' ];

	var floatParams = [ 'resultCount', 'lookaheadTime', 'walkSpeed', 'walkReluctance', 'maxWalkingDistance',
			'initialWaitReluctance', 'waitReluctance', 'minTransferTime',
			'transferCost', 'maxTransfers', 'maxComputationTime' ];

	/***************************************************************************
	 * 
	 **************************************************************************/

	var configureOptionsToggle = function() {

		var toggleElement = jQuery('#optionsToggleAnchor');
		var advancedOptionsElement = jQuery('#advancedSearchOptions');

		toggleElement.click(function() {
			var isVis = advancedOptionsElement.is(':visible');
			toggleElement.text(isVis ? 'Show Options' : 'Hide Options');
			advancedOptionsElement.slideToggle();
		});
	};

	configureOptionsToggle();

	/***************************************************************************
	 * 
	 **************************************************************************/

	var fromElement = data.fromElement;
	var toElement = data.toElement;
	var focusedElement = null;

	fromElement.focusin(function() {
		focusedElement = fromElement;
	});
	fromElement.focusout(function() {
		focusedElement = null;
	});

	toElement.focusin(function() {
		focusedElement = toElement;
	});
	toElement.focusout(function() {
		focusedElement = null;
	});

	google.maps.event.addListener(map, "click", function(o) {
		var latlng = o.latLng;
		if (latlng && focusedElement) {
			var txt = latlng.lat() + ',' + latlng.lng();
			focusedElement.val(txt);
		}
	});

	/***************************************************************************
	 * 
	 **************************************************************************/

	var configureTime = function() {
		var now = new Date();
		var dateString = OBA.L10n.formatDate('MM/dd/yy', now);
		var timeString = OBA.L10n.formatDate('hh:mmaa', now);

		jQuery('#date').val(dateString);
		jQuery('#time').val(timeString);
	};

	configureTime();

	/***************************************************************************
	 * 
	 **************************************************************************/

	var parseLocation = function() {
		var params = {};
		var href = window.location.href;
		var index = href.indexOf('?');
		if (index == -1)
			return params;
		href = href.slice(index + 1);
		index = href.indexOf('#');
		if (index != -1)
			href = href.slice(0, index);
		var tokens = href.split('&');
		jQuery.each(tokens, function() {
			var kvp = this.split('=');
			var key = decodeURIComponent(kvp[0]);
			var value = decodeURIComponent(kvp[1]);
			if (!(key in params))
				params[key] = [];
			params[key].push(value);
		});

		return params;
	};

	var getParam = function(params, name) {
		if (!(name in params))
			return undefined;
		var val = params[name];
		if (!val)
			return undefined;
		val = val[0];
		return val;
	};

	var setParamsFromLocation = function() {

		var params = parseLocation();

		var from = getParam(params, 'from');
		if (from)
			fromElement.val(from);

		var to = getParam(params, 'to');
		if (to)
			toElement.val(to);

		var dateAndTime = getParam(params, 'dateAndTime');
		if (dateAndTime) {
			var tokens = dateAndTime.split(' ');
			jQuery('#date').val(tokens[0]);
			jQuery('#time').val(tokens[1]);
		}

		jQuery.each(boolParams, function() {
			var val = getParam(params, this);
			if (val)
				jQuery('#' + this).attr('checked', val == 'true');
		});

		jQuery.each(floatParams, function() {
			var val = getParam(params, this);
			if (val)
				jQuery('#' + this).val(val);
		});

	};

	setParamsFromLocation();

	/***************************************************************************
	 * 
	 **************************************************************************/

	var planOverlays = [];
	var resultsPanel = data.resultsPanel;
	var bounds = new google.maps.LatLngBounds();

	var itinerariesPanel = null;
	var itineraryPanel = null;
	var itinerariesEntry = null;
	
	var clearExistingPlan = function() {

		resultsPanel.empty();
		
		itinerariesPanel = jQuery("<div/>");
		itineraryPanel = jQuery("<div/>");
		
		itinerariesPanel.appendTo(resultsPanel);
		itineraryPanel.appendTo(resultsPanel);
		
		itinerariesEntry = null;		
	};

	var itinerariesHandler = function(entry) {
		
		itinerariesEntry = entry;
		jQuery.each(entry.itineraries, itinerarySummaryHandler);
	};

	var itinerarySummaryHandler = function(index) {
		
		var itinerary = this;

		var content = jQuery('.itinerarySummaryTemplate').clone();
		content.removeClass('itinerarySummaryTemplate');
		content.addClass('itinerarySummary');
		
		var indexElement = content.find('.itinerarySummary-index');
		indexElement.text((index+1)+'.');
		
		var timeLengthElement = content.find('.itinerarySummary-timeLength');
		var mins = Math.round((itinerary.endTime - itinerary.startTime) / (1000*60));
		timeLengthElement.text(mins + ' mins');
		
		var timeRangeElement = content.find('.itinerarySummary-timeRange');
		
		var startTime = OBA.L10n.formatDate('hh:mm AA', new Date(itinerary.startTime));
		var startTimeElement = timeRangeElement.find(".startTime");
		startTimeElement.text(startTime);

		var endTime = OBA.L10n.formatDate('hh:mm AA', new Date(itinerary.endTime));
		var endTimeElement = timeRangeElement.find(".endTime");
		endTimeElement.text(endTime);		
		
		content.click(function() {
			itineraryHandler(itinerary);
		});
		
		content.show();
		content.appendTo(itinerariesPanel);
	};
	
	var itineraryHandler = function(itinerary){
		
		jQuery.each(planOverlays, function() {
			this.setMap(null);
		});
		planOverlays = [];
		bounds = new google.maps.LatLngBounds();
		itineraryPanel.empty();
		
		if (itinerariesEntry.from) {
			var from = itinerariesEntry.from;
			var location = new google.maps.LatLng(from.location.lat,
					from.location.lon);
			var startIconUrl = OBA.Resources.Map['RouteStart.png'];
			var marker = new google.maps.Marker({
				position : location,
				map : map,
				icon : startIconUrl,
				clickable : false
			});
			planOverlays.push(marker);
			bounds.extend(location);
		}

		if (itinerariesEntry.to) {
			var to = itinerariesEntry.to;
			var location = new google.maps.LatLng(to.location.lat,
					to.location.lon);
			var endIconUrl = OBA.Resources.Map['RouteEnd.png'];
			var marker = new google.maps.Marker({
				position : location,
				map : map,
				icon : endIconUrl,
				clickable : false
			});
			planOverlays.push(marker);
			bounds.extend(location);
		}
		
		var legs = itinerary.legs || [];

		jQuery.each(legs, function(index) {
			var leg = this;
			var prevLeg = undefined;
			if (index > 0)
				prevLeg = legs[index - 1];
			var nextLeg = undefined;
			if (index + 1 < legs.length)
				nextLeg = legs[index + 1];
			legHandler(leg, prevLeg, nextLeg);
		});

		if (!bounds.isEmpty())
			map.fitBounds(bounds);
	};
	
	var legHandler = function(leg, prevLeg, nextLeg) {

		if (leg.transitLeg) {
			transitLegHandler(leg, leg.transitLeg);
		}

		var streetLegs = leg.streetLegs || [];
		if (streetLegs.length > 0) {
			streetLegsHandler(leg, streetLegs, prevLeg, nextLeg);
		}
	};

	var transitLegHandler = function(leg, transitLeg) {

		var path = transitLeg.path;
		if (path) {
			var points = OBA.Maps.decodePolyline(path);
			var opts = {
				path : points,
				strokeColor : '#0000FF',
				strokeWeight : 4,
				strokeOpacity : 0.5,
				zIndex : 1
			};
			var line = new google.maps.Polyline(opts);
			line.setMap(map);
			planOverlays.push(line);

			jQuery.each(points, function() {
				bounds.extend(this);
			});
		}

		var content = jQuery('.transitLegTemplate').clone();
		content.removeClass('transitLegTemplate');
		content.addClass('transitLeg');

		var stopNameElement = content.find(".stopName");
		var stopLinkElement = content.find(".stopLink");

		var fromStop = transitLeg.fromStop;

		if (fromStop) {
			stopNameElement.text(fromStop.name);

			var point = new google.maps.LatLng(fromStop.lat, fromStop.lon);
			var icon = OBA.Maps.getWhiteCircle14MarkerImage();
			var fromStopMarker = new google.maps.Marker({
				position : point,
				icon : icon,
				map : map
			});

			planOverlays.push(fromStopMarker);

			var href = stopLinkElement.attr('href');
			href = href.replace(/STOP_ID/, fromStop.id);
			stopLinkElement.attr('href', href);

		} else {
			stopNameElement.text("continues as");
			stopNameElement.removeClass('stopNameEmphasized');

			stopLinkElement.hide();
		}

		var routeShortNameElement = content.find(".routeShortName");
		var routeShortName = transitLeg.routeShortName
				|| transitLeg.trip.route.shortName;
		routeShortNameElement.text(routeShortName);

		var tripHeadsignElement = content.find(".tripHeadsign");
		var tripHeadsign = transitLeg.tripHeadsign
				|| transitLeg.trip.tripHeadsign;
		tripHeadsignElement.text(tripHeadsign);

		var startTime = OBA.L10n
				.formatDate('hh:mm AA', new Date(leg.startTime));
		var startTimeElement = content.find(".startTime");
		startTimeElement.text(startTime);

		var endTime = OBA.L10n.formatDate('hh:mm AA', new Date(leg.endTime));
		var endTimeElement = content.find(".endTime");
		endTimeElement.text(endTime);

		var mins = Math.ceil((leg.endTime - leg.startTime) / (60 * 1000));
		var elapsedTimeElement = content.find(".elapsedTime");
		elapsedTimeElement.text('(' + mins + ' mins)');

		var legStatus = content.find(".legStatus");

		if (transitLeg.predictedDepartureTime != 0) {
			var label = computeRealTimeLabel(transitLeg.predictedDepartureTime,
					transitLeg.scheduledDepartureTime);
			legStatus.text(label);
			var labelClass = computeRealTimeClass(
					transitLeg.predictedDepartureTime,
					transitLeg.scheduledDepartureTime);
			legStatus.addClass(labelClass);
		} else {
			legStatus.hide();
		}

		content.show();
		content.appendTo(itineraryPanel);
	};

	/***************************************************************************
	 * 
	 **************************************************************************/

	var computeRealTimeLabel = function(predicted, scheduled) {

		var now = (new Date()).getTime();
		var diff = ((predicted - scheduled) / (1000.0 * 60));
		var minutes = Math.abs(Math.round(diff));

		var m = OBA.Resources.ArrivalAndDepartureMessages;

		var pastTense = predicted < now;

		if (diff < -1.5) {
			if (pastTense)
				return OBA.L10n.format(m.departedEarly, minutes);
			else
				return OBA.L10n.format(m.early, minutes);
		} else if (diff < 1.5) {
			if (pastTense)
				return m.departedOnTime;
			else
				return m.onTime;
		} else {
			if (pastTense)
				return OBA.L10n.format(m.departedLate, minutes);
			else
				return OBA.L10n.format(m.delayed, minutes);
		}

	};

	var computeRealTimeClass = function(predicted, scheduled) {

		var diff = ((predicted - scheduled) / (1000.0 * 60));

		if (diff < -1.5) {
			return "statusEarly";
		} else if (diff < 1.5) {
			return "statusOnTime";
		} else {
			return "statusDelayed";
		}
	};

	/***************************************************************************
	 * 
	 **************************************************************************/

	var streetLegsHandler = function(leg, streetLegs, prevLeg, nextLeg) {

		var content = jQuery('.streetLegTemplate').clone();
		content.removeClass('streetLegTemplate');
		content.addClass('streetLeg');

		var fromLabel = null;
		if (prevLeg && prevLeg.transitLeg && prevLeg.transitLeg.toStop)
			fromLabel = prevLeg.transitLeg.toStop.name;
		var fromLabelElement = content.find(".walkFromLocation");
		if (fromLabel != null)
			fromLabelElement.text(OBA.L10n.format(fromLabelElement.text(),
					fromLabel));
		else
			fromLabelElement.hide();

		var toLabel = 'destination';
		if (nextLeg && nextLeg.transitLeg && nextLeg.transitLeg.fromStop)
			toLabel = nextLeg.transitLeg.fromStop.name;
		var toLabelElement = content.find(".walkToLocation");
		toLabelElement.text(OBA.L10n.format(toLabelElement.text(), toLabel));

		var mins = Math.ceil((leg.endTime - leg.startTime) / (60 * 1000));
		var timeElement = content.find('.walkToTime');
		timeElement.text(OBA.L10n.format(timeElement.text(), mins));

		content.show();
		content.appendTo(itineraryPanel);

		var points = [];

		jQuery.each(streetLegs, function() {
			if (this.path) {
				var legPoints = OBA.Maps.decodePolyline(this.path);
				points = points.concat(legPoints);
			}
		});

		if (points.length > 0) {
			var opts = {
				path : points,
				strokeColor : '#000000',
				zIndex : 0
			};
			var line = new google.maps.Polyline(opts);
			line.setMap(map);
			planOverlays.push(line);

			jQuery.each(points, function() {
				bounds.extend(this);
			});
		}
	};

	var planHandler = function(entry) {

		itinerariesHandler(entry);

		var itineraries = entry.itineraries || [];
		if (itineraries.length == 0)
			return;
		itineraryHandler(itineraries[0]);
	};

	var constructParams = function() {

		var params = {};

		var from = fromElement.val();
		var to = toElement.val();

		params.from = from;
		params.to = to;

		var fromTokens = from.split(',');
		var toTokens = to.split(',');

		params.latFrom = fromTokens[0];
		params.lonFrom = fromTokens[1];
		params.latTo = toTokens[0];
		params.lonTo = toTokens[1];
		params.dateAndTime = jQuery('#date').val() + ' '
				+ jQuery('#time').val();

		params.arriveBy = jQuery('#arriveBy').val() == 'arriveBy';

		params.useRealTime = jQuery('#useRealtime').is(':checked');

		if (jQuery('#walkOnly').is(':checked')) {
			params.mode = 'walk';
		}

		jQuery.each(floatParams, function() {
			var val = jQuery('#' + this).val();
			if (val.length > 0)
				params[this] = parseFloat(val);
		});

		return params;
	};

	var createUrlForParams = function(params) {

		var parent = jQuery('<div/>');
		parent.appendTo(resultsPanel);
		parent.addClass('linkToThisSearchPanel');

		var link = jQuery('<a/>');
		link.text('Link to this search');

		var href = window.location.href;
		var index = href.indexOf('?');
		if (index != -1)
			href = href.slice(0, index);

		var first = true;
		for ( var key in params) {
			href += first ? '?' : '&';
			first = false;
			href += encodeURIComponent(key) + '='
					+ encodeURIComponent(params[key]);
		}

		link.attr('href', href);
		link.appendTo(parent);
	};

	var directionsButton = data.directionsButton;

	directionsButton.click(function() {

		clearExistingPlan();

		var params = constructParams();
		createUrlForParams(params);

		OBA.Api.planTrip(params, planHandler);
	});
};