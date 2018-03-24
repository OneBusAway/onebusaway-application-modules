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
jQuery(function() {
	//toggle Deploy deploy progress list
	jQuery("#deployConfig #deployConfig_progress #expand").bind({
			'click' : toggleDeployConfigResultList});

	jQuery("#deployConfig_listButton").click(onDeployListClick);
	jQuery("#deployConfig_deployButton").click(onDeployClick);

	onDeployListClick();
});

function toggleDeployConfigResultList() {
	var $image = jQuery("#deployConfig #deployConfig_progress #expand");
	changeImageSrc($image);
	//Toggle progress result list
	jQuery("#deployConfig #deployConfig_resultList").toggle();
} // end toggle


function onDeployListClick() {
	var environment = jQuery("#deploy_environment").text();
	jQuery.ajax({
		url: "../api/config/deploy/list/depot/" + environment + "?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
				var configResponse = response;
				if (configResponse != undefined) {
					var txt = "<ul>";
					// the header is set wrong for the proxied object, run eval to correct
					if (typeof response=="string") {
						configResponse = eval('(' + response + ')');
					}
					// parse array of deploy names
					var size = configResponse.length;
					if (size > 0) {
						for (var i=0; i<size; i++) {
							txt = txt + "<li>" + configResponse[i] + "</li>";
						}
					}
					txt = txt + "</ul>";
					jQuery("#listDepots").html(txt).css("font-size", "12px");	

				}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again");
		}
	});
	jQuery.ajax({
		url: "../api/config/deploy/list/dsc/" + environment + "?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
				var configResponse = response;
				if (configResponse != undefined) {
					var txt = "<ul>";
					// the header is set wrong for the proxied object, run eval to correct
					if (typeof response=="string") {
						configResponse = eval('(' + response + ')');
					}
					// parse array of deploy names
					var size = configResponse.length;
					if (size > 0) {
						for (var i=0; i<size; i++) {
							txt = txt + "<li>" + configResponse[i] + "</li>";
						}
					}
					txt = txt + "</ul>";
					jQuery("#listDscs").html(txt).css("font-size", "12px");	

				}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again");
		}
	});
} // end onDeployList

function onDeployClick() {
	var environment = jQuery("#deploy_environment").text();

	jQuery.ajax({
		url: "../api/config/deploy/from/" + environment + "?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
				var deployResponse = response;
				if (deployResponse != undefined) {
					// the header is set wrong for the proxied object, run eval to correct
					if (typeof response=="string") {
						deployResponse = eval('(' + response + ')');
					}
					jQuery("#deployConfig_resultList").html("calling...");
					jQuery("#deployConfig_id").text(deployResponse.id);
					jQuery("#deployResultsHolder #deployResults").show().css("display","block");
					jQuery("#deployConfig #requestLabels").show().css("display","block");
					jQuery("#deployContentsHolder #deployBox #deploying").show().css("display","block");
					jQuery("#deployResultsHolder #deployResults #deployConfig_progress #deploy_msg").text("(In Progess)");
					jQuery("#deployConfig_deployProgress").text("Deploying ...");
					jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../css/img/ajax-loader.gif");
					window.setTimeout(updateDeployStatus, 5000);
				} else {
					jQuery("#deployConfig_id").text(error);
					jQuery("#deployConfig_resultList").html("error");
				}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again");
		}
	});
} // end onDeployClick

function updateDeployStatus() {
	id = jQuery("#deployConfig_id").text();
	jQuery.ajax({
		url: "../api/config/deploy/status/" + id + "/list?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
				var txt = "<ul>";
				var deployResponse = response;
				if (deployResponse == null) {
					jQuery("#deployConfig_deployProgress").text("Deploy Complete!");
					jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../css/img/dialog-warning-4.png");
					jQuery("#deployConfig_resultList").html("unknown id=" + id);
					return;
				}
				// the header is set wrong for the proxied object, run eval to correct
				if (typeof response=="string") {
					deployResponse = eval('(' + response + ')');
				}
				if (deployResponse.status != "complete" && deployResponse.status != "error") {
					window.setTimeout(updateDeployStatus, 5000); // recurse
				} else {
					toggleDeployConfigResultList();
					jQuery("#deployResultsHolder #deployResults #deployConfig_progress").show().css("display","block");
					jQuery("#deployResultsHolder #deployResults #deployConfig_resultList").show().css("display","block");
					if (deployResponse.status == "complete") {
						jQuery("#deployResultsHolder #deployResults #deployConfig_progress #deploy_msg").text("Files Deployed:");
						jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../css/img/dialog-accept-2.png");
						jQuery("#deployConfig_deployProgress").text("Deploy Complete!");
						// set resultList to deployNames list
						var size = deployResponse.depotIdMapNames.length;
						if (size > 0) {
							for (var i=0; i<size; i++) {
								txt = txt + "<li>" + deployResponse.depotIdMapNames[i] + " (depot id map)</li>";
							}
						}
						size = deployResponse.dscFilenames.length;
						if (size > 0) {
							for (var i=0; i<size; i++) {
								txt = txt + "<li>" + deployResponse.dscFilenames[i] + " (dsc file)</li>";
							}
						}
					} else {
						jQuery("#deployResultsHolder #deployResults #deployConfig_progress #deploy_msg").text("Error Message:");
						jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../css/img/dialog-warning-4.png");
						jQuery("#deployConfig_deployProgress").text("Deploy Failed!");
						// we've got an error
						txt = txt + "<li><font color=\"red\">ERROR!  Please consult the logs and check the "
							+ "filesystem permissions before continuing</font></li>";
					}
				}
				txt = txt + "</ul>";
				jQuery("#deployConfig_resultList").html(txt).css("font-size", "12px");	
		},
		error: function(request) {
			clearTimeout(timeout);
			toggleDeployConfigResultList();
			jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../css/img/dialog-warning-4.png");
			jQuery("#deployConfig_deployProgress").text("Deploy Failed!");
			jQuery("#deployResultsHolder #deployResults #deployConfig_progress").show().css("display","block");
			jQuery("#deployResultsHolder #deployResults #deployConfig_resultList").show().css("display","block");

			// error out on a 500 error, the session will be lost so it will not recover
			var txt = "<ul>";
			txt = txt + "<li><font color=\"red\">The server returned an internal error.  Please consult the logs" 
				+ " or retry your request</font></li>";
			txt = txt + "</ul>";
			jQuery("#deployConfig_resultList").html(txt).css("font-size", "12px");
		}
	});
} // end update

function changeImageSrc($image) {
	
	var $imageSource = $image.attr("src");
	if($imageSource.indexOf("right-3") != -1) {
		//Change the img to down arrow
		$image.attr("src", "../css/img/arrow-down-3.png");
	} else {
		//Change the img to right arrow
		$image.attr("src", "../css/img/arrow-right-3.png");
	}
} // end change


