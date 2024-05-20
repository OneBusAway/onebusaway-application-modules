/*
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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

function initCompare() {

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

}

function onPrintFixedRouteRptClick() {
    window.print();
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

