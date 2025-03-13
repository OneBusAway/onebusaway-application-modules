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
	//Initialize the accordion control
	$("#accordion").accordion({
		autoHeight: false
	});
	
	//Allow only positive numbers for text boxes with numeric units
	$(".positiveInteger").numeric(
			{decimal:false, negative:false}, 
			function(){ 
				/* no-op */
			});

	//Render the dynamic content
	getAgencyMap();


	//Handle reset and save click events
	$("#results #reset").on("click", resetToPrevious);
	$("#results #save").on("click", saveParameters);
	
	window.onbeforeunload = confirmMessage;

	var field = jQuery("#csrfField");
	var csrf_token = field.val();
	var csrf_name = field.attr('name');
	if (csrf_name && csrf_token) {
		jQuery.ajaxPrefilter(function (options, originalOptions, jqXHR) {
			if (options.type.toLowerCase() === "post") {
				// initialize `data` to empty string if it does not exist
				options.data = options.data || "";
				// add leading ampersand if `data` is non-empty
				options.data += options.data ? "&" : "";
				// add _token entry
				options.data += csrf_name + '=' + csrf_token;
			}
		});
	}
	
});

function confirmMessage() {
	var changedElements = $("input.changed[type='text']");
	if(changedElements.length > 0) {
		return "You have unsaved changes. Are you sure you want to leave this page?";
	}
	return null;
}

function listenForChanges() {
	//Listen to change event and mark inputs as changed
	$("input").on("keyup propertychange paste", function() {
		$(this).addClass("changed");
		$("#results input[type='button']").prop("disabled", false).css("color", "#595454");
	});

}
function getAgencyMap() {
	$.ajax({
		url: "parameters!getAgencyIdMap.action?ts=" + new Date().getTime(),
		type: "GET",
		dataType: "json",
		success: function(response) {
			updateAccordion(response);
		},
		error: function(request) {
			alert("Error loading agency map from the server : "+ request.responseText);
			console.log(request);
		}

	});

}

function getConfigParameters() {
	$.ajax({
		url: "parameters!getParameters.action?ts=" + new Date().getTime(),
		type: "GET",
		dataType: "json",
		success: function(response) {
			updateParametersView(response.configParameters);
		},
		error: function(request) {
			alert("Error loading parameters from the server : "+ request.responseText);
			console.log(request);
		}
		
	});

	// now add listeners ONLY after content was added
	listenForChanges();
}

function updateAccordion(data) {
	// dynamically build up content based on parameters
	// and agencyId APIs
	var rootDiv = document.getElementById("dynamicAgencies");

	var i = 0;
	$.each(data["configParameters"], function(key, val) {
		var headerLabel = document.createElement('label');
		headerLabel.setAttribute('class', 'propertyHeader');
		headerLabel.innerHTML = 'Exclude Scheduled Data: ' + val;
		var descriptionDiv = document.createElement('div');
		descriptionDiv.setAttribute('class', 'propertyDescription');
		descriptionDiv.innerHTML = '<p>Enter true if you want to hide scheduled buses, and only show realtime buses.</p>';

		var inputDiv = document.createElement('div');
		var hiddenInput = document.createElement('input');
		hiddenInput.setAttribute('type', 'hidden');
		hiddenInput.setAttribute('id', key+'_'+'hideScheduleInfo');
		hiddenInput.setAttribute('value', key+'_'+'hideScheduleInfo');
		hiddenInput.innerHTML = '';

		var textInput = document.createElement('input');
		textInput.setAttribute('type', 'text');
		textInput.setAttribute('id', key+'_'+'hideScheduleInfo');
		textInput.setAttribute('value', key+'_'+'hideScheduleInfo');

		var unitLabel = document.createElement('label');
		unitLabel.setAttribute('id', 'propertyUnit');
		unitLabel.innerHTML = '&nbsp;true/false';
		inputDiv.appendChild(hiddenInput);
		inputDiv.appendChild(textInput);
		inputDiv.appendChild(unitLabel);

		var breakElement = document.createElement('br');

		var agencyDiv = document.createElement('div')
		agencyDiv.setAttribute('id', val);
		if (i % 2 == 0) {
			agencyDiv.setAttribute('class', 'propertyHolder');
		} else {
			// odd row to right
			agencyDiv.setAttribute('class', 'propertyHolder rightProperty');
		}

		agencyDiv.appendChild(headerLabel);
		agencyDiv.appendChild(descriptionDiv);
		agencyDiv.appendChild(inputDiv);
		rootDiv.appendChild(agencyDiv)
		rootDiv.appendChild(breakElement);
		i++;
	})

	// now Load config parameters from the server
	getConfigParameters();

}
function updateParametersView(configParameters) {
	var panels = $("#accordion").children("li");
	//Update view by looping through sections in each accordion panel
	for(var i=0; i<panels.length; i++) {
		var sections = $(panels[i]).children("div").children("div");
		for(var j=0; j<sections.length; j++) {
			if($(sections[j]).children(".propertyHolder").length > 0) {
				//We have multiple sections per panel here. Process each one of them
				var properties = $(sections[j]).children(".propertyHolder");
				for(var k=0; k<properties.length; k++) {
					var keyElement = $(properties[k]).find("input[type='hidden']");
					var configKey = keyElement.val();
					$(keyElement).next().val(configParameters[configKey]);
				}
			} else {
				//Sections are properties in this case. Set the values to the next sibling 
				//of the hidden child
				var keyElement = $(sections[j]).find("input[type='hidden']");
				var configKey = keyElement.val();
				$(keyElement).next().val(configParameters[configKey]);
			}
		}
	}
}

function resetToPrevious() {
	clearChanges();
	
	//Reset parameter values to last saved values on server
	getConfigParameters();
}

function saveParameters() {
	var data = buildData();
	
	if(data != null) {
		$.ajax({
			url: "parameters!saveParameters.action",
			type: "POST",
			dataType: "json",
			data: {"params": data},
			traditional: true,
			success: function(response) {
				if(response.saveSuccess) {
					$("#results #messageBox #message").text("Your changes have been saved. " +getTime());
					$("#results #messageBox").show();
					clearChanges();
					$("#results #messageBox").delay(10000).fadeOut(5000);
				} else {
					alert("Failed to save parameters. Please try again.");
				}
			},
			error: function(request) {
				//alert("Error saving parameter values");
				console.log("error. Request = ");
				console.log(request);
			}
		});
	} else {
		alert("Cannot save parameters. One or more values is blank");
	}
}

function buildData() {
	var data = new Array();
	var invalid = false;
	var changedElements = $("input.changed[type='text']");
	if (changedElements.length == 0) {
		console.log("no changed elements detected!");
	}
	for(var i=0; i<changedElements.length; i++) {
		var changedElement = $(changedElements[i]);
		if(changedElement.val() == "" || changedElement.val() == null) {
			//Cannot save blank values
			invalid = true;
			break;
		} else {
			var changedValue = changedElement.val();
			var key = $(changedElements[i]).prev("input[type='hidden']").val();
			data[i] = key + ":" +changedValue;
		}
	}
	if(invalid) {
		data = null;
	}
	return data;
}

function getTime() {
	var lastUpdateTime = new Date();
	var hours = lastUpdateTime.getHours();
	var meridian;
	if(hours > 12) {
		hours = hours - 12;
		meridian = "PM";
	} else {
		if(hours == 12) {
			meridian = "PM";
		} else {
			meridian = "AM";
		}
	}
	var minutes = lastUpdateTime.getMinutes();
	if(minutes < 10) {
		minutes = "0" + minutes;
	}
	var seconds = lastUpdateTime.getSeconds();
	if(seconds < 10) {
		seconds = "0" + seconds;
	}
	var time = hours + ":" +  minutes + ":" + seconds + " " +meridian;
	
	return time;
}

function clearChanges() {
	$("input[type='text']").removeClass("changed");
	$("#results input[type='button']").attr("disabled", "disabled").css("color", "#999999");
}



