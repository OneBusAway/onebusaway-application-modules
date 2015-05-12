/*! metrics.js */
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
			for (i = 0; i < agencies.length; i++) {
				buildTable(agencies[i], data);
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

function callMetric(agency, index, path) {
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

function parseMetric(metric) {
	var obj = metric;
	var value = obj["metricValue"];
	if (value == undefined || value == null || value == "") {
		return "empty";
	}
	return value;
}

function buildTable(agency, data) {
	$('#metrics').append("<tr><td colspan='2'><b>" + agency + "</b></td></tr>");
	$.each(data, function(index, value) {
		value = value.replace(/{agencyId}/g, agency);
		if (value == "/metric/list" || value == "/metric/list-uris" || value == "/metric/list-agencies") {
			$('#metrics').append("<tr><td>" + value + "</td><td>this</td></tr>");
		} else {
			metric = callMetric(agency, index, value);
			$('#metrics').append("<tr><td>" + value + "</td><td>" + metrics[agency+"_"+index] + "</td></tr>");
		}
	})
}