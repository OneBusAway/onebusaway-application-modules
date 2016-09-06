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
	    	<td class="tdLabel"><label for="service-alert_allAffects_routeId" class="label">Route:</label></td> \
	    	<td><input class="alertCondition" name="allAffects['
			+ currentConditionsCt
			+ '].routeId" value="" id="service-alert_routeId" type="text"></td> \
		</tr> \
		<tr> \
	    	<td class="tdLabel"><label for="service-alert__allAffectsstopId" class="label">Stop:</label></td> \
	    	<td><input class="alertCondition" name="allAffects['
			+ currentConditionsCt
			+ '].stopId" value="" id="service-alert_stopId" type="text"></td> \
		</tr> \
		</table>';
	
	var newRow = '<tr class="affectsClause"> \
		<td> \
			<div class="conditionClauseLabel"> \
				<div class="conditionClauseLabelFirstLine">Condition</div> \
				<div class="conditionClauseLabelLetter">'
					+ String.fromCharCode('A'.charCodeAt(0) + currentConditionsCt)
				+ '</div> \
			</div> \
		</td> \
		<td>' + newConditionTable + '</td> \
		<td class="deleteCondition">Delete Condition</td> \
		</tr>';

	$('#conditionTable').append(newRow);
	$("#conditionTable td:last.deleteCondition").click(onDeleteCondition);
}
function onDeleteCondition() {
	$(this).closest('tr').remove();
	// Reset the name attributes for the remaining conditions to match their 
	// new positions in the list.
	$('#conditionTable tr.affectsClause').each(function(index) {
		$(this).find('.conditionClauseLabelLetter').text(String.fromCharCode('A'.charCodeAt(0) + index));
		$(this).find('.alertCondition').each(function() {
			var nameAttr = $(this).attr('name');
			var idx1 = nameAttr.indexOf('[');
			var idx2 = nameAttr.indexOf(']');
			nameAttr = nameAttr.substring(0,idx1+1) + index + nameAttr.substring(idx2);
			$(this).attr('name', nameAttr);
		});
	});
}
