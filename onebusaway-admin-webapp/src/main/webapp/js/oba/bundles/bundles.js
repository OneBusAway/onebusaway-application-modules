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

var timeout = null;
var agencyMetadataAvailable = false;
var agencyMetadata;		//For agency metadata
var selectedDirectory = "";  //Selected on Choose tab, used by Upload tab
var destinationDirectory = ""; //For "copy" on Choose tab, used by Upload tab
var userComments = "";		// User comments about the selected dataset
var buildBundleId = "";		// Build request id, stored in info.json.
var fromResultLink = false; // If this was called with a Result Link
// For Fixed Route Comparison Report on Compare tab
var currentReportDataset = "";
var currentReportBuildName = "";
var currentArchivedReportDataset = "";
var currentArchivedReportBuildName = "";
var compareToDataset =  "";
var compareToBuildName =  "";
var compareToArchivedDataset =  "";
var compareToArchivedBuildName =  "";
var csrfParameter = "";
var csrfHeader = "";
var csrfToken = "";


jQuery(function() {
	// these are provided by sec:csrfMetaTags
	csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
	csrfHeader = $("meta[name='_csrf_header']").attr("content");
	csrfToken = $("meta[name='_csrf']").attr("content");

	//Initialize tabs
	jQuery("#tabs").tabs();

	// check if we were called with a hash -- re-enter from email link
	if (window.location.hash) {
		var hash = window.location.hash;
		hash = hash.split('?')[0];
		// TODO this doesn't work when fromEmail query string is present 
		// alert("hash=" + hash);
		$(hash).click();
	}
	// politely set our hash as tabs are changed
	jQuery("#tabs").bind("tabsshow", function(event, ui) {
		window.location.hash = ui.tab.hash;
		if (ui.tab.hash == "#Deploy") {
			// when deploy tab clicked, pre-load some data
			jQuery("#deployBundle_listButton").click();
			jQuery("#deployBundle_listCurrentButton").click();
		}
	});

	initCreate();
	initUpload();
	initValidate();
	initBuild();
	initCompare();
	initStage();
	initDeploy();
	initDownload();
	initSync();


	jQuery("#prevalidate_continue").click(onPrevalidateContinueClick);

	jQuery("#upload_continue").click(onUploadContinueClick);

	jQuery("#build_continue").click(onBuildContinueClick);

	jQuery("#stage_continue").click(onStageContinueClick);

	jQuery("#deploy_continue").click(onDeployContinueClick);

	var qs = parseQuerystring();
	if (qs["fromEmail"] == "true") {
		//alert("called from email!");
		fromResultLink = true;
		buildBundleId = qs["id"];
		updateBuildStatus("test");
		jQuery("#prevalidate_id").text(qs["id"]);
		jQuery("#buildBundle_id").text(qs["id"]);
		jQuery("#prevalidate_bundleName").val(qs["name"]);
		jQuery("#Build #bundleBuildName").val(qs["name"]);
		jQuery("#Build #startDatePicker").val(qs["startDate"]);
		jQuery("#Build #endDatePicker").val(qs["endDate"]);
		// Reshow hidden result elements
		$("#buildBundle #buildingTest").show();
		$("#buildBundle_testResultLink").show();
		$("#bundleTestResultsHolder").show();
		$("#testProgressBarDiv").show();
		// just in case set the tab
		var $tabs = jQuery("#tabs");
		$tabs.tabs('select', 3);
	}

});



//Helper Method for Agency Row
function showThisAgency(agency){
	var new_row = '<tr> \
		<td><div><input type="checkbox" /></div></td> \
		<td><input type="text" class="agencyId"/></td> \
		<td><select class="agencyDataSourceType"> \
		<option value="gtfs">gtfs</option> \
		<option value="aux">aux</option> \
		</select></td> \
		<td><select class="agencyProtocol"> \
		<option value="http">http</option> \
		<option value="ftp">ftp</option> \
		<option value="file">file</option> \
		</select></td> \
		<td><input type="text" class="agencyDataSource"/></td> \
		</tr>';
	$('#agency_data').append(new_row);	
}










function enableContinueButton(continueButton) {
	jQuery(continueButton).removeAttr("disabled").css("color", "#000");
}

function disableContinueButton(continueButton) {
	jQuery(continueButton).attr("disabled", "disabled").css("color", "#999");
}

function toggleAdvancedOptions() {
	var $image = jQuery("#createDirectory #advancedOptions #expand");
	changeImageSrc($image);
	//Toggle advanced options box
	jQuery("#advancedOptionsContents").toggle();
}

function toggleValidationResultList() {
	var $image = jQuery("#prevalidateInputs #prevalidate_progress #expand");
	changeImageSrc($image);
	//Toggle progress result list
	jQuery("#prevalidateInputs #prevalidate_resultList").toggle();
}

function toggleBuildBundleResultList(event) {
	if (event.data.buildType == "test") {
		jQuery("#buildBundle_testResultList").toggle();
	} else {
		jQuery("#buildBundle_finalResultList").toggle();
	}
}

function toggleDeployBundleResultList() {
	var $image = jQuery("#deployBundle #deployBundle_progress #expand");
	changeImageSrc($image);
	//Toggle progress result list
	jQuery("#deployBundle #deployBundle_resultList").toggle();
}

//add support for parsing query string
function parseQuerystring (){
	var nvpair = {};
	var qs = window.location.hash.replace('#', 'hash=');
	qs = qs.replace('?', '&');
	var pairs = qs.split('&');
	$.each(pairs, function(i, v){
		var pair = v.split('=');
		nvpair[pair[0]] = pair[1];
	});
	return nvpair;
}

//retrieve transit agency metadata from server
function getAgencyMetadata(){
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/agency/list",
		type: "GET",
		async: false,
		success: function(response) {
			agencyMetadata = response;
			if (agencyMetadata.length > 0) {
				agencyMetadataAvailable = true;
				addUploadFileAgencyDropdown();
				$("#agency_data tr:last .agencyId").hide();
				jQuery(".agencyIdSelect").change(onAgencyIdSelectClick);
				var url = agencyMetadata[0].gtfsFeedUrl;
				if (url == null) {
				  url = "";
				}
				if (url.toLowerCase().substring(0,4) === 'http'
					|| url.toLowerCase().substring(0,3) === 'ftp') {
					$("#agency_data tr:last .agencyDataSource").val(url);
				}	
			}
		},
		error: function(request) {
			console.log("There was an error trying to retrieve agency metadata");
		}
	});
}
function addUploadFileAgencyDropdown() {
	console.log("starting addUploadFileAgencyDropdown");
	agencyDropDown = $('<select class="agencyIdSelect">');
	for (var i = 0; i < agencyMetadata.length; i++) {
		var name = agencyMetadata[i].name;
		name += " (";
		name += agencyMetadata[i].shortName;
		name += ")";
		agencyDropDown.append(jQuery("<option>").attr('value',agencyMetadata[i].legacyId).text(name));
	};
	agencyDropDown.insertBefore("#agencyId");
}
