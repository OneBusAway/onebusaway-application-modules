function init() {

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
    var url = $resultLink.text();
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
