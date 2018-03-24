/*
 * Copyright (c) 2016 Cambridge Systematics, Inc.
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
// For Fixed Route Comparison Report
var currentReportDataset = "";
var currentReportBuildName = "";
var currentArchivedReportDataset = "";
var currentArchivedReportBuildName = "";
var compareToDataset =  "";
var compareToBuildName =  "";
var compareToArchivedDataset =  "";
var compareToArchivedBuildName =  "";

jQuery(function() {
	// Set values for dataset select lists
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
	$('#buildingReportDiv').hide();
	$("#currentArchivedDatasetList").hide();
	$("#currentArchivedBuildNameList").hide();
	$("#compareToArchivedDatasetList").hide();
	$("#compareToArchivedBuildNameList").hide();

	$("#currentDatasetList").on("change", onCurrentDatasetChange);
	$("#currentBuildNameList").on("change", onCurrentBuildNameChange);

	$("#currentArchivedDatasetList").on("change", onCurrentArchivedDatasetChange);
	$("#currentArchivedBuildNameList").on("change", onCurrentArchivedBuildNameChange);

	$("#compareToDatasetList").on("change", onCompareToDatasetChange);
	$("#compareToBuildNameList").on("change", onCompareToBuildNameChange);

	$("#compareToArchivedDatasetList").on("change", onCompareToArchivedDatasetChange);
	$("#compareToArchivedBuildNameList").on("change", onCompareToArchivedBuildNameChange);

	$("#printFixedRouteRptButton").click(onPrintFixedRouteRptClick);

    $('#useArchiveCheckbox').change(function() {
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

});

/**
 * Functions used for generating reports on differences between two bundle builds.
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

function resetCurrentReportDataset() {
	if (!$("#useArchiveCheckbox").is(":checked")) {
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
	if (!$("#useArchiveCheckbox").is(":checked")) {
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

function buildDiffReport() {
	// Clear any previous reports
	$("#diffResultsTable tbody").empty();
	$('#fixedRouteDiffTable tbody').empty();
	var useArchived = $("#useArchiveCheckbox").is(":checked");
	if (!useArchived) {
		var dataset_1 = currentReportDataset;
		var dataset_1_build_id = 0;
		var dataset_2 = compareToDataset;
		var dataset_2_build_id = 0;
		var buildName_1 = currentReportBuildName;
		var buildName_2 = compareToBuildName;
	} else {
		$('#buildingReportDiv').show();
		var dataset_1 = currentArchivedReportDataset;
		var dataset_1_build_id = $('#currentArchivedBuildNameList option:selected').val();
		var dataset_2 = compareToArchivedDataset;
		var dataset_2_build_id = $('#compareToArchivedBuildNameList option:selected').val();
		var buildName_1 = currentArchivedReportBuildName;
		var buildName_2 = compareToArchivedBuildName;		
	}
	$.ajax({
		url: "../bundles/compare-bundles!diffResult.action",
		data: {
			"useArchived" : useArchived,
			"datasetName" : dataset_1,
			"dataset_1_build_id" : dataset_1_build_id,
			"buildName" : buildName_1,
			"datasetName2" : dataset_2,
			"dataset_2_build_id" : dataset_2_build_id,
			"buildName2": buildName_2
		},
		type: "GET",
		async: false,
		success: function(data) {
			$('#buildingReportDiv').hide();
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
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	})
}

function getExistingBuildList(datasetName) {
	var buildNameList;
	var useArchivedGtfs = $("#useArchiveCheckbox").is(":checked");
	if (datasetName) {
		$.ajax({
			url: "bundle-reports!existingBuildList.action",
			data: {
				"selectedBundleName" : datasetName,
				"useArchivedGtfs" : useArchivedGtfs
			},
			type: "GET",
			async: false,
			success: function(data) {
				buildNameList=data;
			}
		})
	}
	return buildNameList;
}

function onPrintFixedRouteRptClick() {
	window.print();
}


