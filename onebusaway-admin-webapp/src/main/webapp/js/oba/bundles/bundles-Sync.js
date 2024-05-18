function init() {

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
