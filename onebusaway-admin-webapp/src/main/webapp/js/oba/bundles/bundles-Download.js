function init() {

}


//download the specified bundle
function onDownloadBundleClick() {
    var downloadDataset = selectedDirectory;
    var downloadFileName = $("#Download #download_bundleName").text();
    window.location='manage-bundles!downloadBundle.action'
        + '?downloadDataSet=' + downloadDataset
        + '&downloadFilename=' + downloadFileName
}
