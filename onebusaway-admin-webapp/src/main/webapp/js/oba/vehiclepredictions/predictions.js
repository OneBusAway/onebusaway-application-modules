/**
 * Copyright (c) 2016 Cambridge Systematics, Inc.
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

jQuery(function() {
	startup();
});


function startup() {
	console.log("startup");
	// stuff to do on load
	jQuery("#display_vehicle").click(onSearchClick);
}

function onSearchClick() {
	console.log("search click");
	var vehicleId = jQuery("#vehicleId").val();
	var agencyId="1";
	var beginDate=new Date().toISOString().substring(0,10);
	var numDays=1;
	var beginTime=new Date().toISOString().substring(11,19);
	var domain="prod.wmata.obaweb.org";
	var port=8080;
	var url= "http://gtfsrt." + domain + ":" + port + "/web/reports/avlJsonData.jsp?a="
	+ agencyId + "&beginDate=" + beginDate + "&numDays=" + numDays + "&v=" + vehicleId + 
	"&beginTime=" + encodeURI(beginTime);
	console.log("getting " + url);
	jQuery.ajax({
		url: url,
		type: "GET",
		async: false,
		success: function(response) {
			console.log(response);
		}
	});
}
