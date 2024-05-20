/*
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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

function initSync() {
    //Handle sync button click event
    jQuery("#syncBundle_syncButton").click(onSyncDeployedBundleClick);

    $("#Sync #syncProgressDiv").hide();

}


// Sync active bundle with staging
function onSyncDeployedBundleClick() {
    var environment = jQuery("#deploy_environment").text();
    $("#Sync #syncProgressIcon").attr("src", "../../css/img/ajax-loader.gif");
    $("#syncProgressText").text("Syncing bundles in Progress...");
    $("#Sync #syncProgressDiv").show();
    jQuery.ajax({
        url: "sync-bundle!syncBundle.action?ts=" + new Date().getTime(),
        type: "GET",
        async: false,
        success: function(response) {
            var bundleResponse = response;
            $("#Sync #syncProgressIcon").attr("src", "../../css/img/dialog-accept-2.png");
            $("#syncProgressText").text("Syncing bundles complete!");
        },
        error: function(request) {
            alert("There was an error processing your request. Please try again.");
        }
    });
}
