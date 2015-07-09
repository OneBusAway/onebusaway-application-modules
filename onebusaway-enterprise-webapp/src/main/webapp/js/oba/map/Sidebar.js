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

var expandAlerts = false;

OBA.Sidebar = function() {
	var theWindow = jQuery(window),
		contentDiv = jQuery("#content"),
		topBarDiv = jQuery("#topbar"), 
		mainbox = jQuery("#mainbox"),
		menuBar = jQuery("#cssmenu1"),
		adDiv = jQuery("#ad");
		mapDiv = jQuery("#map");

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
					if (window.location.hash !== "#" + searchInput.val()) {
						jQuery.history.load(searchInput.val());	
					} else {
						doSearch(searchInput.val());
					}
		        }
		    }
		});
		
		searchForm.submit(function(e) {
			e.preventDefault();
			
			// Close the autocomplete list when the form is submitted.
			searchInput.autocomplete("close");

			// if search hasn't changed, force the search again to make panning, etc. happen
			if (window.location.hash !== "#" + searchInput.val()) {
				jQuery.history.load(searchInput.val());	
			} else {
				doSearch(searchInput.val());
			}
			
			(wizard && wizard.enabled()) ? results.triggerHandler('search_launched') : null;
		});
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
		
		var h = theWindow.height() - topBarDiv.height() - 1,
			h2 = theWindow.height() - topBarDiv.height() - alertsHeight - 1;
		
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
								
				var serviceAlertHeader = jQuery("<p class='serviceAlert'>Service Alert for " + routeResult.shortName + "</p>")
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
								.text(routeResult.shortName + " " + routeResult.longName)
								.css("border-bottom", "5px solid #" + routeResult.color);
				
				var descriptionBox = jQuery("<p></p>")
								.addClass("description")
								.text(routeResult.description);
	
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
															routeResult.shortName + 
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
				var link = jQuery('<a href="#' + stopId.match(/\d*$/) + '%20' + routeResult.shortName + '">' + routeResult.shortName + '</a>');
				
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
			var showAll = jQuery("<li></li>").addClass("filtered-match").html('<a href="#' + stopId.match(/\d*$/) + '">See&nbsp;All</a>');
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
			var link = jQuery('<a href="#' + route.shortName + '"></a>')
							.text(route.shortName)
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
	function doSearch(q) {
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
						routeMap.addStop(matches[0], function(marker) {
							routeMap.showPopupForStopId(matches[0].id, routeFilterShortName);						
						});
						
						routeMap.showLocation(latlng);
						
						(wizard && wizard.enabled()) ? results.triggerHandler('stop_result') : null;
						break;
				}	
				
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
				}
			}
		});
	}
	
	function googleTranslateElementInit() {
		
		var translate_element = jQuery("#google_translate_element");		
		translate_element.click(function (e) {
			e.preventDefault();
			translate_element.html(' ')
							 .attr('src','//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit');
			new google.translate.TranslateElement({pageLanguage: 'en', 
				layout: google.translate.TranslateElement.InlineLayout.SIMPLE}, 'google_translate_element');
			translate_element.unbind('click');
		});						
	}
	
	return {
		initialize: function() {
			addSearchBehavior();
			addResizeBehavior();
			googleTranslateElementInit();
			
			// Add behavior to the close link in the global alert dialog under the map
			// so it closes when the link is clicked.
			mapGlobalAlerts.find("a").click(function(event){
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
						var searchInput = jQuery("#searchbar form input[type=text]");
						searchInput.val(hash);
						doSearch(hash);
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

					jQuery.each(serviceAlerts, function(_, serviceAlert) {
						
						// If this is not a global alert, display it
						if (!serviceAlert.Affects.Operators || (serviceAlert.Affects.Operators && !serviceAlert.Affects.Operators.hasOwnProperty("AllOperators"))) {
							var text = null;
							
							if(typeof serviceAlert.Description !== 'undefined') {
								text = serviceAlert.Description;
							} else if(typeof serviceAlert.Summary !== 'undefined') {
								text = serviceAlert.Summary;
							}
							
							if(text !== null) {
								serviceAlertsList.append(jQuery("<li></li>").html(text.replace(/\n/g, "<br/>")));
								showAlerts = true;
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