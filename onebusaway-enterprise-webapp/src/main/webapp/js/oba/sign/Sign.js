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

OBA.Sign = function() {
	
	var refreshInterval = 30;
	var timeout = 30;
	var scrollSpeed = 2; // pixels per frame.  1 or 2 are good defaults
	var configurableMessageHtml = null;
	var stopIdsToRequest = null;
	var stopInfo = {};
	var routeInfo = {};
	var vehiclesPerStop = 3;
	var sortByRoute = false; // otherwise sort by arrival distance/departure time
	var showHeader = false;
	var lineRef = "";
	var showVehicleIds = false;

	var url = window.location.href;
	var signPosition = url.indexOf("/sign/sign");
	var baseUrl = url.substring(0, signPosition);

	var hostname = window.location.hostname;
	var hostnamePosition = url.indexOf(hostname);
    var obaApiBaseUrlPosition = url.indexOf("/", hostnamePosition);

    var obaApiBaseUrl = "";
	if (OBA.Config.apiBaseUrl.startsWith("/")) {
		// if we have a relative path, guess it as our api path as well
		obaApiBaseUrl = url.substring(0, obaApiBaseUrlPosition);
	} else {
		// the api tier is configured on a separate tier
		// OBA.Config.apiBaseUrl will fully specify the path and the variable should be empty
		obaApiBaseUrl = "";
	}

	var setupUITimeout = null;
	var disableAlerts = false;

	function getParameterByName(name, defaultValue) {
		name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
		var regexS = "[\\?&]"+name+"=([^&#]*)";
		var regex = new RegExp(regexS);
		var results = regex.exec(window.location.href);
		if(results === null) {
			return defaultValue;
		} else {
			return decodeURIComponent(results[1].replace(/\+/g, " "));
		}
	}

	function detectSize() {
		var wrappedWindow = jQuery(window);
		var h = wrappedWindow.height();
		var w = wrappedWindow.width();

		if(w > h) {
			if(h >= 1150) {
				jQuery('body').removeClass().addClass('landscape').addClass('size1200');
			} else if(h >= 1000) {
				jQuery('body').removeClass().addClass('landscape').addClass('size1000');
			} else if(h >= 700) {
				jQuery('body').removeClass().addClass('landscape').addClass('size700');
			} else {
				jQuery('body').removeClass().addClass('landscape');
			}
		} else {
			if(h >= 1900) {
				jQuery('body').removeClass().addClass('portrait').addClass('size1900');
			} else if(h >= 1500) {
				jQuery('body').removeClass().addClass('portrait').addClass('size1600');
			} else if(h >= 1200) {
				jQuery('body').removeClass().addClass('portrait').addClass('size1200');
			} else if(h >= 1000) {
				jQuery('body').removeClass().addClass('portrait').addClass('size1000');
			} else if(h >= 700) {
				jQuery('body').removeClass().addClass('portrait').addClass('size700');
			} else {
				jQuery('body').removeClass().addClass('portrait');
			}
		}
	}

	function setupUI() {

		jQuery.fx.interval = 50;

		// configure interface with URL params
		refreshInterval = getParameterByName("refresh", refreshInterval);
		var disableAlertsStr = getParameterByName("disableAlerts", "false");
		disableAlerts = (disableAlertsStr.toLowerCase() == "true");

		scrollSpeed = parseInt(getParameterByName("scrollSpeed", 2));

		vehiclesPerStop = getParameterByName("vehiclesPerStop", vehiclesPerStop);

		var sortByRouteStr = getParameterByName("sortByRoute", "false");
		sortByRoute = (sortByRouteStr.toLowerCase() == "true");
		var showHeaderStr = getParameterByName("showHeader", "false");
		showHeader = (showHeaderStr.toLowerCase() == "true");

		showVehicleIds = getParameterByName("showVehicleIds", "false");

		var fontSize = getParameterByName("fontSize", null);
		if (fontSize) {
			$("body, body input, body textarea").attr("style", "font-size: " + fontSize + "em;")
		}
		lineRef = getParameterByName("routeId", "");

		timeout = refreshInterval;

		configurableMessageHtml = getParameterByName("message", configurableMessageHtml);
		if(configurableMessageHtml !== null) {
			var header = jQuery("#branding");

			jQuery("<p></p>")
					.attr("id", "message")
					.text(configurableMessageHtml)
					.appendTo(header);
		}

		// add event handlers
		detectSize();
		jQuery.event.add(window, "resize", detectSize);

		// setup error handling/timeout
		jQuery.ajaxSetup({
			"error": showError,
			"timeout": 60000, // All ajax calls timeout after one minute.
			"cache": false
		});

		jQuery("#content").html("").empty();
		jQuery("#pager").html("").empty();

		var stopIds = getParameterByName("stopIds", null);
		if(stopIds !== null && stopIds.length > 0) {
			stopIdsToRequest = [];
			jQuery.each(stopIds.split(","), function(_, o) {
				var stopIdParts = o.split("_");
				if(stopIdParts.length === 2) {
					stopIdsToRequest.push({ agency : stopIdParts[0], id : stopIdParts[1] });
				} else {
					stopIdsToRequest.push({ agency : OBA.Config.signDefaultAgencyId, id : o });
				}
			});
		} else {
			showError("No stops are configured for display.");
			return;
		}

		// This can probably be done in a way cooler way
		var initMonitor = function() {};
		initMonitor.count = stopIdsToRequest.length;
		initMonitor.done = function() {
			initMonitor.count -= 1;
			if (initMonitor.count === 0) {
				advance();
			}
		};

		jQuery.each(stopIdsToRequest, function(_, stopId) {
			jQuery("#pager").append('<span id="' + stopId.id + '" class="dot"></span>');
			initStop(stopId, initMonitor);
		});

        var stopTotal = 0;
        jQuery.each(stopIdsToRequest, function(_, stopId){
            stopTotal++;
            jQuery("#totalpage").text(stopTotal);

        });

        updateClock();
		setInterval(updateClock, 10 * 1000);
	}

	function advance() {
		var idToDisplay = stopIdsToRequest.shift();
		stopIdsToRequest.push(idToDisplay);
		update(idToDisplay);
	}

	function updateClock() {
		// try and get date from server
		jQuery.ajax({
                dataType: "json",
                url: obaApiBaseUrl + OBA.Config.timeApiUrl,
                success: function (json, textStatus, jqXHR) {
                    var now;
                    if (json.data == null) {
                        //console.log("call to time server failed: url=" + obaApiBaseUrl + OBA.Config.timeApiUrl);
                        now = new Date();
                    } else {
                        now = new Date(json.data.entry.time);
                    }
                    jQuery("#clock #time").text(now.format("h:MM TT"));
                    jQuery("#clock #day").text(now.format("dddd, mmmm d"));
                },
                error: function (jqxhr, textStatus, error) {
                    now = new Date();
                    jQuery("#clock #time").text(now.format("h:MM TT"));
                    jQuery("#clock #day").text(now.format("dddd, mmmm d"));
                }
            }
			);

	}

	function initStop(stopId, initMonitor) {
		var stopIdStr = stopId.agency + "_" + stopId.id;
		var params = { stopId: stopIdStr, "type": "json", "key": "OBAKEY" };
		jQuery.getJSON(baseUrl + "/" + OBA.Config.stopForId, params, function(json) {
			stopInfo[stopIdStr] = json.stop;
			jQuery.each(json.stop.routesAvailable, function(_, route) {
				routeInfo[route.id] = route;
			});
			initMonitor.done();
		});
	}

	function signForRoute(routeId, tripShortName, element) {
		var sign = element.addClass("sign");
		sign.css("border-left-color", routeInfo[routeId].color);
		sign.css("border-left-style", "solid");
		var shortName = routeInfo[routeId].shortName;
		if (shortName == null || shortName == "")
			shortName = routeId.split("_")[1];
		if (tripShortName != null && tripShortName != "") {
			// use the trip override
            sign.text(tripShortName);
        } else {
            sign.text(shortName);
        }
		return sign;
	}

	function getNewElementForStop(stopId) {
		var stopIdStr = stopId.agency + "_" + stopId.id;
		var stopName = "Stop " + stopIdStr;
		if (stopInfo[stopIdStr] != null) {
			stopName = stopInfo[stopIdStr].name;
		} else {
			console.log("missing stopInfo for " + stopIdStr);
		}
		var newElement = jQuery(
			'<div>' +
				'<div class="error"></div>' +
				'<div class="header">' +
					'<div class="name"><h1>' + stopName + '</h1></div>' +
					' <div class="stop-id"><h2>Stop #' + stopId.id + '</h2></div>' +
				'</div>' +
				'<div class="arrivals"><div class="arrivals_scroller"><table></table></div></div>' +
				'<div class="alerts"><div class="alerts_header"><h2>Service Change Notices</h2></div><div id="stop' + stopId.id + '" class="scroller"></div></div>' +
			'</div>').addClass("slide");

		_gaq.push(['_trackEvent', "DIY Sign", "Add Stop", stopId.id]);

		return newElement;
	}

	function updateElementForStop(stopId, stopElement, sortOrderAndRouteIdAndHeadsign, headsignToDistanceAways, applicableSituations) {
		if(stopElement === null) {
			return;
		}

		var tableBody = stopElement.find("table");
		tableBody.html("").empty();
		if (showHeader) {
			var thead = "<thead><tr class='headerFirstRow'><th class='routeHeader'>Route</th><th class='headsignHeader'>Destination</th><th colspan='2' class='nextArrivalHeader'>Next Arrivals</th></tr></thead>"
			tableBody.append(thead);
		}

		// situations
		stopElement.find(".alerts .scroller").html("").empty();
		if (disableAlerts || jQuery.isEmptyObject(applicableSituations)) {
			stopElement.find(".alerts").hide();
			stopElement.find(".arrivals").width("100%");
		} else if (isValidAlert(applicableSituations)) {

			stopElement.find(".alerts").show();
			stopElement.find(".arrivals").width("70%");

			stopElement.find("div.scroller").html("").empty();

			// Probably not needed because we are starting from scratch
			stopElement.find(".alerts_body").html("").empty().remove();

			jQuery.each(applicableSituations, function(situationId, situation) {

				var signWrapper = jQuery("<div></div>").addClass("sign_wrapper");

				var existingSigns = [];

				// this loop grabs the route name if present
				if (typeof situation.Affects.VehicleJourneys != "undefined") {
					jQuery.each(situation.Affects.VehicleJourneys.AffectedVehicleJourney, function(_, journey) {
						if (journey.LineRef in routeInfo && jQuery.inArray(journey.LineRef, existingSigns) < 0) {
							var sign = signForRoute(journey.LineRef, null, jQuery("<div></div>"));
							sign.addClass("alert_sign");
							signWrapper.append(sign);
							existingSigns.push(journey.LineRef);
						}
					});
				}

				// even if route name/ affected journeys not present we still display the alert
				var alert = jQuery("<div></div>")
								.addClass("alert")
								.html('<p class="alert_summary">' + situation.Summary.replace(/\n\n/g, "<br/><br/>").replace(/\n/g, " ") + '</p><p>' + situation.Description.replace(/$/g, "<br/><br/>").replace(/\n\n/g, "<br/><br/>").replace(/\n/g, " ") + '</p>');

				alert.prepend(signWrapper);

				stopElement.find("div.scroller").append(alert);
			});
		}

		// arrivals
		var r = 0;
		var table = [];

		jQuery.each(sortOrderAndRouteIdAndHeadsign, function(index, routeIdAndHeadsignArray) {
			var routeIdAndHeadsign = routeIdAndHeadsignArray.routeIdAndHeadsign;
			var distanceAways = headsignToDistanceAways[routeIdAndHeadsign];
			var headsign = routeIdAndHeadsign.split("_")[2];
			var row = {
					etaCount : 0,
					vehicleInfo : null,
					lineRef : null,
					tripShortName : null, // if the trip has an override route name (Express)
					headsign : headsign,
					vehicleId : null,
					monitored : false,
					timePredictions : [],
					distanceAways : [],
					etas: []
			};
			row.etaCount = 0;
			table.push(row);

			jQuery.each(distanceAways, function(_, vehicleInfo) {
				if((_ + 1) > vehiclesPerStop) {
					return;
				}

				if (row.etaCount == 0) {
					row.vehicleInfo = vehicleInfo;
					row.lineRef = vehicleInfo.lineRef;
					row.tripShortName = vehicleInfo.tripShortName;
					row.vehicleId = vehicleInfo.vehicleId;
					row.monitored = vehicleInfo.monitored;
				}
				row.etaCount++;
				if (typeof vehicleInfo.timePrediction != 'undefined') {
					row.timePredictions.push(vehicleInfo.timePrediction);
					row.etas.push(vehicleInfo.timePrediction);
				} else {
					row.etas.push(vehicleInfo.distanceAway);
				}
				row.distanceAways.push(vehicleInfo.distanceAway);

			});
			r++;
		});


		jQuery.each(table, function(_, rowInfo) {
				var row = jQuery("<tr></tr>");

				if((_ + 1) === table.length) {
					row.addClass("last");
				}

				// sign
				var sign = signForRoute(rowInfo.lineRef, rowInfo.tripShortName, jQuery("<td></td>"));
				sign.addClass("arrival_sign");
				sign.appendTo(row);

				// name cell
				var spanTxt = ""
				if (showVehicleIds == "true") {
					spanTxt = " Vehicle #" + rowInfo.vehicleId;
				}

	            var vehicleIdSpan = jQuery("<span></span>")
					.addClass("bus-id")
					.text(spanTxt);

				jQuery('<td class="stopLocation"></td>')
					.text(rowInfo.headsign)
					.append(vehicleIdSpan)
					.appendTo(row);

				var distanceTableData = jQuery('<td></td>')
					.addClass('distance');
				var etaDiv = jQuery('<div></div>')
					.addClass('eta');

				var etaTextPara = jQuery('<p></p>')
					.addClass('etaText')
					.append(rowInfo.etas[0]);

				if (rowInfo.monitored && typeof realtimeIcon !== 'undefined') {
					etaDiv.append(realtimeIcon);
				}
				etaDiv.append(etaTextPara);
				distanceTableData.append(etaDiv);

				var additionalStopsDiv = jQuery('<div></div>')
					.addClass("additional_stops")
					.append(toEtaSpan1(rowInfo.etas, rowInfo.monitored));

				distanceTableData.append(additionalStopsDiv);

				distanceTableData.appendTo(row);

				tableBody.append(row);
		});

		// no arrivals
		if(r === 0) {
			stopElement.find("thead").html("").empty(); // clear out the header when there is no data to show
			jQuery('<tr class="last">' +
					'<td colspan="4">' +
						'No vehicles en-route to this stop. Please check back shortly for an update.</li>' +
					'</td>' +
				   '</tr>')
				   .appendTo(tableBody);
		}

		stopElement.find('tbody tr:even').addClass('even');
		stopElement.find('tbody tr:odd').addClass('odd');

	}

	function isValidAlert(applicableSituations) {
		var found = false;
		jQuery.each(applicableSituations, function(situationId, situation) {
			if (typeof situation.Affects.VehicleJourneys != "undefined"
				&& typeof situation.Affects.VehicleJourneys.AffectedVehicleJourney != "undefined") {
				jQuery.each(situation.Affects.VehicleJourneys.AffectedVehicleJourney, function(_, journey) {
					// journey.LineRef does not have an agency ID
					for (routeId in routeInfo) {
						if (trimAgencyId(routeId) == journey.LineRef) {
							found = true;
						}
					}
				});
			}
		});

		if (!found) {
			// check for a global service alert
            jQuery.each(applicableSituations, function(situationId, situation) {
                if (typeof situation.Affects.Operators != "undefined") {
                    jQuery.each(situation.Affects.Operators, function (_, operator) {
                        found = true;
                    });
                }
            });
        }
		return found;
	}

	function trimAgencyId(id) {
		return id.split("_")[1];
	}

	// from jQuery 1.7
	function isNumeric(n) {
		var number = !jQuery.isArray( n ) && (n - parseFloat( n ) + 1) >= 0;
		return number;
	}

	function hasLeadingNumber(n) {
		return leadingNumber(n) != null;
	}

	function leadingNumber(n) {
		var leadingNumberRegex = /^\d+/g;

		var result = leadingNumberRegex.exec(n);
		if (result == null) return null;
		return result[0];
	}

	function toEtaSpan1(etas, monitored) {
		var list = '<div class="prediction">';
		for (index = 1; index < etas.length; index++) {
			if (index == 1) {
				list = list + etas[index];
			} else {
				list = list + "<br/>" + etas[index];
			}
		}
		list += "</div>";
		return list;
	}


	function updateTimestamp(date) {
		jQuery("#lastupdated")
			.html("")
			.remove();

		jQuery("<span></span>")
			.attr("id", "lastupdated")
			.text("Last updated " + date.format("mmm d, yyyy h:MM:ss TT"))
			.appendTo("#footer");
	}

	function showError(textStatus) {

		jQuery("#content").html("").empty();

		var error = jQuery("<div></div>").attr("id", "error");
		jQuery("<p></p>").html(typeof textStatus === 'string' ? textStatus : "An error occurred while updating arrival information&mdash;please check back later.").appendTo(error);

		jQuery("#content").append(error);

		clearTimeout(setupUITimeout);
		setupUITimeout = setTimeout(setupUI, 30000);
	}

	function hideError() {
		jQuery("#error")
			.children()
			.html("")
			.remove();
		jQuery("#pager").show();
	}

	function containsSortKeys(value, array) {
		var found = false;
		jQuery.each(array, function(index, sortKey) {
			if (sortKey.routeIdAndHeadsign == value) {
				found = true;
				return true;
			}
		});
		return found;
	}

	function update(stopId) {

		var carousel = jQuery('.jcarousel ul');
		if(stopId === null) {
			return;
		}

		var stopElement = getNewElementForStop(stopId);

		var params = { OperatorRef: stopId.agency, MonitoringRef: stopId.id, StopMonitoringDetailLevel: "normal", MinimumStopVisitsPerLine: 3 };
		if (lineRef) {
			params.LineRef = lineRef;
		}

		jQuery.getJSON(obaApiBaseUrl + OBA.Config.siriSMUrl, params, function(json) {
			//updateTimestamp(OBA.Util.ISO8601StringToDate(json.Siri.ServiceDelivery.ResponseTimestamp));
			//hideError();

			var situationsById = {};
			if (json == null) return;
			if(typeof json.Siri.ServiceDelivery.SituationExchangeDelivery !== 'undefined' && json.Siri.ServiceDelivery.SituationExchangeDelivery.length > 0) {
				jQuery.each(json.Siri.ServiceDelivery.SituationExchangeDelivery[0].Situations.PtSituationElement, function(_, situationElement) {
					situationsById[situationElement.SituationNumber] = situationElement;
				});
			}

			var headsignToDistanceAways = {};
			var sortedArrivalTimes = [];
			var sortedRouteIds = [];

			var applicableSituations = {};
			var r = 0;
			jQuery.each(json.Siri.ServiceDelivery.StopMonitoringDelivery[0].MonitoredStopVisit, function(_, monitoredStopVisit) {
				var journey = monitoredStopVisit.MonitoredVehicleJourney;

				if(typeof journey.MonitoredCall === 'undefined') {
					return;
				}

				var routeId = journey.LineRef;
				var routeIdParts = routeId.split("_");
				var routeIdWithoutAgency = routeIdParts[1];

				var headsign = journey.DestinationName;
				var routeIdAndHeadsign = routeId + "_" + headsign;
				var updateTimestampReference = OBA.Util.ISO8601StringToDate(json.Siri.ServiceDelivery.ResponseTimestamp).getTime();
				var journeyTimestamp = updateTimestampReference;
				if (monitoredStopVisit.RecordedAtTime != 'undefined') {
					// if we have a stop visit time use it, its more accurate
					journeyTimestamp = OBA.Util.ISO8601StringToDate(monitoredStopVisit.RecordedAtTime).getTime();
				}
				if(typeof headsignToDistanceAways[routeIdAndHeadsign] === 'undefined') {
					headsignToDistanceAways[routeIdAndHeadsign] = [];
					r++;
				}
				if (typeof journey.SituationRef === 'undefined') {
					console.log("issue with journey");
				} else {
					jQuery.each(journey.SituationRef, function (_, situationRef) {
						if (typeof situationsById[situationRef.SituationSimpleRef] !== 'undefined') {
							applicableSituations[situationRef.SituationSimpleRef] = situationsById[situationRef.SituationSimpleRef];
						}
					});
				}

				var vehicleInfo = {};
				vehicleInfo.distanceAway = journey.MonitoredCall.Extensions.Distances.PresentableDistance;
				vehicleInfo.vehicleId = journey.VehicleRef.split("_")[1];
				vehicleInfo.lineRef = journey.LineRef;
				vehicleInfo.tripShortName = null;
				vehicleInfo.monitored = journey.Monitored;
				if (typeof journey.MonitoredCall.ExpectedDepartureTime != 'undefined') {
					//vehicleInfo.expectedDepartureTime = OBA.Util.ISO8601StringToDate(journey.MonitoredCall.ExpectedDepartureTime);
					vehicleInfo.timePrediction = OBA.Util.getArrivalEstimateForISOString(
							journey.MonitoredCall.ExpectedArrivalTime,
							journeyTimestamp, "min");

				}

                // trip may have a route override like an express designator
				if (journey.PublishedLineName != null) {
                    vehicleInfo.tripShortName = journey.PublishedLineName;
                }

                headsignToDistanceAways[routeIdAndHeadsign].push(vehicleInfo);
				var routeShortName = routeInfo[routeId].shortName;
				var sortKeys = {
						routeIdAndHeadsign: routeIdAndHeadsign,
						routeShortName: routeShortName

				};

				if (!containsSortKeys(routeIdAndHeadsign, sortedArrivalTimes)) {
					sortedArrivalTimes.push(sortKeys);
				}
				if (!containsSortKeys(routeIdAndHeadsign, sortedRouteIds)) {
					sortedRouteIds.push(sortKeys);
				}
			});

			// now look for stop-based service alerts
			if (json.Siri.ServiceDelivery.SituationExchangeDelivery != null
			       && json.Siri.ServiceDelivery.SituationExchangeDelivery.length > 0) {
                jQuery.each(json.Siri.ServiceDelivery.SituationExchangeDelivery[0].Situations.PtSituationElement, function (_, ptSituationElement) {
                    if (typeof situationsById[ptSituationElement.SituationNumber] !== 'undefined') {
                        applicableSituations[ptSituationElement.SituationNumber] = ptSituationElement;
                    }
                });
            }


            var sortOrderAndRouteIdAndHeadsign = sortedArrivalTimes;
			// default is to sort by arrival times / arrival distances
			if (sortByRoute) {
				  sortedRouteIds.sort(function(a, b) {
					     var routeShortNameA=a.routeShortName;
					     if (hasLeadingNumber(routeShortNameA))
					     	routeShortNameA = leadingNumber(routeShortNameA);
						 var routeShortNameB=b.routeShortName;
					     if (hasLeadingNumber(routeShortNameB))
					     	routeShortNameB = leadingNumber(routeShortNameB);

						 // perform a true numeric sort if possible
						 if (isNumeric(routeShortNameA) && isNumeric(routeShortNameB)) {
							 return routeShortNameA - routeShortNameB;
						 }
						 // try to handle "21" vs "21E" vs "A Line" in a reasonable/stable manner
						 if (isNumeric(routeShortNameA)) {
                             return  -1 * routeShortNameB.localeCompare(parseInt(routeShortNameA));
                         }

						 if (isNumeric(routeShortNameB)) {
                             return routeShortNameA.localeCompare(parseInt(routeShortNameB));
                         }

                         // by here we are comparing txt to txt "A Line" to "B Line"
						 routeShortNameA = routeShortNameA.toLowerCase();
						 routeShortNameB = routeShortNameB.toLowerCase();
						 // otherwise to string-based sort
						 if (routeShortNameA < routeShortNameB) //sort string ascending
						  return -1;
						 if (routeShortNameA > routeShortNameB)
						  return 1;
						 return 0; //default return value (no sorting)

				  });
				  sortOrderAndRouteIdAndHeadsign = sortedRouteIds;
				}


			// update table for this stop ID
			updateElementForStop(stopId, stopElement, sortOrderAndRouteIdAndHeadsign, headsignToDistanceAways, applicableSituations);

			var oldContent = jQuery("#content");

			var newContent = jQuery('<div id="content"></div>');
			stopElement.appendTo(newContent);

			jQuery("body").prepend(newContent);

			var header = jQuery(".header");
			if (header[0].offsetHeight <  header[0].scrollHeight)
				header.simplyScroll({
					customClass: 'title_header'
			 	});

			if (stopIdsToRequest.length < 2) {
				newContent.css("bottom", "80px").css("margin-bottom", "0px");
				jQuery("#pager").hide();
			}

			var maxSignWidth = Math.max.apply(Math, newContent.find('.alert_sign').map(function() { return $(this).width(); }).get());

			newContent.find('.alert_sign').width(maxSignWidth);

			var contentWidth = oldContent.width();

			// If we only have one stop to display, make the animation 0 so there is
			// effectively no animation. The content just changes.
			var animationSpeed = 0;
			if (stopIdsToRequest.length > 1) {
				animationSpeed = 2000;
			}

			oldContent.animate({left: contentWidth}, animationSpeed, function() {
				oldContent.html("").empty().remove();
				jQuery("span.dot").css('background-color', 'rgb(90,90,90)');
				jQuery("span#" + stopId.id + ".dot").css('background-color', 'white');

                var currentStopText = "N/A";
                var stopList = getParameterByName("stopIds", null);
                if (stopList !== null && stopList.length > 0){
                    var stopArray = [];
                    jQuery.each(stopList.split(","), function(_, o){
                        var stopIdParts = o.split("_");
                        if (stopIdParts.length === 2) {
                            stopArray.push(stopIdParts[1]);
                        }
                    });
                }

                for (var i = 0; i < stopArray.length; i ++){
                    if (stopArray[i] === stopId.id) {
                        currentStopText = i + 1;
                    }
                }

                jQuery("span#currentpage").text(currentStopText);

				var alerts = stopElement.find(".alert");
				var totalHeight = 0;

				jQuery.each(alerts, function(_, alert) {
					totalHeight += jQuery(alert).height();
				});

				if (totalHeight > stopElement.find(".scroller").height()) {

					stopElement.find(".alert:last").css("margin-bottom", "30px").css("border-bottom", "2px dashed rgb(200,200,200)");

					stopElement.find(".scroller").simplyScroll({
						customClass: 'alerts_body',
						orientation: 'vertical',
						pauseOnHover: false,
						autoMode: 'loop',
						frameRate: 20,
						speed: scrollSpeed
					});
				}
				
				var arrivalsHeight = 0;
				alerts = stopElement.find(".arrivals");
				jQuery.each(alerts, function(_, arrival) {
					arrivalsHeight += jQuery(arrival).height();
				});

				
				var scrollerHeight = stopElement.find(".arrivals_scroller").height();
				if (arrivalsHeight < scrollerHeight) {
					// scroll the arrivals
					stopElement.find(".arrivals_scroller").simplyScroll({
						customClass: 'arrivals_body',
						orientation: 'vertical',
						pauseOnHover: false,
						autoMode: 'loop',
						frameRate: 20,
						speed: scrollSpeed
					});
				}
				
				setTimeout(advance, refreshInterval * 1000);
			});
			
		}); // ajax()			
	}
	
	return {
		initialize: function() {			
			setupUI();			
		}
	};
};

jQuery(document).ready(function() { OBA.Sign().initialize(); });