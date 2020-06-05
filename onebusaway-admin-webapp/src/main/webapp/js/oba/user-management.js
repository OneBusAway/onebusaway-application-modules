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
                    showUserDetails(response);
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

    //Inactivate image click
    jQuery("#actions #inactivate").on("click", inactivateUser);

    //Inactivate image click
    jQuery("#actions #activate").on("click", activateUser);

	//Delete image click
	jQuery("#actions #delete").on("click", showDeleteDialog);
	
});

function showEditUser() {
	jQuery("#editUser").show();
	jQuery("#editUser #editSubmit").click(editUser);

	hideResult();
}

function editUser() {
	
	var userData = new Object();
	userData.id = jQuery("#userDetails #userId").val();
	//console.log(jQuery("#userDetails #userId"));
	userData.username = jQuery("#editUser #editUserName").text();
	userData.password = jQuery("#editUser #newPassword").val();
	userData.role = jQuery("#editUser #newRole option:selected").val();
	// these are provided by sec:csrfMetaTags
	var csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
	var csrfHeader = $("meta[name='_csrf_header']").attr("content");
	var csrfToken = $("meta[name='_csrf']").attr("content");
	var data = {};
	data[csrfParameter] = csrfToken;
	data["userData"] = JSON.stringify(userData);

	//make sure a role is selected before calling server to edit the user
	if (!userData.role || 0 === userData.role.length) {
		alert("Role is required");
	}
	else {
        jQuery.ajax({
            url: "manage-users!editUser.action",
            type: "POST",
            dataType: "json",
            data: data,
            traditional: true,
            success: function (response) {
                jQuery("#userResult #result").text(response);
                showResult();
            },
            error: function () {
                alert("Error updating user");
            }
        });
    }
}

function showDeleteDialog() {
	if(jQuery("#editUser").is(":visible")) {
		jQuery("#editUser").hide();
	}
	turnOffEditClick();
	hideResult();
	
	var userName = jQuery("#userDetails #username").text();
	var deleteDialog = jQuery("<div id='deleteConfirm'>" +
			"<div><label>Are you sure you want to delete user: </label>" +
			"<label id='deleteMessage'>" +userName + "</label></div>" +
			"<div id='deleteButtons'><button id='deleteSubmit'>OK</button>" +
			"<label id='deleteCancel'>Cancel</label></div></div>")
			.dialog({
				title: "Delete User?",
				autoOpen: false,
				height: "auto",
				width: "auto",
				position: [500,125]	
			});
	
	deleteDialog.dialog('open');
	
	var confirmButton = deleteDialog.find("#deleteSubmit");
	confirmButton.on("click", {"dialog": deleteDialog}, deleteUser);
	
	var cancelButton = deleteDialog.find("#deleteCancel");
	cancelButton.on("click", function(){
		deleteDialog.dialog('close');
		turnOnEditClick();
	});
}

function deleteUser(event) {
    var deleteDialog = event.data.dialog;

    var userData = new Object();
    userData.id = jQuery("#userDetails #userId").val();
    userData.userName = jQuery("#userDetails #username").text();
	// these are provided by sec:csrfMetaTags
	var csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
	var csrfHeader = $("meta[name='_csrf_header']").attr("content");
	var csrfToken = $("meta[name='_csrf']").attr("content");
	var data = {};
	data[csrfParameter] = csrfToken;
	data["userData"] = JSON.stringify(userData);


	jQuery.ajax({
        url:"manage-users!deleteUser.action",
        type: "POST",
        dataType: "json",
        data : data,
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

function inactivateUser() {
	
	var userData = new Object();
	userData.id = jQuery("#userDetails #userId").val();
	userData.userName = jQuery("#userDetails #username").text();
	// these are provided by sec:csrfMetaTags
	var csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
	var csrfHeader = $("meta[name='_csrf_header']").attr("content");
	var csrfToken = $("meta[name='_csrf']").attr("content");
	var data = {};
	data[csrfParameter] = csrfToken;
	data["userData"] = JSON.stringify(userData);


	jQuery.ajax({
		url:"manage-users!inactivateUser.action",
		type: "POST",
		dataType: "json",
		data : data,
		traditional: true,
		success: function(response) {
			jQuery("#userResult #result").text(response);
			showResult();
			jQuery("#search #searchUser").val("");
			jQuery("#userDetails").hide();
			turnOnEditClick();
            showUserToEdit(userData.userName);
		},
		error: function() {
			alert("Error inactivating user");
		}
	});
}

function activateUser() {

    var userData = new Object();
    userData.id = jQuery("#userDetails #userId").val();
    userData.userName = jQuery("#userDetails #username").text();
	// these are provided by sec:csrfMetaTags
	var csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
	var csrfHeader = $("meta[name='_csrf_header']").attr("content");
	var csrfToken = $("meta[name='_csrf']").attr("content");
	var data = {};
	data[csrfParameter] = csrfToken;
	data["userData"] = JSON.stringify(userData);


	jQuery.ajax({
        url:"manage-users!activateUser.action",
        type: "POST",
        dataType: "json",
        data : data,
        traditional: true,
        success: function(response) {
            jQuery("#userResult #result").text(response);
            showResult();
            jQuery("#search #searchUser").val("");
            jQuery("#userDetails").hide();
            turnOnEditClick();
            showUserToEdit(userData.userName);
        },
        error: function() {
            alert("Error activating user");
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

function showUserDetails(response) {
    jQuery("#userDetails #userId").val(response.id);
    jQuery("#userDetails #username").text(response.username);
    var userRole = response.role;
    jQuery("#userDetails #userRole").text(userRole.split('_')[1]);
    jQuery("#userDetails #disabled").text(response.disabled);
    jQuery("#userDetails").show();
    var isDisabled = response.disabled;
    if(isDisabled) {
        jQuery("#inactive").show();
        jQuery("#activate").show();
        jQuery("#inactivate").hide();
    }
    else {
        jQuery("#inactive").hide();
        jQuery("#activate").hide();
        jQuery("#inactivate").show();
    }
    jQuery("#editUser #editUserName").text(response.username);
    jQuery("#editUser #newPassword").val("");
    jQuery("#editUser #newRole").val(userRole).attr("selected", true);
}

function showUserToEdit(username) {
    /* if we've come to this page from list-users, we might have a username
     * if we do, set it and get User Details for editing
     */
    var uname = username;
    if(!!username) {
        jQuery.ajax({
            url: "../../api/users/getUserDetails",
            data: {"user" : uname},
            type: "GET",
            async: false,
            success: function(response) {
                showUserDetails(response);
            },
            error: function(request) {
                alert("There was an error processing your request. Please try again.");
            }
        });
    }
}