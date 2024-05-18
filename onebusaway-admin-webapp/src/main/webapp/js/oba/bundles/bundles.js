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
		if (ui.tab.hash == "#Deploy") {
			// when deploy tab clicked, pre-load some data
			jQuery("#deployBundle_listButton").click();
			jQuery("#deployBundle_listCurrentButton").click();
		}
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

	initCreate();


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

	// delete existing dataset
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

	//Handle deploy list button click event
	jQuery("#deployBundle_listButton").click(onDeployListClick);
	jQuery("#deployBundle_listCurrentButton").click(deployListBundles);

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

	// for deploying named bundle
	$("#DeployingPopup").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		buttons: [{
			id: "deployingSuccessCancel",
			text: "Continue",
			click: function() {
				$(this).dialog("close");
			}
		}],
		open: function() {
			$('.ui-dialog-buttonpane').find('button:contains("Continue")').addClass('cancelDeletePopup');
		}
	});

	// For Delete deployment
	$("#deleteDeployPopup").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		buttons: [{
			id: "deleteDeployCancel",
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		},
			{
				id: "deleteDeployContinue",
				text: "Delete dataset",
				click: function() {
					$(this).dialog("close");
					onDeleteDeployConfirmed();
				}
			}],
		open: function() {
			$('.ui-dialog-buttonpane').find('button:contains("Cancel")').addClass('cancelDeletePopup');
		}
	});

	// For "Delete Success" popup to confirm the directory was deleted
	$("#deleteDeploySuccessPopup").dialog({
		autoOpen: false,
		modal: true,
		width: 'auto',
		buttons: [{
			id: "deleteDeploySuccessCancel",
			text: "Continue",
			click: function() {
				$(this).dialog("close");
			}
		}],
		open: function() {
			$('.ui-dialog-buttonpane').find('button:contains("Continue")').addClass('cancelDeletePopup');
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
	// load some data when tab becomes active
	jQuery("#deployBundle_listButton").click();
	jQuery("#deployBundle_listCurrentButton").click();
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

function onDeleteDeployedClick() {
	selectedDirectory = $(this).closest("tr").find(".deployedItemName").text();
	var continueDelete = $("#deleteDeployPopup").dialog("open");
}

function onAnyCommentsClick() {
	$("#addCommentsPopup").dialog("open");
}


function onDeleteDeployedItemClick() {
	var selectedItem = selectedDirectory;
	console.log("requesting delete of deployed item " + selectedItem + " using CSRF token " + csrfToken);
	jQuery.ajax({
		url: "../../api/bundle/deploy/delete/" + selectedItem + "?ts=" + new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			$("#deleteSuccessPopup").dialog("open");
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
