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

	//Initialize date pickers
	jQuery("#startDatePicker").datepicker(
			{ 
				dateFormat: "yy-mm-dd",
				altField: "#startDate",
				onSelect: function(selectedDate) {
					jQuery("#endDatePicker").datepicker("option", "minDate", selectedDate);
				}
			});
	jQuery("#endDatePicker").datepicker(
			{
				dateFormat: "yy-mm-dd",
				altField: "#endDate",
				onSelect: function(selectedDate) {
					jQuery("#startDatePicker").datepicker("option", "maxDate", selectedDate);
				}
			});

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
	});

	// Set values for dataset select lists on Compare tab
	$("#currentDatasetList > option").each(function(index, value) {
		$(this).val(index);
	});
	$("#compareToDatasetList > option").each(function(index, value) {
		$(this).val(index);
	});
	$("#currentArchivedDatasetList > option").each(function(index, value) {
		$(this).val(index);
	});
	$("#compareToArchivedDatasetList > option").each(function(index, value) {
		$(this).val(index);
	});
	$('#Compare #buildingReportDiv').hide();
	$("#currentArchivedDatasetList").hide();
	$("#currentArchivedBuildNameList").hide();
	$("#compareToArchivedDatasetList").hide();
	$("#compareToArchivedBuildNameList").hide();
	
    $('#Compare #useArchiveCheckbox').change(function() {
    	$("#diffResultsTable tbody").empty();
    	$('#fixedRouteDiffTable tbody').empty();
        if($(this).is(":checked")) {
        	$("#currentDatasetList").hide();
        	$("#currentBuildNameList").hide();
        	$("#compareToDatasetList").hide();
        	$("#compareToBuildNameList").hide();
        	$("#currentArchivedDatasetList").show();
        	$("#currentArchivedBuildNameList").show();
        	$("#compareToArchivedDatasetList").show();
        	$("#compareToArchivedBuildNameList").show();
    		if (currentArchivedReportDataset && currentArchivedReportBuildName
    				&& compareToArchivedDataset && compareToArchivedBuildName) {
    			buildDiffReport();
    		}
        } else {
        	$("#currentArchivedDatasetList").hide();
        	$("#currentArchivedBuildNameList").hide();
        	$("#compareToArchivedDatasetList").hide();
        	$("#compareToArchivedBuildNameList").hide();
        	$("#currentDatasetList").show();
        	$("#currentBuildNameList").show();
        	$("#compareToDatasetList").show();
        	$("#compareToBuildNameList").show();
    		if (currentReportDataset && currentReportBuildName
    				&& compareToDataset && compareToBuildName) {
    			buildDiffReport();
    		}
        }
	});

	jQuery("#currentDirectories").selectable({ 
		stop: function() {
			var names = $.map($('#listItem.ui-selected strong, this'), function(element, i) {
				return $(element).text();  
			});
			if (names.length > 0) {
				var $element = jQuery("#createDirectory #directoryName");
				// only return the first selection, as multiple selections are possible
				$element.attr("value", names[0]);
				jQuery("#createDirectory #createDirectoryContents #createDirectoryResult").show().css("display","block");
				jQuery("#createDirectoryResult #resultImage").attr("src", "../../css/img/warning_16.png");

				if(jQuery("#select").is(":checked")){
					jQuery("#createDirectoryMessage").text("Click Select button to load your directory")
					.css("font-weight", "bold").css("color", "red");
					//Enable select button			
					enableSelectButton();
				}

				//Do little different if its copy.
				if (jQuery("#copy").is(":checked")) {					
					jQuery("#createDirectoryMessage").text("Click Copy to copy directory contents")
					.css("font-weight", "bold").css("color", "red");	
					//Enable select button			
					enableSelectButton();
				}
			}
		}
	});

	jQuery("#compareCurrentDirectories").selectable({
		stop: function() {
			var names = $.map($('#compareListItem.ui-selected strong, this'), function(element, i) {  
				return $(element).text();  
			}); 
			if (names.length > 0) {
				var data = {};
				data[csrfParameter] = csrfToken;
				data["selectedBundleName"] = names[0];

				jQuery.ajax({
					url: "manage-bundles!existingBuildList.action",
					data: data,
					type: "GET",
					async: false,
					success: function(data) {
						$('#compareSelectedBuild').text('');
						$('#diffResult').text('');
						$.each(data, function(index, value) {
							$('#compareSelectedBuild').append(
									"<div id=\"compareBuildListItem\"><div class=\"listData\"><strong>"+value+"</strong></div></div>");
						});
					}
				})
			}
		}
	});

	jQuery("#compareSelectedBuild").selectable({
		stop: function() {
			var bundleNames = $.map($('#compareListItem.ui-selected strong, this'), function(element, i) {  
				return $(element).text();  
			}); 
			var buildNames = $.map($('#compareBuildListItem.ui-selected strong, this'), function(element, i) {  
				return $(element).text();  
			});
			if (buildNames.length > 0) {
				// Clear any previous results from the tables
				$('#diffResultsTable tr').slice(1).remove();
				$('#fixedRouteDiffTable tr').slice(1).remove();
				var data = {};
				data[csrfParameter] = csrfToken;
				data["datasetName"] = selectedDirectory;
				data["buildName"] = jQuery("#bundleBuildName").val();
				data["datasetName2"] = bundleNames[0];
				data["buildName2"] = buildNames[0];

				jQuery.ajax({
					url: "compare-bundles!diffResult.action",
					data: data,
					type: "GET",
					async: false,
					success: function(data) {
						$.each(data.diffResults, function(index, value) {
							// Skip first three rows of results
							if (index >= 3) {
								var diffRow = formatDiffRow(value);
								$("#diffResultsTable").append(diffRow);
							}
						});
						var baseBundle = selectedDirectory + " / " + jQuery("#bundleBuildName").val();
						var compareToBundle = bundleNames[0] + " / " + buildNames[0];
						$("#baseBundle").text(baseBundle + " (green)");
						$("#compareToBundle").text(compareToBundle + " (red)");
						$.each(data.fixedRouteDiffs, function(index, value) {
							var modeName = value.modeName;
							var modeClass = "";
							var modeFirstLineClass=" modeFirstLine";
							var addSpacer = true;
							if (value.srcCode == 1) {
								modeClass = "currentRpt";
							} else if (value.srcCode == 2) {
								modeClass = "selectedRpt";
							}
							$.each(value.routes, function(index2, value2) {
								var routeNum = value2.routeNum;
								var routeName = value2.routeName;
								var routeFirstLineClass=" routeFirstLine";
								addSpacer = false;
								if (index2 > 0) {
									modeName = "";
									modeFirstLineClass = "";
								}
								var routeClass = modeClass;
								if (value2.srcCode == 1) {
									routeClass = "currentRpt";
								} else if (value2.srcCode == 2) {
									routeClass = "selectedRpt";
								}
								$.each(value2.headsignCounts, function(headsignIdx, headsign) {
									var headsignName = headsign.headsign;
									var headsignBorderClass = "";
									if (headsignIdx > 0) {
										modeName = "";
										routeNum = "";
										routeName = "";
										modeFirstLineClass = "";
										routeFirstLineClass = "";
										headsignBorderClass = " headsignBorder";
										addSpacer = false;
									}
									var headsignClass = routeClass;
									if (headsign.srcCode == 1) {
										headsignClass = "currentRpt";
									} else if (headsign.srcCode == 2) {
										headsignClass = "selectedRpt";
									}
									$.each(headsign.dirCounts, function(dirIdx, direction) {
										var dirName = direction.direction;
										var dirBorderClass = "";
										if (dirIdx > 0) {
											modeName = "";
											routeNum = "";
											routeName = "";
											headsignName = "";
											modeFirstLineClass = "";
											routeFirstLineClass = "";
											headsignBorderClass = "";
											dirBorderClass = " dirBorder";
											addSpacer = false;
										}
										var dirClass = headsignClass;
										if (direction.srcCode == 1) {
											dirClass = "currentRpt";
										} else if (direction.srcCode == 2) {
											dirClass = "selectedRpt";
										}
										$.each(direction.stopCounts, function(index3, value3) {
											var stopCt = value3.stopCt;
											var stopClass = "";
											if (dirClass == "currentRpt") {
												stopClass = "currentStopCt";
											} else if (dirClass == "selectedRpt") {
												stopClass = "selectedStopCt";
											}
											if (value3.srcCode == 1) {
												stopClass = "currentStopCt";
											} else if (value3.srcCode == 2) {
												stopClass = "selectedStopCt";
											}
											var weekdayTrips = value3.tripCts[0];
											var satTrips = value3.tripCts[1];
											var sunTrips = value3.tripCts[2];
											if (index3 > 0) {
												modeName = "";
												modeFirstLineClass = "";
												routeNum = "";
												routeName = "";
												headsignName = "";
												dirName = "";
												routeFirstLineClass = "";
												headsignBorderClass = "";
												dirBorderClass = "";
												addSpacer = false;
											}
											if (index > 0 && headsignIdx == 0
													&& dirIdx == 0 && index3 == 0) {
												addSpacer = true;
											}
											if (addSpacer) {
												var new_spacer_row = '<tr class="spacer"> \
													<td></td> \
													<td></td> \
													<td></td> \
													<td></td> \
													<td></td> \
													<td></td> \
													<td></td> \
													<td></td> \
													<td></td> \
													</tr>';
												$('#fixedRouteDiffTable').append(new_spacer_row);
											}
											var new_row = '<tr class="fixedRouteDiff' + modeFirstLineClass + routeFirstLineClass + '"> \
												<td class="' + modeClass + ' modeName" >' + modeName + '</td> \
												<td class="' + routeClass + routeFirstLineClass + ' rtNum" >' + routeNum + '</td> \
												<td class="' + routeClass + routeFirstLineClass + '">' + routeName + '</td> \
												<td class="' + headsignClass + routeFirstLineClass + headsignBorderClass + '">' + headsignName + '</td> \
												<td class="' + dirClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + dirName + '</td> \
												<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + stopCt + '</td> \
												<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + weekdayTrips + '</td> \
												<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + satTrips + '</td> \
												<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + sunTrips + '</td> \
												</tr>';
											$('#fixedRouteDiffTable').append(new_row);
										});
									});
								});
							});
						});
						// Add bottom border to reprot
						var new_spacer_row = '<tr class="spacer"> \
							<td></td> \
							<td></td> \
							<td></td> \
							<td></td> \
							<td></td> \
							<td></td> \
							<td></td> \
							<td></td> \
							</tr>';
						$('#fixedRouteDiffTable').append(new_spacer_row);
					}
				})
			}
		}
	});

	jQuery("#create_continue").click(onCreateContinueClick);

	jQuery("#prevalidate_continue").click(onPrevalidateContinueClick);

	jQuery("#upload_continue").click(onUploadContinueClick);

	jQuery("#build_continue").click(onBuildContinueClick);

	jQuery("#stage_continue").click(onStageContinueClick);

	jQuery("#deploy_continue").click(onDeployContinueClick);

	jQuery('#Create #filenameError').hide();

	// hookup ajax call to select
	jQuery("#newDirectoryButton").click(onCreateDatasetClick);

	// On choose tab, only one existing directory can be selected at a time
	jQuery("#existingDataset tr input:checkbox").click(function () {
		var state = $(this).prop("checked");
		$(this).parent().parent().parent().find('input:checked').prop("checked", false);
		$(this).prop("checked", state);
		// If this item is selected, enable the "Add files..." button
		if (state == true) {
			jQuery("#existingDirectoryButton").removeAttr("disabled").css("color", "#000");
		} else {
			jQuery("#existingDirectoryButton").attr("disabled", "disabled").css("color", "#999");
		}
	});

	// use an existing dataset
	jQuery("#existingDirectoryButton").click(onExistingDatasetClick);

	// copy existing dataset to a new directory
	jQuery(".copyDirectory").click(onCopyExistingDatasetClick);

	// copy existing dataset to a new directory
	jQuery(".deleteDirectory").click(onDeleteExistingDatasetClick);
	
	// upload bundle source data for selected agency
	jQuery("#uploadButton").click(onUploadSelectedAgenciesClick);

	// add another row to the list of agencies and their source data
	jQuery("#addAnotherAgency").click(onAddAnotherAgencyClick);

	// toggle agency row as selected when checkbox is clicked
	jQuery("#agency_data").on("change", "tr :checkbox", onSelectAgencyChange);

	// change input type to 'file' if protocol changes to 'file'
	jQuery("#agency_data").on("change", "tr .agencyProtocol", onAgencyProtocolChange);

	jQuery("#addNewAgency").click(onAddNewAgencyClick);

	// remove selected agencies
	jQuery("#uploadFiles #agency_data").on('click', '.removeAgency', onRemoveSelectedAgenciesClick);

	// popup the Comments box
	jQuery("#anyNotes").click(onAnyCommentsClick);

	// if bundle comment has changed, save it to info.json
	jQuery("#Upload #bundleComment").change(onBundleCommentChanged);

	//toggle advanced option contents
	jQuery("#createDirectory #advancedOptions #expand").bind({
		'click' : toggleAdvancedOptions	});

	//initially hide the Request Id label if the Request Id is blank
	if (jQuery("#prevalidate_id").text().length == 0) {
		jQuery("#prevalidate_id_label").hide();
	}

	//initially disable the Validate button on the Pre-validate tab
	disableValidateButton();

	//if bundle build name is entered, enable the Validate button
	jQuery("#Validate #prevalidate_bundleName").on("input propertychange", onBundleNameChanged);
	jQuery("#Build #bundleBuildName").on("input propertychange", onBundleNameChanged);

	//initially hide the Validation Progress label
	jQuery("#prevalidate_progress").hide();

	//toggle validation progress list
	jQuery("#prevalidateInputs #prevalidate_progress #expand").bind({
		'click' : toggleValidationResultList});

	//toggle bundle build progress list
	jQuery("#buildBundle #buildBundle_buildTestProgress #expand").bind({
		'click' : toggleBuildBundleResultList});

	//handle create, select and copy radio buttons
	jQuery("input[name='options']").change(directoryOptionChanged);

	//Handle validate button click event
	jQuery("#prevalidateInputs #validateBox #validateButton").click(onValidateClick);

	//Handle build button click event
	jQuery("#Build #testBuildBundleButton").click({buildType: "test"}, onBuildClick);
	jQuery("#Build #finalBuildBundleButton").click({buildType: "final"}, onBuildClick);

	// Toggle the test and final build details textarea
	jQuery("#viewTestBuildDetails").click({buildType: "test"}, toggleBuildBundleResultList);
	jQuery("#viewFinalBuildDetails").click({buildType: "final"}, toggleBuildBundleResultList);


	//Enable or disable create/select button when user enters/removes directory name
	//For a copy, a value must also be provided for the destination directory
	//Using bind() with propertychange event as live() does not work in IE for unknown reasons
	jQuery("#createDataset #directoryName").bind("input propertychange", function() {
		var text = jQuery("#createDataset #directoryName").val();
		var validDatasetNameExp = /^[a-zA-Z0-9_-]+$/;
		jQuery('#Create #filenameError').hide();
		disableSelectButton();
		if (text.length > 0) {
			if (text.match(validDatasetNameExp)) {
				if (!jQuery("#copy").is(":checked") || copyDestText.length > 0) {
					enableSelectButton();
				}
			} else {
				jQuery('#Create #filenameError').show();
				jQuery("#createDirectory #createDirectoryContents #createDirectoryResult").hide();
			}
		}
	});

	//Enable "Continue" button when user enters a destination name for copying
	//a dataset. If the name is removed or invalid, disable the "Continue" button.
	//Using bind() with propertychange event as live() does not work in IE for unknown reasons
	jQuery("#destinationDirectory").bind("input propertychange", function() {
		var text = jQuery("#destinationDirectory").val();
		var validDatasetNameExp = /^[a-zA-Z0-9_-]+$/;
		jQuery('#copyFilenameError').hide();
		$("#copyContinue").button("disable");
		if (text.length > 0) {
			if (text.match(validDatasetNameExp)) {
				$("#copyContinue").button("enable");
			} else {
				jQuery('#copyFilenameError').show();
			}
		}
	});

	// On Compare tab
	jQuery("#currentDatasetList").on("change", onCurrentDatasetChange);
	jQuery("#currentBuildNameList").on("change", onCurrentBuildNameChange);

	jQuery("#currentArchivedDatasetList").on("change", onCurrentArchivedDatasetChange);
	jQuery("#currentArchivedBuildNameList").on("change", onCurrentArchivedBuildNameChange);

	jQuery("#compareToDatasetList").on("change", onCompareToDatasetChange);
	jQuery("#compareToBuildNameList").on("change", onCompareToBuildNameChange);

	jQuery("#compareToArchivedDatasetList").on("change", onCompareToArchivedDatasetChange);
	jQuery("#compareToArchivedBuildNameList").on("change", onCompareToArchivedBuildNameChange);

	jQuery("#printFixedRouteRptButton").click(onPrintFixedRouteRptClick);

	disableStageButton();
	disableDownloadButton();
	disableBuildButtons();

	//toggle bundle staging progress list
	jQuery("#stageBundle #stageBundle_progress #expand").bind({
		'click' : toggleStageBundleResultList});


	//Handle stage button click event
	jQuery("#stageBundle_stageButton").click(onStageClick);


	//toggle bundle deploy progress list
	jQuery("#deployBundle #deployBundle_progress #expand").bind({
		'click' : toggleDeployBundleResultList});

	//Handle deploy button click event
	jQuery("#deployBundle_deployButton").click(onDeployClick);
	jQuery("#deployBundle_listButton").click(onDeployListClick);
	onDeployListClick();
	
	//Handle download button click event
	jQuery("#downloadBundle_downloadButton").click(onDownloadBundleClick);

	//Handle sync button click event
	jQuery("#syncBundle_syncButton").click(onSyncDeployedBundleClick);

	//Retrieve transit agency metadata
	getAgencyMetadata();
	
	// Retrieve dataset name and build name for bundle currently deployed on staging.
	//getDeployedOnStagingBundleInfo();
	
	// For "Copy" popup to specify a destination directory
	$("#copyPopup").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		buttons: [{
			id: "copyCancel",
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		},
		{
			id: "copyContinue",
			text: "Continue",
			click: function() {
				destinationDirectory = $("#destinationDirectory").val();
				$(this).dialog("close");
				onCopyDestinationSpecified();
			}
		}],
        open: function() {
            $('.ui-dialog-buttonpane').find('button:contains("Cancel")').addClass('cancelCopyPopup');
        }		
	});
	
	// For "Delete" popup to confirm deleting the directory
	$("#deletePopup").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		buttons: [{
			id: "deleteCancel",
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		},
		{
			id: "deleteContinue",
			text: "Delete dataset",
			click: function() {
				$(this).dialog("close");
				onDeleteDatasetConfirmed();
			}
		}],
        open: function() {
            $('.ui-dialog-buttonpane').find('button:contains("Cancel")').addClass('cancelDeletePopup');
        }
	});

	// For "Delete Success" popup to confirm the directory was deleted
	$("#deleteSuccessPopup").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		buttons: [{
			id: "deleteSuccessCancel",
			text: "Continue",
			click: function() {
				$(this).dialog("close");
			}
		}],
        open: function() {
            $('.ui-dialog-buttonpane').find('button:contains("Continue")').addClass('cancelDeletePopup');
        }
	});

	// For "Add Comments" popup to add user commments about a dataset
	$("#addCommentsPopup").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		buttons: {
			"Cancel": function() {
				$(this).dialog("close");
			},
			"Continue": function() {
				$(this).dialog("close");
			}
		}
	});

	$("#Build input[type=button]").removeAttr('disabled');

	$("#buildBundle_testResultList").prop('readonly', true);
	
	$("#buildBundle_finalResultList").prop('readonly', true);
	
	$("#testProgressBar").progressbar({
		value: 0
	});
	
	$("#finalProgressBar").progressbar({
		value: 0
	});

	clearPreviousBuildResults();
	$("#Sync #syncProgressDiv").hide();
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

function onCreateContinueClick() {
	var $tabs = jQuery("#tabs");
	$tabs.tabs('select', 1);
}

function onUploadContinueClick() {
	var $tabs = jQuery("#tabs");
	$tabs.tabs('select', 2);
}

function onAgencyIdSelectClick() {
	var idx = $(this).find(":selected").index();
	$(this).closest('tr').find(".agencyId").val(agencyMetadata[idx].legacyId);
	var url = agencyMetadata[idx].gtfsFeedUrl;
	if (url == null) {
		url = "";
	}
	var previous_protocol = $(this).closest('tr').find(".agencyProtocol").val();
	var protocol = "file";
	if (url.toLowerCase().substring(0,4) === 'http') {
	  protocol = "http";
	} else if (url.toLowerCase().substring(0,3) === 'ftp') {
		protocol = "ftp";
	}
	if ((previous_protocol == "file" && protocol != "file")
			|| (previous_protocol != "file" && protocol == "file")) {
		var dataSource = $(this).closest('tr').find(".agencyDataSource");
		if (protocol == "file") {
			dataSource.replaceWith('<input class="agencyDataSource" type="file" undefined=""></input>');
		} else if (dataSource.attr('type') == 'file') {
			dataSource.replaceWith('<input class="agencyDataSource" type="text" undefined=""></input>');
		}  
	}
	$(this).closest('tr').find(".agencyProtocol").val(protocol);
	if (protocol != "file") {		// Not possible to provide a value for "file" 
									// fields for security reasons.
		$(this).closest('tr').find(".agencyDataSource").val(url);
	}
}

function onPrevalidateContinueClick() {
	var $tabs = jQuery("#tabs");
	$tabs.tabs('select', 3);
}

function onBuildContinueClick() {
	var $tabs = jQuery("#tabs");
	$tabs.tabs('select', 4);
}
/*
function onCurrentDatasetNameSelectClick() {
	var idx = $(this).find(":selected").index();
	if (idx == 0) {
		//clean data
	} else {
		currentReportDataset = $(this).find(":selected").text();
		currentReportBuildName = getLatestBuildName(currentReportDataset);
	}
}
*/

function onStageContinueClick() {
	var $tabs = jQuery("#tabs");
	$tabs.tabs('select', 5);
}

function onDeployContinueClick() {
	var $tabs = jQuery("#tabs");
	$tabs.tabs('select', 6);
}
//Helper method for setting up different DIVs HTML
function setDivHtml(field, info){
	var messages = '<ul>';
	$.each(info, function(i, str) {		
		messages = messages + '<li>' + str + '</li>';			    
	});
	messages = messages + '</ul>';
	jQuery(field).html(messages).css("font-size", "12px");	
}
//Helper method for showing build file list on bundle selection
function showBuildFileList(info, id) {
	var txt = '<ul>';
	$.each(info, function(i, str) {		
		if(str.search(".csv") != -1){
			var encoded = encodeURIComponent(str);
			var description = str.slice(0, str.lastIndexOf(".csv"));
			txt = txt + "<li>" + description + ":"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<img src=\"../../css/img/go-down-5.png\" />"
			+ "<a href=\"manage-bundles!downloadOutputFile.action?id="
			+ id+ "&downloadFilename=" 
			+ encoded + "\">" + ".csv" +  "</a></li>";	
		}		    
	});

	// append log file
	txt = txt + "<li>" + "Bundle Builder Log:" + "&nbsp;"
	+ " " + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
	+ "<img src=\"../../css/img/go-down-5.png\" />"
	+ "<a href=\"manage-bundles!downloadOutputFile.action?id="
	+ id+ "&downloadFilename=" 
	+ encodeURIComponent("bundleBuilder.out.txt") + "\">" + ".txt" +  "</a></li>";	
	txt = txt + '</ul>';

	jQuery("#buildBundle_fileList").html(txt).css("display", "block");
	jQuery("#buildBundle #downloadLogs").show().css("display", "block");
	jQuery("#buildBundle #downloadLogs #downloadButton").attr("href", "manage-bundles!buildOutputZip.action?id=" + id);
}
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
//Populates bundle information in related fields.
function showBundleInfo(bundleInfo){
	var bundleObj = JSON.parse(bundleInfo);
	
	if (bundleObj.agencyList != undefined) {
		//Populating Upload Tab Fields
		$.each(bundleObj.agencyList, function(i, agency) {
			jQuery("#agencyId").val(agency.agencyId);
		    jQuery("#agencyDataSource").val(agency.agencyDataSource);
		    jQuery("#agencyDataSourceType").val(agency.agencyDataSourceType);
		    jQuery("#agencyProtocol").val(agency.agencyProtocol);
		});
	}

	if (bundleObj.validationResponse != undefined) {
		//Populating Pre-Validate Tab Fields
		jQuery("#prevalidate_bundleName").val(bundleObj.validationResponse.bundleBuildName);
		if ((jQuery("#prevalidate_bundleName").val()).length > 0) {
			enableValidateButton();
		}
		jQuery("#prevalidate_id").text(bundleObj.validationResponse.requestId);
		if (jQuery("#prevalidate_id").text().length > 0) {
			jQuery("#prevalidate_id_label").show();
		}
		setDivHtml(document.getElementById('prevalidate_resultList'), bundleObj.validationResponse.statusMessages);
	}
	
	if (bundleObj.buildResponse == undefined) {
		// Set Comments field to ""
		jQuery("#uploadFiles #bundleComment").val("");
		return;
	}
	
	//Populating Build Tab Fields
	if (bundleObj.buildResponse.email != undefined && bundleObj.buildResponse.email != null 
			&& bundleObj.buildResponse.email != "null") {
		jQuery("#buildBundle_email").val(bundleObj.buildResponse.email);
	}
	jQuery("#startDatePicker").val(bundleObj.buildResponse.startDate);
	jQuery("#startDate").val(bundleObj.buildResponse.startDate);
	jQuery("#endDatePicker").val(bundleObj.buildResponse.endDate);
	jQuery("#endDate").val(bundleObj.buildResponse.endDate);
	jQuery("#uploadFiles #bundleComment").val(bundleObj.buildResponse.comment);
	jQuery("#selected_bundleDirectory").text(bundleObj.directoryName);
	jQuery("#buildBundle_id").text(bundleObj.buildResponse.requestId);
	if (!fromResultLink) {
		buildBundleId = bundleObj.buildResponse.requestId;
	}
	if (bundleObj.buildResponse.statusMessages != null) {
		setDivHtml(document.getElementById('testBuildBundle_resultList'), bundleObj.buildResponse.statusMessages);
		setDivHtml(document.getElementById('finalBuildBundle_resultList'), bundleObj.buildResponse.statusMessages);
	}
	if (buildBundleId != null) {
		showBuildFileList(bundleObj.buildResponse.buildOutputFiles, bundleObj.buildResponse.requestId);
	}
}

function onCreateDatasetClick() {
	selectedDirectory = jQuery("#createDataset #directoryName").val();
	$("#Download #download_selectedDataset").text(selectedDirectory);
	// Clear fields on the Upload tab
	if (agencyMetadataAvailable) {
		$('#agency_data tr').slice(1).remove();
		onAddAnotherAgencyClick("file");
	}
	$("#uploadFiles #bundleComment").val("");
	$('#existingFilesTable tr').slice(1).remove();
	onSelectDataset("create");
}

function onExistingDatasetClick() {
	var selectedCheckbox = jQuery("#Create #existingDataset").find("input:checked");
	selectedDirectory = selectedCheckbox.closest("tr").find(".directoryName").text();
	$("#Download #download_selectedDataset").text(selectedDirectory);
	onSelectDataset("existing");
	updateFixedRouteParams(selectedDirectory);
}

function onCopyExistingDatasetClick() {
	selectedDirectory = $(this).closest("tr").find(".directoryName").text();
	$("#Download #download_selectedDataset").text(selectedDirectory);
	$("#destinationDirectory").val("");
	jQuery('#copyFilenameError').hide();
	$("#copyContinue").button("disable");
	var continueCopy = $("#copyPopup").dialog("open");
}

function onCopyDestinationSpecified() {
	onSelectDataset("copy");
}

function onDeleteDatasetConfirmed() {
	onDeleteDataset();
}

function onDeleteExistingDatasetClick() {
	selectedDirectory = $(this).closest("tr").find(".directoryName").text();
	var continueDelete = $("#deletePopup").dialog("open");
}

function onAnyCommentsClick() {
	$("#addCommentsPopup").dialog("open");
}

function onSelectDataset(sourceDirectoryType) {
	var bundleDir = selectedDirectory;
	var actionName = "selectDirectory";
	if (sourceDirectoryType=="copy") {
		actionName = "copyDirectory"
	} else if (sourceDirectoryType=="delete") {
		actionName = "deleteDirectory"
	}
	var copyDir = destinationDirectory;

	// initially hide the Request Id label when picking a new bundle
	jQuery("#prevalidate_id_label").hide();
	
	if (sourceDirectoryType == "create") {
		// Check for valid bundle directory name
		var bundleNameCheck = /^[a-zA-Z0-9\.\_\-]+$/;
		if (!bundleNameCheck.test(bundleDir)) {
			jQuery("#createDirectory #createDirectoryContents #createDirectoryResult").show().css("display","block");
			jQuery("#createDirectoryResult #resultImage").attr("src", "../../css/img/warning_16.png");
			jQuery("#createDirectoryMessage").text("Invalid bundle directory name: can contain only "
				+ "letters, numbers, periods, hyphens and underscores.")
				.css("font-weight", "bold").css("color", "red");
			return false;
		}
		actionName = "createDirectory";
	}
	// Reset fields on Compare tab
	resetCurrentReportDataset();
	resetCompareToDataset();
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();
	var data = {};
	data[csrfParameter] = csrfToken;
	data["directoryName"] = bundleDir;
	data["destDirectoryName"] = copyDir;
	data["ts"] = new Date().getTime();

	jQuery.ajax({
		url: "manage-bundles!" + actionName + ".action",
		type: "POST",
		data: data,
			async: false,
			success: function(response) {
				clearPreviousBuildResults();
				disableSelectButton();
				var status = response;
				if (status != undefined) {
					if(status.selected == true) {
						jQuery("#createDirectoryResult #resultImage").attr("src", "../../css/img/dialog-accept-2.png");
						jQuery("#createDirectoryMessage").text(status.message).css("color", "green");
						// Clear the bundle name for the Pre-validate, Build and Stage tabs
						$("#Validate #prevalidate_bundleName").val("");
						$("#Build #bundleBuildName").val("");
						$("#Stage #staging_bundleName").text("");
						$("#Download #download_bundleName").text("");
						// If "createDirectory", add the new directory to the current list of bundle directories.
						if (actionName == "createDirectory") {
							disableStageButton();
							disableDownloadButton();
							// Add a new div for this directory to the list of existing directories
							var idx = 0;
							$("#createDirectory #currentDirectories").find("#listItem").each(function() {
								idx++
								if (status.directoryName < $(this).find(".listData").first().text()) {
									idx--;
									return false;
								}
							});
							var insertAfterThis = $("#createDirectory #currentDirectories");
							if (idx > 0) {
								insertAfterThis = insertAfterThis.find("#listItem").eq(idx-1);
							}
							var newDirRow = '<div class="ui-selectee" style="" id="listItem">'
								+ '<div style="" class="listData ui-selectee">'
								+ '<strong class="ui-selectee" style="">'
								+ status.directoryName
								+ '</strong></div>'
								+ '<div style="" class="listData ui-selectee"> </div>'
								+ '<div style="" class="listData ui-selectee">'
								+ status.timestamp;
							+ '</div></div>"';
							if (idx == 0) {
								insertAfterThis.prepend(newDirRow);
							} else {
								insertAfterThis.after(newDirRow);
							}
						}			
						if (sourceDirectoryType=="copy") {
							selectedDirectory = copyDir;
							$("#Download #download_selectedDataset").text(selectedDirectory);
						}
						
						if (actionName != "createDirectory" && status.bundleInfo != null) {
							// Display the name of the most recently built bundle on the Build and Stage tabs
							if (status.bundleInfo.buildResponse != null && !fromResultLink) {
								$("#Validate #prevalidate_bundleName").val(status.bundleInfo.buildResponse.bundleBuildName);
								$("#Build #bundleBuildName").val(status.bundleInfo.buildResponse.bundleBuildName);
								$("#Stage #staging_bundleName").text(status.bundleInfo.buildResponse.bundleBuildName);
								$("#Download #download_bundleName").text(status.bundleInfo.buildResponse.bundleBuildName);
								// Get results of previous build
								if (status.bundleInfo.buildResponse.statusMessages != null
										&& status.bundleInfo.buildResponse.statusMessages.length > 0) {
									getPreviousBuildResults(status.bundleInfo.buildResponse);
								}
							}
							enableStageButton();
							enableDownloadButton();
							var agencies = status.bundleInfo.agencyList;
							if (agencies != null) {
								if (agencies.length > 0) {
									$("#agency_data .agencySelected").remove();
									// Clear the existingFilesTable
									$('#existingFilesTable .previouslyUploaded').remove();
								}
								for (var i = 0; i < agencies.length; ++i) {
									var agency = agencies[i];
									// Check if this is a duplicate
									var dupe = false;
									for (var j = 0; j < i; ++j) {
										var agency2 = agencies[j];
										if (agency2.agencyId == agency.agencyId && agency.agencyBundleUploadDate != null) {
											var updateDate = Date.parse(agency.agencyBundleUploadDate);
											var stringDate = new Date(updateDate).toString();
										}
										if (agency2.agencyId == agency.agencyId 
											&& agency2.agencyDataSource == agency.agencyDataSource
											&& agency2.agencyDataSourceType == agency.agencyDataSourceType
											&& agency2.agencyProtocol == agency.agencyProtocol) {
											dupe = true;
											break;
										}
									}
									if (dupe) continue;
									// Get agency name
									var agencyName = "";
									for (var j=0; j<agencyMetadata.length; ++j) {
										if (agencyMetadata[j].legacyId == agency.agencyId) {
											agencyName = agencyMetadata[j].name;
											agencyName += " (" + agencyMetadata[j].shortName + ")";
											break;
										}
									}

									// Add row to the list of previously uploaded files
									onAddAnotherAgencyClick(agency.agencyProtocol);
									$("#agency_data tr:last .agencyIdSelect").val(agency.agencyId);
									$("#agency_data tr:last .agencyDataSourceType").val(agency.agencyDataSourceType);
									$("#agency_data tr:last .agencyProtocol").val(agency.agencyProtocol);
									if(agency.agencyProtocol != "file") {
										$("#agency_data tr:last .agencyDataSource").val(agency.agencyDataSource);
									} 

									// Add row for this file to existingFilesTable
									var uploadDate = "";
									if (agency.agencyBundleUploadDate != null) {
										uploadDate = agency.agencyBundleUploadDate;
									}
									var new_row = '<tr class="previouslyUploaded"> \
										<td>' + agencyName + '</td> \
										<td>' + agency.agencyDataSourceType + '</td> \
										<td>' + agency.agencyDataSource + '</td> \
										<td>' + uploadDate + '</td> \
										<td></td>  \
										</tr>';
									$('#existingFilesTable').append(new_row);
								}
							}
						}
						enableBuildButtons();
					}					
					else {
						jQuery("#createDirectoryResult #resultImage").attr("src", "../../css/img/warning_16.png");
						jQuery("#createDirectoryMessage").text(status.message).css("color", "red");
						disableBuildButtons();
					}
					var continueButton = jQuery("#create_continue");
					enableContinueButton(continueButton);
					var bundleDir = status.directoryName;
					var bundleInfo = status.bundleInfo;
					console.log("bundleInfo=" + bundleInfo);
					if(bundleInfo != null || bundleInfo != undefined){
						showBundleInfo(JSON.stringify(bundleInfo));
					}					
					console.log("bundleDir=" + bundleDir);
					jQuery("#selectedDataset").text("You are adding files to " + bundleDir);
					jQuery("#prevalidate_bundleDirectory").text(bundleDir);
					jQuery("#selected_bundleDirectory").text(bundleDir);
					jQuery("#s3_location").text(status.bucketName);
					jQuery("#gtfs_location").text(bundleDir + "/" + status.gtfsPath + " directory");
					jQuery("#stif_location").text(bundleDir + "/" + status.stifPath + " directory");
					enableContinueButton(jQuery("#upload_continue"));
					
					// Add dataset name to Build tab
					jQuery("#Build #datasetName").text(bundleDir);
				} else {
					alert("null status");
					disableBuildButtons();
				}
				if (!fromResultLink) {
					var $tabs = jQuery("#tabs");
					$tabs.tabs('select', 1);
				}
			},
			error: function(request) {
				alert("There was an error processing your request. Please try again.");
			}
	});
}

function onDeleteDataset() {
	var bundleDir = selectedDirectory;
	var actionName = "deleteDirectory";
	var data = {};
	data[csrfParameter] = csrfToken;
	data["ts"] = new Date().getTime();
	data["directoryName"] = bundleDir;

	jQuery.ajax({
		url: "manage-bundles!" + actionName + ".action",
		type: "POST",
		data: data,
		async: false,
		success: function(response) {
			disableSelectButton();
			$("#deleteSuccessPopup").dialog("open");
			// Remove dataset from list of datasets
			var datasetTd = $('td').filter(function(){
			    return $(this).text() === bundleDir;
			})
			datasetTd.parent().remove();
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
}

function onUploadSelectedAgenciesClick() {
	var bundleDir = selectedDirectory;
	var cleanedDirs = [];
	$('#agency_data .agencySelected').each(function() {
		$this = $(this)
		var agencyId = $(this).find('.agencyIdSelect').val();
		var agencyDataSourceType = $(this).find('.agencyDataSourceType').val();
		var agencyProtocol = $(this).find('.agencyProtocol').val();
		var agencyDataSource = $(this).find('.agencyDataSource').val();
		if (agencyProtocol == "file") {
			var agencyDataFile = $(this).find(':file')[0].files[0];
		}
		// Check if the target directory for this agency has already been cleaned
		var cleanDir = "true";
		if (cleanedDirs.indexOf(agencyId) == -1) {
			cleanedDirs.push(agencyId);
		} else {
			cleanDir = "false";
		}
		if (agencyProtocol != "file") {
			var actionName = "uploadSourceData";	
			jQuery.ajax({
				url: "upload-gtfs!" + actionName + ".action?ts=" + new Date().getTime(),
				type: "GET",
				data: {
					"directoryName" : bundleDir,
					"agencyId" : agencyId,
					"agencyDataSourceType" : agencyDataSourceType,
					"agencyProtocol" : agencyProtocol,
					"agencyDataSource" : agencyDataSource,
					"cleanDir" : cleanDir
				},
				async: false,
				success: function(response) {
					console.log("Successfully uploaded " + agencyDataSource);
					$this.find("div").addClass('agencyCheckboxUploadSuccess');
					$this.find(".agencyDataSource").addClass('agencyUploadSuccess');
				},
				error: function(request) {
					console.log("Error uploadeding " + agencyDataSource);
					alert("There was an error processing your request. Please try again.");
				}
			});
		} else {
			console.log("about to call manage-bundles!uploadSourceFile");
			var files = agencyDataFile;
			console.log("file name is: " + agencyDataFile.name);
			var formData = new FormData();
			formData.append("ts", new Date().getTime());
			formData.append("directoryName", bundleDir);
			formData.append("agencyId", agencyId);
			formData.append("agencyDataSourceType", agencyDataSourceType);
			formData.append("agencyProtocol", agencyProtocol);
			formData.append("agencySourceFile", agencyDataFile);
			formData.append("cleanDir", cleanDir);
			formData.append(csrfParameter, csrfToken);
			var actionName = "uploadSourceFile";
			jQuery.ajax({
				url: "upload-gtfs!" + actionName + ".action",
				type: "POST",
				data: formData,
				cache: false,
				processData: false,
				contentType: false, 
				async: false,
				success: function(response) {
					console.log("Successfully uploaded " + agencyDataFile.name);
					$this.find("div").addClass('agencyCheckboxUploadSuccess');
					$this.find(".agencyDataSource").addClass('agencyUploadSuccess');
				},
				error: function(request) {
					console.log("Error uploadeding " + agencyDataFile.name);
					alert("There was an error processing your request. Please try again.");
				}
			});
		}
	});
	onUploadContinueClick();
}

function onBundleCommentChanged() {
	var data = {};
	data[csrfParameter] = csrfToken;
	data["ts"] = new Date().getTime();
	data["directoryName"] = selectedDirectory;
	data["comments"] = $("#uploadFiles #bundleComment").val();

	jQuery.ajax({
		url: "manage-bundles!updateBundleComments.action",
		type: "POST",
		data: data
	});
}

function onAddNewAgencyClick() {
	var data = {};
	data[csrfParameter] = csrfToken;
	data["gtfs_id"] = "";
	data["name"] = $("#newAgencyName").val();
	data["short_name"] = $("#newAgencyShortName").val();
	data["legacy_id"] = $("#newAgencyLegacyId").val();
	data["gtfs_feed_url"] = "";
	data["gtfs_rt_feed_url"] = "";
	data["bounding_box"] = "";
	data["ntd_id"] = "";
	data["agency_message"] = "";

	// Make ajax call
	jQuery.ajax({
		url: "../../api/agency/create",
		type: "POST",
		async: false,
		data: data,
		success: function(response) {
			getAgencyMetadata();
			//clear fields for new agency
			$("#newAgencyName").val("");
			$("#newAgencyShortName").val("");
			$("#newAgencyLegacyId").val("");
		},
		error: function(request) {
			console.log("Error adding new agency");
			alert("There was an error processing your request. Please try again.");
		}
	});
}

/*
 * When a dataset is selected from the "Choose" tab, this function is called
 * for displaying the results from the most recent build for the selected
 * dataset.
 */
function getPreviousBuildResults(buildResponse) {
	// Display the progress messages produced by the build 
	$("#Build #buildBundle_testResultList").val(buildResponse.statusMessages.join("\n"));
	// Create the formatted list of bundle build output files.
	updateBuildList(buildResponse.requestId,"test");

	$("#buildBundle_testResultLink").hide();// Hide the div for the result link
	$("#testProgressBarDiv #testBuildProgress").text("Previous Build Messages");
	$("#testProgressBarDiv").show();
	$("#bundleTestResultsHolder").show();
}

function onAddAnotherAgencyClick(inputType) {
	// If no agency metadata defined, just return.
	if (!agencyMetadataAvailable) {
		return;
	}
	if (inputType === undefined) {
		inputType = "file";
	}
	var file_selected = ""; // For Protocol select group
	if (inputType === "file") {
		file_selected = "selected";
	}
	var metadata = "";
	var url = "";
	if (agencyMetadataAvailable) {
	  metadata = '<select class="agencyIdSelect">';
	  for (var i=0; i<agencyMetadata.length; ++i) {
		  metadata += '<option value="' + agencyMetadata[i].legacyId + '">'
		  + agencyMetadata[i].name + ' ('
		  + agencyMetadata[i].shortName + ')</option>';
	  }
	  metadata += '</select>';
	  url = agencyMetadata[0].gtfsFeedUrl;
	  if (url == null) {
		  url = "";
	  }
	  if (url.toLowerCase().substring(0,4) === 'http'
		  || (url.toLowerCase().substring(0,3) === 'ftp')) {
	  	var urlValue = ' value="' + url + '"';
	  }
	}
	var new_row = '<tr class="agencySelected"> \
		<td>' + metadata + '<input type="text" class="agencyId"/></td> \
		<td><select class="agencyDataSourceType"> \
		<option value="gtfs">gtfs</option> \
		<option value="aux">aux</option> \
		</select></td> \
		<td><select class="agencyProtocol"> \
		<option value="http">http</option> \
		<option value="ftp">ftp</option> \
		<option value="file" ' + file_selected + '>file</option> \
		</select></td> \
		<td class="agencyDataSourceData"> \
		<input type=' + inputType + ' class="agencyDataSource" ' + urlValue + '/></td> \
		<td class="removeAgency">remove</td> \
		</tr>';
	$('#agency_data').append(new_row);
	if (agencyMetadataAvailable) {
		$("#agency_data tr:last .agencyId").hide();
	}
	jQuery(".agencyIdSelect").change(onAgencyIdSelectClick);
}

function onSelectAgencyChange() {
	console.log("in onSelectAgencyChange, v1");
	console.log("parent: " + $(this).parent().get(0).tagName);
	$this = $(this);
	if ($this.parent().is("th")) {	// For the checkbox in the header, turn all the rest on/off
		if ($this.is(":checked")) {
			$this.closest("table").find("tr td :checkbox").each(function() {
				$(this).prop('checked', true);
				$(this).closest("tr").addClass("agencySelected");
			});
		} else {
			$this.closest("table").find("tr td :checkbox").each(function() {
				$(this).prop('checked', false);
				$(this).closest("tr").removeClass("agencySelected");
				$(this).closest('tr').find("div").removeClass('agencyCheckboxUploadSuccess');
				$(this).closest('tr').find(".agencyDataSource").removeClass('agencyUploadSuccess');
			});
		}
	} else {		// For toggling a single checkbox
		$this.closest('tr').toggleClass('agencySelected');
		$this.closest('tr').find("div").removeClass('agencyCheckboxUploadSuccess');
		$this.closest('tr').find(".agencyDataSource").removeClass('agencyUploadSuccess');
	}
}

function onAgencyProtocolChange() {
	console.log("in onAgencyProtocolChange, v1");
	var protocol = $(this).val();
	var dataSource = $(this).closest('tr').find(".agencyDataSource");
	if (protocol == "file") {
		dataSource.replaceWith('<input class="agencyDataSource" type="file" undefined=""></input>');
	} else if (dataSource.attr('type') == 'file') {
		dataSource.replaceWith('<input class="agencyDataSource" type="text" undefined=""></input>');
	}
}

function onBundleNameChanged() {
	// Changes for Pre-validate tab
	var text = jQuery("#Validate #prevalidate_bundleName").val();
	if (text.length == 0 || selectedDirectory.length == 0) {
		disableValidateButton();
	} else {
		enableValidateButton();
		jQuery("#prevalidateInputs #validateBox #validating").hide();
		jQuery("#prevalidate_progress").hide();
		jQuery("#prevalidate_exception").hide();
		jQuery("#prevalidate_resultList").empty();
		jQuery("#prevalidate_fileList").empty();
	}

	// Changes for Build tab
	clearPreviousBuildResults();
}

function onRemoveSelectedAgenciesClick() {
	$(this).closest('tr').remove();
}

function onPrintFixedRouteRptClick() {
	window.print();
}

function enableContinueButton(continueButton) {
	jQuery(continueButton).removeAttr("disabled").css("color", "#000");
}

function disableContinueButton(continueButton) {
	jQuery(continueButton).attr("disabled", "disabled").css("color", "#999");
}

function enableSelectButton() {
	jQuery("#Create #createDataset #newDirectoryButton").removeAttr("disabled").css("color", "#000");
}

function disableSelectButton() {
	jQuery("#Create #createDataset #newDirectoryButton").attr("disabled", "disabled").css("color", "#999");
}

function enableAddFilesButton() {
	jQuery("#Create #createDataset #existingDirectoryButton").removeAttr("disabled").css("color", "#000");
}

function disableAddFilesButton() {
	jQuery("#Create #createDataset #existingDirectoryButton").attr("disabled", "disabled").css("color", "#999");
}

function enableValidateButton() {
	jQuery("#prevalidateInputs #validateBox #validateButton").removeAttr("disabled").css("color", "#000");
}

function disableValidateButton() {
	jQuery("#prevalidateInputs #validateBox #validateButton").attr("disabled", "disabled").css("color", "#999");
}

function enableStageButton() {
	jQuery("#stageBundle_stageButton").removeAttr("disabled").css("color", "#000");
	enableContinueButton($("#stage_continue"));
}

function disableStageButton() {
	jQuery("#stageBundle_stageButton").attr("disabled", "disabled").css("color", "#999");
	disableContinueButton($("#stage_continue"));
}

function enableDeployButton() {
	jQuery("#deployBundle_deployButton").removeAttr("disabled").css("color", "#000");
	enableContinueButton($("#deploy_continue"));
}

function disableDeployButton() {
	jQuery("#deployBundle_deployButton").attr("disabled", "disabled").css("color", "#999");
	disableContinueButton($("#deploy_continue"));
}

function enableDownloadButton() {
	jQuery("#downloadBundle_downloadButton").removeAttr("disabled").css("color", "#000");
}

function disableDownloadButton() {
	jQuery("#downloadBundle_downloadButton").attr("disabled", "disabled").css("color", "#999");
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

function toggleStageBundleResultList() {
	var $image = jQuery("#stageBundle #stageBundle_progress #expand");
	changeImageSrc($image);
	//Toggle progress result list
	jQuery("#stageBundle #stageBundle_resultList").toggle();
}


function changeImageSrc($image) {

	var $imageSource = $image.attr("src");
	if($imageSource.indexOf("right-3") != -1) {
		//Change the img to down arrow
		$image.attr("src", "../../css/img/arrow-down-3.png");
	} else {
		//Change the img to right arrow
		$image.attr("src", "../../css/img/arrow-right-3.png");
	}
}

function directoryOptionChanged() {
	//Clear the results regardless of the selection
	jQuery("#createDirectory #createDirectoryContents #createDirectoryResult").hide();
	jQuery("#createDirectory #directoryName").val("");
	jQuery("#createDirectory #createDirectoryContents #directoryButton").attr("disabled", "disabled").css("color", "#999");
	jQuery('#currentDirectories .ui-selected').removeClass('ui-selected');
	
	if(jQuery("#create").is(":checked")) {
		//Change the button text and hide select directory list
		jQuery("#createDirectoryContents #directoryButton").val("Create");
		jQuery("#createDirectoryContents #directoryButton").attr("name","method:createDirectory");
		jQuery("#selectExistingContents").hide();
		jQuery('#copyDirectory').hide();
		// Blank out the Comment box
		jQuery("#uploadFiles #bundleComment").val("");
	} 
	else if(jQuery("#copy").is(":checked")) {
		jQuery("#createDirectoryContents #directoryButton").val("Copy");
		jQuery("#createDirectoryContents #directoryButton").attr("name","method:selectDirectory");
		jQuery("#selectExistingContents").show();
		var element = '<br><div id="copyDirectory">' + 'Destination: ' + 
		'<input type="text" id="destDirectoryName" class="destDirectoryName" required="required"/>' + '<label class="required">*</label></div>';
			if(jQuery('#destDirectoryName').length){						
				//Do nothing
				console.log('Element exists');
				jQuery('#copyDirectory').show();
			}else{
				jQuery(element).insertAfter("#directoryButton");
				//Enable or disable create/select button when user enters/removes the destination 
				// directory name for a Copy
				jQuery("#createDirectoryContents #destDirectoryName").bind("input propertychange", function() {
					var text = selectedDirectory;
					var copyDestText = jQuery("#createDirectory #destDirectoryName").val();
					if (text.length > 0 && copyDestText.length > 0) {
						enableSelectButton();
					} else {
						disableSelectButton();
						jQuery("#createDirectory #createDirectoryContents #createDirectoryResult").hide();
					}
				});
			}	
	}
	else 
	{
		//Change the button text and show select directory list
		jQuery("#createDirectoryContents #directoryButton").val("Select");
		jQuery("#createDirectoryContents #directoryButton").attr("name","method:selectDirectory");
		jQuery("#selectExistingContents").show();
		jQuery('#copyDirectory').hide();
		// TODO replace the existingDirectories form call with this below
//		jQuery.ajax({
//		url: "manage-bundles!requestExistingDirectories.action",
//		type: "GET",
//		async: false,
//		success: function(response) {
//		jQuery("#selectExistingContents").show();

//		},
//		error: function(request) {
//		alert(request.statustext);
//		}
//		});
	}

}

function onValidateClick() {
	var bundleDirectory = jQuery("#prevalidate_bundleDirectory").text();
	if (bundleDirectory == undefined || bundleDirectory == null || bundleDirectory == "") {
		alert("missing bundle directory");
		return;
	}
	var bundleName = jQuery("#prevalidate_bundleName").val();
	if (bundleName == undefined || bundleName == null || bundleName == "") {
		alert("missing bundle build name");
		return;
	} 
	else if(~bundleName.indexOf(" ")){
		alert("bundle build name cannot contain spaces");
		return;
	}
	else {
		jQuery("#buildBundle_bundleName").val(bundleName);
	}
	disableValidateButton();
	jQuery("#prevalidate_progress").show();
	jQuery("#prevalidate_exception").hide();
	jQuery("#prevalidate_resultList").empty();
	jQuery("#prevalidate_fileList").empty();
	jQuery("#prevalidate_validationProgress").text("Validating ... ");
	jQuery("#prevalidateInputs #validateBox #validating #validationProgress").attr("src","../../css/img/ajax-loader.gif");
	jQuery("#prevalidateInputs #validateBox #validating").show().css("display","inline");
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/validate/" + bundleDirectory + "/" + bundleName + "/create?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var bundleResponse = response;
			if (bundleResponse != undefined) {
				jQuery("#prevalidate_id").text(bundleResponse.id);
				if (jQuery("#prevalidate_id").text().length > 0) {
					jQuery("#prevalidate_id_label").show();
				}
				jQuery("#Build #bundleBuildName").val(bundleName);
				window.setTimeout(updateValidateStatus, 5000);
			} else {
				jQuery("#prevalidate_id").text(error);
				jQuery("#prevalidate_resultList").text("error");
				if (jQuery("#prevalidate_id").text().length > 0) {
					jQuery("#prevalidate_id_label").show();
				}
				enableValidateButton();
			}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
			enableValidateButton();
		}
	});
}

function updateValidateStatus() {
	var id = jQuery("#prevalidate_id").text();
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/validate/" + id + "/list?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var txt = "<ul>";
			var bundleResponse = response;
			if (bundleResponse == null) {
				jQuery("#prevalidate_validationProgress").text("Complete.");
				jQuery("#prevalidateInputs #validateBox #validating #validationProgress").attr("src","../../css/img/dialog-accept-2.png");
				jQuery("#prevalidate_resultList").html("unknown id=" + id);
			}
			var size = bundleResponse.statusMessages.length;
			if (size > 0) {
				for (var i=0; i<size; i++) {
					txt = txt + "<li>" + bundleResponse.statusMessages[i] + "</li>";
				}
			}
			if (bundleResponse.complete == false) {
				window.setTimeout(updateValidateStatus, 5000); // recurse
			} else {
				jQuery("#prevalidate_validationProgress").text("Complete.");
				jQuery("#prevalidateInputs #validateBox #validating #validationProgress").attr("src","../../css/img/dialog-accept-2.png");
				updateValidateList(id);
				enableValidateButton();
			}
			txt = txt + "</ul>";
			jQuery("#prevalidate_resultList").html(txt).css("font-size", "12px");
			if (bundleResponse.exception != null) {
				if (bundleResponse.exception.message != undefined) {
					jQuery("#prevalidate_exception").show().css("display","inline");
					jQuery("#prevalidate_exception").html(bundleResponse.exception.message);
				}
			}

		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(updateValidateStatus, 10000);
		}
	});
}

//populate list of files that were result of validation
function updateValidateList(id) {
	var data = {};
	data[csrfParameter] = csrfToken;
	data["id"] = id;
	data["ts"] = new Date().getTime();

	jQuery.ajax({
		url: "manage-bundles!fileList.action",
		type: "POST",
		data: data,
		async: false,
		success: function(response) {
			var txt = "<ul>";

			var list = response;
			if (list != null) {
				var size = list.length;
				if (size > 0) {
					for (var i=0; i<size; i++) {
						var encoded = encodeURIComponent(list[i]);
						txt = txt + "<li><a href=\"manage-bundles!downloadValidateFile.action?id="
						+ id+ "&downloadFilename=" 
						+ encoded + "\">" + encoded +  "</a></li>";
					}
				}
			}
			txt = txt + "</ul>";
			jQuery("#prevalidate_fileList").html(txt);
			jQuery("#prevalidateInputs #validateBox #validateButton").removeAttr("disabled");
			var continueButton = jQuery("#prevalidate_continue");
			enableContinueButton(continueButton);
		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(function() {
				updateValidateList(id);
			}, 10000);
		}
	});	
}

function onBuildClick(event) {
	var bundleDir = selectedDirectory;
	var bundleName = jQuery("#Build #bundleBuildName").val();
	var startDate = jQuery("#startDate").val();
	var endDate = jQuery("#endDate").val();
	var bundleComment = jQuery("#uploadFiles #bundleComment").val();
	var archive = false;
	var consolidate = false;
	var buildType = event.data.buildType;
	
	if (buildType == "test") {
		jQuery("#testProgressBarDiv").show();
		jQuery("#buildBundle #buildingTest").show();
		jQuery("#buildBundle #buildingTest #buildingTestProgress").attr("src","../../css/img/ajax-loader.gif");
		jQuery("#buildBundle_buildTestProgress").text("Bundle Build in Progress...");
		jQuery("#buildBundle_testFileList").html("");
		jQuery("#buildBundle #buildingTest").show();
		// Show result link
		$("#buildBundle_testResultLink").show();
	} else {
		archive = true;
		consolidate = true;
		jQuery("#finalProgressBarDiv").show();
		jQuery("#buildBundle #buildingFinal").show();
		jQuery("#buildBundle #buildingFinal #buildingFinalProgress").attr("src","../../css/img/ajax-loader.gif");
		jQuery("#buildBundle_buildFinalProgress").text("Bundle Build in Progress...");
		jQuery("#buildBundle_finalFileList").html("");
		jQuery("#buildBundle #buildingFinal").show();
		// Show result link
		$("#buildBundle_finalResultLink").show();
	}

	var valid = validateBundleBuildFields(bundleDir, bundleName, startDate, endDate);
	if(valid == false) {
		return;
	}

	clearPreviousBuildResults();
	disableBuildButtons();

	buildBundle(bundleName, startDate, endDate, bundleComment, archive, consolidate, false, buildType);
}

function clearPreviousBuildResults() {
	jQuery("#buildBundle_testResultList").val("");
	jQuery("#buildBundle_finalResultList").val("");
	jQuery("#buildBundle_testException").html("");
	jQuery("#buildBundle_finalException").html("");
	jQuery("#buildBundle_testFileList").html("");
	jQuery("#buildBundle_finalFileList").html("");

	// Hide result divs
	jQuery("#buildBundle_testResultLink").hide();
	jQuery("#bundleTestResultsHolder").hide();
	jQuery("#buildBundle_finalResultLink").hide();
	jQuery("#bundleFinalResultsHolder").hide();

	jQuery("#buildBundle #buildingTest").hide();
	jQuery("#buildBundle #buildingFinal").hide();

	//Reset the Progress bar
	$("#testProgressBar").progressbar('value', 0)
	$("#finalProgressBar").progressbar('value', 0)

	//Make sure any "Bundle Build Failed" message is cleared
	$("#testProgressBarDiv #testBuildProgress").text("Previous Build Messages");
}

function disableBuildButtons() {
	jQuery("#buildBundle #testBuildBundleButton").prop("disabled", true).css("color", "#999");
	jQuery("#buildBundle #finalBuildBundleButton").prop("disabled", true).css("color", "#999");
}

function enableBuildButtons() {
	jQuery("#buildBundle #testBuildBundleButton").prop("disabled", false).css("color", "#000");
	jQuery("#buildBundle #finalBuildBundleButton").prop("disabled", false).css("color", "#000");
}

function validateBundleBuildFields(bundleDir, bundleName, startDate, endDate) {
	var valid = true;
	var errors = "";
	if (bundleDir == undefined || bundleDir == null || bundleDir == "") {
		errors += "missing bundle directory\n";
		valid = false;
	} 
	if (bundleName == undefined || bundleName == null || bundleName == "") {
		errors += "missing bundle build name\n";
		valid = false;
	} 
	else if(~bundleName.indexOf(" ")){
		errors += "bundle build name cannot contain spaces\n";
		valid = false;
	}
	if (startDate == undefined || startDate == null || startDate == "") {
		errors += "missing bundle start date\n";
		valid = false;
	}
	if (endDate == undefined || endDate == null || endDate == "") {
		errors += "missing bundle end date\n";
		valid = false;
	}
	if(errors.length > 0) {
		alert(errors);
	}
	return valid;
}

function bundleUrl(buildType) {
	var id = buildBundleId;
	var $resultLink = jQuery("#buildBundle #buildBundle_testResultLink #testResultLink");
	if (buildType == "final") {
		$resultLink = jQuery("#buildBundle #buildBundle_finalResultLink #finalResultLink");
	}
	jQuery("#buildBundle_exception").hide();
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/build/" + id + "/url?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var bundleResponse = response;
			if(bundleResponse.exception !=null) {
				$resultLink
				.text("(exception)")
				.css("padding-left", "5px")
				.css("font-size", "12px")
				.addClass("adminLabel")
				.css("color", "red");
			} else {
				$resultLink
				.text(bundleResponse.bundleResultLink)
				.css("padding-left", "5px")
				.css("font-size", "12px")
				.addClass("adminLabel")
				.css("color", "green");
			}
		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(bundleUrl.bind(null, buildType), 10000);
		}
	});
	var url = $esultLink.text();
	if (url == null || url == "") {
		window.setTimeout(bundleUrl.bind(null, buildType), 5000);
	}
}
function buildBundle(bundleName, startDate, endDate, bundleComment, archive, consolidate, predate, buildType){
	bundleDirectory = selectedDirectory;
	$("#testProgressBarDiv #testBuildProgress").text("Initializing build process");
	var $buildBundle_resultList = jQuery("#buildBundle_testResultList");
	if (buildType == "test") {
		$("#buildingTest").show();
		$("#buildBundle_testResultLink").show();
		$("#testProgressBarDiv").show();
		$("#bundleTestResultsHolder").show();
		$("#bundleTestResults").show();
		$("#buildingFinal").hide();
		$("#buildBundle_finalResultLink").hide();
		$("#finalProgressBarDiv").hide();
		$("#bundleFinalResults").hide();
	} else {
		$("#buildingTest").hide();
		$("#buildBundle_testResultLink").hide();
		$("#testProgressBarDiv").hide();
		$("#bundleFinalResultsHolder").show();
		$("#bundleTestResults").hide();
		$("#buildingFinal").show();
		$("#buildBundle_finalResultLink").show();
		$("#finalProgressBarDiv").show();
		$("#bundleFinalResults").show();
		$buildBundle_resultList = jQuery("#buildBundle_finalResultList");
	}
	var email = jQuery("#buildNotificationEmail").val();
	if (email == "") { email = "null"; }
	var data = {};
	data[csrfParameter] = csrfToken;
	data["bundleDirectory"] = bundleDirectory;
	data["bundleName"] = bundleName;
	data["email"] = email;
	data["bundleStartDate"] = startDate;
	data["bundleEndDate"] = endDate;
	data["archive"] = archive;
	data["consolidate"] = consolidate;
	data["predate"] = predate;
	data["bundleComment"] = bundleComment; /*comment needs to be the last on the form*/

	jQuery.ajax({
		url: "../../api/build/create",
		type: "POST",
		async: false,
		data: data,
		success: function(response) {
			var bundleResponse = response;
			if (bundleResponse != undefined) {
				//display exception message if there is any
				if(bundleResponse.exception !=null) {
					enableBuildButtons();
					alert(bundleResponse.exception.message);
				} else {
					$buildBundle_resultList.val("calling...");
					jQuery("#buildBundle_id").text(bundleResponse.id);
					buildBundleId = bundleResponse.id;
					window.setTimeout(updateBuildStatus.bind(null, buildType), 5000);
					bundleUrl(buildType);
				}
			} else {
				jQuery("#buildBundle_id").text(error);
				buildBundleId = error;
				$buildBundle_resultList.val("error");
				enableBuildButtons();
			}
		},
		error: function(request) {
			enableBuildButtons();
			alert("There was an error processing your request. Please try again.");
		}
	});
}

function updateBuildStatus(buildType) {
	console.log("build type: " + buildType);
	disableStageButton();
	disableDownloadButton();
	id = buildBundleId;
	// Initialize vars for Test section
	var $buildBundle_buildProgress = jQuery("#buildBundle_buildTestProgress");
	var $resultLink = jQuery("#testResultLink");
	var $progressBar = jQuery("#testProgressBar");
	var $buildingProgress = jQuery("#buildBundle #buildingTest #buildingTestProgress");
	var $buildProgress = jQuery("#testBuildProgress");
	var $buildBundle_resultList = jQuery("#buildBundle_testResultList");
	var $buildBundle_exception = jQuery("#buildBundle_testException");
	if (buildType == "final") {
		$buildBundle_buildProgress = jQuery("#buildBundle_buildFinalProgress");
		$resultLink = jQuery("#finalResultLink");
		$progressBar = jQuery("#finalProgressBar");
		$buildingProgress = jQuery("#buildBundle #buildingFinal #buildingFinalProgress");
		$buildProgress = jQuery("#finalBuildProgress");
		$buildBundle_resultList = jQuery("#buildBundle_finalResultList");
		$buildBundle_exception = jQuery("#buildBundle_finalException");
	}
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/build/" + id + "/list?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var txt = "";
			var currentTask = 0;
			var totalTasks = 0;
			var bundleResponse = response;
			if (bundleResponse == null) {
				$buildBundle_buildProgress.text("Bundle Status Unknown!");
				$buildingProgress.attr("src","../../css/img/dialog-warning-4.png");
				$buildBundle_resultList.val("unknown id=" + id);
				enableBuildButtons();
			} else {
				if (!$resultLink.text()) {
					$resultLink
						.text(bundleResponse.bundleResultLink)
						.css("padding-left", "5px")
						.css("font-size", "12px")
						.addClass("adminLabel")
						.css("color", "green");
				}
				jQuery("#Build #datasetName").text(bundleResponse.bundleDirectoryName);
				// Check if this was called via a Result Link
				if ((selectedDirectory == "") && fromResultLink) {
					selectedDirectory = bundleResponse.bundleDirectoryName;
					// Initialize all tabs as of selected on Choose tab
					onSelectDataset("existing");
				}
				var size = 0;
				if (bundleResponse.statusList != null) {
					size = bundleResponse.statusList.length;
				}
				if (size > 0) {
					for (var i=0; i<size; i++) {
						var nextLine = bundleResponse.statusList[i];
						if (nextLine.indexOf("running task") >= 0) {
							var idxCurrent = nextLine.search("\\d+/\\d+");
							var idxTotal = nextLine.indexOf("/", idxCurrent) + 1;
							var idxTotalEnd = nextLine.indexOf(")", idxTotal);
							if (idxCurrent > 0 && idxTotal > 0) {
								currentTask = parseInt(nextLine.substring(idxCurrent, idxTotal - 1));
								totalTasks = parseInt(nextLine.substring(idxTotal, idxTotalEnd));
								$progressBar.progressbar('value', (currentTask-1)/totalTasks * 100);
								$buildProgress.text("Completed " + (currentTask-1)
									+ " of " + totalTasks + " build tasks.");
							}
						} else if (currentTask > 0 && currentTask == totalTasks) { // All tasks finished
							$progressBar.progressbar('value', 100);
							$buildProgress.text("Completed " + currentTask
									+ " of " + totalTasks + " build tasks.");
						}
						txt = txt + nextLine + "\n";
					}
				}
				if (bundleResponse.complete == false ) {
					window.setTimeout(updateBuildStatus.bind(null, buildType), 5000); // recurse
				} else {
					$buildBundle_buildProgress.text("Bundle Complete!");
					$buildingProgress.attr("src","../../css/img/dialog-accept-2.png");
					updateBuildList(id, buildType);
					$("#Stage #staging_bundleName").text($("#Build #bundleBuildName").val());
					$("#Download #download_bundleName").text($("#Build #bundleBuildName").val());
					enableStageButton();
					enableDownloadButton();
					enableBuildButtons();
					// Update fields for Compare tab
					// Add dataset to lists if it isn't there already
					addToDatasetLists(selectedDirectory);
					updateFixedRouteParams(selectedDirectory);
				}
				fromResultLink = false;
				$buildBundle_resultList.val(txt).css("font-size", "12px");
				// Make sure that the textarea remains scrolled to the bottom.
				$buildBundle_resultList.scrollTop(1500);  // Just use some arbitrarily large number
				// check for exception
				if (bundleResponse.exception != null) {
					$buildBundle_buildProgress.text("Bundle Failed!");
					$buildProgress.text("Bundle build failed");
					$buildingProgress.attr("src","../../css/img/dialog-warning-4.png");
					if (bundleResponse.exception.message != undefined) {
						$buildBundle_exception.show().css("display","inline");
						$buildBundle_exception.html(bundleResponse.exception.message);
					}
					disableStageButton();
					disableDownloadButton();
					enableBuildButton();
				}
			}
		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(updateBuildStatus.bind(null, buildType), 10000);
		}
	});
}

//populate list of files that were result of building
function updateBuildList(id,buildType) {
	var summaryList = null;
	var data = {};
	data[csrfParameter] = csrfToken;
	data["ts"] = new Date().getTime();
	data["id"] = id;
	data["downloadFilename"] = "summary.csv";
	jQuery.ajax({
		url: "manage-bundles!downloadOutputFile.action",
		type: "POST",
		data: data,
			async: false,
			success: function(response){
				summaryList = response;
			}
	});

	var $buildBundle_fileList = jQuery("#buildBundle_testFileList");
	if (buildType == "final") {
		$buildBundle_fileList = jQuery("#buildBundle_finalFileList");
	}
	var lines = summaryList.split(/\n/);
	lines.pop(lines.length-1); // discard header
	var fileDescriptionMap = new Array();
	var fileCountMap = new Array();
	for (var i = 0; i < lines.length; i++) {
		var dataField = lines[i].split(',');
		fileDescriptionMap[dataField[0]] = dataField[1];
		fileCountMap[dataField[0]] = dataField[2];
	}
	jQuery.ajax({
		url: "manage-bundles!buildList.action?ts=" +new Date().getTime(),
		type: "GET",
		data: {"id": id},
		async: false,
		success: function(response) {
			var txt = "<ul>";

			var list = response;
			if (list != null) {
				var size = list.length;
				if (size > 0) {
					for (var i=0; i<size; i++) {
						var description = fileDescriptionMap[list[i]];
						var lineCount = fileCountMap[list[i]];
						var fileExtension = list[i].substring(list[i].lastIndexOf("."));
						// for html files, leave lineCount blank
						if (fileExtension === ".html") {
							lineCount = "";
						}
						if (description != undefined) {
							var encoded = encodeURIComponent(list[i]);
							txt = txt + "<li>" + description + ":" + "&nbsp;"
							+ lineCount + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							+ "<img src=\"../../css/img/go-down-5.png\" />"
							+ "<a href=\"manage-bundles!downloadOutputFile.action?id="
							+ id+ "&downloadFilename=" 
							+ encoded + "\">"
							+ fileExtension
							+  "</a></li>";
						}
					}
				}
			}
			// append log file
			txt = txt + "<li>" + "Bundle Builder Log:" + "&nbsp;"
			+ " " + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<img src=\"../../css/img/go-down-5.png\" />"
			+ "<a href=\"manage-bundles!downloadOutputFile.action?id="
			+ id+ "&downloadFilename=" 
			+ encodeURIComponent("bundleBuilder.out.txt") + "\">" + ".txt" +  "</a></li>";

			txt = txt + "</ul>";
			$buildBundle_fileList.html(txt).css("display", "block");
			/* This has only been implemented for MTA
			jQuery("#buildBundle #downloadTestLogs").show().css("display", "block");
			jQuery("#buildBundle #downloadTestLogs #downloadTestButton").attr("href", "manage-bundles!buildOutputZip.action?id=" + id);
			*/
			var continueButton = jQuery("#build_continue");
			enableContinueButton(continueButton);
		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(function() {
				updateBuildList(id, buildType);
			}, 10000);
		}
	});	
}
/**
 * Functions used with the Compare tab for generating reports on differences
 * between two bundle builds.
 */
function onCurrentDatasetChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#currentDatasetList option:selected").val() == 0) {
		resetCurrentReportDataset();
	} else {
		currentReportDataset = $("#currentDatasetList option:selected").text();
		currentReportBuildName = "";
		var buildNameList = getExistingBuildList(currentReportDataset);
		initBuildNameList($("#currentBuildNameList"), buildNameList);
	}
}

function onCurrentBuildNameChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#currentBuildNameList option:selected").val() == 0) {
		currentReportBuildName = "";
	} else {
		currentReportBuildName = $("#currentBuildNameList option:selected").text();
		if (currentReportDataset && currentReportBuildName
				&& compareToDataset && compareToBuildName) {
			buildDiffReport();
		}
	}
}

function onCurrentArchivedDatasetChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#currentArchivedDatasetList option:selected").val() == 0) {
		resetCurrentReportDataset();
	} else {
		currentArchivedReportDataset = $("#currentArchivedDatasetList option:selected").text();
		currentArchivedReportBuildName = "";
		var buildNameList = getExistingBuildList(currentArchivedReportDataset);
		initBuildNameList($("#currentArchivedBuildNameList"), buildNameList);
	}
}

function onCurrentArchivedBuildNameChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#currentArchivedBuildNameList option:selected").val() == 0) {
		currentArchivedReportBuildName = "";
	} else {
		currentArchivedReportBuildName = $("#currentArchivedBuildNameList option:selected").text();
		if (currentArchivedReportDataset && currentArchivedReportBuildName
				&& compareToArchivedDataset && compareToArchivedBuildName) {
			buildDiffReport();
		}
	}
}

function onCompareToDatasetChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#compareToDatasetList option:selected").val() == 0) {
		resetCompareToDataset();
	} else {
		compareToDataset = $("#compareToDatasetList option:selected").text();
		compareToBuildName = "";
		var buildNameList = getExistingBuildList(compareToDataset);
		initBuildNameList($("#compareToBuildNameList"), buildNameList);
	}
}

function onCompareToBuildNameChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#compareToBuildNameList option:selected").val() == 0) {
		compareToBuildName = "";
	} else {
		compareToBuildName = $("#compareToBuildNameList option:selected").text();
		if (currentReportDataset && currentReportBuildName
				&& compareToDataset && compareToBuildName) {
			buildDiffReport();
		}
	}
}

function onCompareToArchivedDatasetChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#compareToArchivedDatasetList option:selected").val() == 0) {
		resetCompareToReportDataset();
	} else {
		compareToArchivedDataset = $("#compareToArchivedDatasetList option:selected").text();
		compareToArchivedBuildName = "";
		var buildNameList = getExistingBuildList(compareToArchivedDataset);
		initBuildNameList($("#compareToArchivedBuildNameList"), buildNameList);
	}
}

function onCompareToArchivedBuildNameChange() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	if ($("#compareToArchivedBuildNameList option:selected").val() == 0) {
		compareToArchivedBuildName = "";
	} else {
		compareToArchivedBuildName = $("#compareToArchivedBuildNameList option:selected").text();
		if (currentArchivedReportDataset && currentArchivedReportBuildName
				&& compareToArchivedDataset && compareToArchivedBuildName) {
			buildDiffReport();
		}
	}
}

// Called when a dataset is selected on the Choose tab.
function updateFixedRouteParams(datasetName) {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();

	currentReportDataset = datasetName;
	currentReportBuildName = $("#bundleBuildName").val();
	// Select the current dataset name
	$("#currentDatasetList option").filter(function() {
	    return $(this).text() == datasetName;
	}).prop('selected', true);
	// Populate list of build names for this dataset and select the current one
	var buildNameList = getExistingBuildList(datasetName);
	initBuildNameList($("#currentBuildNameList"), buildNameList);
	$("#currentBuildNameList option").filter(function() {
	    return $(this).text() == $("#bundleBuildName").val();
	}).prop('selected', true);

	resetCompareToDataset();
	return;
}

function getExistingBuildList(datasetName) {
	var buildNameList;
	var useArchivedGtfs = jQuery("#useArchiveCheckbox").is(":checked");
	var data = {};
	data[csrfParameter] = csrfToken;
	data["selectedBundleName"] = datasetName;
	data["useArchivedGtfs"] = useArchivedGtfs;

	if (datasetName) {
		jQuery.ajax({
			url: "manage-bundles!existingBuildList.action",
			data: data,
			type: "POST",
			async: false,
			success: function(data) {
				buildNameList=data;
			}
		})
	}
	return buildNameList;
}

function initBuildNameList($buildNameList, buildNameMap) {
	var row_0 = '<option value="0">Select a build name</option>';
	$buildNameList.find('option').remove().end().append(row_0);
	var i;
	var getKeys = function(buildNameMap) {
		   var keys = [];
		   for(var key in buildNameMap){
		      keys.push(key);
		   }
		   return keys;
		}
	for (var key in buildNameMap) {
		var name = key;
		var gid = buildNameMap[key];
		//var nextRow = '<option value="' + (i+1) + '">' + buildNameList[i] + '</option>';
		var nextRow = '<option value="' + buildNameMap[key] + '">' + key + '</option>';
		$buildNameList.append(nextRow);
	}
	$buildNameList.val("0");
	return;
}

function resetCurrentReportDataset() {
	if (!jQuery("#useArchiveCheckbox").is(":checked")) {
		currentReportDataset = "";
		currentReportBuildName = "";
		$("#currentDatasetList").val("0");
		var row_0 = '<option value="0">Select a build name</option>';
		$("#currentBuildNameList").find('option').remove().end().append(row_0);
	} else {
		currentArchivedReportDataset = "";
		currentArchivedReportBuildName = "";
		$("#currentArchivedDatasetList").val("0");
		var row_0 = '<option value="0">Select an archived build name</option>';
		$("#currentArchivedBuildNameList").find('option').remove().end().append(row_0);
	}
}
function resetCompareToDataset() {
	if (!jQuery("#useArchiveCheckbox").is(":checked")) {
		compareToDataset = "";
		compareToBuildName = "";
		$("#compareToDatasetList").val("0");
		var row_0 = '<option value="0">Select a build name</option>';
		$("#compareToBuildNameList").find('option').remove().end().append(row_0);
	} else {
		compareToArchivedDataset = "";
		compareToArchivedBuildName = "";
		$("#compareToArchivedDatasetList").val("0");
		var row_0 = '<option value="0">Select an archived build name</option>';
		$("#compareToBuildNameList").find('option').remove().end().append(row_0);		
	}
}

function addToDatasetLists(directoryName) {
	var exists = false;
	$('#currentDatasetList option').each(function() {
	    if (this.text == directoryName) {
	        exists = true;
	    }
	});

	if (!exists) {
		var datasetAdded = false;
		$("#currentDatasetList option").each(function() {
			if (this.value > 0 && (directoryName < this.text)) {
				var newRow = '<option value=' + this.value + '>' + directoryName + '</option>';
				$(this).before(newRow);
				datasetAdded = true;
				return false;
			}
		});
		$("#compareToDatasetList option").each(function() {
			if (this.value > 0 && (directoryName < this.text)) {
				var newRow = '<option value=' + this.value + '>' + directoryName + '</option>';
				$(this).first().before(newRow);
				return false;
			}
		});
		if (!datasetAdded) {
			var datasetVal = $("#currentDatasetList > option").length;
			var newRow = '<option value=' + datasetVal + '>Select a build name</option>';
			$("#currentDatasetList").find('option').end().append(newRow);
			$("#compareToDatasetList").find('option').end().append(newRow);
		}
	}
}

function buildDiffReport() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();
	var useArchived = jQuery("#useArchiveCheckbox").is(":checked");
	if (!useArchived) {
		var dataset_1 = currentReportDataset;
		var dataset_1_build_id = 0;
		var dataset_2 = compareToDataset;
		var dataset_2_build_id = 0;
		var buildName_1 = currentReportBuildName;
		var buildName_2 = compareToBuildName;
	} else {
		$('#Compare #buildingReportDiv').show();
		var dataset_1 = currentArchivedReportDataset;
		var dataset_1_build_id = $('#currentArchivedBuildNameList option:selected').val();
		var dataset_2 = compareToArchivedDataset;
		var dataset_2_build_id = $('#compareToArchivedBuildNameList option:selected').val();
		var buildName_1 = currentArchivedReportBuildName;
		var buildName_2 = compareToArchivedBuildName;		
	}
	var data = {};
	data[csrfParameter] = csrfToken;
	data["useArchived"] = useArchived;
	data["datasetName"] = dataset_1;
	data["dataset_1_build_id"] = dataset_1_build_id;
	data["buildName"] =buildName_1;
	data["datasetName2"] = dataset_2;
	data["dataset_2_build_id"] =dataset_2_build_id;
	data["buildName2"] = buildName_2;

	jQuery.ajax({
		url: "compare-bundles!diffResult.action",
		data: data,
		type: "POST",
		async: false,
		success: function(data) {
			$('#Compare #buildingReportDiv').hide();
			$.each(data.diffResults, function(index, value) {
				// Skip first three rows of results
				if (index >= 3) {
					var diffRow = formatDiffRow(value);
					$("#diffResultsTable").append(diffRow);
				}
			});
			var baseBundle = dataset_1 + " / " + buildName_1;
			var compareToBundle = dataset_2 + " / " + buildName_2;
			$("#baseBundle").text(baseBundle + " (green)");
			$("#compareToBundle").text(compareToBundle + " (red)");
			$.each(data.fixedRouteDiffs, function(index, value) {
				var modeName = value.modeName;
				var modeClass = "";
				var modeFirstLineClass=" modeFirstLine";
				var addSpacer = true;
				if (value.srcCode == 1) {
					modeClass = "currentRpt";
				} else if (value.srcCode == 2) {
					modeClass = "selectedRpt";
				}
				$.each(value.routes, function(index2, value2) {
					var routeNum = value2.routeNum;
					var routeName = value2.routeName;
					var routeFirstLineClass=" routeFirstLine";
					addSpacer = false;
					if (index2 > 0) {
						modeName = "";
						modeFirstLineClass = "";
					}
					var routeClass = modeClass;
					if (value2.srcCode == 1) {
						routeClass = "currentRpt";
					} else if (value2.srcCode == 2) {
						routeClass = "selectedRpt";
					}
					$.each(value2.headsignCounts, function(headsignIdx, headsign) {
						var headsignName = headsign.headsign;
						var headsignBorderClass = "";
						if (headsignIdx > 0) {
							modeName = "";
							routeNum = "";
							routeName = "";
							modeFirstLineClass = "";
							routeFirstLineClass = "";
							headsignBorderClass = " headsignBorder";
							addSpacer = false;
						}
						var headsignClass = routeClass;
						if (headsign.srcCode == 1) {
							headsignClass = "currentRpt";
						} else if (headsign.srcCode == 2) {
							headsignClass = "selectedRpt";
						}
						$.each(headsign.dirCounts, function(dirIdx, direction) {
							var dirName = direction.direction;
							var dirBorderClass = "";
							if (dirIdx > 0) {
								modeName = "";
								routeNum = "";
								routeName = "";
								headsignName = "";
								modeFirstLineClass = "";
								routeFirstLineClass = "";
								headsignBorderClass = "";
								dirBorderClass = " dirBorder";
								addSpacer = false;
							}
							var dirClass = headsignClass;
							if (direction.srcCode == 1) {
								dirClass = "currentRpt";
							} else if (direction.srcCode == 2) {
								dirClass = "selectedRpt";
							}
							$.each(direction.stopCounts, function(index3, value3) {
								var stopCt = value3.stopCt;
								var stopClass = "";
								if (dirClass == "currentRpt") {
									stopClass = "currentStopCt";
								} else if (dirClass == "selectedRpt") {
									stopClass = "selectedStopCt";
								}
								if (value3.srcCode == 1) {
									stopClass = "currentStopCt";
								} else if (value3.srcCode == 2) {
									stopClass = "selectedStopCt";
								}
								var weekdayTrips = value3.tripCts[0];
								var satTrips = value3.tripCts[1];
								var sunTrips = value3.tripCts[2];
								if (index3 > 0) {
									modeName = "";
									modeFirstLineClass = "";
									routeNum = "";
									routeName = "";
									headsignName = "";
									dirName = "";
									routeFirstLineClass = "";
									headsignBorderClass = "";
									dirBorderClass = "";
									addSpacer = false;
								}
								if (index > 0 && headsignIdx == 0
										&& dirIdx == 0 && index3 == 0) {
									addSpacer = true;
								}
								if (addSpacer) {
									var new_spacer_row = '<tr class="spacer"> \
										<td></td> \
										<td></td> \
										<td></td> \
										<td></td> \
										<td></td> \
										<td></td> \
										<td></td> \
										<td></td> \
										<td></td> \
										</tr>';
									$('#fixedRouteDiffTable').append(new_spacer_row);
								}
								var new_row = '<tr class="fixedRouteDiff' + modeFirstLineClass + routeFirstLineClass + '"> \
									<td class="' + modeClass + ' modeName" >' + modeName + '</td> \
									<td class="' + routeClass + routeFirstLineClass + ' rtNum" >' + routeNum + '</td> \
									<td class="' + routeClass + routeFirstLineClass + '">' + routeName + '</td> \
									<td class="' + headsignClass + routeFirstLineClass + headsignBorderClass + '">' + headsignName + '</td> \
									<td class="' + dirClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + dirName + '</td> \
									<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + stopCt + '</td> \
									<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + weekdayTrips + '</td> \
									<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + satTrips + '</td> \
									<td class="' + stopClass + routeFirstLineClass + headsignBorderClass + dirBorderClass + '">' + sunTrips + '</td> \
									</tr>';
								$('#fixedRouteDiffTable').append(new_row);
							});
						});
					});
				});
			});
			// Add bottom border to reprot
			var new_spacer_row = '<tr class="spacer"> \
				<td></td> \
				<td></td> \
				<td></td> \
				<td></td> \
				<td></td> \
				<td></td> \
				<td></td> \
				<td></td> \
				<td></td> \
				</tr>';
			$('#fixedRouteDiffTable').append(new_spacer_row);
		}
	})
}

/*
 * This is used in the Compare tab to format each row for the
 * Diff Results table.
 */
function formatDiffRow(value) {
	var tokens = value.split(",");
	var newRow = "";
	var dataClass = "redListData";
	var testChar = tokens[0].charAt(0);
	if (tokens[0].charAt(0) == '+') {
		dataClass = "greenListData";
	}
	var teststr = tokens[0].substr(1);
	tokens[0] = tokens[0].substr(1);
	var dataItems = "";
	tokens.forEach(function(entry, idx) {
		if (entry == "null") {
			entry = "";
		}
		var tdClass = "";
		if (!isNaN(entry) && idx > 0) {
			tdClass = " class=numericTd ";
		}
		dataItems += "<td" + tdClass + ">" + entry + "</td>";
	});
	var newRow = "<tr class=" + dataClass + " >"
		+ dataItems
		+ "</tr>";
	return newRow;
}


function onStageClick() {
	stageBundle();
}

function stageBundle() {
	var environment = jQuery("#deploy_environment").text();
	var bundleDir = selectedDirectory;
	var bundleName = jQuery("#Build #bundleBuildName").val();
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/bundle/stagerequest/" + environment + "/" + bundleDir + "/" + bundleName + "?ts=" +new Date().getTime(), 
		type: "GET",
		async: false,
		success: function(response) {
			/*var bundleResponse = response;
				if (bundleResponse != undefined) {
					if (typeof response=="string") {
						if (bundleResponse.match(/SUCCESS/)) {
							toggleStageBundleResultList();
							jQuery("#bundleStagingResultsHolder #bundleStagingResults #stageBundle_progress").show().css("display","block");
							jQuery("#bundleStagingResultsHolder #bundleStagingResults #stageBundle_resultList").show().css("display","block");
							jQuery("#stageBundle_resultList").html(bundleName);
							jQuery("#stageContentsHolder #stageBox #staging #stagingProgress").attr("src","../../css/img/dialog-accept-2.png");
							jQuery("#stageBundle_stageProgress").text("Staging Complete!");
							var continueButton = jQuery("#stage_continue");
							enableContinueButton(continueButton);
						} else {
							jQuery("#stageBundle_id").text("Failed to Stage requested Bundle!");
							jQuery("#stageBundle_resultList").html("error");
						}
					}*/
			var bundleResponse = response;
			if (bundleResponse != undefined) {
				// the header is set wrong for the proxied object, run eval to correct
				if (typeof response=="string") {
					bundleResponse = eval('(' + response + ')');
				}
				jQuery("#stageBundle_resultList").html("calling...");
				jQuery("#stageBundle_id").text(bundleResponse.id);
				jQuery("#stageBundle #requestLabels").show().css("display","block");
				jQuery("#stageContentsHolder #stageBox #staging").show().css("display","block");
				jQuery("#stageBundle_stageProgress").text("Staging ...");
				jQuery("#stageContentsHolder #stageBox #staging #stagingProgress").attr("src","../../css/img/ajax-loader.gif");
				window.setTimeout(updateStageStatus, 5000);
			} else {
				jQuery("#stageBundle_id").text(error);
				jQuery("#stageBundle_resultList").html("error");
			}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});

}

function updateStageStatus() {
	id = jQuery("#stageBundle_id").text();
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/bundle/stage/status/" + id + "/list?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var txt = "<ul>";
			var bundleResponse = response;
			if (bundleResponse == null) {
				jQuery("#stageBundle_stageProgress").text("Stage Complete!");
				jQuery("#stageContentsHolder #stageBox #staging #stagingProgress").attr("src","../../css/img/dialog-warning-4.png");
				jQuery("#stageBundle_resultList").html("unknown id=" + id);
				return;
			}
			// the header is set wrong for the proxied object, run eval to correct
			if (typeof response=="string") {
				bundleResponse = eval('(' + response + ')');
			}
			if (bundleResponse.status != "complete" && bundleResponse.status != "error") {
				window.setTimeout(updateStageStatus, 5000); // recurse
			} else {
				toggleStageBundleResultList();
				jQuery("#bundleStagingResultsHolder #bundleStagingResults #stageBundle_progress").show().css("display","block");
				jQuery("#bundleStagingResultsHolder #bundleStagingResults #stageBundle_resultList").show().css("display","block");
				if (bundleResponse.status == "complete") {
					jQuery("#stageContentsHolder #stageBox #staging #stagingProgress").attr("src","../../css/img/dialog-accept-2.png");
					jQuery("#stageBundle_stageProgress").text("Staging Complete!");
					// set resultList to bundleNames list
					var size = bundleResponse.bundleNames.length;
					if (size > 0) {
						for (var i=0; i<size; i++) {
							txt = txt + "<li>" + bundleResponse.bundleNames[i] + "</li>";
						}
					}
					var continueButton = jQuery("#stage_continue");
					enableContinueButton(continueButton);
				} else {
					jQuery("#stageContentsHolder #stageBox #staging #stagingProgress").attr("src","../../css/img/dialog-warning-4.png");
					jQuery("#stageBundle_stageProgress").text("Staging Failed!");
					// we've got an error
					txt = txt + "<li><font color=\"red\">ERROR!  Please consult the logs and check the "
					+ "filesystem permissions before continuing</font></li>";
				}
			}
			txt = txt + "</ul>";
			jQuery("#stageBundle_resultList").html(txt).css("font-size", "12px");	
		},
		error: function(request) {
			clearTimeout(timeout);

			jQuery("#stageContentsHolder #stagingBox #staging #stagingProgress").attr("src","../../css/img/dialog-warning-4.png");
			jQuery("#stageBundle_stageProgress").text("Staging Failed!");
			jQuery("#bundleStagingResultsHolder #bundleStagingResults #stageBundle_progress").show().css("display","block");
			jQuery("#bundleStagingResultsHolder #bundleStagingResults #stageBundle_resultList").show().css("display","block");
			toggleStageBundleResultList();

			// error out on a 500 error, the session will be lost so it will not recover
			var txt = "<ul>";
			txt = txt + "<li><font color=\"red\">The server returned an internal error.  Please consult the logs" 
			+ " or retry your request</font></li>";
			txt = txt + "</ul>";
			jQuery("#stageBundle_resultList").html(txt).css("font-size", "12px");
		}
	});
}


function onDeployClick() {
	deployBundle();
}

function deployBundle(){
	var environment = jQuery("#deploy_environment").text();
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/bundle/deploy/from/" + environment + "?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var bundleResponse = response;
			if (bundleResponse != undefined) {
				// the header is set wrong for the proxied object, run eval to correct
				if (typeof response=="string") {
					bundleResponse = eval('(' + response + ')');
				}
				jQuery("#deployBundle_resultList").html("calling...");
				jQuery("#deployBundle_id").text(bundleResponse.id);
				jQuery("#deployBundle #requestLabels").show().css("display","block");
				jQuery("#deployContentsHolder #deployBox #deploying").show().css("display","block");
				jQuery("#deployBundle_deployProgress").text("Deploying ...");
				jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../../css/img/ajax-loader.gif");
				window.setTimeout(updateDeployStatus, 5000);
			} else {
				jQuery("#deployBundle_id").text(error);
				jQuery("#deployBundle_resultList").html("error");
			}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
}

function updateDeployStatus() {
	id = jQuery("#deployBundle_id").text();
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/bundle/deploy/status/" + id + "/list?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var txt = "<ul>";
			var bundleResponse = response;
			if (bundleResponse == null) {
				jQuery("#deployBundle_deployProgress").text("Deploy Complete!");
				jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../../css/img/dialog-warning-4.png");
				jQuery("#deployBundle_resultList").html("unknown id=" + id);
				return;
			}
			// the header is set wrong for the proxied object, run eval to correct
			if (typeof response=="string") {
				bundleResponse = eval('(' + response + ')');
			}
			if (bundleResponse.status != "complete" && bundleResponse.status != "error") {
				window.setTimeout(updateDeployStatus, 5000); // recurse
			} else {
				toggleDeployBundleResultList();
				jQuery("#bundleResultsHolder #bundleResults #deployBundle_progress").show().css("display","block");
				jQuery("#bundleResultsHolder #bundleResults #deployBundle_resultList").show().css("display","block");
				if (bundleResponse.status == "complete") {
					jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../../css/img/dialog-accept-2.png");
					jQuery("#deployBundle_deployProgress").text("Deploy Complete!");
					// set resultList to bundleNames list
					var size = bundleResponse.bundleNames.length;
					if (size > 0) {
						for (var i=0; i<size; i++) {
							txt = txt + "<li>" + bundleResponse.bundleNames[i] + "</li>";
						}
					}
					var continueButton = jQuery("#deploy_continue");
					enableContinueButton(continueButton);
				} else {
					jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../../css/img/dialog-warning-4.png");
					jQuery("#deployBundle_deployProgress").text("Deploy Failed!");
					// we've got an error
					txt = txt + "<li><font color=\"red\">ERROR!  Please consult the logs and check the "
					+ "filesystem permissions before continuing</font></li>";
				}
			}
			txt = txt + "</ul>";
			jQuery("#deployBundle_resultList").html(txt).css("font-size", "12px");	
		},
		error: function(request) {
			clearTimeout(timeout);
			toggleDeployBundleResultList();
			jQuery("#deployContentsHolder #deployBox #deploying #deployingProgress").attr("src","../../css/img/dialog-warning-4.png");
			jQuery("#deployBundle_deployProgress").text("Deploy Failed!");
			jQuery("#bundleResultsHolder #bundleResults #deployBundle_progress").show().css("display","block");
			jQuery("#bundleResultsHolder #bundleResults #deployBundle_resultList").show().css("display","block");

			// error out on a 500 error, the session will be lost so it will not recover
			var txt = "<ul>";
			txt = txt + "<li><font color=\"red\">The server returned an internal error.  Please consult the logs" 
			+ " or retry your request</font></li>";
			txt = txt + "</ul>";
			jQuery("#deployBundle_resultList").html(txt).css("font-size", "12px");
		}
	});
}

function onDeployListClick(){
	var environment = jQuery("#deploy_environment").text();
	// TODO consider POST
	jQuery.ajax({
		url: "../../api/bundle/deploy/list/" + environment + "?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var bundleResponse = response;
			if (bundleResponse != undefined) {
				var txt = "<ul>";
				// the header is set wrong for the proxied object, run eval to correct
				if (typeof response=="string") {
					bundleResponse = eval('(' + response + ')');
				}
				// parse array of bundle names
				var size = bundleResponse.length;
				if (size > 0) {
					for (var i=0; i<size; i++) {
						txt = txt + "<li>" + bundleResponse[i] + "</li>";
					}
				}
				txt = txt + "</ul>";
				jQuery("#deployBundle_bundleList").html(txt).css("font-size", "12px");	

			}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
}

//download the specified bundle
function onDownloadBundleClick() {
	var downloadDataset = selectedDirectory;
	var downloadFileName = $("#Download #download_bundleName").text();
	window.location='manage-bundles!downloadBundle.action'
		+ '?downloadDataSet=' + downloadDataset
		+ '&downloadFilename=' + downloadFileName
}

// Sync active bundle with staging
function onSyncDeployedBundleClick() {
	var environment = jQuery("#deploy_environment").text();
	$("#Sync #syncProgressIcon").attr("src", "../../css/img/ajax-loader.gif");
	$("#syncProgressText").text("Syncing bundles in Progress...");
	$("#Sync #syncProgressDiv").show();
	jQuery.ajax({
		url: "sync-bundle!syncBundle.action?ts=" + new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var bundleResponse = response;
			$("#Sync #syncProgressIcon").attr("src", "../../css/img/dialog-accept-2.png");
			$("#syncProgressText").text("Syncing bundles complete!");
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
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
