/*
 * Copyright (C) 2016 Cambridge Systematics, Inc. 
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

    //if we have an API key, show the edit section
    //otherwise, hide the edit section
    var key = jQuery("#key").val();
    if(!!key) {
        showEditApiKey();
    }
    else {
        $('#editApiKey').hide();
    }

	jQuery('#requestIntervalError').hide();

	//If the Minimum API Request Interval is not a number, disable the "Save"
	//button. Using bind() with propertychange event as live() does not work
	//in IE for unknown reasons
	jQuery("#minApiReqInt").bind("input propertychange", function() {
		var text = jQuery("#minApiReqInt").val();
		var validDatasetNameExp = /^\d*$/;
		if ((text.length == 0)
				|| ((text.length > 0) && (!text.match(validDatasetNameExp)))) {
			jQuery('#requestIntervalError').show();
			$("#api-key_save").prop('disabled', true);
		} else {
			$('#requestIntervalError').hide();
			$("#api-key_save").prop('disabled', false);
		}
	});

    jQuery("#newKey").click(function(e) {
        e.preventDefault();
        clearApiKeyFields();
        handleGenerateApiKeyClick();
        hideDelete();
        hideSearch();
        hideResult();
    });

});

function handleGenerateApiKeyClick() {
	jQuery.ajax({
		url: "../../api/config/generate-api-key",
		type: "GET",
		success: function(response) {
			jQuery("#key").val(response);
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
    showEditApiKey();
    hideResult();
}

function hideResult() {
    if(jQuery("#apiKeyResult").is(":visible")) {
        $("#apiKeyResult").hide();
    }
}

function hideDelete() {
    if(jQuery("#deleteKey").is(":visible")) {
        $("#deleteKey").hide();
    }
}

function hideSearch() {
    if(jQuery("#searchKey").is(":visible")) {
        $("#searchKey").hide();
    }
}

function showEditApiKey() {
    $('#editApiKey').show();
}

function clearApiKeyFields() {
	$("#minApiReqInt").val("100");
	$("#contactName").val("");
	$("#contactCompany").val("");
	$("#contactEmail").val("");
	$("#contactDetails").val("");
	$("#key").val("");
	$('#requestIntervalError').hide();
	$("#api-key_save").prop('disabled', false);
    $('#searchKey').show();
    $('#deleteKey').show();
    $("#apiKeyResult").hide();
}
