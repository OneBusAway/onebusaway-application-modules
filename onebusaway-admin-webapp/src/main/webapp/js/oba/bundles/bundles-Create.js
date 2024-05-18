function initCreate() {
    jQuery('#Create #filenameError').hide();
    // hookup ajax call to select
    jQuery("#newDirectoryButton").click(onCreateDatasetClick);

}

function onExistingDatasetClick() {
    var selectedCheckbox = jQuery("#Create #existingDataset").find("input:checked");
    selectedDirectory = selectedCheckbox.closest("tr").find(".directoryName").text();
    $("#Download #download_selectedDataset").text(selectedDirectory);
    onSelectDataset("existing");
    updateFixedRouteParams(selectedDirectory);
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

function onCreateDatasetClick() {
    selectedDirectory = jQuery("#createDataset #directoryName").val();
    $("#Download #download_selectedDataset").text(selectedDirectory);
    // Clear fields on the Upload tab
    if (agencyMetadataAvailable) {
        $('#agency_data tr').slice(1).remove();
        //onAddAnotherAgencyClick("file");
        jQuery("#addAnotherAgency").click();

    }
    $("#uploadFiles #bundleComment").val("");
    $('#existingFilesTable tr').slice(1).remove();
    onSelectDataset("create");
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

function onDeleteDeployConfirmed() {
    onDeleteDeployedItemClick();
    // refresh automatically
    deployListBundles();
}

function onDeleteExistingDatasetClick() {
    selectedDirectory = $(this).closest("tr").find(".directoryName").text();
    var continueDelete = $("#deletePopup").dialog("open");
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


