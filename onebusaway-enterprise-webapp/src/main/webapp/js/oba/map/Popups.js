/*
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var OBA = window.OBA || {};

// do not add constructor params here!
OBA.Popups = (function() {	

	var infoWindow = null;

	var refreshPopupRequest = null;
	
	var stopBubbleListener = null, stopBubbleTrigger = null;
	
	function closeInfoWindow() {
		if(infoWindow !== null) {
			infoWindow.close();
		}
		infoWindow = null;
	}
	
	// PUBLIC METHODS
	function showPopupWithContent(map, marker, content) {
		closeInfoWindow();
		
		infoWindow = new google.maps.InfoWindow({
		    	pixelOffset: new google.maps.Size(0, (marker.getIcon().size.height / 2)),
		    	disableAutoPan: false
		});
		
		google.maps.event.addListener(infoWindow, "closeclick", closeInfoWindow);

		infoWindow.setContent(content);
		infoWindow.open(map, marker);
	}
	
	function showPopupWithContentFromRequest(map, marker, url, params, contentFn, routeFilter) {
		closeInfoWindow();
		
		infoWindow = new google.maps.InfoWindow({
	    	pixelOffset: new google.maps.Size(0, (marker.getIcon().size.height / 2)),
	    	disableAutoPan: false,
	    	stopId: marker.stopId // to lock an icon on the map when a popup is open for it
		});

		google.maps.event.addListener(infoWindow, "closeclick", closeInfoWindow);

		var popupContainerId = "container" + Math.floor(Math.random() * 1000000);
		var refreshFn = function(openBubble) {
			// pass a new "now" time for debugging if we're given one
			if(OBA.Config.time !== null) {
				params.time = OBA.Config.time;
			}
			
			if(refreshPopupRequest !== null) {
				refreshPopupRequest.abort();
				openBubble = true;
			}
			refreshPopupRequest = jQuery.getJSON(url, params, function(json) {
				if(infoWindow === null) {
					return;
				}
				
				infoWindow.setContent(contentFn(json, popupContainerId, marker, routeFilter));
				
				if(openBubble === true) {
					infoWindow.open(map, marker);
				}
				
				// hack to prevent scrollbars in the IEs
				var sizeChanged = false;
				var content = jQuery("#" + popupContainerId);
				if(content.height() > 300) {
					content.css("overflow-y", "scroll")
							.css("height", "280");
					sizeChanged = true;
				}
				if(content.width() > 500) {
					content.css("overflow-x", "hidden")
							.css("width", "480");
					sizeChanged = true;
				}
				if(sizeChanged) {
					infoWindow.setContent(content.get(0));
					infoWindow.open(map, marker);
				}
			});
		};
		refreshFn(true);		
		infoWindow.refreshFn = refreshFn;	

		var updateTimestamp = function() {
			var timestampContainer = jQuery("#" + popupContainerId).find(".updated");
			
			if(timestampContainer.length === 0) {
				return;
			}
			
			var age = parseInt(timestampContainer.attr("age"), 10);
			var referenceEpoch = parseInt(timestampContainer.attr("referenceEpoch"), 10);
			var newAge = age + ((new Date().getTime() - referenceEpoch) / 1000);
			timestampContainer.text("Data updated " + OBA.Util.displayTime(newAge));
		};
		updateTimestamp();		
		infoWindow.updateTimestamp = updateTimestamp;
	}
	
	// CONTENT GENERATION
	function getServiceAlerts(r, situationRefs) {
	    var html = '';

	    var situationIds = {};
        var situationRefsCount = 0; 
        if (situationRefs != null) {
            jQuery.each(situationRefs, function(_, situation) {
                situationIds[situation.SituationSimpleRef] = true;
                situationRefsCount += 1;
            });
        }
        
        if (situationRefs == null || situationRefsCount > 0) {
            if (r.Siri.ServiceDelivery.SituationExchangeDelivery != null && r.Siri.ServiceDelivery.SituationExchangeDelivery.length > 0) {
                jQuery.each(r.Siri.ServiceDelivery.SituationExchangeDelivery[0].Situations.PtSituationElement, function(_, ptSituationElement) {
                    var situationId = ptSituationElement.SituationNumber;
                    if (ptSituationElement.Description && (situationRefs == null || situationIds[situationId] === true)) {
                        html += '<li>' + ptSituationElement.Description.replace(/\n/g, "<br/>") + '</li>';
                    }
                });
            }
        }
        
        if (html !== '') {
            html = '<div class="serviceAlertContainer"><p class="title">Service Change:</p><ul class="alerts">' + html + '</ul></div>';
        }
        
        return html;
	}
	
	function processAlertData(situationExchangeDelivery) {
		var alertData = {};
		
		if (situationExchangeDelivery && situationExchangeDelivery.length > 0) {
            jQuery.each(situationExchangeDelivery[0].Situations.PtSituationElement, function(_, ptSituationElement) {
            	// Skip the alert if there is no VehicleJourneys... It's probably a global alert.
            	if (!ptSituationElement.Affects.hasOwnProperty('VehicleJourneys'))
            		return true;
            	jQuery.each(ptSituationElement.Affects.VehicleJourneys.AffectedVehicleJourney, function(_, affectedVehicleJourney) {
            		var lineRef = affectedVehicleJourney.LineRef;
            		if (!(lineRef in alertData)) {
            			alertData[lineRef] = {};
            		}
            		if (!(ptSituationElement.SituationNumber in alertData[lineRef])) {
            			alertData[lineRef][ptSituationElement.SituationNumber] = ptSituationElement;
            		}
            	});
            });
		}
		
		return alertData;
	}
	
	function activateAlertLinks(content) {
		var alertLinks = content.find(".alert-link");
		jQuery.each(alertLinks, function(_, alertLink) {
			var element = jQuery(alertLink);
			var idParts = element.attr("id").split("|");
			var stopId = idParts[1];
			var routeId = idParts[2];
			var routeShortName = idParts[3];
			
			element.click(function(e) {
				e.preventDefault();
				var alertElement = jQuery('#alerts-' + routeId.hashCode());
				if (alertElement.length === 0) {
					expandAlerts = true;
					jQuery.history.load(stopId + " " + routeShortName);
				} else {
					$("#searchbar").animate({
						scrollTop: alertElement.parent().offset().top - jQuery("#searchbar").offset().top + jQuery("#searchbar").scrollTop()
						}, 
						500,
						function() {
							if (alertElement.accordion("option", "active") !== 0) {
								alertElement.accordion("activate" , 0);
							} else {
								alertElement.animate(
									{ opacity : 0 },
									100,
									function() {
										alertElement.animate({ opacity : 1 }, 500, "swing");
									}
								);
							}
						});
				}
			});
			
		});
	}
	
	function getVehicleContentForResponse(r, popupContainerId, marker) {
		var alertData = processAlertData(r.Siri.ServiceDelivery.SituationExchangeDelivery);
		
		var activity = r.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity[0];
		if(activity === null || activity.MonitoredVehicleJourney === null) {
			return null;
		}

		var vehicleId = activity.MonitoredVehicleJourney.VehicleRef;
		var vehicleIdParts = vehicleId.split("_");
		var vehicleIdWithoutAgency = vehicleIdParts[1];
		var routeName = activity.MonitoredVehicleJourney.LineRef;

		var html = '<div id="' + popupContainerId + '" class="popup">';
		
		// header
		html += '<div class="header vehicle">';
		html += '<p class="title">' + activity.MonitoredVehicleJourney.PublishedLineName + " " + activity.MonitoredVehicleJourney.DestinationName + '</p><p>';
		html += '<span class="type">Vehicle #' + vehicleIdWithoutAgency + '</span>';

		var updateTimestamp = OBA.Util.ISO8601StringToDate(activity.RecordedAtTime).getTime();
		var updateTimestampReference = OBA.Util.ISO8601StringToDate(r.Siri.ServiceDelivery.ResponseTimestamp).getTime();

		var age = (parseInt(updateTimestampReference, 10) - parseInt(updateTimestamp, 10)) / 1000;
		var staleClass = ((age > OBA.Config.staleTimeout) ? " stale" : "");			

		html += '<span class="updated' + staleClass + '"' + 
				' age="' + age + '"' + 
				' referenceEpoch="' + new Date().getTime() + '"' + 
				'>Data updated ' 
				+ OBA.Util.displayTime(age) 
				+ '</span>'; 
		
		// (end header)
		html += '</p>';
		html += '</div>';
		
		// service available at stop
		if(typeof activity.MonitoredVehicleJourney.MonitoredCall === 'undefined' && (
			(typeof activity.MonitoredVehicleJourney.OnwardCalls === 'undefined'
				|| typeof activity.MonitoredVehicleJourney.OnwardCalls.OnwardCall === 'undefined') 
			|| (typeof activity.MonitoredVehicleJourney.OnwardCalls !== 'undefined' 
				&& activity.MonitoredVehicleJourney.OnwardCalls.length === 0)
			)) {

			html += '<p class="service">Next stops are not known for this vehicle.</p>';
		} else {
			if(typeof activity.MonitoredVehicleJourney.OnwardCalls !== 'undefined'
				&& typeof activity.MonitoredVehicleJourney.OnwardCalls.OnwardCall !== 'undefined') {

				html += '<p class="service">Next stops:</p>';
				html += '<ul>';			

				jQuery.each(activity.MonitoredVehicleJourney.OnwardCalls.OnwardCall, function(_, onwardCall) {
					var stopIdParts = onwardCall.StopPointRef.split("_");
					var stopIdWithoutAgencyId = stopIdParts[1];
						
					var lastClass = ((_ === activity.MonitoredVehicleJourney.OnwardCalls.OnwardCall.length - 1) ? " last" : "");

					html += '<li class="nextStop' + lastClass + '">';				
					html += '<a href="#' + stopIdWithoutAgencyId + '">' + onwardCall.StopPointName + '</a>';
					html += '<span>';
						
					if(typeof onwardCall.ExpectedArrivalTime !== 'undefined' && onwardCall.ExpectedArrivalTime !== null) {
						html += OBA.Util.getArrivalEstimateForISOString(onwardCall.ExpectedArrivalTime, updateTimestampReference);
						html += ", " + onwardCall.Extensions.Distances.PresentableDistance;
					} else {
						html += onwardCall.Extensions.Distances.PresentableDistance;
					}

					html += '</span></li>';
				});
				
				html += '</ul>';
			}
		}
		
		// service alerts
		if (routeName in alertData) {
			html += ' <a id="alert-link||' + routeName + '" class="alert-link" href="#">Service Alert for ' + activity.MonitoredVehicleJourney.PublishedLineName + '</a>';
		}
		
		html += OBA.Config.infoBubbleFooterFunction('route', activity.MonitoredVehicleJourney.PublishedLineName);
		
		html += "<ul class='links'>";
		html += "<a href='#' id='zoomHere'>Center & Zoom Here</a>";
		html += "</ul>";
		
		// (end popup)
		html += '</div>';
		
		var content = jQuery(html);
		var zoomHereLink = content.find("#zoomHere");

		zoomHereLink.click(function(e) {
			e.preventDefault();
			
			var map = marker.map;
			map.setCenter(marker.getPosition());
			map.setZoom(16);
		});
		
		activateAlertLinks(content);
		
		return content.get(0);
	}

	function getStopContentForResponse(r, popupContainerId, marker, routeFilter) {
		var siri = r.siri;
		var stopResult = r.stop;
		
		var alertData = processAlertData(r.siri.Siri.ServiceDelivery.SituationExchangeDelivery);

		var html = '<div id="' + popupContainerId + '" class="popup">';
		
		// header
		var stopId = stopResult.id;
		var stopIdParts = stopId.split("_");
		var stopIdWithoutAgency = stopIdParts[1];
		
		html += '<div class="header stop">';
		html += '<p class="title">' + stopResult.name + '</p><p>';
		html += '<span class="type">Stopcode ' + stopIdWithoutAgency + '</span>';
		
		// update time across all arrivals
		var updateTimestampReference = OBA.Util.ISO8601StringToDate(siri.Siri.ServiceDelivery.ResponseTimestamp).getTime();
		var maxUpdateTimestamp = null;
		jQuery.each(siri.Siri.ServiceDelivery.StopMonitoringDelivery[0].MonitoredStopVisit, function(_, monitoredJourney) {
			var updateTimestamp = OBA.Util.ISO8601StringToDate(monitoredJourney.RecordedAtTime).getTime();
			if(updateTimestamp > maxUpdateTimestamp) {
				maxUpdateTimestamp = updateTimestamp;
			}
		});	
		
		if (maxUpdateTimestamp === null) {
			maxUpdateTimestamp = updateTimestampReference;
		}
		
		if(maxUpdateTimestamp !== null) {
			var age = (parseInt(updateTimestampReference, 10) - parseInt(maxUpdateTimestamp, 10)) / 1000;
			var staleClass = ((age > OBA.Config.staleTimeout) ? " stale" : "");

			html += '<span class="updated' + staleClass + '"' + 
					' age="' + age + '"' + 
					' referenceEpoch="' + new Date().getTime() + '"' + 
					'>Data updated ' 
					+ OBA.Util.displayTime(age) 
					+ '</span>'; 
		}
		
		// (end header)
		html += '  </p>';
		html += ' </div>';
		
	    var routeAndDirectionWithArrivals = {};
	    var routeAndDirectionWithArrivalsCount = 0;
	    var routeAndDirectionWithoutArrivals = {};
	    var routeAndDirectionWithoutArrivalsCount = 0;
	    var routeAndDirectionWithoutSerivce = {};
	    var routeAndDirectionWithoutSerivceCount = 0;
	    var totalRouteCount = 0;
		
		var filterExistsInResults = false;
		
		jQuery.each(stopResult.routesAvailable, function(_, routeResult) {
			if (routeResult.shortName === routeFilter) {
				filterExistsInResults = true;
				return false;
			}
		});
		
	    // break up routes here between those with and without service
		var filteredMatches = jQuery("<div></div>");
		var filteredMatchesData = jQuery('<div></div>').addClass("popup-filtered-matches");
		filteredMatches.append(filteredMatchesData);
		filteredMatchesData.append(jQuery("<h2></h2>").text("Other Routes Here:").addClass("service"));
		filteredMatchesData.append("<ul></ul>");
	    
		jQuery.each(stopResult.routesAvailable, function(_, route) {
	    	if (filterExistsInResults && route.shortName !== routeFilter) {
	    		var filteredMatch = jQuery("<li></li>").addClass("filtered-match");
	    		var link = jQuery('<a href="#' + stopResult.id.match(/\d*$/) + '%20' + route.shortName + '"><span class="route-name">' + route.shortName + '</span></a>');
	    		link.appendTo(filteredMatch);
	    		filteredMatches.find("ul").append(filteredMatch);
	    		return true; //continue
	    	}
	    	
	    	jQuery.each(route.directions, function(__, direction) {
	    		if(direction.hasUpcomingScheduledService === false) {
	    			routeAndDirectionWithoutSerivce[route.id + "_" + direction.directionId] = { "id":route.id, "shortName":route.shortName, "destination":direction.destination };
	    			routeAndDirectionWithoutSerivceCount++;
	    		} else {
	    			routeAndDirectionWithoutArrivals[route.id + "_" + direction.directionId + "_" + direction.destination.hashCode()] = { "id":route.id, "shortName":route.shortName, "destination":direction.destination };
	    			routeAndDirectionWithoutArrivalsCount++;
	    		}
	    	});
	    	totalRouteCount++;
	    });
	    
	    // ...now those with and without arrivals
	    var visits = siri.Siri.ServiceDelivery.StopMonitoringDelivery[0].MonitoredStopVisit;
	    jQuery.each(visits, function(_, monitoredJourney) {
			var routeId = monitoredJourney.MonitoredVehicleJourney.LineRef;
			var routeShortName = monitoredJourney.MonitoredVehicleJourney.PublishedLineName;
			
			if (filterExistsInResults && routeShortName !== routeFilter) {
				return true; //continue
			}
			
			var directionId = monitoredJourney.MonitoredVehicleJourney.DirectionRef;
			var destinationNameHash = monitoredJourney.MonitoredVehicleJourney.DestinationName.hashCode();

			if(typeof routeAndDirectionWithArrivals[routeId + "_" + directionId + "_" + destinationNameHash] === 'undefined') {
				routeAndDirectionWithArrivals[routeId + "_" + directionId + "_" + destinationNameHash] = [];
				delete routeAndDirectionWithoutArrivals[routeId + "_" + directionId + "_" + destinationNameHash];
				routeAndDirectionWithoutArrivalsCount--;
			}

			routeAndDirectionWithArrivals[routeId + "_" + directionId + "_" + destinationNameHash].push(monitoredJourney.MonitoredVehicleJourney);
			routeAndDirectionWithArrivalsCount++;
		});	    
	    
	    // service available
		var maxObservationsToShow = 3;
		if(totalRouteCount > 5) {
			maxObservationsToShow = 1;
		} else if(totalRouteCount > 3) {
			maxObservationsToShow = 2;
		}	

		if(routeAndDirectionWithArrivalsCount > 0) {
		    html += '<p class="service">Buses en-route:</p>';

			jQuery.each(routeAndDirectionWithArrivals, function(_, mvjs) {
				var mvj = mvjs[0];

				html += '<ul>';

				html += '<li class="route">';
				html += '<a href="#' + stopIdWithoutAgency + '%20' + mvj.PublishedLineName + '"><span class="route-name">' + mvj.PublishedLineName + "</span>&nbsp;&nbsp; " + mvj.DestinationName + '</a>';
				if (mvj.LineRef in alertData) {
					html += ' <a id="alert-link|' + stopIdWithoutAgency + '|' + mvj.LineRef + '|' + mvj.PublishedLineName + '" class="alert-link" href="#">Alert</a>';
				}
				html += '</li>';

				jQuery.each(mvjs, function(_, monitoredVehicleJourney) {
					if(_ >= maxObservationsToShow) {
						return false;
					}

					if(typeof monitoredVehicleJourney.MonitoredCall !== 'undefined') {
						var distance = monitoredVehicleJourney.MonitoredCall.Extensions.Distances.PresentableDistance;
						
						var timePrediction = null;
						if(typeof monitoredVehicleJourney.MonitoredCall.ExpectedArrivalTime !== 'undefined' 
							&& monitoredVehicleJourney.MonitoredCall.ExpectedArrivalTime !== null) {
							timePrediction = OBA.Util.getArrivalEstimateForISOString(
									monitoredVehicleJourney.MonitoredCall.ExpectedArrivalTime, 
									updateTimestampReference);
						}

						var wrapped = false;
						if(typeof monitoredVehicleJourney.ProgressStatus !== 'undefined' 
							&& monitoredVehicleJourney.ProgressStatus.indexOf("prevTrip") !== -1) {
							wrapped = true;
						}

						var layover = false;
						if(typeof monitoredVehicleJourney.ProgressStatus !== 'undefined' 
							&& monitoredVehicleJourney.ProgressStatus.indexOf("layover") !== -1) {
							layover = true;
						}

						var stalled = false;
						if(typeof monitoredVehicleJourney.ProgressRate !== 'undefined' 
							&& monitoredVehicleJourney.ProgressRate === "noProgress") {
							stalled = true;
						}

						// time mode
						if(timePrediction != null && stalled === false) {
							if(wrapped === false) {
								timePrediction += ", " + distance;
							}
							
							var lastClass = ((_ === maxObservationsToShow - 1 || _ === mvjs.length - 1) ? " last" : "");
							html += '<li class="arrival' + lastClass + '">' + timePrediction + '</li>';

						// distance mode
						} else {
							if(layover === true) {
								if(typeof monitoredVehicleJourney.OriginAimedDepartureTime !== 'undefined') {
									var departureTime = OBA.Util.ISO8601StringToDate(monitoredVehicleJourney.OriginAimedDepartureTime);

									if(departureTime.getTime() < updateTimestampReference) {
										distance += " <span class='not_bold'>(at terminal)</span>";
									} else {
										distance += " <span class='not_bold'>(at terminal, scheduled to depart " + departureTime.format("h:MM TT") + ")</span>";
									}
								} else {
									distance += " <span class='not_bold'>(at terminal)</span>";
								}
							} else if(wrapped === true) {
								distance += " <span class='not_bold'>(+ scheduled layover at terminal)</span>";
							}
								
							var lastClass = ((_ === maxObservationsToShow - 1 || _ === mvjs.length - 1) ? " last" : "");
							html += '<li class="arrival' + lastClass + '">' + distance + '</li>';
						}
					}
				});
			});
		}
		
		if(routeAndDirectionWithoutArrivalsCount > 0) {
		    html += '<p class="service muted">No buses en-route to this stop for:</p>';

			html += '<ul>';
			var i = 0;
			jQuery.each(routeAndDirectionWithoutArrivals, function(_, d) {
				html += '<li class="route">';
				html += '<a class="muted" href="#' + stopIdWithoutAgency + "%20" + d.shortName + '"><span class="route-name">' + d.shortName + "</span>&nbsp;&nbsp; " + d.destination + '</a>';
				if (d.id in alertData) {
					html += ' <a id="alert-link|' + stopIdWithoutAgency + '|' + d.id + '|' + d.shortName + '" class="alert-link" href="#">Alert</a>';
				}
				html += '</li>';
				
				i++;
			});
			html += '<li class="last muted">(check back shortly for an update)</li>';
			html += '</ul>';
		}

		if(routeAndDirectionWithoutSerivceCount > 0) {
			html += '<p class="service muted">No scheduled service at this time for:</p>';

			html += '<ul class="no-service-routes">';
			var i = 0;
			jQuery.each(routeAndDirectionWithoutSerivce, function(_, d) {
				html += '<li class="route">';
				html += '<a class="muted" href="#' + stopIdWithoutAgency + "%20" + d.shortName + '"><span class="route-name">' + d.shortName + '</span></a>';
				html += '</li>';
				
				i++;
			});
			html += '</ul>';
		}
		
		// filtered out roues
		if (filteredMatches.find("li").length > 0) {
			var showAll = jQuery("<li></li>").addClass("filtered-match").html('<a href="#' + stopResult.id.match(/\d*$/) + '"><span class="route-name">See&nbsp;All</span></a>');
			filteredMatches.find("ul").append(showAll);
			html += filteredMatches.html();
		}

		html += OBA.Config.infoBubbleFooterFunction("stop", stopIdWithoutAgency);	        

		html += "<ul class='links'>";
		html += "<a href='#' id='zoomHere'>Center & Zoom Here</a>";
		html += "</ul>";
		
		// (end popup)
		html += '</div>';

		var content = jQuery(html);
		var zoomHereLink = content.find("#zoomHere");

		zoomHereLink.click(function(e) {
			e.preventDefault();
			
			var map = marker.map;
			map.setCenter(marker.getPosition());
			map.setZoom(16);
		});
		
		marker.setVisible(true);
		
		activateAlertLinks(content);
		
		(stopBubbleListener !== null)? stopBubbleListener.triggerHandler(stopBubbleTrigger) : null;

		return content.get(0);
	}
	
	function registerStopBubbleListener(obj, trigger) {
		stopBubbleListener = obj;
		stopBubbleTrigger = trigger;
		return stopBubbleListener;
	}
	
	function unregisterStopBubbleListener() {
		stopBubbleListener = null;
		stopBubbleTrigger = null;
		return null;
	}

	//////////////////// CONSTRUCTOR /////////////////////

	// timer to update data periodically 
	setInterval(function() {
		if(infoWindow !== null && typeof infoWindow.refreshFn === 'function') {
			infoWindow.refreshFn();
		}
	}, OBA.Config.refreshInterval);

	// updates timestamp in popup bubble every second
	setInterval(function() {
		if(infoWindow !== null && typeof infoWindow.updateTimestamp === 'function') {
			infoWindow.updateTimestamp();
		}
	}, 1000);
	
	return {
		reset: function() {
			closeInfoWindow();
		},
		
		getPopupStopId: function() {
			if(infoWindow !== null) {
				return infoWindow.stopId;
			} else {
				return null;
			}
		},
		
		// WAYS TO CREATE/DISPLAY A POPUP
		showPopupWithContent: showPopupWithContent, 
		
		showPopupWithContentFromRequest: showPopupWithContentFromRequest,
		
		// CONTENT METHODS
		getVehicleContentForResponse: getVehicleContentForResponse,
		
		getStopContentForResponse: getStopContentForResponse,
		
		registerStopBubbleListener: registerStopBubbleListener,
		
		unregisterStopBubbleListener: unregisterStopBubbleListener
	};
})();
