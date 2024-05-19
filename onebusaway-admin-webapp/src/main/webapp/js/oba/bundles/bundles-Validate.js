function initValidate() {
    //if bundle build name is entered, enable the Validate button
    jQuery("#Validate #prevalidate_bundleName").on("input propertychange", onBundleNameChanged);
    jQuery("#Build #bundleBuildName").on("input propertychange", onBundleNameChanged);

    //initially hide the Validation Progress label
    jQuery("#prevalidate_progress").hide();

    //toggle validation progress list
    jQuery("#prevalidateInputs #prevalidate_progress #expand").bind({
        'click' : toggleValidationResultList});

    //initially disable the Validate button on the Pre-validate tab
    disableValidateButton();

}

function onPrevalidateContinueClick() {
    var $tabs = jQuery("#tabs");
    $tabs.tabs('select', 3);
}

function enableValidateButton() {
    jQuery("#prevalidateInputs #validateBox #validateButton").removeAttr("disabled").css("color", "#000");
}

function disableValidateButton() {
    jQuery("#prevalidateInputs #validateBox #validateButton").attr("disabled", "disabled").css("color", "#999");
}

function onBundleNameChanged() {
    // Changes for Pre-validate tab
    var text = jQuery("#Validate #prevalidate_bundleName").val();
    if (text.length == 0 || selectedDirectory.length == 0) {
        disableValidateButton();
    } else {
        enableValidateButton();
        jQuery("#prevalidateInputs #validateBox #validating").hide();
        jQuery("#prevalidate_progress").hide();
        jQuery("#prevalidate_exception").hide();
        jQuery("#prevalidate_resultList").empty();
        jQuery("#prevalidate_fileList").empty();
    }

    // Changes for Build tab
    clearPreviousBuildResults();
}

function onValidateClick() {
    var bundleDirectory = jQuery("#prevalidate_bundleDirectory").text();
    if (bundleDirectory == undefined || bundleDirectory == null || bundleDirectory == "") {
        alert("missing bundle directory");
        return;
    }
    var bundleName = jQuery("#prevalidate_bundleName").val();
    if (bundleName == undefined || bundleName == null || bundleName == "") {
        alert("missing bundle build name");
        return;
    }
    else if(~bundleName.indexOf(" ")){
        alert("bundle build name cannot contain spaces");
        return;
    }
    else {
        jQuery("#buildBundle_bundleName").val(bundleName);
    }
    disableValidateButton();
    jQuery("#prevalidate_progress").show();
    jQuery("#prevalidate_exception").hide();
    jQuery("#prevalidate_resultList").empty();
    jQuery("#prevalidate_fileList").empty();
    jQuery("#prevalidate_validationProgress").text("Validating ... ");
    jQuery("#prevalidateInputs #validateBox #validating #validationProgress").attr("src","../../css/img/ajax-loader.gif");
    jQuery("#prevalidateInputs #validateBox #validating").show().css("display","inline");
    // TODO consider POST
    jQuery.ajax({
        url: "../../api/validate/" + bundleDirectory + "/" + bundleName + "/create?ts=" +new Date().getTime(),
        type: "GET",
        async: false,
        success: function(response) {
            var bundleResponse = response;
            if (bundleResponse != undefined) {
                jQuery("#prevalidate_id").text(bundleResponse.id);
                if (jQuery("#prevalidate_id").text().length > 0) {
                    jQuery("#prevalidate_id_label").show();
                }
                jQuery("#Build #bundleBuildName").val(bundleName);
                window.setTimeout(updateValidateStatus, 5000);
            } else {
                jQuery("#prevalidate_id").text(error);
                jQuery("#prevalidate_resultList").text("error");
                if (jQuery("#prevalidate_id").text().length > 0) {
                    jQuery("#prevalidate_id_label").show();
                }
                enableValidateButton();
            }
        },
        error: function(request) {
            alert("There was an error processing your request. Please try again.");
            enableValidateButton();
        }
    });
}

function updateValidateStatus() {
    var id = jQuery("#prevalidate_id").text();
    // TODO consider POST
    jQuery.ajax({
        url: "../../api/validate/" + id + "/list?ts=" +new Date().getTime(),
        type: "GET",
        async: false,
        success: function(response) {
            var txt = "<ul>";
            var bundleResponse = response;
            if (bundleResponse == null) {
                jQuery("#prevalidate_validationProgress").text("Complete.");
                jQuery("#prevalidateInputs #validateBox #validating #validationProgress").attr("src","../../css/img/dialog-accept-2.png");
                jQuery("#prevalidate_resultList").html("unknown id=" + id);
            }
            var size = bundleResponse.statusMessages.length;
            if (size > 0) {
                for (var i=0; i<size; i++) {
                    txt = txt + "<li>" + bundleResponse.statusMessages[i] + "</li>";
                }
            }
            if (bundleResponse.complete == false) {
                window.setTimeout(updateValidateStatus, 5000); // recurse
            } else {
                jQuery("#prevalidate_validationProgress").text("Complete.");
                jQuery("#prevalidateInputs #validateBox #validating #validationProgress").attr("src","../../css/img/dialog-accept-2.png");
                updateValidateList(id);
                enableValidateButton();
            }
            txt = txt + "</ul>";
            jQuery("#prevalidate_resultList").html(txt).css("font-size", "12px");
            if (bundleResponse.exception != null) {
                if (bundleResponse.exception.message != undefined) {
                    jQuery("#prevalidate_exception").show().css("display","inline");
                    jQuery("#prevalidate_exception").html(bundleResponse.exception.message);
                }
            }

        },
        error: function(request) {
            clearTimeout(timeout);
            timeout = setTimeout(updateValidateStatus, 10000);
        }
    });
}

//populate list of files that were result of validation
function updateValidateList(id) {
    var data = {};
    data[csrfParameter] = csrfToken;
    data["id"] = id;
    data["ts"] = new Date().getTime();

    jQuery.ajax({
        url: "manage-bundles!fileList.action",
        type: "POST",
        data: data,
        async: false,
        success: function(response) {
            var txt = "<ul>";

            var list = response;
            if (list != null) {
                var size = list.length;
                if (size > 0) {
                    for (var i=0; i<size; i++) {
                        var encoded = encodeURIComponent(list[i]);
                        txt = txt + "<li><a href=\"manage-bundles!downloadValidateFile.action?id="
                            + id+ "&downloadFilename="
                            + encoded + "\">" + encoded +  "</a></li>";
                    }
                }
            }
            txt = txt + "</ul>";
            jQuery("#prevalidate_fileList").html(txt);
            jQuery("#prevalidateInputs #validateBox #validateButton").removeAttr("disabled");
            var continueButton = jQuery("#prevalidate_continue");
            enableContinueButton(continueButton);
        },
        error: function(request) {
            clearTimeout(timeout);
            timeout = setTimeout(function() {
                updateValidateList(id);
            }, 10000);
        }
    });
}
