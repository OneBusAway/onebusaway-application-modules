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

var expandAlerts = false;

OBA.Sidebar = function() {
	var theWindow = jQuery(window),
		contentDiv = jQuery("#content"),
		topBarDiv = jQuery("#topbar"), 
		mainbox = jQuery("#mainbox"),
		menuBar = jQuery("#cssmenu1"),
		adDiv = jQuery("#ad");
		mapDiv = jQuery("#map"),
	    bottomBarDiv = jQuery("#bottombar"),
		mobileDiv = jQuery("#mobilebox");

    var searchBarDiv = jQuery("#searchbar"),
		matches = jQuery("#matches"),
		filteredMatches = jQuery("#filtered-matches"),
		suggestions = jQuery("#suggestions"),
		noResults = jQuery("#no-results"),
		welcome = jQuery("#welcome"),
		loading = jQuery("#loading"),
		availableRoutes = jQuery("#available-routes"),
		cantFind = jQuery("#cant-find"),
		sidebarGlobalAlerts = jQuery("#global-alerts"),
		mapGlobalAlerts = jQuery("#map-global-alerts").detach();

	var routeMap = null,
		wizard = null,
		results = jQuery("#matches"); // for wizard

	var searchRequest = null;
	
	function addSearchBehavior() {
		
		// Get our form and text input so we can customize them
		var searchForm = jQuery("#searchbar form");
		var searchInput = jQuery("#searchbar form input[type=text]");
		
		// add autocomplete behavior
		searchInput.autocomplete({
			source: OBA.Config.autocompleteUrl,
			select: function(event, ui) {
		        if(ui.item){
		        	// Make sure the input has the value selected from the suggestions and initiate the search
		        	searchInput.val(ui.item.value);
		        	// if search hasn't changed, force the search again to make panning, etc. happen
					if (decodeURIComponent(window.location.hash) !== "#" + searchInput.val()) {
						jQuery.history.load(decodeURIComponent(searchInput.val()));
					} else {
						doSearch(searchInput.val(), true);
					}
		        }
		    }
		});
		
		searchForm.submit(function(e) {
			e.preventDefault();
			
			// Close the autocomplete list when the form is submitted.
			searchInput.autocomplete("close");

			// if search hasn't changed, force the search again to make panning, etc. happen
			if (decodeURIComponent(window.location.hash) !== "#" + searchInput.val()) {
				jQuery.history.load(searchInput.val());	
			} else {
				doSearch(searchInput.val(), true);
			}
			
			(wizard && wizard.enabled()) ? results.triggerHandler('search_launched') : null;
		});
	}

    function getParameters(hashBased) {
        var query;
        if(hashBased) {
            var pos = location.href.indexOf("?");
            if(pos==-1) return [];
            query = location.href.substr(pos+1);
        } else {
            query = location.search.substr(1);
        }
        var result = {};
        query.split("&").forEach(function(part) {
            if(!part) return;
            part = part.split("+").join(" "); // replace every + with space, regexp-free version
            var eq = part.indexOf("=");
            var key = eq>-1 ? part.substr(0,eq) : part;
            var val = eq>-1 ? decodeURIComponent(part.substr(eq+1)) : "";
            var from = key.indexOf("[");
            if(from==-1) result[decodeURIComponent(key)] = val;
            else {
                var to = key.indexOf("]",from);
                var index = decodeURIComponent(key.substring(from+1,to));
                key = decodeURIComponent(key.substring(0,from));
                if(!result[key]) result[key] = [];
                if(!index) result[key].push(val);
                else result[key][index] = val;
            }
        });
        return result;
    }

	var resize = function() {
		var w = theWindow.width();
		
		if (w <= 1060) {
			mainbox.css("width", "960px");
		} else {
			mainbox.css("width", w - 150); // 75px margin on each 
										   // side for dropdown menus
		}

		// size set so we can have MTA menu items calculate their widths properly
		menuBar.width(mainbox.width());
		
		var alertsHeight = 0;
		
		if (mapGlobalAlerts.length > 0) {
			alertsHeight = mapGlobalAlerts.outerHeight();
		}

		// Check if bottomBar is enabled and adjust height accordingly
		if (bottomBarDiv.is(':visible') == true){
		var h = theWindow.height() - topBarDiv.height() - bottomBarDiv.outerHeight() - 1,
			h2 = theWindow.height() - topBarDiv.height() - bottomBarDiv.outerHeight() - alertsHeight - 1;
		} else {
			var h = theWindow.height() - topBarDiv.height() - 1,
			h2 = theWindow.height() - topBarDiv.height() - alertsHeight - 1;
		}
		
		searchBarDiv.height(h);
		mapDiv.height(h2);
	};

	function addResizeBehavior() {
		
		resize();

		// call when the window is resized
		theWindow.resize(resize);
	}

	// show user list of addresses
	function disambiguateLocations(locations) {

		suggestions.find("h2")
			.text("Did you mean?");

		var resultsList = suggestions.find("ul");

		var bounds = null;
		jQuery.each(locations, function(i, location) {
			var latlng = new google.maps.LatLng(location.latitude, location.longitude);
			var address = location.formattedAddress;
			var neighborhood = location.neighborhood;

		    // sidebar item
			var link = jQuery("<a href='#" + location.latitude + "%2C" + location.longitude + "'></a>")
							.text(address);

			var listItem = jQuery("<li></li>")
							.addClass("locationItem")
							.css("background-image", "url('img/location/location_" + (i + 1) + ".png')")
							.append(link);

			resultsList.append(listItem);

			// marker
			var marker = routeMap.addDisambiguationMarker(latlng, address, neighborhood, (i + 1));

			listItem.hover(function() {
				routeMap.highlightDisambiguationMarker(marker, (i + 1));
				listItem.css("background-image", "url('img/location/location_active_sidebar_" + (i + 1) + ".png')");
			}, function() {
				routeMap.unhighlightDisambiguationMarker(marker, (i + 1));
				listItem.css("background-image", "url('img/location/location_" + (i + 1) + ".png')");
			});

			// calculate extent of all options
			if(bounds === null) {
				bounds = new google.maps.LatLngBounds(latlng, latlng);
			} else {
				bounds.extend(latlng);
			}
		});

		routeMap.showBounds(bounds);

		suggestions.show();
	}

	function loadStopsForRouteAndDirection(routeResult, direction, destinationContainer) {
		var stopsList = destinationContainer.find("ul");
		
		// if stops are already loaded, don't request them again
		if(! stopsList.hasClass("not-loaded")) {
			return;
		}

		var loading = destinationContainer.find(".loading");
		
		loading.show();

		// multiple of these can be out at once without being inconsistent UI-wise.
		jQuery.getJSON(OBA.Config.stopsOnRouteForDirection + "?callback=?", { routeId: routeResult.id, directionId: direction.directionId }, 
		function(json) { 
			loading.hide();

			stopsList.removeClass("not-loaded");

			jQuery.each(json.stops, function(_, stop) {
				routeMap.addStop(stop, null);
				
				var stopLink = jQuery("<a href='#'></a>")
									.text(stop.name);
					
				var imagePiece = "middle";
				if(_ === 0) {
					imagePiece = "start";
				} else if(_ === json.stops.length - 1) {
					imagePiece = "end";
				}
				
				// transparent stops-on-route icon mask
				var stopItem = jQuery('<li></li>')
								.css("background", "#" + routeResult.color + " url('img/stop-on-route/stop_on_route_" + imagePiece + ".png') no-repeat left center")
								.append(stopLink);

				stopsList.append(stopItem);
				
				stopLink.click(function(e) {
					e.preventDefault();
					
					routeMap.showPopupForStopId(stop.id, null);
					
					(wizard !== null)? results.triggerHandler("stop_click") : null;
				});
				
				stopLink.hover(function() {
					routeMap.highlightStop(stop);
				}, function() {
					routeMap.unhighlightStop();					
				});
				
				stopLink.hoverIntent({
					over: function(e) { 
						stopLink.addClass('stopHover');
					},
					out: function(e) { 
						stopLink.removeClass('stopHover');
					},
					sensitivity: 1,
					interval: 400
				});
			});
			
		});
	}
	
	function addRoutesToLegend(routeResults, title, filter, stopId) {

		var filterExistsInResults = false;

		jQuery.each(routeResults, function(_, routeResult) {
			if (routeResult.shortName === filter) {
				filterExistsInResults = true;
				return false;
			}
		});

		if(typeof title !== "undefined" && title !== null) {
			matches.find("h2").text(title);
		}

		var resultsList = matches.find("ul");

		jQuery.each(routeResults, function(_, routeResult) {				
			
			if (!filter || routeResult.shortName === filter || !filterExistsInResults) {
			
				// service alerts
				var serviceAlertList = jQuery("<ul></ul>")
								.addClass("alerts");
								
				var serviceAlertHeader = jQuery("<p class='serviceAlert'>" + OBA.Config.serviceAlertText + " for " + getRouteShortName(routeResult) + "</p>")
												.append(jQuery("<span class='click_info'> + Click for info</span>"));
				
				var serviceAlertContainer = jQuery("<div></div>")
												.attr("id", "alerts-" + routeResult.id.hashCode())
												.addClass("serviceAlertContainer")
												.append(serviceAlertHeader)
												.append(serviceAlertList);
				
				serviceAlertContainer.accordion({ header: 'p.serviceAlert', 
					collapsible: true, 
					active: false, 
					autoHeight: false });
				
				// If popup.js has specified to expand alerts, that has been taken into account above and we
				// reset the global state to not expand alerts.
				if (expandAlerts) {
					serviceAlertContainer.accordion("activate" , 0);
					expandAlerts = false;
				}
				

				// sidebar item
				var titleBox = jQuery("<p></p>")
								.addClass("name")
								.text(getRouteShortLongName(routeResult))
								.css("border-bottom", "5px solid #" + routeResult.color);
				
				var descriptionBox = jQuery("<p></p>")
								.addClass("description")
								.text(routeResult.description == null ? '' : routeResult.description);
	
				var listItem = jQuery("<li></li>")
								.addClass("legendItem")
								.append(titleBox)
								.append(descriptionBox)
								.append(serviceAlertContainer);
		
				resultsList.append(listItem);
				
				// on click of title, pan to route extent 
				titleBox.click(function(e) {
					e.preventDefault();
					
					routeMap.panToRoute(routeResult.id);
				});
	
				// hover polylines
				titleBox.hover(function(e) {
					titleBox.css("color", "#" + routeResult.color);
				}, function(e) {
					titleBox.css("color", "");
				});
	
				titleBox.hoverIntent({
					over: function(e) { 
						routeMap.highlightRoute(routeResult.id); 
					}, out: function(e) { 
						routeMap.unhighlightRoute(routeResult.id); 
					},
					sensitivity: 10
				});
	

				// if we don't have any directions then there is no service today
				if (routeResult.directions.length == 0) {
					var noServiceMessage = jQuery("<div></div>")
						.addClass("no-service")
						.text("No scheduled service today for the " +
							getRouteShortName(routeResult));

					descriptionBox.append(noServiceMessage);
				}

				// direction picker
				jQuery.each(routeResult.directions, function(_, direction) {
					var directionHeader = jQuery("<p></p>");
					
					jQuery("<span></span>")
						.text("to " + direction.destination)
						.appendTo(directionHeader);
					
					if(direction.hasUpcomingScheduledService === false) {
						var noServiceMessage = jQuery("<div></div>")
													.addClass("no-service")
													.text("No scheduled service for the " + 
															getRouteShortName(routeResult) + 
															" to " + direction.destination + " at this time.");
	
						directionHeader.append(noServiceMessage);
					}
	
					var stopsList = jQuery("<ul></ul>")
												.addClass("stops")
												.addClass("not-loaded");
	
					var loading = jQuery("<div><span>Loading...</span></div>")
												.addClass("loading");
	
					var destinationContainer = jQuery("<p></p>")
												.addClass("destination")
												.append(directionHeader)
												.append(stopsList)
												.append(loading);
					
					// load stops when user expands stop list
					directionHeader.click(function(e) {
						loadStopsForRouteAndDirection(routeResult, direction, destinationContainer);
					});
					
					// accordion-ize
					destinationContainer.accordion({ header: 'p', 
						collapsible: true, 
						active: false, 
						autoHeight: false });
					
					listItem.append(destinationContainer);
				});
				
				// add to map
				routeMap.addRoute(routeResult);
			}
			
			if (filter && routeResult.shortName !== filter && filterExistsInResults) {
				
				var filteredMatch = jQuery("<li></li>").addClass("filtered-match");
				var link = jQuery('<a href="#' + OBA.Util.displayStopId(stopId) + '%20' + getRouteShortName(routeResult) + '">' + getRouteShortName(routeResult) + '</a>');
				
				var allPolylines = [];
				jQuery.each(routeResult.directions, function(_, direction) {
					allPolylines = allPolylines.concat(direction.polylines);
				});
				
				link.hover(function() {
					routeMap.showHoverPolyline(allPolylines, routeResult.color);
				}, function() {
					routeMap.removeHoverPolyline();
				});
				
				link.appendTo(filteredMatch);
				
				filteredMatches.find("ul").append(filteredMatch);
			}
		});

		matches.show();
		
		if (filteredMatches.find("li").length > 1) {
			var showAll = jQuery("<li></li>").addClass("filtered-match").html('<a href="#' + OBA.Util.displayStopId(stopId) + '">See&nbsp;All</a>');
			filteredMatches.find("ul").append(showAll);
			filteredMatches.show();
			
			var maxWidth = 0;
			jQuery.each(filteredMatches.find("li"), function(_, item) {
				var wrappedItem = jQuery(item);
				if (wrappedItem.width() > maxWidth) {
					maxWidth = wrappedItem.width();
				}
			});
			filteredMatches.find("li").width(maxWidth);
		}
	}

	// show multiple route choices to user
	function showRoutePickerList(routeResults) {	
		suggestions.find("h2").text("Did you mean?");

		var resultsList = suggestions.find("ul");

		jQuery.each(routeResults, function(_, route) {
			var link = jQuery('<a href="#' + getRouteShortName(route) + '"></a>')
							.text(getRouteShortName(route))
							.attr("title", route.description);

			var listItem = jQuery("<li></li>")
							.addClass("routeItem")
							.append(link);
			
			resultsList.append(listItem);

			// polyline hovers
			var allPolylines = [];

			// "region" routes (searches for a zip code, etc.)
			if(typeof route.polylines !== "undefined") {
				jQuery.each(route.polylines, function(__, polyline) {
					allPolylines.push(polyline);
				});
			
			// "did you mean" route suggestions--ex. X17 suggests X17J,A,C
			} else if(route.directions !== "undefined") {
				jQuery.each(route.directions, function(__, direction) {
					jQuery.each(direction.polylines, function(___, polyline) {
						allPolylines.push(polyline);						
					});
				});
			}

			if(allPolylines.length > 0) {
				link.hover(function() {
					routeMap.showHoverPolyline(allPolylines, route.color);
				}, function() {
					routeMap.removeHoverPolyline();
				});
			}
		});
		
		suggestions.show();
	}
	
	function getRouteShortName(routeResult){
		var longName = routeResult.longName;
		var shortName = routeResult.shortName;
		
		if(longName == null && shortName == null){
			shortName = "route";
		}
		else if (shortName == null){
			shortName = longName;
		}
		return shortName;
	}
	
	function getRouteShortLongName(routeResult){

		var longName = routeResult.longName;
		var shortName = routeResult.shortName;
		var shortAndLongName = shortName + " " + longName;
		var description = routeResult.description;
		
		if(longName == null && shortName == null){
			if(description == null){
				shortAndLongName = "";
			}
			else{
				shortAndLongName = description;
			}
		}
		else if (longName == null){ 
			shortAndLongName = shortName;
		}
		else if (shortName == null){
			shortAndLongName = longName;
		}
		return shortAndLongName;
	}
	
	// show multiple stop choices to user
	function showStopPickerList(stopResults) {
		suggestions.find("h2").text("Did you mean?");

		var resultsList = suggestions.find("ul");

		jQuery.each(stopResults, function(_, stop) {
			var link = jQuery('<a href="#' + stop.id+ '"></a>') /*shortName*/
							.text(stop.name)/*shortName*/
							.attr("title", stop.name);/*description*/

			var listItem = jQuery("<li></li>")
							.addClass("locationItem")
							.append(link);
			
			resultsList.append(listItem);
		});
		
		
		suggestions.show();
		
	}
	
	function resetSearchPanelAndMap() {
		adDiv.hide();
		welcome.hide();
		noResults.hide();

		matches.hide();
		matches.children().empty();
		
		filteredMatches.hide();
		filteredMatches.find("ul").empty();

		suggestions.hide();		
		suggestions.children().empty();

		routeMap.reset();
		
		if (mapGlobalAlerts.find(".global-alert-content").length > 0){
			mapGlobalAlerts.appendTo(contentDiv);
			resize();
		}
	}
	
	function showNoResults(message) {
		if (typeof message !== "undefined") { 
			noResults.html("<h2>" + message + "</h2>"); 
			noResults.show();
		}

		adDiv.show();
		welcome.show();
		cantFind.show();

		(wizard && wizard.enabled()) ? results.triggerHandler('no_result') : null;
	}

	// process search results
	function doSearch(q, showPopup) {
		resetSearchPanelAndMap();		

		(wizard && wizard.enabled()) ? results.triggerHandler('search_launched') : null;
		
		cantFind.hide();
		availableRoutes.hide();
		sidebarGlobalAlerts.hide();
		
		loading.show();
		
		if(searchRequest !== null) {
			searchRequest.abort();
		}		
		searchRequest = jQuery.getJSON(OBA.Config.searchUrl + "?callback=?", { q: q }, function(json) {

			// Be sure the autocomplete list is closed
			jQuery("#searchbar form input[type=text]").autocomplete("close");
			
			loading.hide();

			var resultType = json.searchResults.resultType;
			var empty = json.searchResults.empty;
			
			var matches = json.searchResults.matches;
			var suggestions = json.searchResults.suggestions;
			
			var routeFilter = json.searchResults.routeFilter;
			var routeFilterShortName;

            // Get stopIds for coloring stops in RouteMap.js
            var stopsOnRoutes = { stops:[] };

            jQuery.each(matches, function(_, match) {
                if (match.stopIdsForRoute) {
                    jQuery.each(match.stopIdsForRoute, function (_, stop) {
                        if (stopsOnRoutes.stops.length < 1 || stopsOnRoutes.stops.indexOf(stop.id) === -1) {
                            stopsOnRoutes.stops.push(stop);
                        }
                    });
                }
			});

            jQuery("body").data( "savedData", stopsOnRoutes);

			if (routeFilter.length > 0) {
				routeFilterShortName = routeFilter[0].shortName;
			}

			OBA.Config.analyticsFunction("Search", q + " [M:" + matches.length + " S:" + suggestions.length + "]");

			if(empty === true) {
				showNoResults("No matches.");
				return;
			} else {
				noResults.hide();
			}

			// direct matches
			if(matches.length === 1) {
				switch(resultType) {
					case "GeocodeResult":
						// result is a region
						if(matches[0].isRegion === true) {
							if(matches[0].nearbyRoutes.length === 0) {
								showNoResults("No stops nearby.");
							} else {
								showRoutePickerList(matches[0].nearbyRoutes);								
							}

							var latLngBounds = new google.maps.LatLngBounds(
									new google.maps.LatLng(matches[0].bounds.minLat, matches[0].bounds.minLon), 
									new google.maps.LatLng(matches[0].bounds.maxLat, matches[0].bounds.maxLon));

							routeMap.showBounds(latLngBounds);
							
							(wizard && wizard.enabled()) ? results.triggerHandler('location_result') : null;
							
						// result is a point--intersection or address
						} else {
							if(matches[0].nearbyRoutes.length === 0) {
								showNoResults("No stops nearby.");
							} else {
								addRoutesToLegend(matches[0].nearbyRoutes, "Nearby routes:", null, null);
							}
							
							var latlng = new google.maps.LatLng(matches[0].latitude, matches[0].longitude);
							
							routeMap.showLocation(latlng);
							routeMap.addLocationMarker(latlng, matches[0].formattedAddress, matches[0].neighborhood);
							
							(wizard && wizard.enabled()) ? results.triggerHandler('intersection_result') : null;
						}

						break;
				
					case "RouteResult":
						addRoutesToLegend(matches, "Routes:", null, null);

						routeMap.panToRoute(matches[0].id);
						(wizard && wizard.enabled()) ? results.triggerHandler('route_result') : null;
						break;
					
					case "StopResult":
						addRoutesToLegend(matches[0].routesAvailable, "Routes available:", routeFilterShortName, matches[0].id);

						var latlng = new google.maps.LatLng(matches[0].latitude, matches[0].longitude);
                        if (showPopup != undefined && !showPopup) {
							routeMap.addStop(matches[0], null);
                            routeMap.highlightStop(matches[0]);
                        } else {
                            routeMap.addStop(matches[0], function(marker) {
                                routeMap.showPopupForStopId(matches[0].id, routeFilterShortName);
                            });
						}
						routeMap.showLocation(latlng);
						
						(wizard && wizard.enabled()) ? results.triggerHandler('stop_result') : null;
						break;
				}	
				
			} else if (matches.length > 1 && resultType == "RouteResult") {
				// suppport multiple routes found
				addRoutesToLegend(matches, "Routes:", null, null);
				routeMap.panToRoute(matches[0].id);
				(wizard && wizard.enabled()) ? results.triggerHandler('route_result') : null;
			} else if (matches.length > 1 && resultType == "StopResult") {
				// we've matched multiple stops across agencies -- disambiguate
				showStopPickerList(matches);
				
			} else if(suggestions.length > 0){    // did you mean suggestions
				
				switch(resultType) {
					case "GeocodeResult":					
						disambiguateLocations(suggestions);

						(wizard && wizard.enabled()) ? results.triggerHandler('disambiguation_result') : null;
						break;

					// a route query with no direct matches
					case "RouteResult":
						showRoutePickerList(suggestions);								
						(wizard && wizard.enabled()) ? results.triggerHandler('route_result') : null;
						break;
					case "StopResult":
						// this is a new option based on search on stop name
                        showStopPickerList(suggestions);
                        break;
				}
			}
		});
	}

	return {
		initialize: function() {
			addSearchBehavior();
			addResizeBehavior();
			//googleTranslateElementInit();
			
			// Add behavior to the close link in the global alert dialog under the map
			// so it closes when the link is clicked.
			mapGlobalAlerts.find("a#closeMapGlobalAlerts").click(function(event){
				event.preventDefault();
				mapGlobalAlerts.detach();
				resize();
			});
			
			// Remove the global alerts dialog under the map on page load
			// if it exists. It will be displayed again when a search is performed.
			mapGlobalAlerts.detach();
			
			// initialize map, and continue initialization of things that use the map
			// on load only when google maps says it's ready.
			routeMap = OBA.RouteMap(document.getElementById("map"), function() {
				// deep link handler
				jQuery.history.init(function(hash) {
					if(hash !== null && hash !== "") {
                        var params = getParameters(true);
						var pos = hash.indexOf("?");
						if (pos > 0) {
							hash = hash.substring(0, pos);
						}
						var searchInput = jQuery("#searchbar form input[type=text]");
						searchInput.val(decodeURIComponent(hash));
						var showPopup = true;
						if (params['showPopup'] != undefined)
							showPopup = !(params['showPopup'] == "false");
						doSearch(decodeURIComponent(hash), showPopup);
					} else {
						// Launch wizard
						(wizard !== null) ? null : wizard = OBA.Wizard(routeMap);
					}
				});
			}, function(routeId, serviceAlerts) { // service alert notification handler
				var serviceAlertsContainer = jQuery("#alerts-" + routeId.hashCode());
				if(serviceAlertsContainer.length === 0) {
					return;
				}

				if(serviceAlerts.length === 0) {
					serviceAlertsContainer.hide();
				} else {
					
					var serviceAlertsList = serviceAlertsContainer.find("ul");
					serviceAlertsList.empty();
					
					var showAlerts = false;
					var alertIds = [];
			        
					jQuery.each(serviceAlerts, function(_, serviceAlert) {
						var moreInfoLinkPrefix = "";
						var moreInfoLinkSuffix = "";

						if (serviceAlert.InfoLinks && serviceAlert.InfoLinks.InfoLink.length > 0) {
				        	moreInfoLinkPrefix = ' <a href="' + serviceAlert.InfoLinks.InfoLink[0].Uri + '" target="alert">';
				        	moreInfoLinkSuffix = "</a>";
				        }
						
						// If this is not a global alert, display it
						if (!serviceAlert.Affects.Operators || (serviceAlert.Affects.Operators && !serviceAlert.Affects.Operators.hasOwnProperty("AllOperators"))) {
							var text = null;
							if(typeof serviceAlert.Description !== 'undefined' && typeof serviceAlert.Summary !== 'undefined') {
								// we have a service alert with both summary and description, do a little formatting
								if (serviceAlert.Summary == serviceAlert.Description) {
									text = '<strong>' + serviceAlert.Summary + '</strong>';
								} else {
									text = '<strong>' + serviceAlert.Summary + '</strong><br/><br/>' + serviceAlert.Description;
								}
							} else if(typeof serviceAlert.Description !== 'undefined') {
								text = serviceAlert.Description;
							} else if(typeof serviceAlert.Summary !== 'undefined') {
								text = serviceAlert.Summary;
							}
							text = moreInfoLinkPrefix + text + moreInfoLinkSuffix;
							if(text !== null) {
								text = text.replace(/\n/g, "<br/>");
								var newText = jQuery("<li></li>").html(text);
								if (!alertIds.includes(serviceAlert.SituationNumber)) {
									alertIds.push(serviceAlert.SituationNumber)
									serviceAlertsList.append(newText);
									showAlerts = true;
								}
							}
						}
					});
					
					if (showAlerts) {
						serviceAlertsContainer.show();
					}
				}
			});
		}
	};
};

// for IE: only start using google maps when the VML/SVG namespace is ready
if(jQuery.browser.msie) {
	window.onload = function() { OBA.Sidebar().initialize(); };
} else {
	jQuery(document).ready(function() { OBA.Sidebar().initialize(); });
}