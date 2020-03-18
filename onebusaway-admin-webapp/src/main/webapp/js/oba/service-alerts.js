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
	// add another condition for the service alert
	jQuery("#addAnotherCondition").click(onAddAnotherCondition);

	// delete the condition for this service alert
	jQuery(".deleteCondition").click(onDeleteCondition);
	
	// hook up service alert links
    for (var i = 0; i < 1000; i++) {
    	var selector = "#tweetAlertLink" + i;

    	if (jQuery(selector).length ) {
            jQuery(selector).click(onTweetCondition);
        } else {
    		// first element not found signifies break
    		break;
		}
    }

	for (var i = 0; i < 1000; i++) {
		var selector = "#validateCondition" + i;

		if (jQuery(selector).length ) {
			jQuery(selector).click(onValidateCondition);
		} else {
			// first element not found signifies break
			break;
		}
	}


	// show service alerts template list
	jQuery("#loadTemplate").click(showHideLoadTemplate);

	// Check  if this is the "Edit Service Alert" page
	if ($("#service-alert_submit").length > 0) {
		// Only enable the Save button if an "owning agency" is selected.
		if ($("#service-alert_agencyId option:selected").val() == "null") {
			$("#service-alert_submit").attr("disabled", "disabled");
		}
		$("#service-alert_agencyId").change(function() {
			if ($("#service-alert_agencyId option:selected").val() == "null") {
				$("#service-alert_submit").attr("disabled", "disabled");
			} else {
				$("#service-alert_submit").removeAttr("disabled");
			}
		});
	}
	
	jQuery("#publicationWindowStartDate").datepicker({ 
		dateFormat: "yy-mm-dd",
		onSelect: function(selectedDate) {
			jQuery("#publicationWindowEndDate").datepicker("option", "minDate", selectedDate);
		}
	});
	
	jQuery("#publicationWindowEndDate").datepicker({ 
		dateFormat: "yy-mm-dd",
		onSelect: function(selectedDate) {
			jQuery("#publicationWindowStartDate").datepicker("option", "maxDate", selectedDate);
		}
	});
	
	jQuery("#loadTemplateInput [name='template']").click(function(){
		var selectedTemplateId = jQuery("#loadTemplateInput [name='template']").val();
		loadTemplate(selectedTemplateId);
	})
	
	jQuery("#loadTemplate").click(function(){
		jQuery("#loadTemplateInput").toggle();
	});
	
});


function getAgencySelectOptions() {
	var optionsList = "";
    var x = document.getElementById("service-alert_agencyId");
    var i;
    for (i = 1; i < x.length; i++) {
        optionsList += '<option value="' + x.options[i].value + '">' 
        	+ x.options[i].text + '</option>';
    }
	return optionsList;
}

function onAddAnotherCondition() {
	var currentConditionsCt = jQuery('#conditionTable').children().children('tr').length;
	var agencySelectOptions = getAgencySelectOptions();
	var newConditionTable = '<table class="affectsClauseConditions"> \
		<tr> \
		    <td class="tdLabel"><label for="service-alert_allAffects_agencyId" class="label">Agency:</label> \
			</td> \
			<td> \
			<select class="alertCondition" name="allAffects['
				+ currentConditionsCt
				+ '].agencyId" id="service-alert_allAffects_agencyId"> \
		    	<option value="null">Select agency affected</option>'
				+ agencySelectOptions
			+ '</select> \
			</td> \
		</tr> \
		<tr> \
	    	<td class="tdLabel"><label for="service-alert_allAffects_' + currentConditionsCt + '__routeId" class="label">Route:</label></td> \
	    	<td><input class="alertCondition" name="allAffects['
			+ currentConditionsCt
			+ '].routeId" value="" id="service-alert_allAffects_' + currentConditionsCt + '__routeId" type="text"></td> \
		</tr> \
		<tr><td style="text-align:center" colspan="2" id="routeValidation' + currentConditionsCt + '">Click Validate to lookup Route</td></tr> \
		<tr> \
	    	<td class="tdLabel"><label for="service-alert_allAffects_' + currentConditionsCt + '__stopId" class="label">Stop:</label></td> \
	    	<td><input class="alertCondition" name="allAffects['
			+ currentConditionsCt
			+ '].stopId" value="" id="service-alert_allAffects_' + currentConditionsCt + '__stopId" type="text"></td> \
		</tr> \
		<tr><td style="text-align:center" colspan="2" id="stopValidation' + currentConditionsCt + '">Click Validate to lookup Stop</td></tr> \
		</table>';
	var labelLetter = String.fromCharCode('A'.charCodeAt(0) + currentConditionsCt);
	if (currentConditionsCt >= 26) {
		labelLetter = String.fromCharCode('A'.charCodeAt(0) + (currentConditionsCt%26)) + String.fromCharCode('A'.charCodeAt(0) + (currentConditionsCt%26));
	}
	var newRow = '<tr class="affectsClause"> \
		<td> \
			<div class="conditionClauseLabel"> \
				<div class="conditionClauseLabelFirstLine">Condition</div> \
				<div class="conditionClauseLabelLetter">'
					+ labelLetter
				+ '</div> \
			</div> \
		</td> \
		<td>' + newConditionTable + '</td> \
		<td id="validateCondition'+ currentConditionsCt + '" class="validateCondition">Validate</td>\
		<td class="deleteCondition">Delete Condition</td> \
		</tr>';

	$('#conditionTable').append(newRow);
	$("#conditionTable td:last.deleteCondition").click(onDeleteCondition);
	$("#conditionTable td:last.validateCondition").click(onValidateCondition);
}
function onDeleteCondition() {
	$(this).closest('tr').remove();
	// Reset the name attributes for the remaining conditions to match their 
	// new positions in the list.
	$('#conditionTable tr.affectsClause').each(function(index) {
		var labelLetter = String.fromCharCode('A'.charCodeAt(0) + index);
		if (index >= 26) {
			labelLetter = String.fromCharCode('A'.charCodeAt(0) + Math.floor(index/26)) + String.fromCharCode('A'.charCodeAt(0) + (index%26));
		}

		$(this).find('.conditionClauseLabelLetter').text(labelLetter);
		$(this).find('.alertCondition').each(function() {
			var nameAttr = $(this).attr('name');
			var idx1 = nameAttr.indexOf('[');
			var idx2 = nameAttr.indexOf(']');
			nameAttr = nameAttr.substring(0,idx1+1) + index + nameAttr.substring(idx2);
			$(this).attr('name', nameAttr);
		});
	});
}

function showHideLoadTemplate(){
	
}

function loadTemplate(id) {
	if(id != "null"){
		var urlArray = window.location.href.split('?');
		var url = urlArray[0] + "?";
		if(url.length > 2 && urlArray[1].includes("newServiceAlert=true")){
			url += 'newServiceAlert=true&';
		}	
		url += 'alertId=' + id + '&';
		url += 'fromFavorite=true'
			
		window.location.href = url;
	}
}
function onTweetCondition(handler) {
    var aId = handler.target.id;
    var id = aId.replace("tweetA", "");
    var divSelector = "#tweetDiv" + id;
    var alertDiv = jQuery(divSelector);
    var alertId = alertDiv.html();
    sendAndShowTweet(alertId);
}
function onValidateCondition(handler) {
	var aId = handler.target.id;
	var id = aId.replace("validateCondition", "");
	var selector = "service-alert_allAffects_"+ id;
	var stopField = document.getElementById(selector + "__stopId");
	if (stopField == null) {
		return;
	}
	var stopId = stopField.value;
	var routeField = document.getElementById(selector + "__routeId");
	var routeId = routeField.value;
	if (stopId != null && stopId != "") {
		var url = OBA.Config.apiBaseUrl + "/where/stop/" + stopId + ".json?key=" + OBA.Config.obaApiKey;
		jQuery.ajax({
			url: url,
			data: {},
			type: "GET",
			async: true,
			success: function (data) {
				if (data && data.code && data.code == 200) {
					document.getElementById("stopValidation" + id).innerText = data.data.entry.name

				} else {
					document.getElementById("stopValidation" + id).innerText = "Stop not found";
				}
			}
		})
	}
	if (routeId != null && routeId != "") {

		var url = OBA.Config.apiBaseUrl + "/where/route/" + routeId + ".json?key=" + OBA.Config.obaApiKey;
		jQuery.ajax({
			url: url,
			data: {},
			type: "GET",
			async: true,
			success: function (data) {
				if (data && data.code && data.code == 200) {
					document.getElementById("routeValidation" + id).innerText = data.data.entry.shortName

				} else {
					document.getElementById("routeValidation" + id).innerText = "Route not found";
				}
			},
			error: function (data) {
				document.getElementById("routeValidation" + id).innerText = "Route not found";
			}
		})
	}
}
function sendAndShowTweet(alertId) {
    jQuery.ajax({
        url: "service-alert-edit!tweetAlert.action",
        data: {
            "alertId": alertId
        },
        type: "GET",
        async: false,
        success: function (data) {
            jQuery("#dialog").show();
            jQuery("#dialog").html(data);
            jQuery("#dialog").dialog();
        },
        error: function (data) {
            jQuery("#dialog").show();
            jQuery("#dialog").html(data);
            jQuery("#dialog").dialog();
        }
    })
}