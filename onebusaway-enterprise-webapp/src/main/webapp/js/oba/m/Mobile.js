/*
 * Copyright (C) 2011 Metropolitan Transportation Authority
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

OBA.Mobile = (function() {
    var expandAlerts = false;

    var theWindow = jQuery(window);
    var mainbox = jQuery("#mainbox");
    var topBarDiv = jQuery("#branding");
    var bottomBarDiv = jQuery("#footer");
    var routeMap = null;
	var locationField = null;
	var typeField = null;
	var refreshBar = null;
	var defaultTimeout = OBA.Config.refreshInterval;
	
	function addAutocompleteBehavior() {
		
		var searchForm = jQuery("#searchPanel form");
		var searchInput = jQuery("#searchPanel form input[type=text]");
		
		searchInput.autocomplete({
			source: "../" + OBA.Config.autocompleteUrl,
			select: function(event, ui) {
		        if(ui.item){
		        	searchInput.val(ui.item.value);
		        	searchForm.submit();
		        }
		    }
		});
		
		// Close the autocomplete list when the form is submitted
		searchForm.submit(function() {
			searchInput.autocomplete("close");
			return true;
		});
	}
	
	function addRefreshBehavior() {
		// refresh button logic
		refreshBar = jQuery("#refresh")
					.css("position", "relative")
					.css("right", "20")
					.css("left", "12");

		var refreshTimestamp = refreshBar
								.find("strong");

		var titleText = refreshBar.find("a");
		// only refresh if a single search result
		if (titleText.text().includes("Refresh")) {
			// ajax refresh for browsers that support it
			refreshBar.find("a").click(function(e) {
				e.preventDefault();

				refreshTimestamp.text("Loading...");
				refreshBar.addClass("loadingRefresh");

				jQuery("#content")
					.load(location.href + " #content>*", null, function() {
						refreshTimestamp.text("Updated " + new Date().format("mediumTime"));
						refreshBar.removeClass("loadingRefresh");
						updateServiceAlertHeaderText();
					});
			});
		}
				
		// scrolling/fixed refresh bar logic
		var contentDiv = jQuery("#content")
							.css("padding-top", refreshBar.height() * 0.1);

		var topLimit = contentDiv.offset().top + (refreshBar.height() * 0.25) - 20;
		
		jQuery("body")
					.css("position", "relative");

		var theWindow = jQuery(window);
		var repositionRefreshBar = function() {
			var top = theWindow.scrollTop();

			if(top < topLimit) {
				top = topLimit;
			}
			
			// refreshBar.css("top", top + 3);
		};
		repositionRefreshBar();
		
		theWindow.scroll(repositionRefreshBar)
					.resize(repositionRefreshBar);
		
		setTimeout(refreshContent, defaultTimeout);
	}
	
	function initLocationUI() {
		jQuery("#submitButton").removeClass("loading");
		
		var searchPanelForm = jQuery("#searchPanel form");
		
		var splitButton = jQuery("<div></div>").attr("id", "nearby-button-bar");
		
		var nearbyStopsBtn = jQuery("<div></div>").attr("id", "nearby-stops-button")
			.attr("aria-label", "Find nearby stops using GPS").attr("tabindex", 0)
			.addClass("nearby-button").appendTo(splitButton);
		
		var nearbyRoutesBtn = jQuery("<div></div>").attr("id", "nearby-routes-button")
			.attr("aria-label", "Find nearby routes using GPS").attr("tabindex", 0)
			.addClass("nearby-button").appendTo(splitButton);
		
		nearbyStopsBtn.append(jQuery("<div></div>").attr("id", "nearby-stops-button-icon")
				.append(jQuery("<span></span>").addClass("nearby-text").text("Nearby Stops")));
		nearbyRoutesBtn.append(jQuery("<div></div>").attr("id", "nearby-routes-button-icon")
				.append(jQuery("<span></span>").addClass("nearby-text").text("Nearby Routes")));
		
		searchPanelForm.before(splitButton);
				
		$( ".nearby-button" ).mousedown(function() {
			// change other button to mouse up
			if (jQuery(this).attr("id") === "nearby-stops-button") {
				nearbyRoutesBtn.removeClass("down");
			} else {
				nearbyStopsBtn.removeClass("down");
			}
			jQuery(this).addClass("down");
		});
		
		$( ".nearby-button" ).mouseup(function() {
			if (jQuery(this).attr("id") === "nearby-stops-button") {
				typeField.val("stops");
			} else {
				typeField.val("routes");
			}
			queryByLocation();
		});
	};
	
	// event when user turns on location
	function queryByLocation() {
		// show "finding location" message button to user while 
		// location is being found
		jQuery("#submitButton").addClass("loading");
		jQuery(".q").attr("placeholder", "Finding your location...");

		navigator.geolocation.getCurrentPosition(function(location) {
			
			jQuery(".q").attr("placeholder", "Searching...");

			// update search field
			if(locationField !== null) {
				locationField.val(location.coords.latitude + "," + location.coords.longitude);
			}
			
			var searchPanelForm = jQuery("#searchPanel form");
			
			searchPanelForm.find(".q").val("");
			searchPanelForm.submit();
			
		}, function() {
			alert("Unable to determine your location.");
			jQuery(".nearby-button").removeClass("down");
			jQuery("#submitButton").removeClass("loading");
			jQuery(".q").removeAttr("placeholder");
			
		});
	};
	
	function refreshContent() {
		refreshBar.find("a").click();
		setTimeout(refreshContent, defaultTimeout);
	}

	function addMapBehaviour() {
		var mapElement = document.getElementById("map");
		if (mapElement !== null) {
			$("#mapExpander").click(function () {
				$mapExpander = $(this);
				$mapDiv = $('#map');
				$mapDiv.slideToggle(500, function () {
					$mapExpander.children('span').text(function () {
						return $mapDiv.is(":visible") ? "HIDE MAP" : "SHOW MAP";
					});
					if ($mapDiv.is(":visible")) updateMap();
				});
			});
		}
    }

	function updateServiceAlertHeaderText() {
		$('#serviceAlertHeader span').html("<strong>" + OBA.Config.serviceAlertText + ":</strong>");
	}

    function addRoutesToLegend(routeResults, title, filter, stopId) {

        var filterExistsInResults = false;

        jQuery.each(routeResults, function(_, routeResult) {
            if (routeResult.shortName === filter) {
                filterExistsInResults = true;
                return false;
            }
        });

        jQuery.each(routeResults, function(_, routeResult) {

            if (!filter || routeResult.shortName === filter || !filterExistsInResults) {
                // add to map
                routeMap.addRoute(routeResult);
            }
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

        // Check if bottomBar is enabled and adjust height accordingly
        if (bottomBarDiv.is(':visible') == true){
            var h = theWindow.height() - topBarDiv.height() - bottomBarDiv.outerHeight() - 1;
        } else {
            var h = theWindow.height() - topBarDiv.height()  - 1;
        }

        jQuery("#map").height(h * 0.5);//only use half of that space
		jQuery("#map").width(w * 0.92); //match refresh button width
    };

    function addResizeBehavior() {

        resize();

        // call when the window is resized
        theWindow.resize(resize);
    }

    function updateMap() {
        var q = jQuery(".q").val();
        if (q != '' && q != null && $('#map').is(":visible")) {

			var searchResponse = jQuery.getJSON(OBA.Config.searchUrl + "?callback=?", {q: q}, function (json) {

				var resultType = json.searchResults.resultType;
				var matches = json.searchResults.matches;
				var routeFilter = json.searchResults.routeFilter;
				var routeFilterShortName;

				if (routeFilter.length > 0) {
					routeFilterShortName = routeFilter[0].shortName;
				}

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

				var showPopup = false;//don't want popup to show up initially

				// direct matches
				if(matches.length === 1 ) {
					switch(resultType) {

						case "RouteResult":
							addRoutesToLegend(matches, "Routes:", null, null);

							routeMap.panToRoute(matches[0].id);
							break;

						case "StopResult":
							addRoutesToLegend(matches[0].routesAvailable, "Routes available:", routeFilterShortName, matches[0].id);

							var latlng = new google.maps.LatLng(matches[0].latitude, matches[0].longitude);
							if (showPopup != undefined && !showPopup) {
								routeMap.addStop(matches[0], null);
								routeMap.highlightStop(matches[0], OBA.Config.useActiveBusPin);
							} else {
								routeMap.addStop(matches[0], function(marker) {
									routeMap.showPopupForStopId(matches[0].id, routeFilterShortName);
								});
							}
							routeMap.showLocation(latlng);

							break;
					}

				} else if (matches.length > 1 && resultType == "RouteResult") {
					// suppport multiple routes found
					addRoutesToLegend(matches, "Routes:", null, null);
					routeMap.panToRoute(matches[0].id);
				}

			});
		}
	}

	return {
		initialize: function() {
			locationField = jQuery("#l");
			typeField = jQuery("#t");
			
			if(navigator.geolocation) {
				initLocationUI();
			}			
			
			addRefreshBehavior();
			addAutocompleteBehavior();
			addMapBehaviour();
            addResizeBehavior();

            updateServiceAlertHeaderText();

            $("#all-routes-button").click(function() {
                window.location = OBA.Config.urlPrefix + "m/routes/index";
            });

            // initialize map, and continue initialization of things that use the map
            // on load only when google maps says it's ready.
			var mapElement = document.getElementById("map");
			if (mapElement !== null) {
				routeMap = OBA.RouteMap(mapElement, function() {
					// deep link handler
					updateMap();

				}, function(routeId, serviceAlerts) { // service alert notification handler
				});

			$('#map').hide();
			}


		}
	};
})();

jQuery(document).ready(function() { OBA.Mobile.initialize();});