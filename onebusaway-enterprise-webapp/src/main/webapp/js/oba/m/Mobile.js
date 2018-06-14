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

	function updateServiceAlertHeaderText() {
		$('#serviceAlertHeader span').html("<strong>" + OBA.Config.serviceAlertText + ":</strong>");
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

    function addRoutesToLegend(routeResults, title, filter, stopId) {

        var filterExistsInResults = false;

        jQuery.each(routeResults, function(_, routeResult) {
            if (routeResult.shortName === filter) {
                filterExistsInResults = true;
                return false;
            }
        });

        // if(typeof title !== "undefined" && title !== null) {
        //     matches.find("h2").text(title);
        // }

        //var resultsList = matches.find("ul");

        jQuery.each(routeResults, function(_, routeResult) {

            if (!filter || routeResult.shortName === filter || !filterExistsInResults) {

                // service alerts
                // var serviceAlertList = jQuery("<ul></ul>")
                //     .addClass("alerts");
                //
                // var serviceAlertHeader = jQuery("<p class='serviceAlert'>" + OBA.Config.serviceAlertText + " for " + getRouteShortName(routeResult) + "</p>")
                //     .append(jQuery("<span class='click_info'> + Click for info</span>"));
                //
                // var serviceAlertContainer = jQuery("<div></div>")
                //     .attr("id", "alerts-" + routeResult.id.hashCode())
                //     .addClass("serviceAlertContainer")
                //     .append(serviceAlertHeader)
                //     .append(serviceAlertList);
                //
                // serviceAlertContainer.accordion({ header: 'p.serviceAlert',
                //     collapsible: true,
                //     active: false,
                //     autoHeight: false });

                // If popup.js has specified to expand alerts, that has been taken into account above and we
                // reset the global state to not expand alerts.
                // if (expandAlerts) {
                //     serviceAlertContainer.accordion("activate" , 0);
                //     expandAlerts = false;
                // }


                // sidebar item
                // var titleBox = jQuery("<p></p>")
                //     .addClass("name")
                //     .text(getRouteShortLongName(routeResult))
                //     .css("border-bottom", "5px solid #" + routeResult.color);
                //
                // var descriptionBox = jQuery("<p></p>")
                //     .addClass("description")
                //     .text(routeResult.description == null ? '' : routeResult.description);
                //
                // var listItem = jQuery("<li></li>")
                //     .addClass("legendItem")
                //     .append(titleBox)
                //     .append(descriptionBox)
                //     .append(serviceAlertContainer);
                //
                // resultsList.append(listItem);

                // on click of title, pan to route extent
                // titleBox.click(function(e) {
                //     e.preventDefault();
                //
                //     routeMap.panToRoute(routeResult.id);
                // });

                // hover polylines
                // titleBox.hover(function(e) {
                //     titleBox.css("color", "#" + routeResult.color);
                // }, function(e) {
                //     titleBox.css("color", "");
                // });

                // titleBox.hoverIntent({
                //     over: function(e) {
                //         routeMap.highlightRoute(routeResult.id);
                //     }, out: function(e) {
                //         routeMap.unhighlightRoute(routeResult.id);
                //     },
                //     sensitivity: 10
                // });

                // direction picker
                // jQuery.each(routeResult.directions, function(_, direction) {
                //     var directionHeader = jQuery("<p></p>");
                //
                //     jQuery("<span></span>")
                //         .text("to " + direction.destination)
                //         .appendTo(directionHeader);
                //
                //     if(direction.hasUpcomingScheduledService === false) {
                //         var noServiceMessage = jQuery("<div></div>")
                //             .addClass("no-service")
                //             .text("No scheduled service for the " +
                //                 getRouteShortName(routeResult) +
                //                 " to " + direction.destination + " at this time.");
                //
                //         directionHeader.append(noServiceMessage);
                //     }
                //
                //     var stopsList = jQuery("<ul></ul>")
                //         .addClass("stops")
                //         .addClass("not-loaded");
                //
                //     var loading = jQuery("<div><span>Loading...</span></div>")
                //         .addClass("loading");
                //
                //     var destinationContainer = jQuery("<p></p>")
                //         .addClass("destination")
                //         .append(directionHeader)
                //         .append(stopsList)
                //         .append(loading);

                    // load stops when user expands stop list
                    // directionHeader.click(function(e) {
                    //     loadStopsForRouteAndDirection(routeResult, direction, destinationContainer);
                    // });

                    // accordion-ize
                    // destinationContainer.accordion({ header: 'p',
                    //     collapsible: true,
                    //     active: false,
                    //     autoHeight: false });
                    //
                    // listItem.append(destinationContainer);
                //});

                // add to map
                routeMap.addRoute(routeResult);
            }

            // if (filter && routeResult.shortName !== filter && filterExistsInResults) {
            //
            //     var filteredMatch = jQuery("<li></li>").addClass("filtered-match");
            //     var link = jQuery('<a href="#' + OBA.Util.displayStopId(stopId) + '%20' + getRouteShortName(routeResult) + '">' + getRouteShortName(routeResult) + '</a>');
            //
            //     var allPolylines = [];
            //     jQuery.each(routeResult.directions, function(_, direction) {
            //         allPolylines = allPolylines.concat(direction.polylines);
            //     });
            //
            //     link.hover(function() {
            //         routeMap.showHoverPolyline(allPolylines, routeResult.color);
            //     }, function() {
            //         routeMap.removeHoverPolyline();
            //     });
            //
            //     link.appendTo(filteredMatch);
            //
            //     filteredMatches.find("ul").append(filteredMatch);
            // }
        });

       // matches.show();

        // if (filteredMatches.find("li").length > 1) {
        //     var showAll = jQuery("<li></li>").addClass("filtered-match").html('<a href="#' + OBA.Util.displayStopId(stopId) + '">See&nbsp;All</a>');
        //     filteredMatches.find("ul").append(showAll);
        //     filteredMatches.show();
        //
        //     var maxWidth = 0;
        //     jQuery.each(filteredMatches.find("li"), function(_, item) {
        //         var wrappedItem = jQuery(item);
        //         if (wrappedItem.width() > maxWidth) {
        //             maxWidth = wrappedItem.width();
        //         }
        //     });
        //     filteredMatches.find("li").width(maxWidth);
        // }
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
				var showPopup = true;
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
								routeMap.highlightStop(matches[0]);
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

    function loadMap() {
    	// initialize map, and continue initialization of things that use the map
		// on load only when google maps says it's ready.
		routeMap = OBA.RouteMap(document.getElementById("map"), function() {
			// deep link handler
			updateMap();

		}, function(routeId, serviceAlerts) { // service alert notification handler


		});
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
            loadMap();

            $('#map').hide();

		},
		loadMap: loadMap
	};
})();

jQuery(document).ready(function() { OBA.Mobile.initialize();});