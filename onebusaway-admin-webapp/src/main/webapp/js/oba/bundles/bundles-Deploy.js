function init() {

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
