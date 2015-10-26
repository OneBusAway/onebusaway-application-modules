/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
jQuery(function() {
	
	//Handle autocomplete 
	jQuery("#searchUser").autocomplete({
		//Set up autocomplete source
		source: function(req, add) {
			//Get the search string from request
			var searchString = req.term;
			jQuery.ajax({
				url: "../../api/users?search=" +searchString,
				type: "GET",
				async: false,
				success: function(response) {
					//add the result to callback for display
					add(response);	
				},
				error: function(request) {
					alert("There was an error processing your request. Please try again.");
				}
			});
		},
		select: function(event, ui) {
			var selected = ui.item.value;
			jQuery.ajax({
				url: "../../api/users/getUserDetails",
				data: {"user" : selected},
				type: "GET",
				async: false,
				success: function(response) {
					jQuery("#userDetails #userId").val(response.id);
					jQuery("#userDetails #userName").text(response.userName);
					var userRole = response.role;
					switch(userRole) {
						case "ROLE_USER":
							jQuery("#userDetails #userRole").text("OPERATOR");
						break;
						
						case "ROLE_ADMINISTRATOR":
							jQuery("#userDetails #userRole").text("ADMINISTRATOR");
						break;
						
						default:
							jQuery("#userDetails #userRole").text("ANONYMOUS");
						break;
					}
					jQuery("#userDetails").show();
					
					jQuery("#editUser #editUserName").text(response.userName);
					jQuery("#editUser #newPassword").val("");
					jQuery("#editUser #newRole").val(userRole).attr("selected", true);
					
					hideResult();
				},
				error: function(request) {
					alert("There was an error processing your request. Please try again.");
				}
			});
		}
		
	});
	
	//Edit image click
	jQuery("#actions #edit").on("click", showEditUser);
	
	//Delete image click
	jQuery("#actions #deactivate").on("click", showDeleteDialog);
	
});

function showEditUser() {
	jQuery("#editUser").show();
	jQuery("#editUser #editSubmit").click(editUser);
	hideResult();
}

function editUser() {
	
	var userData = new Object();
	userData.id = jQuery("#userDetails #userId").val();
	userData.userName = jQuery("#editUser #editUserName").text();
	userData.password = jQuery("#editUser #newPassword").val();
	userData.role = jQuery("#editUser #newRole option:selected").val();
	
	jQuery.ajax({
		url:"manage-users!editUser.action",
		type: "POST",
		dataType: "json",
		data : {"userData":JSON.stringify(userData)},
		traditional: true,
		success: function(response) {
			jQuery("#userResult #result").text(response);
			showResult();
		},
		error: function() {
			alert("Error updating user");
		}
	});
}

function showDeleteDialog() {
	if(jQuery("#editUser").is(":visible")) {
		jQuery("#editUser").hide();
	}
	turnOffEditClick();
	hideResult();
	
	var userName = jQuery("#userDetails #userName").text();
	var deleteDialog = jQuery("<div id='deleteConfirm'>" +
			"<div><label>Are you sure you want to deactivate user: </label>" +
			"<label id='deleteMessage'>" +userName + "</label></div>" +
			"<div id='deleteButtons'><button id='deleteSubmit'>OK</button>" +
			"<label id='deleteCancel'>Cancel</label></div></div>")
			.dialog({
				title: "Deactivate User?",
				autoOpen: false,
				height: "auto",
				width: "auto",
				position: [500,125]	
			});
	
	deleteDialog.dialog('open');
	
	var confirmButton = deleteDialog.find("#deleteSubmit");
	confirmButton.on("click", {"dialog": deleteDialog}, deactivateUser);
	
	var cancelButton = deleteDialog.find("#deleteCancel");
	cancelButton.on("click", function(){
		deleteDialog.dialog('close');
		turnOnEditClick();
	});
}

function deactivateUser(event) {
	var deleteDialog = event.data.dialog;
	
	var userData = new Object();
	userData.id = jQuery("#userDetails #userId").val();
	userData.userName = jQuery("#userDetails #userName").text();
	
	jQuery.ajax({
		url:"manage-users!deactivateUser.action",
		type: "POST",
		dataType: "json",
		data : {"userData":JSON.stringify(userData)},
		traditional: true,
		success: function(response) {
			jQuery("#userResult #result").text(response);
			showResult();
			deleteDialog.dialog('close');
			jQuery("#search #searchUser").val("");
			jQuery("#userDetails").hide();
			turnOnEditClick();
		},
		error: function() {
			alert("Error deactivating user");
		}
	});
}

function turnOffEditClick() {
	jQuery("#actions #edit").off("click");
	jQuery("#actions #edit").hover(function() {
		jQuery(this).css("cursor", "default");
	});
}

function turnOnEditClick() {
	jQuery("#actions #edit").on("click", showEditUser);
	jQuery("#actions #edit").hover(function() {
		jQuery(this).css("cursor", "pointer");
	});
}

function hideResult() {
	if(jQuery("#userResult").is(":visible")) {
		jQuery("#userResult").hide();
	}
}

function showResult() {
	jQuery("#userResult #result").css("display", "block");
	jQuery("#userResult").show();
}

