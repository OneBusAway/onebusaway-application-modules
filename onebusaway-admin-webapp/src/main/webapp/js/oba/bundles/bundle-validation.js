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

jQuery(function() {
	jQuery("#csvFile").click(onCsvFileClick);

	jQuery("#csvOptions input:radio").click(onCsvOptionsClick);

	jQuery("#environmentOptions input:radio").click(onEnvironmentOptionsClick);

	jQuery("#validateBundleButton").click(onValidateBundleButtonClick);

	$("#csvOptions #csvFile").hide();
});

function onCsvFileClick() {
	$("#bundleValidationResults").find("tr:gt(0)").remove();
	$("#bundleValidationResults").hide();
}

function onCsvOptionsClick() {
	$("#bundleValidationResults").find("tr:gt(0)").remove();
	$("#bundleValidationResults").hide();
	if ($('input[name=inputFile]:checked').val()=="wiki") {
		$("#csvFile").hide();
		$("#csvWikiUrl").show();
	} else {
		$("#csvFile").show();
		$("#csvWikiUrl").hide();
	}
}

function onEnvironmentOptionsClick() {
	$("#bundleValidationResults").find("tr:gt(0)").remove();
	$("#bundleValidationResults").hide();
}

function onValidateBundleButtonClick() {
	$("#processing").show();
	var wikiUrl = "";
	var csvFile = "";
	var csvDataFile = "";
	if ($('input[name=inputFile]:checked').val()=="wiki") {
		wikiUrl = $("#csvWikiUrl").val();
	} else {
		csvFile = $("#csvFile").val();
		if (csvFile.length > 0) {
			csvDataFile = document.getElementById('csvFile').files[0];
		}
	}
	var checkEnvironment = jQuery("input[name=environmentOptions]:checked").val();
	var checkEnvironment = jQuery("input[name=environmentOptions]:checked").val();
	var formData = new FormData();
	formData.append("ts", new Date().getTime());
	formData.append("wikiUrl", wikiUrl);
	formData.append("csvFile", csvFile);
	formData.append("checkEnvironment", checkEnvironment);
	formData.append("csvDataFile", csvDataFile);

	jQuery.ajax({
		url: "validate-bundle!runValidateBundle.action",
		type: "POST",
		data: formData,
		cache: false,
		processData: false,
		contentType: false, 
		async: false,
		success: function(data) {
			$("#processing").hide();
			$('#bundleValidationResults').show();
			$.each(data, function(index, value) {
				var testClass = '';
				if (value.testStatus === 'Pass') {
				    testClass = 'testPass';
				} else {
				    testClass = 'testFail';
				}
				var new_row = '<tr> \
					<td>' + value.linenum + '</td> \
					<td>' + value.csvLinenum + '</td> \
					<td class=' + testClass + '>' + value.testStatus + '</td> \
					<td>' + value.specificTest + '</td> \
					<td>' + value.testResult + '</td> \
					<td><a href="' + value.testQuery + '">' + value.testQuery + '</a></td> \
					</tr>';
				$('#bundleValidationResults').append(new_row);
			});
		},
		error: function(request) {
			$("#processing").hide();
			console.log("Error calling Validate");
		}
	});
}
