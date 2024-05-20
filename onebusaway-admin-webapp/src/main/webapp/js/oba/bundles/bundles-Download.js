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
