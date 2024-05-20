function initDeploy() {
    //toggle bundle deploy progress list
    jQuery("#deployBundle #deployBundle_progress #expand").bind({
        'click' : toggleDeployBundleResultList});

    //Handle deploy list button click event
    jQuery("#deployBundle_listButton").click(onDeployListClick);
    jQuery("#deployBundle_listCurrentButton").click(deployListBundles);

    onDeployListClick();

    // for deploying named bundle
    $("#DeployingPopup").dialog({
        autoOpen: false,
        modal: true,
        width: 'auto',
        buttons: [{
            id: "deployingSuccessCancel",
            text: "Continue",
            click: function() {
                $(this).dialog("close");
            }
        }],
        open: function() {
            $('.ui-dialog-buttonpane').find('button:contains("Continue")').addClass('cancelDeletePopup');
        }
    });

    // For Delete deployment
    $("#deleteDeployPopup").dialog({
        autoOpen: false,
        modal: true,
        width: 'auto',
        buttons: [{
            id: "deleteDeployCancel",
            text: "Cancel",
            click: function() {
                $(this).dialog("close");
            }
        },
            {
                id: "deleteDeployContinue",
                text: "Delete dataset",
                click: function() {
                    $(this).dialog("close");
                    onDeleteDeployConfirmed();
                }
            }],
        open: function() {
            $('.ui-dialog-buttonpane').find('button:contains("Cancel")').addClass('cancelDeletePopup');
        }
    });

    // For "Delete Success" popup to confirm the directory was deleted
    $("#deleteDeploySuccessPopup").dialog({
        autoOpen: false,
        modal: true,
        width: 'auto',
        buttons: [{
            id: "deleteDeploySuccessCancel",
            text: "Continue",
            click: function() {
                $(this).dialog("close");
            }
        }],
        open: function() {
            $('.ui-dialog-buttonpane').find('button:contains("Continue")').addClass('cancelDeletePopup');
        }
    });


}

function onDeleteDeployedClick() {
    var continueDelete = $("#deleteDeployPopup").dialog("open");
}

function onDeployContinueClick() {
    var $tabs = jQuery("#tabs");
    // load some data when tab becomes active
    jQuery("#deployBundle_listButton").click();
    jQuery("#deployBundle_listCurrentButton").click();
    $tabs.tabs('select', 6);
}

function enableDeployButton() {
    jQuery("#deployBundle_deployButton").removeAttr("disabled").css("color", "#000");
    enableContinueButton($("#deploy_continue"));
}

function disableDeployButton() {
    jQuery("#deployBundle_deployButton").attr("disabled", "disabled").css("color", "#999");
    disableContinueButton($("#deploy_continue"));
}


function deployListBundles() {
    // get a list of currently deployed bundles so it can be pruned
    // CSRF token not required on /api/bundle/*
    jQuery.ajax({
        url: "../../api/bundle/list?ts=" +new Date().getTime(),
        type: "GET",
        async: false,
        success: function(data) {
            if (data == undefined) {
                console.log("empty response");
                return;
            }
            if (typeof data=="string") {
                // work around bad header
                data = eval('(' + data + ')');
            }
            jQuery("#deployBundleCurrentTable tbody").empty();

            // iterate over list
            jQuery.each(data.bundles, function(index, value) {
                console.log("value=" + value);
                var deployName = value.name;
                var start = value["service-date-from"];
                var end = value["service-date-to"];
                var updated = value.updated;
                console.log("deployName=" + index + ":" + deployName);
                var newRow = '<tr class="deployResultListRow"> \
					<td class="deployedItemName">' + deployName + '</td> \
					<td class="deployDate">' + start + '</td> \
					<td class="deployDate">' + end + '</td> \
					<td class="deployDate">' + updated + '</td> \
					<td class="deleteDeployedItem">delete</td></tr>';
                jQuery("#deployBundleCurrentTable").append(newRow);
            });
            // apply the onClick listener to the delete column
            jQuery(".deleteDeployedItem").click(onDeleteDeployedClick);

        },
        error: function (request) {
            alert("There was an error processing your request. Please try again.");
        }
    });
}

function onDeployBundleClick(){
    var selectedItem = $(this).closest("tr").find(".deployedItemName").text();
    // give some feedback to the user that the link was clicked
    // this action can be rather slow so this prevents multiple clicks
    // ideally this would be async instead
    $("#DeployingPopup").dialog("open");
    jQuery.ajax({
        url: "../../api/bundle/deploy/name/" + selectedItem + "?ts=" +new Date().getTime(),
        type: "GET",
        async: false,
        success: function(response) {
            var bundleResponse = response;
            if (bundleResponse != undefined) {
                // the header is set wrong for the proxied object, run eval to correct
                if (typeof response=="string") {
                    bundleResponse = eval('(' + response + ')');
                }
                // we treat call as async, and don't parse the response
                // instead we rely on the list to update or for user to give up!
                window.setTimeout(updateDeployStatus, 5000);
            } else {
                alert("Response from Server was not understood");
            }
        },
        error: function(request) {
            alert("There was an error processing your request. Please try again.");
        }
    });
}

function updateDeployStatus() {
    onDeployListClick();
}

function onDeployListClick(){
    var environment = jQuery("#deploy_environment").text();
    jQuery.ajax({
        url: "../../api/bundle/staged/list?ts=" +new Date().getTime(),
        type: "GET",
        async: false,
        success: function(response) {
            var bundleResponse = response;
            if (bundleResponse != undefined) {
                var txt = "<ul>";
                // the header is set wrong for the proxied object, run eval to correct
                if (typeof response=="string") {
                    bundleResponse = eval('(' + response + ')');
                }

                jQuery("#deployStagedCurrentTable tbody").empty();
                jQuery.each(response.bundles, function(index, value) {
                    console.log("value=" + value);
                    var deployName = value.name;
                    var start = value["service-date-from"];
                    var end = value["service-date-to"];
                    var updated = value.updated;
                    console.log("deployName=" + index + ":" + deployName);
                    var newRow = '<tr class="deployResultListRow"> \
					<td class="deployedItemName">' + deployName + '</td> \
					<td class="deployDate">' + start + '</td> \
					<td class="deployDate">' + end + '</td> \
					<td class="deployDate">' + updated + '</td> \
					<td class="deployStagedItem">deploy</td></tr>';
                    jQuery("#deployStagedCurrentTable").append(newRow);
                });
                jQuery(".deployStagedItem").click(onDeployBundleClick);
                deployListBundles();

            }
        },
        error: function(request) {
            jQuery("#DeployingPopup").hide();
            alert("There was an error processing your request. Please try again.");
        }
    });
}
