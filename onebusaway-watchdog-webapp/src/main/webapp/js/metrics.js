/*
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
jQuery(function() {
	var agencies;
	var urls;
	var metrics = {};
	findAgencies();
	findUrls();
});

function findUrls() {
	jQuery.ajax({
		url: "/onebusaway-watchdog-webapp/api/metric/list-uris",
		type: "GET",
		async: false,
		success: function(data) {
			var general = {};
			var specific = {};
			for (i = 0; i<data.length; i++) {
				if (data[i].indexOf("{agencyId}") > -1) {
					specific[i] = data[i];
				} else {
					general[i] = data[i];
				}
			}
			buildGeneralTable(general)
			for (i = 0; i < agencies.length; i++) {
				buildAgencyTable(agencies[i], specific);
			}
			
		},
		error: function(error) {
			console.log("exception retrieving uris:" + error);
		}
	})
}

function findAgencies() {
	jQuery.ajax({
		url: "/onebusaway-watchdog-webapp/api/metric/list-agencies",
		type: "GET",
		async: false,
		success: function(data) {
			agencies = data;
		},
		error: function(error) {
			console.log("exception retrieving agencies:" + error);
		}
	})
}

function callAgencyMetric(agency, index, path) {
	jQuery.ajax({
		url: "/onebusaway-watchdog-webapp/api" + path,
		type: "GET",
		async: false,
		success: function(data) {
			metrics[agency+"_"+index] = parseMetric(data);
		},
		error: function(error) {
			console.log("callMetric exception:" + error);
			return "error"
		}
	})
}

function callMetric(index, path) {
	jQuery.ajax({
		url: "/onebusaway-watchdog-webapp/api" + path,
		type: "GET",
		async: false,
		success: function(data) {
			metrics[index] = parseMetric(data);
		},
		error: function(error) {
			console.log("callMetric exception:" + error);
			return "error"
		}
	})
}


function parseMetric(metric) {
	var obj = metric;
	var value = obj["metricValue"];
	if (value instanceof String && value.length > 40) {
		value = value.substring(0, 40);
		value = value + "...";
	}
	var MAX_LENGTH = 10;
	if (value instanceof Array && value.length > MAX_LENGTH) {
		var short = [];
		for (var i = 0; i < MAX_LENGTH; i++) {
			short[i] = value[i];
		}
		short[MAX_LENGTH] = "...";
		return short;
	}
	if (value == 2147483647)
		value = "NaN"
	return value;
}

function formatLinkName(link) {
	var last = link.lastIndexOf("/");
	if (last > -1) {
		if (link.indexOf("/stop/") > -1) {
			return link.substring(last+1, last.length) + " (stops)";
		}
		if (link.indexOf("/trip/") > -1) {
			return link.substring(last+1, last.length) + " (trips)";
		}
		return link.substring(last+1, last.length);
	}
	return link;
}

function buildGeneralTable(data) {
	$('#metrics').append("<tr><td colspan='2'><b>General</b></td></tr>");
	$.each(data, function(index, value) {
		if (!(value == "/metric/list" || value == "/metric/list-uris" || value == "/metric/list-agencies")) {
			metric = callMetric(index, value);
			name = formatLinkName(value);
			link = "/onebusaway-watchdog-webapp/api" + value;
			$('#metrics').append("<tr><td><a href=\"" + link + "\">" + name + "</a></td><td>" + metrics[index] + "</td></tr>");
		}
	})
}

function buildAgencyTable(agency, data) {
	$('#metrics').append("<tr><td colspan='2'><b>" + agency + "</b></td></tr>");
	$.each(data, function(index, value) {
		value = value.replace(/{agencyId}/g, agency);
		metric = callAgencyMetric(agency, index, value);
		name = formatLinkName(value);
		link = "/onebusaway-watchdog-webapp/api" + value;
		if (value.indexOf("invalid-lat-lons") > -1) {
			$('#metrics').append("<tr><td><a href=\"" + link + "\">" + name + "</a></td><td>" + "..." + "</td></tr>");
		} else {
			$('#metrics').append("<tr><td><a href=\"" + link + "\">" + name + "</a></td><td>" + metrics[agency+"_"+index] + "</td></tr>");
		}
	})
}