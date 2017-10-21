/*
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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

// TODO module explanation

var maps = new Object();;

var autoRefresh = false;

var forceBounds = false;

var slots = [
	{
		"module": null,
		"container": ["#avl_title", "#avl_web", "#avl_timestamp", "#avl_latlon", "#avl_trip",
		              "#avl_route", "#avl_block", "#avl_schdev", "#avl_nextstopid",
		              "#avl_nextstoppred", "#avl_finalstopid", "#avl_finalstoppred",
		              "#avl_nexttripid", "#avl_error", "#avlAge"],
		"map": "avlMap",
		"options": "#avl_options"
	},
	{
		"module": null,
		"container": ["#oba_title", "#oba_web", "#oba_timestamp", "#oba_latlon", "#oba_trip",
		              "#oba_route", "#oba_block", "#oba_schdev", "#oba_nextstopid",
		              "#oba_nextstoppred", "#oba_finalstopid", "#oba_finalstoppred",
		              "#oba_nexttripid", "#oba_error", "#obaAge"],
		"map": "obaMap",
		"options": "#oba_options"
	}
];

var modules = {
		"transitime": createTransitimeModule,
		"oba": createObaModule,
		"linkavl": createLinkAvlModule
}

function initSlots() {
	
	// Add GFTS RT slots
	OBA.config.gtfsRtSources.split(",").forEach(function(source) {
		modules[source] = function() {
			createGtfsRtModule.source = source;
			return createGtfsRtModule.apply(null, arguments);
		}
		slots.forEach(function(slot) {
			var opt = jQuery(document.createElement("option"));
			opt.attr("value", source).text("GTFS-RT " + source);
			jQuery(slot.options).append(opt);
		})
	})
	
	function setSlotModule(slot, name) {
		var createModule = modules[name];
		var args = slot.container.map(function(x) { return jQuery(x); });
		args.push(slot.map);
		slot.module = createModule.apply(null, args);
	}
	
	slots.forEach(function(slot) { 
		jQuery(slot.options).change(function(evt) {
			var val = evt.target.value;
			setSlotModule(slot, val)
		})
	})
	
	var names = OBA.config.defaultModules.split(',');

	for (var i = 0; i < 2; i++) {
		var name = names[i];
		jQuery(slots[i].options).val(name);
		setSlotModule(slots[i], name);
	}

}

jQuery(function() {
	startup();
	setTimeout(refreshTimers, 1000);
});

function refresh() {
	doSearch();
}

function refreshTimers() {
	slots.forEach(function(slot) {
		if (slot.module)
			slot.module.updateAge();
	})
	triggers();
	setTimeout(refreshTimers, 1000);
}

function triggers() {
	jQuery(".stoppred").each(function() {
		var style = styleForPrediction($(this));
		$(this).css('background-color', style);
	})
}

function styleForPrediction(field) {
	var now = new Date();
	var fieldVal = field.html();
	var fieldPred = new Date();
	fieldPred.setHours(fieldVal.split(":")[0]);
	fieldPred.setMinutes(fieldVal.split(":")[1]);
	fieldPred.setSeconds(fieldVal.split(":")[2]);

	//if prediction is in future that's great
	if (fieldPred.getTime() >= now.getTime()) {
		return "#33FFCC" // green == good
	} else if (fieldPred.getTime() < now.getTime()
			&& fieldPred.getTime() > now.getTime() - 90000) {
		// if prediction is within 90 seconds past that's ok
		return "yellow"; // yellow == warning
	} else {
	    // prediction is in past
		return "#FFCCCC"; // red == bad

	}
}

function startup() {
	
	// stuff to do on load
	jQuery("#display_vehicle").click(onSearchClick);
	jQuery("#autorefresh").click(onRefreshClick);
	jQuery("#advanced").click(onAdvancedClick);
	jQuery("#clear_map").click(onClearMapClick);
	
	
	jQuery('html').bind('keypress', function(e){
		if (e.keyCode == 13) {
			onSearchClick();
			return false;
		}
	})
	
	jQuery("#vehicleAgencyId").val(OBA.config.defaultVehicleAgencyId || "1");
	jQuery("#tripAgencyId").val(OBA.config.defaultTripAgencyId || "1");
	jQuery("#stopAgencyId").val(OBA.config.defaultStopAgencyId || "1");
	
	initSlots();
}

function onClearMapClick() {
	jQuery.each(maps, function(index,value) {
		console.log("clearing layers for map " + index + ":" + value);
		if (value != null) {
			console.log("clearing map " + index);
			value.eachLayer(function(layer){
				value.removeLayer(layer);
			})
			// TODO refactor
			L.control.scale({metric: false}).addTo(value);
			L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
				attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
				maxZoom: 19
			}).addTo(value);

		} else {
			console.log("null markers for" + index);
		}
	});
}

function loadMap(latLng1, latLng2, mapName) {
	var map;
	if (maps[mapName] != undefined) {
		map = maps[mapName];
	} else {
		map = L.map(mapName);
		maps[mapName] = map;
		
		L.control.scale({metric: false}).addTo(map);
		L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
			attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
			maxZoom: 19
		}).addTo(map);
	}
	
	// if latLng2 is present, its transitime's guess at vehicle position
	if (latLng2 == null || latLng2 == undefined || latLng2.lat == 0 && latLng2.lng == 0) {
		// center map around single marker
		map.setView(latLng1, 17);
		var marker = L.marker(latLng1);
		map.addLayer(marker);
	} else if (latLng1 == null || latLng1 == undefined) {
		//console.log(mapName + " missing input");
	} else {
		console.log(mapName + " " + latLng1 + " vs " + latLng2);
		// show two marks and center between intermediate position
		var redIcon = L.Icon.extend({
			  options: {
			      iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png'
			  }
			});
		
		var marker1 = L.marker(latLng1);
		map.addLayer(marker1);

		var marker2 = L.marker(latLng2, {icon: new redIcon});
		map.addLayer(marker2);
		
		var group = new L.featureGroup([marker1, marker2]);
		// this doesn't work as well as it should so make it optional
		if (forceBounds == true) {
			var centerLat = (latLng1.lat + latLng2.lat) /2;
			var centerLon = (latLng1.lng + latLng2.lng) /2;
			map.setView(L.latLng(centerLat, centerLon), 17);
			map.fitBounds(group.getBounds());
		} else {
			map.setView(latLng1, 17);
		}
	}	
}

function onRefreshClick() {
	if (jQuery("#autorefresh").is(":checked")) {
		autoRefresh = true;
		doSearch();
	} else {
		autoRefresh = false;
	}
}

function onAdvancedClick() {
	if (jQuery("#advanced").is(":checked")) {
		jQuery(".advanced").show();
	} else {
		jQuery(".advanced").hide();
	}
}

function onSearchClick() {
	jQuery("#maps").show();
	doSearch();
}

function doSearch() {
	if (autoRefresh) {
		setTimeout(refresh, 15000);
	}
	
	var vehicleId = jQuery("#vehicleId").val();
	var stopAgencyId= jQuery("#stopAgencyId").val();
	var tripAgencyId= jQuery("#tripAgencyId").val();
	var vehicleAgencyId= jQuery("#vehicleAgencyId").val();
	var now = new Date();
	var oneMinuteAgo = new Date(now.getTime() - (1 * 60 * 1000));
	var beginDate=formatDate(oneMinuteAgo);
	var numDays=1;
	var beginTime=formatTime(oneMinuteAgo);
	
	slots.forEach(function(slot) {
		if (slot.module)
			slot.module.refresh(vehicleAgencyId, tripAgencyId, stopAgencyId, beginDate, numDays, vehicleId, beginTime);
	})
}


function formatDate(date) {
	var local = new Date(date);
    local.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return local.toJSON().slice(0, 10);
}

function formatTime(date) {
	if (date == null) return null;
	var local = new Date(date);
    local.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    if (local.toJSON() == null) return null; // invalid date
    return local.toJSON().slice(11, 19);
	
}

function formatScheduleDeviation(s) {
	var r = s;
	if (s < 120 && s > -120) {
		r = s + " s";
	} else {
		r = Math.round(s/60.0*10)/10 + " min"
	}
	if (s == 0) {
		r = r + " (ontime)";
	} else if (s > 0) {
		r = r + " (late)";
	} else {
		r = r + " (early)";
	}
	return r;
}

function updateAge(node, age) {
	var time = Math.round((new Date().getTime() - age)/1000);
	node.html("<b>Age</b>: " + time + " s");
}

function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
	  var R = 6371000; // Radius of the earth in m
	  var dLat = deg2rad(lat2-lat1);  // deg2rad below
	  var dLon = deg2rad(lon2-lon1); 
	  var a = 
	    Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
	    Math.sin(dLon/2) * Math.sin(dLon/2)
	    ; 
	  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	  var d = R * c; // Distance in km
	  return d;
	}

function deg2rad(deg) {
  return deg * (Math.PI/180)
}
