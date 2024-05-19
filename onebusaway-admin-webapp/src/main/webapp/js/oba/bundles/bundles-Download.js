function initDownload() {
    //Handle download button click event
    jQuery("#downloadBundle_downloadButton").click(onDownloadBundleClick);

}

function enableDownloadButton() {
    jQuery("#downloadBundle_downloadButton").removeAttr("disabled").css("color", "#000");
}

function disableDownloadButton() {
    jQuery("#downloadBundle_downloadButton").attr("disabled", "disabled").css("color", "#999");
}


//download the specified bundle
function onDownloadBundleClick() {
    var downloadDataset = selectedDirectory;
    var downloadFileName = $("#Download #download_bundleName").text();
    window.location='manage-bundles!downloadBundle.action'
        + '?downloadDataSet=' + downloadDataset
        + '&downloadFilename=' + downloadFileName
}
