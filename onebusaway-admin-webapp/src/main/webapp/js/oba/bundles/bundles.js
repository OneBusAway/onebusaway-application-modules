/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
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

var timeout = null;
var agencyMetadataAvailable = false;
var agencyMetadata;		//For agency metadata

jQuery(function() {
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
	var qs = parseQuerystring();
	if (qs["fromEmail"] == "true") {
		//alert("called from email!");
		jQuery("#prevalidate_id").text(qs["id"]);
		jQuery("#buildBundle_id").text(qs["id"]);
		jQuery("#buildBundle_bundleName").val(qs["name"]);
		jQuery("#buildBox #bundleStartDateHolder #startDatePicker").val(qs["startDate"]);
		jQuery("#buildBox #bundleEndDateHolder #endDatePicker").val(qs["endDate"]);
		jQuery("#comments").val(qs["bundleComment"]);
		//hide the result link when reentering from email
		jQuery("#buildBundle_resultLink").hide();
		// just in case set the tab
		var $tabs = jQuery("#tabs");
		$tabs.tabs('select', 3);
		updateBuildStatus();
	}
	// politely set our hash as tabs are changed
	jQuery("#tabs").bind("tabsshow", function(event, ui) {
		window.location.hash = ui.tab.hash;
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
				jQuery.ajax({
					url: "manage-bundles!existingBuildList.action",
					data: {
						"diffBundleName" : names[0]
					},
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
				jQuery.ajax({
					url: "manage-bundles!diffResult.action",
					data: {
						"diffBundleName" : bundleNames[0],
						"diffBuildName" : buildNames[0],
						"bundleDirectory" : jQuery("#createDirectory #directoryName").val(),
						"bundleName": jQuery("#buildBundle_bundleName").val()
					},
					type: "GET",
					async: false,
					success: function(data) {
						$('#diffResult').text('');
						$.each(data, function(index, value) {
							$('#diffResult').append(
									"<div id=\"diffResultItem\">"+value+"</div>");
						});
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

	// hookup ajax call to select
	jQuery("#directoryButton").click(onSelectClick);

	// upload bundle source data for selected agency
	jQuery("#uploadSelectedAgenciesButton").click(onUploadSelectedAgenciesClick);

	// add another row to the list of agencies and their source data
	jQuery("#addAnotherAgencyButton").click(onAddAnotherAgencyClick);

	// toggle agency row as selected when checkbox is clicked
	jQuery("#agency_data").on("change", "tr :checkbox", onSelectAgencyChange);

	// change input type to 'file' if protocol changes to 'file'
	jQuery("#agency_data").on("change", "tr .agencyProtocol", onAgencyProtocolChange);

	// remove selected agencies
	jQuery("#removeSelectedAgenciesButton").click(onRemoveSelectedAgenciesClick);

	//toggle advanced option contents
	jQuery("#createDirectory #advancedOptions #expand").bind({
		'click' : toggleAdvancedOptions	});

	//initially hide the Request Id label if the Request Id is blank
	if (jQuery("#prevalidate_id").text().length == 0) {
		jQuery("#prevalidate_id_label").hide();
	}
	
	//initially hide the Validation Progress label
	jQuery("#prevalidate_progress").hide();
	
	//toggle validation progress list
	jQuery("#prevalidateInputs #prevalidate_progress #expand").bind({
		'click' : toggleValidationResultList});

	//toggle bundle build progress list
	jQuery("#buildBundle #buildBundle_progress #expand").bind({
		'click' : toggleBuildBundleResultList});

	//handle create, select and copy radio buttons
	jQuery("input[name='options']").change(directoryOptionChanged);

	//Handle validate button click event
	jQuery("#prevalidateInputs #validateBox #validateButton").click(onValidateClick);

	//Handle build button click event
	jQuery("#buildBundle_buildButton").click(onBuildClick);

	//Handle reset button click event
	jQuery("#buildBundle_resetButton").click(onResetClick);

	//Enable or disable create/select button when user enters/removes directory name
	//For a copy, a value must also be provided for the destination directory
	//Using bind() with propertychange event as live() does not work in IE for unknown reasons
	jQuery("#createDirectoryContents #directoryName").bind("input propertychange", function() {
		var text = jQuery("#createDirectory #directoryName").val();
		var copyDestText = jQuery("#createDirectory #destDirectoryName").val();
		if (text.length > 0 && (!jQuery("#copy").is(":checked") || copyDestText.length > 0)) {
			enableSelectButton();
		} else {
			disableSelectButton();
			jQuery("#createDirectory #createDirectoryContents #createDirectoryResult").hide();
		}
	});
	
	disableStageButton();
	disableBuildButton();

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
	
	//Retrieve transit agency metadata
	getAgencyMetadata();
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
	if (url.toLowerCase().startsWith("http")) {
	  protocol = "http";
	} else if (url.toLowerCase().startsWith("ftp")) {
		protocol = "ftp";
	}
	if ((previous_protocol == "file" && protocol != "file")
			|| (previous_protocol != "file" && protocol == "file")) {
		var dataSource = $(this).closest('tr').find(".agencyDataSource");
		if (protocol == "file") {
			dataSource.clone().attr('type','file').insertAfter(dataSource).prev().remove();
		} else if (dataSource.attr('type') == 'file') {
			dataSource.clone().attr('type','text').insertAfter(dataSource).prev().remove();
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
			//showThisAgency(agency);
			jQuery("#agencyId").val(agency.agencyId);
		    jQuery("#agencyDataSource").val(agency.agencyDataSource);
		    jQuery("#agencyDataSourceType").val(agency.agencyDataSourceType);
		    jQuery("#agencyProtocol").val(agency.agencyProtocol);
		});
	}

	if (bundleObj.validationResponse != undefined) {
		//Populating Pre-Validate Tab Fields
		jQuery("#prevalidate_bundleName").val(bundleObj.validationResponse.bundleBuildName);
		jQuery("#prevalidate_id").text(bundleObj.validationResponse.requestId);
		if (jQuery("#prevalidate_id").text().length > 0) {
			jQuery("#prevalidate_id_label").show();
		}
		setDivHtml(document.getElementById('prevalidate_resultList'), bundleObj.validationResponse.statusMessages);
	}
	
	if (bundleObj.buildResponse == undefined) {
		// Set Comments field to ""
		jQuery("#commentBox #bundleComment").val("");
		return;
	}
	
	//Populating Build Tab Fields
	if (bundleObj.buildResponse.email != undefined && bundleObj.buildResponse.email != null 
			&& bundleObj.buildResponse.email != "null") {
		jQuery("#buildBundle_email").val(bundleObj.buildResponse.email);
	}
	jQuery("#buildBundle_bundleName").val(bundleObj.buildResponse.bundleBuildName);
	jQuery("#startDatePicker").val(bundleObj.buildResponse.startDate);
	jQuery("#startDate").val(bundleObj.buildResponse.startDate);
	jQuery("#endDatePicker").val(bundleObj.buildResponse.endDate);
	jQuery("#endDate").val(bundleObj.buildResponse.endDate);
	jQuery("#commentBox #bundleComment").val(bundleObj.buildResponse.comment);
	jQuery("#selected_bundleDirectory").text(bundleObj.directoryName);
	jQuery("#buildBundle_id").text(bundleObj.buildResponse.requestId);
	setDivHtml(document.getElementById('buildBundle_resultList'), bundleObj.buildResponse.statusMessages);
	showBuildFileList(bundleObj.buildResponse.buildOutputFiles, bundleObj.buildResponse.requestId);
}
function onSelectClick() {
	var bundleDir = jQuery("#createDirectory #directoryName").val();
	var actionName = "selectDirectory";
	var copyDir = "";

	// initially hide the Request Id label when picking a new bundle
	jQuery("#prevalidate_id_label").hide();
	
	if (jQuery("#create").is(":checked")) {
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

	if(jQuery("#copy").is(":checked")) {
		copyDir = jQuery("#destDirectoryName").val();
		actionName = "copyDirectory";
	}

	jQuery.ajax({
		url: "manage-bundles!" + actionName + ".action?ts=" +new Date().getTime(),
		type: "GET",
		data: {"directoryName" : bundleDir,
			"destDirectoryName" : copyDir},
			async: false,
			success: function(response) {				
				disableSelectButton();
				var status = response;
				if (status != undefined) {
					jQuery("#createDirectory #createDirectoryContents #createDirectoryResult").show().css("display","block");
					if(status.selected == true) {
						jQuery("#createDirectoryResult #resultImage").attr("src", "../../css/img/dialog-accept-2.png");
						jQuery("#createDirectoryMessage").text(status.message).css("color", "green");
						// If "createDirectory", add the new directory to the current list of bundle directories.
						if (actionName == "createDirectory") {
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
						enableBuildButton();
						enableResetButton();
					}					
					else {
						jQuery("#createDirectoryResult #resultImage").attr("src", "../../css/img/warning_16.png");
						jQuery("#createDirectoryMessage").text(status.message).css("color", "red");
						disableBuildButton();
						disableResetButton();
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
					jQuery("#prevalidate_bundleDirectory").text(bundleDir);
					jQuery("#selected_bundleDirectory").text(bundleDir);
					jQuery("#s3_location").text(status.bucketName);
					jQuery("#gtfs_location").text(bundleDir + "/" + status.gtfsPath + " directory");
					jQuery("#stif_location").text(bundleDir + "/" + status.stifPath + " directory");
					enableContinueButton(jQuery("#upload_continue"));
				} else {
					alert("null status");
					disableBuildButton();
				}
			},
			error: function(request) {
				alert("There was an error processing your request. Please try again.");
			}
	});
}

function onUploadSelectedAgenciesClick() {
	var bundleDir = jQuery("#createDirectory #directoryName").val();
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
				url: "manage-bundles!" + actionName + ".action?ts=" + new Date().getTime(),
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
			//var agencyFile = agencyDataFile.files[0];
			console.log("file name is: " + agencyDataFile.name);
			var formData = new FormData();
			formData.append("ts", new Date().getTime());
			formData.append("directoryName", bundleDir);
			formData.append("agencyId", agencyId);
			formData.append("agencyDataSourceType", agencyDataSourceType);
			formData.append("agencySourceFile", agencyDataFile);
			formData.append("cleanDir", cleanDir);
			var actionName = "uploadSourceFile";
			jQuery.ajax({
				url: "manage-bundles!" + actionName + ".action",
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
}

function onAddAnotherAgencyClick() {
	var metadata = "";
	var url = "";
	if (agencyMetadataAvailable) {
	  metadata = '<select class="agencyIdSelect">';
	  for (var i=0; i<agencyMetadata.length; ++i) {
		  metadata += '<option value="' + agencyMetadata[i].legacyId + '">'
		  + agencyMetadata[i].shortName + '</option>';
	  }
	  metadata += '</select>';
	  url = agencyMetadata[0].gtfsFeedUrl;
	  if (url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("ftp")) {
	  	var urlValue = ' value="' + url + '"';
	  }
	}
	var new_row = '<tr> \
		<td><div><input type="checkbox" /></div></td> \
		<td>' + metadata + '<input type="text" class="agencyId"/></td> \
		<td><select class="agencyDataSourceType"> \
		<option value="gtfs">gtfs</option> \
		<option value="aux">aux</option> \
		</select></td> \
		<td><select class="agencyProtocol"> \
		<option value="http">http</option> \
		<option value="ftp">ftp</option> \
		<option value="file">file</option> \
		</select></td> \
		<td><input type="text" class="agencyDataSource" ' + urlValue + '/></td> \
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
	//var elementType = $(this).prop('tagName');
	var dataSource = $(this).closest('tr').find(".agencyDataSource");
	if (protocol == "file") {
		dataSource.clone().attr('type','file').insertAfter(dataSource).prev().remove();
	} else if (dataSource.attr('type') == 'file') {
		dataSource.clone().attr('type','text').insertAfter(dataSource).prev().remove();
	}
}

function onRemoveSelectedAgenciesClick() {
	console.log("in onRemoveSelectedAgenciesClick, v2");
	$('#agency_data .agencySelected').remove();
}

function enableContinueButton(continueButton) {
	jQuery(continueButton).removeAttr("disabled").css("color", "#000");
}

function disableContinueButton(continueButton) {
	jQuery(continueButton).attr("disabled", "disabled").css("color", "#999");
}

function enableSelectButton() {
	jQuery("#createDirectory #createDirectoryContents #directoryButton").removeAttr("disabled").css("color", "#000");
}

function disableSelectButton() {
	jQuery("#createDirectory #createDirectoryContents #directoryButton").attr("disabled", "disabled").css("color", "#999");
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

function enableBuildButton() {
	jQuery("#buildBundle_buildButton").removeAttr("disabled").css("color", "#000");
	enableContinueButton($("#create_continue"));
}

function disableBuildButton() {
	jQuery("#buildBundle_buildButton").attr("disabled", "disabled").css("color", "#999");
	disableContinueButton($("#create_continue"));
}

function enableResetButton() {
	jQuery("#buildBundle_resetButton").removeAttr("disabled").css("color", "#000");
}

function disableResetButton() {
	jQuery("#buildBundle_resetButton").attr("disabled", "disabled").css("color", "#999");
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

function toggleBuildBundleResultList() {
	var $image = jQuery("#buildBundle #buildBundle_progress #expand");
	changeImageSrc($image);
	//Toggle progress result list
	jQuery("#buildBundle #buildBundle_resultList").toggle();
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
		jQuery("#commentBox #bundleComment").val("");
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
					var text = jQuery("#createDirectory #directoryName").val();
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
		// TODO replace the exitingDirectories form call with this below
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

	jQuery("#prevalidate_progress").show();
	jQuery("#prevalidate_exception").hide();
	jQuery("#prevalidateInputs #validateBox #validateButton").attr("disabled", "disabled");
	jQuery("#prevalidateInputs #validateBox #validating").show().css("display","inline");
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
				//jQuery("#prevalidate_resultList").text("calling...");
				window.setTimeout(updateValidateStatus, 5000);
			} else {
				jQuery("#prevalidate_id").text(error);
				jQuery("#prevalidate_resultList").text("error");
				if (jQuery("#prevalidate_id").text().length > 0) {
					jQuery("#prevalidate_id_label").show();
				}
			}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
}

function updateValidateStatus() {
	var id = jQuery("#prevalidate_id").text();
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
	jQuery.ajax({
		url: "manage-bundles!fileList.action?ts=" +new Date().getTime(),
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

function onBuildClick() {
	var bundleDir = jQuery("#createDirectory #directoryName").val();
	var bundleName = jQuery("#buildBundle_bundleName").val();
	var startDate = jQuery("#startDate").val();
	var endDate = jQuery("#endDate").val();
	var bundleComment = jQuery("#bundleComment").val();
	var archive = jQuery("#buildBundle_archive").is(":checked");
	var consolidate = jQuery("#buildBundle_consolidate").is(":checked");
	var predate = jQuery("#buildBundle_predate").is(":checked");

	var valid = validateBundleBuildFields(bundleDir, bundleName, startDate, endDate);
	if(valid == false) {
		return;
	}
	jQuery("#buildBundle #buildBox #building #buildingProgress").attr("src","../../css/img/ajax-loader.gif");
	jQuery("#buildBundle_buildProgress").text("Bundle Build in Progress...");
	jQuery("#buildBundle_fileList").html("");
	jQuery("#buildBundle #downloadLogs").hide();
	jQuery("#buildBundle #buildBox #building").show().css("width","300px").css("margin-top", "20px");

	disableBuildButton();
	disableResetButton();
	buildBundle(bundleName, startDate, endDate, bundleComment, archive, consolidate, predate);
}

function onResetClick() {
	jQuery("#startDatePicker").val("");
	jQuery("#endDatePicker").val("");
	jQuery("#buildBundle_bundleName").val("");

	jQuery("#buildBundle_resultList").html("");
	jQuery("#buildBundle_exception").html("");
	jQuery("#buildBundle_fileList").html("");
	jQuery("#buildBundle_fileList").html("");

	jQuery("#buildBundle #downloadLogs").hide();
	jQuery("#buildBundle #buildBox #building").hide();
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

function bundleUrl() {
	var id = jQuery("#buildBundle_id").text();
	jQuery("#buildBundle_exception").hide();
	jQuery.ajax({
		url: "../../api/build/" + id + "/url?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var bundleResponse = response;
			if(bundleResponse.exception !=null) {
				jQuery("#buildBundle #buildBox #buildBundle_resultLink #resultLink")
				.text("(exception)")
				.css("padding-left", "5px")
				.css("font-size", "12px")
				.addClass("adminLabel")
				.css("color", "red");
			} else {
				jQuery("#buildBundle #buildBox #buildBundle_resultLink #resultLink")
				.text(bundleResponse.bundleResultLink)
				.css("padding-left", "5px")
				.css("font-size", "12px")
				.addClass("adminLabel")
				.css("color", "green");
			}
		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(bundleUrl, 10000);
		}
	});
	var url = jQuery("#buildBundle #buildBox #buildBundle_resultLink #resultLink").text();
	if (url == null || url == "") {
		window.setTimeout(bundleUrl, 5000);
	}
}
function buildBundle(bundleName, startDate, endDate, bundleComment, archive, consolidate, predate){
	var bundleDirectory = jQuery("#selected_bundleDirectory").text();
	var email = jQuery("#buildBundle_email").val();
	if (email == "") { email = "null"; }
	jQuery.ajax({
		url: "../../api/build/create?ts=" +new Date().getTime(),
		type: "POST",
		async: false,
		data: {
			bundleDirectory: bundleDirectory,
			bundleName: bundleName,
			email: email,
			bundleStartDate: startDate,
			bundleEndDate: endDate,
			archive: archive,
			consolidate: consolidate,
			predate: predate,
			bundleComment: bundleComment /*comment needs to be the last on the form*/
		},
		success: function(response) {
			var bundleResponse = response;
			if (bundleResponse != undefined) {
				//display exception message if there is any
				if(bundleResponse.exception !=null) {
					alert(bundleResponse.exception.message);
				} else {
					jQuery("#buildBundle_resultList").html("calling...");
					jQuery("#buildBundle_id").text(bundleResponse.id);
					window.setTimeout(updateBuildStatus, 5000);
					bundleUrl();
				}
			} else {
				jQuery("#buildBundle_id").text(error);
				jQuery("#buildBundle_resultList").html("error");
			}
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
}

function updateBuildStatus() {
	disableStageButton();
	id = jQuery("#buildBundle_id").text();
	jQuery.ajax({
		url: "../../api/build/" + id + "/list?ts=" +new Date().getTime(),
		type: "GET",
		async: false,
		success: function(response) {
			var txt = "<ul>";
			var bundleResponse = response;
			if (bundleResponse == null) {
				jQuery("#buildBundle_buildProgress").text("Bundle Status Unkown!");
				jQuery("#buildBundle #buildBox #building #buildingProgress").attr("src","../../css/img/dialog-warning-4.png");
				jQuery("#buildBundle_resultList").html("unknown id=" + id);
			}
			var size = bundleResponse.statusList.length;
			if (size > 0) {
				for (var i=0; i<size; i++) {
					txt = txt + "<li>" + bundleResponse.statusList[i] + "</li>";
				}
			}
			if (bundleResponse.complete == false) {
				window.setTimeout(updateBuildStatus, 5000); // recurse
			} else {
				jQuery("#buildBundle_buildProgress").text("Bundle Complete!");
				jQuery("#buildBundle #buildBox #building #buildingProgress").attr("src","../../css/img/dialog-accept-2.png");
				updateBuildList(id);
				enableStageButton();
				enableBuildButton();
				enableResetButton();
			}
			txt = txt + "</ul>";
			jQuery("#buildBundle_resultList").html(txt).css("font-size", "12px");	
			// check for exception
			if (bundleResponse.exception != null) {
				jQuery("#buildBundle_buildProgress").text("Bundle Failed!");
				jQuery("#buildBundle #buildBox #building #buildingProgress").attr("src","../../css/img/dialog-warning-4.png");
				if (bundleResponse.exception.message != undefined) {
					jQuery("#buildBundle_exception").show().css("display","inline");
					jQuery("#buildBundle_exception").html(bundleResponse.exception.message);
				}
				disableStageButton();
				enableBuildButton();
				enableResetButton();
			}
		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(updateBuildStatus, 10000);
		}
	});
}

//populate list of files that were result of building
function updateBuildList(id) {
	var summaryList = null;
	jQuery.ajax({
		url: "manage-bundles!downloadOutputFile.action?ts=" +new Date().getTime(),
		type: "GET",
		data: {"id": id,
			"downloadFilename": "summary.csv"},
			async: false,
			success: function(response){
				summaryList = response;
			}
	});

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
						if (description != undefined) {
							var encoded = encodeURIComponent(list[i]);
							txt = txt + "<li>" + description + ":" + "&nbsp;"
							+ lineCount + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							+ "<img src=\"../../css/img/go-down-5.png\" />"
							+ "<a href=\"manage-bundles!downloadOutputFile.action?id="
							+ id+ "&downloadFilename=" 
							+ encoded + "\">" + ".csv" +  "</a></li>";
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
			jQuery("#buildBundle_fileList").html(txt).css("display", "block");
			jQuery("#buildBundle #downloadLogs").show().css("display", "block");
			jQuery("#buildBundle #downloadLogs #downloadButton").attr("href", "manage-bundles!buildOutputZip.action?id=" + id);
			var continueButton = jQuery("#build_continue");
			enableContinueButton(continueButton);
		},
		error: function(request) {
			clearTimeout(timeout);
			timeout = setTimeout(function() {
				updateBuildList(id);
			}, 10000);
		}
	});	
}

function onStageClick() {
	stageBundle();
}

function stageBundle() {
	var environment = jQuery("#deploy_environment").text();
	var bundleDir = jQuery("#createDirectory #directoryName").val();
	var bundleName = jQuery("#buildBundle_bundleName").val();
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
				//console.log("url: " + url);
				if (url.toLowerCase().startsWith("http")
						|| url.toLowerCase().startsWith("ftp")) {
					//console.log("set url");
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
	jQuery(agencyMetadata).each(function() {
		agencyDropDown.append(jQuery("<option>").attr('value',this.legacyId).text(this.shortName));
	});
	agencyDropDown.insertBefore("#agencyId");
}