/*
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

var csrfParameter = "";
var csrfHeader = "";
var csrfToken = "";


jQuery(function() {
    // these are provided by sec:csrfMetaTags
    csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
    csrfHeader = $("meta[name='_csrf_header']").attr("content");
    csrfToken = $("meta[name='_csrf']").attr("content");


    var table = $('#blockSummaryTable').DataTable({
        "paging":false,
        "order": [[ 2, "asc" ]],
        columnDefs: [ { orderable: false, targets: [4,6] }]
    });

    $.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {

        var vehicleIdInput =  table.cell(dataIndex,5).nodes().to$().find('input').val();
        var vehicleIdField = $('#findVehicleField').val().trim();
        var regex = new RegExp(vehicleIdField,"g");

        if(vehicleIdInput.match(regex)){
            return true;
        }
        return false;
    });

    // Search By Block
    $('#findBlockField').keyup(delay(function (e) {
        table
            .column( 0 )
            .search( this.value.trim() )
            .draw();
    },300));

    // Search By VehicleID
    $('#findVehicleField').keyup(delay(function (e) {
        table.draw();
        var valid = hasValidVehicle($(this).val());
        var info = table.page.info();
        if(info && info.recordsDisplay < 1 && valid){
            $('#notAssignedWrapper').show();
        } else {
            $('#notAssignedWrapper').hide();
        }
    }, 300));

    $('#notAssignedApplyButton').click(function (e) {
        var selectedBlock = $('#notAssignedDropDown').val()
        table.rows().eq(0).each( function ( index ) {
            var row = table.row( index );
            var data = row.data();
            var rowBlockIdTag = $.parseHTML(data[0])[0];
            var rowBlockId = rowBlockIdTag.innerText;
            if(rowBlockId == selectedBlock){
                var vehicleId = $('#findVehicleField').val();
                table.cell(index,5).nodes().to$().find('input').val(vehicleId);
                table.draw();
            }
        } );
    })

    $( "#dialog-message" ).dialog({
        autoOpen: false,
        modal: true,
        buttons: {
            OK: function() {
                $( this ).dialog( "close" );
            }
        },
        width: 700
    });

    $('.trips').click(function(e){
        var blockId = $(this).parent().parent().find(".blockId").text();
        var tripsForBlockUrl = '/api/vehicle-assign/trips/block/' + blockId;
        $.ajax({
            url: tripsForBlockUrl,
            type: "GET",
            dataType: 'text json',
            success: function (data) {
                $("#dialog-body tbody").empty();
                if (data) {
                    for (var i = 0; i < data.length; i++) {
                        content = "<tr>";
                        content += "<td>" + data[i].tripId + "</td>";
                        content += "<td>" + data[i].headSign + "</td>";
                        content += "<td>" + data[i].startTime + "</td>";
                        content += "<td>" + data[i].endTime + "</td>";
                        content += "</tr>";
                        $("#dialog-body tbody").append(content);
                    }

                }
                $( "#dialog-message" ).dialog("open");
            }
        });
    })

    $('#vehicleAssignmentForm').on('submit', function(e){
        var form = this;

        // Encode a set of form elements from all pages as an array of names and values
        var params = table.$('input,select,textarea').serializeArray();

        // Iterate over all form elements
        $.each(params, function(){
            // If element doesn't exist in DOM
            if(!$.contains(document, form[this.name])){
                // Create a hidden element
                $(form).append(
                    $('<input>')
                        .attr('type', 'hidden')
                        .attr('name', this.name)
                        .val(this.value)
                );
            }
        });
    });

    $(".close").click(function(e){
        e.preventDefault();
        $(this).parent().parent().find(".checkmark").hide();
        $(this).parent().parent().find(".custom-combobox-input").val("");


    })
});

$( function() {
    $.widget( "custom.combobox", {
        _create: function() {
            this.wrapper = $( "<span>" )
                .addClass( "custom-combobox" )
                .insertAfter( this.element );

            this.element.hide();
            this._createAutocomplete();
            this._createShowAllButton();
        },

        _createAutocomplete: function() {
            var selected = this.element.children( ":selected" ),
                value = selected.val() ? selected.text() : "";

            var name = this.element.parent().find(".vehicleName").first().val();
            var savedValue = this.element.parent().find(".savedVehicleIdValue").first().val();

            this.input = $( "<input>" )
                .appendTo( this.wrapper )
                .val( savedValue )
                .attr( "title", "" )
                .attr("name", name)
                .addClass( "custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left vehicleIdInput" )
                .change(vehicleIdChange)
                .autocomplete({
                    delay: 0,
                    minLength: 0,
                    source: function( request, response ) {
                        $.ajax({
                            url: "/api/vehicle-assign/active-vehicles/list",
                            dataType: "json",
                            data: {
                                q: request.term
                            },
                            success: function( data ) {
                                response( data );
                            }
                        });
                    }
                })
                .tooltip({
                    classes: {
                        "ui-tooltip": "ui-state-highlight"
                    }
                });

            this._on( this.input, {
                autocompleteselect: function( event, ui ) {
                    ui.item.selected = true;
                    this._trigger( "select", event, {
                        item: ui.item
                    });
                },

                autocompletechange: "_removeIfInvalid"
            });
        },

        _createShowAllButton: function() {
            var input = this.input,
                wasOpen = false;

            $( "<a>" )
                .attr( "tabIndex", -1 )
                .tooltip()
                .appendTo( this.wrapper )
                .button({
                    icons: {
                        primary: "ui-icon-triangle-1-s"
                    },
                    text: false
                })
                .removeClass( "ui-corner-all" )
                .addClass( "custom-combobox-toggle ui-corner-right" )
                .on( "mousedown", function() {
                    wasOpen = input.autocomplete( "widget" ).is( ":visible" );
                })
                .on( "click", function() {
                    input.trigger( "focus" );

                    // Close if already visible
                    if ( wasOpen ) {
                        return;
                    }

                    // Pass empty string as value to search for, displaying all results
                    input.autocomplete( "search", "" );
                });
        },

        _removeIfInvalid: function( event, ui ) {
            // Selected an item, nothing to do
            if ( ui.item ) {
                return;
            }

            // Search for a match (case-insensitive)
            var value = this.input.val();
            var valid = hasValidVehicle(value);

            // Found a match, nothing to do
            if ( valid ) {
                return;
            }

            // Remove invalid value
            this.input
                .val( "" )
                .attr( "title", value + " is not a valid vehicle id" )
                .tooltip( "open" );
            this.element.val( "" );
            this._delay(function() {
                this.input.tooltip( "close" ).attr( "title", "" );
            }, 2500 );
            this.input.autocomplete( "instance" ).term = "";
        },

        _destroy: function() {
            this.wrapper.remove();
            this.element.show();
        }
    });

    $(".combobox").combobox();

   $(".ui-autocomplete-input").keypress(function(e) {
        var code = (e.keyCode ? e.keyCode : e.which);
        if(code == 13) { //Enter keycode
            return false;
        }
    });

} );

function hasValidVehicle(value){
    // Search for a match (case-insensitive)
    var valueLowerCase = value.toLowerCase(),
        valid = false;
    $("#hiddenVehicles").children( "option" ).each(function() {
        if ( $( this ).text().toLowerCase() === valueLowerCase ) {
            valid = true;
            return false;
        }
    });
    return valid;
}

function vehicleIdChange(e) {
    var currentVehicleIdVal = $(this).val();
    var savedVehicleIdVal = $(this).parent().parent().find(".savedVehicleIdValue").val();
    var validationField = $(this).parent().parent().parent().next().find(".validation");

    if(currentVehicleIdVal != savedVehicleIdVal) {
        validationField.hide();
    }
};

function delay(callback, ms) {
    var timer = 0;
    return function() {
        var context = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            callback.apply(context, args);
        }, ms || 0);
    };
}