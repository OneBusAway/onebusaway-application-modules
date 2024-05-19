function initUpload() {
    // toggle agency row as selected when checkbox is clicked
    jQuery("#agency_data").on("change", "tr :checkbox", onSelectAgencyChange);

    // change input type to 'file' if protocol changes to 'file'
    jQuery("#agency_data").on("change", "tr .agencyProtocol", onAgencyProtocolChange);

    // upload bundle source data for selected agency
    jQuery("#uploadButton").click(onUploadSelectedAgenciesClick);

    // add another row to the list of agencies and their source data
    jQuery("#addAnotherAgency").click(onAddAnotherAgencyClick);

    jQuery("#addNewAgency").click(onAddNewAgencyClick);

    // remove selected agencies
    jQuery("#uploadFiles #agency_data").on('click', '.removeAgency', onRemoveSelectedAgenciesClick);

    // popup the Comments box
    jQuery("#anyNotes").click(onAnyCommentsClick);

    // if bundle comment has changed, save it to info.json
    jQuery("#Upload #bundleComment").change(onBundleCommentChanged);

    //Retrieve transit agency metadata
    getAgencyMetadata();

    // Retrieve dataset name and build name for bundle currently deployed on staging.
    //getDeployedOnStagingBundleInfo();

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


}

function onUploadContinueClick() {
    var $tabs = jQuery("#tabs");
    $tabs.tabs('select', 2);
}

function onAnyCommentsClick() {
    $("#addCommentsPopup").dialog("open");
}

function onRemoveSelectedAgenciesClick() {
    $(this).closest('tr').remove();
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

function onUploadSelectedAgenciesClickAsync() {
    var bundleDir = selectedDirectory;
    // TOOD -- in progress
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
                    console.log("Error uploading " + agencyDataFile.name);
                    alert("There was an error processing your request. Please try again.");
                }
            });
        }
    });
    onUploadContinueClick();
}
